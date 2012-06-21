package com.scalaworks.OXO
package View

import java.awt.event.KeyEvent

import scala.swing.Swing.EmptyIcon
import scala.swing.event.Key
import scala.swing.AbstractButton
import scala.swing.Action
import scala.swing.ButtonGroup
import scala.swing.CheckMenuItem
import scala.swing.Menu
import scala.swing.MenuBar
import scala.swing.MenuItem
import scala.swing.RadioMenuItem
import scala.swing.Separator

import com.scalaworks.OXO.Model.OXOplayers
import com.scalaworks.OXO.OXO_GUI.RESOURCEPATH
import com.scalaworks.OXO.View.OXO_View.t
import com.scalaworks.OXO.OXO_GUI

import javax.swing.ImageIcon
import javax.swing.KeyStroke

/////////////////////////////////////////////////////////////////////////////
// Menu building
//

object OXO_ViewMenu {
  private val AMPERSAND = '&'

  private def mutateTextNmeIcon(
    pComp: AbstractButton,
    pActionTitleResourceText: String,
    pActionBlock: => Unit = {},
    pAccelerator: Option[javax.swing.KeyStroke] = None,
    pIcon: javax.swing.Icon = EmptyIcon) =
    {
      var ampFlag = false
      var mne: Option[Char] = None

      // The ampersand filter evaluator
      def sifter(c: Char): Boolean = {
        val ret = (c != AMPERSAND) || ampFlag
        if (ampFlag) {
          if (c != AMPERSAND) mne = Some(c)
          ampFlag = false
        } else ampFlag = (c == AMPERSAND)
        ret
      }

      // The ampersand parser
      pComp.action = Action(t(pActionTitleResourceText).filter(sifter)) { pActionBlock }
      // Mutate component
      if (!mne.isEmpty) {
        pComp.mnemonic = Key.withName((mne.get).toUpper.toString)
      }
      pComp.icon = pIcon
      pComp.action.accelerator = pAccelerator
    }

  private def menuItemFactory(
    pActionTitleResourceText: String,
    pActionBlock: => Unit,
    pAccelerator: Option[javax.swing.KeyStroke] = None,
    pIcon: javax.swing.Icon = EmptyIcon): MenuItem =
    {
      val comp = new MenuItem("")
      mutateTextNmeIcon(comp, pActionTitleResourceText, pActionBlock, pAccelerator, pIcon)
      comp
    }

  private def menuFactory(
    pActionTitleResourceText: String,
    pIcon: javax.swing.Icon = EmptyIcon,
    pAccelerator: Option[javax.swing.KeyStroke] = None): Menu =
    {
      val comp = new Menu("")
      mutateTextNmeIcon(comp, pActionTitleResourceText, {}, pAccelerator, pIcon)
      comp
    }

  private def radioMenuItemFactory(
    pActionTitleResourceText: String,
    pActionBlock: => Unit,
    pAccelerator: Option[javax.swing.KeyStroke] = None,
    pSelected: Boolean = false,
    pIcon: javax.swing.Icon = EmptyIcon): RadioMenuItem =
    {
      val comp = new RadioMenuItem("")
      mutateTextNmeIcon(comp, pActionTitleResourceText, pActionBlock, pAccelerator, pIcon)
      comp.selected = pSelected
      comp
    }

  private def checkMenuItemFactory(
    pActionTitleResourceText: String,
    pActionBlock: => Unit,
    pAccelerator: Option[javax.swing.KeyStroke] = None,
    pSelected: Boolean = false,
    pIcon: javax.swing.Icon = EmptyIcon): CheckMenuItem =
    {
      val comp = new CheckMenuItem("")
      mutateTextNmeIcon(comp, pActionTitleResourceText, pActionBlock, pAccelerator, pIcon)
      comp.selected = pSelected
      comp
    }

  private val mnuSaveItem = {
    menuItemFactory("mnuSaveItem.text", { OXO_FileStorage.saveFile }, Some(KeyStroke.getKeyStroke(KeyEvent.VK_S, OXO_GUI.shortcutKeyMask)), EmptyIcon)
  }

  private val mnuSaveAsItem = {
    menuItemFactory("mnuSaveAsItem.text", {}, None, EmptyIcon)
  }

