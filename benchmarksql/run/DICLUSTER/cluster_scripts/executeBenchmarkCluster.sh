#!/usr/bin/env bash
set -e
user="e.subtil"
USAGE="Usage:
            -c: compile, compiles benchmark.
            -l: log, prints output to log files instead of terminal screen.
            -t: test, uses test database instead of benchmark database.
            -m: mode, uses PostgreSQL driver instead of LSD driver"

print_usage() {
  echo "$USAGE"
}

compile() {
  cd ../
  ./gradlew build
  cd run/
}

# FLAGS
compile=''
log=''
test=''
mode='lsd'

while getopts 'hcltm' flag; do
  case "${flag}" in
  h)
    echo "$USAGE"
    exit 1
    ;;
  c) compile=true ;;
  t) test=true ;;
  m) mode='postgres' ;;
  l) log=true ;;
  *)
    print_usage
    exit 1
    ;;
  esac
done

if [[ -n "$compile" ]]; then
  compile
fi

command="./runBenchmark.sh"
if [[ -n "$test" ]]; then
  mode="$mode-test"
fi
command="$command $mode.properties"
if [[ -n "$log" ]]; then
  command="mkdir -p /tmp/$user/logs && $command >/tmp/$user/logs/$mode-$(date +"%Y-%m-%d-%T").log && cp -R /tmp/$user/logs ~/logs/"
fi

# execute
echo "Executing '$command'"
eval "$command"

set +e
echo "Finished!"