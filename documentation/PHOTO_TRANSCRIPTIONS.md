# Photograph transcription and provenance

All fourteen supplied photographs were visually inspected. Sideways pages were rotated for inspection. These transcriptions preserve readable engineering content, not calendar/keyboard background text. Unreadable/cropped/overwritten content is stated explicitly. Handwriting is not OEM approval.

## 4884.jpg — Fault collection switches

SHA-256: `279c00aa26cc85340c2b9d4fd2e67b944ac8620fc4a06f4e7fe9250c9fa888b5`

Top: MCC emergency stop S26; TMB emergency stop S25; top stop button S24; top LH/RH skirt S15/S16; top LH/RH comb horizontal S11/S12; top LH/RH comb vertical S13/S14; top LH/RH handrail inlet S17/S18; top step upthrust S19; middle stop S29; middle LH/RH skirt S27/S28 (LH overwritten); drive chain S21A (suffix A requires confirmation); emergency brake S22; overcurrent OL1; top step sag S20; top floor plate S23.
Bottom: BMB emergency stop S57; bottom stop button S55; bottom LH/RH skirt S45/S46; bottom LH/RH comb horizontal S41/S42; bottom LH/RH comb vertical S43/S44; bottom LH/RH handrail inlet S47/S48; bottom step upthrust S49; LH/RH step chain S51/S52; LH/RH handrail broken S53/S54; float switch S56; bottom step sag S50; bottom floor plate S58.
Handwritten labels are not an approved wiring schedule. S27 and S21A need confirmation.

## 4885(1).jpg — Control panel identification

SHA-256: `81d782407eb3b8ec177458c5a49c4ebf4aa4ff5388d50d55b8f67827d4e1c753`

K10 brake contactor; K12 VVVF mode contactor; K13 Y-Δ mode contactor; K14 UP contactor (last digit overwritten); K15 DOWN contactor; K16 star contactor; K17 delta contactor; K20 safety contactor; K49 power ON contactor.
Q01 main circuit breaker; Q02 2-pole 230 V input MCB; Q03 1-phase RCBO (lighting; an earlier word is crossed out); Q04 110 V RCBO (safety); Q05 1-pole 230 V MCB (brake supply); Q06 24 V MCB.
K00 phase monitor relay; K03 ON-delay timer (last digit overwritten); K04 surge protection device; K05 MCC fan timer; KR01 inspection relay; KR02 emergency-brake relay (power fail); KR04 emergency-brake relay; KR06 lighting relay.
Pole counts, Q04 supply voltage, K14 and K03 must be checked against the approved panel drawing. These are recorded labels, not universal ratings.

## 4886(1).jpg — Power and safety equipment

SHA-256: `e3bf6a5c2875566aedbbe5d30b3104e6713b4ea9907aeb7a346bfe4349663d9e`

T1 control transformer; E-BK emergency brake; S-BK service brake; PES safety controller; OL1 overcurrent relay; MT1 motor 1; G01 24 V SMPS; G02 brake power supply; G03 emergency-brake power supply; INV VVVF drive; FL02/FL03 three-phase EMI filter / single-phase EMI filter.
Left edges of some identifiers are cropped. The supplied typed schedule restores T1, E-BK, S-BK, G01–G03 and FL02/FL03; this restoration is user-supplied, not independently verified.

## 4887(1).jpg — MPC inputs and outputs

SHA-256: `e37e1a1cb1ebfd39b597e2e517f11fbc99fe4618ad7a574be26b6443ddbbc482`

Notes: handrail speed A/B; auto-start radar sensor.
Inputs: I1 safety feedback; I2 drive chain switch; I3 reset; I4 inspection; I5 UP input; I6 DOWN input; I7 contactor status (written “Contactor stack”); I8 MT1 LH brake switch; I9 MT1 RH brake switch. I10 and I11 have no function written.
Outputs: Q1 UP contactor; Q2 DOWN contactor; Q3 star; Q4 delta; Q5 V3F/VVVF mode; Q6 Y-Δ mode; Q7 brake relay; Q8 lighting; Q9–Q12 inverter control, grouped with no individual functions; BZ1/BZ2 alarm.
Input and output lists are logical terminal references, not physical terminal-block layouts.

## 4888.jpg — MPC input continuation

SHA-256: `fc38ea719ea3025036d9ffd5a6e265aaf3c3fc3b01d290d469bb83889c625a5b`

I12 RMS switch; I13 0.45/0.65 speed-selection switch (first handwritten speed is difficult to read; supplied typed value used); I14 inverter fault; I15/I16 reserved, shown by a shared handwritten “Reserved”; I17 speed sensor A; I18 speed sensor B; I19 top step missing; I20 bottom step missing; I21 LH handrail speed; I22 RH handrail speed; I23 top auto-start; I24 bottom auto-start.
The right-hand list is partly cropped and is transcribed from the fuller 4889.jpg photograph.

