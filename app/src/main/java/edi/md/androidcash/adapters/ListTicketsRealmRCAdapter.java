package edi.md.androidcash.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import edi.md.androidcash.R;
import edi.md.androidcash.RealmHelper.Bill;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;

import static edi.md.androidcash.MainActivity.displayMetrics;

/**
 * Created by Igor on 09.03.2020
 */

public class ListTicketsRealmRCAdapter extends RealmRecyclerViewAdapter<Bill, ListTicketsRealmRCAdapter.ViewHolderString> {

    Realm mRealm;
    SimpleDateFormat simpleDateFormatMD,simpleShiftDateFormatMD;
    TimeZone timeZoneMD;
    Context context;

    protected OrderedRealmCollection<Bill> adapterData;
    String shiftName;
    long shiftDate;

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public void clear(){
        int size = getItemCount();
        notifyItemRangeRemoved(0, size);
    }

    public ListTicketsRealmRCAdapter(Context context, @Nullable OrderedRealmCollection<Bill> data,String shiftName,long shiftDate, boolean autoUpdate) {
        super(data, autoUpdate);
        mRealm = Realm.getDefaultInstance();

        simpleDateFormatMD = new SimpleDateFormat("HH:mm");
        simpleShiftDateFormatMD = new SimpleDateFormat("dd.MM.yyyy");
        timeZoneMD = TimeZone.getTimeZone("Europe/Chisinau");
        simpleDateFormatMD.setTimeZone(timeZoneMD);
        simpleShiftDateFormatMD.setTimeZone(timeZoneMD);
        this.context = context;
        this.shiftName = shiftName;
        this.shiftDate = shiftDate;
        this.adapterData = data;
    }


    @NonNull
    @Override
    public ViewHolderString onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rc_tickets_list, parent, false);
        return new ViewHolderString(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderString holder, int position) {

        Bill item  = getItem(position);

        holder.bind(item);

        holder.itemView.setOnClickListener(view -> {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_content_string_ticket, null);

            AlertDialog contentBill = new AlertDialog.Builder(context,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
            contentBill.setView(dialogView);
            contentBill.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            TextView nameShiftBill = dialogView.findViewById(R.id.tv_shift_name_bill_number);
            TextView total = dialogView.findViewById(R.id.tv_total_for_bill);
            RecyclerView ticketContent = dialogView.findViewById(R.id.rc_content_tickets);

            String name = "Shift " + shiftName + " from " + simpleShiftDateFormatMD.format(shiftDate) + " - " + "Check No: " + item.getShiftReceiptNumSoftware();
            nameShiftBill.setText(name);

            total.setText(String.valueOf(item.getSum()) + " MDL");

            ListContentTicketsRealmRCAdapter adapter = new ListContentTicketsRealmRCAdapter(item.getBillStrings(),true);
            ticketContent.setAdapter(adapter);
            contentBill.show();


            int displayWidth = displayMetrics.widthPixels;
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(contentBill.getWindow().getAttributes());
            int dialogWindowWidth = (int) (displayWidth * 0.8f);
            int dialogWindowHeight = (int) (displayWidth * 0.8f);
            layoutParams.width = dialogWindowWidth;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            contentBill.getWindow().setAttributes(layoutParams);


        });


    }


    class ViewHolderString extends RecyclerView.ViewHolder{
        private TextView nrBill;
        private TextView createdDate;
        private TextView closedDate;
        private TextView author;
        private TextView client;
        private TextView payment;
        private TextView discount;
        private TextView sum;
        private TextView state;

        ViewHolderString(View itemView) {
            super(itemView);
            nrBill = itemView.findViewById(R.id.tv_nr_bill);
            createdDate = itemView.findViewById(R.id.tv_created_date);
            closedDate = itemView.findViewById(R.id.tv_closed_date);
            author = itemView.findViewById(R.id.tv_author);
            client = itemView.findViewById(R.id.tv_client);
            payment = itemView.findViewById(R.id.tv_payment);
            discount = itemView.findViewById(R.id.tv_discount);
            sum = itemView.findViewById(R.id.tv_sum);
            state = itemView.findViewById(R.id.tv_state);
        }

        private void bind(Bill bill) {
            nrBill.setText(String.valueOf(bill.getShiftReceiptNumSoftware()));
            createdDate.setText(simpleDateFormatMD.format(bill.getCreateDate()));
            if(bill.getCloseDate() != 0)
                closedDate.setText(simpleDateFormatMD.format(bill.getCloseDate()));
            else
                closedDate.setText("-");
            author.setText(bill.getAuthorName());
            client.setText("");
            if(bill.getBillPaymentTypes() != null && bill.getBillPaymentTypes().size() > 0){
                if(bill.getBillPaymentTypes().size() == 1){
                    payment.setText(bill.getBillPaymentTypes().get(0).getName());
                }
                else
                    payment.setText("Combo");
            }
            else{
                payment.setText("-");
            }
            discount.setText(String.valueOf(bill.getSum() - bill.getSumWithDiscount()));
            sum.setText(String.valueOf(bill.getSum()));
            state.setText(String.valueOf(bill.isSynchronized()));

        }
    }

}
