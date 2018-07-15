package exe.phormapps.myscheduler;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import java.util.Calendar;

import sun.bob.mcalendarview.MCalendarView;
import sun.bob.mcalendarview.vo.DateData;

/**
 * Set up view for viewing a calendar
 *
 * Created by bakerwyatt19 on 5/26/2018.
 */

public class CalendarFragment extends Fragment {

    View myView;
    MCalendarView calendarView;
    Calendar selectedDay;
    Calendar prevDay;
    Button clearButt;
    ListView calTaskList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.calendarview, container, false);

        calTaskList = (ListView) myView.findViewById(R.id.calendarTaskList);
        calendarView = (MCalendarView) myView.findViewById(R.id.calendarView);
        selectedDay = Calendar.getInstance();
        prevDay = Calendar.getInstance();

        ((NavigationActivity) getActivity()).updateCalListView(calTaskList, selectedDay);
        ((NavigationActivity) getActivity()).highlightCalDay(calendarView, selectedDay, prevDay);

        //Highlight selected day of calendar
        calendarView.setOnDateClickListener(new sun.bob.mcalendarview.listeners.OnDateClickListener() {
            @Override
            public void onDateClick(View view, DateData date) {
                selectedDay.set(date.getYear(), date.getMonth() - 1, date.getDay());
                ((NavigationActivity) getActivity()).updateCalListView(calTaskList, selectedDay);
                ((NavigationActivity) getActivity()).highlightCalDay(calendarView, selectedDay, prevDay);
                prevDay.set(selectedDay.get(Calendar.YEAR), selectedDay.get(Calendar.MONTH), selectedDay.get(Calendar.DAY_OF_MONTH));
            }
        });

        clearButt = (Button) myView.findViewById(R.id.clearTasks);
        clearButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarView.unMarkDate(selectedDay.get(Calendar.YEAR)
                        , selectedDay.get(Calendar.MONTH) + 1
                        , selectedDay.get(Calendar.DAY_OF_MONTH));
                ((NavigationActivity) getActivity()).onClearDay(selectedDay);
                ((NavigationActivity) getActivity()).highlightCalDay(calendarView, selectedDay, prevDay);
            }
        });
        return myView;
    }
}
