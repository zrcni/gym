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
    },
    handleRedirectCallback: function () {
      return Promise.resolve()
    },
    getUser: function () {
      return Promise.resolve()
    }
  }
}

var smileParser = function () {}
smileParser.smileParse = function () {} 

var emojiMart = function () {}
emojiMart.Emoji = function() {}
emojiMart.Picker = function() {}

var Emoji = {
  id: '',
  colons: '',
  text: '',
  emoticons: [''],
  skin: 0,
  native: ''
}

var Sentry = {
  init: function() {},
  configureScope: function() {}
}

var SentryScope = {
  setUser: function() {},
  setTag: function() {},
  setLevel: function() {}
}

var reactColor = {
  PhotoshopPicker: function() {}
}

var parseColor = function() {}

var Color = {
  hex: '',
  hsl: {
    h: 0,
    s: 0,
    l: 0,
    a: 1
  },
  rgb: {
    r: 0,
    g: 0,
    b: 0,
    a: 0
  }
}
