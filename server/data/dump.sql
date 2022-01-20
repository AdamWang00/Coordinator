CREATE TABLE IF NOT EXISTS user (
    username TEXT PRIMARY KEY NOT NULL,
    hashedPassword TEXT NOT NULL,
    weeklySchedule BLOB NOT NULL
);

CREATE TABLE IF NOT EXISTS scheduleEvent (
    id INTEGER PRIMARY KEY,
    username TEXT NOT NULL,
    name TEXT NOT NULL,
    startIndex TEXT NOT NULL,
    endIndex TEXT NOT NULL,
    FOREIGN KEY (username) REFERENCES user(username) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS meetingGroup (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    adminUsername TEXT,
    parentMeetingGroupId INTEGER,
    highlightedMeetingTimeId INTEGER,
    currentStateEnum INTEGER NOT NULL,
    meetingDurationMinutes INTEGER NOT NULL,
    subgroupSize INTEGER NOT NULL,
    rgb STRING NOT NULL,
    FOREIGN KEY (adminUsername) REFERENCES user(username) ON UPDATE CASCADE ON DELETE SET NULL,
    FOREIGN KEY (parentMeetingGroupId) REFERENCES meetingGroup(id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (highlightedMeetingTimeId) REFERENCES meetingTime(id) ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS meetingGroupMembership (
    username TEXT NOT NULL,
    meetingGroupId INTEGER NOT NULL,
    PRIMARY KEY (username, meetingGroupId),
    FOREIGN KEY (username) REFERENCES user(username) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (meetingGroupId) REFERENCES meetingGroup(id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS meetingTime (
    id INTEGER PRIMARY KEY,
    meetingGroupId INTEGER NOT NULL,
    startIndex INTEGER NOT NULL,
    endIndex INTEGER NOT NULL,
    availabilityRank INTEGER NOT NULL,
    rgb STRING NOT NULL,
    FOREIGN KEY (meetingGroupId) REFERENCES meetingGroup(id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS meetingTimePreference (
    username TEXT NOT NULL,
    meetingTimeId INTEGER NOT NULL,
    preferenceScore INTEGER NOT NULL DEFAULT 2,
    isPinned INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (username, meetingTimeId),
    FOREIGN KEY (username) REFERENCES user(username) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (meetingTimeId) REFERENCES meetingTime(id) ON UPDATE CASCADE ON DELETE CASCADE
);