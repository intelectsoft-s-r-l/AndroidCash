package edi.md.androidcash.adapters;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import edi.md.androidcash.R;
import edi.md.androidcash.RealmHelper.BillString;
import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;


/**
 * Created by Igor on 23.12.2019
 */

public class CustomNewBillRealmAdapter extends RealmBaseAdapter<BillString> implements ListAdapter {


    private static class ViewHolder {
        TextView nameString, countString, priceString, sumString;
    }


    public CustomNewBillRealmAdapter(@Nullable OrderedRealmCollection<BillString> data) {
        super(data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listview_new_bill, parent, false);
            viewHolder = new ViewHolder();
           //find views for id
            viewHolder.countString = convertView.findViewById(R.id.txtCount_in_new_bill);
            viewHolder.nameString = convertView.findViewById(R.id.txtName_in_new_bill);
            viewHolder.priceString = convertView.findViewById(R.id.txtPrice_in_new_bill);
            viewHolder.sumString = convertView.findViewById(R.id.txtTotal_in_new_bill);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (adapterData != null) {
            final BillString item = adapterData.get(position);

            viewHolder.nameString.setText(item.getAssortmentFullName());

            viewHolder.countString.setText(String.format("%.2f", item.getQuantity()).replace(",","."));
            viewHolder.priceString.setText(String.format("%.2f", item.getPriceWithDiscount()).replace(",","."));
            viewHolder.sumString.setText(String.format("%.2f", item.getSum()).replace(",","."));
            if(item.getPriceWithDiscount() < item.getPrice())
                viewHolder.priceString.setTextColor(Color.parseColor("#79BD60"));
        }
        return convertView;
    }
}
