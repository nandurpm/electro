# ELECTRO 3.0.2 release

Version 3.0.2 / code 16 uses the new signing identity explicitly authorized by the user after the previous credentials could not be recovered. The application ID remains com.electrokit.app.

The active certificate is recorded in signing/release-certificate.sha256. The published 3.0.1 and original certificates are retained separately as historical records. This release cannot update an installation signed with either previous key. Uninstalling the old app removes its local data.

The new private key and passwords are provided in a separate private backup, outside the repository and public source archive. Future builds must reuse that key. GitHub Actions requires the four ELECTROKIT signing secrets from the new backup; the old secrets fail keystore validation.

Local tests passed: 121 debug/UI tests, 80 release tests, 10 Python tests, and zero lint errors. Signed verification is generated from the actual APK. Physical-device installation and external reference checks remain unrun.
