#!/bin/bash
#
# this script creates an additional database

export DATABASE=$1
export PASSWORD=$(cat /proc/sys/kernel/random/uuid)

echo creating database $DATABASE with $PASSWORD
echo $DATABASE:$PASSWORD >> /root/databases
echo $DATABASE:$PASSWORD >> /var/lib/postgresql/data/passwd

psql -U postgres <<EOF
CREATE DATABASE $DATABASE;
CREATE USER $DATABASE WITH PASSWORD '$PASSWORD';
GRANT ALL PRIVILEGES ON DATABASE $DATABASE TO $DATABASE;
EOF

psql -U postgres -d $DATABASE <<EOF
GRANT ALL ON SCHEMA public TO $DATABASE;
EOF

