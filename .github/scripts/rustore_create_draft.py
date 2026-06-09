import json, os, urllib.request, urllib.error

token = os.environ['RUSTORE_TOKEN']
package = 'io.github.pavelshel1.delta'

# Ищем существующий черновик
req_list = urllib.request.Request(
    f'https://public-api.rustore.ru/public/v1/application/{package}/version',
    headers={'Public-Token': token},
    method='GET'
)

version_id = None
try:
    with urllib.request.urlopen(req_list) as resp:
        result = json.loads(resp.read())
        versions = result.get('body', {}).get('content', [])
        for v in versions:
            if v.get('appVersionStatus') == 'DRAFT':
                version_id = v['versionId']
                print(f"Found existing draft: {version_id}")
                break
except urllib.error.HTTPError as e:
    print(f"GET versions error {e.code}: {e.read().decode()}")
    raise

# Если черновика нет — создаём
if version_id is None:
    payload = json.dumps({'publishType': 'MANUAL'}).encode('utf-8')
    req_post = urllib.request.Request(
        f'https://public-api.rustore.ru/public/v1/application/{package}/version',
        data=payload,
        headers={
            'Public-Token': token,
            'Content-Type': 'application/json'
        },
        method='POST'
    )
    try:
        with urllib.request.urlopen(req_post) as resp:
            result = json.loads(resp.read())
            print(f"Create draft response: {result.get('code')}")
            version_id = result['body']
            print(f"Created new draft: {version_id}")
    except urllib.error.HTTPError as e:
        print(f"POST error {e.code}: {e.read().decode()}")
        raise

print(f"Using version ID: {version_id}")
with open(os.environ['GITHUB_OUTPUT'], 'a') as f:
    f.write(f'version_id={version_id}\n')