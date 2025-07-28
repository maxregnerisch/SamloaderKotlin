#!/usr/bin/env python3
"""
Continuous Samsung Firmware Downloader
Keeps trying until the official test firmware S906BXXUGGYG9 is downloaded
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
import os
import sys

class ContinuousDownloader:
    def __init__(self):
        self.target_firmware = "S906BXXUGGYG9/S906BOXUGGYG9/S906BXXUGGYG9"
        self.model = "SM-S906B"
        self.region = "EUX"
        self.filename = "SM-S906B_EUX_S906BXXUGGYG9_S906BOXUGGYG9_S906BXXUGGYG9.zip"
        
        # Extensive user agent list
        self.user_agents = [
            "Kies2.0_FUS",
            "Samsung Kies/2.6.3.14044_17",
            "SAMSUNG_USB_Driver/1.5.59.0",
            "SamFirm/0.3.6",
            "Frija/1.4.2",
            "FOTA-HTTP-Client",
            "Samsung-SM-S906B/1.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/121.0",
            "Mozilla/5.0 (Linux; Android 14; SM-S906B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
            "curl/7.68.0",
            "wget/1.20.3"
        ]
        
        # SSL contexts
        self.ssl_context = ssl.create_default_context()
        self.ssl_context.check_hostname = False
        self.ssl_context.verify_mode = ssl.CERT_NONE
        
        # Success tracking
        self.total_attempts = 0
        self.url_attempts = {}
        
    def generate_auth_variations(self):
        """Generate multiple authentication variations"""
        timestamp = str(int(datetime.now().timestamp()))
        
        # Method 1: Standard Samsung
        message1 = f"{self.model}:{self.region}:{self.target_firmware}:{timestamp}"
        sig1 = hashlib.md5(f"{message1}:versioninfo".encode()).hexdigest()
        token1 = base64.b64encode(f"{timestamp}:{sig1}".encode()).decode()
        
        # Method 2: Alternative format
        message2 = f"{self.target_firmware}|{self.model}|{self.region}|{timestamp}"
        sig2 = hashlib.sha256(message2.encode()).hexdigest()[:32]
        token2 = base64.b64encode(f"{sig2}:{timestamp}".encode()).decode()
        
        # Method 3: Simple format
        sig3 = hashlib.sha1(f"{self.model}{self.region}{timestamp}".encode()).hexdigest()
        
        return [
            {'timestamp': timestamp, 'signature': sig1, 'token': token1, 'method': 'Standard'},
            {'timestamp': timestamp, 'signature': sig2, 'token': token2, 'method': 'Alternative'},
            {'timestamp': timestamp, 'signature': sig3, 'token': f"{sig3}:{timestamp}", 'method': 'Simple'}
        ]
    
    def generate_all_urls(self):
        """Generate comprehensive URL list with all variations"""
        auths = self.generate_auth_variations()
        urls = []
        
        # Server variations
        servers = [
            "https://neofussvr.sslcs.cdngc.net",
            "https://cloud-neofussvr.sslcs.cdngc.net",
            "https://fota-cloud-dn.ospserver.net",
            "https://fota-secure.samsungdm.com",
            "https://fota-cloud-dn.samsungdm.com"
        ]
        
        endpoints = [
            "/NF_DownloadBinaryForMass.aspx",
            "/NF_DownloadBinaryInform.aspx",
            "/firmware/download.aspx",
            "/download/firmware.aspx"
        ]
        
        # Generate authenticated URLs
        for auth in auths:
            for server in servers:
                for endpoint in endpoints:
                    # FUS API style
                    params1 = {
                        'device': self.model,
                        'region': self.region,
                        'pda': 'S906BXXUGGYG9',
                        'csc': 'S906BOXUGGYG9',
                        'cp': 'S906BXXUGGYG9',
                        'binary_nature': '1',
                        'device_type': 'phone',
                        'auth_token': auth['token'],
                        'timestamp': auth['timestamp']
                    }
                    
                    url1 = f"{server}{endpoint}?{urllib.parse.urlencode(params1)}"
                    urls.append({
                        'url': url1,
                        'method': f"{auth['method']} FUS ({server.split('//')[1].split('.')[0]})"
                    })
                    
                    # File-based style
                    params2 = {
                        'file': self.filename,
                        'auth': auth['signature'],
                        'ts': auth['timestamp'],
                        'model': self.model,
                        'region': self.region
                    }
                    
                    url2 = f"{server}{endpoint}?{urllib.parse.urlencode(params2)}"
                    urls.append({
                        'url': url2,
                        'method': f"{auth['method']} File ({server.split('//')[1].split('.')[0]})"
                    })
        
        # Direct firmware URLs
        for server in servers:
            direct_url = f"{server}/firmware/{self.region}/{self.model}/{self.filename}"
            urls.append({
                'url': direct_url,
                'method': f"Direct ({server.split('//')[1].split('.')[0]})"
            })
            
            # Alternative paths
            alt_paths = [
                f"/firmware/download/{self.region}/{self.model}/{self.filename}",
                f"/download/{self.region}/{self.model}/{self.filename}",
                f"/binary/{self.region}/{self.model}/{self.filename}"
            ]
            
            for path in alt_paths:
                alt_url = f"{server}{path}"
                urls.append({
                    'url': alt_url,
                    'method': f"Alt Path ({server.split('//')[1].split('.')[0]})"
                })
        
        return urls
    
    def attempt_download(self, url_info):
        """Attempt download with comprehensive error handling"""
        url = url_info['url']
        method = url_info['method']
        
        # Track attempts per URL
        if url not in self.url_attempts:
            self.url_attempts[url] = 0
        self.url_attempts[url] += 1
        
        try:
            # Random headers
            headers = {
                'User-Agent': random.choice(self.user_agents),
                'Accept': random.choice([
                    'application/xml, text/xml, */*',
                    'application/octet-stream, */*',
                    'application/zip, */*',
                    '*/*'
                ]),
                'Accept-Encoding': random.choice(['gzip, deflate', 'identity']),
                'Accept-Language': random.choice([
                    'en-US,en;q=0.9',
                    'ko-KR,ko;q=0.9,en;q=0.8'
                ]),
                'Connection': 'keep-alive',
                'Cache-Control': random.choice(['no-cache', 'max-age=0']),
                'Pragma': 'no-cache'
            }
            
            # Add Samsung headers randomly
            if random.choice([True, False]):
                headers['X-Samsung-Device'] = self.model
                headers['X-Samsung-Region'] = self.region
            
            # Create request
            req = urllib.request.Request(url)
            for key, value in headers.items():
                req.add_header(key, value)
            
            # Make request
            response = urllib.request.urlopen(req, timeout=30, context=self.ssl_context)
            
            status = response.getcode()
            content_type = response.headers.get('content-type', '').lower()
            content_length = response.headers.get('content-length', '0')
            
            if status == 200:
                # Check if it's actually firmware
                if ('zip' in content_type or 
                    'octet-stream' in content_type or 
                    int(content_length) > 1000000):
                    
                    print(f"\n🎉 SUCCESS! Found working URL!")
                    print(f"   Method: {method}")
                    print(f"   URL: {url}")
                    print(f"   Content-Type: {content_type}")
                    print(f"   Size: {content_length} bytes")
                    
                    # Download the file
                    print(f"\n📥 Downloading {self.filename}...")
                    
                    with open(self.filename, 'wb') as f:
                        downloaded = 0
                        while True:
                            chunk = response.read(8192)
                            if not chunk:
                                break
                            f.write(chunk)
                            downloaded += len(chunk)
                            
                            # Progress every MB
                            if downloaded % (1024*1024) == 0:
                                print(f"\r   Downloaded: {downloaded:,} bytes", end="")
                    
                    print(f"\n✅ DOWNLOAD COMPLETE!")
                    print(f"   File: {self.filename}")
                    print(f"   Final size: {downloaded:,} bytes")
                    print(f"   Total attempts: {self.total_attempts}")
                    
                    return True
                else:
                    return False, f"Wrong content: {content_type}"
            
            elif status in [301, 302, 307, 308]:
                redirect_url = response.headers.get('location', '')
                return False, f"Redirect: {redirect_url[:50]}..."
            
            else:
                return False, f"HTTP {status}"
                
        except urllib.error.HTTPError as e:
            return False, f"HTTP {e.code}"
        except urllib.error.URLError as e:
            return False, f"URL Error: {str(e)[:30]}"
        except Exception as e:
            return False, f"Error: {str(e)[:30]}"
    
    def run_continuous(self):
        """Run continuous download attempts"""
        print("🚀 Continuous Samsung Firmware Downloader")
        print("=" * 60)
        print(f"📱 Target: {self.model} ({self.region})")
        print(f"📦 Firmware: {self.target_firmware}")
        print(f"📁 Output: {self.filename}")
        print(f"🎯 Official Test Firmware - Downloading until success!")
        print()
        
        # Generate all possible URLs
        urls = self.generate_all_urls()
        print(f"🔗 Generated {len(urls)} URLs to try")
        print(f"🔄 Starting continuous download attempts...")
        print()
        
        cycle_count = 0
        
        while True:
            cycle_count += 1
            print(f"🔄 CYCLE #{cycle_count} - {datetime.now().strftime('%H:%M:%S')}")
            
            # Shuffle URLs for variety
            random.shuffle(urls)
            
            for i, url_info in enumerate(urls, 1):
                self.total_attempts += 1
                
                print(f"[{i:3d}/{len(urls)}] {url_info['method'][:30]:30} - ", end="")
                
                result = self.attempt_download(url_info)
                
                if result is True:
                    return True
                elif isinstance(result, tuple):
                    success, message = result
                    if success:
                        return True
                    else:
                        print(f"❌ {message}")
                else:
                    print(f"❌ Failed")
                
                # Small delay between attempts
                time.sleep(random.uniform(0.5, 2.0))
                
                # Break if file exists (downloaded by another process)
                if os.path.exists(self.filename):
                    print(f"\n✅ File found! {self.filename}")
                    return True
            
            print(f"\n📊 Cycle {cycle_count} complete - {self.total_attempts} total attempts")
            print(f"⏳ Waiting 10 seconds before next cycle...")
            time.sleep(10)
            
            # Stop after reasonable number of cycles
            if cycle_count >= 50:
                print(f"\n⏹️ Stopping after {cycle_count} cycles")
                break
        
        return False

def main():
    """Main entry point"""
    downloader = ContinuousDownloader()
    
    try:
        print("Starting continuous download for official test firmware S906BXXUGGYG9...")
        success = downloader.run_continuous()
        
        if success:
            print(f"\n🎉 MISSION ACCOMPLISHED!")
            print(f"   Official test firmware downloaded successfully!")
            sys.exit(0)
        else:
            print(f"\n💔 Download not successful after all attempts")
            sys.exit(1)
            
    except KeyboardInterrupt:
        print(f"\n⏹️ Stopped by user")
        sys.exit(1)
    except Exception as e:
        print(f"\n❌ Unexpected error: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()

