#!/usr/bin/env bash
set -euo pipefail

oldest=1.21
newest=26.3

version=$1
jar=$2
pack=$3

game_versions=$(curl -sf -A maxlevels-release https://api.modrinth.com/v2/tag/game_version |
  jq -c --arg oldest "$oldest" --arg newest "$newest" '
    [.[] | select(.version_type == "release") | .version]
    | .[index($newest):index($oldest) + 1]
    | reverse')

data=$(jq -n \
  --arg version "$version" \
  --argjson game_versions "$game_versions" \
  --arg changelog "https://github.com/minodevss/maxlevels/releases/tag/v$version" \
  '{
    project_id: "Fz9s2F2z",
    name: "MaxLevels \($version)",
    version_number: $version,
    changelog: $changelog,
    dependencies: [],
    game_versions: $game_versions,
    version_type: "release",
    loaders: ["paper", "folia", "purpur"],
    featured: true,
    file_parts: ["plugin", "resourcepack"],
    primary_file: "plugin"
  }')

curl -sf -A maxlevels-release \
  -H "Authorization: $MODRINTH_TOKEN" \
  -F "data=$data" \
  -F "plugin=@$jar" \
  -F "resourcepack=@$pack" \
  https://api.modrinth.com/v2/version | jq -r '"Published \(.version_number) for \(.game_versions | join(", "))"'
