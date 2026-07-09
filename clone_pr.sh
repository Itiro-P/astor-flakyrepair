#!/bin/bash
# uso: ./clone_pr.sh <url> [pasta-destino]

URL="$1"
REPO="${URL##*/}"
REPO="${REPO%.git}"

BASE_DEST="${2:-.}"
DEST="$BASE_DEST/$REPO"

mkdir -p "$BASE_DEST" || exit 1

git clone --single-branch --depth 2 "$URL" "$DEST" || exit 1
cd "$DEST" || exit 1
git checkout HEAD~1