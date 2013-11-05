import org.junit.*;

import static org.junit.Assert.*;

/**
 * Testing skeleton for AlphaMon.
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
public class TestAlphamon {
    private Game game;

    @Before
    public void setup() {
        game = new GameImpl();
    }

    @Test
    public void shouldHaveNoPlayerInTurnAfterNewGame() {
        game.newGame();
        assertEquals(Color.NONE, game.getPlayerInTurn());
    }

    @Test
    public void shouldHaveBlackPlayerInTurnAfterNewGame() {
        game.newGame();
        game.nextTurn(); // will throw [1,2] and thus black starts
        assertEquals(Color.BLACK, game.getPlayerInTurn());
    }
}