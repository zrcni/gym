import ReactModal from 'react-modal'
import toastr from 'toastr'
import ReactContenteditable from 'react-contenteditable'
import * as emojiMart from 'emoji-mart'
import * as auth0spa from '@auth0/auth0-spa-js'
import * as Sentry from '@sentry/browser'
import smileParser from 'smile-parser'

window.ReactModal = ReactModal
window.toastr = toastr
window.auth0spa = auth0spa
window.ReactContenteditable = ReactContenteditable
window.emojiMart = emojiMart
window.smileParser = smileParser
window.Sentry = Sentry
