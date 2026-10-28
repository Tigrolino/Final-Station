"""
Final Station release zipper.

Run it (double-click, or `py build.py` in this folder), pick what to zip,
enter a version, and the zips land in the right Releases/ subfolder.
"""
import hashlib
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent
SKIP = {"Thumbs.db", "desktop.ini", ".DS_Store"}


def targets():
    """Everything that can be zipped: (label, source folder, output folder, zip name prefix)."""
    items = [("Resource pack", ROOT / "Resourcepack", ROOT / "Releases" / "Resourcepacks",
              "Final-Station-Resourcepack")]
    for d in sorted((ROOT / "Datapacks").iterdir()):
        if d.is_dir() and (d / "pack.mcmeta").is_file():
            items.append((f"Datapack: {d.name}", d, ROOT / "Releases" / "Datapacks", d.name))
    items.append(("Skripts", ROOT / "Skripts", ROOT / "Releases" / "Other", "Final-Station-Skripts"))
    return items


def make_zip(src, out_file):
    """Zip the CONTENTS of src, so pack.mcmeta ends up at the top of the zip."""
    out_file.parent.mkdir(parents=True, exist_ok=True)
    count = 0
    with zipfile.ZipFile(out_file, "w", zipfile.ZIP_DEFLATED) as z:
        for f in sorted(src.rglob("*")):
            if f.is_file() and f.name not in SKIP:
                z.write(f, f.relative_to(src).as_posix())
                count += 1
    return count


def main():
    items = targets()
    print("\n=== Final Station release zipper ===\n")
    for i, (label, *_rest) in enumerate(items, 1):
        print(f"  {i:>2}) {label}")
    print("\nType the numbers you want, separated by spaces (e.g. 1 3 4), or 'a' for all.")
    choice = input("> ").strip().lower()
    if not choice:
        print("Nothing selected.")
        return

    if choice in ("a", "all"):
        picked = items
    else:
        try:
            picked = [items[int(n) - 1] for n in choice.replace(",", " ").split()]
        except (ValueError, IndexError):
            print("Invalid selection, use the numbers from the list.")
            return

    version = input("Version (e.g. 1.0.0): ").strip().lstrip("vV") or "1.0.0"

    print()
    for label, src, out_dir, prefix in picked:
        out_file = out_dir / f"{prefix}-{version}.zip"
        if out_file.exists():
            if input(f"{out_file.name} already exists. Overwrite? (y/n) ").strip().lower() != "y":
                print(f"  skipped {label}")
                continue
        if not (src / "pack.mcmeta").exists() and src.name != "Skripts":
            print(f"  WARNING: {src.name} has no pack.mcmeta")
        n = make_zip(src, out_file)
        print(f"  ✓ {label} -> {out_file.relative_to(ROOT)} ({n} files)")
        sha1 = hashlib.sha1(out_file.read_bytes()).hexdigest()
        print(f"    sha1: {sha1}")
    print("\nDone!")


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        pass
    if sys.stdin.isatty():
        input("\nPress Enter to close...")
