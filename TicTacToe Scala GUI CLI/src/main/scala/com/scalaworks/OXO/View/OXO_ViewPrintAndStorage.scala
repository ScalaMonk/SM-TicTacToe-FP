package com.scalaworks.OXO
package View

import java.io.File
import scala.swing.Dialog.Message
import scala.swing.Dialog.Options
import scala.swing.Dialog.Result
import scala.swing.Dialog
import com.scalaworks.OXO.Control.OXOgame
import com.scalaworks.OXO.OXO_GUI
import javax.swing.filechooser.FileFilter
import javax.swing.JFileChooser
import oracle.help.htmlBrowser.ICEBrowser
import scala.swing.BorderPanel
import scala.swing.GridBagPanel
import scala.swing.Label
import scala.swing.Swing.EmptyIcon
import scala.swing.Alignment
import scala.swing.Component
import java.net.URL

object OXO_ViewPrint {

  def gameToString(game: OXOgame) =
    {
      val data = List(List(1, 2, 3), List(4, 5, 6), List(7, 8, 9))
      <html>
        <head>
          <meta http-equiv="CONTENT-TYPE" content="text/html; charset=windows-1252"/>
          <title>Tic Tac</title>
          <style type="text/css" media="all">
            td {{
                    border-style: none; border-width: medium; border-color: inherit; padding: 0mm;
               }}
          </style>
        </head>
        <body>
          <table style="page-break-before: always;" cellpadding="2" cellspacing="0">
            <colgroup><col width="14"/> <col width="14"/> <col width="14"/> </colgroup>
            <tbody>
              <table>{
                data.map(row => <tr>{
                  row.map(col => <td>
                                   <p style="border: 1px solid ; padding: 0.5mm;"><TT>{ col }</TT></p>
                                 </td>)
                }</tr>)
              }</table>
            </tbody>
          </table>
        </body>
      </html>
    }

  class HTMLbrowser extends Component {
    override lazy val peer = new ICEBrowser
    def add(component: Component) { peer.add(component.peer) }
  } // class HTMLbrowser

  val browser = new HTMLbrowser
  val tempFilename = "OXOinf.htm"

  def printAll(game: OXOgame) {
    val temp = new java.io.File(tempFilename)
    val pw = new java.io.PrintWriter(temp)

    pw.print(gameToString(game))
    pw.close()
    println("Storing to: " + temp.getAbsolutePath)

    try {
      browser.peer.printURL(OXO_GUI.resourceFromUserDirectory(tempFilename).toURI.toURL)
    } catch {
      case t: IllegalStateException => {}
      case t                        => t.printStackTrace() // todo: handle error
    }
    if (!temp.delete()) {
      // wasn't deleted for some reason, delete on exit instead
      temp.deleteOnExit()
    }
  }

  def printAllPreview(game: OXOgame) {

    val temp = new java.io.File(tempFilename)
    val pw = new java.io.PrintWriter(temp)

    pw.print(gameToString(game))
    pw.close()

    println("Stored to: " + temp.getAbsolutePath)

    try {
      browser.peer.setURL(OXO_GUI.resourceFromUserDirectory(tempFilename).toURI.toURL)
    } catch {
      case t: IllegalStateException => { t.printStackTrace() }
      case t                        => t.printStackTrace() // todo: handle error
    }

    val dialog = new Dialog {
      contents = browser
      modal = true
      visible = true
    }
    if (!temp.delete()) {
      // wasn't deleted for some reason, delete on exit instead
      temp.deleteOnExit()
    }

  }

}

object OXO_FileStorage {

  class FileChooserEx extends JFileChooser {

    override def approveSelection() {
      val file = getSelectedFile
      if (file.exists()) {
        if (Dialog.showConfirmation(null,
          file + " exists. Overwrite?",
          "Save Warning",
          Options.YesNo,
          Message.Warning) == Result.Yes)
          super.approveSelection
      }
    }
  }

  def isAcceptable(f: File, ext: String*) = {
    if (f.isDirectory) { true }
    else {
      val s = f.getName()
      ext.foldLeft(false) { (bool, elem) => bool || s.endsWith(elem) }
    }
  }

  class FileNameExtensionFilter extends FileFilter {
    override def accept(file: File) = {
      file.isDirectory() || isAcceptable(file, ".html", ".htm")
    }
    def getDescription = "Tic Tac Toe Report (*.html)"
  }

  def saveFile() {

    val chooser = new FileChooserEx() {
      setSelectedFile(new File("OXO Game Report" + "." + "html"))
      setFileFilter(new FileNameExtensionFilter)
      setAcceptAllFileFilterUsed(false)
    }

    chooser.setDialogTitle("Save As...")
    if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
      val choosenFile = chooser.getSelectedFile
      val pw = new java.io.PrintWriter(new java.io.File(choosenFile.toURI))
      pw.println("gsgggags")
      pw.close
      println("Storing to: " + choosenFile.getAbsoluteFile)
    }
  }
}