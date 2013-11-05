/**
 * Skeleton implementation of HotGammon.
 * <p/>
 * This source code is from the book
 * "Flexible, Reliable Software:
 * Using Patterns and Agile Development"
 * published 2010 by CRC Press.
 * Author:
 * Henrik B Christensen
 * Computer Science Department
 * Aarhus University
 * <p/>
 * This source code is provided WITHOUT ANY WARRANTY either
 * expressed or implied. You may study, use, modify, and
 * distribute it for non-commercial purposes. For any
 * commercial use, see http://www.baerbak.com/
 */

public class GameImpl implements Game {
    public void newGame() {

    }

    public void nextTurn() {

    }

    public boolean move(Location from, Location to) {
        return false;
    }

    public Color getPlayerInTurn() {
        return Color.NONE;
    }

    public int getNumberOfMovesLeft() {
        return 0;
    }

    public int[] diceThrown() {
        return new int[]{1, 1};
    }

    public int[] diceValuesLeft() {
        return new int[]{};
    }

    public Color winner() {
        return Color.NONE;
    }

    public Color getColor(Location location) {
        return Color.NONE;
    }

    public int getCount(Location location) {
        return 0;
    }
}
