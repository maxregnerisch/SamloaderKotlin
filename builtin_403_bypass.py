#!/usr/bin/env python3
"""
Built-in 403 Bypass Tool for Samsung Firmware
Uses only Python standard library - no external dependencies
"""

import urllib.request
import urllib.parse
import urllib.error
import ssl
import time
import random
import hashlib
import base64
from datetime import datetime
import threading
import os
import sys

class BuiltIn403Bypass:
    def __init__(self):
        self.target_firmware = "S906BXXUGGYG9/S906BOXUGGYG9/S906BXXUGGYG9"
        self.model = "SM-S906B"
        self.region = "EUX"
        self.filename = "SM-S906B_EUX_S906BXXUGGYG9_S906BOXUGGYG9_S906BXXUGGYG9.zip"
        
        # User agents for rotation
        self.user_agents = [
            "Kies2.0_FUS",
            "Samsung Kies/2.6.3.14044_17",
            "SAMSUNG_USB_Driver/1.5.59.0",
            "SamFirm/0.3.6",
            "Frija/1.4.2",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Samsung-SM-S906B/1.0",
            "FOTA-HTTP-Client"
        ]
        
        # SSL context that ignores certificate errors
        self.ssl_context = ssl.create_default_context()
        self.ssl_context.check_hostname = False
        self.ssl_context.verify_mode = ssl.CERT_NONE
        
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
        """Generate randomized headers"""
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
                'ko-KR,ko;q=0.9,en;q=0.8'
            ]),
            'Connection': 'keep-alive',
            'Cache-Control': random.choice(['no-cache', 'max-age=0']),
            'Pragma': 'no-cache'
        }
        
        # Add Samsung-specific headers randomly
        if random.choice([True, False]):
            headers['X-Samsung-Device'] = self.model
            headers['X-Samsung-Region'] = self.region
            
        return headers, auth
    
    def generate_bypass_urls(self):
        """Generate bypass URLs using built-in libraries"""
        auth = self.generate_auth_token()
        urls = []
        
        # Base parameters
        base_params = {
            'device': self.model,
            'region': self.region,
            'pda': 'S906BXXUGGYG9',
            'csc': 'S906BOXUGGYG9',
            'cp': 'S906BXXUGGYG9'
        }
        
        # Method 1: FUS API
        params1 = base_params.copy()
        params1.update({
            'binary_nature': '1',
            'device_type': 'phone',
            'auth_token': auth['token'],
            'timestamp': auth['timestamp']
        })
        
        url1 = f"https://neofussvr.sslcs.cdngc.net/NF_DownloadBinaryForMass.aspx?{urllib.parse.urlencode(params1)}"
        urls.append({
            'url': url1,
            'method': 'FUS API Standard'
        })
        
        # Method 2: Cloud distribution
        params2 = {
            'file': self.filename,
            'auth': auth['signature'],
            'ts': auth['timestamp']
        }
        
        url2 = f"https://cloud-neofussvr.sslcs.cdngc.net/NF_DownloadBinaryForMass.aspx?{urllib.parse.urlencode(params2)}"
        urls.append({
            'url': url2,
            'method': 'Cloud Distribution'
        })
        
        # Method 3: Direct URLs
        servers = [
            "https://neofussvr.sslcs.cdngc.net",
            "https://cloud-neofussvr.sslcs.cdngc.net",
            "https://fota-cloud-dn.ospserver.net"
        ]
        
        for server in servers:
            direct_url = f"{server}/firmware/{self.region}/{self.model}/{self.filename}"
            urls.append({
                'url': direct_url,
                'method': f'Direct ({server.split("//")[1].split(".")[0]})'
            })
        
        return urls
    
    def test_url(self, url_info, max_retries=3):
        """Test URL using urllib"""
        url = url_info['url']
        method = url_info['method']
        
        print(f"🔄 Testing: {method}")
        print(f"   URL: {url[:80]}{'...' if len(url) > 80 else ''}")
        
        for attempt in range(max_retries):
            try:
                # Generate headers
                headers, auth = self.get_random_headers()
                
                # Create request
                req = urllib.request.Request(url)
                
                # Add headers
                for key, value in headers.items():
                    req.add_header(key, value)
                
                print(f"   Attempt {attempt + 1}/{max_retries} - ", end="")
                
                # Add delay
                time.sleep(random.uniform(1, 3))
                
                # Make request
                try:
                    response = urllib.request.urlopen(req, timeout=30, context=self.ssl_context)
                    
                    status_code = response.getcode()
                    content_type = response.headers.get('content-type', '').lower()
                    content_length = response.headers.get('content-length', '0')
                    
                    print(f"Status: {status_code}")
                    
                    if status_code == 200:
                        if ('zip' in content_type or 
                            'octet-stream' in content_type or 
                            int(content_length) > 1000000):
                            
                            print(f"   ✅ SUCCESS! Content-Type: {content_type}, Size: {content_length}")
                            return response, url, method
                        else:
                            print(f"   ⚠️  Wrong content type: {content_type}")
                    
                    elif status_code in [301, 302]:
                        redirect_url = response.headers.get('location')
                        print(f"   🔄 Redirect: {redirect_url[:50]}...")
                        
                        # Follow redirect
                        if redirect_url:
                            redirect_req = urllib.request.Request(redirect_url)
                            for key, value in headers.items():
                                redirect_req.add_header(key, value)
                            
                            redirect_response = urllib.request.urlopen(redirect_req, timeout=30, context=self.ssl_context)
                            if redirect_response.getcode() == 200:
                                print(f"   ✅ SUCCESS after redirect!")
                                return redirect_response, redirect_url, f"{method} (Redirected)"
                    
                    else:
                        print(f"   ❌ HTTP {status_code}")
                
                except urllib.error.HTTPError as e:
                    if e.code == 403:
                        print(f"   ❌ 403 Forbidden")
                    elif e.code == 404:
                        print(f"   ❌ 404 Not Found")
                    else:
                        print(f"   ❌ HTTP {e.code}")
                
                except urllib.error.URLError as e:
                    print(f"   ❌ URL Error: {str(e)[:50]}")
                
            except Exception as e:
                print(f"   ❌ Error: {str(e)[:50]}")
            
            # Wait before retry
            if attempt < max_retries - 1:
                time.sleep(random.uniform(2, 5))
        
        return None, None, None
    
    def download_file(self, response, filename, method):
        """Download file with progress"""
        print(f"\n📥 Downloading {filename} via {method}")
        
        try:
            # Get content length
            content_length = response.headers.get('content-length')
            total_size = int(content_length) if content_length else 0
            
            downloaded = 0
            
            with open(filename, 'wb') as f:
                while True:
                    chunk = response.read(8192)
                    if not chunk:
                        break
                    
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
    
    def run_bypass(self):
        """Main bypass function"""
        print("🚀 Built-in 403 Bypass Tool for Samsung Firmware")
        print("=" * 60)
        print(f"📱 Target: {self.model} ({self.region})")
        print(f"📦 Firmware: {self.target_firmware}")
        print(f"📁 Output: {self.filename}")
        print()
        
        # Generate URLs
        urls = self.generate_bypass_urls()
        print(f"🔗 Generated {len(urls)} bypass URLs to try")
        print()
        
        # Test each URL
        for i, url_info in enumerate(urls, 1):
            print(f"[{i}/{len(urls)}] Testing: {url_info['method']}")
            
            response, working_url, working_method = self.test_url(url_info)
            
            if response:
                print(f"\n🎯 FOUND WORKING URL!")
                print(f"   Method: {working_method}")
                print(f"   URL: {working_url}")
                
                # Download the file
                success = self.download_file(response, self.filename, working_method)
                
                if success:
                    print(f"\n🎉 SUCCESS! Firmware downloaded successfully!")
                    return True
                else:
                    print(f"\n❌ Download failed, trying next URL...")
                    continue
            
            print()
        
        print(f"💔 All bypass attempts failed")
        print(f"\n💡 Alternative methods:")
        print(f"   • Use SamFirm: SamFirm -m {self.model} -r {self.region}")
        print(f"   • Use Frija with VPN")
        print(f"   • Try different time zones")
        
        return False

def main():
    """Main entry point"""
    bypass = BuiltIn403Bypass()
    
    try:
        success = bypass.run_bypass()
        sys.exit(0 if success else 1)
    except KeyboardInterrupt:
        print("\n⏹️  Interrupted by user")
        sys.exit(1)
    except Exception as e:
        print(f"\n❌ Error: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()

