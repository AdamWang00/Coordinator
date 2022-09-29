# Coordinator

#### Introduction
Teams spend a lot of time finding common timeslots to meet.

#### Problem(s)
Some use sites like when2meet, where each member fills in their yes/no availability over a certain period of time. However, one would have to repeatedly fill in details for different meetups, projects and groups, and the process generally requires one or more people to take charge of just scheduling the meeting. This gets even more complicated when trying to form multiple sub-teams (e.g. forming project pairs in CS32) by matching their schedules optimally. Some users may also not be comfortable revealing their exact schedule to everybody else, especially in a large group.

#### Solution
My solution for this problem would be a web app where people can create accounts and store the time periods that they are available.

#### Feature 1
Users can create a very basic account which stores their schedule. This schedule is not visible to other users; it will only be used for finding ideal meeting timings when this user is in a group. This feature shouldn't be too challenging, but the hardest part will probably be setting up the authentication flow.

#### Feature 2
When a user wants to find a common time for a group to meet, the user can add all of its members into a group. The application will then suggest the optimal time(s) (based on best coverage) for the group. The user can specify parameters like total hours, number of separate meetings, maximum meeting length, etc. This streamlines the scheduling process for teams trying to meet up. The most challenging aspect of this feature would be programming an efficient algorithm to produce the optimal time for the group, based on the parameter constraints.

#### Feature 3
Out of the returned options, individual members can then vote on the options that they most prefer. We could display these options against their own schedules, which would make it easier to decide (e.g. maybe the user has many meetings on the same day). The most challenging part of this feature would be to implementing an intuitive UI for users to view and vote for the options.

#### Feature 4
When a user wants to split a group into multiple subgroups (e.g. forming project pairs from the entire CS32 class), they can add all of the members into a super-group and then select the sub-group size. The application will then attempt to form sub-groups by matching the schedules of all members within the super-group. Finally, the application can suggest optimal time(s) for each sub-group, as described in Feature 1. This significantly reduces the effort needed to subdivide a large group. The hardest part of this would be developing an efficient algorithm to match people based on their schedules.

#### Possible Additional Feature 1
An additional feature which also adds algorithmic complexity to this project is the ability to set different levels of availability (eg. preferred, available, inconvenient, unavailable etc). We can then assign weights, and our algorithm will have to take that into account when finding the most optimal time(s).

#### Possible Additional Feature 2
When forming subgroups, we might also want to offer the ability to match users based on other parameters besides meeting availability, say, project preferences, strengths, or working styles. To make this more generic / useful for different needs, this could be implemented by matching based on users’ responses to questions that they are presented with.

#### Why
The advantage of this over current solutions is that users do not have to fill in the same calendars repeatedly for different groups of people. Users can have their availability saved and be reused for multiple groups of people. Another big advantage of this solution is privacy. Users may not want to have to reveal their entire schedule to other people. This application will not reveal any information about anyone’s availability except the time slot that it has determined to be the most optimal.

## Design Details / Code Architecture
Inside the server folder there are 5 packages.
The coordinator package contains all internal representations of key objects (User, MeetingGroup, 
MeetingTime, MeetingTimePreference) as well as Coordinator which contains methods to connect all these
objects together. It also contains a CoordinatorDatabaseClient class for Coordinator to interact with the SQL database
containing all of our data.

The database package and class contain methods to create a connection as well as execute general SQL queries.

The kmeans package contains all the methods to run the algorithms necessary for the backend. 
It contains a class to run the k-means algorithm, one that iteratively runs k-means to create evenly sized groups 
and one that selects the best times based on a list of weights. The classes themselves are generic,
and work on anything that implements the VectorData interface.

The main package contains the class main that creates the routes and spark servers on startup.

The routes package contains all the classes and routes for the backend to interact with the front end.
It contains some general routes for encryption, as well as three subpackages containing more routes.
auth contains routes for getting login and account information, group contains routes for group formation
and joining while user contains routes for getting information about a users groups and schedule.

All frontend material is contained within the frontend folder. Within there, the design is divided into 
several React components and style files.

## How to Build and Run
First, create a database file (e.g. `mydb.sqlite3`) initialized with the 6 tables found in `server/data/dump.sql` (or use an existing database file in the `server/data` folder). 
Then, run `mvn package` from the server folder to build the Java project, and run `yarn` from the frontend folder to install JavaScript dependencies.
To run the backend, run `./run --db <path to database file>` from the server folder, and to run the frontend, run `yarn start` from the frontend folder.

## Checkstyle Errors
There is one Checkstyle error: our `MeetingGroup` class takes in more than 7 
arguments in its constructor because it has many independent fields that must be initialized.
