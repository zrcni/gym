(ns gym.frontend.config)

(def ^:private env js/window.__env__)

(goog-define commit-sha "")
(def sentry-dsn (.-SENTRY_DSN env))
(def api-url (.-API_URL env))
(def auth0-client-id (.-AUTH0_CLIENT_ID env))
(def auth0-domain (.-AUTH0_DOMAIN env))
(def debug? false?)
