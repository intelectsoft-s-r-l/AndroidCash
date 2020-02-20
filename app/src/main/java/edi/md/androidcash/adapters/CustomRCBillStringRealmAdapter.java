package edi.md.androidcash.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.Date;

import edi.md.androidcash.BaseApplication;
import edi.md.androidcash.MainActivity;
import edi.md.androidcash.R;
import edi.md.androidcash.RealmHelper.Bill;
import edi.md.androidcash.RealmHelper.BillString;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by Igor on 10.02.2020
 */

public class CustomRCBillStringRealmAdapter extends RealmRecyclerViewAdapter<BillString, CustomRCBillStringRealmAdapter.ViewHolderString> {

    Realm mRealm;

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public void clear(){
        int size = getItemCount();
        notifyItemRangeRemoved(0, size);
    }

    public CustomRCBillStringRealmAdapter(@Nullable OrderedRealmCollection<BillString> data, boolean autoUpdate) {
        super(data, autoUpdate);
        mRealm = Realm.getDefaultInstance();
    }

    @NonNull
    @Override
    public ViewHolderString onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rc_bill_strings, parent, false);
        return new ViewHolderString(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderString holder, int position) {

        BillString string  = getItem(position);


        holder.bind(string);

        holder.itemView.setOnClickListener(v -> {
            holder.collapsOtherItems(position);
            // Get the current state of the item
            boolean expanded = string.isExpanded();
            // Change the state
            mRealm.executeTransaction(realm ->{
                string.setExpanded(!expanded);
            });

            // Notify the adapter that item has changed
            notifyItemChanged(position);
        });

        holder.addQuantity.setOnClickListener(v->{
            double existQuantity = string.getQuantity();
            existQuantity += 1 ;

            double sum = string.getPrice() * existQuantity;
            double sumWithDisc = string.getPriceWithDiscount() * existQuantity;

            MainActivity.editLineCount(string,sumWithDisc,sum,existQuantity);

            notifyItemChanged(position);
        });

        holder.deleteQuantity.setOnClickListener(v ->{
            double existQuantity = string.getQuantity();
            if(existQuantity - 1 > 0){
                existQuantity -= 1;

                double sum = string.getPrice() * existQuantity;
                double sumWithDisc = string.getPriceWithDiscount() * existQuantity;

                MainActivity.editLineCount(string,sumWithDisc,sum,existQuantity);
            }

            notifyItemChanged(position);
        });

        holder.delete.setOnClickListener(v->{

            MainActivity.deleteBillString(string);
            notifyItemRemoved(position);
            notifyDataSetChanged();

        });

        holder.quantity.setOnClickListener(v->{

        });
    }

    class ViewHolderString extends RecyclerView.ViewHolder{
        private TextView title;
        private TextView sum;
        private TextView price;
        private TextView discount;
        private TextView quantity;
        private TextView delete;
        ImageButton addQuantity;
        ImageButton deleteQuantity;

        private View subItem;

        ViewHolderString(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_title);
            sum = itemView.findViewById(R.id.item_sum);
            price = itemView.findViewById(R.id.txt_item_price);
            discount = itemView.findViewById(R.id.txt_discount_item);
            quantity = itemView.findViewById(R.id.txt_count_item);
            addQuantity = itemView.findViewById(R.id.btn_plus);
            deleteQuantity = itemView.findViewById(R.id.btn_minus);
            delete = itemView.findViewById(R.id.txt_btn_delete);

            subItem = itemView.findViewById(R.id.sub_item);
        }

        private void bind(BillString billString) {
            if(!billString.isDeleted()){
                boolean expanded = billString.isExpanded();

                subItem.setVisibility(expanded ? View.VISIBLE : View.GONE);

                title.setText(billString.getAssortmentFullName());
                sum.setText(String.format("%.2f", billString.getSumWithDiscount()) + " MDL");
                quantity.setText(String.format("%.2f", billString.getQuantity()));
                price.setText(String.format("%.2f", billString.getPrice()));
                discount.setText(String.format("%.2f", billString.getPrice() - billString.getPriceWithDiscount()));
            }
        }

        private void collapsOtherItems(int position){
            int allItems = getItemCount();
            for(int i= 0; i < allItems; i++){
                if(i == position)
                    continue;
                else{
                    BillString billString = getItem(i);
                    if(!billString.isDeleted()){
                        boolean expand = billString.isExpanded();

                        if(expand)
                            mRealm.executeTransaction(realm ->{
                                billString.setExpanded(!expand);
                            });

                        notifyItemChanged(i);
                    }
                }
            }
        }
    }
}
