#!/bin/bash

set -e

./refresh-commit-sha.sh

npm install

npm run build

lein uberjar
