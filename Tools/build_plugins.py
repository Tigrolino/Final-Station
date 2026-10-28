"""Build custom plugins with Java 25 and an existing Paper dependency tree."""
from argparse import ArgumentParser
from pathlib import Path
import hashlib
import os
import shutil
import subprocess
import urllib.request
import zipfile

ROOT = Path(__file__).resolve().parents[1]
PROJECTS = {
    "SpawnGuard": ("SpawnGuard-source", "SpawnGuard-1.3.jar"),
    "StatsPlugin": ("StatsPlugin-source", "StatisticsViewer-1.8.jar"),
    "PlaceableDecor": ("PlaceableDecor-source", "PlaceableDecor-1.1.0.jar"),
    "FinalStationMap": ("FinalStationMap-source", "Pl3xMap-26.3-555-finalstation-3.jar"),
}
DEPENDENCIES = {
    "annotations-26.0.2-1.jar": (
        "https://repo.maven.apache.org/maven2/org/jetbrains/annotations/26.0.2-1/annotations-26.0.2-1.jar",
        "2037be378980d3ba9333e97955f3b2cde392aa124d04ca73ce2eee6657199297"),
    "Pl3xMap-26.2-555.jar": (
        "https://cdn.modrinth.com/data/34T8oVNY/versions/r9x0W3ZW/Pl3xMap-26.2-555.jar",
        "365d37ea0d89892d3ca9c5f4fec7422b4b8b875a06c55371e5c44a34a0ef54c8"),
    "cloud-bukkit-2.0.1.jar": (
        "https://repo.maven.apache.org/maven2/org/incendo/cloud-bukkit/2.0.1/cloud-bukkit-2.0.1.jar",
        "95f50374dae55ff3bd1235e882125adc984678736f32daff65fddf73677b7bcd"),
}


def dependency(name, directory):
    path = directory / name
    url, expected = DEPENDENCIES[name]
    if not path.is_file():
        directory.mkdir(parents=True, exist_ok=True)
        request = urllib.request.Request(url, headers={"User-Agent": "Final-Station-build"})
        with urllib.request.urlopen(request, timeout=60) as response:
            data = response.read()
        if hashlib.sha256(data).hexdigest() != expected:
            raise ValueError(f"Unexpected checksum for {name}")
        path.write_bytes(data)
    if hashlib.sha256(path.read_bytes()).hexdigest() != expected:
        raise ValueError(f"Unexpected checksum for {path}")
    return path


def run(*args):
    subprocess.run([str(arg) for arg in args], check=True)


def compile_java(sources, output, classpath):
    output.mkdir(parents=True, exist_ok=True)
    run("javac", "--release", "25", "-encoding", "UTF-8", "-proc:none", "-classpath",
        os.pathsep.join(map(str, classpath)), "-d", output, *sources)


