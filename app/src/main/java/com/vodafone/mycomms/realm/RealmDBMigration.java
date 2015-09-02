package com.vodafone.mycomms.realm;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.vodafone.mycomms.util.Constants;

import io.realm.Realm;
import io.realm.RealmMigration;
import io.realm.internal.ColumnType;
import io.realm.internal.Table;
import model.GlobalContactsSettings;

/**
 * Created by str_rbm on 29/06/2015.
 */
public class RealmDBMigration implements RealmMigration {
    @Override
    public long execute(Realm realm, long version) {

        Log.i(Constants.TAG, "RealmDBMigration.execute: Checking migration");

        /*
            // Version 0
            Contacts, Recents, Favourites, Avatars, Chats, GroupChats, ChatsMessages,
            UserProfile, News.

            // Version 1
            NEW REALM CLASS
            class GlobalContactsSettings
                @PrimaryKey
                private String profileId;

                private String user; //This field can contain either username or email, depending on the user
                private String password;
                private String token;
        */
        // Migrate from version 0
        if(version==0) {
            Log.i(Constants.TAG, "RealmDBMigration.execute: Migrating from version 0");

            try {
                Table ldapSettings = realm.getTable(GlobalContactsSettings.class);
                if(ldapSettings.getColumnIndex(Constants.LDAP_SETTINGS_FIELD_PROFILE_ID)==-1) {
                    long columnIndex = ldapSettings.addColumn(
                            ColumnType.STRING, Constants.LDAP_SETTINGS_FIELD_PROFILE_ID);
                    ldapSettings.setPrimaryKey(Constants.LDAP_SETTINGS_FIELD_PROFILE_ID);
                    ldapSettings.addSearchIndex(columnIndex);
                } else if(!ldapSettings.hasPrimaryKey()) {
                    ldapSettings.setPrimaryKey(Constants.LDAP_SETTINGS_FIELD_PROFILE_ID);
                    ldapSettings.addSearchIndex(
                            ldapSettings.getColumnIndex(Constants.LDAP_SETTINGS_FIELD_PROFILE_ID));
                }
                if(ldapSettings.getColumnIndex(Constants.LDAP_SETTINGS_FIELD_USER)==-1)
                    ldapSettings.addColumn(
                        ColumnType.STRING, Constants.LDAP_SETTINGS_FIELD_USER);
                if(ldapSettings.getColumnIndex(Constants.LDAP_SETTINGS_FIELD_PASSWORD)==-1)
                    ldapSettings.addColumn(
                        ColumnType.STRING, Constants.LDAP_SETTINGS_FIELD_PASSWORD);
                if(ldapSettings.getColumnIndex(Constants.LDAP_SETTINGS_FIELD_TOKEN)==-1)
                    ldapSettings.addColumn(
                        ColumnType.STRING, Constants.LDAP_SETTINGS_FIELD_TOKEN);
                if(ldapSettings.getColumnIndex(Constants.LDAP_SETTINGS_FIELD_TOKEN_TYPE)==-1)
                    ldapSettings.addColumn(
                            ColumnType.STRING, Constants.LDAP_SETTINGS_FIELD_TOKEN_TYPE);
                if(ldapSettings.getColumnIndex(Constants.LDAP_SETTINGS_FIELD_URL)==-1)
                    ldapSettings.addColumn(
                            ColumnType.STRING, Constants.LDAP_SETTINGS_FIELD_URL);

                version = 1;
            } catch (Exception e) {
                Log.e(Constants.TAG, "RealmDBMigration.execute: ", e);
                Crashlytics.logException(e);
            }
        }

        // Migrate from version 1
        if(version == 1) {
            Log.i(Constants.TAG, "RealmDBMigration.execute: Migrating from version 1");

            try {
                Table ldapSettings = realm.getTable(GlobalContactsSettings.class);
                if(ldapSettings.getColumnIndex(Constants.LDAP_SETTINGS_FIELD_PROFILE_ID)==-1) {
                    long columnIndex = ldapSettings.addColumn(
                            ColumnType.STRING, Constants.LDAP_SETTINGS_FIELD_PROFILE_ID);
                    ldapSettings.setPrimaryKey(Constants.LDAP_SETTINGS_FIELD_PROFILE_ID);
                    ldapSettings.addSearchIndex(columnIndex);
                } else if(!ldapSettings.hasPrimaryKey()) {
                    ldapSettings.setPrimaryKey(Constants.LDAP_SETTINGS_FIELD_PROFILE_ID);
                    ldapSettings.addSearchIndex(
                            ldapSettings.getColumnIndex(Constants.LDAP_SETTINGS_FIELD_PROFILE_ID));
                }
                if(ldapSettings.getColumnIndex(Constants.LDAP_SETTINGS_FIELD_USER)==-1)
                    ldapSettings.addColumn(
                            ColumnType.STRING, Constants.LDAP_SETTINGS_FIELD_USER);
                if(ldapSettings.getColumnIndex(Constants.LDAP_SETTINGS_FIELD_PASSWORD)==-1)
                    ldapSettings.addColumn(
                            ColumnType.STRING, Constants.LDAP_SETTINGS_FIELD_PASSWORD);
                if(ldapSettings.getColumnIndex(Constants.LDAP_SETTINGS_FIELD_TOKEN)==-1)
                    ldapSettings.addColumn(
                            ColumnType.STRING, Constants.LDAP_SETTINGS_FIELD_TOKEN);
                if(ldapSettings.getColumnIndex(Constants.LDAP_SETTINGS_FIELD_TOKEN_TYPE)==-1)
                    ldapSettings.addColumn(
                            ColumnType.STRING, Constants.LDAP_SETTINGS_FIELD_TOKEN_TYPE);
                if(ldapSettings.getColumnIndex(Constants.LDAP_SETTINGS_FIELD_URL)==-1)
                    ldapSettings.addColumn(
                            ColumnType.STRING, Constants.LDAP_SETTINGS_FIELD_URL);

                version = 1;
            } catch (Exception e) {
                Log.e(Constants.TAG, "RealmDBMigration.execute: ", e);
                Crashlytics.logException(e);
            }
        }

/*        if (version == 0) {
            Table personTable = realm.getTable(Chat.class);

            long fistNameIndex = getIndexForProperty(personTable, "firstName");
            long lastNameIndex = getIndexForProperty(personTable, "lastName");
            long fullNameIndex = personTable.addColumn(ColumnType.STRING, "fullName");
            for (int i = 0; i < personTable.size(); i++) {
                personTable.setString(fullNameIndex, i, personTable.getString(fistNameIndex, i) + " " +
                        personTable.getString(lastNameIndex, i));
            }
            personTable.removeColumn(getIndexForProperty(personTable, "firstName"));
            personTable.removeColumn(getIndexForProperty(personTable, "lastName"));

            version++;
        }*/

        /*
            // Version 2
                class Pet                   // add a new model class
                    String name;
                    String type;
                class Person
                    String fullName;
                    int age;
                    RealmList<Pet> pets;    // add an array property
        */
        // Migrate from version 1 to version 2
//        if (version == 1) {
//            Table personTable = realm.getTable(Person.class);
//            Table petTable = realm.getTable(Pet.class);
//            petTable.addColumn(ColumnType.STRING, "name");
//            petTable.addColumn(ColumnType.STRING, "type");
//            long petsIndex = personTable.addColumnLink(ColumnType.LINK_LIST, "pets", petTable);
//            long fullNameIndex = getIndexForProperty(personTable, "fullName");
//
//            for (int i = 0; i < personTable.size(); i++) {
//                if (personTable.getString(fullNameIndex, i).equals("JP McDonald")) {
//                    personTable.getUncheckedRow(i).getLinkList(petsIndex).add(petTable.add("Jimbo", "dog"));
//                }
//            }
//            version++;
//        }

        /*
            // Version 3
                class Pet
                    String name;
                    int type;               // type becomes int
                class Person
                    String fullName;
                    RealmList<Pet> pets;    // age and pets re-ordered
                    int age;
        */
        // Migrate from version 2 to version 3
//        if (version == 2) {
//            Table petTable = realm.getTable(Pet.class);
//            long oldTypeIndex = getIndexForProperty(petTable, "type");
//            long typeIndex = petTable.addColumn(ColumnType.INTEGER, "type");
//            for (int i = 0; i < petTable.size(); i++) {
//                String type = petTable.getString(oldTypeIndex, i);
//                if (type.equals("dog")) {
//                    petTable.setLong(typeIndex, i, 1);
//                }
//                else if (type.equals("cat")) {
//                    petTable.setLong(typeIndex, i, 2);
//                }
//                else if (type.equals("hamster")) {
//                    petTable.setLong(typeIndex, i, 3);
//                }
//            }
//            petTable.removeColumn(oldTypeIndex);
//            version++;
//        }
        return version;
    }

    private long getIndexForProperty(Table table, String name) {
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (table.getColumnName(i).equals(name)) {
                return i;
            }
        }
        return -1;
    }
}