  private val mnuStartXItem = {
    menuFactory("startXMenuItem.text", OXOplayers.X.buttonIcon)
  }

  private val mnuStartOItem = {
    menuFactory("startOMenuItem.text", OXOplayers.O.buttonIcon)
  }

  private val mnuPrintItem = {
    menuItemFactory("printMenuItem.text",
      { OXO_ViewPrint.printAll(OXO_View.game) },
      Some(KeyStroke.getKeyStroke(KeyEvent.VK_P, OXO_GUI.shortcutKeyMask)))
  }

  private val mnuPrintPreview = {
    menuItemFactory("printPreviewMenuItem.text",
      { println(OXO_View.game) })
  }

  private val mnuClearBoardItem =
    menuItemFactory("clearBoardMenuItem.text", { OXO_View.clearBoard() })

  private val mnuAudioSwitchItem = menuItemFactory(
    "audioOffMenuItem.text",
    {
      // audioOn = selected
    },
    Some(KeyStroke.getKeyStroke(KeyEvent.VK_A, OXO_GUI.shortcutKeyMask)))

  private val mnuResetGameItem = menuItemFactory(
    "resetGameMenuItem.text",
    {
      OXO_View.clearBoard()
      OXO_View.lblStatusField.text = t("lblGameReset.text")
      OXOplayers.X.score = 0
      OXOplayers.O.score = 0
      OXOplayers.FREE.score = 0
    })

  private val mnuUndoItem = menuItemFactory(
    "undoMenuItem.text",
    {
      OXO_View.buttonsSeq(OXO_View.game.undoMove).icon = EmptyIcon
      updateGUI
    },
    Some(KeyStroke.getKeyStroke(KeyEvent.VK_Z, OXO_GUI.shortcutKeyMask)),
    new ImageIcon(getClass.getResource(('/' + RESOURCEPATH + "images/px16-edit-undo.png"))))

  private val mnuRedoItem = menuItemFactory(
    "redoMenuItem.text",
    {
      val redo = OXO_View.game.redoMove
      OXO_View.buttonsSeq(redo._1).icon = redo._2.avatar // Restore visual board
      updateGUI
    },
    Some(KeyStroke.getKeyStroke(KeyEvent.VK_Y, OXO_GUI.shortcutKeyMask)),
    new ImageIcon(getClass.getResource(('/' + RESOURCEPATH + "images/px16-edit-redo.png"))))

  private val mnuComputerPlay = radioMenuItemFactory("CompVersusUserMenuItem.text",
    { OXO_View.rivalOn = true; OXO_View.audioable('/' + RESOURCEPATH + "audio/computer.au") })

  def updateGUI: Int = {
    val moveCounter = OXO_View.game.movecounter
    val ind = OXO_View.game.getUpdatedFieldAt(moveCounter)
    if (ind._1 >= 0) OXO_View.buttonsSeq(ind._1).icon = ind._2.avatar

    val isMvCntrNatural = moveCounter > 0
    val endOfGame = moveCounter == OXO_View.game.getNsquares
    val isStart = moveCounter == 0 || endOfGame

    mnuUndoItem.enabled = isMvCntrNatural && !endOfGame
    mnuRedoItem.enabled = moveCounter != OXO_View.game.maxMovecounter
    mnuResetGameItem.enabled = isMvCntrNatural || (OXOplayers.X.score > 0) || (OXOplayers.O.score > 0)
    mnuClearBoardItem.enabled = isMvCntrNatural
    mnuStartXItem.enabled = isStart
    mnuStartOItem.enabled = isStart

    mnuComputerPlay.selected = OXO_View.rivalOn

    mnuPrintItem.enabled = moveCounter == OXO_View.game.getNsquares
    mnuPrintPreview.enabled = moveCounter == OXO_View.game.getNsquares

    val updatedProgBar =
      if (endOfGame)
        (OXO_View.game.maxMovecounter, "Game ended")
      else (moveCounter, t("TurnYofX").format(moveCounter, OXO_View.game.getNsquares))

    OXO_View.progressBar.value = updatedProgBar._1
    OXO_View.progressBar.label = updatedProgBar._2

    ind._1 /*Fieldindex updated*/
  } // updateGUI

