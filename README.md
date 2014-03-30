Wolves And Sheep
========

This is a multi-agent, bio-inspired simulation; it is a competitive game and a tournament written for 5-50 beginner programmers.

It is designed as educational tool:  Students of an intermediate programming class taught in Java write code that controls a single player in this simulation.  Players are pitched against each other; typically 5 at a time.  A tournament runs many combinations of players in many adjustable scenarios.

The tournament is set up to run regularly on a server, taking .jar files provided by students.  It uses standard visibility and sandboxing features of Java to provide a basic level of

The project is described in detail here:

Reitter, David T. Hungry Wolves, Creepy Sheepies: The Gamification of the Programmer’s Classroom.  In: Carroll, J. (Ed.), Innovative Practices in Teaching Information Sciences and Technology.  Springer, 2014.


Instructions: Wolves and Sheep
====

During the final project, you will apply many of the things you learned in this class.  You will also improve your team-working skills.  In order to do this job well, you need to understand how different classes interface with each other.

In this game, we have a number of players interacting in a simple way.  The game takes place on a (virtual), two-dimensional piece of land.  There are two types of player: Sheep, and Wolves. Sheep start at one point of the land, and they have to get to get to a green pasture to win.  They move one step at a time.  Usually, there are several sheep that start out at the same time.  The more sheep get to the destination, the better.  This diagram shows five sheep (“s”) and the green pasture at the top:
Unfortunately, there is also a wolf. The wolf’s objective is to find (and eat) as many sheep as possible.  The wolf may be a little faster than the sheep, but it always needs a number of steps to finish eating one.

Sheep and wolves are realized as separate Java classes.  Each student will submit either a Sheep or a Wolf class. 

We’re going to play a tournament: we’re going to have an instance of each wolf and instances of four or five different sheep play the game and tally up the results.

Beware... the rules of the game are simple, but its dynamics can be very complex.


Teamwork
-------

Each student is submit their own player class (either a wolf or a sheep).

We’re going to designate teams of 3-5 students each.  I expect team members to collaborate.
That’s because your sheep may work in teams, too.  Therefore, sheep playing the game together are written by the students of one team.  That way, the sheep may collaborate to get away from the wolf.  In the team, you should think about what it takes to make that collaboration happen, and what good strategies are to win this game.

Wolves, on the other hand, are loners.  Yet, your team may collaborate to write the best wolves possible!

Team collaboration means that you may exchange ideas, help each other with coding problems, or share a small, common set of helper functions to do certain calculations.  However, your individual players must have individual strategies.

Summary: Initially, you choose whether you’d like to write a wolf or a sheep agent.  Then, you are assigned to either a “sheep” or a “wolf” team.  You will write your own agent, but you will collaborate with your fellow group members.  Your grade will depend on the performance of your agent, but also on the aggregate performance of your team.

Example:  Suppose we have 30 students (numbered 0..29) in this class.  We will form 7 teams (A,B,C,D,E,F,G).   Teams A,B,C,D will write the sheep - this means we’ll have about 16 Sheep classes coming in.  Teams E,F,G will write wolves - which makes another 14 Wolf classes.   

In the tournament, we’ll run many thousand games, each with one combination of the sheep classes from one of the four sheep teams, and exactly one wolf (out of 14) - that’s 56 combinations. 

We need three rules of engagement to make this work.

Rule 1: You may not collaborate with other students outside of the team.

Rule 2: You should collaborate with the other students within your team.  While everyone has to do their own implementation, you, for example may collude to plot joint strategies against the wolf (if you’re programming a sheep agent).

Rule 3: You must submit only your own code and may not write code for others.  Small, shared functions that are marked as such are OK.

Game Rules
-----

The playing field is a m by n rectangular grid with m*n cells.  In each game, there are a number of players (several sheep players, and one wolf).   Each player occupies exactly one cell in the grid, and no cell will contain more than one player (except a combination of the wolf and a sheep).  Cells may also contain an obstacle, in which case no player can occupy the cell. 
 Players start at an assigned location. There are multiple steps (iterations): In each iteration, each sheep may move exactly one step to the left, the right, up, down, or diagonally, but not beyond the boundaries of the grid.  In each iteration, the wolf may also move a constant k steps (k is to be determined by the game, and fixed throughout the tournament.  All players will be told k).  All players make one move at a time and see the updated positions.  The wolf moves last.

The player instance determines a move and communicates it as Δx and Δy (steps in each direction), where Δx and Δy are signed integers.   For sheep, (Δx2 + Δy2) <= 2.0.   For the wolf, (Δx2 + Δy2) < k2.  For instance, if the wolf decides to move 2 steps to the left (Δx=-2) and 2 steps up (Δy=-2), then that would be a legal move if k is at least 2.9, because:

 
 











If more than one sheep try to occupy the same cell, one of the sheep’s moves fails and the sheep will not move.  If the wolf and a sheep attempt to occupy the same location, the wolf begins to eat the sheep.  A meal takes a number of steps f, during which the wolf will not move.  The sheep perishes.  Several green pastures (“goal cells”) are determined by the game.  Their location is known to all players. If a sheep reaches one these pastures, the sheep wins its game and disappears.  The wolf cannot enter a pasture. The game terminates when all sheep have reached pastures or have been eaten, or after m+n steps, whichever comes first.

Parameters: The game parameters are available to the players upon instantiation:


Scoring: A sheep class is awarded one point whenever its instance reaches a goal cell.  A wolf class is awarded one point whenever its instance eats a sheep.  Sheep and Wolf classes will be ranked separately.

Coding
===
We will develop interfaces for the players in class.  In general, the Player class defines an initialize() method and a move() method (which decides about the move).
There will also be a whiteboard object that is shared between the sheep, but not visible to the wolf.  This whiteboard can be used by the sheep to communicate.  Sheep will also get references to the other sheep in the game.  This, too, may be used to communicate and coordinate the joint quest to evade the wolf.

You must create your code in your own package.  Use this command at the top of each of your source files:

package lastname;

Substitute your last name (all lower-case) for lastname, and place the source file into a folder named after the package (or use your IDE to put it into the right folder).  Do this for all of your classes.

Call your sheep class Sheep, or your wolf class Wolf.  

You will get a test environment to run your class against basic other wolves or sheep.  You will be asked to submit working code as a .jar file, and with source code.  Name your jar file lastname.jar.  The jar files will be used for evaluation.

Do not rely on any private field or function, or anything that is not documented.  You cannot extend (or change) the “was” package – it will be removed from your .jar file.



Performance Requirements
===

Each player has to execute its functions (chiefly, “move”) in reasonable time.
The definition of “reasonable” may change, but in principle, players are disqualified for either the game or the whole tournament if they take too long to make a move.
For example:  If a player takes more than 7 ms. on average to run its functions (per turn), it will be disqualified for the rest of the game (it will no longer make a move).  If a player spends more than 200ms on a single function or turn, it will also be disqualified for the rest of the game. If this happens more than 5 times during a tournament, it will be disqualified permanently for the tournament.
If your player takes up too much memory, it may also be banned.  Be reasonable.

Submission
===
You will be able to test your agent using provided code. We will develop this in class.
Your agent source code and a compiled .jar file are due as announced. If we have time, you can fine-tune your program and improve your performance before the final tournament.  You will be given code for a tournament or a visual interface that will allow you to test your class. 
