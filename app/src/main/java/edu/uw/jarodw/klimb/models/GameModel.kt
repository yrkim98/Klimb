package edu.uw.jarodw.klimb.models
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

data class GameModel(val name: String, val startTime: LocalDateTime, val endTime: LocalDateTime, val maxNumberOfPlayers: Int, val iconLink: String) {
}