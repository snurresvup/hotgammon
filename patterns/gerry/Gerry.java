/**
 * This interface is an AI player for backgammon that can
 * play <strong>Black</strong> only!
 * <p/>
 * <p/>
 * Original evaluation function:
 * <pre>
 * Provided as a public service to the backgammon
 * programming community by Gerry Tesauro, IBM Research.
 * (e-mail: tesauro@watson.ibm.com)
 * </pre>
 * <p/>
 * Move generator and C to Java conversion by Henrik B Christensen
 * <p/>
 * <p/>
 * If you want to monitor the moves considered by Gerry while it
 * evaluate, the MoveHook class provides an interface for doing so.
 *
 * @author Gerry Tesauro, IBM Research (e-mail: tesauro@watson.ibm.com)
 *         and Henrik B Christensen.
 * @version 1.1 Gerry did not consider moves during bear off where the
 *          die number was higher than the highest black point containing
 *          black men.
 *          <p/>
 *          This source code is from the book
 *          "Flexible, Reliable Software:
 *          Using Patterns and Agile Development"
 *          published 2010 by CRC Press.
 *          Author:
 *          Henrik B Christensen
 *          Computer Science Department
 *          Aarhus University
 *          <p/>
 *          This source code is provided WITHOUT ANY WARRANTY either
 *          expressed or implied. You may study, use, modify, and
 *          distribute it for non-commercial purposes. For any
 *          commercial use, see http://www.baerbak.com/
 */

public class Gerry {

    float wr[], wc[], x[];

    private static final int BOARDSIZE = 28;
    private static final int BEAR_OFF = 26;
    int[] theboard;
    int[] dice;
    float maxscore;
    Move bestmove;

    /**
     * Construct an AI player
     */
    public Gerry() {
        rdwts();
        x = new float[122];
    }

    /**
     * calculate a best move for black given the
     * present game state.
     *
     * @param boardstate the state of the board, coded as an array of
     *                   28 integers: one for each of the 24 locations on the board, two
     *                   for red/black bar and two for red/black bear-off. The indeces
     *                   used are those outlined in the Location enum of HotGammon!
     *                   The value is 0 for no checker, negative for red (-2 = 2 red checkers),
     *                   and positive for black (+5 = 5 black checkers). This is also
     *                   the convention used in HotGammon's Color enum.
     * @param dicestate  the state of the dice rolled, coded as an
     *                   array of 2 integers, one for each die value.
     * @return the best move according to Gerry's evaluation function.
     */
    public Move play(int[] boardstate, int[] dicestate) {
        theboard = boardstate;
        setupDice(dicestate);

        // generate valid moves and evaluate each of them
        generateMoves();

        return bestmove;
    }

    /**
     * translate the game's dice state into a form suitable for
     * move generation
     */
    void setupDice(int[] ldice) {
        dice = new int[4];
        dice[0] = ldice[0];
        dice[1] = ldice[1];
        if (ldice[0] == ldice[1]) {
            dice[2] = dice[3] = ldice[0];
        } else {
            dice[2] = dice[3] = 0;
        }
    }

    /**
     * this hook allows clients to monitor the moves being considered by
     * the ai
     */
    public static class MoveHook {
        public void considerMove(Move move) {
        }
    }

    MoveHook movehook = new MoveHook();

    public void setNewMoveHook(MoveHook newHook) {
        movehook = newHook;
    }

    /**
     * generate all possible moves based upon the internal variables
     * that define the current board setup and dice thorw.  Forwards to
     * the recursive generation, evaluation and nomination of a best
     * move to make from the current board situation. After invocation
     * the instance variable 'bestmove' will contain the "best" move
     * sequence (from 0 to four moves).
     * POSTCONDITION: 'bestmove' is guarantied never
     * to be null after calling this method.
     */
    void generateMoves() {
        maxscore = -99999999.0f;
        bestmove = new Move();
        if (dice[2] == 0) {
            // only two moves possible
            recurse(theboard, dice, 0, new Move());
            // try the opposite dice combination afterwards
            int[] diceOpposite = new int[4];
            diceOpposite[0] = dice[1];
            diceOpposite[1] = dice[0];
            diceOpposite[2] = 0;
            recurse(theboard, diceOpposite, 0, new Move());
        } else {
            // a double has been rolled, 4 moves possible
            recurse(theboard, dice, 0, new Move());
        }
    }

