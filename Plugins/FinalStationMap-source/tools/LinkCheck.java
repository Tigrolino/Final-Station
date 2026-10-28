import org.objectweb.asm.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/**
 * Verifies that every server class, method and field a plugin's classes refer to really exists in
 * the server.
 */
public class LinkCheck {
    static ClassLoader cl;
    static Set<String> problems = new TreeSet<>();
    static int checked = 0;
    static final String EXTRA = System.getProperty("extra");

    public static void main(String[] a) throws Exception {
        // a[0] = dir of plugin classes to scan, a[1] = prefix filter (e.g. net/pl3x/map/bukkit),
        // rest = classpath entries
        List<URL> urls = new ArrayList<>();
        for (int i = 2; i < a.length; i++) {
            Path p = Paths.get(a[i]);
            if (Files.isDirectory(p) && !a[i].endsWith("/classes")) {
                try (Stream<Path> s = Files.walk(p)) {
                    for (Path j : s.filter(x -> x.toString().endsWith(".jar")).toList())
                        urls.add(j.toUri().toURL());
                }
                urls.add(p.toUri().toURL());
            } else urls.add(p.toUri().toURL());
        }
        cl = new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getPlatformClassLoader());
        Path root = Paths.get(a[0]);
        try (Stream<Path> s = Files.walk(root)) {
            for (Path p : s.filter(x -> x.toString().endsWith(".class")).toList()) {
                String rel = root.relativize(p).toString();
                if (!rel.startsWith(a[1])) continue;
                scan(Files.readAllBytes(p), rel);
            }
        }
        System.out.println("references checked: " + checked);
        System.out.println("problems: " + problems.size());
        problems.forEach(System.out::println);
    }

    static boolean interesting(String owner) {
        return owner.startsWith("net/minecraft/")
                || owner.startsWith("org/bukkit/")
                || owner.startsWith("io/papermc/")
                || owner.startsWith("com/mojang/")
                || owner.startsWith("com/destroystokyo/")
                || owner.startsWith("ca/spottedleaf/")
                || (EXTRA != null && owner.startsWith(EXTRA));
    }

    static Class<?> load(String internal) {
        try {
            return Class.forName(internal.replace('/', '.'), false, cl);
        } catch (Throwable t) {
            return null;
        }
    }

    static void scan(byte[] bytes, String where) {
        new ClassReader(bytes)
                .accept(
                        new ClassVisitor(Opcodes.ASM9) {
                            @Override
                            public void visit(
                                    int v,
                                    int acc,
                                    String name,
                                    String sig,
                                    String sup,
                                    String[] itf) {
                                if (sup != null) type(sup, where + " extends");
                                if (itf != null)
                                    for (String i : itf) type(i, where + " implements");
                            }

                            @Override
                            public MethodVisitor visitMethod(
                                    int acc, String mname, String desc, String sig, String[] ex) {
                                String at = where + "#" + mname;
                                return new MethodVisitor(Opcodes.ASM9) {
                                    @Override
                                    public void visitMethodInsn(
                                            int op,
                                            String owner,
                                            String name,
                                            String d,
                                            boolean itf) {
                                        if (owner.startsWith("[")) return;
                                        if (!interesting(owner)) return;
                                        checked++;
                                        Class<?> c = load(owner);
                                        if (c == null) {
                                            problems.add(
                                                    "MISSING CLASS " + owner + "   (" + at + ")");
                                            return;
                                        }
                                        if (!hasMethod(c, name, d, op == Opcodes.INVOKESTATIC))
                                            problems.add(
                                                    "MISSING METHOD "
                                                            + owner
                                                            + "."
                                                            + name
                                                            + d
                                                            + "   ("
                                                            + at
                                                            + ")");
                                    }

                                    @Override
                                    public void visitFieldInsn(
                                            int op, String owner, String name, String d) {
                                        if (!interesting(owner)) return;
                                        checked++;
                                        Class<?> c = load(owner);
                                        if (c == null) {
                                            problems.add(
                                                    "MISSING CLASS " + owner + "   (" + at + ")");
                                            return;
                                        }
                                        if (!hasField(c, name, d))
                                            problems.add(
                                                    "MISSING FIELD "
                                                            + owner
                                                            + "."
                                                            + name
                                                            + " "
                                                            + d
                                                            + "   ("
                                                            + at
                                                            + ")");
                                    }

                                    @Override
                                    public void visitTypeInsn(int op, String t) {
                                        type(t, at);
                                    }

                                    @Override
                                    public void visitLdcInsn(Object v) {
                                        if (v instanceof Type t && t.getSort() == Type.OBJECT)
                                            type(t.getInternalName(), at);
                                    }

                                    @Override
                                    public void visitInvokeDynamicInsn(
                                            String name, String d, Handle bsm, Object... args) {
                                        for (Object o : args)
                                            if (o instanceof Handle h
                                                    && interesting(h.getOwner())) {
                                                checked++;
                                                Class<?> c = load(h.getOwner());
                                                if (c == null) {
                                                    problems.add(
                                                            "MISSING CLASS "
                                                                    + h.getOwner()
                                                                    + "   ("
                                                                    + at
                                                                    + ")");
                                                    continue;
                                                }
                                                boolean ok =
                                                        h.getTag() <= Opcodes.H_PUTSTATIC
                                                                ? hasField(
                                                                        c, h.getName(), h.getDesc())
                                                                : h.getName().equals("<init>")
                                                                        ? hasCtor(c, h.getDesc())
                                                                        : hasMethod(
                                                                                c,
                                                                                h.getName(),
                                                                                h.getDesc(),
                                                                                h.getTag()
                                                                                        == Opcodes
                                                                                                .H_INVOKESTATIC);
                                                if (!ok)
                                                    problems.add(
                                                            "MISSING (lambda) "
                                                                    + h.getOwner()
                                                                    + "."
                                                                    + h.getName()
                                                                    + h.getDesc()
                                                                    + "   ("
                                                                    + at
                                                                    + ")");
                                            }
                                    }
                                };
                            }
                        },
                        0);
    }

    static void type(String t, String at) {
        while (t.startsWith("[")) t = t.substring(1);
        if (t.startsWith("L") && t.endsWith(";")) t = t.substring(1, t.length() - 1);
        if (!interesting(t)) return;
        checked++;
        if (load(t) == null) problems.add("MISSING CLASS " + t + "   (" + at + ")");
    }

    static boolean hasCtor(Class<?> c, String desc) {
        for (var k : c.getDeclaredConstructors())
            if (Type.getConstructorDescriptor(k).equals(desc)) return true;
        return false;
    }

    static boolean hasMethod(Class<?> c, String name, String desc, boolean isStatic) {
        if (name.equals("<init>")) return hasCtor(c, desc);
        Deque<Class<?>> todo = new ArrayDeque<>();
        Set<Class<?>> seen = new HashSet<>();
        todo.add(c);
        while (!todo.isEmpty()) {
            Class<?> k = todo.poll();
            if (k == null || !seen.add(k)) continue;
            try {
                for (var m : k.getDeclaredMethods())
                    if (m.getName().equals(name) && Type.getMethodDescriptor(m).equals(desc))
                        return true;
            } catch (Throwable t) {
                problems.add("UNLOADABLE " + k.getName() + ": " + t);
                return true;
            }
            if (k.getSuperclass() != null) todo.add(k.getSuperclass());
            else if (k.isInterface()) todo.add(Object.class);
            todo.addAll(Arrays.asList(k.getInterfaces()));
        }
        return false;
    }

    static boolean hasField(Class<?> c, String name, String desc) {
        Deque<Class<?>> todo = new ArrayDeque<>();
        Set<Class<?>> seen = new HashSet<>();
        todo.add(c);
        while (!todo.isEmpty()) {
            Class<?> k = todo.poll();
            if (k == null || !seen.add(k)) continue;
            try {
                for (var f : k.getDeclaredFields())
                    if (f.getName().equals(name) && Type.getDescriptor(f.getType()).equals(desc))
                        return true;
            } catch (Throwable t) {
                problems.add("UNLOADABLE " + k.getName() + ": " + t);
                return true;
            }
            if (k.getSuperclass() != null) todo.add(k.getSuperclass());
            else if (k.isInterface()) todo.add(Object.class);
            todo.addAll(Arrays.asList(k.getInterfaces()));
        }
        return false;
    }
}
