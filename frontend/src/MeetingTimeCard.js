import "./zMeetingTimeCard.css";
import {
  baseUrl,
  config,
  diagnosticLogStyle,
  enterToClick,
  hexStringToRgba,
  increment
} from "./utils/definitions";
import axios from "axios";
import {vecIndexPairToString} from "./utils/arrayHelperFunctions";

// Functional component: a single meeting time inside the meeting group card
export function MeetingTimeCard({
                                  mt, selectedTimes, setSelectedTimes, pref, setPrefs,
                                  loginToken, isMember, highestScore, setGroupRefreshCount,
                                }) {
  // Listeners
  const onMouseEnterHighlightTime = () =>
      setSelectedTimes((prev) => {
        return {
          times: prev.times,
          pinnedTimes: prev.pinnedTimes,
          highlightedTime: mt,
        }
      })
  const onClickTogglePinnedTime = e => {
    e.stopPropagation()
    console.log("Pinning", e.type, e.target)
    setSelectedTimes((prev) => {
      // Toggle pinned status
      const updatedIsPinned = (prev.pinnedTimes.find(pmt => pmt.id === mt.id) === undefined)
      console.log("%cSending request to update user's pin on meeting time " +
        mt.id + " to " + updatedIsPinned + ".", diagnosticLogStyle)
      // Axios request
      const body = { meetingTimeId: mt.id, updatedIsPinned: updatedIsPinned }
      axios.post(`${baseUrl}/group/time/pin`,
        body, config(loginToken))
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
        })
      return {
        times: prev.times,
        pinnedTimes: updatedIsPinned ?
            [mt, ...prev.pinnedTimes] :
            prev.pinnedTimes.filter(pmt => pmt.id !== mt.id),
        highlightedTime: mt,
      }
    })
  }
  const onSetPreference = e => {
    e.stopPropagation()
    const newPref = Number(e.target.value)
    setPrefs(prev => ({...prev, [mt.id]: newPref}))
    // Diagnostics
    console.log("%cSending request to update user's preference for group " +
        mt.meetingGroupId + ".", diagnosticLogStyle)
    // Axios request
    const body = {meetingTimeId: mt.id, updatedScore: newPref}
    axios.post(`${baseUrl}/group/time/score`,
        body, config(loginToken))
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
        })
  }

  // Return
  return (
      <div className="MeetingTimeCard tabHighlightable" tabIndex={0}
           onFocus={onMouseEnterHighlightTime}
           onMouseEnter={onMouseEnterHighlightTime}
           onClick={onClickTogglePinnedTime}
           onKeyDown={e => enterToClick(onClickTogglePinnedTime, e)}>
        <span className="insideTabHighlightable"
              tabIndex={-1}
              style={{
                backgroundColor:
                    highestScore === mt.totalPreferenceScore ? "honeydew" : "white",
              }}>
          <span className="MeetingTimeColor"
                style={{backgroundColor: hexStringToRgba(mt.rgb, 0.75)}}>
            {typeof selectedTimes.pinnedTimes.find(pmt => pmt.id === mt.id) !== "undefined" &&
            <span className="pinned">{"P"}</span>}
          </span>
          <span className="MeetingTimeContent">
            {vecIndexPairToString(mt.startIndex, mt.endIndex)}
          </span>
          {isMember && (
              <div className="PreferenceSelection">
                <span>
                  <input type="radio" name={mt.id} checked={pref === 1} value={1}
                         onChange={onSetPreference} onClick={e => e.stopPropagation()}/>
                  <label className="NotPreferred"/>
                </span>
                <span>
                  <input type="radio" name={mt.id} checked={pref === 2} value={2}
                         onChange={onSetPreference} onClick={e => e.stopPropagation()}/>
                  <label className="NeutralPreferred"/>
                </span>
                <span>
                  <input type="radio" name={mt.id} checked={pref === 3} value={3}
                         onChange={onSetPreference} onClick={e => e.stopPropagation()}/>
                  <label className="Preferred"/>
                </span>
              </div>)}
        </span>
      </div>
  );
}
