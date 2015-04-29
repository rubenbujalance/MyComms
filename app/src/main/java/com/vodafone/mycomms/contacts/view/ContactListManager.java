package com.vodafone.mycomms.contacts.view;

import java.util.ArrayList;
import java.util.List;

import model.Contact;

/**
 * Created by str_vig on 28/04/2015.
 */
public class ContactListManager {
    private static ContactListManager instance;

    private List<Contact> contactList = new ArrayList<Contact>();
    private List<Contact> recentList = new ArrayList<Contact>();
    private List<Contact> favouriteList = new ArrayList<Contact>();

    private ContactListManager(){
        addContact(new Contact("001","Captain", "Planet", "Saviour of the planet" , "Planet Earth" , "The planeteers", true, "15:30" , "Planet Earth" , true,true));
        addContact(new Contact("002", "Bat"   , "Man"   , "The Batman"            , "Gotham City"  , "Wayne Corp.",  true, "16:50" , "USA" , false, false));
        addContact(new Contact("003", "Super" , "Man", "The Superman"            , "Metropolis" ,   "Daily Planet", false , "01:30", "USA" , false, false));
        addContact(new Contact("004", "Super" , "Tom", "House cat"            , "Tom and Jerry City" ,   "Hanna-Barbera", false, "01:50", "USA", false, false));
        addContact(new Contact("005", "Super" , "Jerry", "Mouse"            , "Tom and Jerry City" ,   "Hanna-Barbera", true , "09:15", "USA", true, false ));
    }

    synchronized public static ContactListManager getInstance(){
        if(instance == null){
            instance = new ContactListManager();
        }
        return instance;
    }

    public void addContact(Contact contact){
        contactList.add(contact);
        if(contact.isFavourite()){
            favouriteList.add(contact);
        }
        if(contact.isRecent()){
            recentList.add(contact);
        }
    }
    public List<Contact> getContactList() {
        return contactList;
    }

    public void setContactList(List<Contact> contactList) {
        this.contactList = contactList;
    }

    public List<Contact> getRecentList() {
        return recentList;
    }

    public List<Contact> getFavouriteList() {
        return favouriteList;
    }

    public void setRecentList(List<Contact> recentList) {
        this.recentList = recentList;
    }
}
