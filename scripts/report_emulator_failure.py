"""Expose a bounded emulator-startup diagnostic in the GitHub check annotation."""
import json
import sys
from pathlib import Path


def main():
    # Check annotations remain useful when the artifact/log-storage endpoint is inaccessible.
    for filename in sys.argv[1:]:
        path = Path(filename)
        text = path.read_text(errors="replace")[-10000:] if path.is_file() else "No log was created."
        # ASCII quoting avoids control characters invalidating the workflow command.
        escaped = json.dumps(text, ensure_ascii=True).replace("%", "%25")
        print("::error title=Android CI diagnostics::" + path.name + " " + escaped)


if __name__ == "__main__":
    main()
