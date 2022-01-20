import "./zMeetingGroupCard.css";
import {
  baseUrl,
  config,
  diagnosticLogStyle,
  enterToClick,
  hexStringToRgba,
  increment,
  lockConfirmationMessage,
  leaveConfirmationMessage,
  deleteConfirmationMessage, minutesToHourMin
} from "./utils/definitions";
import axios from "axios";
import {useEffect, useState} from "react";
import React from "react";
import {MeetingTimeCard} from "./MeetingTimeCard";

function MeetingGroupCardButton({onClick, extraClass = {}, label, tooltip = "", content}) {

  let classes = "MeetingGroupCardButton tabHighlightable " + extraClass
  if (tooltip) classes += " hasTooltip"

  return (
      <button className={classes} onClick={onClick}
              aria-label={label} data-tooltip={tooltip}>
        <span className="insideTabHighlightable">
          <span className="MeetingGroupCardButtonContent">
          {content}
          </span>
        </span>
      </button>
  )
}

// Functional component: a single meeting group card
function MeetingGroupCard({
                            meetingGroup, selectedTimes, setSelectedTimes,
                            username, loginToken, setGroupRefreshCount,
                          }) {
  // States
  const [prefs, setPrefs] = useState({})
  const [highestScore, setHighestScore] = useState(0)
  const [memberListCollapsed, setMemberListCollapsed] = useState(true)
  const isLocked = meetingGroup.parentMeetingGroupId || meetingGroup.currentState === "LOCKED"
  const hasMembers = meetingGroup.members && meetingGroup.members.length > 0
  const groupColorStyle = {
    backgroundColor: meetingGroup.rgb ? hexStringToRgba(meetingGroup.rgb, 0.75) : ""
  }

  // Determine highest score based on this meetingGroup's meetingTimes
  useEffect(() => {
    if (!meetingGroup.meetingTimes || !meetingGroup.meetingTimes.length) return
    const newMax = Math.max.apply(Math, meetingGroup.meetingTimes.map(
        mt => mt.totalPreferenceScore))
    setHighestScore(newMax)
  }, [meetingGroup])

  // Fetch time preferences
  useEffect(() => {
    // Axios request
    axios.get(`${baseUrl}/group/${meetingGroup.id}/time`,
        config(loginToken))
        .then(response => {
          if (response.data.preferences) {
            for (let pref of response.data.preferences) {
              setPrefs((prev) => ({
                ...prev,
                [pref.meetingTimeId]: pref.preferenceScore,
              }))
              setSelectedTimes(prev => {
                // Toggle pinned status
                const mt = meetingGroup.meetingTimes.find(t => t.id === pref.meetingTimeId)
                const currentIsPinned = (prev.pinnedTimes.find(pmt => pmt.id === mt.id) !== undefined)
                if (pref.isPinned && !currentIsPinned) {
                  return {
                    times: prev.times,
                    pinnedTimes: [mt, ...prev.pinnedTimes],
                    highlightedTime: mt,
                  }
                } else if (!pref.isPinned && currentIsPinned) {
                  return {
                    times: prev.times,
                    pinnedTimes: prev.pinnedTimes.filter(pmt => pmt.id !== mt.id),
                    highlightedTime: mt,
                  }
                } else {
                  return prev
                }
              })
            }
          } else {
            console.error("Response is missing field response.data.preferences!")
            console.log(response)
          }
        })
  }, [meetingGroup, setSelectedTimes, loginToken])

  // Clean up pinnedTimes in selectedTimes on unmount
  useEffect(() => {
    return () => setSelectedTimes(prev => {
      return {
        times: prev.times,
        pinnedTimes: prev.pinnedTimes.filter(pmt => pmt.meetingGroupId !== meetingGroup.id),
        highlightedTime: prev.highlightedTime
      }
    })
  }, [meetingGroup, setSelectedTimes])

  // Listeners
  const onMouseOverShowAllTimes = e => {
    const bottom = document
        .getElementById("meetingGroup" + meetingGroup.id)
        .querySelector(".MeetingGroupCardInnerBottom")
    if (!bottom || bottom.contains(e.target)) return
    setSelectedTimes((prev) => {
      return {
        times: meetingGroup.meetingTimes,
        pinnedTimes: prev.pinnedTimes,
        highlightedTime: prev.highlightedTime,
      }
    })
  }
  const onMouseLeaveUnShowAllTimes = () =>
      setSelectedTimes((prev) => {
        return {
          times: [],
          pinnedTimes: prev.pinnedTimes,
          highlightedTime: null,
        }
      })
  const onMouseEnterUnShowAllTimes = () =>
      setSelectedTimes((prev) => {
        return {
          times: [],
          pinnedTimes: prev.pinnedTimes,
          highlightedTime: prev.highlightedTime,
        }
      })
  const onClickCopyId = e => {
    e.stopPropagation()
    const temp = document.createElement("input")
    document.body.append(temp)
    temp.value = "#" + meetingGroup.joinCode
    temp.select()
    temp.setSelectionRange(0, 99999)
    document.execCommand("copy")
    temp.remove()
    document
        .getElementById("idCopier" + meetingGroup.joinCode)
        .setAttribute("data-tooltip", "Copied!")
    setTimeout(() => {
      document
          .getElementById("idCopier" + meetingGroup.joinCode)
          .setAttribute("data-tooltip", "Copy Group Code")
    }, 1250)
  }
  const onClickLockGroup = () => {
    // Check that group has members
    if (meetingGroup.members && meetingGroup.members.length === 0) {
      alert("You can't lock a meeting group that has no members!")
      return
    }
    if (window.confirm(lockConfirmationMessage)) {
      // Diagnostics
      console.log(
          "%cSending request to lock group " + meetingGroup.id + ".",
          diagnosticLogStyle
      )
      // Axios request
      axios
          .post(
              `${baseUrl}/group/${meetingGroup.id}/lock`,
              null,
              config(loginToken)
          )
          .then((response) => {
            if (response.data.message) {
              setGroupRefreshCount(increment)
            } else {
              console.error("Response is missing field response.data.message!")
              console.log(response)
            }
          })
          .catch((error) => {
            console.log(error)
            setGroupRefreshCount(increment)
          })
    }
  }
  const onClickLeaveGroup = () => {
    // Check that group has members
    if (window.confirm(leaveConfirmationMessage)) {
      // Diagnostics
      console.log(
          "%cSending request to leave group " + meetingGroup.id + ".",
          diagnosticLogStyle
      )
      // Axios request
      axios
          .post(
              `${baseUrl}/group/${meetingGroup.joinCode}/leave`,
              null,
              config(loginToken)
          )
          .then((response) => {
            if (response.data.message) {
              setGroupRefreshCount(increment)
            } else {
              console.error("Response is missing field response.data.message!")
              console.log(response)
            }
          })
          .catch((error) => {
            console.log(error)
            setGroupRefreshCount(increment)
          })
    }
  }
  const onClickDeleteGroup = () => {
    // Check that group has members
    if (window.confirm(deleteConfirmationMessage)) {
      // Diagnostics
      console.log(
          "%cSending request to delete group " + meetingGroup.id + ".",
          diagnosticLogStyle
      )
      // Axios request
      axios
          .delete(
              `${baseUrl}/group/${meetingGroup.id}`,
              config(loginToken)
          )
          .then((response) => {
            if (response.data.message) {
              setGroupRefreshCount(increment)
            } else {
              console.error("Response is missing field response.data.message!")
              console.log(response)
            }
          })
          .catch((error) => {
            console.log(error)
            setGroupRefreshCount(increment)
          })
    }
  }
  const onMouseLeaveUnHighlightTime = () =>
      setSelectedTimes(prev => {
        return {
          times: prev.times,
          pinnedTimes: prev.pinnedTimes,
          highlightedTime: null,
        }
      })

  // JSX Helper
  const memToJsx = members => {
    const others = members.filter(mem => mem !== username)
        .map(ele => ele === meetingGroup.adminUsername ? ele + " (admin)" : ele)
        .join(", ")
    if (members.includes(username)) {
      const displayYou = (others ? "you, " : "just you")
      return (
          <span>
            <span className="italicized">{displayYou}</span>
            {others && <span>{others}</span>}
          </span>
      )
    }
    return (<span>{others}</span>)
  }

  // Return
  return (
      <div id={"meetingGroup" + meetingGroup.id}
           className="MeetingGroupCard paperTexture shadow"
           onFocus={onMouseOverShowAllTimes}
           onMouseOver={onMouseOverShowAllTimes}
           onMouseLeave={onMouseLeaveUnShowAllTimes}>
        <div className="MeetingGroupCardInnerTop">
          <button id={"idCopier" + meetingGroup.joinCode}
                  className={"idCopier " + (isLocked ? "" : "hasTooltip")}
                  onClick={onClickCopyId} data-tooltip="Copy Group Code"
                  onKeyDown={(e) => {
                    enterToClick(onClickCopyId, e, "idCopier" + meetingGroup.joinCode)
                  }} tabIndex={0} aria-label="Copy Group Code"
                  {...(isLocked ? {"aria-disabled": true, disabled: true} : {})}>
            <span className="insideTabHighlightable">
              <span className="idCopierColor" style={groupColorStyle}/>
              <span className="idCopierContent">
                {isLocked ? "locked." : "#" + meetingGroup.joinCode}
              </span>
            </span>
          </button>
          <span className="MeetingGroupCardName">{meetingGroup.name}</span>
          <div className="MeetingGroupCardButtonWrapper">
            {meetingGroup.adminUsername === username &&
            meetingGroup.currentState === "OPEN" &&
            <MeetingGroupCardButton onClick={onClickLockGroup} content={"Lock"}
                                    label={"Lock Group"} tooltip={"Lock Group"}/>}
            {meetingGroup.isMember &&
            <MeetingGroupCardButton onClick={onClickLeaveGroup} content={"Leave"}
                                    label={"Leave Group"} tooltip={"Leave Group"}/>}
            {meetingGroup.adminUsername === username &&
            <MeetingGroupCardButton onClick={onClickDeleteGroup} content={"Delete"}
                                    label={"Delete Group"} tooltip={"Delete Group"}/>}
          </div>
        </div>

        <div className="MeetingDuration MeetingGroupCardInnerMiddle">
          <span className="semiBold MeetingGroupCardInnerMiddleLabel">
            {"Meeting duration:"}
          </span>
          <span>
            {minutesToHourMin(meetingGroup.meetingDurationMinutes)}
          </span>
        </div>
        {meetingGroup.subgroupSize > 0 && (
            <div className="SubgroupSize MeetingGroupCardInnerMiddle">
              <span className="semiBold MeetingGroupCardInnerMiddleLabel">
                {"Subgroup size:"}
              </span>
              <span>
                {meetingGroup.subgroupSize}
              </span>
            </div>)}
        <div className="Members MeetingGroupCardInnerMiddle">
          <span className="semiBold MeetingGroupCardInnerMiddleLabel">
            {"Members (" + meetingGroup.members.length + "):"}
          </span>
          {hasMembers ?
              <React.Fragment>
                {meetingGroup.members.length > 4 ?
                    <button className="MembersList cursorPointer hasTooltip"
                            data-tooltip={memberListCollapsed ? "Expand Member List" : "Collapse Member List"}
                            onClick={() => setMemberListCollapsed((x) => !x)}>
                      {memberListCollapsed ?
                          memToJsx(meetingGroup.members.slice(0, 3)) :
                          memToJsx(meetingGroup.members)}
                      {memberListCollapsed && <span>{", ... (click for more)"}</span>}
                    </button> :
                    <span className="MembersList">
                      {memToJsx(meetingGroup.members)}
                    </span>}
              </React.Fragment> :
              <span className="NoMembers">{"...none!"}</span>}
        </div>

        {meetingGroup.meetingTimes && meetingGroup.meetingTimes.length > 0 && (
            <div className="MeetingGroupCardInnerBottom"
                 onMouseEnter={onMouseEnterUnShowAllTimes}
                 onMouseLeave={onMouseLeaveUnHighlightTime}>
              <div className="MeetingTimeCardsWrapper">
                {meetingGroup.meetingTimes.map((mt, i) => (
                    <MeetingTimeCard
                        key={i} mt={mt} pref={prefs[mt.id]} setPrefs={setPrefs}
                        selectedTimes={selectedTimes} setSelectedTimes={setSelectedTimes}
                        isMember={meetingGroup.isMember} loginToken={loginToken}
                        highestScore={highestScore}
                        setGroupRefreshCount={setGroupRefreshCount}/>))}
              </div>
            </div>)}
      </div>
  )
}

export default MeetingGroupCard;
