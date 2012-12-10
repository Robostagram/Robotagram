package models

import play.api.Logger

class TimerThread(val name: String, sleepingCondition: Unit => Boolean, abortCondition: Unit => Boolean, execute: Unit => Unit) extends Runnable {

  def run() {
    Logger.debug("Starting timer: " + name)
    while(sleepingCondition.apply()) {
      if (abortCondition.apply()) {
        Logger.debug("Cancelling timer: " + name)
        return
      }
      Thread.sleep(TimerThread.DEFAULT_SLEEP_RHYTHM)
    }
    if (!abortCondition.apply()) {
      Logger.debug("Executing payload of timer: " + name)
      execute.apply()
    }
  }

}

object TimerThread {
  
  val DEFAULT_SLEEP_RHYTHM: Long = 1000
  
}