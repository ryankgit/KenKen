import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

/**
 * This program solves the game KenKen using Depth First Search (DFS) (w/ backtracking).
 * Takes file input from user, builds 2D-array, places values on 2D array ("board")
 * using recursive call within for loop
 * @author Ryan Kirsch
 * @version April 2019
 */

public class KenKen {
    // KenKen instance variables
    private static int[][] board;   // 2D array to keep track of board
    private static int boardSize;   // size of board (3,4, or 6 using test files, but could be of any int size)
    private static Point endPoint;  // last position (bottom right point) on board
    private static HashMap<Point, Cage> mapOfCageAtPoints;    // HashMap used to reference Cage (value) from Point (key)

    /**
     * main - prompts user for file, calls solveRec on empty board at position (0,0), prints solved board
     */
    public static void main(String[] args) {
        // get file
        Scanner keyboard = new Scanner(System.in);
        System.out.println("Enter file name: ");
        String fileName = keyboard.next();

        // read file - call readFile()
        readFile(fileName);
        // set new starting index at (0,0)
        Point startIndex = new Point(0, 0);
        // solve (call recursive method)
        boolean solved = solveRec(board, startIndex);
        if (solved) {
            System.out.println("Solution to KenKen puzzle:");
            System.out.println();
            // print board with solutions
            for (int[] row : board)
                System.out.println(Arrays.toString(row));
        } else {
            // no solution found, print message
            System.out.println("No solution found for " + boardSize + "x" + boardSize +" KenKen board.");
        }
    }

    /**
     * solveRec() - DFS recursively solves KenKen board. Makes recursive call within for loop
     * @param board 2D array game board
     * @param index Point position on board
     */
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

