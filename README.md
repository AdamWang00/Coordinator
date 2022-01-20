# cs0320 Term Project 2021

## Team Members:
Adam Wang, Joshua Tan, Stephen Chen, Zack Cheng

## Team Strengths and Weaknesses:
### Adam Wang
- *Strengths*:
   - Full stack experience
   - Comfortable with multiple languages and frameworks
- *Weaknesses*:
   - Tendency to procrastinate when task is ambiguous
   - Frequently forgets to comment
### Joshua Tan
- *Strengths*:
   - Good at full stack development (React, Node.js etc)
   - Good at JavaScript
   - Comments a lot (maybe too much)
- *Weaknesses*:
   - Careless code
   - Consistently bad testing
### Stephen Chen:
- *Strengths*:
   - Really good at SQL
   - Good at quantitative problem solving
   - Has passion for testing edge cases
- *Weaknesses*:
   - Frequently forgets to comment
   - Despises Javascript (probably hates what he cannot understand)
### Zack Cheng:
- *Strengths*:
   - Really good with design and frontend (HTML/CS/JS)
   - Decent with data structures and algorithms
   - Consistent with commenting and code/visual style
- *Weaknesses*:
   - Strong dislike for SQL
   - Also dislikes relying on too many external packages

## Project Idea(s):

### Idea 1: Fish\Literature

Rules linked here https://en.wikipedia.org/wiki/Literature_(card_game)Fish (or Literature as it is more commonly known)is a card game that I loved to play while on road trips/traveling. Looking online for an implementation that I could easily play with friends was lacking. We would like to create an application to play Literature, that improves upon the existing ones with several key improvements.
#### Requirements
The only online implementation of this card game we could find was lackluster. The user interface was not friendly, the card selection was not intuitive and there was only 1 level of AI. Additionally, it did not appear to use the game rules that I was accustomed to.

#### Feature 0
Obviously, the core component would be to creating a simulation of the game with an easily accessible GUI. Programming the logic of the game and displaying it back to users is the first, more important features.

#### Feature 1
Learning how to play Fish can be somewhat daunting of a task, but I’ve found that after playing through a couple turns it becomes very intuitive. Including a tutorial that does so, would help new players understand the game and get involved. If the core functionality of the game is working, the most difficult part of implementing this is likely finding the best way to explain a somewhat complex game.

#### Feature 2
One feature of the game involves choosing from a (restricted) list of possible cards to guess that your opponent has. Currently the UI in the existing implementation, merely lists the possible cards in an unappealing, unintuitive way. We would like to improve this through one of many possible options (ex: sorting them based on category, then having the user select the category first).

#### Feature 3
Another feature of our implementation would be to include several levels of AI difficulty so newcomers to the game can start off simple, but increase the difficulty level as they become more proficient. We will also implement the option to select different rules for the game as people seem to play by different rules. Creating an good AI for a game of incomplete information should pose a significant challenge, especially when there are multiple difficulties involved. Theoretically, a computer could play this game optimally (it is often a test of memory), so making a AI that makes mistakes could be a challenge.

**HTA Approval (dpark20):** Idea approved contingent on the AI being fairly complex and not a standard ML-esque algorithm.

### Idea 2: Diagram to LaTeX conversion

#### Introduction
Oftentimes, I want to generate a diagram in LaTeX, which the TikZ package is made for. However, the TikZ package requires exact coordinates for many of its parts, and working with it is a nightmare. We want to create an application that allows users to create various types of diagrams easily, and then automatically generate the equivalent LaTeX code that will create said diagram.

#### Problem(s)
Conversion from TikZ / LaTeX to diagrams is near impossible and applications that convert from diagrams to LaTeX exist, but only for 1 extremely specific type of diagram at a time. We wish to fill this niche by creating a general purpose diagram creator that creates equivalent LaTeX, and can also convert LaTeX to equivalent diagrams.

#### Feature 1
Users should be able to create diagrams quickly, easily and with lots of customizability. Ideally, it would have as much flexibility and ease of use as other applications that already exist and fill this space, such as draw.io / lucidchart etc. The problem with existing similar applications is their lack of flexibility, so this part is necessary to improve on them. After figuring out how to do the first basic diagrams, adding more features shouldn't be that difficult, but somewhat tedious.

#### Feature 2
Ease of use features could include selecting from various pre-existing templates for diagrams or user-specified customizable types of objects. Example of user-specified object type: A user might be making a diagram that involves them repeatedly making a similar object (say several 3x4 tables with the same header). The user could “save” this data-type so they could easily repeatedly make this object. This is, again, necessary for improving upon similar implementations, and would be what pulls users to this. Giving users a lot of power and flexibility might lead to unpredictable inputs, which will be difficult to handle.

#### Feature 3
Users should be able to easily export their diagrams in various forms. This includes various image files, but uniquely this application will also create equivalent LaTeX code that merely be copied and pasted into a LaTeX to create the equivalent diagram.   
Users should be able to input LaTeX code into this application, and it should create the corresponding diagram. Oftentimes making LaTeX/TiKz diagrams involves listing precise coordinates, and making fine-tuned changes is difficult without changing all interconnected parts. Thus, by importing the LaTeX code they will convert it into an easier to work with format, make the necessary changes, and then re-export the code. These two processes are the core functionality of this application, but is likely easier to implement than the general-purpose diagram creation.

**HTA Approval (dpark20):** Idea not approved (yet!). It needs to be a little clearer what the algorithmic component of the application is. There is definitely a challenge in converting things, but we need to see more on how you plan on approaching this algorithmically before approving! Feel free to resubmit if you'd like to pursue this idea.

### Idea 3:  Coordinator

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

**HTA Approval (dpark20):** Idea approved contingent on the algorithmic complexity being more than just assigning weight values to each and adding up scores to find the max score. This seems a little too simple! There are definitely interesting ways to add more depth to the decision-making.

Great ideas! Looking forward to seeing what you all build.


## Mentor TA: Grace Bramley-Simmons - grace_bramley-simmons@brown.edu

## Meetings
_On your first meeting with your mentor TA, you should plan dates for at least the following meetings:_

**Specs, Mockup, and Design Meeting:** 3/15

**4-Way Checkpoint:** 4/5

**Adversary Checkpoint:** 4/12

## Division of Labor

Zack - frontend, website design

Stephen - backend algorithms

Adam - database interactions

Joshua - authentication, endpoint and route handling

## Known Bugs
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
