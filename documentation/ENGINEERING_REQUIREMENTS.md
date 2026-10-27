# MASTER DEVELOPMENT PROMPT — ELECTRO APP

## 1. Role and Project Objective

Act as a senior Android developer, electrical and electronics R&D engineer, escalator control-system specialist, technical documentation engineer, and professional UI/UX designer.

I have an existing Android application called **ElectroKit**. Upgrade this existing project and rename the app to **Electro**.

The main objective is to build a professional, technically accurate, visually attractive, comprehensive electrical and electronics engineering application with a dedicated **Elevator & Escalator Engineering** module.

I have provided:

1. The existing ElectroKit Android project/source code.
2. Fourteen photographs containing handwritten escalator electrical, mechanical, PLC/MPC, control-panel, sensor, safety and motor engineering information.
3. A complete fault-code database containing 125 records.
4. Existing engineering calculators and application features that must remain operational.

**Do not rebuild the application as an unrelated project. Improve the existing codebase, retaining its calculations, settings, features, and architecture wherever practical.**

The development work must include complete implementation, testing, bug fixing, Android build verification, and professional technical illustrations.

---

## 2. Rename ElectroKit to Electro

Change the user-facing application name throughout the project:

- App display name: **Electro**
- Launcher name: **Electro**
- App toolbar and title
- Splash screen
- About screen
- Home dashboard
- Navigation labels
- Notifications
- Help and documentation
- README, where relevant
- Branding and visual assets

Create a professional, recognizable app icon using an appropriate electrical engineering symbol.

Use a modern electrical/electronics engineering visual identity.

Important: Preserve the existing Android `applicationId`, package structure, signing compatibility, and user data migration unless changing them is technically essential. Renaming the displayed app must not unintentionally create a different installed application.

## 3. New Main Engineering Module

Add a dedicated section:

**Elevator & Escalator Engineering**

Include the following submodules:

1. Escalator Mechanical Engineering
2. Escalator Electrical System
3. Escalator Control Panel
4. MPC Controller — Inputs and Outputs
5. Sensors and Safety Switches
6. Escalator Fault Code Explorer
7. Motor and Drive Engineering
8. Braking Systems
9. Wiring and Component Identification
10. Testing and Troubleshooting
11. Engineering Formulas and Calculations
12. Technical Reference Library

All modules must be connected through logical navigation.

Add global search covering engineering content, component identifiers, controller terminals, and fault codes.

---

## 4. Extract and Implement All 14 Handwritten Photographs

Analyze **every uploaded photograph carefully**.

Correct image orientation, inspect the handwriting, identify terminology, and transcribe all readable technical information.

Do not ignore any photograph or selectively extract only major components.

Create a source traceability system recording which photograph supplies each item.

### A. Mechanical Engineering

Include detailed explanations, high-quality labelled illustrations, locations, functions and applicable safety considerations for:

- Truss
- Balustrade
- Handrail
- Handrail drive
- Handrail inlet
- Handrail guard
- Inner and outer decking
- Skirt panel
- Steps
- Step tread and step riser
- Step chain
- Step chain wheel
- Step rollers
- Step tracks
- Drive chain
- Drive sprockets
- Step sprockets
- Drive station
- Tensioning/return station
- Comb plate
- Comb segments
- Landing plate
- Floor plate
- Newel
- Steps at landing
- Main drive assembly
- Mechanical braking arrangements
- Other components visible in the photographs

Include definitions of:

- Rise height
- Horizontal projection
- Angle of inclination
- Step width
- Nominal speed
- Theoretical passenger capacity
- Truss support points
- Handrail clearance
- Pit dimensions
- Landing area
- Headroom
- Contact length
- Relevant mechanical clearances

Create a complete educational escalator cutaway illustration with selectable labelled components.

Do not invent dimensions or clearances. Use verified OEM or applicable-standard values only where authoritative references are available.

### B. Electrical Control Panel Components

Develop a searchable component identification database.

Include these identifiers and functions from my photographs:

