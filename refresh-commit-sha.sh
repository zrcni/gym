#!/bin/bash

set -e

commit_sha=`git rev-parse --short HEAD`

echo -n "$commit_sha"  > .commit_sha
