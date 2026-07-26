import os
import json
import hashlib
import time
import requests

# --- Configuration ---
CLOUDINARY_CLOUD_NAME = "dzao8h1ay"
CLOUDINARY_API_KEY = "818269883432412"
CLOUDINARY_API_SECRET = "TWQzFg_c4N28mPs3g07qlC29HT8"

SUPABASE_URL = "https://kbhftumajmnqcddcgmil.supabase.co"
SUPABASE_ANON_KEY = "sb_publishable_FmfiG2VEFjYbSlAowZsp9Q_H22S8myb"

CATALOG_FILE = "scripts/catalog.json"

def upload_to_cloudinary(file_path, public_id):
    print(f"Uploading {file_path} to Cloudinary as {public_id}...")
    timestamp = str(int(time.time()))
    params_to_sign = f"public_id={public_id}&timestamp={timestamp}{CLOUDINARY_API_SECRET}"
    signature = hashlib.sha1(params_to_sign.encode('utf-8')).hexdigest()

    url = f"https://api.cloudinary.com/v1_1/{CLOUDINARY_CLOUD_NAME}/image/upload"
    files = {'file': open(file_path, 'rb')}
    data = {
        "api_key": CLOUDINARY_API_KEY,
        "public_id": public_id,
        "timestamp": timestamp,
        "signature": signature
    }

    response = requests.post(url, files=files, data=data)
    if response.status_code != 200:
        print(f"Error uploading {public_id}: {response.text}")
        return None
    return response.json()['secure_url']

def sync_to_supabase(catalog_item, image_url):
    print(f"Syncing {catalog_item['name']} to Supabase...")
    headers = {
        "apikey": SUPABASE_ANON_KEY,
        "Authorization": f"Bearer {SUPABASE_ANON_KEY}",
        "Content-Type": "application/json",
        "Prefer": "resolution=merge-duplicates"
    }

    data = {
        "name": catalog_item['name'],
        "description": catalog_item['description'],
        "category": catalog_item['category'],
        "price": catalog_item['price'],
        "image_url": image_url
    }

    # Upsert logic (name is treated as unique for this script's simplicity)
    # In a real pro setup, you'd use a UUID or slug.
    url = f"{SUPABASE_URL}/rest/v1/coffee"
    response = requests.post(url, headers=headers, json=data)
    if response.status_code not in [200, 201]:
        print(f"Error syncing {catalog_item['name']}: {response.text}")

def main():
    if not os.path.exists(CATALOG_FILE):
        print(f"Error: {CATALOG_FILE} not found.")
        return

    with open(CATALOG_FILE, 'r') as f:
        catalog = json.load(f)

    for item in catalog:
        # 1. Upload/Refresh image
        image_url = upload_to_cloudinary(item['local_image'], item['public_id'])

        # 2. Sync record
        if image_url:
            sync_to_supabase(item, image_url)

    print("Sync Completed Successfully!")

if __name__ == "__main__":
    main()