  def menuBar = new MenuBar {
    // File menu

    contents += new Menu("") {
      mutateTextNmeIcon(this, "fileMenu.text")

      contents.append(mnuSaveItem, mnuSaveAsItem, new Separator, OXO_ViewMenu.menuItemFactory(
        "pageSetupMenuItem.text",
        {}, None), mnuPrintPreview, mnuPrintItem,
        new Separator, OXO_ViewMenu.mnuResetGameItem,
        OXO_ViewMenu.menuItemFactory(
          "exitMenuItem.text",
          { sys.exit }, None,
          new ImageIcon(getClass.getResource(('/' + RESOURCEPATH + "images/px-16gnome_application_exit.png")))))
    }

    // Edit menu
    contents += new Menu("") {
      mutateTextNmeIcon(this, "editMenu.text")
      contents.append(OXO_ViewMenu.mnuUndoItem, OXO_ViewMenu.mnuRedoItem, OXO_ViewMenu.mnuClearBoardItem)
    }

    // Game menu
    contents += new Menu("") {
      mutateTextNmeIcon(this, "gameMenu.text")

      mnuStartXItem.contents.append(
        menuItemFactory(
          "asComputerStartsMenuItem.text",
          {
            OXO_View.rivalOn = true
            OXO_View.game.doMove(OXO_View.game.compete)
            updateGUI
          }),
        menuItemFactory(
          "asPlayerBeginMenuItem.text",
          {
            OXO_View.rivalOn = true
            OXO_View.lblStatusField.text = t("youBegins.text")
          }))

      mnuStartOItem.contents.append(
        OXO_ViewMenu.menuItemFactory(
          "asComputerStartsMenuItem.text",
          {
            OXO_View.game.userStarted_(false)
            OXO_View.rivalOn = true
            OXO_View.game.doMove(OXO_View.game.compete)
            updateGUI
          }),
        menuItemFactory(
          "asPlayerBeginMenuItem.text",
          {
            OXO_View.game.userStarted_(false)
            OXO_View.rivalOn = true
          }))

      contents.append(OXO_ViewMenu.mnuStartXItem, OXO_ViewMenu.mnuStartOItem, new Separator)
      contents ++= (
        new ButtonGroup(
          mnuComputerPlay,
          radioMenuItemFactory("2PlayersMenuItem.text", { OXO_View.rivalOn = false }, None, true))).buttons

      contents.append(new Separator, OXO_ViewMenu.mnuResetGameItem)
    }

    // View menu
    contents += new Menu("") {
      mutateTextNmeIcon(this, "viewMenu.text")

      val mnuHints = new CheckMenuItem("")
      mutateTextNmeIcon(mnuHints,
        "hintsMenuItem.text",
        { OXO_View.hintsOn = mnuHints.selected; OXO_View.displayHint },
        Some(KeyStroke.getKeyStroke(KeyEvent.VK_D, OXO_GUI.shortcutKeyMask)))
      contents += mnuHints

      contents += OXO_ViewMenu.menuItemFactory(
        "scoreMenuItem.text",
        { OXO_View.lblStatusField.text = OXO_View.game.formattedScore },
        Some(KeyStroke.getKeyStroke(KeyEvent.VK_I, OXO_GUI.shortcutKeyMask)))
      contents += OXO_ViewMenu.menuItemFactory(
        "squareIdMenuItem.text",
        { OXO_View.lblStatusField.text = OXO_View.game.formattedScore },
        Some(KeyStroke.getKeyStroke(KeyEvent.VK_N, OXO_GUI.shortcutKeyMask)))
      contents += OXO_ViewMenu.mnuAudioSwitchItem
    }

    // Window menu
    contents += new Menu("") {
      mutateTextNmeIcon(this, "windowMenu.text")
    }

    // Help Menu
    contents += new Menu("") {
      mutateTextNmeIcon(this, "helpMenu.text")

      contents += OXO_ViewMenu.menuItemFactory(
        "showHelpBox.Action.text",
        OXO_ViewHelp.showHelp,
        Some(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0)))

      contents.append(new Separator,
        new MenuItem(Action(t("showAboutBox.Action.text")) {
          new OXOAboutBox()
        }) { mnemonic = Key.O })
    }
    updateGUI
  } // def menuBar
} // object OXO_ViewMenu

