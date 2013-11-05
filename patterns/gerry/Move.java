/**
 * This class contains the set of moves that
 * Gerry computes for black player. The
 * relevant methods are getFrom(i) and getTo(i)
 * that represents a move from one location to
 * another. i represent the move number,
 * starting from 0. Thus i may be in range 1..4
 * depending on the number of moves possible
 * for black. Method 'noOfMoves' indicate
 * the number of moves possible - note in
 * special situations on backgammon it may
 * be 0 - if checkers in the bar and nowhere
 * to move to.
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
public class Move {
    int index;
    int[] leg;
    boolean solitude;

    public Move() {
        index = 0;
        leg = new int[8];
        solitude = false;
    }

    private Move(int theindex,
                 int[] theleg, boolean thesolitude) {
        index = theindex;
        leg = new int[8];
        solitude = thesolitude;
        System.arraycopy(theleg, 0, leg, 0, 8);
    }

    /**
     * mark these moves as found as solitude moves
     */
    public void markAsSolitude() {
        solitude = true;
    }

    public void add(int from, int to) {
        leg[index++] = from;
        leg[index++] = to;
    }

    public int noOfMoves() {
        return index / 2;
    }

    public int getFrom(int at) {
        return leg[at * 2];
    }

    public int getTo(int at) {
        return leg[at * 2 + 1];
    }

    /**
     * return true in case the moves are a 'solitude'
     * move i.e. a move that could only be accomplished by using
     * only one of the dice
     */
    public boolean isSolitude() {
        return solitude;
    }

    public Move getClone() {
        return new Move(index, leg, solitude);
    }

    public String toString() {
        String val = "Move: " + (isSolitude() == true ? "solitude" : "");
        for (int i = 0; i < noOfMoves(); i++) {
            val += "(" + getFrom(i) + "-" + getTo(i) + ") ";
        }
        return val;
    }
}
