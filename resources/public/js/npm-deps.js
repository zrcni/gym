import React from 'react'
import ReactDOM from 'react-dom'
import ReactModal from 'react-modal'
import toastr from 'toastr'
import * as reactColor from 'react-color'
import ReactContenteditable from 'react-contenteditable'
import * as auth0spa from '@auth0/auth0-spa-js'
import * as Sentry from '@sentry/browser'
import EmojiPickerReact from 'emoji-picker-react'
import parseColor from 'parse-color'

window.React = React
window.ReactDOM = ReactDOM
window.ReactModal = ReactModal
window.toastr = toastr
window.reactColor = reactColor
window.auth0spa = auth0spa
window.ReactContenteditable = ReactContenteditable
window.EmojiPickerReact = EmojiPickerReact
window.Sentry = Sentry
window.parseColor = parseColor

window.getCaretPosition = function(editableDiv) {
  var caretPos = 0,
    sel, range;
  if (window.getSelection) {
    sel = window.getSelection();
    if (sel.rangeCount) {
      range = sel.getRangeAt(0);
      if (range.commonAncestorContainer.parentNode == editableDiv) {
        caretPos = range.endOffset;
      }
    }
  } else if (document.selection && document.selection.createRange) {
    range = document.selection.createRange();
    if (range.parentElement() == editableDiv) {
      var tempEl = document.createElement("span");
      editableDiv.insertBefore(tempEl, editableDiv.firstChild);
      var tempRange = range.duplicate();
      tempRange.moveToElementText(tempEl);
      tempRange.setEndPoint("EndToEnd", range);
      caretPos = tempRange.text.length;
    }
  }
  return caretPos;
}
