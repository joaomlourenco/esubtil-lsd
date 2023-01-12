#!/usr/bin/env bash
user="e.subtil"
database_cluster_dir="/tmp/$user/psql/databases/"

source set_env.sh

# erase any previous directories and server
/home/$user/pgsql/bin/pg_ctl stop
rm -rf /tmp/$user

# create database directory
set -e
mkdir -p $database_cluster_dir

# create database cluster
/home/$user/pgsql/bin/initdb

# copy configuration files
cp postgresql.conf pg_hba.conf $database_cluster_dir

# start server
/home/$user/pgsql/bin/pg_ctl start

# create benchmark user and database
/home/$user/pgsql/bin/createuser -Plds benchmarksql
/home/$user/pgsql/bin/createdb -O benchmarksql benchmarksql_lsd
/home/$user/pgsql/bin/createdb -O benchmarksql benchmarksql_psql

source build_dbs.sh

set +e
