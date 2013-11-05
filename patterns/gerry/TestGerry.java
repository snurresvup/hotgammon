import org.junit.*;

import static org.junit.Assert.*;

/**
 * Learning tests for "Gerry" - a backgammon game AI player.
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
public class TestGerry {
    private int[] openingboard;
    private Gerry gerry;
    private Move move;

    @Before
    public void setUp() {
        gerry = new Gerry();
        openingboard = new int[28];
        // standard backgammon setup
        openingboard[1] = 2;
        openingboard[6] = -5;
        openingboard[8] = -3;
        openingboard[12] = 5;
        openingboard[13] = -5;
        openingboard[17] = 3;
        openingboard[19] = 5;
        openingboard[24] = -2;
    }

    @Test
    public void testOpening1() {
        move = gerry.play(openingboard, new int[]{1, 6});
        // 6-1 -> B8-B7 + R12-B7
        assertEquals(move.getFrom(0), 17);
        assertEquals(move.getTo(0), 18);
        assertEquals(move.getFrom(1), 12);
        assertEquals(move.getTo(1), 18);
    }
}
