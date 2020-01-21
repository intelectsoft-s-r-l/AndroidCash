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
            schema.get("AssortmentRealm")
                    .renameField("vAT","vat")
                    .renameField("iD","id");
            oldVersion++;
        }
        if(oldVersion == 2){
            schema.get("Bill")
                    .renameField("billID","id");
            oldVersion++;
        }
        if(oldVersion == 3){
            schema.get("Promotion")
                    .addField("id",String.class);
            oldVersion++;
        }
        if(oldVersion == 6){
            schema.get("Shift")
                    .addField("billCounter",int.class);
            oldVersion++;
        }
        if(oldVersion == 7){
            schema.get("Shift").setRequired("id",true)
                    .setRequired("workPlaceId",true)
                    .setRequired("author",true);
            oldVersion++;
        }
        if(oldVersion == 8){
            schema.get("Shift").renameField("isActive","isSended");
            oldVersion++;
        }
        if(oldVersion == 9){
            schema.get("Bill").addField("inProcessOfSync",int.class);
            schema.get("BillString").removeField("isSinchronized");
            oldVersion++;
        }
    }
}
