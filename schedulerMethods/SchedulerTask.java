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

import java.util.TimerTask;

public abstract class SchedulerTask implements Runnable {
 
    final Object lock = new Object();
 
    int state = VIRGIN;
    static final int VIRGIN = 0;
    static final int SCHEDULED = 1;
    static final int CANCELLED = 2;
 
    TimerTask timerTask;
 
    protected SchedulerTask() {
    }
 
    public abstract void run();
 
    public boolean cancel() {
        synchronized(lock) {
            if (timerTask != null) {
                timerTask.cancel();
            }
            boolean result = (state == SCHEDULED);
            state = CANCELLED;
            return result;
        }
    }
 
    public long scheduledExecutionTime() {
        synchronized(lock) {
         return timerTask == null ? 0 : timerTask.scheduledExecutionTime();
        }
    }
 
}
