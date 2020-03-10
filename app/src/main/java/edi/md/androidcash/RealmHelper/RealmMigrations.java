package edi.md.androidcash.RealmHelper;
import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * Created by Igor on 20.12.2019
 */

public class RealmMigrations implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        final RealmSchema schema = realm.getSchema();
        if(oldVersion == 1){
            schema.get("Bill").addField("expanded",boolean.class);
            oldVersion++;
        }
        if (oldVersion == 2){
            schema.create("History").addField("type",int.class).addField("msg",String.class).addField("date", long.class);
        }

    }
}
