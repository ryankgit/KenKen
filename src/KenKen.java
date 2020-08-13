import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;

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
    private static String fileName; // String name of KenKen .txt file

    /**
     * main - calls solveRec on empty board at position (0,0)
     */
    public static void main(String[] args) {
        // read file
        readFile();
        // set new starting index at (0,0)
        Point startIndex = new Point(0, 0);
        // solve (call recursive method with 2D array board and 0,0 starting index)
        solveRec(board, startIndex);
        // display solved board w/ Swing GUI
        new GUI();
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
     * readFile() - gets file via user input, reads file, instantiates instance variables, creates Cages
     */
    private static void readFile() {
        // get user input, instantiate fileName
        fileName = JOptionPane.showInputDialog(null, "Enter File Name:");

        try {
            // create new Scanner
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
            // read cages
            while (file.hasNextLine()) {
                // get line
                String line = file.nextLine();
                // split line into array
                String[] arr = line.split(" ");

                // Create new Cage
                Cage c = new Cage(Integer.parseInt(arr[0]), arr[1].charAt(0), getPoints(arr), getRandColor());

                // for each point in cage, add to mapOfCageAtPoints, with point as key, cage (c) as value
                for (Point p : c.listOfCagePoints) {
                    mapOfCageAtPoints.put(p, c);
                }
            }
            // close scanner
            file.close();

        } catch (Exception e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    /**
     * getPoints - from file input, get points in group
     * @param arr String array of file line read
     * @return Point array of points in group
     */
    private static Point[] getPoints(String[] arr) {
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
        // get Cage at index position
        Cage cage = mapOfCageAtPoints.get(index);

        // get number of filled listOfCagePoints in cage (filledPositions = 1 because you assume value will be placed in cage)
        int filledPositions = 1;
        // boolean denoting if a point in listOfCagePoints contains a value that equals 1, set to false by default
        // (used if cageTotalWithValue = total, but there are still empty positions, but that empty position can equal 1)
        boolean cagePositionEqualsOne = false;
        // iterate listOfCagePoints
        for (Point p : cage.listOfCagePoints) {
            // count filled position on board
            if (board[p.x][p.y] != 0)
                filledPositions++;
            // determine if Cage has a position where the value = 1
            if (board[p.x][p.y] == 1)
                cagePositionEqualsOne = true;
        }

        // get total value of cage with index included (call getCageTotalWithValue() method)
        int cageTotalWithValue = cage.getCageTotalWithValue(value, index);

        // if value can be placed in row and col:
        if (checkRow(value, row) && checkCol(value, col)) {
            // if cage has reached it's total and all listOfCagePoints are filled, return true
            if (cageTotalWithValue == cage.total && filledPositions == cage.listOfCagePoints.length)
                return true;
            // else if there are still empty listOfCagePoints:
            else if (filledPositions < cage.listOfCagePoints.length) {
                // if cage total hasn't been reached yet (still space to add in more values), return true
                if (cageTotalWithValue < cage.total)
                    return true;
                // else if cage total is more than the total it should be and
                // the cage op is '-' or '/' (ie. total will be reduced later), return true
                else if (cageTotalWithValue > cage.total && cage.op == '-' || cage.op == '/')
                    return true;
                // if all listOfCagePoints aren't filled, but it multiplies up to the total,
                // and there is not a cage position that equals 1 (!cagePositionEqualsOne), return true
                else return (cageTotalWithValue == cage.total && !cagePositionEqualsOne && cage.op == '*');
            }
        }
        // value cannot be placed at this index (checkRow/checkCol returned false), return false
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

        // if last col reached:
        if (col == boardSize - 1) {
            // move to next row
            row++;
            // start at col 0
            col = 0;
        } else {
            // not at last col, increment col, keep current row
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
            // if any listOfCagePoints is row == value, value cannot be placed in this row, return false
            if (board[rowNum][i] == value)
                return false;
        }
        // value can be placed in this row, return true
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
            // if any listOfCagePoints is col == value, value cannot be placed in this col, return false
            if (board[i][colNum] == value)
                return false;
        }
        // value can be placed in this col, return true
        return true;
    }

    /**
     * getRandColor() - get random Color
     * @return random Color
     */
    private static Color getRandColor(){
        // create new Random to assign color
        Random rand = new Random();
        // return random Color object based on Random.nextInt, bound white
        // note: nextInt(bound) returns "uniformly distributed int value between 0 and bound"
        return new Color(rand.nextInt(0xFFFFFF));
    }

    /**
     * Cage Object - contains all info related to a Cage on the board
     */
    private static class Cage {
        // Cage instance variables
        private int total;  // group total
        private char op;    // operator for group
        private Point[] listOfCagePoints;   // list of Points that make up Cage
        private Color color; // random assigned Color

        // Cage max instance variables (only instantiated if cage op is subtraction or division by getCageMaxInfo())
        private int cageMax;
        private Point cageMaxIndex;
        private boolean cageMaxChanged;

        /**
         * Cage constructor - initializes Cage instance variables
         * @param total group total to achieve
         * @param op cage operator
         * @param listOfCagePoints array of Points in group
         * @param color Color value assigned to Cage at creation by getRandColor() function
         */
        private Cage(int total, char op, Point[] listOfCagePoints, Color color) {
            // set Cage instance variables:
            this.total = total;
            this.op = op;
            this.listOfCagePoints = listOfCagePoints;
            this.color =  color;
        }

        /**
         * getCageMaxInfo - instantiates cageMax, cageMaxIndex, cageMaxChanged instance variables, determines their values based on Cage
         * @param value default/assumed max value
         * @param index index of value within Cage
         */
        private void getCageMaxInfo(int value, Point index){
            // instantiate cage max info instance variables:
            // default cage max value is set to value
            cageMax = value;
            // default max value index
            cageMaxIndex = index;
            // boolean if cage max value has been changed, default = false
            cageMaxChanged = false;

            // iterate points in listOfCagePoints (search cage for larger max value)
            for (Point p : listOfCagePoints){
                // if a value is found larger than current
                if (board[p.x][p.y] > cageMax){
                    // set cageMax larger found value
                    cageMax = board[p.x][p.y];
                    // set cageMaxIndex to new max's index
                    cageMaxIndex = p;
                    // cageMax was changed, set maxChanged to true
                    cageMaxChanged = true;
                }
            }
        }

        /**
         * getCageTotalWithValue - checks if value can be placed in position, given
         * cage constrains
         * @param value int value being placed in cage
         * @return int value of result of placing value in cage
         */
        private int getCageTotalWithValue(int value, Point index) {
            // IF ADDITION:
            if (op == '+'){
                // add up current value(s) of cage
                for (Point p : listOfCagePoints)
                    value += board[p.x][p.y];
                return value;
            }
            // IF SUBTRACTION:
            else if (op == '-'){
                // get and instantiate cageMax, cageMaxIndex, cageMaxChanged instance variables
                getCageMaxInfo(value, index);

                // iterate points in Cage
                for (Point p : listOfCagePoints){
                    // for all values other than maxPoint's value
                    if (p != cageMaxIndex)
                        // subtract each point's value from max
                        cageMax -= board[p.x][p.y];
                }
                if (cageMaxChanged)
                    return cageMax - value;
                else
                    return cageMax;
            }
            // IF MULTIPLICATION:
            else if (op == '*'){
                // iterate points in Cage
                for (Point p : listOfCagePoints){
                    // for all values where value != 0 (multiplying by 0 will set value = 0)
                    if (board[p.x][p.y] != 0)
                        // multiple each value together
                        value *= board[p.x][p.y];
                }
                return value;
            }
            // IF DIVISION:
            else if (op == '/'){
                // get and instantiate cageMax, cageMaxIndex, cageMaxChanged instance variables
                getCageMaxInfo(value, index);

                // iterate points in Cage
                for (Point p : listOfCagePoints){
                    // divide each point's value from max (except maxPoint's value)
                    // also do not divide max by and position that is 0
                    if (p != cageMaxIndex && board[p.x][p.y] != 0)
                        cageMax /= board[p.x][p.y];
                }
                if (cageMaxChanged)
                    return cageMax / value;
                else
                    return cageMax;
            }
            // FREE SPACE   (op == '#')
            else {
                // value must be equal to total in this space
                return value;
            }
        }
    }

    /**
     * GUI Object - Implement Swing GUI to display solved and unsolved boards side by side
     * June 2020
     */
    private static class GUI {

        /**
         * boardJPanel() - create JPanel split into grid of boardSize, sets border
         * @return new JPanel grid layout
         */
        private JPanel newJPanelTemplate(){
            // create new JPanel
            JPanel panel = new JPanel();
            // set panel border
            panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
            // set panel to grid layout based on boardSize
            panel.setLayout(new GridLayout(boardSize, boardSize, 1, 1));
            // return panel template
            return panel;
        }

        /**
         * stylePanel() - apply JPanel/JLabel styles
         * @param label JLabel (either cage total+op or solved value)
         * @param panel JPanel (board) grid
         * @param cage current Cage to pull Color value from
         */
        private void stylePanel(JLabel label, JPanel panel, Cage cage){
            label.setOpaque(true);
            // set background based on cage color
            label.setBackground(cage.color);
            // add label to panel
            panel.add(label);
        }

        /**
         * genFrame() - creates JPanel container to hold blank/solvedPanels, creates JFrame to hold container,
         * defines frame properties
         * @param blankPanel JPanel grid w/ JLabels corresponding to cage instructions
         * @param solvedPanel JPanel grid w/ JLabels corresponding to cage solutions
         */
        private void genFrame(JPanel blankPanel, JPanel solvedPanel){
            // container panel to hold blank and solved board panels
            JPanel container = new JPanel();
            container.setLayout(new GridLayout(1,2));

            JFrame frame = new JFrame(fileName);
            // add container to JFrame
            frame.add(container);

            // add blankPanel to container panel
            container.add(blankPanel, BorderLayout.CENTER);
            // add solvedPanel to container panel
            container.add(solvedPanel, BorderLayout.CENTER);

            // set frame style, size, settings, close operation, etc
            frame.setPreferredSize(new Dimension(800, 400));
            frame.setResizable(false);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }

        /**
         * GUI constructor - creates new JFrame, creates container JPanel to hold two boardJPanel grid
         * layouts (unsolved and solved board), sets JLabels within JPanel grids to display board values
         */
        private GUI(){
            // create blank board panel
            JPanel blankPanel = newJPanelTemplate();
            // create solved board panel
            JPanel solvedPanel = newJPanelTemplate();

            // fill blankPanel grid
            for (int i = 0; i < board.length; i++){
                for (int j = 0; j < board[i].length; j++){
                    // get cage at board pos
                    Cage c = mapOfCageAtPoints.get(new Point(j, i));
                    // set label based on cage operator and total
                    JLabel l = new JLabel(c.total + " " + c.op, SwingConstants.CENTER);
                    // set panel display properties, display color
                    stylePanel(l, blankPanel, c);
                }
            }

            // fill solvedPanel grid
            for (int i = 0; i < board.length; i++){
                for (int j = 0; j < board[i].length; j++){
                    // get cage at board pos
                    Cage c = mapOfCageAtPoints.get(new Point(j, i));
                    // set label based on int value in board array
                    JLabel l = new JLabel(Integer.toString(board[j][i]), SwingConstants.CENTER);
                    // set panel display properties, display color
                    stylePanel(l, solvedPanel, c);
                }
            }

            // create frame: add blankPanel and solvedPanel to container panel, add to JFrame, set JFrame style properties, etc
            genFrame(blankPanel, solvedPanel);
        }
    }
}
