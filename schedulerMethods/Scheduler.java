/*
Copyright (C) 2016  R.W. Sutnavage

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/.
*/
package schedulerMethods;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
 
public class Scheduler {
 
    class SchedulerTimerTask extends TimerTask {
        private SchedulerTask schedulerTask;
        private ScheduleIterator iterator;
        public SchedulerTimerTask(SchedulerTask schedulerTask,
                ScheduleIterator iterator) {
            this.schedulerTask = schedulerTask;
            this.iterator = iterator;
        }
        public void run() {
            schedulerTask.run();
            reschedule(schedulerTask, iterator);
        }
    }
 
    private final Timer timer = new Timer();
 
    public Scheduler() {
    }
 
    public void cancel() {
        timer.cancel();
    }
 
    public void schedule(SchedulerTask schedulerTask,
            ScheduleIterator iterator) {
 
        Date time = iterator.next();
        if (time == null) {
            schedulerTask.cancel();
        } else {
            synchronized(schedulerTask.lock) {
                if (schedulerTask.state != SchedulerTask.VIRGIN) {
                  throw new IllegalStateException("Task already scheduled " + "or cancelled");
                }
                schedulerTask.state = SchedulerTask.SCHEDULED;
                schedulerTask.timerTask =
                    new SchedulerTimerTask(schedulerTask, iterator);
                timer.schedule(schedulerTask.timerTask, time);
            }
        }
    }
 
    private void reschedule(SchedulerTask schedulerTask,
            ScheduleIterator iterator) {
 
        Date time = iterator.next();
        if (time == null) {
            schedulerTask.cancel();
        } else {
            synchronized(schedulerTask.lock) {
                if (schedulerTask.state != SchedulerTask.CANCELLED) {
                    schedulerTask.timerTask =
                        new SchedulerTimerTask(schedulerTask, iterator);
                    timer.schedule(schedulerTask.timerTask, time);
                }
            }
        }
    }
 
}
