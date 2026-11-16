#!/usr/bin/env bash
set -euo pipefail

version=$1
plugin=$(cd "$(dirname "$2")" && pwd)/$(basename "$2")
root=$(cd "$(dirname "$0")/.." && pwd)
fixtures=$root/scripts/fixtures
cache=$root/run/cache
server=$root/run/boot/$version

rm -rf "$server"
mkdir -p "$cache" "$server/plugins/MaxLevels" "$server/world/datapacks"
cp "$plugin" "$server/plugins/"
cp "$fixtures/config.yml" "$server/plugins/MaxLevels/"
cp -R "$fixtures/datapack" "$server/world/datapacks/maxlevels-test"
printf 'server-port=%d\nonline-mode=false\n' $((20000 + RANDOM % 20000)) > "$server/server.properties"

jar=$cache/paper-$version.jar
if [ ! -f "$jar" ]; then
  url=$(curl -sf -A maxlevels-boot-test "https://fill.papermc.io/v3/projects/paper/versions/$version/builds/latest" |
    jq -r '.downloads."server:default".url')
  curl -sfL -A maxlevels-boot-test -o "$jar" "$url"
fi

cd "$server"
mkfifo console
java -Dcom.mojang.eula.agree=true -Xmx1G -jar "$jar" --nogui < console > server.log 2>&1 &
pid=$!
exec 3> console
trap 'kill $pid 2>/dev/null || true' EXIT

plain_log() {
  sed $'s/\x1b\\[[0-9;]*m//g' server.log
}

await() {
  for _ in $(seq 1 600); do
    grep -qE "$1" <(plain_log) && return 0
    kill -0 $pid 2>/dev/null || break
    sleep 0.5
  done
  echo "Timed out waiting for: $1"
  tail -n 40 server.log
  exit 1
}

run() {
  echo "$1" >&3
  await "$2"
}

await 'Done \('
run 'forceload add 0 0' 'Marked chunk|is already marked'
run 'summon minecraft:armor_stand 0 100 0 {NoGravity:1b}' 'Summoned new'
run 'item replace entity @e[type=minecraft:armor_stand,limit=1] weapon.mainhand with minecraft:diamond_sword' 'Replaced'
run 'enchant @e[type=minecraft:armor_stand,limit=1] minecraft:sharpness 10' 'Applied enchantment Sharpness X to'
run 'enchant @e[type=minecraft:armor_stand,limit=1] minecraft:sharpness 11' 'higher than the maximum level of 10'
run 'enchant @e[type=minecraft:armor_stand,limit=1] maxlevels_test:lifesteal 5' 'Applied enchantment Lifesteal V to'
echo stop >&3
wait $pid || true
trap - EXIT

failed=0
expect() {
  if grep -qF "$1" <(plain_log); then echo "ok    $1"; else echo "FAIL  $1"; failed=1; fi
}
expect 'Applied sharpness 5 -> 10, efficiency 5 -> 3, maxlevels_test:lifesteal 1 -> 5'
expect 'Unknown enchantments: missing_enchantment'
expect 'Anvil max cost 39 -> 100'
expect "Ignoring 'smite: 300': level must be a whole number from 1 to 255"
expect 'Applied enchantment Sharpness X to'
expect 'higher than the maximum level of 10'
expect 'Applied enchantment Lifesteal V to'
if grep 'MaxLevels' <(plain_log) | grep -qiE 'error|exception'; then
  echo "FAIL  MaxLevels logged an error"
  failed=1
fi
exit $failed
