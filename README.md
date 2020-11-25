# KenKen

This program solves the game [KenKen](https://en.wikipedia.org/wiki/KenKen#General_rules) using a depth-first search algorithm. Takes file input from user and places values on a 2D-array ("board") using recursive call within a for-loop. Both the unsolved and solved boards are displayed using Java's Swing GUI.

Practice with DFS, 2D-arrays, HashMaps, and Java's Swing GUI.

# Recursive Method

~~~java
private static boolean solveRec(int[][] board, Point index) {
        // base case: endPoint of board != 0 (ie. board is full with working solutions)
        if (board[endPoint.x][endPoint.y] != 0)
            return true;

        // try each number 1 - boardSize
        for (int i = 1; i <= boardSize; i++){
            // if solvable with number (i) in position (index), given cage constraints
            if (solvable(i, index)) {
                // add i to board at position index
                board[index.x][index.y] = i;
                // increment index
                Point indexAdvanced = incrementIndex(index);
                // recursive call, on updated board and advanced index
                boolean temp = solveRec(board, indexAdvanced);
                // if works with number in index position, return true
                if (temp)
                    return true;
                else {
                    // reset last index position to 0
                    board[index.x][index.y] = 0;
                }
            }
        }
        // no board solution found
        return false;
    }
~~~


# Structure of Test Files

Each test file is structured as follows:
~~~
[Size of board]
[Cage value] [Cage operator] [Cell x coord] [Cell y coord] [Cell x coord] [Cell y coord] ...
[Cage value] [Cage operator] [Cell x coord] [Cell y coord] [Cell x coord] [Cell y coord] ...
[Cage value] [Cage operator] [Cell x coord] [Cell y coord] [Cell x coord] [Cell y coord] ...
...
~~~

`kenken4-1.txt` example:
~~~
4
16 * 0 0 0 1 1 1
7 +  0 2 0 3 1 2
2 -  1 0 2 0
4 #  1 3
12 * 2 1 3 0 3 1
2 /  2 2 2 3
2 /  3 2 3 3
~~~

# Demo

Program prompts user for test file path:

![Example program input](/example_images/user_input.png)

Program recursively solves test file via Depth First Search:

* Cages are identified by like colors
* Unsolved board displayed on the left, solved board on the right

![Example program output](/example_images/program_output.png)

# Future Work

* Move Cage class to seperate file to improve program modularity and readability
* Move GUI class to seperate file to improve program modularity and readability

