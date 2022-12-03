#!/bin/bash

set -e

./refresh-commit-sha.sh

export NODE_OPTIONS='--openssl-legacy-provider'

npm install

npm run build

lein uberjar
