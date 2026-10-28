import org.objectweb.asm.*;

import java.nio.file.*;

/**
 * Changes the default map title (Lang.UI_TITLE, "ui.title" in the language file) from "Pl3xMap" to
 * the given text.
 */
public class PatchTitle {
    public static void main(String[] a) throws Exception {
        byte[] in = Files.readAllBytes(Paths.get(a[0]));
        int[] hits = {0};
        ClassReader cr = new ClassReader(in);
        ClassWriter cw = new ClassWriter(cr, 0);
        cr.accept(
                new ClassVisitor(Opcodes.ASM9, cw) {
                    @Override
                    public MethodVisitor visitMethod(
                            int acc, String name, String desc, String sig, String[] ex) {
                        MethodVisitor mv = super.visitMethod(acc, name, desc, sig, ex);
                        if (!name.equals("<clinit>")) return mv;
                        return new MethodVisitor(Opcodes.ASM9, mv) {
                            boolean pending = false;

                            @Override
                            public void visitLdcInsn(Object v) {
                                if ("Pl3xMap".equals(v)) {
                                    pending = true;
                                    return;
                                } // decide when we see where it is stored
                                flush();
                                super.visitLdcInsn(v);
                            }

                            void flush() {
                                if (pending) {
                                    pending = false;
                                    super.visitLdcInsn("Pl3xMap");
                                }
                            }

                            @Override
                            public void visitFieldInsn(
                                    int op, String owner, String fname, String d) {
                                if (pending) {
                                    pending = false;
                                    if (op == Opcodes.PUTSTATIC && fname.equals("UI_TITLE")) {
                                        hits[0]++;
                                        super.visitLdcInsn(a[2]);
                                    } else super.visitLdcInsn("Pl3xMap");
                                }
                                super.visitFieldInsn(op, owner, fname, d);
                            }

                            @Override
                            public void visitInsn(int op) {
                                flush();
                                super.visitInsn(op);
                            }

                            @Override
                            public void visitMethodInsn(
                                    int op, String o, String n, String d, boolean i) {
                                flush();
                                super.visitMethodInsn(op, o, n, d, i);
                            }

                            @Override
                            public void visitTypeInsn(int op, String t) {
                                flush();
                                super.visitTypeInsn(op, t);
                            }

                            @Override
                            public void visitIntInsn(int op, int v) {
                                flush();
                                super.visitIntInsn(op, v);
                            }

                            @Override
                            public void visitVarInsn(int op, int v) {
                                flush();
                                super.visitVarInsn(op, v);
                            }
                        };
                    }
                },
                0);
        if (hits[0] != 1)
            throw new IllegalStateException("expected exactly one title default, found " + hits[0]);
        Files.createDirectories(Paths.get(a[1]).getParent());
        Files.write(Paths.get(a[1]), cw.toByteArray());
        System.out.println("patched UI_TITLE default -> " + a[2]);
    }
}
