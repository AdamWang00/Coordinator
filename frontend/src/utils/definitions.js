export const baseUrl = "http://coordinator.adamwang.me"

// Self-Explanatory
export const groupIdLength = 6
export const usernameMinLength = 2
export const usernameMaxLength = 20
export const passwordMinLength = 2
export const passwordMaxLength = 20
export const meetingGroupNameMinLength = 2
export const meetingGroupNameMaxLength = 100
export const meetingGroupViewRefreshDelay = 5000
export const dayNames = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"]

// Various definitions to be kept in sync with the css
export const subdivisions = 4
export const timeIntervalMinutes = 60 / 4
export const rows = subdivisions * 24
export const cols = 7
export const horizontalBorderWidth = 1
export const verticalBorderWidth = 1
export const compressDelay = 500

// Creates a css color string
export const pinnedOpacity = 1
export const defaultOpacity = 0.5
export const rgba = (r, g, b, a = defaultOpacity) =>
    "rgba(" + r + ", " + g + ", " + b + ", " + a + ")"

// Sneaky currying
export function curry(f) { // curry(f) does the currying transform
  return function(a) {
    return function(b) {
      return f(a, b);
    };
  };
}

// String printing
export const minutesToHourMin = input => {
  const hours = Math.floor(input / 60)
  const hoursString = hours ? hours + "h " : ""
  const min = input % 60
  const minString = min ? min + "min" : ""
  return hoursString + minString
}

// Color conversions
export const numToHex2Digit = (num) => {
  let hex = Number(num).toString(16)
  if (hex.length < 2) hex = "0" + hex
  return hex
}
export const rgbToHexString = (r, g, b) => {
  return "#" + numToHex2Digit(r) + numToHex2Digit(g) + numToHex2Digit(b)
}
export const hexStringToRgb = (hexString) => {
  // StackOverflow: Tim Down 2011
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hexString)
  return result ? {
    r: parseInt(result[1], 16),
    g: parseInt(result[2], 16),
    b: parseInt(result[3], 16)
  } : null
}
export const hexStringToRgba = (hexString, a = defaultOpacity) => {
  const rgb = hexStringToRgb(hexString)
  return rgba(rgb.r, rgb.g, rgb.b, a)
}

// For axios
export const config = (loginToken) => {
  return {
    headers: {
      "Content-Type": "application/json",
      "Authorization": loginToken,
    }
  }
}

// Misc for brevity
export const increment = x => x + 1
export const enterToClick = (callbackFn, e, idToFocusOn = null) => {
  if (e.key !== "Enter") return
  callbackFn(e)
  if (idToFocusOn) {
    setTimeout(() =>
        document.getElementById(idToFocusOn).focus(), 0)
  }
}

// Misc for dialogs
export const diagnosticLogStyle = "color: yellowgreen"
export const logoutConfirmationMessage =
    "Would you like to log out?"
export const lockConfirmationMessage = "Would you like to lock this group? " +
    "Note that users will no longer be able to join!"
export const leaveConfirmationMessage = "Would you like to leave this group? You will " +
    "also leave this group's parent group (if exists) and this group's subgroups (if any)!"
export const deleteConfirmationMessage = "Would you like to delete this group? " +
    "Doing so will delete this group's subgroups (if any)! "
export const roundConfirmationMessage = (a, b) => "It seems that you set " +
    "your meeting duration to " + b + " minutes. Is it okay if we round that " +
    "up to " + a + " minutes?"
export const roundCancelAlert =
    "Alright, no rounding; so please edit your input!"
export const inputTooShortAlert = (a, b, c) => "Hey, that " + c + " is way too " +
    "short! Please provide one above " + b + " characters (current: " + a + ")."
export const inputTooLongAlert = (a, b, c) => "Hey, that " + c + " is way too " +
    "long! Please stay under " + b + " characters (current: " + a + ")."