package tk.zwander.common.data

/**
 * Data class representing firmware version information from Samsung's version.xml
 */
data class FirmwareVersionInfo(
    val baseUrl: String,
    val model: String,
    val countryCode: String,
    val latestVersion: LatestVersionInfo,
    val upgradeVersions: List<UpgradeVersionInfo>,
    val pollingInfo: PollingInfo,
    val activeDeviceInfo: ActiveDeviceInfo,
    val wifiConnectedPollingInfo: WiFiConnectedPollingInfo
)

/**
 * Latest firmware version information
 */
data class LatestVersionInfo(
    val versionCode: String,
    val androidVersion: String
)

/**
 * Upgrade version information with firmware size and rollback count
 */
data class UpgradeVersionInfo(
    val versionCode: String,
    val rollbackCount: Int,
    val firmwareSize: Long
)

/**
 * Polling configuration information
 */
data class PollingInfo(
    val period: Int,
    val time: Int,
    val range: Int
)

/**
 * Active device heartbeat information
 */
data class ActiveDeviceInfo(
    val cycleOfDeviceHeartbeat: Int,
    val serviceUrl: String
)

/**
 * WiFi connected polling information
 */
data class WiFiConnectedPollingInfo(
    val cycle: String,
    val activated: Boolean
)