| Identifier | Component |
|---|---|
| T1 | Control Transformer |
| EBK | Emergency Brake |
| SBK1 | Service Brake 1 |
| SBK2 | Service Brake 2 |
| PES | Safety Controller |
| OL1 | Overcurrent Relay |
| MT1 | Motor 1 |
| G01 | 24 V SMPS |
| G02 | Brake Power Supply |
| G03 | Emergency Brake Power Supply |
| NV | VFD Drive |
| FL02/FL03 | Three-phase / Single-phase EMI Filters |
| K10 | Brake Contactor |
| K12 | VVVF Mode Contactor |
| K13 | Star-Delta Mode Contactor |
| K14 | UP Contactor |
| K15 | DOWN Contactor |
| K16 | Star Contactor |
| K17 | Delta Contactor |
| K20 | Safety Contactor |
| K99 | Power ON Contactor |
| Q01 | Main Circuit Breaker |
| Q02 | 2-Pole 230 V Input MCB |
| Q03 | Lighting RCBO |
| Q04 | Safety RCBO |
| Q05 | 230 V Brake Supply MCB |
| Q06 | 24 V MCB |
| K00 | Phase Monitor Relay |
| K03 | ON Delay Timer |
| K04 | Surge Protection Device |
| K05 | MCC Fan Timer |
| KR01 | Inspection Relay |
| KR02 | Emergency Brake Relay — Power Failure |
| KR04 | Emergency Brake Relay |
| KR06 | Lighting Relay |
| FCB1 | Fault Collection Board 1 |
| FCB2 | Fault Collection Board 2 |
| XS1 | Top Inspection Plug |
| XS2 | Bottom Inspection Plug |

The above identifiers are transcribed from the handwritten notes. Treat unclear or potentially inconsistent labels as requiring verification rather than silently changing them.

**Dictionary alignment (3.0.2).** The supplied DMRC drawing code dictionary resolves four of the labels above that were previously read from handwriting. They have been re-coded in the app and are listed here as changed, not silently substituted:

| Previous reading | Dictionary code | Resolution |
|---|---|---|
| E-BK | EBK | Emergency brake; no separate suffix in the dictionary. |
| S-BK | SBK1 and SBK2 | Two service-brake designations, recorded as two entries. |
| INV | NV | NV is the VFD drive. INV is retained separately as the inverter *fault* signal. |
| K49 | K99 | Power ON contactor. |
| EK05 / EK15 | EL05 / EL15 | Top and bottom traffic lights; the handwritten K is an L. |

The same alignment added the dictionary entries that were absent from the app (power and protection devices, controllers, displays, lighting, fans, metering, plugs and signal terminals) and recorded `X1–X10`, `Y5–Y6` and `Q13–Q14` as grouped ranges because the individual point functions are not supplied.

For each component provide:

- Identifier
- Full name
- Function
- Typical installation location
- Input and output connections, where verified
- Electrical operating principle
- Relationship with other components
- Testing and troubleshooting overview
- Technical illustration or schematic symbol
- Safety precautions

### C. MPC Controller Input Terminals

Create an interactive input-terminal identification page.

Include:

| Input | Description |
|---|---|
| I1 | Safety Feedback |
| I2 | Drive Chain Switch |
| I3 | Reset |
| I4 | Inspection |
| I5 | Up Input |
| I6 | Down Input |
| I7 | Contactor Status |
| I8 | Motor 1 LH Brake Switch |
| I9 | Motor 1 RH Brake Switch |
| I12 | Remote Mode Switch (RMS) |
| I13 | 0.45 / 0.65 Speed Selection Switch |
| I14 | Inverter Fault |
| I17 | Speed Sensor A |
| I18 | Speed Sensor B |
| I19 | Top Step Missing Sensor |
| I20 | Bottom Step Missing Sensor |
| I21 | LH Handrail Speed Sensor |
| I22 | RH Handrail Speed Sensor |
| I23 | Top Auto-Start Sensor |
| I24 | Bottom Auto-Start Sensor |

Preserve unconfirmed terminal assignments as **Not Verified** rather than filling gaps.

Build a terminal diagram showing the input numbers and descriptions.

Selecting a terminal should display its function, associated field device, and any verified diagnostic relationship.

### D. MPC Controller Output Terminals

Include:

| Output | Description |
|---|---|
| Q1 | UP Contactor |
| Q2 | DOWN Contactor |
| Q3 | Star |
| Q4 | Delta |
| Q5 | VVVF Mode |
| Q6 | Star-Delta Mode |
| Q7 | Brake Relay |
| Q8 | Lighting |
| Q9–Q12 | Inverter Control |
| BZ1/BZ2 | Alarm |

Do not assign independent functions to Q9, Q10, Q11 or Q12 without an approved wiring or controller document.

