import React from 'react';
import ReactDOM from 'react-dom';
import './zIndex.css';
import UserView from './UserView';
import {useEffect} from "react";
import LoginView from "./LoginView";
import { CookiesProvider, useCookies } from "react-cookie";

function App() {
  const [cookies, setCookie] = useCookies(["token", "username"])
  const { token, username } = cookies;

  useEffect(() => {
    document.title = username ?
        "Hello, " + username + "!" :
        "Login"
  }, [username])

  // If logged in
  if (token) {
    return <UserView username={username}
                     setUsername={u => setCookie("username", u)}
                     loginToken={token}
                     setLoginToken={t => setCookie("token", t)}
                     />
  }
  // Default
  return <LoginView setUsername={u => setCookie("username", u)}
                    setLoginToken={t => setCookie("token", t)}
                    />

}

ReactDOM.render(
  <CookiesProvider>
    <App/>
  </CookiesProvider>,
  document.getElementById('root')
);