package com.github.fm_jm.neuraltrends

import groovy.time.TimeCategory
import groovy.time.TimeDuration


class DurationsHelper {
    static def normalize(TimeDuration d){
        int months = d.months
        int days = d.days
        int hours = d.hours
        int minutes = d.minutes
        int seconds = d.seconds
        minutes += (int) Math.floor(seconds/60)
        seconds = seconds % 60
        hours += (int) Math.floor(minutes/60)
        minutes = minutes % 60
        days += (int) Math.floor(hours/24)
        hours = hours % 24
        months += (int) Math.floor(days/30)
        days = days % 30
        def out
        use(TimeCategory){
            out = months.months + days.days + hours.hours + minutes.minutes + seconds.seconds
        }
    }

    static def divide(TimeDuration d, int x){
        normalize(
            TimeCategory.getSeconds(
                (int) Math.floor(
                    (d.seconds + d.minutes*60 + d.hours*60*60 + d.days*24*60*60 + d.months*30*24*60*60) /
                    x
                )
            )
        )
    }

    static List<Integer> toList(def duration){
        [duration.days, duration.hours, duration.minutes, duration.seconds]
    }

    static def toDuration(List<Integer> l){
        use(TimeCategory){
            normalize(l[0].days + l[1].hours + l[2].minutes + l[3].seconds)
        }
    }
}
