package com.scalaworks
package OXO

import java.awt.Toolkit
import java.util.Locale

import scala.swing.Swing.EmptyIcon
import scala.swing.Swing.Icon
import scala.swing.Applet
import scala.swing.Reactor
import scala.swing.SimpleSwingApplication

import com.scalaworks.OXO.Control.OXOgame
import com.scalaworks.OXO.View.OXO_View
import com.scalaworks.OXO.View.OXO_ViewMenu

import javax.swing.UIManager
import javax.swing.UnsupportedLookAndFeelException

/** Object OXO_GUI has the entry point for use as a Java GUI (main).
 *
 *  Companion (private) class has the entry for use as an applet.
 *
 *  Private object Main has an entry point for CLI operation.
 */
object OXO_GUI extends SimpleSwingApplication {
  val shortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
  val RESOURCEPATH = "com/scalaworks/OXO/resources/"

  val locale = Locale.getDefault()
  val applicationResourceMap = java.util.ResourceBundle.getBundle(RESOURCEPATH + "OXO_Main", locale)

  def t(key: String) =
    try {
      applicationResourceMap.getString(key)
    } catch {
      case _ => key
    }

  var verbose = false
  var norun = false
  var boardside = 3

  def getIcon(pPath: String) = {
    try Icon(getClass.getResource(pPath))
    catch {
      case _ =>
        System.err.printf("OXO: Couldn't load image: %s\n", pPath)
        EmptyIcon
    }
  }

  override def main(args: Array[String]) = {
    ProcesCommandLineSwitches(args)
    super.main(args)
  }

  private def initLookAndFeel(pLaF: String) {
    // Specify the look and feel to use by defining the LOOKANDFEEL constant
    // Valid values are: null (use the default), "Metal", "System", "Motif",
    // and "GTK"
    // If you choose the Metal L&F, you can also choose a theme.
    // Specify the theme to use by defining the THEME constant
    // Valid values are: "DefaultMetal", "Ocean",  and "Test"
    // val THEME = "DefaultMetal"

    /*def lookUpAndFeel(plaf: String): String = plaf.toUpperCase match {
      case "ALLOY" => "com.incors.plaf.alloy.AlloyLookAndFeel"
      case "CROSS" => UIManager.getCrossPlatformLookAndFeelClassName()
      case "GTK"   => "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"
      case "MAC"   => "com.sun.java.swing.plaf.mac.MacLookAndFeel"
      case "MACOS" => "it.unitn.ing.swing.plaf.macos.MacOSLookAndFeel"
      case "METAL" => {
        //if (THEME.equals("DefaultMetal"))
        //  MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
        //else if (THEME.equals("Ocean"))
        //  MetalLookAndFeel.setCurrentTheme(new OceanTheme());
        "javax.swing.plaf.metal.MetalLookAndFeel"
      }
      case "MOTIF"             => "com.sun.java.swing.plaf.motif.MotifLookAndFeel"
      case "NIMBUS"            => "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"
      case "PAGOSOFT" | "PAGO" => "com.pagosoft.plaf.PgsLookAndFeel"
      case "QUAQUA"            => "ch.randelshofer.quaqua.QuaquaLookAndFeel"
      case "WINDOWS"           => "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"
      case "WINDOWS95" |
        "WINDOWS98" |
        "WINDOWSME" |
        "WINDOWSNT" |
        "WINDOWS2000" => "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel"
      case "SYNTH"  => "javax.swing.plaf.synth.SynthLookAndFeel"
      case "SYSTEM" => UIManager.getSystemLookAndFeelClassName()
      case /*DEFAULT*/ _ => {
        System.err.printf("No specific Application.lookAndFeel setting in properties file: %s\n", plaf)
        UIManager.getCrossPlatformLookAndFeelClassName()
      }
    } */

    var lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName()

    for (laf <- UIManager.getInstalledLookAndFeels()) {
      if (pLaF == laf.getName())
        lookAndFeel =laf.getClassName()
    }

