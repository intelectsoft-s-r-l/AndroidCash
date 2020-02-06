package edi.md.androidcash.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import edi.md.androidcash.R;
import edi.md.androidcash.RealmHelper.AssortmentRealm;

/**
 * Created by Igor on 30.01.2020
 */

public class GridQuickButtonAssortmentAdapter extends ArrayAdapter<AssortmentRealm> {

    int heightView;
    public GridQuickButtonAssortmentAdapter(@NonNull Context context, int resource, @NonNull List<AssortmentRealm> objects, int heightButton) {
        super(context, resource, objects);
        this.heightView = heightButton;
    }

    private static class ViewHolder {
       LinearLayout ll;
       TextView txt;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder viewHolder;
        if(convertView == null){
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            convertView = inflater.inflate(R.layout.item_grid_quick_buttons,parent,false);

            viewHolder.ll = convertView.findViewById(R.id.ll_item_grid_view);
            viewHolder.txt = convertView.findViewById(R.id.btn_assortment);

            viewHolder.ll.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, heightView));

            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        AssortmentRealm item = getItem(position);
        if (item != null) {
            if(item.getId() != null)
                viewHolder.txt.setText(item.getName());
            else
                viewHolder.txt.setText("");
        }
        else
            viewHolder.txt.setText("");

        return convertView;
    }
}
