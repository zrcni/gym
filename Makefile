build-site:
	API_URL=${API_URL} npm run build && lein minify-assets && lein cljsbuild once min
