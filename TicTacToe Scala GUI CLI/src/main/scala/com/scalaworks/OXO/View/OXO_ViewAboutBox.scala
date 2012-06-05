package com.scalaworks.OXO
package View

import scala.swing.Swing.EmptyIcon
import scala.swing.Action
import scala.swing.Alignment
import scala.swing.BorderPanel
import scala.swing.Button
import scala.swing.Dialog
import scala.swing.GridBagPanel
import scala.swing.Label
import scala.swing.Swing
import scala.swing.Window

import com.scalaworks.OXO.OXO_GUI.RESOURCEPATH
import com.scalaworks.OXO.OXO_GUI.getIcon
import com.scalaworks.OXO.OXO_GUI

/*
 * OXOAboutBox.scala
 */
class OXOAboutBox() extends Dialog {

  val resourceMap = java.util.ResourceBundle.getBundle(getClass.getPackage.getName + "/resources/OXO_VaboutBox")

  title = resourceMap.getString("title") // NOI18N
  modal = true
  //resizable = false

  contents = new BorderPanel {
    border = Swing.EmptyBorder(20, 20, 20, 20)

    layout(new GridBagPanel {
      val gbc = new Constraints
      gbc.fill = scala.swing.GridBagPanel.Fill.Both
      gbc.gridheight = 6
      gbc.ipadx = 12
      gbc.grid = (0, 0)
      // this.getClass.getResource(path)
      add(new Label("", getIcon('/' + RESOURCEPATH + "images/about.gif"), Alignment.Center) {
        name = ("imageLabel") // NOI18N
      }, gbc)
      gbc.gridheight = 1

      gbc.grid = (1, 0)
      add(new Label(OXO_GUI.applicationResourceMap.getString("Application.title"), EmptyIcon, Alignment.Left) {
        font = (font.deriveFont(font.getStyle() | java.awt.Font.BOLD, font.getSize() + 4));
        name = ("appTitleLabel") // NOI18N
      }, gbc)

      gbc.grid = (1, 1)
      add(new Label(resourceMap.getString("appDescLabel.text"), EmptyIcon, Alignment.Left) {
        name = ("appDescLabel") // NOI18N
      }, gbc)

      gbc.grid = (1, 2)
      add(new Label(resourceMap.getString("versionLabel.text"), EmptyIcon, Alignment.Left) {
        font = (font.deriveFont(font.getStyle() | java.awt.Font.BOLD));
        name = ("versionLabel") // NOI18N
      }, gbc)

      gbc.grid = (1, 3)
      add(new Label(resourceMap.getString("vendorLabel.text"), EmptyIcon, Alignment.Left) {
        font = (font.deriveFont(font.getStyle() | java.awt.Font.BOLD))
        name = ("vendorLabel") // NOI18N
      }, gbc)

      gbc.grid = (1, 4)
      add(new Label(resourceMap.getString("homepageLabel.text"), EmptyIcon, Alignment.Left) {
        font = (font.deriveFont(font.getStyle() | java.awt.Font.BOLD))
        name = ("homepageLabel") // NOI18N
      }, gbc)

      gbc.grid = (2, 2)
      add(new Label(OXO_GUI.applicationResourceMap.getString("Application.version"), EmptyIcon, Alignment.Left) {
        name = ("appVersionLabel") // NOI18N
      }, gbc)

      gbc.grid = (2, 3)
      add(new Label(OXO_GUI.applicationResourceMap.getString("Application.vendor"), EmptyIcon, Alignment.Left) {
        name = ("appVendorLabel") // NOI18N
      }, gbc)

      gbc.grid = (2, 4)
      add(new Label(OXO_GUI.applicationResourceMap.getString("Application.homepage"), EmptyIcon, Alignment.Left) {
        name = ("appHomepageLabel") // NOI18N
      }, gbc)

      gbc.grid = (2, 5)
      add(new Button(Action(resourceMap.getString("closeAboutBox.Action.text")) { dispose() }), gbc)
    }) = BorderPanel.Position.Center
  }
  visible = true
}