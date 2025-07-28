#!/usr/bin/env python3
"""
Samsung Firmware 403 Bypass Downloader
Advanced tool to bypass 403 errors and download Samsung firmware
Specifically designed for S906BXXUGGYG9 firmware
"""

import requests
import time
import random
import hashlib
import base64
import urllib.parse
from datetime import datetime
import threading
import os
import sys
from urllib.parse import urlparse, parse_qs

class Samsung403BypassDownloader:
    def __init__(self):
        self.session = requests.Session()
        self.target_firmware = "S906BXXUGGYG9/S906BOXUGGYG9/S906BXXUGGYG9"
        self.model = "SM-S906B"
        self.region = "EUX"
        self.filename = "SM-S906B_EUX_S906BXXUGGYG9_S906BOXUGGYG9_S906BXXUGGYG9.zip"
        
        # User agents that work with Samsung servers
        self.user_agents = [
            "Kies2.0_FUS",
            "Samsung Kies/2.6.3.14044_17",
            "SAMSUNG_USB_Driver/1.5.59.0",
            "SamFirm/0.3.6",
            "Frija/1.4.2",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
            "Samsung-SM-S906B/1.0",
            "FOTA-HTTP-Client"
        ]
        
        # Proxy list for rotation (add your own proxies here)
        self.proxies_list = [
            None,  # Direct connection
            # Add working proxies here if needed
            # {"http": "http://proxy1:port", "https": "https://proxy1:port"},
        ]
        
        self.current_proxy_index = 0
        
    def generate_auth_token(self, timestamp=None):
        """Generate Samsung authentication token"""
        if not timestamp:
            timestamp = str(int(datetime.now().timestamp()))
        
        message = f"{self.model}:{self.region}:{self.target_firmware}:{timestamp}"
        secret = "versioninfo"
        signature = hashlib.md5(f"{message}:{secret}".encode()).hexdigest()
        token = base64.b64encode(f"{timestamp}:{signature}".encode()).decode()
        
        return {
            'timestamp': timestamp,
            'signature': signature,
            'token': token
        }
    
    def get_random_headers(self):
        """Generate randomized headers to avoid detection"""
        auth = self.generate_auth_token()
        
        headers = {
            'User-Agent': random.choice(self.user_agents),
            'Accept': random.choice([
                'application/xml, text/xml, */*',
                '*/*',
                'application/octet-stream',
                'application/zip'
            ]),
            'Accept-Encoding': 'gzip, deflate',
            'Accept-Language': random.choice([
                'en-US,en;q=0.9',
                'ko-KR,ko;q=0.9,en;q=0.8',
                'en-GB,en;q=0.9'
            ]),
            'Connection': 'keep-alive',
            'Cache-Control': random.choice(['no-cache', 'max-age=0']),
            'Pragma': 'no-cache',
            'DNT': '1',
            'Upgrade-Insecure-Requests': '1'
        }
        
        # Add Samsung-specific headers randomly
        if random.choice([True, False]):
            headers['X-Samsung-Device'] = self.model
            headers['X-Samsung-Region'] = self.region
            
        if random.choice([True, False]):
            headers['Authorization'] = f"Bearer {auth['token']}"
            
        return headers, auth
    
    def rotate_proxy(self):
        """Rotate to next proxy"""
        self.current_proxy_index = (self.current_proxy_index + 1) % len(self.proxies_list)
        return self.proxies_list[self.current_proxy_index]
    
    def generate_bypass_urls(self):
        """Generate multiple URL variations for bypass attempts"""
        auth = self.generate_auth_token()
        urls = []
        
        # Method 1: FUS API with various parameter combinations
        base_params = {
            'device': self.model,
            'region': self.region,
            'pda': 'S906BXXUGGYG9',
            'csc': 'S906BOXUGGYG9',
            'cp': 'S906BXXUGGYG9'
        }
        
        # Variation 1: Standard FUS
        params1 = base_params.copy()
        params1.update({
            'binary_nature': '1',
            'device_type': 'phone',
            'auth_token': auth['token'],
            'timestamp': auth['timestamp']
        })
        urls.append({
            'url': f"https://neofussvr.sslcs.cdngc.net/NF_DownloadBinaryForMass.aspx?{urllib.parse.urlencode(params1)}",
            'method': 'FUS API Standard'
        })
        
        # Variation 2: Cloud distribution
        params2 = {
            'file': self.filename,
            'auth': auth['signature'],
            'ts': auth['timestamp'],
            'device': self.model,
            'region': self.region
        }
        urls.append({
            'url': f"https://cloud-neofussvr.sslcs.cdngc.net/NF_DownloadBinaryForMass.aspx?{urllib.parse.urlencode(params2)}",
            'method': 'Cloud Distribution'
        })
        
        # Variation 3: Alternative servers
        alt_servers = [
            "https://fota-cloud-dn.ospserver.net",
            "https://neofussvr.sslcs.cdngc.net",
            "https://cloud-neofussvr.sslcs.cdngc.net"
        ]
        
        for server in alt_servers:
            # Direct firmware path
            urls.append({
                'url': f"{server}/firmware/{self.region}/{self.model}/{self.filename}",
                'method': f'Direct ({server.split("//")[1].split(".")[0]})'
            })
            
            # API endpoint
            params3 = base_params.copy()
            params3.update({
                'file': self.filename,
                'auth': auth['signature']
            })
            urls.append({
                'url': f"{server}/NF_DownloadBinaryForMass.aspx?{urllib.parse.urlencode(params3)}",
                'method': f'API ({server.split("//")[1].split(".")[0]})'
            })
        
        # Variation 4: Session-based downloads
        session_params = {
            'device': self.model,
            'region': self.region,
            'version': self.target_firmware,
            'session': auth['token'],
            'binary_nature': '1'
        }
        urls.append({
            'url': f"https://neofussvr.sslcs.cdngc.net/NF_DownloadBinaryInform.aspx?{urllib.parse.urlencode(session_params)}",
            'method': 'Session Download'
        })
        
        return urls
    
    def attempt_download(self, url_info, max_retries=3):
        """Attempt to download from a specific URL with 403 bypass techniques"""
        url = url_info['url']
        method = url_info['method']
        
        print(f"\n🔄 Attempting: {method}")
        print(f"   URL: {url[:80]}{'...' if len(url) > 80 else ''}")
        
        for attempt in range(max_retries):
            try:
                # Rotate proxy
                proxy = self.rotate_proxy()
                
                # Generate random headers
                headers, auth = self.get_random_headers()
                
                print(f"   Attempt {attempt + 1}/{max_retries} - ", end="")
                
                # Add random delay to avoid rate limiting
                time.sleep(random.uniform(1, 3))
                
                # Make request with timeout
                response = self.session.get(
                    url,
                    headers=headers,
                    proxies=proxy,
                    timeout=30,
                    allow_redirects=True,
                    stream=True
                )
                
                print(f"Status: {response.status_code}")
                
                # Check if successful
                if response.status_code == 200:
                    content_type = response.headers.get('content-type', '').lower()
                    content_length = response.headers.get('content-length', '0')
                    
                    if 'application/zip' in content_type or 'application/octet-stream' in content_type or int(content_length) > 1000000:
                        print(f"   ✅ SUCCESS! Content-Type: {content_type}, Size: {content_length} bytes")
                        return response, url, method
                    else:
                        print(f"   ⚠️  Got 200 but wrong content type: {content_type}")
                        
                elif response.status_code == 302 or response.status_code == 301:
                    # Follow redirect manually
                    redirect_url = response.headers.get('location')
                    if redirect_url:
                        print(f"   🔄 Redirect to: {redirect_url[:60]}...")
                        redirect_response = self.session.get(
                            redirect_url,
                            headers=headers,
                            proxies=proxy,
                            timeout=30,
                            stream=True
                        )
                        if redirect_response.status_code == 200:
                            print(f"   ✅ SUCCESS after redirect!")
                            return redirect_response, redirect_url, f"{method} (Redirected)"
                        
                elif response.status_code == 403:
                    print(f"   ❌ 403 Forbidden")
                    
                elif response.status_code == 404:
                    print(f"   ❌ 404 Not Found")
                    
                else:
                    print(f"   ❌ HTTP {response.status_code}")
                    
            except requests.exceptions.Timeout:
                print(f"   ⏰ Timeout")
            except requests.exceptions.RequestException as e:
                print(f"   ❌ Error: {str(e)[:50]}")
            
            # Wait before retry
            if attempt < max_retries - 1:
                wait_time = random.uniform(2, 5)
                time.sleep(wait_time)
        
        return None, None, None
    
    def download_file(self, response, filename, method):
        """Download file with progress tracking"""
        print(f"\n📥 Downloading {filename} via {method}")
        
        total_size = int(response.headers.get('content-length', 0))
        downloaded = 0
        
        try:
            with open(filename, 'wb') as f:
                for chunk in response.iter_content(chunk_size=8192):
                    if chunk:
                        f.write(chunk)
                        downloaded += len(chunk)
                        
                        if total_size > 0:
                            progress = (downloaded / total_size) * 100
                            print(f"\r   Progress: {progress:.1f}% ({downloaded:,}/{total_size:,} bytes)", end="")
                        else:
                            print(f"\r   Downloaded: {downloaded:,} bytes", end="")
            
            print(f"\n✅ Download completed: {filename}")
            print(f"   Final size: {downloaded:,} bytes")
            return True
            
        except Exception as e:
            print(f"\n❌ Download failed: {e}")
            return False
    
    def run_bypass_download(self):
        """Main function to run the 403 bypass download"""
        print("🚀 Samsung Firmware 403 Bypass Downloader")
        print("=" * 60)
        print(f"📱 Target: {self.model} ({self.region})")
        print(f"📦 Firmware: {self.target_firmware}")
        print(f"📁 Output: {self.filename}")
        print()
        
        # Generate all possible URLs
        urls = self.generate_bypass_urls()
        print(f"🔗 Generated {len(urls)} bypass URLs to try")
        
        # Try each URL until one works
        for i, url_info in enumerate(urls, 1):
            print(f"\n[{i}/{len(urls)}] Testing: {url_info['method']}")
            
            response, working_url, working_method = self.attempt_download(url_info)
            
            if response:
                print(f"\n🎯 FOUND WORKING URL!")
                print(f"   Method: {working_method}")
                print(f"   URL: {working_url}")
                
                # Download the file
                success = self.download_file(response, self.filename, working_method)
                
                if success:
                    print(f"\n🎉 SUCCESS! Firmware downloaded successfully!")
                    print(f"   File: {self.filename}")
                    print(f"   Method: {working_method}")
                    return True
                else:
                    print(f"\n❌ Download failed, trying next URL...")
                    continue
        
        print(f"\n💔 All bypass attempts failed")
        print(f"   Tried {len(urls)} different URLs and methods")
        print(f"\n💡 Alternative suggestions:")
        print(f"   • Use SamFirm tool: SamFirm -m {self.model} -r {self.region}")
        print(f"   • Use Frija tool with VPN")
        print(f"   • Try different time of day (Samsung servers vary by load)")
        print(f"   • Use residential proxy services")
        
        return False

def main():
    """Main entry point"""
    if len(sys.argv) > 1 and sys.argv[1] == '--help':
        print("Samsung Firmware 403 Bypass Downloader")
        print("Usage: python3 samsung_403_bypass_downloader.py")
        print("\nThis tool attempts to bypass 403 errors when downloading")
        print("Samsung firmware S906BXXUGGYG9 using multiple techniques:")
        print("• User-Agent rotation")
        print("• Header randomization") 
        print("• Proxy rotation")
        print("• Multiple URL patterns")
        print("• Authentication token generation")
        print("• Redirect following")
        return
    
    downloader = Samsung403BypassDownloader()
    
    try:
        success = downloader.run_bypass_download()
        sys.exit(0 if success else 1)
    except KeyboardInterrupt:
        print("\n\n⏹️  Download interrupted by user")
        sys.exit(1)
    except Exception as e:
        print(f"\n❌ Unexpected error: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()

