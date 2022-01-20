import {cols, rows} from "./definitions";
import {vecToTable} from "./arrayHelperFunctions";

export const randInt = (min = 1000, max = 9999) => {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}
export const randAlphaNum = (qty) => {
  const letters = "QWERTYUPASDFGHJKLZXCVBNM", alpha = []
  while (qty > 0) {
    alpha.push((Math.random() < 0.5) ? randInt(0, 9) : letters[randInt(0, 23)])
    qty--
  }
  return alpha.join("")
}
export const randWord = (qty) => {
  let words = ["Club", "Texture", "Coat", "Interest", "Focus", "Post",
    "Spring", "Fall", "Club", "Daily", "Weekly", "Spark", "Bubble", "Exam",
    "Task", "Cook", "Team", "Airport", "Group", "First", "Day", "Project",
    "Discussion", "Club", "Stage", "Test", "Order", "Assignment", "Coding",
    "Sports", "Potato", "Nerd", "Meeting", "Group", "Fruit", "Complete",
    "Pink", "Core", "Hard", "Call", "Plan", "Way", "Discussion", "Full",
    "Group", "Life", "Plot", "Test", "CS", "Debate", "Supper", "Dinner",
    "Mass", "Effect", "Team", "Easy", "Control", "Development", "Exam", "Group",
    "Ideation", "Update", "Project", "Meeting", "Last", "Final", "Meeting",
  ], list = []
  while (qty > 0) {
    const word = words[randInt(0, words.length - 1)]
    words = words.filter(ele => ele !== word)
    list.push(word)
    qty--
  }
  return list
}
export const randSchedule = (len = rows * cols) => {
  const vec = Array(len)
  while (len > 3) {
    vec[len - 1] = Math.random() < 0.5;
    vec[len - 2] = vec[len - 1];
    vec[len - 3] = vec[len - 1];
    vec[len - 4] = vec[len - 1];
    len -= 4
  }
  return vecToTable(vec)
}
export const filledSchedule = (bool) => {
  Array.from(Array(rows), () => new Array(cols).fill(bool))
}