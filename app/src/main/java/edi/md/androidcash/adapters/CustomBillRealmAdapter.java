package edi.md.androidcash.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import edi.md.androidcash.NetworkUtils.User;
import edi.md.androidcash.R;
import edi.md.androidcash.RealmHelper.Bill;
import edi.md.androidcash.RealmHelper.BillPaymentType;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmBaseAdapter;
import io.realm.RealmList;

/**
 * Created by Igor on 24.12.2019
 */

public class CustomBillRealmAdapter extends RealmBaseAdapter<Bill> implements ListAdapter {

    SimpleDateFormat sdfChisinau;
    TimeZone tzInChisinau;
    private Realm mRealm;
    User userA;


    private static class ViewHolder {
        TextView number_bill,date_created,author_bill,sum_bill,sumAfterDisc_bill,payType_bill,state_bill;

    }

    public CustomBillRealmAdapter(@Nullable OrderedRealmCollection<Bill> data) {
        super(data);
        mRealm = Realm.getDefaultInstance();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null){
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());

                convertView = inflater.inflate(R.layout.item_listview_list_bills,parent,false);

                viewHolder.number_bill = convertView.findViewById(R.id.txtNo_bill);
                viewHolder.date_created = convertView.findViewById(R.id.txtDateCreate_bill);
                viewHolder.author_bill = convertView.findViewById(R.id.txtAuthor_bill);
                viewHolder.sum_bill = convertView.findViewById(R.id.txtSum_bill);
                viewHolder.sumAfterDisc_bill = convertView.findViewById(R.id.txtSumAfterDisc_bill);
                viewHolder.payType_bill = convertView.findViewById(R.id.txtPayType_bill);
                viewHolder.state_bill = convertView.findViewById(R.id.chbx_State_bill);

                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

        if (adapterData != null) {
            final Bill item = adapterData.get(position);

            sdfChisinau = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
            tzInChisinau = TimeZone.getTimeZone("Europe/Chisinau");
            sdfChisinau.setTimeZone(tzInChisinau);
            String authID = item.getAuthor();

            if(authID == null)
                viewHolder.author_bill.setText("Necunoscut");
            else{
                mRealm.executeTransaction(realm -> {
                    User user = realm.where(User.class).equalTo("id",authID).findFirst();
                    if(user != null ){
                        userA = mRealm.copyFromRealm(user);
                    }
                });
                if(userA != null){
                    String Fname = "";
                    if(userA.getFirstName() != null)
                        Fname = userA.getFirstName();
                    String Lname = "";
                    if(userA.getLastName() != null)
                        Lname = userA.getLastName();

                    viewHolder.author_bill.setText(Fname + Lname);
                }
                else
                    viewHolder.author_bill.setText(authID);
            }

            if(item.getShiftReceiptNumSoftware() != 0)
                viewHolder.number_bill.setText(String.valueOf(item.getShiftReceiptNumSoftware()));
            else
                viewHolder.number_bill.setText("0");

            viewHolder.date_created.setText(sdfChisinau.format(item.getCreateDate()));


            viewHolder.sum_bill.setText(String.format("%.2f", item.getSum()).replace(",","."));
            viewHolder.sumAfterDisc_bill.setText(String.format("%.2f", item.getSumWithDiscount()).replace(",","."));

            RealmList<BillPaymentType> payments = item.getBillPaymentTypes();

            if(payments.isEmpty()){
                viewHolder.payType_bill.setText("");
            }
            else if(payments.size() == 1){
                BillPaymentType billPaymentType = payments.first();
                viewHolder.payType_bill.setText(billPaymentType.getName());
            }
            else if (payments.size() > 1 ){
                viewHolder.payType_bill.setText("Combo");
            }

            int state = item.getState();
            if(state == 0)
                viewHolder.state_bill.setText("Open");
            else if (state == 1)
                viewHolder.state_bill.setText("Closed");
            else if (state == 2)
                viewHolder.state_bill.setText("Deleted");
        }
        return convertView;
    }
}