    /**
     * recursive algorithm for generating the possible moves.  Does not
     * work for 'odd' move rules like chesh-besh rules.  As a complete
     * move sequence has been generated based upon the thrown dice, it
     * is evaluated (by calling 'maybeEnterMove') and is possibly voted
     * 'bestmove' by assigning the 'bestmove' instance.  Thus - this
     * method both generates, evaluates, and nominates a best move.
     *
     * @param board    the board on which to generate moves for, coded as
     *                 an 28 integer array.
     * @param dice     an array with 4 integers encoding the thrown
     *                 dice. If doubles all four contain the rolled value; if not
     *                 doubles index 0 and 1 contain the rolled die values and index 2
     *                 MUST be 0.
     * @param dieIndex the index for how 'far' into the
     *                 dice array this recursion has reaced. I.e. if we are generating
     *                 moves based upon the 2nd rolled dice, dieIndex==1. It is basically
     *                 the recursion depth.
     * @param move     set move under consideration so far. As the
     *                 recursion unfolds the move instance is populated with up to four
     *                 moves in case of doubles all the way down to 0 moves.
     */
    void recurse(int[] board, int[] dice, int dieIndex, Move move) {
        // Recursion termination rule:
        // Terminate if all rolled dice are used (4 or 2)
        if (dieIndex == 4 || dice[dieIndex] == 0) {
            maybeEnterMove(board, move);
            return;
        }

        // Recursion unfolding
        int i1, to;
        int die = dice[dieIndex];
        int moves_tried = 0;
        int high_p_b = calcHighestPointWithBlack(board);
        // sweep the board, i1 iterating all possible board locations
        for (i1 = 0; i1 <= 24; i1++) {
            // if a BLACK stone on the location
            if (board[i1] > 0) {
                // determine the location to move TO
                to = calcTo(i1, die, high_p_b);

                // check that the move is valid
                // ORG: if ( isValid(board,i1,to,dice,dieIndex) ) {
                boolean isValid;
                isValid = (to <= BEAR_OFF &&
                        hoserValidate(board, dice[dieIndex], i1, to));
                if (isValid) {
                    // value copy the board to avoid recursion level cross talk
                    int[] boardcopy = new int[BOARDSIZE];
                    System.arraycopy(board, 0, boardcopy, 0, BOARDSIZE);
                    // make the move on the copied board
                    makeMove(boardcopy, i1, to);

                    // similarly, copy the move and enter the current move
                    Move movecopy = move.getClone();
                    movecopy.add(i1, to);

                    // and recurse based upon this board state
                    recurse(boardcopy, dice, dieIndex + 1, movecopy);
                    moves_tried++;
                }
            }
        }

        // Additional termination rule
        if (moves_tried == 0) {
      /* we are now in a situation where moves_tried == 0 which
         means that no moves were found based upon two die rolls.
         However - if move contains a single valid move then
         this is the only possible move and according to the
         BG rule it must be made. We mark the move as a 
         'solitude' move which makes it possible to uphold
         the BG rule that if two moves are possible based upon
         the die rolls then the move using the highest roll MUST
         be taken.
       */
            if (move.noOfMoves() > 0) {
                move.markAsSolitude();
                maybeEnterMove(board, move);
            }
        }
    }

    /**
     * a possible move has been found resulting in a final
     * board state. Evaluate it and nominate it best move in
     * case the board state scores high
     *
     * @param board the board state as it looks after the given move
     * @param move  the move that leads to this board state
     */
    void maybeEnterMove(int[] board, Move move) {
        // call the hook to allow clients to see the move under consideration
        movehook.considerMove(move);

        // Perform the evaluation based upon Gerry's evaluation function
        float score = pubeval(isRacing(board), board);

        // solitude checking
        if (bestmove.isSolitude() && move.isSolitude()) {
            // both must be solitude moves - then the rule that
            // the move with the highest die roll MUST preceed.
            int bestdieroll = calcDie(bestmove.getFrom(0), bestmove.getTo(0));
            int newmoveroll = calcDie(move.getFrom(0), move.getTo(0));
            if (newmoveroll > bestdieroll) {
                bestmove = move.getClone();
                maxscore = score;
            }
        } else {
            if (score > maxscore) {
                bestmove = move.getClone();
                maxscore = score;
            }
        }
    }

