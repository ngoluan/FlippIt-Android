package luan.com.pass;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class WelcomeFragment extends Fragment {
    View mView = null;
    Context mContext = null;

    public WelcomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_welcome, container,
                false);
        mContext = container.getContext();
        Button textBtn = (Button) mView.findViewById(R.id.welcomeTextTutorial);
        Button fileBtn = (Button) mView.findViewById(R.id.welcomeFileTutorial);
        Button contBtn = (Button) mView.findViewById(R.id.welcomeContinue);

        textBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                GeneralUtilities.textTutorial(mContext);
            }
        });
        fileBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                GeneralUtilities.fileTutorial(mContext);
            }
        });
        contBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                MyActivity.mFragmentManager.beginTransaction()
                        .replace(R.id.container, new HistoryFragment())
                        .commit();
            }
        });

        // Inflate the layout for this fragment
        return mView;
    }


}
