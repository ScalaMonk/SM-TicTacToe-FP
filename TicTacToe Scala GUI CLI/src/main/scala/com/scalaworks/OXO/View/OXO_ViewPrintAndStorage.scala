package com.scalaworks.OXO
package View

import java.io.File
import java.nio.file.Files

import scala.swing.Dialog.Message
import scala.swing.Dialog.Options
import scala.swing.Dialog.Result
import scala.swing.Dialog

import com.scalaworks.OXO.Control.OXOgame
import com.scalaworks.OXO.OXO_GUI

import javax.swing.filechooser.FileFilter
import javax.swing.JFileChooser
import oracle.help.htmlBrowser.ICEBrowser

object OXO_ViewPrint {
  val browser = new ICEBrowser
  def printAll(game: OXOgame) {
    println(
      <htlm>{ game }</htlm>)

    val browser = new ICEBrowser
    //val url = new URL(null,"info:C:/JVM Executables/ohj-11.1.2.0.0/demodoc/helpOnHelp/helpOnHelp.htm"
    //    ,new classpath.Handler(ClassLoader.getSystemClassLoader()))

    val url = OXO_GUI.resourceFromUserDirectory("OXOinf.htm")
    try {
      browser.printURL(url.toURI.toURL)
    } catch {
      case t: IllegalStateException => {}
      case t                        => t.printStackTrace() // todo: handle error
    }
  }

  def printAllPreview(game: OXOgame) {
    println(game)

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

  def saveFile()  {

    val chooser = new FileChooserEx() {
      setSelectedFile(new File("OXO Game Report" + "." + "html"))
      setFileFilter(new FileNameExtensionFilter)
      setAcceptAllFileFilterUsed(false)
    }

    chooser.setDialogTitle("Save As...")
    if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
      val choosenFile = chooser.getSelectedFile

      val pw = new java.io.PrintWriter(
        new java.io.File(choosenFile.toURI))
      pw.println("gsgggags")
      pw.close
    }
  }
}