Build a controller I/O reference screen, using separate input/output sections and interactive terminal illustrations.

### E. Sensors and Switches

Create an indexed reference for:

- Speed Monitor Sensor A
- Speed Monitor Sensor B
- Top Step Missing Sensor
- Bottom Step Missing Sensor
- LH Handrail Speed Sensor
- RH Handrail Speed Sensor
- Top Auto-Start Sensor
- Bottom Auto-Start Sensor
- Top and Bottom Start Key Switches
- Reset Button
- Top and Bottom Reset Key Switches
- Inspection Points
- Remote Mode Switch
- Brake Limit Switches
- Drive Chain Switch
- Step Chain Switches
- Comb Horizontal Switches
- Comb Vertical Switches
- Skirt Switches
- Handrail Inlet Switches
- Handrail Broken Switches
- Step Upthrust Switches
- Step Sag Switches
- Floor Plate Switches
- Emergency Stops
- Auxiliary Brake Switch
- Motor Thermal Protection
- Phase Monitoring
- Earthquake and Fire/Smoke Signals
- CBM Sensors

Include the handwritten device identifiers such as B01–B12, S-series, SK-series, RST-series, and other labels wherever they can be reliably read.

For each sensor or switch, explain its operating principle, approximate physical installation location, monitored condition, typical signal type when verified, and its relationship with relevant fault codes.

### F. Fault Collection Switches

Create separate sections for:

**Top Landing**

Include emergency stops, skirt switches, comb switches, handrail inlet switches, step upthrust, step sag, drive chain switch, inspection-related switches, brake switches, and floor plate switches.

**Bottom Landing**

Include bottom emergency stops, skirt switches, comb switches, step chain switches, handrail inlet switches, handrail broken switches, step upthrust, step sag, and floor plate switches.

**Machine and Controller**

Include overcurrent protection, safety controller, brake feedback, motor thermal protection, phase monitor and communication-related signals.

Use the exact verified S-series identification numbers.

Some handwritten identifiers may be duplicated or ambiguous. Flag them for review rather than presenting contradictory wiring references as verified facts.

### G. Motor Engineering

Add the photographed three-phase induction motor example:

| Parameter | Recorded value |
|---|---|
| Rated power | 15 kW |
| Frequency | 50 Hz |
| Rated voltage | 415 V |
| Rated current | 30 A |
| Rated speed | 975 rpm |
| Power factor | 0.76 |
| Efficiency | 91.2% |
| Duty | S1 |
| Frame | 180 |
| Protection class | IP55 |
| Insulation class | F |

Mark these as **Recorded Example — Verify Against Motor Nameplate**, not universal escalator specifications.

Include appropriate calculations for:

- Three-phase power
- Apparent and reactive power
- Rated torque
- Synchronous speed
- Slip
- Electrical efficiency
- Motor current
- Frequency and motor speed
- Gear ratio
- Step speed, given validated drive geometry
- Energy consumption

Use correct units and dimensional validation.

### H. Escalator Operating Principles

Create educational, interactive process diagrams for:

- Power ON
- Control power initialization
- Safety readiness
- UP operation
- DOWN operation
- Automatic start operation
- Inspection operation
- Motor drive operation
- Brake application and release
- Fault detection
- Safety-related stopping
- Alarm and warning indication
- Fault reset classifications

Distinguish clearly between educational conceptual diagrams and OEM-verified operating sequences.

Do not imply that the MPC, VVVF drive or contactors replace certified safety functions.

---

## 5. Complete Escalator Fault Code Explorer

Create a professional, offline-capable fault-code database and troubleshooting interface.

**The following 125 records are the authoritative supplied dataset. Do not omit, renumber, merge or replace any record.**

Use the following data fields:

- Original serial number
- Exact error code as a string
- Original error message
- Reset type
- Group
- Optional user-friendly display text
- Possible causes
- Safe inspection guidance
- Related components
- Related controller terminals, only when verified
- Source and verification status

Preserve leading zeros. Numeric-looking fault codes such as `164` and `165` must remain strings and must not be silently converted to E-prefixed codes.

### Complete fault-code dataset

Columns: `SlNo | Code | Error Message | Reset Type | Group`

