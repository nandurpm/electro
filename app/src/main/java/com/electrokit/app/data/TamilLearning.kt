package com.electrokit.app.data

/** Offline Tamil explanations keyed by stable lesson IDs; technical headings and terms stay English. */
private val tamilBodies = mapOf(
    "multimeter" to listOf(
        "இந்த பாடத்தில் digital multimeter-ன் display, rotary selector, range, COM மற்றும் V/Ω input jacks ஆகியவற்றை அறிந்துகொள்வீர்கள். DC/AC voltage, resistance, continuity, diode, குறைந்த DC current மற்றும் capacitance ஆகிய 7 வகை அடிப்படை சோதனைகளை தனிமைப்படுத்தப்பட்ட low-voltage சாதனங்களில் மட்டுமே பயிற்சி செய்யவும். ஒவ்வொரு பயிற்சிக்கும் மேலே படிப்படியான வழிகாட்டுதல் உள்ளது.",
        "சோதனைக்கு முன் meter மற்றும் test leads-ன் CAT rating, voltage வரம்பு, fuse மற்றும் manual-ஐப் படிக்கவும். Meter case, lead insulation, probe guards மற்றும் battery warning ஆகியவற்றைச் சரிபார்க்கவும். அளவிட வேண்டிய function, range, jack சரியாக உள்ளதா உறுதி செய்யவும். Live மின்கம்பிகளைத் தொடாமல் isolated low-voltage பயிற்சி மூலங்களை மட்டும் பயன்படுத்தவும்.",
        "COM என்பது black lead இணைக்கும் பொதுவான jack. Voltage, resistance, continuity, diode சோதனைக்கு பொதுவாக red lead V/Ω-க்கு செல்லும். Current சோதனைக்கு தனி fused mA அல்லது A jack இருக்கும். Capacitance jack meter-ஐப் பொறுத்து மாறும். Red lead A jack-இல் இருக்கும்போது voltage அளவிடக் கூடாது; short circuit அபாயம் உண்டு.",
        "Auto-range meter, தேர்ந்தெடுத்த function-க்கு பொருத்தமான range-ஐத் தானாகத் தேர்வு செய்யும். Manual-range meter-இல் எதிர்பார்க்கப்படும் அளவைவிட உயர்ந்த range-இல் தொடங்கவும். OL அல்லது 1 என்பது function-ஐப் பொறுத்து overrange அல்லது open circuit-ஐக் குறிக்கலாம். AC மற்றும் DC mode வேறுபட்டவை; சரியான mode தேர்வு செய்யவும்.",
        "தனியாக உள்ள 1.5 V AA battery-யில் V⎓ mode பயன்படுத்தவும். Black probe COM-க்கு, red probe V/Ω-க்கு இணைக்கவும். Black-ஐ negative முனைக்கும் red-ஐ positive முனைக்கும் வைத்தால் பொதுவாக positive reading கிடைக்கும். மாற்றி வைத்தால் negative குறி தோன்றலாம். Battery charge மற்றும் load காரணமாக reading மாறும். Current mode-இல் battery முனைகளை நேரடியாக இணைக்க வேண்டாம்.",
        "உறுதியாக isolated செய்யப்பட்ட குறைந்த AC voltage பயிற்சி மூலத்தை மட்டும் பயன்படுத்தவும். COM மற்றும் V/Ω jack-களில் leads இணைத்து V~ mode மற்றும் சரியான range தேர்வு செய்யவும். AC output முனைகளுக்கு குறுக்காக parallel-ஆக probe வைத்து அளவிடவும். RMS reading waveform மற்றும் frequency-ஐப் பொறுத்து மாறலாம். வீட்டு mains socket-இல் இந்த பயிற்சியைச் செய்ய வேண்டாம்.",
        "முதலில் power OFF செய்து isolate செய்து சேமிக்கப்பட்ட charge-ஐ பாதுகாப்பாக discharge செய்யவும். Resistor-ஐ circuit-இலிருந்து எடுத்து அல்லது ஒரு முனையைப் பிரித்து Ω mode-இல் அளவிடவும். 1 kΩ resistor பொதுவாக 1000 Ω-க்கு அருகில் tolerance-க்குள் இருக்கும். Circuit-இன் parallel paths reading-ஐ மாற்றலாம். Power ON நிலையில் resistance அளவிடக்கூடாது.",
        "Wire-ஐ அனைத்து circuits-லிருந்தும் பிரித்து voltage இல்லை என்பதை உறுதி செய்யவும். Continuity அல்லது buzzer mode-இல் முதலில் இரண்டு probes-ஐ ஒன்றாகத் தொட்டு meter சரியாக வேலை செய்கிறதா பார்க்கவும். பின்னர் தனிமைப்படுத்தப்பட்ட wire-ன் இரு முனைகளையும் சோதிக்கவும். Beep வந்தால் resistance meter threshold-க்குக் கீழே இருக்கலாம்; பாதுகாப்பான earthing அல்லது insulation தரத்தை அது உறுதிசெய்யாது.",
        "Power OFF செய்து diode-ஐ parallel circuit-இலிருந்து isolate செய்யவும். Diode mode தேர்வு செய்து red probe-ஐ anode-க்கும் black probe-ஐ cathode-க்கும் வைக்கவும். சாதாரண silicon diode forward direction-இல் சுமார் 0.5 முதல் 0.8 V காட்டலாம்; reverse direction-இல் OL வரலாம். LED போன்ற சாதனங்கள் மாறுபடும்; diode-ன் உண்மையான polarity marking-ஐ உறுதி செய்யவும்.",
        "குறைந்த ஆற்றலுள்ள AA battery மற்றும் current கட்டுப்படுத்தும் resistor கொண்ட பயிற்சி circuit-ஐ மட்டும் பயன்படுத்தவும். Power OFF நிலையில் meter-ன் fuse, current limit மற்றும் சரியான mA/A jack-ஐ சரிபார்க்கவும். Circuit-ஐ திறந்து meter-ஐ SERIES-ஆக இணைத்த பின்பே battery-ஐ இணைக்கவும். முடிந்ததும் power OFF செய்து red lead-ஐ V/Ω jack-க்கு மாற்றவும். Current mode meter-ஐ supply-க்கு குறுக்காக வைக்க வேண்டாம்.",
        "Meter capacitance அளவிடும் வசதி கொண்டிருந்தால் மட்டுமே இந்த சோதனையை செய்யவும். முதலில் power OFF செய்து capacitor-ஐ manufacturer முறையின்படி discharge செய்யவும்; மீதமுள்ள voltage இல்லை என்பதை சரியான voltage mode-இல் உறுதிசெய்யவும். தேவையானால் capacitor-ஐ circuit-இலிருந்து isolate செய்யவும். Capacitance mode-இல் reading நிலைத்த பின் µF/nF மதிப்பையும் tolerance-ஐயும் ஒப்பிடவும். பெரிய capacitors ஆபத்தான charge வைத்திருக்கலாம்.",
        "V⎓ என்பது DC voltage, V~ என்பது AC voltage, Ω என்பது resistance, buzzer சின்னம் continuity, diode சின்னம் diode-test, A⎓ என்பது DC current, µF/nF என்பது capacitance, Hz என்பது frequency. HOLD, REL, MIN/MAX போன்ற secondary buttons ஒவ்வொரு meter-க்கும் மாறுபடும். உங்கள் meter manual-ஐப் பார்க்கவும்.",
        "Display வெறுமையாக இருந்தால் battery மற்றும் power switch-ஐச் சரிபார்க்கவும். Resistance mode-இல் OL வந்தால் open component, தவறான range அல்லது பழுதான lead காரணமாக இருக்கலாம். Voltage zero என்றால் source இல்லை, contact தவறு அல்லது AC/DC mode தவறாக இருக்கலாம். Current zero என்றால் circuit open, jack தவறு அல்லது fuse பழுதாக இருக்கலாம். Fuse-ஐ bypass செய்ய வேண்டாம்.",
        "Power இல்லாத நிலையில் lead continuity-ஐச் சரிபார்க்கவும். Current-input fuse-ஐ சோதிக்கும் முன் அனைத்து sources-ஐயும் பிரிக்கவும்; manufacturer procedure-ஐ மட்டுமே பின்பற்றவும். சரியான current rating, voltage rating, breaking capacity கொண்ட fuse-ஐ மட்டுமே மாற்று fuse ஆகப் பயன்படுத்தவும். சாதாரண glass fuse எல்லா meter-க்கும் பாதுகாப்பான மாற்றாக இருக்காது.",
        "இந்த பாடம் வீட்டு mains, distribution board, motor drive அல்லது control cabinet-இல் live testing செய்ய அனுமதி அல்ல. அத்தகைய பணிகளுக்கு தகுதியான நபர், approved procedure, safe isolation அல்லது அனுமதிக்கப்பட்ட live-work முறை, சரியான rated meter/leads மற்றும் PPE தேவை. சரியான jack அல்லது CAT rating இல்லாத அளவீடு கடுமையான காயத்துக்குக் காரணமாகலாம்.",
        "சுய சோதனை: Voltage அளவீடு series-ஆ, parallel-ஆ? பதில்: Parallel. Current அளவீடு? பதில்: சரியான fused jack உடன் Series. Power ON circuit-இல் Ω பயன்படுத்தலாமா? வேண்டாம். Continuity beep insulation பாதுகாப்பை உறுதிப்படுத்துமா? இல்லை. Current சோதனை முடிந்தபின் red lead எங்கே? V/Ω jack. Rating சந்தேகமானால்? நிறுத்தி manual-ஐப் பார்க்கவும்."
    ),
        "ethernet" to listOf(
        "Ethernet பொதுவாக RJ45 என அழைக்கப்படும் 8P8C modular connector-ஐப் பயன்படுத்துகிறது. குறிப்பிட்ட plug mating view-இல் contact numbers-ஐ அடையாளம் காணவும்; socket view mirror ஆக இருக்கலாம். உருவாக்கப்பட்ட photograph மூலம் contact numbers-ஐ ஊகிக்காதீர்கள்.",
        "T568A contacts 1–8 வரிசை:\n1 white/green\n2 green\n3 white/orange\n4 blue\n5 white/blue\n6 orange\n7 white/brown\n8 brown",
        "T568B contacts 1–8 வரிசை:\n1 white/orange\n2 orange\n3 white/green\n4 blue\n5 white/blue\n6 green\n7 white/brown\n8 brown",
        "Straight-through cable-க்கு இரு முனைகளிலும் ஒரே scheme பயன்படுத்தவும். Classic 10/100 crossover-க்கு ஒரு முனையில் T568A, மற்ற முனையில் T568B பயன்படுத்தப்படும். Gigabit crossover மற்றும் auto-MDI/MDIX-க்கு கூடுதல் விதிகள் உள்ளன; எல்லா devices-க்கும் crossover தேவை என்று கருதாதீர்கள். Twisted pairs-ஐ ஒன்றாக வைத்திருந்து connector/cable category installation instructions பின்பற்றவும்.",
        "சாதாரண continuity tester பயன்படுத்தும் முன் equipment மற்றும் PoE-யிலிருந்து துண்டிக்கவும். Contacts 1–8 இடையே opens, shorts, reversals சோதிக்கவும். Basic continuity சோதனை split pairs-ஐத் தவறவிடலாம்; CAT5e/CAT6 channel performance-ஐ certify செய்யாது. பொருத்தமான wire-map/certification tester தேவை. Ethernet port வடிவம் ஒரு controller-ன் RS-485 pinout-ஐ நிர்ணயிக்காது."
    ),
    "rs485" to listOf(
        "Half-duplex அமைப்பில் Device 1 ⇄ trunk pair ⇄ Device 2 ⇄ Device 3 என ஒரே differential pair இரு திசைகளுக்கும் பயன்படும். ஒரே நேரத்தில் ஒரு transmitter மட்டுமே இயக்க வேண்டும். Reference/common மற்றும் shield இணைப்புக்கு device manual பார்க்கவும். RS-485 electrical behavior-ஐ வரையறுக்கிறது; universal connector அல்லது application protocol-ஐ அல்ல.",
        "A/B பெயர்கள் vendors இடையே மாறலாம். எழுத்துகள் அல்லது photograph மட்டும் கொண்டு polarity-ஐ முடிவு செய்யாதீர்கள். இரு devices-ன் manuals மற்றும் signal definitions-ஐ ஒப்பிடவும். RJ45, DB9 அல்லது terminal block-இல் RS-485-க்கு universal contact assignment இல்லை.",
        "Full-duplex-இல் transmit மற்றும் receive-க்கு தனித்தனி differential pairs உள்ளன. ஆதரிக்கப்படும் topology-க்கு ஏற்ப transmitter pair, மற்ற device-ன் receiving pair-உடன் இணைகிறது. இது கூடுதல் ground கொண்ட two-wire bus அல்ல. ஒவ்வொரு device-ன் supported mode-ஐச் சரிபார்க்கவும்.",
        "Short stubs கொண்ட trunk/daisy-chain topology பயன்படுத்துவது சிறந்தது. Active transmission line-ன் இரண்டு physical ends-இல் cable impedance மற்றும் transceiver guidance-க்கு ஏற்ப termination அமைக்கவும். 120 Ω பொதுவானது; எல்லா அமைப்புகளுக்கும் அல்ல. Fail-safe bias-ஐ loading, termination மற்றும் transceiver built-in fail-safe behavior-க்கு ஏற்ப கணக்கிட வேண்டும். ஒவ்வொரு node-லும் resistor சேர்க்காதீர்கள்; values-ஐச் சரிபார்க்காமல் நகலெடுக்காதீர்கள்.",
        "USB adapter-க்கு isolation, common reference, driver control மற்றும் பொருந்தும் serial settings தேவைப்படலாம். MAX485 module என்பது USB அல்லது RS-232 interface அல்ல. அதன் UART side-க்கு logic-level supply மற்றும் I/O limits உள்ளன. 3.3 V அல்லது 5 V host இணைக்கும் முன் சரியான chip/module datasheet பார்க்கவும்."
    ),
    "modbus" to listOf(
        "Modbus RTU பொதுவாக RS-485 வழியாக CRC மற்றும் timing rules கொண்ட binary frames பயன்படுத்துகிறது. Baud rate, parity, data/stop bits மற்றும் address பொருந்த வேண்டும். Differential bus மட்டும் Modbus compatibility-ஐ உறுதி செய்யாது. Serial unicast server addresses பொதுவாக 1–247; address 0 broadcast-க்கானது, வழக்கமான response address அல்ல.",
        "Modbus TCP, TCP வழியாக MBAP header உடன் requests அனுப்புகிறது; வழக்கமான port 502. இதில் RTU serial CRC பயன்படுத்தப்படாது. Serial/Ethernet gateway-இல் serial settings மற்றும் unit IDs சரியாக map செய்யப்பட வேண்டும். Ethernet போன்ற connector வடிவத்தை மட்டும் வைத்து controller ports இணைக்காதீர்கள்.",
        "முதலில் physical interface மற்றும் device manuals சரிபார்க்கவும்; பின்னர் addresses, framing, timeouts மற்றும் documented register maps பார்க்கவும். Register numbering conventions மாறலாம். ஒரு register-ன் செயலைக் கண்டறிய தெரியாத register-இல் எழுதாதீர்கள். அனுமதிக்கப்பட்ட equipment-இல் read-only diagnostics பயன்படுத்தவும்."
    ),
    "rs232" to listOf(
        "வழக்கமான PC DTE assignment: 1 DCD; 2 RXD; 3 TXD; 4 DTR; 5 signal ground; 6 DSR; 7 RTS; 8 CTS; 9 RI. Stamped contact numbers மற்றும் வரையறுக்கப்பட்ட mating view பயன்படுத்தவும். DCE assignments மற்றும் null-modem wiring வேறுபடும்; connector gender மட்டும் DTE/DCE-ஐ நிர்ணயிக்காது.",
        "RS-232 என்பது single-ended bipolar signaling interface. இது TTL UART, USB அல்லது RS-485 அல்ல. பொருத்தமான transceiver/adapter பயன்படுத்தி handshake requirements உறுதிப்படுத்தவும்; equipment documentation பின்பற்றவும்."
    ),
    "can" to listOf(
        "High-speed CAN பொதுவாக linear bus-இல் CAN_H மற்றும் CAN_L பயன்படுத்துகிறது. இரண்டு physical ends-இலும் network மற்றும் transceiver specification-க்கு ஏற்ற termination தேவை. High-speed CAN-க்கு ஒவ்வொரு முனையிலும் 120 Ω பொதுவானது; மற்ற CAN physical layers வேறுபடும். Connector assignments ஒவ்வொரு device-க்கும் உரியவை.",
        "Documented bit rate, topology, common-mode மற்றும் grounding limits பின்பற்றவும். USB அல்லது Ethernet போன்ற connector வடிவம் CAN pins-ஐ அடையாளம் காட்டாது. Resistance சோதனைக்கு முன் power off செய்து isolate செய்யவும். அனுமதி மற்றும் protocol definition இல்லாமல் operational equipment-க்கு transmit செய்யாதீர்கள்."
    ),
    "interfaces" to listOf(
        "Connector வடிவம் data rate, alternate modes அல்லது கிடைக்கும் Current-ஐ உறுதி செய்யாது. USB-C, orientation மற்றும் configuration-channel detection பயன்படுத்துகிறது. ஒரு photograph மட்டும் கொண்டு contacts-ஐ ஊகிக்காதீர்கள். சரியான variant-க்கான numbered USB reference pages மற்றும் original USB specifications பார்க்கவும்.",
        "HDMI variants-க்கு வேறுபட்ட contact layouts உள்ளன. DB25 பயன்பாடு interface-ஐப் பொறுத்தது. JST என்பது connector family; universal pinout அல்ல. M8/M12 coding, contact count, orientation மற்றும் device purpose assignments-ஐ மாற்றும். சரியான part number மற்றும் manufacturer drawing பார்க்கவும்.",
        "Board revisions மற்றும் modules மாறுபடும். Logic Voltage, allowable input Current, isolation மற்றும் I/O sourcing அல்லது sinking என்பதை உறுதிப்படுத்தவும். 24 V PLC outputs-ஐ microcontroller pins-க்கு நேரடியாக இணைக்காதீர்கள். Pin label மட்டும் electrical compatibility-க்கான சான்றல்ல.",
        "சில industrial DC sensors-இல் brown supply-க்கும் blue reference-க்கும் black output-க்கும் பயன்படும்; இது universal wiring instruction அல்ல. PNP Current-ஐ source செய்கிறது; NPN Current-ஐ sink செய்கிறது. இணைக்கும் முன் device diagram, load type, supply மற்றும் interface rating சரிபார்க்கவும்."
    )
)

