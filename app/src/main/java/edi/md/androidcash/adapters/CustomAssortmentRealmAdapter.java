package edi.md.androidcash.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import edi.md.androidcash.R;
import edi.md.androidcash.RealmHelper.AssortmentRealm;
import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;

/**
 * Created by Igor on 24.12.2019
 */

public class CustomAssortmentRealmAdapter extends RealmBaseAdapter<AssortmentRealm> implements ListAdapter {

    private static class ViewHolder {
        TextView code,name,price;
    }

    public CustomAssortmentRealmAdapter(@Nullable OrderedRealmCollection<AssortmentRealm> data) {
        super(data);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null){
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());

                convertView = inflater.inflate(R.layout.item_listview_assortment,parent,false);

                viewHolder.code = convertView.findViewById(R.id.txtCode_ASL);
                viewHolder.name = convertView.findViewById(R.id.txtName_ASL);
                viewHolder.price = convertView.findViewById(R.id.txtPrice_ASL);

                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

        if (adapterData != null) {
            final AssortmentRealm item = adapterData.get(position);

            viewHolder.code.setText(item.getCode());
            viewHolder.name.setText(item.getName());
            viewHolder.price.setText(String.format("%.2f", item.getPrice()).replace(",","."));
        }
        return convertView;
    }
}
