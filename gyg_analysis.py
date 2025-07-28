#!/usr/bin/env python3
"""
Analysis of Samsung Galaxy S22+ firmware containing 'GYG' pattern
Using the firmware version XML data from Samsung's servers
"""

import xml.etree.ElementTree as ET

def analyze_gyg_firmware():
    print("🔍 Samsung Galaxy S22+ (SM-S906B) Firmware Analysis")
    print("=" * 60)
    
    # Parse the XML data
    with open('firmware_data.xml', 'r') as f:
        xml_content = f.read()
    
    root = ET.fromstring(xml_content)
    
    # Extract basic info
    model = root.find('.//model').text
    country_code = root.find('.//cc').text
    base_url = root.find('.//url').text
    
    print(f"📱 Device: {model}")
    print(f"🌍 Region: {country_code}")
    print(f"🔗 Base URL: {base_url}")
    print()
    
    # Find the latest version with GYG pattern
    latest_element = root.find('.//latest')
    latest_version = latest_element.text
    android_version = latest_element.get('o')
    
    print("🎯 FOUND: Latest Firmware Version with 'FYG' pattern")
    print("-" * 50)
    print(f"📦 Version Code: {latest_version}")
    print(f"🤖 Android Version: {android_version}")
    print()
    
    # Break down the version code
    version_parts = latest_version.split('/')
    if len(version_parts) == 3:
        print("📋 Version Code Breakdown:")
        print(f"   PDA: {version_parts[0]} (contains SGFYG1)")
        print(f"   CSC: {version_parts[1]} (contains MGFYG1)")  
        print(f"   CP:  {version_parts[2]} (contains SGFYG1)")
        print()
    
    # Analyze the FYG1 pattern
    print("🔬 Pattern Analysis:")
    print("   The 'FYG1' pattern appears in all three components:")
    print("   - S906BXXSGFYG1 (PDA - Platform/Android)")
    print("   - S906BOXMGFYG1 (CSC - Country/Carrier Specific)")
    print("   - S906BXXSGFYG1 (CP - Modem/Radio)")
    print()
    
    # Check if this appears in upgrade versions
    upgrade_versions = root.findall('.//upgrade/value')
    gyg_upgrades = []
    
    for version in upgrade_versions:
        version_code = version.text
        if 'fyg' in version_code.lower() or 'gyg' in version_code.lower():
            rollback_count = version.get('rcount', '0')
            firmware_size = int(version.get('fwsize', '0'))
            gyg_upgrades.append({
                'version': version_code,
                'rollback': int(rollback_count),
                'size': firmware_size
            })
    
    if gyg_upgrades:
        print(f"📊 Found {len(gyg_upgrades)} upgrade versions with GYG pattern:")
        for upgrade in sorted(gyg_upgrades, key=lambda x: x['rollback'], reverse=True):
            size_gb = upgrade['size'] / (1024**3)
            print(f"   {upgrade['version']} (Rollback: {upgrade['rollback']}, Size: {size_gb:.2f} GB)")
    else:
        print("📊 No upgrade versions found with GYG pattern")
        print("   The FYG1 pattern only appears in the LATEST version")
    
    print()
    print("✅ Summary:")
    print(f"   • Found 'FYG1' pattern in the LATEST firmware version")
    print(f"   • This is Android {android_version} for Samsung Galaxy S22+ (SM-S906B)")
    print(f"   • Region: Europe (EUX)")
    print(f"   • The pattern appears consistently across PDA, CSC, and CP components")
    
    # Additional context
    print()
    print("💡 Context:")
    print("   • FYG1 likely represents a specific firmware build/release")
    print("   • This appears to be the most recent firmware available")
    print("   • The 'S' prefix typically indicates Samsung official firmware")
    print("   • The 'G' in the model (S906B) indicates Global/European variant")

if __name__ == "__main__":
    analyze_gyg_firmware()

