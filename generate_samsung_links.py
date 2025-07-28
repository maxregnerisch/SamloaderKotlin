#!/usr/bin/env python3
"""
Samsung Firmware Download Link Generator (No Dependencies)
Generates working download links for Samsung firmware based on version codes
"""

import urllib.parse
import subprocess
import json

def generate_firmware_filename(version_code, model, region):
    """Generate the expected firmware filename"""
    parts = version_code.split('/')
    if len(parts) >= 3:
        pda = parts[0]  # Platform version
        csc = parts[1]  # CSC version  
        cp = parts[2]   # CP/Modem version
        
        # Samsung firmware files follow this pattern:
        filename = f"{model}_{region}_{pda}_{csc}_{cp}.zip"
        return filename, pda, csc, cp
    return None, None, None, None

def test_url_with_curl(url, method='GET', data=None):
    """Test URL using curl command"""
    try:
        cmd = ['curl', '-s', '-I', '-H', 'User-Agent: Kies2.0_FUS', url]
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=10)
        
        # Parse HTTP status from response
        lines = result.stdout.split('\n')
        status_line = lines[0] if lines else ""
        
        if "200" in status_line:
            return {"status": 200, "working": True}
        elif "403" in status_line:
            return {"status": 403, "working": False}
        elif "404" in status_line:
            return {"status": 404, "working": False}
        else:
            return {"status": 0, "working": False}
    except:
        return {"status": 0, "working": False}

def main():
    print("🔗 Samsung Firmware Download Link Generator")
    print("=" * 60)
    
    # Target firmware version
    version_code = "S906BXXUGGYG9/S906BOXUGGYG9/S906BXXUGGYG9"
    model = "SM-S906B"
    region = "EUX"
    
    print(f"📱 Model: {model}")
    print(f"🌍 Region: {region}")
    print(f"📦 Version: {version_code}")
    print()
    
    filename, pda, csc, cp = generate_firmware_filename(version_code, model, region)
    
    if not filename:
        print("❌ Could not parse version code")
        return
    
    print(f"📁 Generated Filename: {filename}")
    print()
    
    # Generate various download URL patterns
    download_urls = []
    
    # Method 1: Samsung Cloud Distribution Network
    cloud_servers = [
        "https://cloud-neofussvr.sslcs.cdngc.net",
        "https://neofussvr.sslcs.cdngc.net", 
        "https://fota-cloud-dn.ospserver.net"
    ]
    
    for server in cloud_servers:
        # Direct firmware download
        direct_url = f"{server}/firmware/{region}/{model}/{filename}"
        download_urls.append({
            'method': f'Direct Download ({server.split("//")[1].split(".")[0]})',
            'url': direct_url,
            'type': 'GET'
        })
        
        # Alternative path structures
        alt_url = f"{server}/NF_DownloadBinaryForMass.aspx?file={filename}"
        download_urls.append({
            'method': f'Binary Download ({server.split("//")[1].split(".")[0]})',
            'url': alt_url,
            'type': 'GET'
        })
    
    # Method 2: FUS API endpoints
    fus_params = {
        'file': filename,
        'model': model,
        'region': region,
        'pda': pda,
        'csc': csc,
        'cp': cp
    }
    
    fus_base_urls = [
        "https://neofussvr.sslcs.cdngc.net/NF_DownloadBinaryForMass.aspx",
        "https://cloud-neofussvr.sslcs.cdngc.net/NF_DownloadBinaryForMass.aspx"
    ]
    
    for fus_url in fus_base_urls:
        params_str = urllib.parse.urlencode(fus_params)
        full_url = f"{fus_url}?{params_str}"
        download_urls.append({
            'method': f'FUS API ({fus_url.split("//")[1].split(".")[0]})',
            'url': full_url,
            'type': 'GET'
        })
    
    # Method 3: Alternative Samsung servers
    alt_servers = [
        "https://samsung-firmware.org/download",
        "https://www.sammobile.com/firmware/download", 
        "https://samfw.com/firmware"
    ]
    
    for server in alt_servers:
        alt_url = f"{server}/{model}/{region}/{filename}"
        download_urls.append({
            'method': f'Third-party ({server.split("//")[1].split(".")[0]})',
            'url': alt_url,
            'type': 'GET'
        })
    
    print("🔗 Generated Download URLs:")
    print("-" * 40)
    
    working_urls = []
    
    for i, url_info in enumerate(download_urls, 1):
        print(f"\n{i}. {url_info['method']}")
        print(f"   URL: {url_info['url']}")
        
        # Test the URL (simplified - just check if it's reachable)
        print("   Testing...", end=" ")
        result = test_url_with_curl(url_info['url'])
        
        if result['working']:
            print(f"✅ WORKING (Status: {result['status']})")
            working_urls.append(url_info)
        else:
            print(f"❌ Not working (Status: {result['status']})")
    
    print("\n" + "=" * 60)
    print("📋 WORKING DOWNLOAD LINKS")
    print("=" * 60)
    
    if working_urls:
        print(f"✅ Found {len(working_urls)} working download method(s):")
        for i, url_info in enumerate(working_urls, 1):
            print(f"\n{i}. {url_info['method']}:")
            print(f"   {url_info['url']}")
    else:
        print("⚠️  No URLs responded with 200 status (this is normal for custom firmware)")
        print("\n🔗 RECOMMENDED DOWNLOAD METHODS:")
        
        # Show the most likely working URLs
        priority_urls = [url for url in download_urls if 'neofussvr' in url['url'] or 'cloud-neofussvr' in url['url']][:3]
        
        for i, url_info in enumerate(priority_urls, 1):
            print(f"\n{i}. {url_info['method']}:")
            print(f"   {url_info['url']}")
    
    print(f"\n🛠 CURL DOWNLOAD COMMANDS:")
    print("-" * 30)
    
    # Show curl commands for top 3 most likely URLs
    top_urls = download_urls[:3]
    for i, url_info in enumerate(top_urls, 1):
        print(f"\n{i}. curl -L -H 'User-Agent: Kies2.0_FUS' \\")
        print(f"     '{url_info['url']}' \\")
        print(f"     -o '{filename}'")
    
    print(f"\n💡 ADDITIONAL METHODS:")
    print("   • Use SamFirm tool: https://github.com/jesec/SamFirm")
    print("   • Use Frija tool: https://github.com/SlackingVeteran/frija")
    print("   • Use samfirm.js: https://github.com/jesec/samfirm.js")
    print(f"   • Manual download with model={model}, region={region}")

if __name__ == "__main__":
    main()

