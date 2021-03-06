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

dev-seed:
	lein run -m gym.backend.seed

run-prod:
	java ${JVM_OPTS} -cp target/gym.jar clojure.main -m gym.backend.main

deploy-prod:
	git push heroku master
