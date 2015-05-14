package com.vodafone.mycomms.contacts.view;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.vodafone.mycomms.R;
import com.vodafone.mycomms.realm.RealmContactTransactions;
import com.vodafone.mycomms.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import model.Contact;
import model.DummyChatItem;
import model.FavouriteContact;
import model.RecentItem;

/**
 * Created by str_vig on 28/04/2015.
 */
public class ContactListManager {
    private static ContactListManager instance;

    private List<Contact> contactList = new ArrayList<Contact>();
    private List<RecentItem> recentList = new ArrayList<RecentItem>();
    private List<FavouriteContact> favouriteList = new ArrayList<FavouriteContact>();

    private List<DummyChatItem> chatList = new ArrayList<DummyChatItem>();

    private ContactListManager(){
        /*addContact(new Contact("001","Captain", "Planet", "Saviour of the planet" , "Planet Earth" , "The planeteers", true, "15:30" , "Planet Earth" , true,true));
        addContact(new Contact("002", "Bat"   , "Man"   , "The Batman"            , "Gotham City"  , "Wayne Corp.",  true, "16:50" , "USA" , false, false));
        addContact(new Contact("003", "Super" , "Man", "The Superman"            , "Metropolis" ,   "Daily Planet", false , "01:30", "USA" , false, false));
        addContact(new Contact("004", "Super" , "Tom", "House cat"            , "Tom and Jerry City" ,   "Hanna-Barbera", false, "01:50", "USA", false, false));
        addContact(new Contact("005", "Super" , "Jerry", "Mouse"            , "Tom and Jerry City" ,   "Hanna-Barbera", true , "09:15", "USA", true, false ));
*/
    }

    private void addRecent(RecentItem item) {
        recentList.add(item);
    }

    private void addChatItem(Contact contact, String lastEventTime) {
        chatList.add(new DummyChatItem(contact, lastEventTime));
    }

    synchronized public static ContactListManager getInstance(){
        if(instance == null){
            instance = new ContactListManager();
        }
        return instance;
    }

    public void addContact(Contact contact){
        contactList.add(contact);
        /*if(contact.isFavourite()){
            favouriteList.add(contact);
        }*/

    }
    public List<Contact> getContactList(Context context, Realm realm) {
        //return contactList;
        return getRealmContacts(context, realm);
    }

    private List<Contact> getRealmContacts(Context context, Realm realm) {
        RealmContactTransactions realmContactTransactions = new RealmContactTransactions(realm, context);
        return realmContactTransactions.getAllContacts();
    }

    public void setContactList(List<Contact> contactList) {
        this.contactList = contactList;
    }

    public List<RecentItem> getRecentList(Context context, Realm realm) {
        return recentList;
    }

    public List<FavouriteContact> getFavouriteList(Context context, Realm realm) {
        return getRealmFavouriteContacts(context, realm);
    }

    private List<FavouriteContact> getRealmFavouriteContacts(Context context, Realm realm) {
        RealmContactTransactions realmContactTransactions = new RealmContactTransactions(realm, context);
        return realmContactTransactions.getAllFavouriteContacts();
    }

    public void setRecentList(List<RecentItem> recentList) {
        this.recentList = recentList;
    }

    public List<DummyChatItem> getChatList() {
        return chatList;
    }

    public void setChatList(List<DummyChatItem> chatList) {
        this.chatList = chatList;
    }

