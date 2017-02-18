#!/bin/bash

wkdb_download_wikidump.sh wkdb.conf 
wkdb_split_dump.sh wkdb.conf
wkdb_create_database_docker.sh wkdb.conf
wkdb_cleaning_up.sh wkdb.conf