def write_jar(path, entries):
    path.parent.mkdir(parents=True, exist_ok=True)
    directories = set()
    for name in entries:
        parts = name.split("/")
        directories.update("/".join(parts[:i]) + "/" for i in range(1, len(parts)))
    with zipfile.ZipFile(path, "w", zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
        for name in sorted(set(entries) | directories):
            info = zipfile.ZipInfo(name, (1980, 1, 1, 0, 0, 0))
            info.compress_type = zipfile.ZIP_DEFLATED
            info.external_attr = (0o40755 << 16) | 0x10 if name.endswith("/") else 0o100644 << 16
            archive.writestr(info, entries.get(name, b""))


def files_under(directory):
    return {p.relative_to(directory).as_posix(): p.read_bytes()
            for p in directory.rglob("*") if p.is_file()} if directory.is_dir() else {}


def build(name, args, classpath):
    folder, filename = PROJECTS[name]
    project = ROOT / "Plugins" / folder
    target = (project / "target").resolve()
    if not target.is_relative_to(project.resolve()):
        raise ValueError("Build output must remain inside the project")
    classes = target / "classes"
    if classes.exists():
        shutil.rmtree(classes)
    deps = list(classpath)
    if name == "FinalStationMap":
        base = dependency("Pl3xMap-26.2-555.jar", args.dependency_dir)
        cloud = dependency("cloud-bukkit-2.0.1.jar", args.dependency_dir)
        deps.append(base)
        sources = sorted((project / "src").rglob("*.java"))
    else:
        sources = sorted((project / "src/main/java").rglob("*.java"))
    compile_java(sources, classes, deps)
    if name == "FinalStationMap":
        tools = target / "tools"
        compile_java([project / "tools/PatchTitle.java"], tools, deps)
        original = target / "upstream/Lang.class"
        original.parent.mkdir(parents=True, exist_ok=True)
        with zipfile.ZipFile(base) as archive:
            entries = {i.filename: archive.read(i) for i in archive.infolist()
                       if not i.is_dir() and i.filename != "web/images/og.png"}
            original.write_bytes(archive.read("net/pl3x/map/core/configuration/Lang.class"))
        run("java", "-classpath", os.pathsep.join(map(str, [tools, *deps])), "PatchTitle",
            original, classes / "net/pl3x/map/core/configuration/Lang.class", "Final Station Map")
        with zipfile.ZipFile(cloud) as archive:
            for item in archive.infolist():
                if item.filename.startswith("org/incendo/cloud/bukkit/parser/ItemStackParser") and item.filename.endswith(".class"):
                    entries[item.filename] = archive.read(item)
        entries.update(files_under(project / "overlay"))
        entries["META-INF/FINAL-STATION-NOTICES.md"] = (project / "NOTICE.md").read_bytes()
        entries["META-INF/LICENSE-Pl3xMap"] = (project / "LICENSE.upstream").read_bytes()
    else:
        entries = files_under(project / "src/main/resources")
        entries["META-INF/MANIFEST.MF"] = b"Manifest-Version: 1.0\r\n\r\n"
        entries["META-INF/LICENSE-FinalStation"] = (ROOT / "LICENSE").read_bytes()
    entries.update(files_under(classes))
    output = target / filename
    write_jar(output, entries)
    if args.output_dir:
        args.output_dir.mkdir(parents=True, exist_ok=True)
        shutil.copy2(output, args.output_dir / filename)
    print(output, flush=True)
    return output


def main():
    parser = ArgumentParser(description=__doc__)
    parser.add_argument("plugins", nargs="*", metavar="PLUGIN")
    parser.add_argument("--server-dir", type=Path, default=ROOT / "test/pikmin-build")
    parser.add_argument("--plugin-dir", type=Path, default=ROOT / "Server Copy - plugins")
    parser.add_argument("--dependency-dir", type=Path, default=ROOT / ".cache/dependencies")
    parser.add_argument("--output-dir", type=Path)
    args = parser.parse_args()
    args.plugins = args.plugins or list(PROJECTS)
    if any(name not in PROJECTS for name in args.plugins):
        parser.error("Choose from: " + ", ".join(PROJECTS))
    server = args.server_dir.resolve()
    classpath = sorted(set(server.glob("*.jar")) | set((server / "libraries").rglob("*.jar"))
                       | set((server / "versions").rglob("*.jar")))
    classpath = [p for p in classpath if not p.name.startswith("mojang_")]
    if not any(p.name.startswith("paper-api-") for p in classpath):
        parser.error("--server-dir must contain Paper's extracted libraries and versions directories")
    classpath += list(args.plugin_dir.glob("PlaceholderAPI*.jar"))
    annotations = list((Path.home() / ".m2/repository/org/jetbrains/annotations").rglob("*.jar"))
    annotations += list(args.dependency_dir.glob("annotations-*.jar"))
    if "StatsPlugin" in args.plugins and not annotations:
        annotations = [dependency("annotations-26.0.2-1.jar", args.dependency_dir)]
    classpath += annotations
    if "StatsPlugin" in args.plugins and (not annotations or not any(p.name.startswith("PlaceholderAPI") for p in classpath)):
        parser.error("StatsPlugin requires PlaceholderAPI and JetBrains annotations in the dependency directory or Maven cache")
    for name in args.plugins:
        build(name, args, classpath)


if __name__ == "__main__":
    main()
