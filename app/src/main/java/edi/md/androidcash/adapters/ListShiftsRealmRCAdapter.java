package edi.md.androidcash.adapters;

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

import edi.md.androidcash.R;
import edi.md.androidcash.RealmHelper.Shift;
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

        Shift string  = getItem(position);

        holder.bind(string);
    }


    class ViewHolderString extends RecyclerView.ViewHolder{
        private TextView name;
        private TextView nr_bill;
        private TextView opened_by;
        private TextView closed_by;
        private TextView openDate;
        private TextView closeDate;
        private TextView colors;
        private TextView sended;

        ViewHolderString(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.txt_name_shift);
            colors = itemView.findViewById(R.id.textView_colors_shift);
            nr_bill = itemView.findViewById(R.id.txt_count_bill);
            opened_by = itemView.findViewById(R.id.txt_shift_list_opened_by);
            closed_by = itemView.findViewById(R.id.txt_shift_list_closed_by);
            openDate = itemView.findViewById(R.id.txt_shift_list_open_date);
            closeDate = itemView.findViewById(R.id.txt_shift_list_closed_date);
            sended = itemView.findViewById(R.id.txt_shift_list_sended);
        }

        private void bind(Shift shift) {
            name.setText(shift.getName());
            openDate.setText(simpleDateFormatMD.format(shift.getStartDate()));
            if(shift.getEndDate() == 0)
                closeDate.setText("-");
            else
                closeDate.setText(simpleDateFormatMD.format(shift.getEndDate()));
            opened_by.setText(shift.getAuthorName());
            closed_by.setText(shift.getClosedByName());
            nr_bill.setText(String.valueOf(shift.getBillCounter()));

            if(shift.isClosed()){
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
                Drawable background = sended.getBackground();

                if (background instanceof ShapeDrawable) {
                    Random rnd = new Random();
                    int color = Color.rgb(0,204,92);
                    ((ShapeDrawable)background).getPaint().setColor(color);
                    sended.setBackground(background);
                }
                else if (background instanceof GradientDrawable) {
                    Random rnd = new Random();
                    int color = Color.rgb(0,204,92);
                    ((GradientDrawable)background).setColor(color);
                    sended.setBackground(background);
                }
                else if (background instanceof ColorDrawable) {
                    Random rnd = new Random();
                    int color = Color.rgb(0,204,92);
                    ((ColorDrawable)background).setColor(color);
                    sended.setBackground(background);
                }
            }
            else{
                Drawable background = sended.getBackground();

                if (background instanceof ShapeDrawable) {
                    Random rnd = new Random();
                    int color = Color.rgb(204,20,0);
                    ((ShapeDrawable)background).getPaint().setColor(color);
                    sended.setBackground(background);
                }
                else if (background instanceof GradientDrawable) {
                    Random rnd = new Random();
                    int color = Color.rgb(204,20,0);
                    ((GradientDrawable)background).setColor(color);
                    sended.setBackground(background);
                }
                else if (background instanceof ColorDrawable) {
                    Random rnd = new Random();
                    int color = Color.rgb(204,20,0);
                    ((ColorDrawable)background).setColor(color);
                    sended.setBackground(background);
                }
            }

        }
    }

}
