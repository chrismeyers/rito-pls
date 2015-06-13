# rito-pls
rito-pls is a Java application currently under development that reports the current League of Legends service statuses 
for a specified region.  The application queries the League of Legends API periodically and presents the current status
of several services (Boards, Game, Store and Website).

The purpose of this project is to get more practice with Java GUI building and multithreaded programming.  The polling
operation takes place on a new thread that allows for the program to function normally while maintaining periodic server
checks.

![Application reporting statuses](http://i.imgur.com/kdXiIhb.png)

## TODO

1. ~~Add a menu to quit, display Riot's legal statement and change polling increment.~~
2. ~~Add a pop-up to display the individual incidents for each service experiencing trouble.~~
3. ~~Add Title to window~~ ~~and a custom icon.~~
4. ~~General cleanup and formatting of GUI.~~
5. ~~Format incident time.~~

##Legal

riot-pls isn’t endorsed by Riot Games and doesn’t reflect the views or opinions of Riot Games or
anyone officially involved in producing or managing *League of Legends*. *League of Legends* and Riot Games
are trademarks or registered trademarks of Riot Games, Inc. *League of Legends* © Riot Games, Inc.
