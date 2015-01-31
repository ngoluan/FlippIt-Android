package luan.com.flippit;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class Welcome2Fragment extends Fragment {
    View mView = null;
    Context mContext = null;

    public Welcome2Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_getstarted2, container,
                false);
        mContext = container.getContext();
        //Button startedBtn = (Button) mView.findViewById(R.id.getStarted);
        Button contBtn = (Button) mView.findViewById(R.id.welcome2);
        contBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                MyActivity.mActionBar.show();
                MyActivity.mFragmentManager.beginTransaction()
                        .replace(R.id.container, new HistoryFragment())
                        .commit();
            }
        });

        // Inflate the layout for this fragment
        return mView;
    }


}