## 4889.jpg — Field device identification

SHA-256: `f1ad88307a303caaf7dd58ace5ecfd57cf2f3b4d98fed03babeaa0770c828244`

SK01 top start key switch; SK02 bottom start key switch; RST1 reset button in MCC; RST2 reset key switch top; RST3 reset key switch bottom; B01 speed monitor sensor A; B02 speed monitor sensor B; B03 top step-missing sensor; B04 LH handrail-speed sensor; B05 RH handrail-speed sensor; B06 top auto-start sensor; B11 bottom step-missing sensor; B12 bottom auto-start sensor; BZ1 top buzzer; BZ2 bottom buzzer; INS inspection point; RMS remote-mode switch.
No B07–B10 assignments are supplied; do not infer them.

## 4890.jpg — Fault collection and PES concept

SHA-256: `99ef8c7080cde9e1d8703f2e9652fdae1dd3514452ee912270881c99aaa6f1d7`

Readable cropped notes: collect safety-device switches; wire them in a series loop (one switch/group wording is cropped); send a single combined “Safety OK” output to the main processor controller; provide top and bottom independent loops.
FCB1 fault collection board 1 (top); FCB2 fault collection board 2 (bottom); XS1 top inspection plug; XS2 bottom inspection plug; PES safety-controller board.
“PES Control Unit”: PES is designed to provide multiple protections for escalators and moving walks; following text is cut off. The wiring and loop architecture are conceptual notes only, not a verified safety circuit.

## 4892.jpg — Additional controller terminal notes

SHA-256: `17fa6d335e6a86be8138c960921e43a03baa04823886f6141d6316b4efc7fafc`

X11 floor plate switch 2; X12/X13 blank; X14 manual-reset signal; X15 escalator speed-detection phase A; X16 phase B; X17 upper step-loss detection; X18 lower step-loss detection; X19 left handrail-speed detection; X20 right handrail-speed detection; OP shown as X15–X20 polarity selection.
Y1/M1–Y2/M2 fault output; Y3/M3–Y4/M4 auxiliary-brake output; BIT0, BIT1, BIT2, BIT3 fault binary code (BIT1 handwriting resembles BiT1/BiTi); BM common terminal (handwriting ambiguous); RS485 operation-panel interface.
Controller model, electrical levels, contact types, binary-code interpretation and the ambiguous common/polarity labels are not verified. X terminals must not be conflated with MPC I terminals.

## 4893.jpg — Detector and brake notes

SHA-256: `3156ba787004b69000ef7e7fec87219666606aa630c30f5922b03e9195219777`

Repeated cropped lines: fault output; auxiliary-brake output; BIT0–BIT3 fault binary code; BM common terminal (uncertain); RS485 operation-panel interface.
FCB1/2 fault collection board 1/2; F01 MCC smoke + heat detector; F02 top smoke + heat detector; F03 bottom smoke + heat detector; KR03 auto-lubrication relay; S05 MT1 LH brake-liner switch; S06 MT1 RH brake-liner switch; S07 MT2 LH brake-liner switch; S08 MT2 RH brake-liner switch; EK05 top traffic light; EK15 bottom traffic light.
B01 speed monitor sensor A; B02 speed monitor sensor 2 (4889 identifies it as B); B03 top step-missing sensor; B04 LH handrail-speed sensor; B05 RH handrail-speed sensor; B06 top auto-start; B11 bottom step-missing; B12 bottom auto-start; PAS public announcement system; KR01 inspection relay; S22 emergency-brake switch.
B02 wording differs across photographs; preserve both readings and use A/B only as supplied notation.

## 4904.jpg — Recorded induction motor example

SHA-256: `5e2401e2b285f0cb1ef3aa1729a9436cd223efd4ffa64151d1c7f1a6ad9f0c13`

Three-phase induction motor, example using 15 kW. Heading appears “Montanari Lifts Components Pvt Ltd” (manufacturer lettering not confirmed).
Power 15 kW; frequency 50 Hz; voltage 415 V; current 30.0 A; speed 975 r/min; duty S1; efficiency 91.2% (IE3 annotation difficult to read); approximate weight 163 kg; frame 180; serial appears 256/HE15-0202 (uncertain); connection DELTA; power factor 0.76; protection IP55; insulation F; ambient-temperature field shows °C but no number.
Recorded example only. Never infer an ambient rating or universal escalator specification from this page.

## 4905.jpg — Escalator terminology — page 1

