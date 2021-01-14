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
 * 네모 박스 그리기 위한 Decorator Class
 */
public class CurrentDecorator implements DayViewDecorator {

    private final Drawable drawable;
    private CalendarDay date;

    //초기화
    public CurrentDecorator(CalendarDay _date, Activity context) {
        drawable = context.getResources().getDrawable(R.drawable.more);
        this.date = _date;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return date.toString().equals(day.toString());
    }

    //날짜에 사각형 그리기
    @Override
    public void decorate(DayViewFacade view) {
        view.setSelectionDrawable(drawable);
    }
}