    /**
     * make a move on a given board. Precondition: the move must be
     * cleared valid before this method executes.
     *
     * @param board the board to make the move on
     * @param from  the index of the location to move from
     * @param to    the index of the location to move to
     */
    void makeMove(int[] board, int from, int to) {
        // make sure to clear blots
        // blot hitting test
        if (board[to] == -1) {
            board[25]--;
            board[to] = 0; // move man to bar
        }
        board[from]--;
        board[to]++;
    }

    boolean isRacing() {
        return isRacing(theboard);
    }

    boolean isRacing(int[] board) {
        int me, you, i;
        for (i = 0; (i <= 24) && (board[i] <= 0); i++) ;    /* first occurance of me  */
        me = i;
        for (i = 25; (i >= 1) && (board[i] >= 0); i--) ;/* first occurance of you */
        you = i;
        return me > you;
    }

    /**
     * calculate the highest point (seen from black's perspective,
     * i.e. B6 > B5) that contains a black man.
     */
    int calcHighestPointWithBlack(int[] board) {
        int i;
        for (i = 0; (i <= 24) && (board[i] <= 0); i++) ;    /* first occurance of me  */
        return i;
    }


    /**
     * calculate the 'to' location based on given 'from' location and
     * die roll. Must take the odd location of black's move off
     * point into consideration
     *
     * @param from              the location to move from
     * @param highestBlackPoint the index of the highest point (seen
     *                          from black) that contains black man/men.
     * @return the index of the to location or 99 in case the move is
     *         invalid.
     */
    int calcTo(int from, int die, int highestBlackPoint) {
        int to = from + die;
        // now if highestBlackPoint is B6 or less then the game is in
        // the bearing off phase. If we are bearing off then if the
        // die is larger than highestBlackPoint (logically!) then
        // we must allow men on the lower points to be bourne off!
        // die is in range 1..6 but highestBlackPoint follows the
        // odd numbering B6 == 19 so we have to map highestBlackPoint.
        int mappedToBlacksPointOfView_HigestBlackPoint = 25 - highestBlackPoint;
        if (mappedToBlacksPointOfView_HigestBlackPoint < die) {
            to = BEAR_OFF;
        } else {
            // to >= 26 are illegal moves
            if (to >= BEAR_OFF) {
                to = 99;
            }
            // correct for moves to the bar: B1 is 24 but the black bear off
            // is #26. Thus die=1 on B1 will give 25 for bear off
            if (to == 25) {
                to = BEAR_OFF;
            }
        }
        return to;
    }

    /**
     * calculate the die roll that lead to the move from location
     * from -> to.
     * Precondition: to and from are valid location indices.
     */
    int calcDie(int from, int to) {
        int die = to - from;
        if (to == BEAR_OFF) {
            // B1 -> Bear off is die == 1. B1=24; Bear_off=26 =>
            // die == 2. Thus correct the error.
            die--;
        }
        return die;
    }


    /**
     * Use own validation function. Only works for black and only
     * for standard backgammon.
     */
    boolean hoserValidate(int[] board, int die,
                          int m1, int m2) {
        // This is a almost direct cut from Hoser backgammon, the
        // old Amiga code. Some bugs fixed though :)
        int sign, i, All_In, bar;

        All_In = -1;
        sign = +1;

        int p = die;
    
    /* what direction are we going? */
        if (board[m1] > 0) { // BLACK
            bar = 0;

            // filter off out of bounds move
            if (m2 > 26) return false;

            // cannot accept moving to red's bar.
            if (m2 == 25) return false;

      /* is there a man on the bar?? */
            if ((board[bar] != 0) && (m1 != 0)) return false;
      
      /* see if all pieces are in home base */
            for (i = 0; (i <= 24) && (All_In == -1); i++)
                if (board[i] > 0) All_In = i;
      
      /* check if we are bearing off. */
            if (m2 == 26) {

                int bearOffPointFromDie = 25 - p;
                int highestPointWithMen = All_In;

                boolean bearOffSituation = highestPointWithMen > 18;
                boolean menOnExactBearOffPoint = (m1 == bearOffPointFromDie);

                // NOTE: as black counts LOWER numbers as HIGHER in inner table
                // the natural > becomes a < instead!
                boolean dieRollLargerThanAnyPointOccupied =
                        (bearOffPointFromDie < highestPointWithMen);
                boolean pointPickedIsHigestOccupiedPoint =
                        (m1 == highestPointWithMen);

                boolean bearOffOK =
                        bearOffSituation &&
                                (menOnExactBearOffPoint ||
                                        (dieRollLargerThanAnyPointOccupied &&
                                                pointPickedIsHigestOccupiedPoint));
                return bearOffOK;
            }
        } else {
            throw new RuntimeException("not checking human");
        }
    
    /* does dice roll make sense for the move picked? */
        if ((m2 - m1) != (p * sign)) return false;
    
    /* if spike has more than 1 opponent */
        if (board[m2] * sign < -1) return false;
    
    /* I suppose that's all there is..*/
        return true;
    }


