package tk.zwander.common.tools

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import io.ktor.client.request.*
import io.ktor.client.statement.*
import tk.zwander.common.data.*
import tk.zwander.common.util.globalHttpClient
import tk.zwander.common.util.firstElementByTagName

/**
 * Tool for parsing Samsung firmware version XML files
 */
object FirmwareVersionParser {
    
    /**
     * Fetch and parse firmware version information from Samsung's servers
     * @param model the device model (e.g., "SM-S906B")
     * @param region the device region (e.g., "EUX")
     * @return FetchResult containing parsed firmware version information
     */
    suspend fun fetchFirmwareVersionInfo(model: String, region: String): FetchResult.FirmwareVersionFetchResult {
        try {
            val response = globalHttpClient.get(
                urlString = "https://fota-cloud-dn.ospserver.net/firmware/${region}/${model}/version.xml"
            ) {
                userAgent("Kies2.0_FUS")
            }

            val responseXml = Ksoup.parse(response.bodyAsText())
            
            if (responseXml.tagName() == "Error") {
                val code = responseXml.firstElementByTagName("Code")?.text() ?: "Unknown"
                val message = responseXml.firstElementByTagName("Message")?.text() ?: "Unknown error"

                return FetchResult.FirmwareVersionFetchResult(
                    error = IllegalStateException("Code: $code, Message: $message"),
                    rawOutput = responseXml.toString()
                )
            }

            return try {
                val firmwareInfo = parseFirmwareVersionXml(responseXml)
                FetchResult.FirmwareVersionFetchResult(
                    firmwareVersionInfo = firmwareInfo,
                    rawOutput = responseXml.toString()
                )
            } catch (e: Exception) {
                FetchResult.FirmwareVersionFetchResult(
                    error = e,
                    rawOutput = responseXml.toString()
                )
            }
        } catch (e: Exception) {
            return FetchResult.FirmwareVersionFetchResult(error = e)
        }
    }

    /**
     * Parse firmware version XML from a string
     * @param xmlContent the XML content as string
     * @return parsed FirmwareVersionInfo
     */
    fun parseFirmwareVersionXmlString(xmlContent: String): FirmwareVersionInfo {
        val document = Ksoup.parse(xmlContent)
        return parseFirmwareVersionXml(document)
    }

    /**
     * Parse firmware version XML from a Ksoup Document
     * @param document the parsed XML document
     * @return parsed FirmwareVersionInfo
     */
    private fun parseFirmwareVersionXml(document: Document): FirmwareVersionInfo {
        val versionInfo = document.firstElementByTagName("versioninfo")
            ?: throw IllegalArgumentException("Invalid XML: missing versioninfo element")

        // Parse base URL
        val baseUrl = versionInfo.firstElementByTagName("url")?.text()
            ?: throw IllegalArgumentException("Invalid XML: missing url element")

        // Parse firmware information
        val firmware = versionInfo.firstElementByTagName("firmware")
            ?: throw IllegalArgumentException("Invalid XML: missing firmware element")

        val model = firmware.firstElementByTagName("model")?.text()
            ?: throw IllegalArgumentException("Invalid XML: missing model element")

        val countryCode = firmware.firstElementByTagName("cc")?.text()
            ?: throw IllegalArgumentException("Invalid XML: missing cc element")

        // Parse version information
        val version = firmware.firstElementByTagName("version")
            ?: throw IllegalArgumentException("Invalid XML: missing version element")

        // Parse latest version
        val latestElement = version.firstElementByTagName("latest")
            ?: throw IllegalArgumentException("Invalid XML: missing latest element")

        val latestVersion = LatestVersionInfo(
            versionCode = latestElement.text(),
            androidVersion = latestElement.attribute("o")?.value ?: ""
        )

        // Parse upgrade versions
        val upgradeElement = version.firstElementByTagName("upgrade")
        val upgradeVersions = upgradeElement?.children()?.mapNotNull { element ->
            if (element.tagName() == "value") {
                try {
                    UpgradeVersionInfo(
                        versionCode = element.text(),
                        rollbackCount = element.attribute("rcount")?.value?.toIntOrNull() ?: 0,
                        firmwareSize = element.attribute("fwsize")?.value?.toLongOrNull() ?: 0L
                    )
                } catch (e: Exception) {
                    null // Skip invalid entries
                }
            } else null
        } ?: emptyList()

        // Parse polling information
        val polling = versionInfo.firstElementByTagName("polling")
        val pollingInfo = if (polling != null) {
            PollingInfo(
                period = polling.firstElementByTagName("period")?.text()?.toIntOrNull() ?: 0,
                time = polling.firstElementByTagName("time")?.text()?.toIntOrNull() ?: 0,
                range = polling.firstElementByTagName("range")?.text()?.toIntOrNull() ?: 0
            )
        } else {
            PollingInfo(0, 0, 0)
        }

        // Parse active device info
        val activeDevice = versionInfo.firstElementByTagName("ActiveDeviceInfo")
        val activeDeviceInfo = if (activeDevice != null) {
            ActiveDeviceInfo(
                cycleOfDeviceHeartbeat = activeDevice.firstElementByTagName("CycleOfDeviceHeartbeat")?.text()?.toIntOrNull() ?: 0,
                serviceUrl = activeDevice.firstElementByTagName("ServiceURL")?.text() ?: ""
            )
        } else {
            ActiveDeviceInfo(0, "")
        }

        // Parse WiFi connected polling info
        val wifiPolling = versionInfo.firstElementByTagName("WiFiConnectedPollingInfo")
        val wifiConnectedPollingInfo = if (wifiPolling != null) {
            WiFiConnectedPollingInfo(
                cycle = wifiPolling.firstElementByTagName("Cycle")?.text() ?: "",
                activated = wifiPolling.firstElementByTagName("Activated")?.text()?.equals("TRUE", ignoreCase = true) ?: false
            )
        } else {
            WiFiConnectedPollingInfo("", false)
        }

        return FirmwareVersionInfo(
            baseUrl = baseUrl,
            model = model,
            countryCode = countryCode,
            latestVersion = latestVersion,
            upgradeVersions = upgradeVersions,
            pollingInfo = pollingInfo,
            activeDeviceInfo = activeDeviceInfo,
            wifiConnectedPollingInfo = wifiConnectedPollingInfo
        )
    }

