// Test file to demonstrate the FirmwareVersionParser tool
// This file shows how the tool works with the actual Samsung firmware XML data

import tk.zwander.common.tools.FirmwareVersionParser

fun main() {
    // The actual XML content from https://fota-cloud-dn.ospserver.net/firmware/EUX/SM-S906B/version.xml
    val xmlContent = """<?xml version="1.0" encoding="UTF-8" ?>
<versioninfo>
	<url>https://fota-cloud-dn.ospserver.net/firmware/</url>
	<firmware>
		<model>SM-S906B</model>
		<cc>EUX</cc>
		<version>
			<latest o="15">S906BXXSGFYG1/S906BOXMGFYG1/S906BXXSGFYG1</latest>
			<upgrade>
				<value rcount='7' fwsize='1938952880'>S906BXXU1AVCJ/S906BOXM1AVCJ/S906BXXU1AVD5</value>
				<value rcount='7' fwsize='260796043'>S906BXXU2AVH9/S906BOXM2AVHB/S906BXXU2AVH9</value>
				<value rcount='5' fwsize='285439519'>S906BXXU6CWH5/S906BOXM6CWH5/S906BXXU6CWH5</value>
				<value rcount='5' fwsize='2872600859'>S906BXXU2BVJA/S906BOXM2BVJA/S906BXXU2BVJA</value>
				<value rcount='5' fwsize='1948835859'>S906BXXS6CWF6/S906BOXM6CWF6/S906BXXS6CWF6</value>
				<value rcount='3' fwsize='2704066741'>S906BXXSBEXFF/S906BOXMBEXFF/S906BXXSBEXFF</value>
				<value rcount='3' fwsize='345161126'>S906BXXUDEXK5/S906BOXMDEXK5/S906BXXUDEXK5</value>
				<value rcount='1' fwsize='314043987'>S906BXXSEFYE3/S906BOXMEFYE3/S906BXXSEFYE3</value>
				<value rcount='5' fwsize='1997727186'>S906BXXS4CWD3/S906BOXM4CWCH/S906BXXU4CWCH</value>
				<value rcount='5' fwsize='2376310615'>S906BXXU3CWBE/S906BOXM3CWBF/S906BXXU3CWBE</value>
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
</versioninfo>"""

    try {
        // Parse the XML content
        val firmwareInfo = FirmwareVersionParser.parseFirmwareVersionXmlString(xmlContent)
        
        // Generate and print the summary report
        println(FirmwareVersionParser.generateSummaryReport(firmwareInfo))
        
        // Demonstrate various filtering capabilities
        println("\n" + "=".repeat(50))
        println("ADVANCED ANALYSIS")
        println("=".repeat(50))
        
        // Get versions sorted by rollback count
        val sortedVersions = FirmwareVersionParser.getAvailableVersionsSorted(firmwareInfo)
        println("\nAll versions sorted by rollback count (newest first):")
        sortedVersions.forEach { version ->
            println("  ${version.versionCode}")
            println("    Rollback: ${version.rollbackCount}, Size: ${FirmwareVersionParser.formatFirmwareSize(version.firmwareSize)}")
        }
        
        // Find versions by rollback count
        println("\nVersions with rollback count 7:")
        val rollback7 = FirmwareVersionParser.getVersionsByRollbackCount(firmwareInfo, 7)
        rollback7.forEach { version ->
            println("  ${version.versionCode} - ${FirmwareVersionParser.formatFirmwareSize(version.firmwareSize)}")
        }
        
        // Find versions by size range (1GB to 3GB)
        println("\nVersions between 1GB and 3GB:")
        val sizeFiltered = FirmwareVersionParser.findVersionsBySize(
            firmwareInfo, 
            1024L * 1024L * 1024L, // 1GB
            3L * 1024L * 1024L * 1024L // 3GB
        )
        sizeFiltered.forEach { version ->
            println("  ${version.versionCode} - ${FirmwareVersionParser.formatFirmwareSize(version.firmwareSize)}")
        }
        
        // Size distribution analysis
        println("\nSize distribution:")
        val sizeGroups = firmwareInfo.upgradeVersions.groupBy { version ->
            when {
                version.firmwareSize < 500L * 1024L * 1024L -> "Small (<500MB)"
                version.firmwareSize < 2L * 1024L * 1024L * 1024L -> "Medium (500MB-2GB)"
                version.firmwareSize < 4L * 1024L * 1024L * 1024L -> "Large (2GB-4GB)"
                else -> "Very Large (>4GB)"
            }
        }
        sizeGroups.forEach { (category, versions) ->
            println("  $category: ${versions.size} versions")
        }
        
    } catch (e: Exception) {
        println("Error parsing XML: ${e.message}")
        e.printStackTrace()
    }
}