/** Selects lesson prose without changing route IDs, technical headings, or sources.
 * @param page Original English lesson.
 * @param language Persisted Learn-only language code.
 * @return Tamil sections when selected; English remains the fallback.
 */
fun learningSections(page: LearningPage, language: String): List<Pair<String, String>> {
    val translated = tamilBodies[page.id]
    return if (language == "ta" && translated?.size == page.sections.size)
        page.sections.mapIndexed { index, section -> section.first to translated[index] }
    else if (language == "ta" && page.id == "multimeter") page.sections.mapIndexed { index, section ->
        section.first to (multimeterTamil.getOrElse(index) { "சரியான function மற்றும் rating-ஐ உறுதிப்படுத்தி manufacturer manual பின்பற்றவும்." } + "\n\nEnglish technical instructions:\n" + section.second)
    } else page.sections
}

/** Tamil summaries for the expanded practical guide; complete English safety instructions remain visible. */
private val multimeterTamil = listOf(
    "Display, rotary switch, range மற்றும் input jacks பற்றி அறிந்து கொள்ளுங்கள். Isolated low-voltage பயிற்சிகள் மட்டும் செய்யவும்.",
    "Meter மற்றும் leads ratings, CAT markings, insulation, fuse மற்றும் battery எச்சரிக்கையைச் சரிபார்க்கவும். Probe tips-ஐத் தொடாதீர்கள்.",
    "Black lead-க்கு COM பயன்படுத்தவும். Voltage மற்றும் Resistance-க்கு red lead V/Ω-இல் இருக்க வேண்டும். Current jack தனியானது; Voltage சோதனைக்கு அதைப் பயன்படுத்தாதீர்கள்.",
    "Manual range-இல் உயர்ந்த range-இல் தொடங்கவும். Auto range தானாக அளவைத் தேர்வு செய்யும். OL வந்தால் manual-ஐப் பார்க்கவும்.",
    "சிறிய isolated battery-யில் DC Voltage-ஐ parallel-ஆக அளவிடவும். Polarity மாறினால் negative reading வரும்.",
    "Isolated low-voltage AC training source மட்டும் பயன்படுத்தவும். V~ தேர்வு செய்யவும். Household mains இந்தப் பயிற்சியில் இல்லை.",
    "Power off செய்து isolate மற்றும் discharge செய்தபின் Resistor-ஐ Ω mode-இல் அளவிடவும். Powered circuit-இல் Ω பயன்படுத்தாதீர்கள்.",
    "துண்டிக்கப்பட்ட wire-இல் continuity சோதிக்கவும். Beep என்பது insulation அல்லது protective conductor பாதுகாப்புக்கான சான்றல்ல.",
    "Power இல்லாத isolated diode-இல் diode mode பயன்படுத்தவும். Anode-க்கு red, cathode-க்கு black probe. Forward/reverse readings device-ஐப் பொறுத்தவை.",
    "Current-ஐ series-ஆக, சரியான fused jack மற்றும் range-உடன் மட்டுமே அளவிடவும். Connections மாற்றும் முன் power off செய்யவும்; பின்னர் red lead V/Ω-க்கு திருப்பவும்.",
    "Capacitance mode ஆதரிக்கப்படும் meter மட்டும் பயன்படுத்தவும். Power off, discharge மற்றும் stored Voltage இல்லை என்பதை உறுதி செய்தபின் சோதிக்கவும்.",
    "V⎓ என்பது DC Voltage; V~ என்பது AC Voltage; Ω என்பது Resistance. SELECT, HOLD, REL, MIN/MAX செயல்களுக்கு meter manual பார்க்கவும்.",
    "Display, battery, contacts, range, jack மற்றும் fuse-ஐச் சரிபார்க்கவும். Fuse-ஐ ஒருபோதும் bypass செய்யாதீர்கள்.",
    "Sources துண்டிக்கப்பட்ட நிலையில் மட்டும் leads மற்றும் fuse சோதிக்கவும். Manufacturer குறிப்பிடும் அதே type மற்றும் rating கொண்ட fuse பயன்படுத்தவும்.",
    "Building mains, control cabinets அல்லது drives-இல் இந்தப் பாடத்தை வைத்து வேலை செய்யாதீர்கள். Qualification, authorization, PPE மற்றும் approved procedure தேவை.",
    "Voltage: parallel. Current: series. Powered circuit-இல் Ω வேண்டாம். Current சோதனைக்குப் பின் red lead V/Ω-க்கு திருப்பவும். சந்தேகம் இருந்தால் நிறுத்தவும்."
)
