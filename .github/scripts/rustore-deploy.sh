#!/usr/bin/env bash
set -euo pipefail

PACKAGE="io.github.pavelshel1.delta"
BASE_URL="https://public-api.rustore.ru"

# ── 1. Authenticate ──────────────────────────────────────────────────────────
echo "==> Authenticating..."

TIMESTAMP=$(date -u '+%Y-%m-%dT%H:%M:%S.000Z')

echo "$RUSTORE_PRIVATE_KEY" | base64 -d > /tmp/rs_key.der
openssl pkey -in /tmp/rs_key.der -inform DER -out /tmp/rs_key.pem 2>/dev/null || \
  openssl rsa -in /tmp/rs_key.der -inform DER -out /tmp/rs_key.pem 2>/dev/null
SIGNATURE=$(printf '%s' "${RUSTORE_KEY_ID}${TIMESTAMP}" | openssl dgst -sha512 -sign /tmp/rs_key.pem | base64 -w 0)
rm -f /tmp/rs_key.der /tmp/rs_key.pem

AUTH_RESP=$(curl -sf "$BASE_URL/public/auth" \
  -H "Content-Type: application/json" \
  -d "{\"keyId\":\"${RUSTORE_KEY_ID}\",\"timestamp\":\"${TIMESTAMP}\",\"signature\":\"${SIGNATURE}\"}")

[[ "$(echo "$AUTH_RESP" | jq -r '.code')" == "OK" ]] || { echo "Auth failed: $AUTH_RESP" >&2; exit 1; }
TOKEN=$(echo "$AUTH_RESP" | jq -r '.body.jwe')
echo "Auth OK"

# ── 2. Create or reuse draft ─────────────────────────────────────────────────
echo "==> Creating draft version..."

DRAFT_PAYLOAD=$(jq -n --arg w "${WHATS_NEW:-}" '{publishType: "MANUAL", whatsNew: $w}')

HTTP_CODE=$(curl -s -o /tmp/rs_draft.json -w "%{http_code}" \
  -X POST "$BASE_URL/public/v1/application/$PACKAGE/version" \
  -H "Public-Token: $TOKEN" \
  -H "Content-Type: application/json" \
  -d "$DRAFT_PAYLOAD")
DRAFT_RESP=$(cat /tmp/rs_draft.json)

if [[ "$HTTP_CODE" == "200" ]] && [[ "$(echo "$DRAFT_RESP" | jq -r '.code')" == "OK" ]]; then
  VERSION_ID=$(echo "$DRAFT_RESP" | jq -r '.body')
  echo "Created new draft: $VERSION_ID"
elif [[ "$HTTP_CODE" == "400" ]] && [[ "$DRAFT_RESP" =~ draft\ version\ with\ ID\ =\ ([0-9]+) ]]; then
  VERSION_ID="${BASH_REMATCH[1]}"
  echo "Reusing existing draft: $VERSION_ID"
else
  echo "Create draft failed (HTTP $HTTP_CODE): $DRAFT_RESP" >&2
  exit 1
fi

# ── 3. Upload AAB ─────────────────────────────────────────────────────────────
echo "==> Uploading AAB..."

AAB_PATH=$(find app/build/outputs/bundle/release -name "*.aab" | head -1)
[[ -n "$AAB_PATH" ]] || { echo "ERROR: AAB file not found" >&2; exit 1; }
echo "File: $AAB_PATH ($(du -h "$AAB_PATH" | cut -f1))"

HTTP_CODE=$(curl -s -o /tmp/rs_upload.json -w "%{http_code}" \
  -X POST "$BASE_URL/public/v1/application/$PACKAGE/version/$VERSION_ID/aab" \
  -H "Public-Token: $TOKEN" \
  -F "file=@$AAB_PATH")
UPLOAD_RESP=$(cat /tmp/rs_upload.json)

echo "Upload HTTP $HTTP_CODE: $UPLOAD_RESP"
[[ "$(echo "$UPLOAD_RESP" | jq -r '.code')" == "OK" ]] || { echo "Upload failed" >&2; exit 1; }

# ── 4. Submit for moderation ──────────────────────────────────────────────────
echo "==> Submitting for moderation..."

HTTP_CODE=$(curl -s -o /tmp/rs_commit.json -w "%{http_code}" \
  -X POST "$BASE_URL/public/v1/application/$PACKAGE/version/$VERSION_ID/commit" \
  -H "Public-Token: $TOKEN")
COMMIT_RESP=$(cat /tmp/rs_commit.json)

echo "Commit HTTP $HTTP_CODE: $COMMIT_RESP"
[[ "$(echo "$COMMIT_RESP" | jq -r '.code')" == "OK" ]] || { echo "Submit failed" >&2; exit 1; }
echo "Done. Version $VERSION_ID submitted for moderation."