    public void loadFakeContacts(Context context, Realm realm) {
        RealmContactTransactions realmContactTransactions = new RealmContactTransactions(realm, context);
        if (realmContactTransactions.getAllContacts().size() == 0){
            InputStream stream = null;
            stream = context.getResources().openRawResource(R.raw.test_contacts);
            JsonElement json = new JsonParser().parse(new InputStreamReader(stream));
            JSONObject jsonObject = null;
            JSONArray jsonArray = null;
            try {
                jsonObject = new JSONObject(json.toString());
                jsonArray = jsonObject.getJSONArray("data");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                ArrayList<Contact> realmContactList = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {

                    jsonObject = jsonArray.getJSONObject(i);
                    Log.i(Constants.TAG, "ContactListManager.loadFakeContacts: jsonObject: "
                            + jsonObject.getString("id"));
                    Contact contact = new Contact();
                    if (!jsonObject.isNull("id")) contact.setId(jsonObject.getString("id"));
                    if (!jsonObject.isNull("platform"))
                        contact.setPlatform(jsonObject.getString("platform"));
                    if (!jsonObject.isNull("firstName"))
                        contact.setFirstName(jsonObject.getString("firstName"));
                    if (!jsonObject.isNull("lastName"))
                        contact.setLastName(jsonObject.getString("lastName"));
                    if (!jsonObject.isNull("avatar"))
                        contact.setAvatar(jsonObject.getString("avatar"));
                    if (!jsonObject.isNull("position"))
                        contact.setPosition(jsonObject.getString("position"));
                    if (!jsonObject.isNull("company"))
                        contact.setCompany(jsonObject.getString("company"));
                    if (!jsonObject.isNull("timezone"))
                        contact.setTimezone(jsonObject.getString("timezone"));
                    if (!jsonObject.isNull("lastSeen"))
                        contact.setLastSeen(jsonObject.getInt("lastSeen"));
                    if (!jsonObject.isNull("officeLocation"))
                        contact.setOfficeLocation(jsonObject.getString("officeLocation"));

                    if (!jsonObject.isNull("phones"))
                        contact.setPhones(jsonObject.getJSONArray("phones").toString());
                    if (!jsonObject.isNull("emails"))
                        contact.setEmails(jsonObject.getJSONArray("emails").toString());

                    realmContactList.add(contact);

                }
                addRecent(new RecentItem(realmContactList.get(0), RecentItem.RecentItemType.MAIL , "5 min"));
                addRecent(new RecentItem(realmContactList.get(0) , RecentItem.RecentItemType.CALL , "4 hours"));
                addRecent(new RecentItem(realmContactList.get(0), RecentItem.RecentItemType.CHAT, "8 hours" ));
                addRecent(new RecentItem(realmContactList.get(2), RecentItem.RecentItemType.CHAT, "3 days"  ));
                addRecent(new RecentItem(realmContactList.get(3), RecentItem.RecentItemType.CHAT, "4 days"));
                addRecent(new RecentItem(realmContactList.get(1), RecentItem.RecentItemType.MAIL , "5 days"));

                addChatItem(realmContactList.get(0), "Tue");
                addChatItem(realmContactList.get(1), "4/24/15");
                addChatItem(realmContactList.get(2), "4/19/15");
                addChatItem(realmContactList.get(3), "3/22/15");
                realmContactTransactions.insertContactList(realmContactList);
            } catch (JSONException e) {
                Log.e(Constants.TAG, "ContactListManager.loadFakeContacts: " + e.toString());
            }
        }
    }

    public void loadFakeFavouriteContacts(Context context, Realm realm) {
        RealmContactTransactions realmContactTransactions = new RealmContactTransactions(realm, context);
        if (realmContactTransactions.getAllFavouriteContacts().size() == 0){
            InputStream stream = null;
            stream = context.getResources().openRawResource(R.raw.test_contacts);
            JsonElement json = new JsonParser().parse(new InputStreamReader(stream));
            JSONObject jsonObject = null;
            JSONArray jsonArray = null;
            try {
                jsonObject = new JSONObject(json.toString());
                jsonArray = jsonObject.getJSONArray("data");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                ArrayList<FavouriteContact> realmContactList = new ArrayList<>();
                for (int i = 0; i < 2; i++) {

                    jsonObject = jsonArray.getJSONObject(i);
                    Log.i(Constants.TAG, "ContactListManager.loadFakeFavouriteContact: jsonObject: "
                            + jsonObject.getString("id"));
                    FavouriteContact contact = new FavouriteContact();
                    if (!jsonObject.isNull("id")) contact.setId(jsonObject.getString("id"));
                    if (!jsonObject.isNull("platform"))
                        contact.setPlatform(jsonObject.getString("platform"));
                    if (!jsonObject.isNull("firstName"))
                        contact.setFirstName(jsonObject.getString("firstName"));
                    if (!jsonObject.isNull("lastName"))
                        contact.setLastName(jsonObject.getString("lastName"));
                    if (!jsonObject.isNull("avatar"))
                        contact.setAvatar(jsonObject.getString("avatar"));
                    if (!jsonObject.isNull("position"))
                        contact.setPosition(jsonObject.getString("position"));
                    if (!jsonObject.isNull("company"))
                        contact.setCompany(jsonObject.getString("company"));
                    if (!jsonObject.isNull("timezone"))
                        contact.setTimezone(jsonObject.getString("timezone"));
                    if (!jsonObject.isNull("lastSeen"))
                        contact.setLastSeen(jsonObject.getInt("lastSeen"));
                    if (!jsonObject.isNull("officeLocation"))
                        contact.setOfficeLocation(jsonObject.getString("officeLocation"));

                    if (!jsonObject.isNull("phones"))
                        contact.setPhones(jsonObject.getJSONArray("phones").toString());
                    if (!jsonObject.isNull("emails"))
                        contact.setEmails(jsonObject.getJSONArray("emails").toString());

                    realmContactList.add(contact);
                }
                realmContactTransactions.insertFavouriteContactList(realmContactList);
            } catch (JSONException e) {
                Log.e(Constants.TAG, "ContactListManager.loadFakeFavouriteContact: " + e.toString());
            }
        }
    }
}
