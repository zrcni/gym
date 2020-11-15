build-site:
	npm run build && lein minify-assets && lein cljsbuild once min

build-server:
	AUTH0_PUBLIC_KEY=${AUTH0_PUBLIC_KEY} \
	JDBC_DATABASE_URL=${JDBC_DATABASE_URL} \
	REDIS_URL=${REDIS_URL} \
	FRONTEND_URLS=${FRONTEND_URLS} \
	PORT=${PORT} \
	HOST_URL=${HOST_URL} \
	API_URL="" \
	AUTH0_CLIENT_ID=${AUTH0_CLIENT_ID} \
	AUTH0_DOMAIN=${AUTH0_DOMAIN} \
	SENTRY_DSN=${SENTRY_DSN} \
	lein uberjar

dev-site:
	COMMIT_REF=$(shell git rev-parse HEAD) \
	lein figwheel

dev-api:
	COMMIT_REF=$(shell git rev-parse HEAD) \
	lein repl

dev-seed:
	lein run -m gym.seed

run-api:
	java ${JVM_OPTS} -cp target/gym.jar clojure.main -m gym.main

deploy-api:
	git push heroku master
