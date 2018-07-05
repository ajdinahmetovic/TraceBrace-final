package tracebrace.tracebrace_final;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class OnBoarding_second extends Fragment {


    public OnBoarding_second() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final Intent intent = new Intent(getContext(), MessagesActivity.class);


        View view = inflater.inflate(R.layout.fragment_on_boarding_second, container, false);

        TextView finnish = view.findViewById(R.id.finnishSetup);

        finnish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
            }
        });



        return view;
    }


}
