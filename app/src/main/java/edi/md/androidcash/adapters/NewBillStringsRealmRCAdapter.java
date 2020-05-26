package edi.md.androidcash.adapters;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import edi.md.androidcash.MainActivity;
import edi.md.androidcash.R;
import edi.md.androidcash.RealmHelper.BillString;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by Igor on 10.02.2020
 */

public class NewBillStringsRealmRCAdapter extends RealmRecyclerViewAdapter<BillString, NewBillStringsRealmRCAdapter.ViewHolderString> {

    Realm mRealm;
    TextView value;
    DisplayMetrics displayMetrics;

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public void clear(){
        int size = getItemCount();
        notifyItemRangeRemoved(0, size);
    }

    public NewBillStringsRealmRCAdapter(@Nullable OrderedRealmCollection<BillString> data, boolean autoUpdate) {
        super(data, autoUpdate);
        mRealm = Realm.getDefaultInstance();
        displayMetrics = MainActivity.displayMetrics;

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
            LayoutInflater inflater = MainActivity.inflater;
            View dialogView = inflater.inflate(R.layout.dialog_change_count_assortment_item, null);

            AlertDialog changeCount = new AlertDialog.Builder(MainActivity.getContext(),R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
            changeCount.setView(dialogView);
            changeCount.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            TextView assortmentName = dialogView.findViewById(R.id.txt_assortmentname);
            value = dialogView.findViewById(R.id.calculator_value);
            MaterialButton oky = dialogView.findViewById(R.id.btn_okey);
            ImageButton btn_cancel =  dialogView.findViewById(R.id.btnClose_dialog);
            MaterialButton dot= dialogView.findViewById(R.id.calculator_dot);
            ImageButton delete = dialogView.findViewById(R.id.calculator_backspace);

            TextView calc1 = dialogView.findViewById(R.id.calculator_1);
            TextView calc2 = dialogView.findViewById(R.id.calculator_2);
            TextView calc3 = dialogView.findViewById(R.id.calculator_3);
            TextView calc4 = dialogView.findViewById(R.id.calculator_4);
            TextView calc5 = dialogView.findViewById(R.id.calculator_5);
            TextView calc6 = dialogView.findViewById(R.id.calculator_6);
            TextView calc7 = dialogView.findViewById(R.id.calculator_7);
            TextView calc8 = dialogView.findViewById(R.id.calculator_8);
            TextView calc9 = dialogView.findViewById(R.id.calculator_9);
            TextView calc0 = dialogView.findViewById(R.id.calculator_0);

            calc1.setOnClickListener(btn);
            calc2.setOnClickListener(btn);
            calc3.setOnClickListener(btn);
            calc4.setOnClickListener(btn);
            calc5.setOnClickListener(btn);
            calc6.setOnClickListener(btn);
            calc7.setOnClickListener(btn);
            calc8.setOnClickListener(btn);
            calc9.setOnClickListener(btn);
            calc0.setOnClickListener(btn);

            assortmentName.setText(string.getAssortmentFullName());

            oky.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!value.getText().toString().equals("") && !value.getText().toString().equals("0")){
                        double quantity = 0;
                        try{
                            quantity = Double.parseDouble(value.getText().toString());
                        }catch (Exception e){
                            quantity = Double.parseDouble(value.getText().toString().replace(",","."));
                        }
                        double sum = string.getPrice() * quantity;
                        double sumWithDisc = string.getPriceWithDiscount() * quantity;

                        MainActivity.editLineCount(string,sumWithDisc,sum,quantity);
                        changeCount.dismiss();
                        notifyItemChanged(position);
                    }
                    else
                        MainActivity.postToastMessage("Enter quantity greater than 0!");
                }
            });

            dot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(string.isAllowNonInteger()){
                        String test = value.getText().toString();
                        boolean contains = false;
                        for (int i = 0; i < test.length(); i++) {
                            String chars = String.valueOf(test.charAt(i));
                            if (chars.equals(".")) {
                                contains = true;
                            }
                        }
                        if (!contains) {
                            if(value.getText().toString().equals(""))
                                value.append("0.");
                            else
                                value.append(".");
                        }
                    }
                    else{
                        MainActivity.postToastMessage("Only integer quantity!");
                    }

                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!value.getText().toString().equals("")) value.setText(value.getText().toString().substring(0, value.getText().toString().length() - 1));
                }
            });

            btn_cancel.setOnClickListener(view -> {
                changeCount.dismiss();
            });

            changeCount.show();

            int displayWidth = displayMetrics.widthPixels;
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(changeCount.getWindow().getAttributes());
            int dialogWindowWidth = (int) (displayWidth * 0.4f);
            layoutParams.width = dialogWindowWidth;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            changeCount.getWindow().setAttributes(layoutParams);
        });
    }
    View.OnClickListener btn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.calculator_1 : addNumberToQuantityItem("1");break;
                case R.id.calculator_2 : addNumberToQuantityItem("2");break;
                case R.id.calculator_3 : addNumberToQuantityItem("3");break;
                case R.id.calculator_4 : addNumberToQuantityItem("4");break;
                case R.id.calculator_5 : addNumberToQuantityItem("5");break;
                case R.id.calculator_6 : addNumberToQuantityItem("6");break;
                case R.id.calculator_7 : addNumberToQuantityItem("7");break;
                case R.id.calculator_8 : addNumberToQuantityItem("8");break;
                case R.id.calculator_9 : addNumberToQuantityItem("9");break;
                case R.id.calculator_0 : addNumberToQuantityItem("0");break;
            }
        }
    };
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
                quantity.setText(String.format("%.2f", billString.getQuantity()).replace(",","."));
                price.setText(String.format("%.2f", billString.getPrice()).replace(",","."));
                discount.setText(String.format("%.2f", billString.getPrice() - billString.getPriceWithDiscount()).replace(",","."));
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

    public void addNumberToQuantityItem(String number){
        String text = value.getText().toString();
        if(text.contains(".")){
            int index = text.indexOf(".");
            if(text.length() - 1 <= index || text.length() - 2 == index){
                value.append(number);
            }
        }
        else{
            value.append(number);
        }
    }

}
