import './zPanelViews.css';
import axios from "axios";
import {useState} from "react";
import TextBox from "./Textbox";
import {randInt} from "./utils/randomHelperFunctions";
import {
  baseUrl,
  diagnosticLogStyle,
  inputTooLongAlert, inputTooShortAlert, passwordMaxLength, passwordMinLength,
  usernameMaxLength,
  usernameMinLength
} from "./utils/definitions";

const loginHeaderOptions =
    ["Welcome back!", "Hey there! (☞ﾟヮﾟ)☞", "Howdy. ᕕ(ᐛ)ᕗ", "Hello again!"]
const loginSubheaderDefault =
    "Please log in below:"
const registerHeaderOptions =
    ["A new user?", "Pleased to meet ya!", "Signing up? :P", "New phone, who dis?"]
const registerSubheaderDefault =
    "Enter your details to create a new account:"

// Functional component: the login page
function LoginView({setUsername, setLoginToken}) {
  // States
  const [loginHeader, setLoginHeader] = useState(loginHeaderOptions[randInt(0, 3)])
  const [loginSubheader, setLoginSubheader] = useState(loginSubheaderDefault)
  const [usernameInput, setUsernameInput] = useState("")
  const [passwordInput, setPasswordInput] = useState("")
  const [registeringMode, setRegisteringMode] = useState(false)

  // Listeners
  const onSubmitRegisterOrLogin = e => {
    e.preventDefault()
    e.stopPropagation()
    let postUrl, body, errorAlert
    if (registeringMode) {
      // Check username for length
      if (usernameInput.length < usernameMinLength) {
        alert(inputTooShortAlert(usernameInput.length, usernameMinLength, "username"))
        return
      } else if (usernameInput.length > usernameMaxLength) {
        alert(inputTooLongAlert(usernameInput.length, usernameMaxLength, "username"))
        return
      }
      // Check password for length
      if (passwordInput.length < passwordMinLength) {
        alert(inputTooShortAlert(passwordInput.length, passwordMinLength, "password"))
        return
      } else if (passwordInput.length > passwordMaxLength) {
        alert(inputTooLongAlert(passwordInput.length, passwordMaxLength, "password"))
        return
      }
      // Diagnostics
      console.log("%cSending request to register new user \"" + usernameInput + "\".",
          diagnosticLogStyle)
      // Preparing request
      postUrl = baseUrl + "/auth/register"
      body = {
        username: usernameInput,
        password: passwordInput,
      }
      errorAlert = "That username is already taken!"
    } else {
      // Diagnostics
      console.log("%cSending request to login as user \"" + usernameInput + "\".",
          diagnosticLogStyle)
      // Preparing request
      postUrl = baseUrl + "/auth/login"
      body = {
        name: "name",
        username: usernameInput,
        password: passwordInput,
      }
      errorAlert = "Incorrect login details!"
    }
    // Axios request
    axios.post(postUrl, body)
        .then(response => {
          if (typeof response.data.token === "string") {
            setLoginToken(response.data.token)
            setUsername(response.data.username)
          } else {
            console.error("Response is missing field response.data.token!")
            console.log(response)
          }
        })
        .catch(error => {
          console.log(error)
          alert(errorAlert)
        })
  }
  const onClickToggleRegisteringMode = () => {
    if (registeringMode) {
      setLoginHeader(loginHeaderOptions[randInt(0, loginHeaderOptions.length - 1)])
      setLoginSubheader(loginSubheaderDefault)
      setRegisteringMode(false)
    } else {
      setLoginHeader(registerHeaderOptions[randInt(0, registerHeaderOptions.length - 1)])
      setLoginSubheader(registerSubheaderDefault)
      setRegisteringMode(true)
    }
  }

  // Return
  return (
      <div id="LoginView">
        <form className="Panel paperTexture biggerShadow"
              onSubmit={onSubmitRegisterOrLogin} autoComplete="off">
          <div className="PanelHeader">
            {loginHeader}
          </div>
          <div className="PanelSubheader">
            {loginSubheader}
          </div>
          <TextBox label={"Username:"} placeholder={"username"} type={"text"}
                   extraAttrs={{
                     autoFocus: true,
                     minLength: usernameMinLength,
                     maxLength: usernameMaxLength
                   }}
                   value={usernameInput} onChangeFunc={setUsernameInput}/>
          <TextBox label={"Password:"} placeholder={"password"} type={"password"}
                   extraAttrs={{
                     minLength: passwordMinLength, maxLength: passwordMaxLength
                   }}
                   value={passwordInput} onChangeFunc={setPasswordInput}/>
          <div className="panelButtonsWrapper">
            <button className="panelButtons tabHighlightable" type="button"
                    aria-label={registeringMode ? "Return to Login Screen"
                        : "Go To Signup Screen"}
                    onClick={onClickToggleRegisteringMode}>
              <span className="insideTabHighlightable">
                {registeringMode ? "Return to Login Screen" : "New User? Register here!"}
              </span>
            </button>
            <button className="panelButtons tabHighlightable" type="submit"
                    aria-label={registeringMode ? "Register" : "Login"}>
              <span className="insideTabHighlightable">
                {registeringMode ? "Register" : "Login"}
              </span>
            </button>
          </div>
        </form>
      </div>
  );
}

export default LoginView;
