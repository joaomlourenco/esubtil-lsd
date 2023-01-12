#!/usr/bin/env bash

user="e.subtil"
dir="/temp/$user/logs/batch-$(date +"%Y-%m-%d-%T")"
echo "Creating directory $dir"
mkdir -p "$dir"

# This should run two benchmarks per iteration, one for PSQL and one for LSD
for i in {1..2}; do
  echo "Running iteration #$i"
  lsd_file="$i-lsd-$(date +"%Y-%m-%d-%T").log"
  echo "Creating log $dir/$lsd_file"
  touch "$dir/$lsd_file"
  echo "Running LSD - $lsd_file"
  ./executeBenchmark.sh >"$dir/$lsd_file" # execute LSD

  cp -R "$dir" ~/logs

  psql_file="$i-psql-$(date +"%Y-%m-%d-%T").log"
  echo "Creating $dir/$psql_file"
  touch "$dir/$psql_file"
  echo "Running POSTGRES - $psql_file"
  ./executeBenchmark.sh -m >"$dir/$psql_file" # execute PSQL

  cp -R "$dir" ~/logs
done
