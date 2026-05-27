#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
SPECS_DIR="$REPO_ROOT/specs"
FEATURE_JSON="$REPO_ROOT/.specify/feature.json"

INCLUDE_ALL=false
INCLUDE_EXAMPLE=false
LIST_ONLY=false
ACTIVATE_NEXT=false

usage() {
  cat <<'EOF'
Usage:
  ./11-scripts/plan-each.sh [--list] [--next] [--all] [--include-example]

What it does:
  Prepares feature directories for /speckit.plan one by one by updating
  .specify/feature.json. The /speckit.plan command itself runs in Copilot Chat,
  so this script cannot execute it directly from bash.

Options:
  --list             List matching feature directories and exit
  --next             Activate only the first matching feature and exit
  --all              Include features that already have plan.md
  --include-example  Include example feature directories
  --help             Show this message

Defaults:
  - Scans top-level directories under specs/ containing spec.md
  - Skips directories that already have plan.md
  - Skips directories with "example" in the directory name
  - Without --list/--next, walks the matching features interactively
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --list)
      LIST_ONLY=true
      ;;
    --next)
      ACTIVATE_NEXT=true
      ;;
    --all)
      INCLUDE_ALL=true
      ;;
    --include-example)
      INCLUDE_EXAMPLE=true
      ;;
    --help|-h)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      echo >&2
      usage >&2
      exit 1
      ;;
  esac
  shift
done

if [[ ! -d "$SPECS_DIR" ]]; then
  echo "specs/ directory not found: $SPECS_DIR" >&2
  exit 1
fi

if [[ ! -f "$FEATURE_JSON" ]]; then
  echo ".specify/feature.json not found: $FEATURE_JSON" >&2
  exit 1
fi

collect_features() {
  local dir base rel

  while IFS= read -r dir; do
    base="$(basename "$dir")"
    rel="specs/$base"

    [[ -f "$dir/spec.md" ]] || continue

    if [[ "$INCLUDE_EXAMPLE" != true && "$base" == *example* ]]; then
      continue
    fi

    if [[ "$INCLUDE_ALL" != true && -f "$dir/plan.md" ]]; then
      continue
    fi

    printf '%s\n' "$rel"
  done < <(find "$SPECS_DIR" -mindepth 1 -maxdepth 1 -type d | sort)
}

activate_feature() {
  local rel_path="$1"

  cat > "$FEATURE_JSON" <<EOF
{
  "feature_directory": "$rel_path"
}
EOF
}

mapfile -t FEATURES < <(collect_features)

if [[ ${#FEATURES[@]} -eq 0 ]]; then
  echo "No matching feature directories found under specs/."
  exit 0
fi

if [[ "$LIST_ONLY" == true ]]; then
  printf '%s\n' "${FEATURES[@]}"
  exit 0
fi

if [[ "$ACTIVATE_NEXT" == true ]]; then
  activate_feature "${FEATURES[0]}"
  echo "Active feature set to: ${FEATURES[0]}"
  echo "Next step: run /speckit.plan in Copilot Chat."
  exit 0
fi

for index in "${!FEATURES[@]}"; do
  feature="${FEATURES[$index]}"
  activate_feature "$feature"

  echo
  echo "[$((index + 1))/${#FEATURES[@]}] Active feature: $feature"
  echo "Run /speckit.plan in Copilot Chat for this feature."
  echo "Press Enter to move to the next feature, 's' to skip, or 'q' to stop."

  read -r reply

  case "$reply" in
    q|Q)
      echo "Stopped after activating: $feature"
      exit 0
      ;;
    s|S)
      continue
      ;;
    *)
      ;;
  esac
done

echo
echo "All matching features have been activated once."
echo "The current active feature in .specify/feature.json is: ${FEATURES[-1]}"