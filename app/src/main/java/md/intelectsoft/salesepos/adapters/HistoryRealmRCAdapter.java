package md.intelectsoft.salesepos.adapters;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.TimeZone;

import md.intelectsoft.salesepos.R;
import md.intelectsoft.salesepos.RealmHelper.History;
import md.intelectsoft.salesepos.Utils.BaseEnum;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by Igor on 09.03.2020
 */

public class HistoryRealmRCAdapter extends RealmRecyclerViewAdapter<History, HistoryRealmRCAdapter.ViewHolderString> {

    Realm mRealm;
    SimpleDateFormat simpleDateFormatMD;
    TimeZone timeZoneMD;

    protected OrderedRealmCollection<History> adapterData;

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public void clear(){
        int size = getItemCount();
        notifyItemRangeRemoved(0, size);
    }

    public HistoryRealmRCAdapter(@Nullable OrderedRealmCollection<History> data, boolean autoUpdate) {
        super(data, autoUpdate);
        mRealm = Realm.getDefaultInstance();

        simpleDateFormatMD = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        timeZoneMD = TimeZone.getTimeZone("Europe/Chisinau");
        simpleDateFormatMD.setTimeZone(timeZoneMD);
        this.adapterData = data;
    }


    @NonNull
    @Override
    public ViewHolderString onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rc_history_list, parent, false);
        return new ViewHolderString(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderString holder, int position) {

        History string  = getItem(position);

        holder.bind(string);
    }


    class ViewHolderString extends RecyclerView.ViewHolder{
        private TextView action;
        private TextView message;
        private TextView date;
        private TextView colors;

        ViewHolderString(View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.txt_date_history);
            colors = itemView.findViewById(R.id.textView_colors);
            action = itemView.findViewById(R.id.txt_action_history);
            message = itemView.findViewById(R.id.txt_msg_history);

        }

        private void bind(History history) {

            date.setText(simpleDateFormatMD.format(history.getDate()));
            message.setText(history.getMsg());

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

            int states = history.getType();
            String actions = "";
            switch (states){
                case BaseEnum.History_CreateBill: actions = "Created bill"; break;
                case BaseEnum.History_DeletedBill: actions = "Deleted bill"; break;
                case BaseEnum.History_ClosedBill: actions = "Closed bill"; break;
                case BaseEnum.History_OpenShift: actions = "Open shift"; break;
                case BaseEnum.History_ClosedShift: actions = "Closed shift"; break;
                case BaseEnum.History_RecreatBill: actions = "Deletion bill cancel"; break;
                case BaseEnum.History_AddedToBill: actions = "Item added to bill"; break;
                case BaseEnum.History_DeletedFromBill: actions = "Item deleted bill"; break;
                case BaseEnum.History_AddedClientToBill: actions = "Added client to bill"; break;
                case BaseEnum.History_DeletedClientFromBill: actions = "Delete client from bill"; break;
                case BaseEnum.History_DeferredBill: actions = "Bill deferred"; break;
                case BaseEnum.History_AddedDiscount: actions = "Added discount to bill"; break;
                case BaseEnum.History_DeletedDiscount: actions = "Deleted discount from bill"; break;
                case BaseEnum.History_PaymentBill: actions = "Bill payment"; break;
                case BaseEnum.History_Printed_X: actions = "Printed X report"; break;
                case BaseEnum.History_Printed_Z: actions = "Printed Z report"; break;
                case BaseEnum.History_InsertMoneyToDraw: actions = "Insert money to draw"; break;
                case BaseEnum.History_WithdrawMoneyFromDraw: actions = "Withdraw money from draw"; break;
                case BaseEnum.History_UserLogIn: actions = "User log-in"; break;
                case BaseEnum.History_SynchronizationStarted: actions = "Synchronization started"; break;
                case BaseEnum.History_SynchronizationFinish: actions = "Synchronization finish"; break;
                case BaseEnum.History_ChangeItemCount: actions = "Change count for item"; break;
            }
            action.setText(actions);
        }
    }

}
