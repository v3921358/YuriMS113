/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.server;

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author flyx
 */
public final class RateWorker implements Runnable {

    private static RateWorker instance;
    private static TimeType time;

    public RateWorker() {
        super();
        run();

    }


    public static RateWorker getInstance() {
        return instance;
    }

    public static TimeType getTime() {
        return time;
    }

    @Override
    public void run() {
        if (instance == null) {
            instance = this;
        }
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        switch (hour) {
            case 0:
                time = TimeType.HOUR0;
                break;
            case 1:
                time = TimeType.HOUR1;
                 break;
            case 2:
                time = TimeType.HOUR2;
                 break;
            case 3:
                time = TimeType.HOUR3;
                 break;
            case 4:
                time = TimeType.HOUR4;
                 break;
            case 5:
                time = TimeType.HOUR5; 
                break;
            case 6:
                time = TimeType.HOUR6;
                 break;
            case 7:
                time = TimeType.HOUR7;
                 break;
            case 8:
                time = TimeType.HOUR8;
                 break;
            case 9:
                time = TimeType.HOUR9;
            case 10:
                time = TimeType.HOUR10;
                 break;
            case 11:
                time = TimeType.HOUR11;
                 break;
            case 12:
                time = TimeType.HOUR12;
                 break;
            case 13:
                time = TimeType.HOUR13;
                 break;
            case 14:
                time = TimeType.HOUR14;
                 break;
            case 15:
                time = TimeType.HOUR15;
                 break;
            case 16:
                time = TimeType.HOUR16;
                 break;
            case 17:
                time = TimeType.HOUR17;
                 break;
            case 18:
                time = TimeType.HOUR18;
                 break;
            case 19:
                time = TimeType.HOUR19;
            case 20:
                time = TimeType.HOUR20;
                 break;
            case 21:
                time = TimeType.HOUR21;
                 break;
            case 22:
                time = TimeType.HOUR22;
                 break;
            case 23:
                time = TimeType.HOUR23;
                 break;
        }
    }

}
