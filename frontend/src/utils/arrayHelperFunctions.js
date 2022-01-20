import {
  cols, dayNames,
  rows, subdivisions, timeIntervalMinutes
} from "./definitions";

// Recursively creates a deep copy of an array
export const clone = items => items.map(item => Array.isArray(item) ? clone(item) : item);

// Transposes an array of arrays
export const transpose = array => array[0].map((_, colIndex) => array.map(row => row[colIndex]));

// Converts a vector from local time to UTC, or vice versa
export const vecLocalToOrFromUTC = (vec, toUTC = true) => {
  const offset = (toUTC ? -1 : 1) * new Date().getTimezoneOffset() / 60 * subdivisions
  return [...vec.slice(offset), ...vec.slice(0, offset)]
}

// Mutates and returns a list of groups by timezone-correcting their meeting
// times and fixing up some other fields for both the times and groups
export const listOfGroupsUtcToLocalAndOtherStuff = (list, user) => {
  const indexUtcToLocal = i => {
    const offset = new Date().getTimezoneOffset() / 60 * subdivisions
    return (i - offset + rows * cols) % (rows * cols)
  }
  return list.map(group => {
    group.meetingTimes = group.meetingTimes.map(mt => {
      mt.startIndex = indexUtcToLocal(mt.startIndex) // Override!
      mt.endIndex = indexUtcToLocal(mt.endIndex) // Override!
      mt.groupName = group.name
      mt.groupRgb = group.rgb
      if (!mt.rgb || mt.rgb === "#FFFFFF") {
        mt.rgb = group.rgb
      }
      return mt
    })
    if (group.members.includes(user)) {
      group.members = [user, ...group.members.filter(item => item !== user)]
    }
    return group
  })
}

// Converts a schedule vector to a schedule table, or vice versa
export const tableToVec = table => {
  return transpose(table).flat()
}
export const vecToTable = vec => {
  if (vec.length !== rows * cols) {
    console.error("Bad vector size!")
    return null
  }
  let table = []
  for (let i = 0; i < cols; i++) {
    table.push(vec.slice(rows * i, rows * (i + 1)))
  }
  return transpose(table);
}

// Converts a schedule vector index to a schedule table index, or vice versa
export const vecIndexToTableIndex = i => [i % rows, Math.floor(i / rows)]
export const tableIndexToVecIndex = (r, c) => c * rows + r

// Converts index to time
export const vecIndexToString = (i, returnDayString = false, check = "") => {
  const div = Math.floor(i / rows) % cols, mod = i % rows
  const dayString = dayNames[div].slice(0, 3)
  const hours = Math.floor(mod / subdivisions)
  const hourString = (hours % 12 ? hours % 12 : 12).toString()
  const ampm = " " + ((hours < 12) ? "am" : "pm")
  let minuteString = (mod % subdivisions * timeIntervalMinutes).toString()
  if (minuteString.length < 2) minuteString = "0" + minuteString
  const result = (dayString === check ? "" : dayString) + " " +
      hourString + ":" + minuteString + ampm
  if (returnDayString) return [result, dayString]
  return result
}
export const vecIndexPairToString = (i, j) => {
  const [res1, ds1] = vecIndexToString(i, true)
  const res2 = vecIndexToString(j + 1, false, ds1)
  return res1 + " to " + res2
}

// Recursively checks arrays for equality
export const arraysEqual = (a, b) => {
  if (a === b) return true
  if (a == null || b == null) return false
  if (a.length !== b.length) return false
  for (let i = 0; i < a.length; ++i) {
    if (!arraysEqual(a[i], b[i])) {
      return false
    }
  }
  return true
}