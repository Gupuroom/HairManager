
package com.example.hm_project.etc;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Date;

/**
 * 오늘 날짜 녹색 굵은 글씨 표시
 */
public class OneDayDecorator implements DayViewDecorator {

    private CalendarDay date;

    //오늘 날짜 설정
    public OneDayDecorator() {
        date = CalendarDay.today();
    }

    //오늘 날짜 반환
    public CalendarDay today(){
        CalendarDay date= new CalendarDay();
        date= CalendarDay.from(date.getYear(),date.getMonth()+1,date.getDay());
        return date;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return date != null && day.equals(date);
    }

    //오늘 날짜 녹색 굵게 로 표시하기
    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new StyleSpan(Typeface.BOLD));
        view.addSpan(new RelativeSizeSpan(1.4f));
        view.addSpan(new ForegroundColorSpan(Color.GREEN));
    }

    /**
     * We're changing the internals, so make sure to call {@linkplain MaterialCalendarView#invalidateDecorators()}
     */
    public void setDate(Date date) {
        this.date = CalendarDay.from(date);
    }
}