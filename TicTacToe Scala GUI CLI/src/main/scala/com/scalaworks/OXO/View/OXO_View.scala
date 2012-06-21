package com.scalaworks.OXO
package View

import java.awt.Color

import scala.swing.Swing.EmptyIcon
import scala.swing.Dimension
import scala.swing.Alignment
import scala.swing.BoxPanel
import scala.swing.Button
import scala.swing.Component
import scala.swing.Dialog
import scala.swing.GridPanel
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.ProgressBar
import scala.swing.Swing
import scala.swing.event

import com.scalaworks.OXO.Control.OXOgame
import com.scalaworks.OXO.Model.OXOboard
import com.scalaworks.OXO.Model.OXOplayers
import com.scalaworks.OXO.OXO_GUI.RESOURCEPATH
import com.scalaworks.OXO.OXO_GUI

import javax.swing.JToolBar
import sun.applet.AppletAudioClip

/** @author FransAdm
 *
 */

object OXO_View {
  private val palet = Array(
    Color.ORANGE,
    Color.YELLOW,
    Color.RED,
    Color.LIGHT_GRAY,
    Color.MAGENTA,
    Color.BLUE,
    Color.GRAY,
    Color.BLACK,
    Color.CYAN,
    Color.WHITE,
    Color.DARK_GRAY,
    Color.GREEN,
    Color.PINK)

  private val dim = new Dimension(70, 70)
  private val borderline = Swing.LineBorder(Color.BLACK)

  var game = new OXOgame(OXO_GUI.boardside)

  var rivalOn = false
  var hintsOn = false
  private var audioOn = true

  private val resourceMap = java.util.ResourceBundle.getBundle(getClass.getPackage.getName + "/resources/OXO_View", OXO_GUI.locale)
  def t(key: String) =
    try {
      resourceMap.getString(key)
    } catch {
      case _ => key
    }

  val progressBar = new ProgressBar() {
    max = game.getNsquares
    labelPainted = true
  }

  object lblStatusField extends Label {
    text = t("statusMessageLabel.text")
    horizontalAlignment = Alignment.Left
  }

  private object lblSoundStatus extends Label {
    preferredSize = new Dimension(16, 16)
    icon = OXOplayers.X.buttonIcon
  }

  def audioable(resource: String) {
    if (audioOn)
      try {
        new AppletAudioClip(getClass.getResource(resource)).play
      } catch {
        case ex =>
          System.err.println("Audio switch off due: " + ex)
          audioOn = false
      }
  }

  def displayHint { lblStatusField.text = game.conclusionAfterTurn }

  /////////////////////////////////////////////////////////////////////////////
  // Visual grid and grid event
  //

  private def doMove(pSquare: Int) {
    def gameOverDialog(message: String) = {
      OXOplayers.FREE.incScore
      //game.setEndOfGame
      Dialog.showMessage(buttonsSeq(pSquare),
        "%s\n%s\nGame over ?".format(message, game.formattedScore),
        "Scala Tic Tac Toe results")
    }

    // doMove starts here
    if (game.isLastTurn) clearBoard(); audioable('/' + RESOURCEPATH + "audio/return.au")
    try {
      var thereIsAwinner = game.doMove(pSquare) // throwable
      if (!thereIsAwinner && rivalOn && !game.isLastTurn) { // Make countermove
        OXO_ViewMenu.updateGUI
        thereIsAwinner = game.doMove(game.compete) // throwable
      }
      OXO_ViewMenu.updateGUI
      if (hintsOn) displayHint
      if (!thereIsAwinner && game.isLastTurnWithPreEmpty) thereIsAwinner = game.isWinner(OXO_ViewMenu.updateGUI)
      if (thereIsAwinner) {
        audioable('/' + RESOURCEPATH + "audio/yahoo1.au")
        game.whoWasInTurn.incScore
        gameOverDialog("Winner is " + game.whoWasInTurn)
        game.setEndOfGame
      } else if (game.isLastTurn) {
        audioable('/' + RESOURCEPATH + "audio/yahoo2.au")
        gameOverDialog("No winner")
      } else audioable('/' + RESOURCEPATH + "audio/ding.au")
    } catch {
      case ex: IllegalArgumentException =>
        audioable('/' + RESOURCEPATH + "audio/beep.au")
        System.err.println(ex.getMessage())
    }
    if (game.isLastTurn) OXO_ViewMenu.updateGUI
    game.conclusionAfterTurn
  } // doMove(pSquare: Int)

  val buttonsSeq = OXOboard.linearIndexRange map (
    n => new Button {
      //contentAreaFilled = true
      background = palet(n % 13)
      preferredSize = dim
      listenTo(mouse.clicks)
      reactions += { case me: event.MouseClicked => doMove(n) }
    })

  def clearBoard() {
    buttonsSeq.foreach(_.icon = EmptyIcon)
    game = new OXOgame(OXO_GUI.boardside)
    OXO_ViewMenu.updateGUI
    lblStatusField.text = t("lblGridCleared.text")
  }

  def UI(opt: Option[Component] = None) = new BoxPanel(Orientation.Vertical) {

    class ToolBar(title: String) extends Component /*with SequentialContainer.Wrapper*/ {

      override lazy val peer: JToolBar = new JToolBar(title)
      //      def add(action: Action) { peer.add(action.peer) }
      def add(component: Component) { peer.add(component.peer) }
    } // class ToolBar

    private def toolBar(): ToolBar = new ToolBar("Settings") {

      add(new Button() {
        //background = colors(n)
        //border = border
        preferredSize = new Dimension(20, 20)
        icon = OXOplayers.X.avatar
        listenTo(mouse.clicks)
        reactions += {
          case me: event.MouseClicked => ()
        }
      })
    }

    private def mainPanel: GridPanel = {
      new GridPanel(OXOboard.boardSide, OXOboard.boardSide) {
        contents ++= buttonsSeq
      }
    } // def mainPanel 

    private val statusBar = new GridPanel(1, 2) {
      contents.append(lblStatusField, progressBar)
    }

    private val soundStatusBar = new GridPanel(1, 1) {
      contents.append(lblSoundStatus)
    }

    // Start of UI view
    if (!opt.isEmpty) contents += opt.get
    contents.append(mainPanel, statusBar, soundStatusBar)
  } // private def UI()

} // object OXO_View
