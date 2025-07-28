# Samsung Firmware Version Parser Tool

This document describes the new `FirmwareVersionParser` tool that has been added to SamloaderKotlin for parsing Samsung firmware version XML files.

## Overview

The `FirmwareVersionParser` tool provides comprehensive functionality for fetching, parsing, and analyzing Samsung firmware version information from Samsung's FOTA (Firmware Over The Air) servers.

## Features

### Core Functionality
- **Fetch firmware version information** directly from Samsung's servers
- **Parse XML content** from strings or documents
- **Extract comprehensive firmware data** including:
  - Latest firmware version and Android version
  - All available upgrade versions with rollback counts and sizes
  - Polling configuration
  - Device heartbeat information
  - WiFi polling settings

### Analysis Tools
- **Sort versions** by rollback count (newest first)
- **Filter versions** by size range
- **Find versions** with specific rollback counts
- **Format firmware sizes** in human-readable format
- **Generate summary reports** with comprehensive firmware information

## Data Structures

### FirmwareVersionInfo
Main data class containing all firmware information:
```kotlin
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
```

### LatestVersionInfo
Information about the latest firmware version:
```kotlin
data class LatestVersionInfo(
    val versionCode: String,
    val androidVersion: String
)
```

### UpgradeVersionInfo
Information about available upgrade versions:
```kotlin
data class UpgradeVersionInfo(
    val versionCode: String,
    val rollbackCount: Int,
    val firmwareSize: Long
)
```

## Usage Examples

### Basic Usage - Fetch from Samsung Servers
```kotlin
suspend fun fetchFirmwareInfo() {
    val result = FirmwareVersionParser.fetchFirmwareVersionInfo("SM-S906B", "EUX")
    
    if (result.error == null && result.firmwareVersionInfo != null) {
        val firmwareInfo = result.firmwareVersionInfo
        println(FirmwareVersionParser.generateSummaryReport(firmwareInfo))
    } else {
        println("Error: ${result.error?.message}")
    }
}
```

### Parse XML from String
```kotlin
fun parseXmlString() {
    val xmlContent = """<?xml version="1.0" encoding="UTF-8" ?>
    <versioninfo>
        <url>https://fota-cloud-dn.ospserver.net/firmware/</url>
        <firmware>
            <model>SM-S906B</model>
            <cc>EUX</cc>
            <version>
                <latest o="15">S906BXXSGFYG1/S906BOXMGFYG1/S906BXXSGFYG1</latest>
                <!-- ... more XML content ... -->
            </version>
        </firmware>
        <!-- ... more XML content ... -->
    </versioninfo>"""
    
    val firmwareInfo = FirmwareVersionParser.parseFirmwareVersionXmlString(xmlContent)
    println("Model: ${firmwareInfo.model}")
    println("Latest Version: ${firmwareInfo.latestVersion.versionCode}")
}
```

### Advanced Filtering and Analysis
```kotlin
suspend fun advancedAnalysis() {
    val result = FirmwareVersionParser.fetchFirmwareVersionInfo("SM-S906B", "EUX")
    val firmwareInfo = result.firmwareVersionInfo ?: return
    
    // Get latest 10 versions
    val latestVersions = FirmwareVersionParser.getAvailableVersionsSorted(firmwareInfo).take(10)
    
    // Find versions with rollback count 7
    val rollback7Versions = FirmwareVersionParser.getVersionsByRollbackCount(firmwareInfo, 7)
    
    // Find versions between 1GB and 3GB
    val sizeFilteredVersions = FirmwareVersionParser.findVersionsBySize(
        firmwareInfo, 
        1024L * 1024L * 1024L, // 1GB
        3L * 1024L * 1024L * 1024L // 3GB
    )
    
    // Format firmware sizes
    firmwareInfo.upgradeVersions.forEach { version ->
        println("${version.versionCode}: ${FirmwareVersionParser.formatFirmwareSize(version.firmwareSize)}")
    }
}
```

## API Reference

### FirmwareVersionParser Methods

#### `fetchFirmwareVersionInfo(model: String, region: String)`
- **Description**: Fetches and parses firmware version information from Samsung's servers
- **Parameters**: 
  - `model`: Device model (e.g., "SM-S906B")
  - `region`: Device region (e.g., "EUX")
- **Returns**: `FetchResult.FirmwareVersionFetchResult`

