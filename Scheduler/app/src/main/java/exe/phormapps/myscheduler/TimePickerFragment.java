package exe.phormapps.myscheduler;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;

/**
 * Set up UI for selecting a time
 *
 * Created by bakerwyatt19 on 6/8/2018.
 */

public class TimePickerFragment extends DialogFragment{

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new TimePickerDialog(getActivity()
                , (TimePickerDialog.OnTimeSetListener) getActivity()
                , 12
                , 0
                , DateFormat.is24HourFormat(getActivity()));
    }
}
