package edi.md.androidcash.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import edi.md.androidcash.R;
import edi.md.androidcash.RealmHelper.Bill;
import edi.md.androidcash.RealmHelper.BillString;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by Igor on 24.12.2019
 */

public class CustomBillStringRealmAdapter extends RealmRecyclerViewAdapter<BillString, CustomBillStringRealmAdapter.BSViewHolder> {

    public CustomBillStringRealmAdapter(@Nullable OrderedRealmCollection<BillString> data, boolean autoUpdate) {
        super(data, autoUpdate);
    }

    @NonNull
    @Override
    public BSViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listview_list_bill_string, parent, false);
        return new BSViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BSViewHolder holder, int position) {

        BillString bill = getItem(position);

        holder.bind(bill);
    }

    public class BSViewHolder  extends RecyclerView.ViewHolder{
        TextView nameString,countString,priceString,priceAfterDiscString;
        public BSViewHolder(@NonNull View view) {
            super(view);
            nameString = view.findViewById(R.id.txtNameASL_in_bill);
            countString = view.findViewById(R.id.txtCountASL_in_bill);
            priceString = view.findViewById(R.id.txtPriceASL_in_bill);
            priceAfterDiscString = view.findViewById(R.id.txtSumAfterDiscASL_in_bill);
        }

        private void bind(BillString item) {
            nameString.setText(item.getAssortmentFullName());
            countString.setText(String.format("%.2f", item.getQuantity()).replace(",","."));
            priceString.setText(String.format("%.2f", item.getPrice()).replace(",","."));
            priceAfterDiscString.setText(String.format("%.2f", item.getPriceWithDiscount()).replace(",","."));
        }
    }


}