#### `parseFirmwareVersionXmlString(xmlContent: String)`
- **Description**: Parses firmware version XML from a string
- **Parameters**: `xmlContent` - The XML content as string
- **Returns**: `FirmwareVersionInfo`

#### `getAvailableVersionsSorted(firmwareInfo: FirmwareVersionInfo)`
- **Description**: Gets all available firmware versions sorted by rollback count (most recent first)
- **Returns**: `List<UpgradeVersionInfo>`

#### `findVersionsBySize(firmwareInfo: FirmwareVersionInfo, minSize: Long, maxSize: Long)`
- **Description**: Finds firmware versions within a specific size range
- **Returns**: `List<UpgradeVersionInfo>`

#### `getVersionsByRollbackCount(firmwareInfo: FirmwareVersionInfo, rollbackCount: Int)`
- **Description**: Gets firmware versions with a specific rollback count
- **Returns**: `List<UpgradeVersionInfo>`

#### `formatFirmwareSize(sizeInBytes: Long)`
- **Description**: Formats firmware size in human-readable format
- **Returns**: `String` (e.g., "1.85 GB")

#### `generateSummaryReport(firmwareInfo: FirmwareVersionInfo)`
- **Description**: Generates a comprehensive summary report of firmware information
- **Returns**: `String`

## Example Output

When you run the parser on Samsung Galaxy S22+ (SM-S906B) firmware data, you get output like:

```
=== Samsung Firmware Version Information ===
Model: SM-S906B
Country Code: EUX
Base URL: https://fota-cloud-dn.ospserver.net/firmware/

Latest Version:
  Version Code: S906BXXSGFYG1/S906BOXMGFYG1/S906BXXSGFYG1
  Android Version: 15

Available Upgrade Versions: 56
  S906BXXU1AVCJ/S906BOXM1AVCJ/S906BXXU1AVD5 (Rollback: 7, Size: 1.81 GB)
  S906BXXU2AVH9/S906BOXM2AVHB/S906BXXU2AVH9 (Rollback: 7, Size: 248.76 MB)
  S906BXXU2AVF1/S906BOXM2AVF1/S906BXXU2AVF1 (Rollback: 7, Size: 1.02 GB)
  S906BXXU1AVA7/S906BOXM1AVA7/S906BXXU1AVA7 (Rollback: 7, Size: 2.23 GB)
  S906BXXS2AVDB/S906BOXM2AVDB/S906BXXS2AVDB (Rollback: 7, Size: 1.74 GB)
  ... and 51 more versions

Polling Configuration:
  Period: 1
  Time: 15
  Range: 23

Device Heartbeat:
  Cycle: 14
  Service URL: https://ifota-apis.samsungdm.com/device/fumo/deviceheartbeat

WiFi Polling:
  Cycle: 
  Activated: false
```

## Integration with Existing Code

The tool integrates seamlessly with the existing SamloaderKotlin architecture:

- Uses the same HTTP client (`globalHttpClient`) as other tools
- Follows the same error handling patterns with `FetchResult`
- Uses the same XML parsing library (Ksoup) as `VersionFetch`
- Maintains consistent code style and structure

## Files Added/Modified

### New Files
- `common/src/commonMain/kotlin/tk/zwander/common/data/FirmwareVersionInfo.kt` - Data classes
- `common/src/commonMain/kotlin/tk/zwander/common/tools/FirmwareVersionParser.kt` - Main parser tool
- `common/src/commonMain/kotlin/tk/zwander/common/tools/FirmwareVersionParserExample.kt` - Usage examples

### Modified Files
- `common/src/commonMain/kotlin/tk/zwander/common/data/FetchResult.kt` - Added `FirmwareVersionFetchResult`

## Testing

A test file `firmware_version_test.kt` is included that demonstrates the tool working with actual Samsung firmware XML data from the SM-S906B (Galaxy S22+) device.

## Benefits

1. **Comprehensive Data Access**: Access to all firmware information, not just the latest version
2. **Advanced Filtering**: Multiple ways to filter and analyze firmware versions
3. **Size Analysis**: Human-readable firmware size formatting and filtering
4. **Rollback Analysis**: Understanding of firmware rollback patterns
5. **Extensible**: Easy to add new analysis methods and filters
6. **Consistent**: Follows existing codebase patterns and conventions
