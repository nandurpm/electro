# Reference-based Compose UI

The supplied ELECTRO nine-screen reference is the design specification: near-black navy backgrounds, cyan panel edges, electric-blue emphasis, compact rounded instrument panels, vivid category badges, lightning branding, component artwork and a five-destination bottom bar.

The implementation retains the existing navigation and data owners. Home uses an illustrated motor banner and six working shortcuts. Shared ElectroPanel and CategoryBadge components establish consistent panel borders and category markers. The theme supplies typography and shapes to calculator inputs, unit conversion, reference tables, engineering details, learning, dialogs and settings. Calculator results emphasize cyan values. Component art uses theme-aware backgrounds; settings are grouped into bordered panels. The existing light/dark/system preference remains effective.

The reference's portrait and invented equipment ratings are not copied into user data. Existing bundled artwork and authored technical content remain connected to actual routes. The reference's many-phone sheet is adapted into individual scrollable Android pages, with growing text containers rather than fixed screenshot dimensions. Large diagrams retain their existing enlargement controls.

Validation includes existing narrow-screen, large-font, navigation, calculator and persistence regression tests, light/dark render previews and source-derived contrast checks. Robolectric previews are rendering evidence, not physical-device validation. The 3.0.1 GitHub release predates this UI revision. Local debug output is for preview; production publication requires the active published signing key and a version increment.
