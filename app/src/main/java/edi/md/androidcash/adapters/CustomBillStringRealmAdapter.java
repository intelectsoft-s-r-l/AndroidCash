package edi.md.androidcash.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import edi.md.androidcash.R;
import edi.md.androidcash.RealmHelper.BillString;
import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;

/**
 * Created by Igor on 24.12.2019
 */

public class CustomBillStringRealmAdapter extends RealmBaseAdapter<BillString> implements ListAdapter {

    SimpleDateFormat sdfChisinau;
    TimeZone tzInChisinau;

    private static class ViewHolder {
        TextView nameString,countString,priceString,priceAfterDiscString;
    }

    public CustomBillStringRealmAdapter(@Nullable OrderedRealmCollection<BillString> data) {
        super(data);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            convertView = inflater.inflate(R.layout.item_listview_list_bill_string,parent,false);

            viewHolder.nameString = convertView.findViewById(R.id.txtNameASL_in_bill);
            viewHolder.countString = convertView.findViewById(R.id.txtCountASL_in_bill);
            viewHolder.priceString = convertView.findViewById(R.id.txtPriceASL_in_bill);
            viewHolder.priceAfterDiscString = convertView.findViewById(R.id.txtSumAfterDiscASL_in_bill);

            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (adapterData != null) {
            final BillString item = adapterData.get(position);

            viewHolder.nameString.setText(item.getAssortmentFullName());
            viewHolder.countString.setText(String.format("%.2f", item.getQuantity()).replace(",","."));
            viewHolder.priceString.setText(String.format("%.2f", item.getPrice()).replace(",","."));
            viewHolder.priceAfterDiscString.setText(String.format("%.2f", item.getPriceWithDiscount()).replace(",","."));
        }
        return convertView;
    }
}
