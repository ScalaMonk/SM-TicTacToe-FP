package com.scalaworks.OXO
package Control

import com.scalaworks.OXO.Main.printCon
import com.scalaworks.OXO.Main.printConView
import com.scalaworks.OXO.OXO_GUI.RESOURCEPATH
import com.scalaworks.OXO.Control.OXOgame.t
import com.scalaworks.OXO.OXO_GUI
import com.scalaworks.OXO.Model.OXOboard
import com.scalaworks.OXO.Model.OXOplayers
import java.io.EOFException

object OXOgame {
  private[this] val resourceMap =
    java.util.ResourceBundle.getBundle(getClass.getPackage.getName + "/resources/OXO_Ctrl", OXO_GUI.locale)
  def t(key: String) =
    try {
      resourceMap.getString(key)
    } catch {
      case _ => key
    }
}

/** (C) OXOgame is the container for the progression of the game
 */
class OXOgame(val pBoardside: Int = 3) {

  object GameResult {
    private var winner = OXOplayers.FREE
    def winner_(value: OXOplayers.PlayersIcons) { winner = value }

    override def toString = {
      if (winner == OXOplayers.FREE) t("gameOverDraw.text")
      else t("gameOverWinner.text").format(winner)
    }
  }

  private[this] var userStarted = true
  def userStarted_(fact: Boolean) { userStarted = fact }

  private[this] val board = new OXOboard(pBoardside)
  def getNsquares = OXOboard.nSquares
  private[this] val Last = getNsquares - 1

  var movecounter = 0
  //def moveCounter = movecounter

  private[this] var maxMoveCounter = 0
  def maxMovecounter = maxMoveCounter

  def incCounter { movecounter += 1 }

  def decCounter { movecounter -= 1 }

  def moveMade { incCounter; maxMoveCounter = movecounter }

  def setEndOfGame {
    movecounter = getNsquares
    /*maxMoveCounter = movecounter*/
  }

  def getUpdatedFieldAt(at: Int) = { board.getUpdatedFieldAt(at: Int) }

  def whoIsInTurn(implicit pTurn: Int = movecounter): OXOplayers.PlayersIcons =
    {
      if (((pTurn % 2) == 0) == userStarted) OXOplayers.X else OXOplayers.O
    }

  def whoWasInTurn: (OXOplayers.PlayersIcons) = whoIsInTurn(movecounter - 1)

  def compete: Int = board.minimax(whoIsInTurn, whoWasInTurn)

  def formattedScore = {
    t("formattedScore.text").format(
      OXOplayers.X.score,
      OXOplayers.O.score,
      OXOplayers.FREE.score)
  }

  private[this] def doTurn(pPlayer: OXOplayers.PlayersIcons, pSquareNum: Int): Boolean =
    {
      board.validateAndsetSquare(pSquareNum, this, pPlayer)
      board.isWinner(pSquareNum, pPlayer)
    }

  def doMove(pKeyId: Int) = {
    val isWinner = doTurn(whoIsInTurn, pKeyId)
    if (isWinner) GameResult.winner_(whoWasInTurn)
    printCon(board.toString)
    printCon(conclusionAfterTurn())
    isWinner
  }

  def undoMove: Int = { board.undoMove(this) }

  def redoMove: (Int, OXOplayers.PlayersIcons) = { board.redoMove(this) }

  def isWinner(pLinInd: Int) = { board.isWinner(pLinInd, whoIsInTurn) }

  def isLastTurn1: Boolean = {
    movecounter == OXOboard.nSquares
  }

  def isLastTurn2: Boolean = {
    movecounter match {
      //          case SecondLast => secondLastEvaluation()
      case Last =>
        val lastfield = board.lastFreeField
        printConView(t("lastFieldAutomagically").format(lastfield + 1, whoIsInTurn))
        board.validateAndsetSquare(lastfield, this, whoIsInTurn)
        true
      case OXOboard.nSquares => true
      case _                 => false
    }
  }

  /** The character based console line
   */
  @throws(classOf[EOFException])
  def play {
    var winner = OXOplayers.FREE
    do {
      printCon(board.toString)
      printCon(conclusionAfterTurn())
      printCon(t("its_Turn.text").format(whoIsInTurn()))
      try {
        val response = Console.readLine()

        if (response == null) throw new EOFException(t("eof.text"))
        if (doTurn(whoIsInTurn, response.toInt - 1)) { winner = whoWasInTurn; winner.incScore }
      } catch {
        case ex: NumberFormatException    => System.err.printf(t("invalidInputExcep.text"), ex.getMessage())
        case ex: IllegalArgumentException => System.err.printf(t("illArgExcep.text"), ex.getMessage())
      }
    } while (winner == OXOplayers.FREE && !isLastTurn2)
    OXOplayers.FREE.incScore

    printCon(board.toString)
    printCon(GameResult.toString)
  } // def play

  def conclusionAfterTurn() = {
    (if (isLastTurn1) t("noMorePos") else t("advice.text").format(compete + 1)) + '\n'
  }

  override def toString = {
    val buf = new StringBuilder

    buf.append(t("gameReportHeader.text"))
    buf.append(formattedScore)
    buf.append("Last game played\n")
    buf.append(GameResult)
    buf.append(board)

    println(board.replay(this))

    buf.toString

  }

  //  override def toString = t("gameExplanation.text").format(pBoardside)
} // class OXOgame