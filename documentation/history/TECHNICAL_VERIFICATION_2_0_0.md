# Electro 2.0.0 technical-data verification

## Authority and exact import

All 125 supplied rows were imported as separate records. The five original fields are tested against a separate source TSV: serial, code string, exact message, reset type and group. Serial gaps 112–114 remain; numeric-looking codes 164–168 remain strings. No A/E records are merged. Derived browsing categories, explanations, possible causes and related-topic links are separate and explicitly not approved trigger/wiring logic.

All fourteen photographs were visually inspected. Sideways pages were rotated for inspection. Every readable technical topic, identifier list, motor parameter and labelled sketch region was transcribed, including repeated/cropped notes and blanks, with source filenames and SHA-256 hashes. The original photographs are not raster assets inside the APK. See documentation/PHOTO_TRANSCRIPTIONS.md and the in-app Technical Reference Library.

## Specific uncertainties preserved

- K14 and K03 are overwritten; typed schedule values are retained as supplied, not independently approved.
- Some identifiers on 4886(1).jpg are cropped; the typed schedule supplies their restored readings.
- The first handwritten I13 speed is difficult to read; typed 0.45/0.65 is retained without asserting a configured speed or permitted setting.
- I7 handwriting resembles “contactor stack”; supplied description Contactor Status is identified as the typed interpretation.
- I10/I11 have no supplied function. I15/I16 are marked reserved in the notes. No B07–B10 sensor assignments are inferred.
- Q9–Q12 remain one inverter-control group. No individual output function, voltage, contact type or common wiring is invented.
- S27 is overwritten; the drive-chain suffix in S21A needs confirmation. S22 is linked only as a brake topic: emergency/auxiliary terminology is not treated as proof of identical devices or logic.
- S05–S08 brake-liner wear references are kept distinct from I8/I9 brake-position/limit feedback. Their electrical equivalence is not asserted.
- B02 is written as sensor B in one image and sensor 2 in another. Both readings remain visible.
- EK05/EK15 traffic-light lettering may be EL05/EL15. These identifiers require drawing confirmation.
- Additional X/Y/OP/BIT/BM/RS485 notes have no confirmed controller model. BM and BIT handwriting and OP polarity notation are uncertain; X terminals are not equated with MPC I terminals.
- The motor serial appears 256/HE15-0202; manufacturer/IE3 lettering is not verified. Ambient-temperature value is absent. Recorded example weight is approximately 163 kg, not a universal motor weight.
- Motor V/I/PF imply 16.3886647 kW input and 91.5266755% efficiency at 15 kW shaft output, slightly different from the recorded 91.2%. At 975 rpm, shaft torque is 146.912255 N·m. If six poles are explicitly assumed, 50 Hz gives 1000 rpm synchronous speed and 2.5% slip. Pole count is not a photographed rating.
- Mechanical contact-length endpoints, panel/skirt wording, crowded truss-support wording, roller terminology and overlapping sketch arrows need the actual dimensional/OEM drawing. No unreadable “8…” annotation is recovered as a dimension.

## Engineering calculations

Independent numerical fixtures cover new power, current, efficiency, PF, torque, synchronous speed/slip, frequency/speed, gear speed/reverse ratio, kinematic capacity/step speed, geometry, energy and conversion results. Every scalar field is independently probed with empty, zero, negative, extremely small/large, NaN and malformed values. The pre-existing extreme-input grid remains.

New numerical fields start empty. Examples are explicit educational inputs; their geometry is not claimed to be an actual machine specification. Balanced sinusoidal power models, shaft-versus-input power, entered efficiency/slip, whole-number poles/teeth and the distinction between theoretical capacity and measured throughput are stated. Units are validated through the shared parser; existing generic-parser behaviour remains compatible, while new dimensionless fields reject pasted physical units.

## Diagrams and CBM

The cutaway and 15 markers are original educational artwork grounded in the supplied sketch's approximate functional regions. Marker coordinates are illustration coordinates, not millimetres or job mounting coordinates. The artwork is selectable and supports zoom/pan. Other equipment illustrations are identification aids, not manufacturer construction or wiring drawings. Logical terminal buttons never pretend to be a physical connector pinout.

CBM values and selectable water/communication scenarios are visibly SIMULATED. They do not reproduce the MPC's trigger logic or establish normal/trip thresholds. No live interface is implemented without an authorised protocol, register map and electrical-interface specification.

## Sources reviewed for general context

- ABB Technical Guide No. 7, induction motor fundamentals and nominal power/torque: https://library.e.abb.com/public/e555e5106a3c44e882f48b75a44f6807/Technical_guide_No_7_3AFE64362569_RevD_EN_lowres.pdf
- Schindler Planning Guide, planning/capacity context: https://www.schindler.lu/content/dam/website/lu/docs/schindler-escalators-planning-guide-en.pdf/_jcr_content/renditions/original./schindler-escalators-planning-guide-en.pdf

Reviewed 26 October 2026. These publications do not validate the supplied Johnson-related controller, fault thresholds, wiring or installed motor. The user photographs and supplied dataset retain their own authority. No numerical statutory clearance, certified stopping distance or switch-bypass procedure is supplied.

## Not verified / not claimed

No OEM approval, certified controller behaviour, exact contactor switching sequence, live machine connectivity, field-test qualification or installation compliance. No instructions to bypass, bridge, disable or defeat protective devices. No physical-device, TalkBack speech, real airplane-mode or API-24/recent-emulator run. Executed automated evidence belongs in VALIDATION.md and verification/, not a claim of field certification.
