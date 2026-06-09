import json
import os
import urllib.request
import urllib.error

token = os.environ['RUSTORE_TOKEN']
package = 'io.github.pavelshel1.delta'

payload = json.dumps({'publishType': 'MANUAL'}).encode('utf-8')

req = urllib.request.Request(
    f'https://public-api.rustore.ru/public/v1/application/{package}/version',
    data=payload,
    headers={
        'Public-Token': token,
        'Content-Type': 'application/json'
    },
    method='POST'
)

try:
    with urllib.request.urlopen(req) as resp:
        result = json.loads(resp.read())
        print(f"Create draft response: {result.get('code')}")
        version_id = result['body']
        print(f"Draft version ID: {version_id}")
        with open(os.environ['GITHUB_OUTPUT'], 'a') as f:
            f.write(f'version_id={version_id}\n')
except urllib.error.HTTPError as e:
    print(f"HTTP Error {e.code}: {e.read().decode()}")
    raise
