
package com.example.hm_project.etc;


import android.app.Activity;
import android.graphics.drawable.Drawable;

import com.example.hm_project.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Collection;
import java.util.HashSet;
/**
 * 빨간 점 그리기 위한 Decorator Class
 */
public class EventDecorator implements DayViewDecorator {
    private int color;
    private HashSet<CalendarDay> dates;
    //초기화
    public EventDecorator(int color, Collection<CalendarDay> dates) {
            this.color = color;
            this.dates = new HashSet<>(dates);
    }
    //초기화
    public EventDecorator(int color) {
        this.color = color;
    }
    //초기화
    public EventDecorator(Collection<CalendarDay> dates) {
        this.dates = new HashSet<>(dates);
    }
    public void setDates(Collection<CalendarDay> dates){
        this.dates = new HashSet<>(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    // 날자밑에 점 그리기
    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpan(5, color));
    }
}