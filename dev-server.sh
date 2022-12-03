#!/bin/bash

set -e

commit_sha=`git rev-parse --short HEAD`

AUTH0_CLIENT_ID="TXEAK5eQSD2ECVStJzdbJPCJ08Q7gWPQ" \
AUTH0_DOMAIN="samulir.eu.auth0.com" \
COMMIT_SHA="$commit_sha" \
API_URL="http://localhost:3001" \
  ./env.sh ./dist \
    COMMIT_SHA \
    SENTRY_DSN \
    API_URL \
    AUTH0_CLIENT_ID \
    AUTH0_DOMAIN

auth0_public_key=`cat ./certs/auth0-public-key.pem`

AUTH0_CLIENT_ID="TXEAK5eQSD2ECVStJzdbJPCJ08Q7gWPQ" \
AUTH0_DOMAIN="samulir.eu.auth0.com" \
API_URL="http://localhost:3001" \
AUTH0_PUBLIC_KEY="$auth0_public_key" \
JDBC_DATABASE_URL="jdbc:postgresql://localhost:5432/postgres?user=postgres&password=postgres" \
FRONTEND_URLS="http://localhost:3001" \
PORT="3001" \
HOST_URL="http://localhost:3001" \
  lein repl
