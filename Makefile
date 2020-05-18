build-site:
	npm run build && lein minify-assets && lein cljsbuild once min

build-server:
	AUTH0_PUBLIC_KEY=${AUTH0_PUBLIC_KEY} \
	JDBC_DATABASE_URL=${JDBC_DATABASE_URL} \
	FRONTEND_URLS=${FRONTEND_URLS} \
	PORT=${PORT} \
	HOST_URL=${HOST_URL} \
	lein uberjar

dev-site:
	COMMIT_REF=$(shell git rev-parse HEAD) \
	AUTH0_CLIENT_ID=TXEAK5eQSD2ECVStJzdbJPCJ08Q7gWPQ \
	API_URL=http://localhost:3001 \
	lein figwheel

dev-api:
	COMMIT_REF=$(shell git rev-parse HEAD) \
	JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/postgres?user=postgres&password=postgres \
	FRONTEND_URLS=http://localhost:3449 \
	PORT=3001 \
	HOST_URL=http://localhost:3001 \
	lein repl

run-api:
	java ${JVM_OPTS} -cp target/gym.jar clojure.main -m gym.server

deploy-api:
	git push heroku master
