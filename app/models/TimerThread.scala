package models

import play.api.Logger

class TimerThread(val name: String, sleepingCondition: Unit => Boolean, abortCondition: Unit => Boolean, execute: Unit => Unit) extends Runnable {

  def run() {
    Logger.debug("Executing timer: " + name)
    while(sleepingCondition.apply()) {
      if (abortCondition.apply()) {
        Logger.debug("Cancelling timer: " + name)
        return
      }
      Thread.sleep(TimerThread.DEFAULT_SLEEP_RYTHM)
    }
    Logger.debug("Executing payload of timer: " + name)
    execute.apply()
  }

}

object TimerThread {
  
  val DEFAULT_SLEEP_RYTHM: Long = 1000
  
}