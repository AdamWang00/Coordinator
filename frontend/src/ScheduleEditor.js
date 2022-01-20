import './zScheduleEditor.css';
import {useCallback, useEffect, useState} from "react";
import React from "react"
import {
  baseUrl,
  cols, compressDelay, config, curry, dayNames,
  defaultOpacity, diagnosticLogStyle, hexStringToRgba,
  horizontalBorderWidth, pinnedOpacity, rows,
  subdivisions, verticalBorderWidth
} from "./utils/definitions";
import {
  clone,
  tableToVec,
  vecIndexToTableIndex,
  vecLocalToOrFromUTC, vecToTable
} from "./utils/arrayHelperFunctions";
import axios from "axios";
import {filledSchedule} from "./utils/randomHelperFunctions";

// Definitions
const widthStyle = {width: 100 / cols + "%"}
const outlineChunks = true
const framesForAnimation = 20 // A little thing to make the pinned times animate

// Just To Fix Extremely Annoying Subpixel Bug on Chrome
const roundForChrome = (num) => {
  if (navigator.userAgent.indexOf("Chrome") > -1) {
    // console.log("Chrome detected!")
    return Math.round(num)
  }
  return num
}

// Helpers
const selectRegion = (info, endRow, endCol) => {
  const copy = clone(info.table)
  let startRow = info.row, startCol = info.col
  if (startRow > endRow) {
    startRow = endRow
    endRow = info.row
  }
  if (startCol > endCol) {
    startCol = endCol
    endCol = info.col
  }
  for (let i = startRow; i <= endRow; i++) {
    for (let j = startCol; j <= endCol; j++) {
      copy[i][j] = info.setTo
    }
  }
  return copy
}
const generateTimeLabels = () => {
  const range = [...Array(rows / subdivisions).keys()]
  return range.map((ele) => {
    const hour = ele % 12 ? ele % 12 : 12
    const ampm = " " + ((ele < 12) ? "am" : "pm")
    return <div key={ele} className="ScheduleEditorTimesCell">
      <span className={"ScheduleEditorTimesContent" +
      (hour % 3 ? " hideWhenCompressed" : " notHideWhenCompressed")}>
        {hour + ":00" + ampm}
      </span>
    </div>
  })
}
const ijToTableDataId = (i, j) => "row" + i + "col" + j
const generateSECellOverlays = (selectedTimes, rerenderTrigger) => {
  if (rerenderTrigger) {
    // Do nothing
  }
  const {times, pinnedTimes, highlightedTime} = selectedTimes
  const scheduleEditorTable = document.getElementById("ScheduleEditorTable")
  if (!scheduleEditorTable) return
  const tableRect = scheduleEditorTable.getBoundingClientRect()
  const parH = tableRect.height
  const compT = -tableRect.top
  const compL = -tableRect.left
  const divList = []
  if (highlightedTime) {
    appendSEOverlayDivStyle(divList, highlightedTime, parH, compT, compL, "highlighted")
  }
  for (let i = 0; i < times.length; i++) {
    appendSEOverlayDivStyle(divList, times[i], parH, compT, compL)
  }
  for (let i = 0; i < pinnedTimes.length; i++) {
    appendSEOverlayDivStyle(divList, pinnedTimes[i], parH, compT, compL, "pinnedBorder")
    appendSEOverlayDivStyle(divList, pinnedTimes[i], parH, compT, compL, "pinned")
  }
  return (
      <React.Fragment>
        {divList.map((ele, i) =>
            <div key={i} className={"SEOverlay hasTooltip" + ele.extraClass}
                 data-tooltip={ele.extraTooltip}
                 style={ele.extraStyle}/>)}
      </React.Fragment>
  )
}
const appendSEOverlayDivStyle = (divStyles, meetingTime, parH, compT, compL, extraClass = "") => {
  // Extract information from meetingTime
  let {startIndex, endIndex, groupName, rgb} = meetingTime
  const [startI, startJ] = vecIndexToTableIndex(startIndex)
  const [endI, endJ] = vecIndexToTableIndex(endIndex)
  const endCell = document.getElementById(ijToTableDataId(endI, endJ % cols))
  if (!endCell) return
  const endCellRect = endCell.getBoundingClientRect()
  const opacity = extraClass === "pinned" ? pinnedOpacity : defaultOpacity
  const bgColor = hexStringToRgba(rgb, opacity)
  // Create overlay divs for this one meetingTime
  for (let j = startJ; j <= endJ; j++) {
    const currCell = j === startJ ?
        document.getElementById(ijToTableDataId(startI, startJ % cols)) :
        document.getElementById(ijToTableDataId(0, j % cols))
    if (currCell === null) {
      console.error("BAD!! appendSEOverlayDivStyle screwed up")
      console.log("j", j, "startI", startI, "startJ", startJ)
      continue
    }
    const currCellRect = currCell.getBoundingClientRect()
    const height = j === endJ ?
        endCellRect.top + endCellRect.height - currCellRect.top :
        parH - currCellRect.top - compT
    let newStyle = {
      extraClass: " " + extraClass,
      extraTooltip: "Meeting time for \"" + groupName + "\"",
      extraStyle: {
        top: (currCellRect.top + compT - .5) + "px",
        left: roundForChrome(currCellRect.left + compL - .5) + "px",
        height: (height + horizontalBorderWidth) + "px",
        width: (currCell.getBoundingClientRect().width + verticalBorderWidth) + "px",
        backgroundColor: extraClass === "highlighted" || extraClass === "pinnedBorder" ?
            "transparent" : bgColor,
      }
    }
    divStyles.push(newStyle)
  }

}

