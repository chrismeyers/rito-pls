# rito-pls
rito-pls [ree-toe please] is a Java application that reports current *League of Legends* service statuses for a specified region.  The application queries the *League of Legends* API periodically and presents the current status of several services (Boards/Forums, Game, Store and Website).

The purpose of this project was to get more practice with Java GUI building and multithreaded programming.  The polling operation takes place on a new thread that allows for the program to function normally while maintaining periodic server checks.

<img src="http://i.imgur.com/kdXiIhb.png" alt="Current GUIt" width="335px" height="358px">
<img src="http://i.imgur.com/fAcPYfP.png" alt="Current GUI">

## Usage

### Get the program running
1. Clone this git repository or download as .zip.
2. Run the .jar `dist/rito-pls.jar` by:
  * Double clicking the .jar file.
    * If double clicking doesn't work, the following program works well to fix the .jar file association:       http://johann.loefflmann.net/en/software/jarfix/index.html
  * or, running the following command from the command line:
    * `java -jar "X:/PATH/TO/JAR/rito-pls.jar"`

### Using the program
1. (Optional) Select a polling rate from `File > Set Polling Rate`.
  * This determines how often the program queries the servers.
  * Default is 10 seconds.
2. Select a region from the dropdown menu.
3. Press "Click to Check" button to begin querying.
4. If incidents exist, click the button next to the status to see the related incident(s).
5. Click the "Checking..." button to stop querying the server.


##Legal

riot-pls isn’t endorsed by Riot Games and doesn’t reflect the views or opinions of Riot Games or
anyone officially involved in producing or managing *League of Legends*. *League of Legends* and Riot Games
are trademarks or registered trademarks of Riot Games, Inc. *League of Legends* © Riot Games, Inc.
