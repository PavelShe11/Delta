import json, os, re, urllib.request, urllib.error

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

version_id = None
try:
    with urllib.request.urlopen(req) as resp:
        result = json.loads(resp.read())
        print(f"Create draft response: {result.get('code')}")
        version_id = result['body']
        print(f"Created new draft: {version_id}")
except urllib.error.HTTPError as e:
    body = e.read().decode()
    print(f"HTTP Error {e.code}: {body}")
    match = re.search(r'draft version with ID = (\d+)', body)
    if e.code == 400 and match:
        version_id = int(match.group(1))
        print(f"Reusing existing draft: {version_id}")
    else:
        raise

print(f"Using version ID: {version_id}")
with open(os.environ['GITHUB_OUTPUT'], 'a') as f:
    f.write(f'version_id={version_id}\n')
