package com.micronautics.meetupRoll

import com.lamatek.swingextras.JNumericField
import java.io.File
import com.micronautics.util.{SortedProperties, PersistableApp}
import com.micronautics.Attribution
import java.awt.{Component, Point, Dimension, Color}
import javax.swing.{UIManager, WindowConstants}
import javax.swing.table.AbstractTableModel
import scala.swing.event.{ButtonClicked, WindowClosing, WindowOpened}
import javax.swing.border.EmptyBorder
import scala.swing.{TabbedPane, BoxPanel, Button, Label, ScrollPane, TextField, Table, Orientation, MainFrame, SimpleSwingApplication}

/**
  * @author Mike Slinn
  */

/* Copyright 1012 Micronautics Research Corporation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Subject to the additional condition that the attribution code in Gui.scala
   remains untouched and displays each time the program runs.

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */

object Gui extends SimpleSwingApplication with PersistableApp {
  private val itemHeight:Int = 20
  private val model = Model()
  private val propertyFileName = "meetupRoll.properties"

  val top = new MainFrame {
    var tabbedPane = new TabbedPane
    val colorEventIsToday = Color.green
    val colorEventIsNotToday = Color.red
    val sizeOfButtons = new Dimension(75, itemHeight)
    val buttonFinish = new Button("Finish") {
      maximumSize = sizeOfButtons
      tooltip = "Send email summarizing winners and shut down"
    }

    val page1 = new BoxPanel(Orientation.Vertical) {
      val textUrl = new TextField {
        maximumSize = new Dimension(Int.MaxValue, itemHeight)
        tooltip = "Meetup URL"
      }
      val buttonAddUrl = new Button("Add") { maximumSize = sizeOfButtons; tooltip = "URL for new meetup" }
      val buttonDelUrl = new Button("Del") { maximumSize = sizeOfButtons; tooltip = "Remove meetup URL" }
      val textPrize = new TextField {
        maximumSize = new Dimension(Int.MaxValue, itemHeight)
        tooltip = "Name of prize"
      }
      val numericFieldPrizeQty = new JNumericField(2, JNumericField.INTEGER)  {
        setMaximumSize(new Dimension(Int.MaxValue, itemHeight))
        tooltip = "Quantity of these prizes to be given away"
      }
      val buttonAddPrize = new Button("Add") { maximumSize = sizeOfButtons; tooltip = "Add new prize" }

      val tableModel = new MyTableModel( Array[Array[Any]](), List("Meetup Name", "Event Name", "Date/Time") )
      val table      = new Table( 1, 2 ) { model = tableModel }
      for ( i <- 0 to 10 )
        tableModel.addRow(Array[AnyRef]("i", "j"))
      contents += new ScrollPane(table)

      contents += new BoxPanel(Orientation.Horizontal) {
        contents += textUrl
        contents += buttonAddUrl
        contents += buttonDelUrl
      }
      contents +=  new BoxPanel(Orientation.Horizontal) {
        contents += textPrize
        peer.add(numericFieldPrizeQty)
        contents += buttonAddPrize
      }

      reactions += {
        case ButtonClicked(`buttonAddPrize`) =>
          println("buttonAddPrize not implmented")

        case ButtonClicked(`buttonAddUrl`) =>
          println("buttonAddUrl not implmented")

        case ButtonClicked(`buttonDelUrl`) =>
          println("buttonDelUrl not implmented")
      }
    }

    val page2 = new BoxPanel(Orientation.Vertical) {
      val labelRandomName = new Label
      val buttonRoll      = new Button("Roll")   { maximumSize = sizeOfButtons; tooltip = "Pick a random attendee" }
      val buttonAward     = new Button("Award")  { maximumSize = sizeOfButtons; tooltip = "Award prize to selected attendee" }

      val tableModel = new MyTableModel( Array[Array[Any]](), List("Winner Name", "Prize", "Email") )
      val table      = new Table( 1, 2 ) { model = tableModel }
      for ( i <- 0 to 10 ) { tableModel.addRow(Array[AnyRef]("i", "j")) }
      contents += new ScrollPane(table)

    contents +=  new BoxPanel(Orientation.Horizontal) {
      xLayoutAlignment = Component.LEFT_ALIGNMENT
      contents += buttonRoll
      contents += buttonAward
      contents += labelRandomName
    }

    //contents += new ScrollPane(new Table(model.prizesWonDP, Array("Winner", "Prize", "Email")))

    reactions += {
      case ButtonClicked(`buttonRoll`) =>
        println("buttonRoll not implmented")

      case ButtonClicked(`buttonAward`) =>
        println("buttonAward not implmented")
    }
  }

  peer.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
  title = "Meetup Roll v0.1"
  size = new Dimension(575, 900)
  try {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
  } catch {
    case ex => println("Error setting native LAF: " + ex)
  }

  tabbedPane.pages += new TabbedPane.Page("Setup", page1)
  tabbedPane.pages += new TabbedPane.Page("Roll", page2)
  contents = new BoxPanel(Orientation.Vertical) {
    contents += tabbedPane
    contents += new Label(" ")
    contents += buttonFinish
    contents += new Label(" ")
    contents += Attribution.attribution
    border = new EmptyBorder(20, 20, 10, 20)
  }
  pack

    reactions += {
      case WindowOpened(_) =>
        loadProperties
        visible = true

      case WindowClosing(_) =>
        saveProperties(locationOnScreen, size)
        sys.exit(0)

      case ButtonClicked(`buttonFinish`) =>
        println("buttonFinish not implmented")
    }

    private def loadProperties {
      val props = readProperties(new File(propertyFileName))
      location = new Point(props.getOrElse("x", "0").toInt, props.getOrElse("y", "0").toInt)
      size = new Dimension(props.getOrElse("width", "575").toInt, props.getOrElse("height", "900").toInt)
    }

    private def saveProperties(location: Point, size: Dimension) {
      val props = new SortedProperties
      props.setProperty("height", size.getHeight.asInstanceOf[Int].toString)
      props.setProperty("width",  size.getWidth.asInstanceOf[Int].toString)
      props.setProperty("x",      location.getX.asInstanceOf[Int].toString)
      props.setProperty("y",      location.getY.asInstanceOf[Int].toString)
      writeProperties(new File(propertyFileName), props)
    }
  }

  class MyTableModel(var rowData: Array[Array[Any]], val columnNames: Seq[String] ) extends AbstractTableModel {
    override def getColumnName( column: Int) = columnNames(column).toString
    def getRowCount() = rowData.length
    def getColumnCount() = columnNames.length
    def getValueAt( row: Int, col: Int): AnyRef = {
      println("MyTableModel.getValueAt(%d, %d)".format(row, col))
      //rowData(row)(col).asInstanceOf[AnyRef]
      rowData(0)(0).asInstanceOf[AnyRef] // for now
    }
    override def isCellEditable( row: Int, column: Int) = false
    override def setValueAt( value: Any, row: Int, col: Int) {
      rowData(row)(col) = value
    }
    def addRow( data: Array[AnyRef]) {
      rowData ++= Array(data.asInstanceOf[Array[Any]])
    }
  }
}