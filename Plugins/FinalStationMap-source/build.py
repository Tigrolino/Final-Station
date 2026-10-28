"""Build the map fork through the shared repository build tool."""
from pathlib import Path
import subprocess
import sys

tool = Path(__file__).resolve().parents[2] / "Tools/build_plugins.py"
subprocess.run([sys.executable, str(tool), "FinalStationMap", *sys.argv[1:]], check=True)
