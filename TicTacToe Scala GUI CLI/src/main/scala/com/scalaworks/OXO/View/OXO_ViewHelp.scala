package com.scalaworks.OXO
package View

import java.net.URL
import oracle.help.CSHManager
import oracle.help.Help
import oracle.help.library.helpset.HelpSet

// Find the HelpSet file and create the HelpSet object:

object OXO_ViewHelp {
  val helpOnHelpURL = new URL(
    "file:/C:/JVM Executables/ohj-11.1.2.0.0/demodoc/helpOnHelp/helpOnHelp.htm")

  val _help = new Help(true, true)
  val _contextManager = new CSHManager(_help)

  try {
    val aBook = new HelpSet( // throws HelpSetParseException
      new URL(
        "file:/C:/JVM Executables/ohj-11.1.2.0.0/demodoc/ohguide/ohguide.hs"))
    _contextManager.addBook(aBook, true)
    _help.setHelpOnHelp(helpOnHelpURL)
  } catch {
    case e => {
      System.err.println("CSHDemo Error: " + e.getMessage())
      System.exit(1)
    }
  }
  
  

}