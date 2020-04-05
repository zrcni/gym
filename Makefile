build-site:
	API_URL=${API_URL} \
	npm run build && lein minify-assets && lein cljsbuild once min

build-server:
	JDBC_DATABASE_URL=${JDBC_DATABASE_URL} \
	FRONTEND_URL=${FRONTEND_URL} \
	PORT=${PORT} \
	HOST_URL=${HOST_URL} \
	lein uberjar

dev-site:
	API_URL=http://localhost:3001 \
	lein figwheel

dev-api:
	JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/postgres?user=postgres&password=postgres \
	FRONTEND_URL=http://localhost:3449 \
	PORT=3001 \
	HOST_URL=http://localhost:3001 \
	lein repl

run-api:
 java ${JVM_OPTS} -cp target/gym.jar clojure.main -m gym.server
 