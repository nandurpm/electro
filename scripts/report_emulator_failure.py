"""Expose a bounded emulator-startup diagnostic in the GitHub check annotation."""
import sys
from pathlib import Path


def main():
    # Check annotations remain useful when the artifact/log-storage endpoint is inaccessible.
    path = Path(sys.argv[1])
    text = path.read_text(errors="replace")[-10000:] if path.is_file() else "No emulator log was created."
    escaped = text.replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A")
    print("::error title=Android emulator startup::" + escaped)


if __name__ == "__main__":
    main()