    // === Original Code - translated to Java by Henrik B Christensen

    /**
     * Gerry's original comment:
     * Backgammon move-selection evaluation function
     * for benchmark comparisons.  Computes a linear
     * evaluation function:  Score = W * X, where X is
     * an input vector encoding the board state (using
     * a raw encoding of the number of men at each location),
     * and W is a weight vector.  Separate weight vectors
     * are used for racing positions and contact positions.
     * Makes lots of obvious mistakes, but provides a
     * decent level of play for benchmarking purposes.
     */
    private float pubeval(boolean race, int[] pos) {
        int i;
        float score;

        if (pos[BEAR_OFF] == 15) return (99999999.F);
    /* all men off, best possible move */

        setx(pos); /* sets input array x[] */
        score = 0.0F;
        if (race) {  /* use race weights */
            for (i = 0; i < 122; ++i) score += wr[i] * x[i];
        } else {  /* use contact weights */
            for (i = 0; i < 122; ++i) score += wc[i] * x[i];
        }
        return (score);
    }


    private void setx(int[] pos) {
    /* sets input vector x[] given board position pos[] */
        int j, jm1, n;
    /* initialize */
        for (j = 0; j < 122; ++j) x[j] = 0.0F;
    
    /* first encode board locations 24-1 */
        for (j = 1; j <= 24; ++j) {
            jm1 = j - 1;
            n = pos[25 - j];
            if (n != 0) {
                if (n == -1) x[5 * jm1 + 0] = 1.0F;
                if (n == 1) x[5 * jm1 + 1] = 1.0F;
                if (n >= 2) x[5 * jm1 + 2] = 1.0F;
                if (n == 3) x[5 * jm1 + 3] = 1.0F;
                if (n >= 4) x[5 * jm1 + 4] = (float) (n - 3) / 2.0F;
            }
        }
    /* encode opponent barmen */
        x[120] = -(float) (pos[0]) / 2.0F;
    /* encode computer's menoff */
        x[121] = (float) (pos[BEAR_OFF]) / 15.0F;
    }


    /**
     * testing helper function
     */
    private static String boardAsString(int[] board) {
        String val = "Board: ";
        for (int i = 0; i < 28; i++) {
            val += "" + board[i] + " ";
        }
        return val;
    }

    /**
     * testing helper function
     */
    private static String diceAsString(int[] dice) {
        String val = "Dice: ";
        for (int i = 0; i < 4; i++) {
            val += "" + dice[i] + " ";
        }
        return val;
    }


