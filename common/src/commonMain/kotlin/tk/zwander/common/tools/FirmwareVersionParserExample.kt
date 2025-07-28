package tk.zwander.common.tools

import tk.zwander.common.data.FirmwareVersionInfo

/**
 * Example usage of the FirmwareVersionParser tool
 */
object FirmwareVersionParserExample {
    
    /**
     * Example of how to fetch and parse firmware version information
     */
    suspend fun exampleUsage() {
        // Example 1: Fetch firmware info for Samsung Galaxy S22+ (SM-S906B) in Europe (EUX)
        val result = FirmwareVersionParser.fetchFirmwareVersionInfo("SM-S906B", "EUX")
        
        if (result.error == null && result.firmwareVersionInfo != null) {
            val firmwareInfo = result.firmwareVersionInfo
            
            // Print summary report
            println(FirmwareVersionParser.generateSummaryReport(firmwareInfo))
            
            // Get the latest 10 versions sorted by rollback count
            val latestVersions = FirmwareVersionParser.getAvailableVersionsSorted(firmwareInfo).take(10)
            println("\nLatest 10 firmware versions:")
            latestVersions.forEach { version ->
                println("${version.versionCode} - Size: ${FirmwareVersionParser.formatFirmwareSize(version.firmwareSize)}")
            }
            
            // Find versions with rollback count of 7 (usually newer versions)
            val rollback7Versions = FirmwareVersionParser.getVersionsByRollbackCount(firmwareInfo, 7)
            println("\nVersions with rollback count 7:")
            rollback7Versions.forEach { version ->
                println("${version.versionCode} - Size: ${FirmwareVersionParser.formatFirmwareSize(version.firmwareSize)}")
            }
            
            // Find versions within a specific size range (1GB to 3GB)
            val sizeFilteredVersions = FirmwareVersionParser.findVersionsBySize(
                firmwareInfo, 
                1024L * 1024L * 1024L, // 1GB
                3L * 1024L * 1024L * 1024L // 3GB
            )
            println("\nVersions between 1GB and 3GB:")
            sizeFilteredVersions.forEach { version ->
                println("${version.versionCode} - Size: ${FirmwareVersionParser.formatFirmwareSize(version.firmwareSize)}")
            }
            
        } else {
            println("Error fetching firmware info: ${result.error?.message}")
        }
    }
    
    /**
     * Example of parsing XML content directly from a string
     */
    fun exampleParseFromString() {
        val xmlContent = """
            <?xml version="1.0" encoding="UTF-8" ?>
            <versioninfo>
                <url>https://fota-cloud-dn.ospserver.net/firmware/</url>
                <firmware>
                    <model>SM-S906B</model>
                    <cc>EUX</cc>
                    <version>
                        <latest o="15">S906BXXSGFYG1/S906BOXMGFYG1/S906BXXSGFYG1</latest>
                        <upgrade>
                            <value rcount='7' fwsize='1938952880'>S906BXXU1AVCJ/S906BOXM1AVCJ/S906BXXU1AVD5</value>
                            <value rcount='5' fwsize='285439519'>S906BXXU6CWH5/S906BOXM6CWH5/S906BXXU6CWH5</value>
                        </upgrade>
                    </version>
                </firmware>
                <polling>
                    <period>1</period>
                    <time>15</time>
                    <range>23</range>
                </polling>
                <ActiveDeviceInfo>
                    <CycleOfDeviceHeartbeat>14</CycleOfDeviceHeartbeat>
                    <ServiceURL>https://ifota-apis.samsungdm.com/device/fumo/deviceheartbeat</ServiceURL>
                </ActiveDeviceInfo>
                <WiFiConnectedPollingInfo>
                    <Cycle></Cycle>
                    <Activated>FALSE</Activated>
                </WiFiConnectedPollingInfo>
            </versioninfo>
        """.trimIndent()
        
        try {
            val firmwareInfo = FirmwareVersionParser.parseFirmwareVersionXmlString(xmlContent)
            println("Parsed firmware info successfully:")
            println("Model: ${firmwareInfo.model}")
            println("Country Code: ${firmwareInfo.countryCode}")
            println("Latest Version: ${firmwareInfo.latestVersion.versionCode}")
            println("Android Version: ${firmwareInfo.latestVersion.androidVersion}")
            println("Available Upgrade Versions: ${firmwareInfo.upgradeVersions.size}")
        } catch (e: Exception) {
            println("Error parsing XML: ${e.message}")
        }
    }
    
    /**
     * Example of advanced filtering and analysis
     */
    suspend fun exampleAdvancedAnalysis() {
        val result = FirmwareVersionParser.fetchFirmwareVersionInfo("SM-S906B", "EUX")
        
        if (result.error == null && result.firmwareVersionInfo != null) {
            val firmwareInfo = result.firmwareVersionInfo
            
            // Analyze firmware size distribution
            val sizeGroups = firmwareInfo.upgradeVersions.groupBy { version ->
                when {
                    version.firmwareSize < 500L * 1024L * 1024L -> "Small (<500MB)"
                    version.firmwareSize < 2L * 1024L * 1024L * 1024L -> "Medium (500MB-2GB)"
                    version.firmwareSize < 4L * 1024L * 1024L * 1024L -> "Large (2GB-4GB)"
                    else -> "Very Large (>4GB)"
                }
            }
            
            println("Firmware size distribution:")
            sizeGroups.forEach { (category, versions) ->
                println("$category: ${versions.size} versions")
            }
            
            // Analyze rollback count distribution
            val rollbackGroups = firmwareInfo.upgradeVersions.groupBy { it.rollbackCount }
            println("\nRollback count distribution:")
            rollbackGroups.toSortedMap(reverseOrder()).forEach { (rollbackCount, versions) ->
                println("Rollback $rollbackCount: ${versions.size} versions")
            }
            
            // Find the largest and smallest firmware versions
            val largestVersion = firmwareInfo.upgradeVersions.maxByOrNull { it.firmwareSize }
            val smallestVersion = firmwareInfo.upgradeVersions.minByOrNull { it.firmwareSize }
            
            println("\nSize extremes:")
            largestVersion?.let { 
                println("Largest: ${it.versionCode} - ${FirmwareVersionParser.formatFirmwareSize(it.firmwareSize)}")
            }
            smallestVersion?.let { 
                println("Smallest: ${it.versionCode} - ${FirmwareVersionParser.formatFirmwareSize(it.firmwareSize)}")
            }
        }
    }
}
