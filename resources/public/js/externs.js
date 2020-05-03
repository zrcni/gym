var auth0spa = function () {}

auth0spa.Auth0Client = function () {
  return {
    getTokenSilently: function () {
      return Promise.resolve()
    },
    logout: function () {},
    loginWithRedirect: function () {
      return Promise.resolve()
    },
    loginWithPopup: function () {
      return Promise.resolve()
    }
  }
}
