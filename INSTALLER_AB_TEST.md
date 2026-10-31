# Installer A/B review only

Do not merge this branch or promote it to stable. A is the official v4.5.1 release; B restores the installer implementation from 4.5.0 on the same v4.5.1 source base. Both use com.electrokit.app, 4.5.1/code 30 and the existing signing identity. Only the installer source/manifest/provider path and corresponding static regression test differ from baseline (plus this note).

Keep Play Protect enabled. Test A first, then B, using the same phone and download/install source. If Android blocks B, record the exact message and cancel; do not select Install anyway. Restore A through the normal installer afterwards. Do not uninstall your app or clear its data. A same-version replacement is intended, but if Android refuses replacement, stop.

This checks installation classification, not whether self-updates succeed. Capability differences are grouped, so a changed verdict does not identify which individual component triggered it. Hash reputation, scan timing and scanner updates can affect results. No Play Protect clearance is claimed. Do not upload another VirusTotal sample for A: it is the unchanged stable file. B has a different hash.