```text
1|E001|Safety Line Broken|Auto|Error
2|E003|Motor 1 Overheat|Manual|Error
3|E004|Motor 2 Overheat|Manual|Error
4|E005|OCR Relay Fault|Manual|Error
5|E007|M1_Brake Liner Worn Out|Manual|Error
6|E008|M2_Brake Liner Worn Out|Manual|Error
7|E013|Contactor Stuck|Manual|Error
8|E017|Inverter Error|Manual|Error
9|E018|Contactor not engaged|Manual|Error
10|E020|B Float High Level|Auto|Error
11|E036|Station Fire|Manual|Error
12|E037|Override ON|Manual|Alarm
13|E038|Phase Fail / Reversal|Auto|Error
14|E039|MCCB Trip|Manual|Error
15|E048|Surge Protection|Auto|Error
16|E050|Smoke Alarm Bottom|Manual|Error
17|E057|Smoke Alarm Top|Manual|Error
18|E059|Earthquake|Manual|Error
19|E060|TOD Comm Error|Auto|Warning
20|E061|Announcement Comm Error|Auto|Warning
21|E062|Controller Smoke Alarm|Manual|Error
22|E065|CBM Water in pit|Auto|CBM Alarm
23|E066|CBM Cabinet overheat|Auto|CBM Alarm
24|E067|CBM Top Pit Over Heat|Auto|CBM Alarm
25|E068|CBM L bearing overheat|Auto|CBM Alarm
26|E069|CBM R bearing overheat|Auto|CBM Alarm
27|E070|CBM Voltage out of limit|Auto|CBM Alarm
28|E071|CBM Current out of limit|Auto|CBM Alarm
29|E072|CBM L Bearing sensor Fail|Auto|CBM Alarm
30|E073|CBM R Bearing Sensor Fail|Auto|CBM Alarm
31|E080|CBM Motor 1_fixing_Error|Auto|CBM Alarm
32|E081|CBM_Motor_2_fixing_Error|Auto|CBM Alarm
33|E082|CBM speed out of limit|Auto|CBM Alarm
34|E083|CBM speed out of limit Acc|Auto|CBM Alarm
35|E084|CBM speed out of limit Dec|Auto|CBM Alarm
36|E085|CBM_CBM module alarm|Auto|CBM Alarm
37|E086|CBM controller tem sensor alarm|Auto|CBM Alarm
38|E087|CBM environment tem_sensor alarm|Auto|CBM Alarm
39|E088|CBM Top pit tem sensor alarm|Auto|CBM Alarm
40|E090|CBM Comm Failed|Auto|CBM Alarm
41|E101|Overspeed 1.2 Times|Manual|Error
42|E102|Overspeed 1.4 Times|Manual|Error
43|E103|Unintended Reverse|Manual|Error
44|E104|Brake Distance Over Limit|Manual|Error
45|E105|L Handrail Under Speed|Manual|Error
46|E106|R Handrail Under Speed|Manual|Error
47|E107|T Step Missing|Manual|Error
48|E108|B Step Missing|Manual|Error
49|E112|Start w/o AB Pulse|Manual|Error
50|E126|Under speed|Manual|Error
51|E127|Sensor Fail L Handrail|Manual|Error
52|E128|Sensor Fail R Handrail|Manual|Error
53|E132|Reset Button Stuck|Manual|Error
54|E149|R Brake Open Fail M 1|Manual|Error
55|E150|L Brake Open Fail M 1|Manual|Error
56|E151|R Brake Open Fail M 2|Manual|Error
57|E152|L Brake Open Fail M 2|Manual|Error
58|E153|R Brake Close Fail M 1|Manual|Error
59|E154|L Brake Close Fail M 1|Manual|Error
60|E155|R Brake Close Fail M 2|Manual|Error
61|E156|L Brake Close Fail M 2|Manual|Error
62|E200|E-Stop MCC|Manual|Error
63|E201|E-stop TMB|Manual|Error
64|E202|Stop Button Top|Auto|Error
65|E208|T L Comb Horizontal|Manual|Error
66|E209|T R Comb Horizontal|Manual|Error
67|E210|T L Comb Vertical|Manual|Error
68|E211|T R Comb Vertical|Manual|Error
69|E212|T L Handrail Inlet|Auto|Error
70|E213|T R Handrail Inlet|Auto|Error
71|E214|T Step Upthrust|Manual|Error
72|E216|Auxiliary Brake Switch|Auto|Error
73|E217|Drive Chain Switch|Manual|Error
74|E219|T L Skirting Switch|Auto|Error
75|E220|T R Skirting Switch|Auto|Error
76|E221|T M L Skirting switch|Auto|Error
77|E222|T M R Skirting switch|Auto|Error
78|E230|T Step Sag switch|Manual|Error
79|E231|T_Floor Plate Switch|Manual|Error
80|E301|E-Stop BMB|Manual|Error
81|E302|Bottom E-Stop|Auto|Error
82|E305|Mid E-Stop|Auto|Error
83|E308|B L Comb Horizontal|Manual|Error
84|E309|B R Comb Horizontal|Manual|Error
85|E310|B L Comb Vertical|Manual|Error
86|E311|B R Comb Vertical|Manual|Error
87|E312|B_L _H_rail Entry Switch|Auto|Error
88|E313|B_R _H_rail Entry Switch|Auto|Error
89|E314|B Step Upthrust|Manual|Error
90|E316|L Step Chain Switch|Manual|Error
91|E317|R Step Chain Switch|Manual|Error
92|E319|B L Skirting Switch|Auto|Error
93|E320|B R Skirting Switch|Auto|Error
94|E325|B L Handrail Broken|Auto|Error
95|E326|B R Handrail Broken|Auto|Error
96|E330|B Step Sag|Auto|Error
97|E331|B Floor Plate Switch|Auto|Error
98|A1|Top machine room overheat|Auto|CBM Alarm
99|A2|Left bearing overheat|Auto|CBM Alarm
100|A3|Right bearing over heat|Auto|CBM Alarm
101|A4|Voltage out of limit|Auto|CBM Alarm
102|A5|Current out of limit|Auto|CBM Alarm
103|A6|Water in pit|Auto|CBM Alarm
104|A7|CBM module alarm|Auto|CBM Alarm
105|A8|Motor fixing alarm|Auto|CBM Alarm
106|A9|Motor 2 fixing alarm|Auto|CBM Alarm
107|164|Need maintenance|Auto|Warning
108|165|Lubrication Oil Low|Auto|Warning
109|166|FCB1 Comm Error|Auto|Warning
110|167|FCB2 Comm Error|Auto|Warning
111|168|PES Comm Error|Auto|Warning
115|E177|PES Over Speed 120%|Manual|Error
116|E178|PES_Over Speed 140%|Manual|Error
117|E179|PES-Reverse Fault|Manual|Error
118|E180|PES-Brake Distance Fault|Manual|Error
119|E181|PES-L Handrail Fault|Manual|Error
120|E182|PES-R_Handrail Fault|Manual|Error
121|E183|PES-T Step Missing|Manual|Error
122|E184|PES-B_ Step Missing|Manual|Error
123|E185|PES Brake Action failed|Manual|Error
124|E188|PES_External Signal Abnormal|Manual|Error
125|E189|PES Board Hardware Fault|Manual|Error
126|E190|PES_EEPROM Data Wrong|Manual|Error
127|E191|PES_MCU Communication Abnormal|Manual|Error
128|E192|PES_Parameter Abnormal|Manual|Error
```