    /**
     * readFile() - reads file, instantiates instance variables, creates Cages
     * @param fileName file to read (passed in main)
     */
    private static void readFile(String fileName) {
        try {
            Scanner file = new Scanner(new File(fileName));
            // instantiate boardSize with first int value
            boardSize = file.nextInt();
            // initialize 2D-array board
            board = new int[boardSize][boardSize];
            // instantiate endPoint
            endPoint = new Point(boardSize - 1, boardSize - 1);
            // instantiate HashMap
            mapOfCageAtPoints = new HashMap<>();

            // skip line total is on
            file.nextLine();
            // read groups
            while (file.hasNextLine()) {
                // get line
                String line = file.nextLine();
                // split line into arr[]
                String arr[] = line.split(" ");             // line.split("\\s+") splits on any whitespace
                // Create new Cage
                Cage c = new Cage(Integer.parseInt(arr[0]), arr[1].charAt(0), getPoints(arr));
                // for each point in cage, add to mapOfCageAtPoints, with point as key, cage (c) as value
                for (Point p : c.listOfCagePoints) {
                    mapOfCageAtPoints.put(p, c);
                    //System.out.println(p.x + " " + p.y + " " + mapOfCageAtPoints.get(p));
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    /**
     * getPoints - from file input, get points in group
     * @param arr String array of file line read
     * @return Point array of points in group
     */
    private static Point[] getPoints(String arr[]) {
        // convert arr to arrayList (so I can use ArrayList's .remove(Object) method)
        ArrayList<String> arrList = new ArrayList<>(Arrays.asList(arr));
        // remove empty string that may or may not be in arr due to extra space (extra space is only in 4x4 file)
        arrList.remove("");

        // get size to make list of points
        // (don't need first two elements, and only need half the space because two ints make one Point)
        int size = (arrList.size() - 2) / 2;
        // make new Point array of size to put Points in and return
        Point[] list = new Point[size];

        int pos = 0;
        // read through elements in arrList
        for (int i = 3; i < arrList.size(); i = i + 2) {
            // create Points, add to list
            list[pos] = (new Point(Integer.parseInt(arrList.get(i-1)), Integer.parseInt(arrList.get(i))));
            pos++;
        }
        // return list of Points
        return list;
    }

    /**
     * solvable() - calls checkRow, checkCol, checks if getCageTotalWithValue is <= cage.total
     * @param value value to be placed in cage
     * @return boolean if value can be placed in that position on board (true/false)
     */
    private static boolean solvable(int value, Point index) {
        // get row and col from index Point
        int row = index.x;
        int col = index.y;
        // get cage at index (look in mapOfCageAtPoints HashMap for value (cage) at key (index))
        Cage cage = mapOfCageAtPoints.get(index);


        int filledPositions = 1;
        int cagePositionEqualsOne = 0;
        // get number of filled listOfCagePoints in cage (filledPositions = 1 because you assume value will be placed in cage too)
        // Also get number of cage listOfCagePoints that equal 1
        // (used if cageTotalWithValue = total, but there are still empty position, but that empty position can equal 1)
        for (Point p : cage.listOfCagePoints) {
            if (board[p.x][p.y] != 0)
                filledPositions++;
            if (board[p.x][p.y] == 1)
                cagePositionEqualsOne++;
        }

        // get total value of cage with index included (call getCageTotalWithValue() method)
        int cageTotalWithValue = cage.getCageTotalWithValue(value, index);

        // if value can be placed in row and col:
        if (checkRow(value, row) && checkCol(value, col)) {
            // if cage has reached it's total and all listOfCagePoints are filled, return true
            if (cageTotalWithValue == cage.total && filledPositions == cage.listOfCagePoints.length)
                return true;
            // else if cage total hasn't been reached yet but there are still empty listOfCagePoints, return true
            else if (cageTotalWithValue < cage.total && filledPositions < cage.listOfCagePoints.length)
                return true;
            // else if cage total is more than the total it should be, and there are still empty listOfCagePoints, and
            // the cage op is - or / (ie. total will be reduced later), return true
            else if (cageTotalWithValue > cage.total && filledPositions < cage.listOfCagePoints.length
                    && cage.op == '-' || cage.op == '/')
                return true;
            // if all listOfCagePoints aren't filled, but it multiplies up to the total,
            // and a listOfCagePoints can still be 1, return true
            else return (cageTotalWithValue == cage.total && filledPositions < cage.listOfCagePoints.length
                    && cagePositionEqualsOne <= 0 && cage.op == '*');
        }
        // value cannot be placed in cage at this index, return false
        return false;
    }

    /**
     * incrementIndex() - increments a Point (used to keep track of index on board)
     * [if index cannot be advanced without going off the board, it will return the same index]
     * @param pos current Point position
     * @return incremented Point position
     */
    private static Point incrementIndex(Point pos){
        // get row and col values from current pos
        int row = pos.x;
        int col = pos.y;

        if (col == boardSize - 1) {  // if last col reached:
            row++;                   // increment row
            col = 0;                 // new col
        } else {                     // else, only increment col
            col++;
        }

        // return incremented pos as new Point
        return new Point(row, col);
    }

    /**
     * checkRow() - checks if value can be places in row
     * @param value value to be placed
     * @param rowNum row number value is placed in
     * @return if row contains value already (false) or not (true)
     */
    private static boolean checkRow(int value, int rowNum){
        // traverse board cols
        for (int i = 0; i < boardSize; i++){
            // if any listOfCagePoints is row == value, value cannot be placed in this row
            if (board[rowNum][i] == value)
                return false;
        }
        return true;
    }

    /**
     * checkCol() - checks if value can be places in col
     * @param value value to be placed
     * @param colNum col number value is placed in
     * @return if col contains value already (false) or not (true)
     */
    private static boolean checkCol(int value, int colNum){
        // traverse board rows
        for (int i = 0; i < boardSize; i++){
            // if any listOfCagePoints is col == value, value cannot be placed in this col
            if (board[i][colNum] == value)
                return false;
        }
        return true;
    }

    /**
     * Cage Object - contains all info related to a cage
     */
    private static class Cage {
        // Cage instance variables
        private int total;  // group total
        private char op;    // operator for group
        private Point[] listOfCagePoints;   // list of Points that make up Cage

        /**
         * Cage constructor - initializes Cage instance variables
         * @param total group total to achieve
         * @param op operator
         * @param listOfCagePoints array of Points in group
         */
        private Cage(int total, char op, Point[] listOfCagePoints) {
            this.total = total;
            this.op = op;
            this.listOfCagePoints = listOfCagePoints;
        }

        /**
         * getCageTotalWithValue - checks if value can be placed in position given
         * cage constrains
         * @param value int value being placed in cage
         * @return int value of result of placing value in cage
         */
        private int getCageTotalWithValue(int value, Point index) {
            int currentValue = 0;   // current total of cage = 0
            boolean maxChanged = false;     // maxChanged boolean used for subtraction and division operations

            // ADDITION:
            if (op == '+'){
                // add up current value(s) of cage
                for (Point p : listOfCagePoints)
                    currentValue += board[p.x][p.y];
                return currentValue + value;
            }
            // SUBTRACTION:
            else if (op == '-'){
                // Get largest value and point in cage:
                int max = value;
                Point maxPoint = index;
                // search cage for larger max value
                for (Point mp : listOfCagePoints){
                    if (board[mp.x][mp.y] > max){
                        max = board[mp.x][mp.y];
                        maxPoint = mp;
                        maxChanged = true;
                    }
                }
                for (Point p : listOfCagePoints){
                    // subtract each point's value from max (except maxPoint's value)
                    if (p != maxPoint)
                        max -= board[p.x][p.y];
                }
                if (maxChanged)
                    return max - value;
                else
                    return max;
            }
            // MULTIPLICATION:
            else if (op == '*'){
                // set current value = 1 so you don't multiple by 0
                currentValue = 1;
                // multiply up current value(s) of cage
                for (Point p : listOfCagePoints){
                    // multiply by 0 would break everything, so only multiply current value if position != 0
                    if (board[p.x][p.y] != 0)
                        currentValue *= board[p.x][p.y];
                }
                return currentValue * value;
            }
            // DIVISION:
            else if (op == '/'){
                // Get largest value and point in cage:
                int max = value;
                Point maxPoint = index;
                // search cage for larger max value
                for (Point mp : listOfCagePoints){
                    if (board[mp.x][mp.y] > max){
                        max = board[mp.x][mp.y];
                        maxPoint = mp;
                        maxChanged = true;
                    }
                }
                for (Point p : listOfCagePoints){
                    // divide each point's value from max (except maxPoint's value)
                    // also do not divide max by and position that is 0
                    if (p != maxPoint && board[p.x][p.y] != 0)
                        max /= board[p.x][p.y];
                }
                if (maxChanged)
                    return max / value;
                else
                    return max;
            }
            // FREE SPACE   (op == '#')
            else {
                // value must be equal to total in this space
                return value;
            }
        }
    }
}