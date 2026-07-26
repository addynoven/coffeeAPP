import hashlib
import time
import requests

cloud_name = "dzao8h1ay"
api_key = "841961614769357"
api_secret = "TWQzFg_c4N28mPs3g07qlC29HT8"

def upload(file_path, public_id):
    timestamp = int(time.time())
    params = f"public_id={public_id}&timestamp={timestamp}"
    to_sign = f"{params}{api_secret}"
    signature = hashlib.sha1(to_sign.encode('utf-8')).hexdigest()
    
    url = f"https://api.cloudinary.com/v1_1/{cloud_name}/image/upload"
    files = {'file': open(file_path, 'rb')}
    data = {
        'api_key': api_key,
        'public_id': public_id,
        'timestamp': timestamp,
        'signature': signature
    }
    
    response = requests.post(url, files=files, data=data)
    print(response.text)

upload("/home/neon/programs/android/testing/app/src/main/res/drawable/coffee_1.png", "coffee_1")