    /**
     * Get all available firmware versions sorted by rollback count (most recent first)
     */
    fun getAvailableVersionsSorted(firmwareInfo: FirmwareVersionInfo): List<UpgradeVersionInfo> {
        return firmwareInfo.upgradeVersions.sortedByDescending { it.rollbackCount }
    }

    /**
     * Find firmware versions by size range
     */
    fun findVersionsBySize(firmwareInfo: FirmwareVersionInfo, minSize: Long, maxSize: Long): List<UpgradeVersionInfo> {
        return firmwareInfo.upgradeVersions.filter { it.firmwareSize in minSize..maxSize }
    }

    /**
     * Get firmware versions with specific rollback count
     */
    fun getVersionsByRollbackCount(firmwareInfo: FirmwareVersionInfo, rollbackCount: Int): List<UpgradeVersionInfo> {
        return firmwareInfo.upgradeVersions.filter { it.rollbackCount == rollbackCount }
    }

    /**
     * Format firmware size in human-readable format
     */
    fun formatFirmwareSize(sizeInBytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = sizeInBytes.toDouble()
        var unitIndex = 0

        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }

        return "%.2f %s".format(size, units[unitIndex])
    }

    /**
     * Generate a summary report of firmware information
     */
    fun generateSummaryReport(firmwareInfo: FirmwareVersionInfo): String {
        val sb = StringBuilder()
        
        sb.appendLine("=== Samsung Firmware Version Information ===")
        sb.appendLine("Model: ${firmwareInfo.model}")
        sb.appendLine("Country Code: ${firmwareInfo.countryCode}")
        sb.appendLine("Base URL: ${firmwareInfo.baseUrl}")
        sb.appendLine()
        
        sb.appendLine("Latest Version:")
        sb.appendLine("  Version Code: ${firmwareInfo.latestVersion.versionCode}")
        sb.appendLine("  Android Version: ${firmwareInfo.latestVersion.androidVersion}")
        sb.appendLine()
        
        sb.appendLine("Available Upgrade Versions: ${firmwareInfo.upgradeVersions.size}")
        val sortedVersions = getAvailableVersionsSorted(firmwareInfo)
        sortedVersions.take(5).forEach { version ->
            sb.appendLine("  ${version.versionCode} (Rollback: ${version.rollbackCount}, Size: ${formatFirmwareSize(version.firmwareSize)})")
        }
        if (sortedVersions.size > 5) {
            sb.appendLine("  ... and ${sortedVersions.size - 5} more versions")
        }
        sb.appendLine()
        
        sb.appendLine("Polling Configuration:")
        sb.appendLine("  Period: ${firmwareInfo.pollingInfo.period}")
        sb.appendLine("  Time: ${firmwareInfo.pollingInfo.time}")
        sb.appendLine("  Range: ${firmwareInfo.pollingInfo.range}")
        sb.appendLine()
        
        sb.appendLine("Device Heartbeat:")
        sb.appendLine("  Cycle: ${firmwareInfo.activeDeviceInfo.cycleOfDeviceHeartbeat}")
        sb.appendLine("  Service URL: ${firmwareInfo.activeDeviceInfo.serviceUrl}")
        sb.appendLine()
        
        sb.appendLine("WiFi Polling:")
        sb.appendLine("  Cycle: ${firmwareInfo.wifiConnectedPollingInfo.cycle}")
        sb.appendLine("  Activated: ${firmwareInfo.wifiConnectedPollingInfo.activated}")
        
        return sb.toString()
    }
}