Serial numbers 112, 113 and 114 are absent from the supplied source. Do not create fictional records for them. Preserve the original serial numbers as provided.

### Fault Explorer Interface

The user must be able to:

- Search by error code or message.
- Filter by Error, Alarm, Warning and CBM Alarm.
- Filter by Auto or Manual reset type.
- Filter by top landing, bottom landing, drive, brake, motor, controller, communication and CBM, where categorization is supported.
- Bookmark frequently used errors.
- View a fault's description and related devices.
- Navigate from a fault to a relevant component page.
- Copy or share a fault reference.
- Access the reference without internet.
- Browse related faults.

For each fault, present a professional detail page containing:

**Fault Code → Error Message → Category → Reset Type → System/Location → Description → Possible Causes → Safe Diagnostic Checks → Related Devices → Technical Reference**

Do not generate speculative root causes as confirmed facts. Label generic diagnostic explanations as possible causes and indicate when OEM verification is required.

The database must distinguish the supplied reset classification from actual permission to restart. An Auto reset classification does not mean that the escalator can safely restart automatically or that an operator can bypass inspection.

### Fault Code UI Example

**E101 — Overspeed 1.2 Times**

- Group: Error
- Reset type: Manual
- System: Escalator motion monitoring
- Description: Recorded overspeed protection fault
- Related systems: Speed sensors, controller, drive and safety monitoring
- Inspection guidance: Qualified-personnel diagnosis according to approved documentation
- Reference status: User-provided fault list