// Functional component: a when2meet-style schedule editor
function ScheduleEditor({loginToken, selectedTimes}) {
  // States
  const [compressed, setCompressed] = useState(true)
  const [rerenderTrigger, setRerenderTrigger] = useState(0)
  const [scheduleTable, setScheduleTable] = useState(null)
  const [selectStartInfo, setSelectStartInfo] = useState(null)
  const utcOffset = -new Date().getTimezoneOffset() / 60

  // Listeners
  const onMouseDownBeginSelection = (e, row, col) => {
    e.preventDefault()
    if (compressed) return
    const newInfo = {
      row: row, col: col,
      table: clone(scheduleTable),
      setTo: !scheduleTable[row][col]
    }
    setSelectStartInfo(newInfo)
    setScheduleTable(selectRegion(newInfo, row, col))
  }
  const onMouseMoveContinueSelection = (e, row, col) => {
    e.preventDefault()
    if (compressed || !selectStartInfo) return
    setScheduleTable(selectRegion(selectStartInfo, row, col))
  }
  const onMouseLeaveOrUpEndSelection = useCallback(e => {
    e.preventDefault()
    if (compressed || !selectStartInfo) return
    if (e.target === document ||
        document.getElementById("ScheduleEditorInner")
            .contains(e.target)) setSelectStartInfo(null)
  }, [compressed, selectStartInfo, setSelectStartInfo])
  const onClickCompressEditor = useCallback((e, compress = true) => {
    if ((compress && document.getElementById("ScheduleEditorInner")
        .contains(e.target))) return
    if (selectStartInfo) {
      setSelectStartInfo(null)
      return
    }
    e.stopPropagation()
    setCompressed(compress)
    if (compress !== compressed) {
      const repeatRerender = (i) => {
        if (i > framesForAnimation) return
        setRerenderTrigger(i);
        setTimeout(() => repeatRerender(i + 1), compressDelay / framesForAnimation)
      }
      repeatRerender(0)
    }
  }, [compressed, selectStartInfo])

  // The Big One: Accessibility
  const traverseCell = curry((dir, {row, col}) => {
    let i = row, j = col
    switch (dir) {
      case "start":
        i = 0
        j = 0
        break
      case "end":
        i = rows - 1
        j = cols - 1
        break
      case "up":
        i = (row - 1 + rows) % rows
        break
      case "down":
        i = (row + 1) % rows
        break
      case "left":
        j = (col - 1 + cols) % cols
        break
      case "right":
        j = (col + 1) % cols
        break
      default:
        return
    }
    if (!document.getElementById(ijToTableDataId(i, j))) {
      console.log("Error in traverseCell: " + row + ", " + col
          + " -" + dir + "-> " + i + ", " + j)
      return
    }
    document.getElementById(ijToTableDataId(i, j)).focus()
  })
  const selectCell = ({row, col}) => {
    if (selectStartInfo) {
      setSelectStartInfo(null)
      return
    }
    const newInfo = {
      row: row, col: col,
      table: clone(scheduleTable),
      setTo: !scheduleTable[row][col]
    }
    setSelectStartInfo(newInfo)
    setScheduleTable(selectRegion(newInfo, row, col))
  }
  const onKeyDownSECellHandler = (e, row, col) => {
    const keyCommands = {
      ArrowUp: traverseCell("up"),
      ArrowDown: traverseCell("down"),
      ArrowLeft: traverseCell("left"),
      ArrowRight: traverseCell("right"),
      Enter: selectCell,
      q: traverseCell("start"),
      e: traverseCell("end"),
      w: traverseCell("up"),
      s: traverseCell("down"),
      a: traverseCell("left"),
      d: traverseCell("right"),
      " ": selectCell,
    }
    if (typeof keyCommands[e.key] === "function") {
      e.preventDefault()
      e.stopPropagation()
      keyCommands[e.key]({row, col})
    }
  }
  const onFocusSECell = (e, row, col) => {
    if (compressed) {
      e.preventDefault()
      e.stopPropagation()
      if (document.getElementById("ScheduleEditorOverlay")) {
        document.getElementById("ScheduleEditorOverlay").focus()
      }
      return
    }
    if (selectStartInfo) {
      setScheduleTable(selectRegion(selectStartInfo, row, col))
    }
  }
  const onTabOrShiftTab = e => {
    if (e.key !== "Tab") return
    e.preventDefault()
    e.stopPropagation()
    if (!e.shiftKey) {
      if (document.getElementById("createGroupButton")) {
        document.getElementById("createGroupButton").focus()
      }
      return
    }
    if (document.getElementById("usernameSpan")) {
      document.getElementById("usernameSpan").focus()
    }
  }

  // JSX Helpers
  const generateSECell = (cell, i, j, str) => {
    // Attributes for SECell
    const id = ijToTableDataId(i, j)
    const classes = ["SECell", str, ...(cell ? [" filled"] : [])]
    // Chunk borders
    if (outlineChunks && cell) {
      classes.push(typeof scheduleTable[i - 1] === "undefined" ? " topMostEdge" :
          !scheduleTable[i - 1][j] ? " topEdge" : "")
      classes.push(typeof scheduleTable[i + 1] === "undefined" ? " bottomMostEdge" :
          !scheduleTable[i + 1][j] ? " bottomEdge" : "")
      classes.push(typeof scheduleTable[i][j - 1] === "undefined" ? " leftMostEdge" :
          !scheduleTable[i][j - 1] ? " leftEdge" : "")
      classes.push(typeof scheduleTable[i][j + 1] === "undefined" ? " rightMostEdge" :
          !scheduleTable[i][j + 1] ? " rightEdge" : "")
    }
    return (
        <td id={id} key={j} className={classes.join("")} style={widthStyle}
            tabIndex={0} onKeyDown={e => onKeyDownSECellHandler(e, i, j)}
            onFocus={e => onFocusSECell(e, i, j)}
            onMouseDown={e => onMouseDownBeginSelection(e, i, j)}
            onMouseMove={e => onMouseMoveContinueSelection(e, i, j)}/>
    )
  }

  // Update database scheduleTable every edit
  useEffect(() => {
    if (!scheduleTable || selectStartInfo) return
    // Diagnostics
    console.log("%cSending request to update user's schedule.", diagnosticLogStyle)
    // Axios request
    const body = {schedule: vecLocalToOrFromUTC(tableToVec(scheduleTable))}
    axios.post(baseUrl + "/user/schedule", body, config(loginToken))
        .then(response => {
          if (!response.data.message) {
            console.error("Response is missing field response.data.message!")
            console.log(response)
          }
        })
        .catch(error => {
          console.log(error)
          alert("Bad connection? We couldn't update your schedule, sorry!")
        })
  }, [scheduleTable, selectStartInfo, loginToken])

  // Request scheduleTable for first time
  useEffect(() => {
    if (scheduleTable) return
    // Diagnostics
    console.log("%cSending request to get user's schedule.", diagnosticLogStyle)
    // Axios request
    axios.get(baseUrl + "/user/schedule", config(loginToken))
        .then(response => {
          if (response.data.schedule) {
            setScheduleTable(vecToTable(
                vecLocalToOrFromUTC(response.data.schedule, false)))
          } else {
            console.error("Response is missing field response.data.schedule!")
            console.log(response)
          }
        })
        .catch(error => {
          console.log(error)
          alert("Bad connection? We couldn't get your schedule, sorry!")
        })
  }, [scheduleTable, loginToken])

  // Setting window listener
  useEffect(() => {
    document.addEventListener("mouseup", onMouseLeaveOrUpEndSelection)
    document.addEventListener("mouseleave", onMouseLeaveOrUpEndSelection)
    window.addEventListener("click", onClickCompressEditor)
    return () => {
      document.removeEventListener("mouseup", onMouseLeaveOrUpEndSelection)
      document.removeEventListener("mouseleave", onMouseLeaveOrUpEndSelection)
      window.removeEventListener("click", onClickCompressEditor)
    }
  }, [onMouseLeaveOrUpEndSelection, onClickCompressEditor])

  // Return
  return (
      <div id="ScheduleEditorOuter">
        <div id="ScheduleEditorInner" className={"shadow paperTexture" +
        (compressed ? " compressed" : " notCompressed")}>
          {compressed &&
          <button id="ScheduleEditorOverlay" className="tabHighlightable hasTooltip"
                  onClick={e => onClickCompressEditor(e, false)}
                  data-tooltip="Expand Editor" onKeyDown={onTabOrShiftTab}>
          </button>}
          <div id="ScheduleEditorTop">
            <p>{"Click and drag to toggle."}</p>
            <span id="Unavailable">Unavailable</span>
            <span id="Available">Available</span>
            <br/>
            <p>{"Times are displayed in your local timezone, UTC"
            + (utcOffset > 0 ? "+" + utcOffset : utcOffset) + "."}</p>
            <br/>
            <table id="ScheduleEditorHeaders">
              <tbody>
              <tr>
                {dayNames.slice(0, cols).map((ele, i) =>
                    <td key={i} style={widthStyle}>{ele.slice(0, 3)}</td>)}
              </tr>
              </tbody>
            </table>
          </div>
          <div id="ScheduleEditorBottom">
            <div id="ScheduleEditorTimes">{generateTimeLabels()}</div>
            <div id="ScheduleEditorTableWrapper">
              {(selectedTimes.pinnedTimes.length > 0
                  || selectedTimes.times.length > 0
                  || selectedTimes.highlightedTime)
              && generateSECellOverlays(selectedTimes, rerenderTrigger)}
              <div id="ScheduleEditorTableInnerWrapper">
                <table id="ScheduleEditorTable">
                  <tbody id="ScheduleEditorTableBody">
                  {scheduleTable ? scheduleTable.map((row, i) => {
                    // Hour and half-hour markings
                    const str = (i % subdivisions ?
                        (i % Math.floor(subdivisions / 2) ? "" : " half") : " hour")
                    return (
                        <tr key={i} className={"SERow" + str}>
                          {row.map((cell, j) => generateSECell(cell, i, j, str))}
                        </tr>)
                  }) : filledSchedule(true)}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
  );
}

export default ScheduleEditor;