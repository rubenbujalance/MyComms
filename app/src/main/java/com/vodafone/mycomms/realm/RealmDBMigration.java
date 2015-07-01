package com.vodafone.mycomms.realm;

import io.realm.Realm;
import io.realm.internal.Table;

/**
 * Created by str_rbm on 29/06/2015.
 */
public class RealmDBMigration implements io.realm.RealmMigration {
    @Override
    public long execute(Realm realm, long version) {

        /*
            // Version 0
            class Chat
                String id;
                String firstName;
                String lastName;
                ...
            // Version 1
            class Person
                String fullName;        // combine firstName and lastName into single field
                int age;
        */
        // Migrate from version 0 to version 1
//        if (version == 0) {
//            Table personTable = realm.getTable(Chat.class);
//
//            long fistNameIndex = getIndexForProperty(personTable, "firstName");
//            long lastNameIndex = getIndexForProperty(personTable, "lastName");
//            long fullNameIndex = personTable.addColumn(ColumnType.STRING, "fullName");
//            for (int i = 0; i < personTable.size(); i++) {
//                personTable.setString(fullNameIndex, i, personTable.getString(fistNameIndex, i) + " " +
//                        personTable.getString(lastNameIndex, i));
//            }
//            personTable.removeColumn(getIndexForProperty(personTable, "firstName"));
//            personTable.removeColumn(getIndexForProperty(personTable, "lastName"));
//            version++;
//        }

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
