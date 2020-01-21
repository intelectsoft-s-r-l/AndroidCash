package edi.md.androidcash.DynamicTabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edi.md.androidcash.R;

/**
 * Created by Igor on 30.12.2019
 */

public class DynamicFragment extends Fragment {
    View view;
    int val;
    TextView c;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_dynamic_tab_content, container, false);
        val = getArguments().getInt("someInt", 0);
        c = view.findViewById(R.id.c);
        c.setText("getStr - " + val);

        return view;
    }
    public static DynamicFragment addfrag(int val) {
        DynamicFragment fragment = new DynamicFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", val);
        fragment.setArguments(args);
        return fragment;
    }
}