    try UIManager.setLookAndFeel(lookAndFeel)
    catch {
      case ex: ClassNotFoundException => {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName())
        System.err.printf(t("lookAndFeelmessage1.error.text"), lookAndFeel);
        System.err.printf(t("lookAndFeelmessage2.error.text"))
      }
      case ex: UnsupportedLookAndFeelException => {
        System.err.printf(t("lookAndFeelmessage3.error.text"), lookAndFeel);
      }
      case ex: Exception => {
        System.err.printf(t("lookAndFeelmessage4.error.text"), lookAndFeel);
        ex.printStackTrace();
      }
    } finally Main.printCon(UIManager.getLookAndFeel().toString)
  }

  type OptionMap = Map[Symbol, Any]

  /** Recursively parse the arguments provided in remainingArguments
   *  adding them to the parsedArguments map and returning the
   *  completed map when done.
   */
  def nextOption(parsedArguments: OptionMap, remainingArguments: List[String]): OptionMap =
    {
      // Does a string look like it could be an option?
      def isOption(s: String) = s.startsWith("--")
      // Match the remaining arguments.
      remainingArguments match {
        // Nothing left so just return the parsed arguments
        case Nil => parsedArguments
        // Option defining the port to listen on. Use the value after the
        // --port option as the number and continue parsing with the
        // remainder of the list.
        case "-Dscala.time" :: tail =>
          nextOption(parsedArguments ++ Map('Dscala_time -> None), tail)
        case "--port" :: value :: tail =>
          nextOption(parsedArguments ++ Map('port -> value.toInt), tail)
        case "--side" :: value :: tail =>
          nextOption(parsedArguments ++ Map('side -> value.toInt), tail)
        // The data directory. This case matches if the directory comes
        // before the port option, the directory doesn't look like an
        // option (doesn't start with --) and the string after it
        // does. Here parsing needs to continue with tail of the
        // arguments provided to this call as the next
        // iteration must consider possibleOption.
        case dir :: possibleOption :: tail if !isOption(dir) && isOption(possibleOption) =>
          nextOption(parsedArguments ++ Map('dir -> dir), remainingArguments.tail)
        // Data directory. This matches the last element in the list if it
        // doesn't look like an option. As we know there is nothing
        // left in the list use Nil for the remainingArguments passed
        // to the next iteration.
        case dir :: Nil if !isOption(dir) =>
          nextOption(parsedArguments ++ Map('dir -> dir), Nil)
        // Nothing else matched so this must be an unknown option.
        case unknownOption :: tail =>
          sys.error("Unknown option " + unknownOption)
      }
    }

  def ProcesCommandLineSwitches(args: Array[String]) {
    try {
      val options = nextOption(Map(), args.toList)
      Main.printCon("OXO_Main: %s\n".format(options))
      if (options.contains('side)) boardside = options('side).toString().toInt
    } catch {
      case _: NumberFormatException => { System.err.println(t("commandParserNumberErr.error.text")) }
    }
  }

  def top =
    new scala.swing.MainFrame {
      initLookAndFeel(t("Application.lookAndFeel"))
      centerOnScreen

      title = t("mainFrame.title")
      iconImage = Icon(resourceFromClassloader('/' + RESOURCEPATH + "images/px24-cross.gif")).getImage()
      menuBar = OXO_ViewMenu.menuBar
      contents = OXO_View.UI(Some(OXO_View.toolBar))
    }
} // object OXO_GUI extends SimpleSwingApplication

/** Main entry for Command Line Interface
 */
private object Main extends App { // Application Object
  import com.scalaworks.OXO.OXO_GUI.t
  if (!OXO_GUI.norun) {
    OXO_GUI.ProcesCommandLineSwitches(args)
    Main.printCon(t("welcome.text"))

    try {
      do {
        val spel = new OXOgame(OXO_GUI.boardside)
        Main.printCon(spel.toString)
        spel.play
        Main.printCon(t("play.score.text").format(spel.formattedScore))
        Main.printCon(t("play.gameOver.text"))
      } while (Console.readLine().equalsIgnoreCase(t("play.Yes.character")))
      Main.printCon(t("play.gameEnded.text"))
    } catch {
      case ex =>
        System.err.println(t("play.fatalErr.text"))
        System.err.println(ex)
        //ex.printStackTrace()
        sys.exit(-1)
    } // catch
  }

  def printCon(s: String) { Console.out.println(s) }
  def printConView(s: String) {
    printCon(s)
  }
} // object Main

/** Class OXO_GUI has the entry point for use as a Java applet.
 */
private class OXO_GUI extends Applet {

  override def getAppletInfo() = OXO_GUI.applicationResourceMap.getString("Application.description") //"TicTacToe by ScalaPino"

  override def getParameterInfo() = {
    val p1 = Array("Name", "String", "Name")
    Array(p1)
  }

  object ui extends UI with Reactor {
    def init() = {
      contents = OXO_View.UI(Some(com.scalaworks.OXO.View.OXO_ViewMenu.menuBar))
    }
  }
  OXO_GUI.initLookAndFeel(OXO_GUI.t("Application.lookAndFeel"))
} // class OXO_GUI