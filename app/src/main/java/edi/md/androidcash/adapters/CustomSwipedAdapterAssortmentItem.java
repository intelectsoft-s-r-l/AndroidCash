package edi.md.androidcash.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import edi.md.androidcash.R;
import edi.md.androidcash.RealmHelper.AssortmentRealm;
import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by Igor on 03.02.2020
 */

public class CustomSwipedAdapterAssortmentItem extends RealmRecyclerViewAdapter<AssortmentRealm, CustomSwipedAdapterAssortmentItem.MyViewHolder> {

    public CustomSwipedAdapterAssortmentItem(@Nullable OrderedRealmCollection data, boolean autoUpdate) {
        super(data, autoUpdate);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listview_assortment, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (getItem(position) != null) {
            final AssortmentRealm item = getItem(position);

            holder.code.setText(item.getCode());
            holder.name.setText(item.getName());
            holder.price.setText(String.format("%.2f", item.getPrice()).replace(",","."));
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView code,name,price;
        MyViewHolder(View itemView) {
            super(itemView);
            code = itemView.findViewById(R.id.txtCode_ASL);
            name = itemView.findViewById(R.id.txtName_ASL);
            price = itemView.findViewById(R.id.txtPrice_ASL);
        }
    }
}
