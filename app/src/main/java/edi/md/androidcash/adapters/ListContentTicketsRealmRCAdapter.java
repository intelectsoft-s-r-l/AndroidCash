package edi.md.androidcash.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import edi.md.androidcash.R;
import edi.md.androidcash.RealmHelper.Bill;
import edi.md.androidcash.RealmHelper.BillString;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by Igor on 09.03.2020
 */

public class ListContentTicketsRealmRCAdapter extends RealmRecyclerViewAdapter<BillString, ListContentTicketsRealmRCAdapter.ViewHolderString> {

    protected OrderedRealmCollection<BillString> adapterData;


    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public void clear(){
        int size = getItemCount();
        notifyItemRangeRemoved(0, size);
    }

    public ListContentTicketsRealmRCAdapter(@Nullable OrderedRealmCollection<BillString> data, boolean autoUpdate) {
        super(data, autoUpdate);
        this.adapterData = data;
    }


    @NonNull
    @Override
    public ViewHolderString onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rc_content_tickets_list, parent, false);
        return new ViewHolderString(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderString holder, int position) {

        BillString item  = getItem(position);

        holder.bind(item);
    }


    class ViewHolderString extends RecyclerView.ViewHolder{
        private TextView name;
        private TextView cnt;
        private TextView price;
        private TextView sum;

        ViewHolderString(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_name_string_ticket);
            cnt = itemView.findViewById(R.id.tv_cnt_string_ticket);
            price = itemView.findViewById(R.id.tv_price_string_ticket);
            sum = itemView.findViewById(R.id.tv_sum_string_ticket);
        }

        private void bind(BillString string) {
            name.setText(string.getAssortmentFullName());
            cnt.setText(String.valueOf(string.getQuantity()));
            price.setText(String.valueOf(string.getPrice()));
            sum.setText(String.valueOf(string.getSum()));
        }
    }

}
