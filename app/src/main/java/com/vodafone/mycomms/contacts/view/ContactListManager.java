package com.vodafone.mycomms.contacts.view;

import android.content.Context;

import com.vodafone.mycomms.realm.RealmContactTransactions;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import model.Contact;
import model.DummyChatItem;
import model.FavouriteContact;
import model.RecentContact;
import model.RecentItem;

/**
 * Created by str_vig on 28/04/2015.
 */
public class ContactListManager {
    private static ContactListManager instance;

    private List<Contact> contactList = new ArrayList<Contact>();
    private List<RecentContact> recentList = new ArrayList<RecentContact>();
    private List<FavouriteContact> favouriteList = new ArrayList<FavouriteContact>();

    private List<DummyChatItem> chatList = new ArrayList<DummyChatItem>();

    private ContactListManager(){
    }

    private void addRecent(RecentContact item) {
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
        RealmContactTransactions realmContactTransactions = new RealmContactTransactions(realm);
        List<Contact> realmContactList = realmContactTransactions.getAllContacts();

        addChatItem(realmContactList.get(0), "Tue");
        addChatItem(realmContactList.get(1), "4/24/15");
        addChatItem(realmContactList.get(2), "4/19/15");
        addChatItem(realmContactList.get(3), "3/22/15");
        return realmContactList;
    }

    public void setContactList(List<Contact> contactList) {
        this.contactList = contactList;
    }

    public List<RecentContact> getRecentList(Context context, Realm realm) {
        return getRealmRecentContacts(context, realm);
    }

    private List<RecentContact> getRealmRecentContacts(Context context, Realm realm) {
        RealmContactTransactions realmContactTransactions = new RealmContactTransactions(realm);
        return realmContactTransactions.getAllRecentContacts();
    }

    public List<FavouriteContact> getFavouriteList(Context context, Realm realm) {
        return getRealmFavouriteContacts(context, realm);
    }

    private List<FavouriteContact> getRealmFavouriteContacts(Context context, Realm realm) {
        RealmContactTransactions realmContactTransactions = new RealmContactTransactions(realm);
        return realmContactTransactions.getAllFavouriteContacts();
    }

    public void setRecentList(List<RecentContact> recentList) {
        this.recentList = recentList;
    }

    public List<DummyChatItem> getChatList() {
        return chatList;
    }

    public void setChatList(List<DummyChatItem> chatList) {
        this.chatList = chatList;
    }


}
