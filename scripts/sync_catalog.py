import os
import json
import hashlib
import time
import requests
from requests.auth import HTTPBasicAuth

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

CLOUDINARY_CLOUD_NAME = os.getenv("CLOUDINARY_CLOUD_NAME")
CLOUDINARY_API_KEY = os.getenv("CLOUDINARY_API_KEY")
CLOUDINARY_API_SECRET = os.getenv("CLOUDINARY_API_SECRET")

SUPABASE_URL = os.getenv("SUPABASE_URL")
SUPABASE_ANON_KEY = os.getenv("SUPABASE_ANON_KEY")

CATALOG_FILE = "scripts/catalog.json"

def get_local_md5(file_path):
    hash_md5 = hashlib.md5()
    with open(file_path, "rb") as f:
        for chunk in iter(lambda: f.read(4096), b""):
            hash_md5.update(chunk)
    return hash_md5.hexdigest()

def get_remote_resources():
    """Fetches all images from the Cloudinary account using Admin API."""
    print("Fetching existing resources from Cloudinary...")
    url = f"https://api.cloudinary.com/v1_1/{CLOUDINARY_CLOUD_NAME}/resources/image"
    auth = HTTPBasicAuth(CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET)
    
    resources = {}
    next_cursor = None
    
    while True:
        params = {"max_results": 500}
        if next_cursor:
            params["next_cursor"] = next_cursor
            
        response = requests.get(url, auth=auth, params=params)
        if response.status_code != 200:
            print(f"Error fetching resources: {response.text}")
            break
            
        data = response.json()
        for res in data.get("resources", []):
            resources[res["public_id"]] = {
                "etag": res.get("etag"),
                "secure_url": res.get("secure_url")
            }
            
        next_cursor = data.get("next_cursor")
        if not next_cursor:
            break
            
    return resources

def delete_remote_resource(public_id):
    print(f"Deleting unused resource: {public_id}...")
    url = f"https://api.cloudinary.com/v1_1/{CLOUDINARY_CLOUD_NAME}/resources/image/upload"
    auth = HTTPBasicAuth(CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET)
    params = {"public_ids[]": [public_id]}
    
    response = requests.delete(url, auth=auth, params=params)
    if response.status_code != 200:
        print(f"Error deleting {public_id}: {response.text}")

def upload_to_cloudinary(file_path, public_id):
    print(f"Uploading {file_path} to Cloudinary as {public_id}...")
    timestamp = str(int(time.time()))
    params_to_sign = f"public_id={public_id}&timestamp={timestamp}{CLOUDINARY_API_SECRET}"
    signature = hashlib.sha1(params_to_sign.encode('utf-8')).hexdigest()
    
    url = f"https://api.cloudinary.com/v1_1/{CLOUDINARY_CLOUD_NAME}/image/upload"
    with open(file_path, 'rb') as f:
        files = {'file': f}
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
        "image_url": image_url,
        # Localized fields
        "name_ja": catalog_item.get('name_ja'),
        "description_ja": catalog_item.get('description_ja'),
        "name_de": catalog_item.get('name_de'),
        "description_de": catalog_item.get('description_de'),
        "name_ru": catalog_item.get('name_ru'),
        "description_ru": catalog_item.get('description_ru'),
        "name_pt": catalog_item.get('name_pt'),
        "description_pt": catalog_item.get('description_pt'),
        "name_fr": catalog_item.get('name_fr'),
        "description_fr": catalog_item.get('description_fr'),
        "name_ar": catalog_item.get('name_ar'),
        "description_ar": catalog_item.get('description_ar'),
        "name_es": catalog_item.get('name_es'),
        "description_es": catalog_item.get('description_es'),
        "name_zh": catalog_item.get('name_zh'),
        "description_zh": catalog_item.get('description_zh'),
        "name_it": catalog_item.get('name_it'),
        "description_it": catalog_item.get('description_it')
    }
    
    url = f"{SUPABASE_URL}/rest/v1/coffee?on_conflict=name"
    response = requests.post(url, headers=headers, json=data)
    if response.status_code not in [200, 201]:
        print(f"Error syncing {catalog_item['name']}: {response.text}")

def main():
    if not os.path.exists(CATALOG_FILE):
        print(f"Error: {CATALOG_FILE} not found.")
        return

    with open(CATALOG_FILE, 'r') as f:
        catalog = json.load(f)

    # 1. Get remote state
    remote_resources = get_remote_resources()
    catalog_public_ids = {item['public_id'] for item in catalog}

    # 2. Prune unused resources from Cloudinary
    for public_id in remote_resources:
        if public_id not in catalog_public_ids:
            delete_remote_resource(public_id)

    # 3. Process catalog
    for item in catalog:
        file_path = item['local_image']
        public_id = item['public_id']
        
        if not os.path.exists(file_path):
            print(f"Warning: Local image {file_path} not found. Skipping.")
            continue

        local_hash = get_local_md5(file_path)
        remote_res = remote_resources.get(public_id)
        
        image_url = None
        if remote_res and remote_res['etag'] == local_hash:
            print(f"Skipping upload for {public_id} (Content unchanged)")
            image_url = remote_res['secure_url']
        else:
            image_url = upload_to_cloudinary(file_path, public_id)
            
        if image_url:
            sync_to_supabase(item, image_url)

    print("Sync Completed Successfully!")

if __name__ == "__main__":
    main()
