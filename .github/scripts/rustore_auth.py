import base64
import json
import os
import urllib.request
import urllib.error
from datetime import datetime, timezone
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import padding

key_id = os.environ['RUSTORE_KEY_ID']
private_key_b64 = os.environ['RUSTORE_PRIVATE_KEY']
timestamp = datetime.now(timezone.utc).strftime('%Y-%m-%dT%H:%M:%S.%f')[:-3] + 'Z'

private_key_bytes = base64.b64decode(private_key_b64)
private_key = serialization.load_der_private_key(private_key_bytes, password=None)

message = (key_id + timestamp).encode('utf-8')
signature = private_key.sign(message, padding.PKCS1v15(), hashes.SHA512())
signature_b64 = base64.b64encode(signature).decode('utf-8')

payload = json.dumps({
    'keyId': key_id,
    'timestamp': timestamp,
    'signature': signature_b64
}).encode('utf-8')

req = urllib.request.Request(
    'https://public-api.rustore.ru/public/auth',
    data=payload,
    headers={'Content-Type': 'application/json'},
    method='POST'
)

try:
    with urllib.request.urlopen(req) as resp:
        result = json.loads(resp.read())
        print(f"Auth response code: {result.get('code')}")
        body = result.get('body', result)
        token = body['jwe']
        with open(os.environ['GITHUB_OUTPUT'], 'a') as f:
            f.write(f'token={token}\n')
except urllib.error.HTTPError as e:
    print(f"HTTP Error {e.code}: {e.read().decode()}")
    raise
