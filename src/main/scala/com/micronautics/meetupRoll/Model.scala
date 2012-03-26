package com.micronautics.meetupRoll

import javax.swing.table.AbstractTableModel
import org.joda.time.DateTime
import scala.collection.mutable.ArrayBuffer

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

/**
  * @author Mike Slinn
  */

case class Meetup(val meetupName: String, val eventTitle: String, val dateTime: DateTime)

case class PrizeWon(val winnerName: String, val prize: String, val email: String)

class Model(
  val meetups: ArrayBuffer[Meetup],
  val prizesWon: ArrayBuffer[PrizeWon]
) {
  def meetupsDP = new AbstractTableModel() {
      def getColumnCount = { 3 }
      def getRowCount    = { 2 }
      def getValueAt(row: Int, col: Int): Object = {
        if (col==0)
          meetups(row-1).meetupName
        else if (col==1)
          meetups(row-1).eventTitle
        else
          meetups(row-1).dateTime
      }
  }

  def prizesWonDP = new AbstractTableModel() {
      def getColumnCount = { 3 }
      def getRowCount    = { 6 }
      def getValueAt(row: Int, col: Int): Object = {
        if (col==0)
          prizesWon(row-1).winnerName
        else if (col==1)
          prizesWon(row-1).prize
        else
          prizesWon(row-1).email
      }
  }
}

object Model {
  def apply() = {
    val atm = ArrayBuffer.empty[Meetup]
    //new Model(, ArrayBuffer.empty[PrizeWon])
  }
}
