#!/bin/bash
set -ex

SCRIPT_DIR=$(dirname $(realpath $0))

if [[ -z $(command -v cockroach) ]]; then
    echo "CockroachDB not installed!"
    # Install CockroachDB
    cd ${HOME}
    curl https://binaries.cockroachdb.com/cockroach-v21.2.5.linux-amd64.tgz | tar -xz
    sudo cp -i cockroach-v21.2.5.linux-amd64/cockroach /usr/local/bin/
    sudo mkdir -p /usr/local/lib/cockroach
    sudo cp -i cockroach-v21.2.5.linux-amd64/lib/libgeos.so /usr/local/lib/cockroach/
    sudo cp -i cockroach-v21.2.5.linux-amd64/lib/libgeos_c.so /usr/local/lib/cockroach/
fi

# Enter the root dir of the repo.
cd ${SCRIPT_DIR}/../

cockroach start --insecure --join=localhost:26257 --background

cockroach init --insecure --host=localhost:26257

# Create DB and tables.
cat sql/cockroachdb_init.sql | cockroach sql --url "postgresql://root@localhost:26257?sslmode=disable"

echo "==== Finished initializing CockroachDB. ===="
