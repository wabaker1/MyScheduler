package exe.phormapps.myscheduler;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

/**
 * Sets up the task view fragment
 *
 * Created by bakerwyatt19 on 5/26/2018.
 */

public class TaskFragment extends Fragment {

    View myView;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.taskview, container, false);
        final ListView taskList = (ListView) myView.findViewById(R.id.taskViewList);
        ((NavigationActivity) getActivity()).updateListView(taskList);
        taskList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                ((NavigationActivity) getActivity()).setListPos(position);
            }
        });
        return myView;
    }
}
