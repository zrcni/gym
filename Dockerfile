FROM clojure:temurin-11-lein-alpine

RUN apk add --update npm git && \
  mkdir /app && \
  mkdir /dist

WORKDIR /app

COPY env.sh run-uberjar-prod.sh package.json package-lock.json \
  system.properties .commit_sha refresh-commit-sha.sh \
  build.sh project.clj webpack.config.js ./

COPY ./resources ./resources/
COPY ./src ./src/
COPY ./env ./env/
COPY ./.git ./.git/

RUN ./build.sh

CMD ["./run-uberjar-prod.sh"]
