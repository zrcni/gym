#!/bin/bash

set -e

auth0_public_key=`cat ./certs/auth0-public-key.pem`

commit_sha=`cat .commit_sha`

API_URL="${HOST_URL}" \
COMMIT_SHA="$commit_sha" \
  ./env.sh ./dist \
    COMMIT_SHA \
    SENTRY_DSN \
    API_URL \
    AUTH0_CLIENT_ID \
    AUTH0_DOMAIN

AUTH0_CLIENT_ID="TXEAK5eQSD2ECVStJzdbJPCJ08Q7gWPQ" \
AUTH0_DOMAIN="samulir.eu.auth0.com" \
API_URL="http://localhost:3001" \
AUTH0_PUBLIC_KEY="$auth0_public_key" \
JDBC_DATABASE_URL="jdbc:postgresql://localhost:5432/postgres?user=postgres&password=postgres" \
FRONTEND_URLS="http://localhost:3001" \
PORT="3001" \
HOST_URL="http://localhost:3001" \
COMMIT_SHA="$commit_sha" \
  java -Dlogback.configurationFile=resources/logback.prod.xml -cp target/gym.jar clojure.main -m gym.backend.main
