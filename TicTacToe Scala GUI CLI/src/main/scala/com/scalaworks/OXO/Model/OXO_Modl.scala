package com.scalaworks.OXO
package Model

import scala.swing.Swing.EmptyIcon
import scala.swing.Swing.Icon

import com.scalaworks.OXO.Control.OXOgame
import com.scalaworks.OXO.OXO_GUI.RESOURCEPATH
import com.scalaworks.OXO.OXO_GUI.getIcon

import OXOboard.boardside
import OXOboard.boardsideRange
import OXOboard.linearIndexRange
import OXOboard.DEF_BOARDSIDE

/** @author FransAdm
 *  (M) OXOPlayers contains field value, player data, avatars and scores
 */
object OXOplayers extends Enumeration {
  //type OXOPlayers = PlayersIcons

  def getIcon0(pPath: String) = {
    try Icon(getClass.getResource(pPath))
    catch {
      case _ =>
        System.err.printf("OXO: Couldn't load image: %s\n", pPath)
        EmptyIcon
    }
  }

  class PlayersIcons(
      name: String,
      val avatar: javax.swing.Icon = EmptyIcon,
      val buttonIcon: javax.swing.Icon = EmptyIcon,
      i: Byte = nextId.toByte) extends Val {
    var score = 0

    def this(i: Byte) { this("", EmptyIcon, EmptyIcon, i) }

    def incScore { score += 1 }
  }

  val FREE: PlayersIcons = new PlayersIcons(-1.toByte) // Unmarked and unvisited
  val X: PlayersIcons = new PlayersIcons("X", getIcon('/' + RESOURCEPATH + "images/px40-cross.gif"), getIcon('/' + RESOURCEPATH + "images/px16-cross.gif")) // Player marker X
  val O: PlayersIcons = new PlayersIcons("O", getIcon('/' + RESOURCEPATH + "images/px40-not.gif"), getIcon('/' + RESOURCEPATH + "images/px16-not.gif")) // Player marker O  
} // object OXOPlayers

protected[this] case class Cell(val player: OXOplayers.PlayersIcons = OXOplayers.FREE,
                                val nVisited: Int = 0,
                                val oldFieldValue: OXOplayers.PlayersIcons = OXOplayers.FREE)

object OXOboard {
  protected val DEF_BOARDSIDE = 3
  // boardside is mutable and is protected by handmade read-only
  var boardside = 0
  def boardSide = boardside

  // The following values will be initialized AFTER instancization of OXOboard
  lazy val nSquares = boardside * boardside

  lazy val linearIndexRange = 0 until nSquares
  private lazy val boardsideRange = 0 until boardside
  private lazy val diagonalNO_ZW = for (i <- boardsideRange) yield (i + (boardside - 1 - i) * boardside)
  private lazy val diagonalNW_ZO = for (i <- boardsideRange) yield (i + i * boardside)
}
/** (M) OXOboard is the virtual board where the game is played in.
 *
 *  This grid is stressed to a linear array of fields starting to count in the left upper corner
 *  zigzag to the right lower corner.
 *
 *  The cells contains a 3-state value; free, X or O.
 *
 *  OXOboard is part of the MODEL.
 */
