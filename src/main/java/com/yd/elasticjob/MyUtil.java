package com.yd.elasticjob;

import org.quartz.CronExpression;

import java.util.Date;

/**
 * @author： 叶小东
 * @date： 2021/6/2 15:24
 */
public class MyUtil {

    /**
     * 判断指定时间是否符合cron表达式
     *
     * @param date
     * @param cron
     * @return
     */
    public static boolean isRunTime(Date date, String cron){
        try {
            return new CronExpression(cron).isSatisfiedBy(date);
        } catch (Exception e){
            return false;
        }
    }

    /**
     * 判断指定时间之后的下一次符合cron表达式的执行时间
     *
     * @param date
     * @param cron
     * @return
     */
    public static Date getTimeAfter(Date date, String cron){
        try {
            return new CronExpression(cron).getTimeAfter(date);
        } catch (Exception e){
            return null;
        }
    }

    /**
     * 判断指定时间之前的上一次符合cron表达式的执行时间
     *
     * @param date
     * @param cron
     * @return
     */
    public static Date getTimeBefore(Date date, String cron){
        try {
            CronExpression cronExpression = new CronExpression(cron);
            Date time1 = cronExpression.getTimeAfter(date);
            Date time2 = cronExpression.getTimeAfter(time1);
            Date time3 = cronExpression.getTimeAfter(time2);
            long l = time1.getTime() - (time3.getTime() - time2.getTime());
            return new Date(l);
        } catch (Exception e){
            return null;
        }
    }
}
