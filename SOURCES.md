# Technical reference register

The application’s prose and vector artwork are original. Component names, physical facts, equations and interface identifiers are used for technical identification. This is a compact educational reference; it does not include or replace licensed standards, manufacturer datasheets or connector drawings.

Primary references consulted or identified for verification:

- Android build tool compatibility: https://developer.android.com/build/releases/agp-8-7-0-release-notes
- Kotlin/Compose setup: https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler
- USB specification library: https://www.usb.org/documents
- USB Type-C specification R2.0: https://www.usb.org/sites/default/files/USB%20Type-C%20Spec%20R2.0%20-%20August%202019.pdf
- Texas Instruments NE555: https://www.ti.com/lit/ds/symlink/ne555.pdf
- Texas Instruments LM317: https://www.ti.com/lit/ds/symlink/lm317.pdf
- Texas Instruments RTD measurement / CVD: https://www.ti.com/lit/pdf/sbaa275
- Modbus serial-line specification: https://www.modbus.org/modbus-specifications
- Raspberry Pi hardware / GPIO: https://www.raspberrypi.com/documentation/computers/raspberry-pi.html
- BeagleBone Black expansion headers: https://docs.beagleboard.org/boards/beaglebone/black/ch07.html
- Arduino Uno R3: https://docs.arduino.cc/hardware/uno-rev3/
- ST L78 regulators: https://www.st.com/resource/en/datasheet/l78.pdf
- AVR / PIC product pages: https://www.microchip.com/en-us/products/microcontrollers-and-microprocessors
- Standards publishers for design verification: IEC https://webstore.iec.ch/ ; IPC https://www.ipc.org/ ; IEEE https://standards.ieee.org/ ; NFPA https://www.nfpa.org/ ; TIA https://tiaonline.org/

The above are references, not a claim that every legacy pin assignment or every part suffix has been individually certified. Per-page variant notes and the README coverage boundaries are part of the data contract. Verify safety-related engineering use against the current applicable standard and the exact manufacturer documentation.

- VESA DisplayPort Technical Overview (May 2010), archived primary slide deck; PDMI contact assignment: https://web.archive.org/web/20110726000611/http://www.displayport.org/cms/sites/default/files/downloads/DisplayPort_Technical_Overview.pdf

- PS8070 hardware manual, passive-backplane PCI assignment (variant-specific): https://www.square1industries.com/manuals/PS8070.pdf

- Android backup/extraction rules: https://developer.android.com/identity/data/autobackup
- Android adaptive and monochrome icon structure: https://developer.android.com/develop/ui/compose/system/icon_design_adaptive


## 1.0.2 verified memory/reference additions

- PIC10F200/202/204/206 program words and SRAM: Microchip DS40001239F, Table 1 (page 1): https://ww1.microchip.com/downloads/aemDocuments/documents/OTH/ProductDocuments/DataSheets/40001239F.pdf
- PIC12F508/509 memory: Microchip DS41236E, Table 3-1 (page 11): https://ww1.microchip.com/downloads/en/DeviceDoc/41236E.pdf
- PIC12F629/675 program words, SRAM and EEPROM: Microchip DS41190C, feature table (page 1): https://ww1.microchip.com/downloads/en/devicedoc/41190c.pdf
- onsemi 2N3904/2N3906 absolute maximum VCEO and collector current: https://www.onsemi.com/download/data-sheet/pdf/2n3903-d.pdf and https://www.onsemi.com/download/data-sheet/pdf/2n3906-d.pdf . Absolute maxima are stress limits, not recommended operating points.
- Each Parts Finder record now contains its product/family source URL; all 36 are specific references. A family datasheet can cover several part numbers; read the column for the named part.
- Compose test dependencies: https://developer.android.com/develop/ui/compose/testing
- Robolectric SDK compatibility: https://robolectric.org/compatibility_table/
