package exe.phormapps.myscheduler;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Call XML for displaying app info
 *
 * Created by bakerwyatt19 on 5/26/2018.
 */

public class InformationFragment extends Fragment {

    View myView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.information, container, false);
        return myView;
    }
}
