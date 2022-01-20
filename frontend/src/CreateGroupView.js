import './zPanelViews.css';
import axios from "axios";
import {useState} from "react";
import TextBox from "./Textbox";
import {randInt} from "./utils/randomHelperFunctions";
import {
  baseUrl,
  config,
  diagnosticLogStyle,
  increment,
  meetingGroupNameMaxLength, meetingGroupNameMinLength,
  inputTooShortAlert, inputTooLongAlert,
  rgbToHexString, roundCancelAlert,
  roundConfirmationMessage,
  timeIntervalMinutes
} from "./utils/definitions";

// Functional component: a panel to set group meeting settings when creating one
function CreateGroupView({loginToken, setGroupRefreshCount, setCreateGroupPanelOpen}) {
  // States
  const [meetingGroupName, setMeetingGroupName] = useState("")
  const [meetingDurationMinutes, setMeetingDurationMinutes] = useState("60")
  const [subgroupSize, setSubgroupSize] = useState("")

  // Listeners
  const onClickCloseCreateGroupPanel = (e, isCancelButton = false) => {
    if (!isCancelButton && document.getElementById("CreateGroupPanel")
        .contains(e.target)) return
    e.preventDefault()
    e.stopPropagation()
    setCreateGroupPanelOpen(false)
  }
  const onSubmitCreateGroup = e => {
    e.preventDefault()
    e.stopPropagation()
    // Checking meetingDurationMinutes
    const roundedMinutes = Math.ceil(meetingDurationMinutes / timeIntervalMinutes)
        * timeIntervalMinutes;
    if (roundedMinutes !== parseInt(meetingDurationMinutes)) {
      console.log(roundedMinutes)
      console.log(meetingDurationMinutes)
      if (!window.confirm(roundConfirmationMessage(
          roundedMinutes, meetingDurationMinutes))) {
        alert(roundCancelAlert)
        return
      }
    }
    // Checking meetingGroupName (imagine naive profanity-checking here)
    if (meetingGroupName.length < meetingGroupNameMinLength) {
      alert(inputTooShortAlert(meetingGroupName.length, meetingGroupNameMinLength, "group name"))
      return
    } else if (meetingGroupName.length > meetingGroupNameMaxLength) {
      alert(inputTooLongAlert(meetingGroupName.length, meetingGroupNameMaxLength, "group name"))
      return
    }
    // Diagnostics
    console.log("%cSending request to create new meeting group.", diagnosticLogStyle)
    // Axios request
    const body = {
      name: meetingGroupName,
      meetingDurationMinutes: roundedMinutes,
      subgroupSize: subgroupSize ? subgroupSize : 0,
      rgb: rgbToHexString(randInt(0, 255), randInt(0, 255), randInt(0, 255)),
    }
    axios.post(baseUrl + "/group", body, config(loginToken))
        .then(response => {
          if (response.data.joinCode) {
            setGroupRefreshCount(increment)
            setCreateGroupPanelOpen(false)
          } else {
            console.error("Response is missing field response.data.joinCode!")
            console.log(response)
          }
        })
        .catch(error => {
          console.log(error)
          alert("Failed to create group!")
        })
  }
  const onShiftTabGoToLastInput = e => {
    if (!e.shiftKey || e.key !== "Tab") return
    e.preventDefault()
    document.getElementById("jsTarget2").focus()
  }
  const onTabGoToFirstInput = e => {
    if (e.shiftKey || e.key !== "Tab") return
    e.preventDefault()
    document.getElementById("jsTarget1").focus()
  }

  // Return
  return (
      <div id="CreateGroupView" onClick={onClickCloseCreateGroupPanel}>
        <form id="CreateGroupPanel" className="Panel paperTexture biggestShadow"
              onSubmit={onSubmitCreateGroup}>
          <div className="PanelHeader">
            {"Create a Meeting Group"}
          </div>
          <div className="PanelSubheader">
            {"You will be the admin of this group."}
          </div>
          <TextBox label={"Meeting Group Name:"} placeholder={"e.g. Science Project Meeting"}
                   extraClass={"longer"} type={"text"}
                   extraAttrs={{id: "jsTarget1", autoFocus: true, onKeyDown: onShiftTabGoToLastInput}}
                   value={meetingGroupName} onChangeFunc={setMeetingGroupName}/>
          <TextBox label={"Meeting Duration (in minutes):"} placeholder={"meeting duration"}
                   extraClass={"longer"} type={"number"} value={meetingDurationMinutes}
                   onChangeFunc={setMeetingDurationMinutes}/>
          <div className="PanelText PanelDivider">
            {"Note: The following is only for meeting groups to be split" +
            " into subgroups! Leave empty if not applicable."}
          </div>
          <TextBox label={"Size per Subgroup (optional):"}
                   placeholder={"e.g. 2, for pairs (optional)"}
                   extraClass={"longer"} type={"number"} value={subgroupSize}
                   extraAttrs={{
                     min: 2,
                     step: 1,
                     required: false,
                     "aria-required": false,
                   }} onChangeFunc={setSubgroupSize}/>
          <div className="panelButtonsWrapper longer">
            <button className="panelButtons tabHighlightable"
                    type="button" aria-label={"Cancel"}
                    onClick={e => onClickCloseCreateGroupPanel(e, true)}>
              <span className="insideTabHighlightable">
                {"Cancel"}
              </span>
            </button>
            <button id="jsTarget2" className="panelButtons tabHighlightable"
                    type="submit" aria-label={"Create Meeting Group"}
                    onKeyDown={onTabGoToFirstInput}>
              <span className="insideTabHighlightable">
                {"Create Meeting Group"}
              </span>
            </button>
          </div>
        </form>
      </div>
  );
}

export default CreateGroupView;
