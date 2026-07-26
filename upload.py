import os
import hashlib
import time
import requests

# --- Load Environment Variables ---
def load_dotenv(path=".env"):
    if not os.path.exists(path):
        return
    with open(path, "r") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            key, value = line.split("=", 1)
            os.environ[key] = value

load_dotenv()

cloud_name = os.getenv("CLOUDINARY_CLOUD_NAME")
api_key = os.getenv("CLOUDINARY_API_KEY")
api_secret = os.getenv("CLOUDINARY_API_SECRET")

def upload(file_path, public_id):
    if not api_secret:
        print("Error: CLOUDINARY_API_SECRET not set in .env")
        return

    timestamp = int(time.time())
    params = f"public_id={public_id}&timestamp={timestamp}"
    to_sign = f"{params}{api_secret}"
    signature = hashlib.sha1(to_sign.encode('utf-8')).hexdigest()
    
    url = f"https://api.cloudinary.com/v1_1/{cloud_name}/image/upload"
    
    try:
        with open(file_path, 'rb') as f:
            files = {'file': f}
            data = {
                'api_key': api_key,
                'public_id': public_id,
                'timestamp': timestamp,
                'signature': signature
            }
            response = requests.post(url, files=files, data=data)
            print(f"Status Code: {response.status_code}")
            print(response.text)
    except FileNotFoundError:
        print(f"Error: File not found at {file_path}")
    except Exception as e:
        print(f"An error occurred: {e}")

if __name__ == "__main__":
    # Example usage
    base_path = "/home/neon/programs/android/testing/app/src/main/res/drawable/"
    upload(os.path.join(base_path, "coffee_1.png"), "coffee_1")
