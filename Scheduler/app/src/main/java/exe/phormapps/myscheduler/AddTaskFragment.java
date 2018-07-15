package exe.phormapps.myscheduler;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import java.text.SimpleDateFormat;

/**
 * Set up view for adding or editing a task
 *
 * Created by bakerwyatt19 on 5/26/2018.
 */

public class AddTaskFragment extends Fragment {

    View myView;
    TextView titleView;
    TextView descView;
    Switch remindSwitch;
    TextView dateDisplay;
    TextView timeDisplay;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.addtaskview, container, false);

        titleView = (TextView) myView.findViewById(R.id.taskTitleTV);
        descView = (TextView) myView.findViewById(R.id.taskDescription);
        remindSwitch = (Switch) myView.findViewById(R.id.taskRemindToggle);
        dateDisplay = (TextView) myView.findViewById(R.id.dateSelectionTB);
        timeDisplay = (TextView) myView.findViewById(R.id.timeSelectionTB);

        ((NavigationActivity) getActivity()).editingFill(titleView, descView, remindSwitch, dateDisplay, timeDisplay);

        return myView;
    }

}