SHA-256: `3fb199c123625b6cff8d38b79063b6d2fe9595b6bd0a564f0f7715fb6f19a808`

Duty cycle: typical daily usage of the escalator. Vertical rise H: floor-level difference at the escalator landings. Incline: angle of the inclined truss section relative to horizontal. Step width A: nominal tread width between skirts. Step-band speed: travelling speed of steps. Theoretical capacity: number of persons theoretically carried in one hour.
Drive station D: drive unit comprises electric motor, service brake, gearbox, and drive wheels for step chain and handrails. Tensioning/return station R: at bottom, where step-chain adjustment is made. Riser height: vertical height difference between adjacent steps. Truss supports S: support at each end with framing/chain reverting angles behind it on the building support beams (middle handwriting crowded).
Handrail clearance: clearance between sidewall and handrail. Pit P: area accommodating the horizontal truss portion. Inclination: passenger travel angle with respect to horizontal.
No numerical clearance or approved dimensional values are recorded.

## 4906.jpg — Escalator terminology — page 2

SHA-256: `c04dbbd93405c18f2d1c000b151efb86ad3eef2e7e9db15f36eef3e26bea9179`

Anti-slide device: on decking between adjacent handrails or at a wall to discourage sliding. Balustrade: enclosure alongside the running steps, comprising decking, inner/outer panels and skirts. Ceiling-intersection guard: guard at the escalator decking/building intersection. Comb: projecting part of the comb plate engaging tread grooves. Comb plate: landing plate supporting combs. Control equipment: controls motion, travel direction, speed and stopping.
Deck/decking extension: balustrade outside the moving handrail, transversely horizontal, including anti-slide devices where applicable. Driving machinery: mechanised power unit driving the escalator. Electromechanical brake: friction shoes on a drum or disc, spring applied and electrically released (one word partly crossed out). Handrail: powered moving rail for passengers to grip. Handrail guard: guard where handrail enters/leaves the balustrade.
These are educational definitions, not approved clearance values or a brake-design specification.

## 4907.jpg — Escalator terminology — page 3

SHA-256: `50075b56fb2f7c4985e9c3b1dba24632ec144a98aabe2b596384a80fee97b121`

Landing: stationary area at entrance/exit. Landing/floor plate: floor-plate portion next to comb plate on the landing; corrected handwriting partly crossed out. Newel: balustrade at the landing where moving handrail changes direction. Number of horizontal/flat steps: measured from the point the first step emerges from the comb plate to the first exposure of the riser of an adjacent step at both landings.
Panel: balustrade portion next to moving stairway occupying vertical space between steps and deck/inner panel. Skirt: immediately adjacent balustrade portion, between steps and deck/inner panel. Step: unit forming the moving stairway. Step chain: main chain to which steps are attached. Step-roller track: track carrying step rollers. Step tread: horizontal grooved part on which passengers stand. Step roller: roller supporting a step. Truss: supporting structure on which components are mounted. Warning band: demarcation of the step nose at floor-plate level. Contact length: horizontal distance between escalator landing point and end of escalator support.
Contact-length endpoints and the panel/skirt wording require the relevant dimensional drawing for precise interpretation.

## 4908.jpg — Annotated cutaway and step sketch

SHA-256: `567593091b10b513d892fc10dc7b8abd8446cdd7b1fd7e2aedcbf0aeff029a90`

Upper drive-station area: control panel, drive unit, upper sprocket, transformer, emergency brake, auxiliary brake, drive chain, main motor, gearbox/driving machine; top and bottom landing/step path; lower sprocket. Handrail, newel, balustrade, decking, sidewall, moving handrail, step rollers and operating pad are labelled.
Safety/device annotations: top LH/RH comb switches, handrail-inlet switches, comb vertical switch, top/middle/bottom skirt switches, step-upthrust, step-sag, top/middle stops, handrail-speed sensor, top radar sensor, bottom radar sensor, floor switches, step-chain switch, handrail-broken switch, handrail guard/entry. Some fine arrows overlap; exact switch mounting coordinates and some LH/RH annotations are uncertain.
Step detail: step tread, cleat, step demarcation line, riser, step chain, and trailing roller/drive-roller wording (roller terminology ambiguous). One “8…” annotation near the lower handrail entry is not legible; no dimension is recovered.
This drawing supports approximate educational locations only, not installation coordinates or a wiring diagram.

## Additional transcription clarification

On 4893.jpg the traffic-light identifiers read EK05/EK15 in the working transcription, but the handwritten K could be L. Verify EK05/EK15 versus EL05/EL15 before relying on the labels. All source text remains user-note authority, not an approved wiring schedule.
