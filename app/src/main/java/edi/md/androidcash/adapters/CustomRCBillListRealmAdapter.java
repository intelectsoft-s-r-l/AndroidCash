package edi.md.androidcash.adapters;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.TimeZone;

import edi.md.androidcash.MainActivity;
import edi.md.androidcash.R;
import edi.md.androidcash.RealmHelper.Bill;
import edi.md.androidcash.Utils.BaseEnum;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

/**
 * Created by Igor on 10.02.2020
 */

public class CustomRCBillListRealmAdapter extends RealmRecyclerViewAdapter<Bill, CustomRCBillListRealmAdapter.ViewHolderString> {

    Realm mRealm;
    SimpleDateFormat simpleDateFormatMD;
    SimpleDateFormat simpleDateFormatHourMD;
    TimeZone timeZoneMD;

    private final RealmChangeListener listener;
    protected OrderedRealmCollection<Bill> adapterData;

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public void clear(){
        int size = getItemCount();
        notifyItemRangeRemoved(0, size);
    }

    public CustomRCBillListRealmAdapter(@Nullable OrderedRealmCollection<Bill> data, boolean autoUpdate) {
        super(data, autoUpdate);
        mRealm = Realm.getDefaultInstance();

        simpleDateFormatMD = new SimpleDateFormat("dd.MM.yyyy");
        simpleDateFormatHourMD = new SimpleDateFormat("HH:mm:ss");
        timeZoneMD = TimeZone.getTimeZone("Europe/Chisinau");
        simpleDateFormatMD.setTimeZone(timeZoneMD);
        simpleDateFormatHourMD.setTimeZone(timeZoneMD);
        this.adapterData = data;
        this.listener = new RealmChangeListener<RealmResults<Bill>>() {
            @Override
            public void onChange(RealmResults<Bill> results) {
                BadgeDrawable badge = MainActivity.tabLayout.getTabAt(2).getOrCreateBadge();
                badge.setVisible(true);
                badge.setNumber(results.size());
            }
        };

        if (data != null) {
            addListener(data);
        }
    }


    @NonNull
    @Override
    public ViewHolderString onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rc_bill_list, parent, false);
        return new ViewHolderString(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderString holder, int position) {

        Bill string  = getItem(position);

        holder.bind(string);

        holder.itemView.setOnClickListener(v -> {
            MainActivity.draweOpen(mRealm.copyFromRealm(string));
        });
    }


    class ViewHolderString extends RecyclerView.ViewHolder{
        private TextView sum;
        private TextView numberBill;
        private TextView date;
        private TextView hour;
        private TextView client;
        private TextView payType;
        private TextView state;
        private TextView colors;

        ViewHolderString(View itemView) {
            super(itemView);
            numberBill = itemView.findViewById(R.id.txt_nr_bill_item);
            sum = itemView.findViewById(R.id.txt_summ_bill_item);
            client = itemView.findViewById(R.id.txt_client_info);
            payType = itemView.findViewById(R.id.txt_pay_type_bill_item);
            state = itemView.findViewById(R.id.txt_state_bill_item);
            date = itemView.findViewById(R.id.txt_date_created_bill_item);
            hour = itemView.findViewById(R.id.txt_hour);
            colors = itemView.findViewById(R.id.textView11);

//            mDrawerLayout = (DrawerLayout)itemView.findViewById(R.id.drawer_layout);
        }

        private void bind(Bill bill) {
            sum.setText("MDL " + String.format("%.2f", bill.getSumWithDiscount()));
            client.setText(bill.getDiscountCardNumber());
            payType.setText(bill.getDiscountCardId());
            numberBill.setText(String.valueOf(bill.getShiftReceiptNumSoftware()));
            hour.setText(simpleDateFormatHourMD.format(bill.getCreateDate()));
            date.setText(simpleDateFormatMD.format(bill.getCreateDate()));
            Drawable background = colors.getBackground();

            if (background instanceof ShapeDrawable) {
                Random rnd = new Random();
                int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                ((ShapeDrawable)background).getPaint().setColor(color);
                colors.setBackground(background);
            }
            else if (background instanceof GradientDrawable) {
                Random rnd = new Random();
                int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                ((GradientDrawable)background).setColor(color);
                colors.setBackground(background);
            }
            else if (background instanceof ColorDrawable) {
                Random rnd = new Random();
                int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                ((ColorDrawable)background).setColor(color);
                colors.setBackground(background);
            }

            int states = bill.getState();
            if(states == BaseEnum.BILL_OPEN)
                state.setText("Open");
            else if (states == BaseEnum.BILL_CLOSED)
                state.setText("Closed");
            else if (states == BaseEnum.BILL_DELETED)
                state.setText("Deleted");
        }
    }
    private void addListener(OrderedRealmCollection<Bill> data) {
        if (data instanceof RealmResults) {
            RealmResults realmResults = (RealmResults) data;
            realmResults.addChangeListener(listener);
        } else {
            throw new IllegalArgumentException("RealmCollection not supported: " + data.getClass());
        }
    }

    private void removeListener(OrderedRealmCollection<Bill> data) {
        if (data instanceof RealmResults) {
            RealmResults realmResults = (RealmResults) data;
            realmResults.removeChangeListener(listener);
        }  else {
            throw new IllegalArgumentException("RealmCollection not supported: " + data.getClass());
        }
    }
    @Override
    public void updateData(OrderedRealmCollection<Bill> data) {
        if (listener != null) {
            if (adapterData != null) {
                removeListener(adapterData);
            }
            if (data != null) {
                addListener(data);
            }
        }

        this.adapterData = data;
        notifyDataSetChanged();
    }
}
