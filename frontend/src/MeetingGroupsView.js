import './zMeetingGroupsView.css';
import MeetingGroupCard from "./MeetingGroupCard";

// Functional component: a card display of meeting groups
function MeetingGroupsView({
                             meetingGroups, selectedTimes, setSelectedTimes,
                             username, loginToken, setGroupRefreshCount
                           }) {

  // Return
  return (
      <div className="MeetingGroupsViewOuter">
        <div className="MeetingGroupsView">
          {meetingGroups.slice(0).reverse().map((ele, i) =>
              <MeetingGroupCard key={i} meetingGroup={ele}
                                selectedTimes={selectedTimes}
                                setSelectedTimes={setSelectedTimes}
                                username={username}
                                loginToken={loginToken}
                                setGroupRefreshCount={setGroupRefreshCount}/>)}
        </div>
      </div>
  );
}

export default MeetingGroupsView