Provide a visually clear fault detail screen with component illustrations and contextual links.

---

## 6. Condition-Based Monitoring (CBM)

Create a dedicated CBM section based on the supplied fault codes and photographs.

Include:

- Machine room/cabinet temperature
- Top pit temperature
- Ambient/environment temperature
- Motor displacement monitoring
- LH and RH bearing temperature
- Speed monitoring
- Voltage monitoring
- Current monitoring
- Energy monitoring
- Water in pit/float switch monitoring
- Sensor failure detection
- CBM communication status
- CBM module status
- Controller-to-CBM communication

Include diagrams where appropriate.

Create an offline educational monitoring dashboard with clearly labelled simulated sample values. Do not suggest that simulated values are live machine data.

For actual device connectivity, only implement a live interface when the communication protocol, register map, electrical interface and authorization are supplied.

---

## 7. Professional UI/UX and Illustrations

The current ElectroKit design is too basic.

I want the Electro app to have a much more professional engineering-oriented design, including:

- High-quality electrical component illustrations
- Realistic industrial equipment representations
- Clean vector engineering diagrams
- Interactive component identification
- Clear wiring schematics
- Escalator cross-section illustrations
- Detailed motor, sensor and contactor visuals
- Modern dashboard cards
- Well-organized technical tables
- Dark and light themes
- Responsive layouts for different Android screen sizes
- Smooth navigation and useful transitions
- Search and filtering throughout the reference library

Do not use decorative illustrations as substitutes for accurate engineering diagrams.

Where possible, use original, scalable SVG/vector drawings and carefully labelled diagrams. Distinguish simplified educational drawings from exact OEM equipment designs.

Support zooming and panning for large drawings.

### Interactive Escalator Diagram

Provide a clean escalator cutaway drawing with selectable components.

Users should be able to select:

- Top machine room
- Drive station
- Motor
- Gearbox
- Brakes
- Drive chain
- Steps
- Step chain
- Handrail
- Comb plate
- Bottom station
- Step sensors
- Handrail sensors
- Safety switches

A selected component opens its information page.

Place markers only where physical locations are adequately supported by drawings or engineering references.

---

## 8. Link Fault Codes, Sensors and Components

Build relationships among the technical databases.

Examples:

**E217 — Drive Chain Switch**

Link to:
- Drive chain mechanical description
- Drive chain safety switch
- Relevant controller input where verified
- Fault category and reset classification

**E105 — L Handrail Under Speed**

Link to:
- LH handrail-speed monitoring
- Relevant speed sensor information
- Escalator motion monitoring
- Possible diagnostic considerations

**E065 — CBM Water in Pit**

Link to:
- Float switch
- CBM monitoring
- Water-level alarm
- Sensor installation overview

Do not assume that similar fault codes share identical trigger logic, even when their descriptions are similar.

Support bidirectional navigation: from components to related faults and from faults to related components.

---

## 9. Calculators and Engineering Tools

Preserve all existing calculators.

Add or improve relevant tools including:

- Three-phase power calculator
- Single-phase power calculator
- Motor current calculator
- Motor efficiency calculator
- Power factor calculator
- Motor torque calculator
- Synchronous speed/slip calculator
- Frequency and speed calculator
- Escalator passenger-capacity calculator
- Escalator nominal-speed reference
- Gear ratio calculator
- Energy consumption calculator
- Electrical unit converter

All calculators must include:

- Input parameters and units
- Formulas
- Calculation steps
- Results
- Unit validation
- Supported unit conversions
- Input error handling
- Engineering assumptions
- Copy/reset options

Fix the previously identified unit-parsing issue. Ensure units such as `m/s`, `oz/ft²`, `°C/W`, `kW` and `mA` are supported correctly wherever they appear in calculator fields.

Do not silently use default assumptions that change the physical meaning of a calculation.

---

## 10. Technical Data Architecture

Store fault codes, components, controller inputs, outputs, sensors and relationships in structured data rather than hardcoding them inside UI screens.

Use the existing project architecture and an appropriate maintainable local persistence solution.

Recommended model entities:

