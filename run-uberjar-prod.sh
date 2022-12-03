#!/bin/bash

set -e
# NOTE: environment variables in production are provided via runtime environment

commit_sha=`cat .commit_sha`

API_URL="${HOST_URL}" \
COMMIT_SHA="$commit_sha" \
  ./env.sh ./dist \
    COMMIT_SHA \
    SENTRY_DSN \
    API_URL \
    AUTH0_CLIENT_ID \
    AUTH0_DOMAIN

COMMIT_SHA="$commit_sha" \
  java -Dlogback.configurationFile=resources/logback.prod.xml -cp target/gym.jar clojure.main -m gym.backend.main
