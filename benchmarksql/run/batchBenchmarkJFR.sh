#!/usr/bin/env bash

# This should run two benchmarks per iteration, one for PSQL and one for LSD
for i in {1..1}
do
  echo "Running LSD"
	./runBenchmarkJFR.sh lsd.properties & # execute LSD

  echo "Running POSTGRES"
	./runBenchmarkJFR.sh postgres.properties # execute PSQL
done