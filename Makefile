build-site:
	API_URL=${API_URL} npm run build && lein minify-assets && lein cljsbuild once min

build-server:
	PG_URL=${PG_URL} FRONTEND_URL=${FRONTEND_URL} lein uberjar
