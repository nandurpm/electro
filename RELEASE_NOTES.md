# ELECTRO 4.0.0 — release notes

VersionName: **4.0.0**. VersionCode: **20**. Android application ID: `com.electrokit.app`.

This release carries forward the existing offline ELECTRO workbench, calculators, local reference data and visual design. It incorporates the correctness, lifecycle, input-persistence and accessibility improvements. Numeric edge cases, E-series calculations, SMD code handling and formula boundaries received additional regression coverage.

The build dependency catalog was restored to the last tested combination after concurrent automated dependency merges introduced duplicate TOML keys. The release workflow performs Android tests, lint, signing certificate validation and signed APK verification before publishing downloadable APK and source ZIP files.

**Limitations:** Manufacturer-specific wiring/ratings are not established by illustrative diagrams. Incomplete localization, broader accessibility and architecture migrations remain documented in the audit. Physical-device verification has not been recorded. For upgrade compatibility, keep the same application ID and signing certificate; back up locally stored data before changing signing identity or uninstalling.

## Historical 3.3.0 source status

The 3.3.0/code 19 audit batch was initially prepared as an unsigned source change. Version 4.0.0 is the requested production-release milestone.

## Historical 3.1.0 release notes

ELECTRO 3.1.0 (version code 17) delivers the next workbench UI redesign step, including refreshed workbench surfaces, reusable ELECTRO panels, updated navigation labels and Profile presentation. The existing offline calculators, engineering references, local persistence and application identity remain connected.

Compose UI regression coverage and design-preview validation were updated with the redesigned workbench. The signed release was built and verified by GitHub Actions using the authorized ELECTRO release certificate.

This signing identity is different from older ELECTRO builds, so existing installations may require uninstalling the older app before installing this release; uninstalling removes the older app's local data.

Automated release verification passed for the signed APK, certificate, source package and release publication. Physical-device validation was not performed.
