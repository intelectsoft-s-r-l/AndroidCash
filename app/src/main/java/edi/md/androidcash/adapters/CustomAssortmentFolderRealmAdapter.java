package edi.md.androidcash.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import edi.md.androidcash.R;
import edi.md.androidcash.RealmHelper.AssortmentRealm;
import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;

/**
 * Created by Igor on 24.12.2019
 */

public class CustomAssortmentFolderRealmAdapter extends RealmBaseAdapter<AssortmentRealm> implements ListAdapter {

    private static class ViewHolder {
        TextView folderName;
    }

    public CustomAssortmentFolderRealmAdapter(@Nullable OrderedRealmCollection<AssortmentRealm> data) {
        super(data);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null){
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());

                convertView = inflater.inflate(R.layout.item_listview_asortiment_folder,parent,false);

                viewHolder.folderName = convertView.findViewById(R.id.txtFolderName);

                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (adapterData != null) {
                viewHolder.folderName.setText(adapterData.get(position).getName());
            }
        return convertView;
    }
}
