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

import md.intelectsoft.salesepos.Fragments.FragmentInformationShift;
import md.intelectsoft.salesepos.R;
import md.intelectsoft.salesepos.RealmHelper.Shift;
import md.intelectsoft.salesepos.ShiftsActivity;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by Igor on 09.03.2020
 */

public class ListShiftsRealmRCAdapter extends RealmRecyclerViewAdapter<Shift, ListShiftsRealmRCAdapter.ViewHolderString> {

    Realm mRealm;
    SimpleDateFormat simpleDateFormatMD;
    TimeZone timeZoneMD;

    protected OrderedRealmCollection<Shift> adapterData;

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public void clear(){
        int size = getItemCount();
        notifyItemRangeRemoved(0, size);
    }

    public ListShiftsRealmRCAdapter(@Nullable OrderedRealmCollection<Shift> data, boolean autoUpdate) {
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

        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rc_shifts_list, parent, false);
        return new ViewHolderString(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderString holder, int position) {

        Shift item  = getItem(position);

        holder.bind(item);

        holder.itemView.setOnClickListener(view -> {
            ShiftsActivity.setViewPagerVisibility(true);
            FragmentInformationShift.updateShift(item);
        });


    }


    class ViewHolderString extends RecyclerView.ViewHolder{
//        private TextView name;
//        private TextView nr_bill;
//        private TextView opened_by;
//        private TextView closed_by;
        private TextView openDate;
//        private TextView closeDate;
        private TextView colors;
        private TextView isClosed;
        private TextView isSync;

        ViewHolderString(View itemView) {
            super(itemView);
            colors = itemView.findViewById(R.id.textView_colors_shift);
            isClosed = itemView.findViewById(R.id.txt_state_shift);
            isSync = itemView.findViewById(R.id.txt_state_sync_shift);
            openDate = itemView.findViewById(R.id.txt_shift_list_open_date);
        }

        private void bind(Shift shift) {
            openDate.setText(simpleDateFormatMD.format(shift.getStartDate()));

            if(shift.isClosed()){
                isClosed.setText("CLOSED");
                isClosed.setTextColor( Color.rgb(0,204,92));
                Drawable background = colors.getBackground();

                if (background instanceof ShapeDrawable) {
                    Random rnd = new Random();
                    int color = Color.rgb(0,204,92);
                    ((ShapeDrawable)background).getPaint().setColor(color);
                    colors.setBackground(background);
                }
                else if (background instanceof GradientDrawable) {
                    Random rnd = new Random();
                    int color = Color.rgb(0,204,92);
                    ((GradientDrawable)background).setColor(color);
                    colors.setBackground(background);
                }
                else if (background instanceof ColorDrawable) {
                    Random rnd = new Random();
                    int color = Color.rgb(0,204,92);
                    ((ColorDrawable)background).setColor(color);
                    colors.setBackground(background);
                }
            }
            else{
                isClosed.setText("OPENED");
                isClosed.setTextColor( Color.rgb(204,20,0));
                Drawable background = colors.getBackground();

                if (background instanceof ShapeDrawable) {
                    Random rnd = new Random();
                    int color = Color.rgb(204,20,0);
                    ((ShapeDrawable)background).getPaint().setColor(color);
                    colors.setBackground(background);
                }
                else if (background instanceof GradientDrawable) {
                    Random rnd = new Random();
                    int color = Color.rgb(204,20,0);
                    ((GradientDrawable)background).setColor(color);
                    colors.setBackground(background);
                }
                else if (background instanceof ColorDrawable) {
                    Random rnd = new Random();
                    int color = Color.rgb(204,20,0);
                    ((ColorDrawable)background).setColor(color);
                    colors.setBackground(background);
                }

            }
            if(shift.isSended()){
                isSync.setText("SYNCHED");
                isSync.setTextColor( Color.rgb(0,204,92));
            }
            else{
                isSync.setText("NOT SYNC");
                isSync.setTextColor( Color.rgb(204,20,0));
            }

        }
    }

}
