build-site:
	npm run build && lein minify-assets && lein cljsbuild once min

build-server:
	AUTH0_PUBLIC_KEY=${AUTH0_PUBLIC_KEY} \
	JDBC_DATABASE_URL=${JDBC_DATABASE_URL} \
	REDIS_URL=${REDIS_URL} \
	FRONTEND_URLS=${FRONTEND_URLS} \
	PORT=${PORT} \
	HOST_URL=${HOST_URL} \
	lein uberjar

dev-site:
	COMMIT_REF=$(shell git rev-parse HEAD) \
	lein figwheel

dev-api:
	COMMIT_REF=$(shell git rev-parse HEAD) \
	lein repl

run-api:
	java ${JVM_OPTS} -cp target/gym.jar clojure.main -m gym.server

deploy-api:
	git push heroku master