// Primary constructor with default argument: an initialized board
class OXOboard(
    private val pBoard: Array[Cell] = Array.fill(DEF_BOARDSIDE * DEF_BOARDSIDE)(new Cell),
    private val pBoardside: Int = DEF_BOARDSIDE) {
  OXOboard.boardside = pBoardside

  def this(pSize: Int) { // Auxiliary constructor
    this(Array.fill(pSize * pSize)(new Cell), pSize)
  }

  /** Copy constructor
   */
  private def copy = {
    new OXOboard(pBoard.clone(), boardside)
  }

  def getUpdatedFieldAt(at: Int) = {
    val index = pBoard.indexWhere(_.nVisited == at)
    (index, if (index >= 0) pBoard(index).player else OXOplayers.FREE)
  }

  def lastFreeField = {
    pBoard.indexWhere(_.player == OXOplayers.FREE)
  }

  def minimax(pActualPlayer: OXOplayers.PlayersIcons, pActualOpponent: OXOplayers.PlayersIcons) = {
    def getFreeList = for (i <- linearIndexRange if pBoard(i).player == OXOplayers.FREE) yield i

    def analyzeLines(pLinIndex: Int): (Int, Int) = {
      var cumX1, cumO1 = 0

      def evaluateWinLine(pWinLine: IndexedSeq[Int]) {

        def valuing(numberPlayerinLine: Int, numberOpponentinLine: Int) = {
          if (numberOpponentinLine == 0) (2 << numberPlayerinLine) - 1 else 0
        }

        val winline = for (sq <- pWinLine) yield pBoard(sq).player
        val numberXinLine = winline.filter(x => x == pActualPlayer).length
        val numberOinLine = winline.filter(o => o == pActualOpponent).length

        cumX1 += valuing(numberXinLine, numberOinLine)
        cumO1 += valuing(numberOinLine, numberXinLine)
      } // evaluateWinLine

      // Checks each Win Line, diagonals and a horizontal
      // and a vertical by generating coordinates of winlines

      // Check NW ZO diagonal 
      evaluateWinLine(OXOboard.diagonalNW_ZO)
      // Check NO_ZW diagonal 
      evaluateWinLine(OXOboard.diagonalNO_ZW)
      // Check  3 rows
      for (y <- OXOboard.boardsideRange)
        evaluateWinLine(for (x <- OXOboard.boardsideRange) yield y * boardside + x)
      // Check 3 columns
      for (x <- OXOboard.boardsideRange)
        evaluateWinLine(for (y <- OXOboard.boardsideRange) yield (y * boardside + x))
      (pLinIndex, cumX1 - cumO1)
    } // def analyzeLines(pLinIndex: Int)

    // compete starts here
    (for (freeSq <- getFreeList) yield {
      setSquareDirect(freeSq, pActualPlayer) // Set testmarker
      val yie = analyzeLines(freeSq: Int) // Evaluate with testmarker
      setSquareDirect(freeSq, OXOplayers.FREE) // Erase testmarker, place backup
      yie
    }).sortBy(_._2).reverse.head._1
  } // minimax

  private def setSquareDirectWithHistory(pLinearIndex: Int, pContent: OXOplayers.PlayersIcons, turn: Int) {
    pBoard(pLinearIndex) = new Cell(pContent, turn, pContent)
  }

  private def setSquareDirect(pLinearIndex: Int, pContent: OXOplayers.PlayersIcons) {
    pBoard(pLinearIndex) = new Cell(pContent, pBoard(pLinearIndex).nVisited, pBoard(pLinearIndex).oldFieldValue)
  }

  // Set cell content by a linear address. Checks for free and valid squares.
  // Throws IllegalArgumentException
  def validateAndsetSquare(pLinearIndex: Int, pGame: OXOgame, pPlayer: OXOplayers.PlayersIcons) {
    if (!OXOboard.linearIndexRange.contains(pLinearIndex))
      throw new IllegalArgumentException(OXOgame.t("invalidPick.offSite.text").format(pLinearIndex + 1))

    if ((pBoard(pLinearIndex).player != OXOplayers.FREE))
      throw new IllegalArgumentException(OXOgame.t("invalidPick.occupied.text").format(pLinearIndex + 1))
    pGame.moveMade
    setSquareDirectWithHistory(pLinearIndex, pPlayer, pGame.movecounter)
  }

  def isWinner(pLinIndex: Int, pPlayer: OXOplayers.PlayersIcons): Boolean =
    { // Check every square indicated by a list of square coordinates "winLine"
      // The checks are ANDed
      def isWinningLine(pWinLine: IndexedSeq[Int]) = (true /: pWinLine)(_ && pBoard(_).player == pPlayer)

      // Checks each Win Line, diagonals and a horizontal
      // and a vertical by generating coordinates of winlines

      // Check NW ZO diagonal 
      ((isWinningLine(OXOboard.diagonalNW_ZO))
        // Check NO_ZW diagonal 
        || (isWinningLine(OXOboard.diagonalNO_ZW))
        // Check row
        || (isWinningLine(
          for (i <- OXOboard.boardsideRange)
            yield (pLinIndex / boardside) * boardside + i))
          // Check column
          || isWinningLine(
            for (i <- OXOboard.boardsideRange)
              yield (i * boardside + pLinIndex % OXOboard.boardside)))
    }

  def undoMove(game: OXOgame): Int =
    {
      val index = pBoard.indexWhere(_.nVisited == game.movecounter)
      setSquareDirect(index, OXOplayers.FREE)
      game.decCounter
      index
    }

  def redoMove(game: OXOgame): (Int, OXOplayers.PlayersIcons) =
    {
      game.incCounter
      val index = pBoard.indexWhere(_.nVisited == game.movecounter)
      val oldFieldValue = pBoard(index).oldFieldValue
      setSquareDirect(index, oldFieldValue)
      (index, oldFieldValue)
    }

  def replay(game: OXOgame) = {
    for (index <- linearIndexRange) setSquareDirect(index, OXOplayers.FREE)
    game.movecounter = 0
    for (n <- 1 to game.maxMovecounter) yield {
      val move = redoMove(game)      
        (move._1, move._2 , minimax(game.whoIsInTurn, game.whoWasInTurn))     
    }
  }

  override def toString =
    {
      val builder = new StringBuilder("Game board:\n")

      for (i0 <- boardsideRange)
        builder.append((for (i1 <- boardsideRange)
          yield (if (pBoard(i0 * boardside + i1).player == OXOplayers.FREE) (i0 * boardside + i1 + 1) % 10 else pBoard(i0 * boardside + i1).player))
          .mkString("[", "][", "]") + "\n")
      builder.append('\n')
      builder.toString()
    }
} // class OXOboard
