#!/bin/bash

set -e

./refresh-commit-sha.sh

flyctl deploy
