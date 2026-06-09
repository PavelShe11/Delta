import glob
import http.client
import os
import sys
import uuid

token = os.environ['RUSTORE_TOKEN']
version_id = os.environ['VERSION_ID']
package = 'io.github.pavelshel1.delta'

aab_files = glob.glob('app/build/outputs/bundle/release/*.aab')
if not aab_files:
    print("ERROR: AAB file not found", file=sys.stderr)
    sys.exit(1)

aab_path = aab_files[0]
file_size = os.path.getsize(aab_path)
file_name = os.path.basename(aab_path)
print(f"File: {aab_path}")
print(f"Size: {file_size} bytes ({file_size / 1024 / 1024:.2f} MB)")
print(f"Name: {file_name}")

boundary = uuid.uuid4().hex
crlf = '\r\n'

prefix = (
    f'--{boundary}{crlf}'
    f'Content-Disposition: form-data; name="file"; filename="{file_name}"{crlf}'
    f'Content-Type: application/octet-stream{crlf}{crlf}'
).encode('utf-8')

suffix = f'{crlf}--{boundary}--{crlf}'.encode('utf-8')

content_length = len(prefix) + file_size + len(suffix)

conn = http.client.HTTPSConnection('public-api.rustore.ru', timeout=600)
conn.putrequest('POST', f'/public/v1/application/{package}/version/{version_id}/aab')
conn.putheader('Public-Token', token)
conn.putheader('Content-Type', f'multipart/form-data; boundary={boundary}')
conn.putheader('Content-Length', str(content_length))
conn.endheaders()

conn.send(prefix)
with open(aab_path, 'rb') as f:
    while True:
        chunk = f.read(64 * 1024)
        if not chunk:
            break
        conn.send(chunk)
conn.send(suffix)

resp = conn.getresponse()
body = resp.read().decode('utf-8', errors='replace')

print(f"HTTP {resp.status} {resp.reason}")
print(f"Response body: {body}")

if resp.status >= 400:
    sys.exit(1)

conn2 = http.client.HTTPSConnection('public-api.rustore.ru', timeout=30)
conn2.request(
    'GET',
    f'/public/v1/application/{package}/version/{version_id}',
    headers={'Public-Token': token}
)
resp2 = conn2.getresponse()
print(f"\nVersion state after upload:")
print(resp2.read().decode('utf-8', errors='replace'))