- FaultCode
- Component
- Sensor
- SafetySwitch
- ControllerInput
- ControllerOutput
- MotorSpecification
- TechnicalDiagram
- ReferenceSource
- FaultComponentRelation

Each technical record should be traceable to its source.

Distinguish:

- User-provided records
- Photograph transcription
- Verified manufacturer documentation
- General engineering reference information
- Unverified or ambiguous information

Provide a maintainable method to update reference records without rewriting UI components.

---

## 11. Technical Accuracy and Safety

This application is an engineering reference, not a certified escalator safety controller.

Follow these requirements:

1. Never invent electrical ratings, component positions, terminal assignments, wiring connections or controller logic.
2. Cross-check handwritten information and identify uncertain readings.
3. Maintain a source and verification status for important records.
4. Do not recommend bypassing, bridging or disabling safety switches.
5. Do not give instructions to defeat overspeed protection, brake interlocks or safety controllers.
6. Clearly distinguish normal troubleshooting from safety-critical commissioning procedures.
7. Use applicable safety standards and manufacturer documentation only when verified and relevant.
8. Clearly indicate the difference between generic engineering information and equipment-specific Johnson-related reference material.
9. Do not claim that the application or its diagrams are OEM-approved unless approval is documented.
10. Treat manual reset, auto reset, alarm and warning classifications exactly as the supplied dataset records them.

---

## 12. Preserve and Improve the Existing App

Before implementing modifications:

- Inspect the full existing project.
- Identify the Android architecture, language and UI framework.
- Review all calculators and existing engineering sections.
- Find current bugs and compilation problems.
- Check dependencies and SDK compatibility.
- Inspect current navigation and UI resources.
- Check existing persistence and settings.
- Preserve user data.
- Avoid unnecessary package, database or application-ID changes.

Then implement the requested modules without breaking existing functionality.

Do not remove existing tools simply to make room for the escalator section.

---

## 13. Quality Assurance and Testing

Carry out comprehensive testing.

### Functional testing

Verify that:

- All 125 fault-code records exist.
- Original code strings and leading zeros are preserved.
- All fault groups and reset types match the supplied dataset.
- Fault searches and filters work.
- Relevant component and fault links work.
- Technical reference pages are accessible.
- Diagrams render correctly.
- Existing calculators still work.
- New calculators produce correct results.
- Navigation and back-button behavior work.
- Dark/light themes display properly.
- Offline reference access works.
- No layout clipping, illegible text or missing images remain.

### Engineering verification

- Check calculations against independently computed test values.
- Validate engineering units.
- Identify unverified specifications.
- Flag ambiguous handwritten device identifiers.
- Ensure educational examples are not presented as certified machine configurations.
- Ensure fault descriptions have not been altered unintentionally.

### Build verification

- Run the project's available tests.
- Run Android lint/static checks where practical.
- Fix compilation errors.
- Build a debug APK.
- Build a release artifact when signing configuration permits.
- Document any environment or signing restrictions.

---

## 14. Required Deliverables

Provide:

1. Updated complete Android source project.
2. App renamed to **Electro**.
3. All newly implemented engineering modules.
4. Imported and validated 125-record fault database.
5. Technical content transcribed from all 14 photographs.
6. Professional illustrations and interactive diagrams.
7. Improved home dashboard and navigation.
8. All existing calculators preserved and validated.
9. New electrical and escalator calculators.
10. APK build, if the environment permits.
11. Technical-data verification report.
12. Testing and bug-fix report.
13. Summary of all files added and modified.
14. Remaining uncertainties requiring manufacturer/wiring-diagram verification.

Do not mark a feature as completed if it is a mock-up, placeholder, or non-functional button.

## 15. Final Execution Instructions

**Implement the changes; do not stop after writing a development plan.**

First inspect the project and all 14 photographs, then execute the changes systematically.

Prioritize correct engineering information, functional implementation, maintainability and professional presentation.

Retain the existing ElectroKit functionality while transforming it into the **Electro — Electrical & Electronics Engineering Toolkit**.

The finished app should be useful to electrical engineers, R&D/design engineers, electrical technicians, escalator engineers and engineering students.

Where information is unavailable, preserve the known data, mark the missing information clearly, and proceed with everything that can be safely implemented.

Do not fabricate technical details to make a page appear complete.

**Final requirement:** Build an attractive, technically reliable and genuinely usable engineering app—not a collection of static text screens or basic placeholder drawings.
