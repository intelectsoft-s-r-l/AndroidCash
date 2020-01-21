package edi.md.androidcash.SettingUtils;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edi.md.androidcash.R;

/**
 * Created by Igor on 28.10.2019
 */

public class Returns extends Fragment {

    TextView tv_result;
    Button btn_connect;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootViewAdmin = inflater.inflate(R.layout.fragment_returns, container, false);

        tv_result = rootViewAdmin.findViewById(R.id.txt_result_connection);
        btn_connect = rootViewAdmin.findViewById(R.id.btn_connectionTest);


        return rootViewAdmin;
    }

}