    /**
     * read in the weight arrays
     */
    private void rdwts() {
        wc = new float[]{
                .25696F,
                -.66937F,
                -1.66135F,
                -2.02487F,
                -2.53398F,
                -.16092F,
                -1.11725F,
                -1.06654F,
                -.92830F,
                -1.99558F,
                -1.10388F,
                -.80802F,
                .09856F,
                -.62086F,
                -1.27999F,
                -.59220F,
                -.73667F,
                .89032F,
                -.38933F,
                -1.59847F,
                -1.50197F,
                -.60966F,
                1.56166F,
                -.47389F,
                -1.80390F,
                -.83425F,
                -.97741F,
                -1.41371F,
                .24500F,
                .10970F,
                -1.36476F,
                -1.05572F,
                1.15420F,
                .11069F,
                -.38319F,
                -.74816F,
                -.59244F,
                .81116F,
                -.39511F,
                .11424F,
                -.73169F,
                -.56074F,
                1.09792F,
                .15977F,
                .13786F,
                -1.18435F,
                -.43363F,
                1.06169F,
                -.21329F,
                .04798F,
                -.94373F,
                -.22982F,
                1.22737F,
                -.13099F,
                -.06295F,
                -.75882F,
                -.13658F,
                1.78389F,
                .30416F,
                .36797F,
                -.69851F,
                .13003F,
                1.23070F,
                .40868F,
                -.21081F,
                -.64073F,
                .31061F,
                1.59554F,
                .65718F,
                .25429F,
                -.80789F,
                .08240F,
                1.78964F,
                .54304F,
                .41174F,
                -1.06161F,
                .07851F,
                2.01451F,
                .49786F,
                .91936F,
                -.90750F,
                .05941F,
                1.83120F,
                .58722F,
                1.28777F,
                -.83711F,
                -.33248F,
                2.64983F,
                .52698F,
                .82132F,
                -.58897F,
                -1.18223F,
                3.35809F,
                .62017F,
                .57353F,
                -.07276F,
                -.36214F,
                4.37655F,
                .45481F,
                .21746F,
                .10504F,
                -.61977F,
                3.54001F,
                .04612F,
                -.18108F,
                .63211F,
                -.87046F,
                2.47673F,
                -.48016F,
                -1.27157F,
                .86505F,
                -1.11342F,
                1.24612F,
                -.82385F,
                -2.77082F,
                1.23606F,
                -1.59529F,
                .10438F,
                -1.30206F,
                -4.11520F,
                5.62596F,
                -2.75800F,
        };

        wr = new float[]{
                .00000F,
                -.17160F,
                .27010F,
                .29906F,
                -.08471F,
                .00000F,
                -1.40375F,
                -1.05121F,
                .07217F,
                -.01351F,
                .00000F,
                -1.29506F,
                -2.16183F,
                .13246F,
                -1.03508F,
                .00000F,
                -2.29847F,
                -2.34631F,
                .17253F,
                .08302F,
                .00000F,
                -1.27266F,
                -2.87401F,
                -.07456F,
                -.34240F,
                .00000F,
                -1.34640F,
                -2.46556F,
                -.13022F,
                -.01591F,
                .00000F,
                .27448F,
                .60015F,
                .48302F,
                .25236F,
                .00000F,
                .39521F,
                .68178F,
                .05281F,
                .09266F,
                .00000F,
                .24855F,
                -.06844F,
                -.37646F,
                .05685F,
                .00000F,
                .17405F,
                .00430F,
                .74427F,
                .00576F,
                .00000F,
                .12392F,
                .31202F,
                -.91035F,
                -.16270F,
                .00000F,
                .01418F,
                -.10839F,
                -.02781F,
                -.88035F,
                .00000F,
                1.07274F,
                2.00366F,
                1.16242F,
                .22520F,
                .00000F,
                .85631F,
                1.06349F,
                1.49549F,
                .18966F,
                .00000F,
                .37183F,
                -.50352F,
                -.14818F,
                .12039F,
                .00000F,
                .13681F,
                .13978F,
                1.11245F,
                -.12707F,
                .00000F,
                -.22082F,
                .20178F,
                -.06285F,
                -.52728F,
                .00000F,
                -.13597F,
                -.19412F,
                -.09308F,
                -1.26062F,
                .00000F,
                3.05454F,
                5.16874F,
                1.50680F,
                5.35000F,
                .00000F,
                2.19605F,
                3.85390F,
                .88296F,
                2.30052F,
                .00000F,
                .92321F,
                1.08744F,
                -.11696F,
                -.78560F,
                .00000F,
                -.09795F,
                -.83050F,
                -1.09167F,
                -4.94251F,
                .00000F,
                -1.00316F,
                -3.66465F,
                -2.56906F,
                -9.67677F,
                .00000F,
                -2.77982F,
                -7.26713F,
                -3.40177F,
                -12.32252F,
                .00000F,
                3.42040F,
        };
    }
}
