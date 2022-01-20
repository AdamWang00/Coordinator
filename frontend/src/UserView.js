import "./zUserView.css";
import ScheduleEditor from "./ScheduleEditor";
import MeetingGroupsView from "./MeetingGroupsView";
import {useEffect, useState} from "react";
import {
  baseUrl,
  config, diagnosticLogStyle, enterToClick,
  groupIdLength, increment,
  logoutConfirmationMessage, meetingGroupViewRefreshDelay,
} from "./utils/definitions";
import {randAlphaNum} from "./utils/randomHelperFunctions";
import axios from 'axios';
import CreateGroupView from "./CreateGroupView";
import {listOfGroupsUtcToLocalAndOtherStuff} from "./utils/arrayHelperFunctions";

const groupIdRegexNoHex = new RegExp("^[A-Z\\d]{" + groupIdLength + "}$", "i");
const groupIdRegexHex = new RegExp("^#([A-Z\\d]{" + groupIdLength + "})$", "i");

// Functional component: the user homepage
function UserView({
                    username, setUsername,
                    loginToken, setLoginToken,
                  }) {
  // States
  const [selectedTimes, setSelectedTimes] = useState({
    times: [],
    pinnedTimes: [],
    highlightedTime: null,
  });
  const [meetingGroups, setMeetingGroups] = useState([])
  const [meetingGroupRefreshCount, setGroupRefreshCount] = useState(0)
  const [createGroupPanelOpen, setCreateGroupPanelOpen] = useState(false)

  // Listeners
  const onClickOpenCreateGroupPanel = e => {
    e.preventDefault()
    e.stopPropagation()
    setCreateGroupPanelOpen(true)
  }
  const onClickPromptJoinGroup = e => {
    e.preventDefault();
    e.stopPropagation();
    // Ask for meeting group invite id
    const placeholder = "e.g. #" + randAlphaNum(groupIdLength);
    let input = prompt(
        "What's the ID of the group you'd like to join?",
        placeholder
    );
    if (!input) return;
    // Check input
    if (input === placeholder || input.trim().length === 0) {
      alert("Hmm, it seems like you didn't provide any group ID.");
      return;
    }
    if (input.trim().match(groupIdRegexNoHex) &&
        window.confirm("Did you mean: #" + input.trim() + "?")) {
      input = "#" + input.trim()
    }
    const match = input.match(groupIdRegexHex)
    if (!match || match.length !== 2) {
      alert(
          "Whoops! Your group ID should be formatted as a hex (#) " +
          "followed by " + groupIdLength + " alphanumeric characters."
      );
      return;
    }
    // Check that user is not already in group
    if (meetingGroups.find(mg => mg.joinCode === match[1] && mg.isMember)) {
      alert("...Hey! You're already a member of that group!")
      return
    }
    // Diagnostics
    console.log("%cSending request to join group #" + match[1] + ".",
        diagnosticLogStyle)
    // Axios request
    axios.post(`${baseUrl}/group/${match[1]}/join`,
        null, config(loginToken))
        .then(response => {
          if (response.data.message) {
            setGroupRefreshCount(increment)
          } else {
            console.error("Response is missing field response.data.message!")
            console.log(response)
          }
        })
        .catch(error => {
          console.log(error)
          alert("Failed to join group!")
        })
  }
  const promptLogout = e => {
    e.stopPropagation()
    e.preventDefault()
    if (window.confirm(logoutConfirmationMessage)) {
      setUsername("")
      setLoginToken("")
    }
  }

  // Increment refresh count every few seconds
  useEffect(() => {
    const intervalHandle = setInterval(() => {
      setGroupRefreshCount(increment)
    }, meetingGroupViewRefreshDelay)
    return () => {
      clearInterval(intervalHandle)
    }
  }, [setGroupRefreshCount])

  // Fetch user's group whenever refresh count is incremented
  useEffect(() => {
    // Diagnostics
    // console.log("%cSending request to get user's groups.", diagnosticLogStyle)
    // Axios request
    axios.get(baseUrl + "/user/groups", config(loginToken))
        .then(response => {
          if (response.data.groups) {
            setMeetingGroups(listOfGroupsUtcToLocalAndOtherStuff(
                response.data.groups, username))
          } else {
            console.error("Response is missing field response.data.groups!")
            console.log(response)
          }
        })
        .catch(error => {
          console.log(error)
        })
  }, [meetingGroupRefreshCount, loginToken, username])

  // Return
  return (
      <div id="UserView">
        {createGroupPanelOpen &&
        <CreateGroupView loginToken={loginToken}
                         setGroupRefreshCount={setGroupRefreshCount}
                         setCreateGroupPanelOpen={setCreateGroupPanelOpen}/>}
        <div id="UserViewUpper">
          <h2 id="UserViewUpperText" className="noSelect">
            <span>{"Hello, "}</span>
            <button id="usernameSpan" className="tabHighlightable" tabIndex={0}
                  onClick={promptLogout} onKeyDown={e => enterToClick(promptLogout, e)}>
              <span className="insideTabHighlightable">
                {username}
              </span>
            </button>
            <span>{"!"}</span>
          </h2>
        </div>
        <div id="UserViewLower">
          <div className="Wrapper">
            <div className="WrapperHeader noSelect">
              <h3> {"Weekly Schedule"} </h3>
            </div>
            <ScheduleEditor loginToken={loginToken}
                            selectedTimes={selectedTimes}/>
          </div>
          <div className="Wrapper">
            <div className="WrapperHeader noSelect">
              <h3> {"Meeting Groups"} </h3>
              <button id="createGroupButton"
                      aria-label="Create a New Group"
                      className="WrapperHeaderButton paperTexture shadow tabHighlightable"
                      onClick={onClickOpenCreateGroupPanel}>
                <span className="insideTabHighlightable hasTooltip"
                      data-tooltip="Create a New Group">
                  <span id="plus">{"+"}</span>
                  <span id="createGroupText">{"Create"}</span>
                </span>
              </button>
              <button id="joinGroupButton"
                      aria-label="Join a Group By Its Id"
                      className="WrapperHeaderButton paperTexture shadow tabHighlightable"
                      onClick={onClickPromptJoinGroup}>
                <span className="insideTabHighlightable hasTooltip"
                      data-tooltip="Join a Group By Its Id">
                  <span id="plus">{"+"}</span>
                  <span id="joinGroupText">{"Join"}</span>
                </span>
              </button>
            </div>
            <MeetingGroupsView meetingGroups={meetingGroups}
                               selectedTimes={selectedTimes}
                               setSelectedTimes={setSelectedTimes}
                               username={username}
                               loginToken={loginToken}
                               setGroupRefreshCount={setGroupRefreshCount}/>
          </div>
        </div>
      </div>
  )
}

export default UserView