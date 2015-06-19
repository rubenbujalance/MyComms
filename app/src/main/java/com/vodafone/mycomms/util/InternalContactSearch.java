package com.vodafone.mycomms.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;

import model.Contact;

/**
 * Created by str_oan on 05/06/2015.
 */
public class InternalContactSearch
{
    private Context context;
    private String profileId;


    public InternalContactSearch()
    {
        this.context = null;
    }

    public  InternalContactSearch(Context context, String profileId)
    {
        this.context = context;
        this.profileId = profileId;
    }

    /**
     * Loads all contacts from Local DB by key word storing them into ArrayList of contacts
     * @author str_oan
     * @param keyWord (String) ->  key word passed as a parameter
     * @return (ArrayList Contact ) -> list of found contacts
     */
    public ArrayList<Contact> getLocalContactsByKeyWord(String keyWord)
    {
        ArrayList<String> ids = getContactsIds(keyWord);
        ArrayList<Contact> contacts = new ArrayList<>();
        Contact contact;
        for(String id : ids)
        {
            //TODO ALEX - Filter by profile id
            contact = new Contact("");
            contact.setId("local_contact_"+profileId+"_"+id);
            contact.setContactId("local_contact_" + id);
            contact.setPlatform(Constants.PLATFORM_LOCAL);
            contact.setProfileId(profileId);
            contact = setContactsCompanyDataByContactsIds(id, contact);
            contact = setContactsBasicDataByContactsIds(id, contact);
            contact = setContactsEmailDataByContactsIds(id, contact);
            contact = setContactsPhoneDataByContactsIds(id, contact);
            contacts.add(contact);
        }
        return contacts;
    }

    /**
     * Sets Contact Company data (if any) into the passed Contact
     * @author str_oan
     * @param id (String)-> contact id
     * @param contact (Contact) -> contact where data will be added
     * @return (Contact) -> the input Contact with new info
     */
    private Contact setContactsCompanyDataByContactsIds(String id, Contact contact)
    {
        try
        {
            Cursor cursor = getCompanyDataByContactId(id);
            if(cursor.moveToFirst())
            {
                do
                {
                    contact.setCompany(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY)));
                    contact.setOfficeLocation(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION)));
                    contact.setPosition(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION)));
                }
                while(cursor.moveToNext());
            }
            cursor.close();
            return contact;
        }
        catch (Exception ex)
        {
            String message = ex.getMessage();
            Log.e(Constants.TAG, "setContactsCompanyByContactsIds() -> ERROR: " + message);
            return contact;
        }
    }

    /**
     * Sets Contact Basic data (if any) into the passed Contact
     * @author str_oan
     * @param id (String)-> contact id
     * @param contact (Contact) -> contact where data will be added
     * @return (Contact) -> the input Contact with new info
     */
    private Contact setContactsBasicDataByContactsIds(String id, Contact contact)
    {
        try
        {
            Cursor cursor = getContactBasicDataByContactId(id);
            if(cursor.moveToFirst())
            {
                do
                {
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String[] splitName = name.split(" ");
                    if(splitName.length > 1)
                    {
                        contact.setFirstName(splitName[0]);
                        contact.setLastName(splitName[1]);
                    }
                    else
                    {
                        contact.setFirstName(splitName[0]);
                    }

                    contact.setLastSeen(Long.parseLong(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LAST_TIME_CONTACTED))));
                }
                while(cursor.moveToNext());
            }
            cursor.close();
            return contact;
        }
        catch (Exception ex)
        {
            String message = ex.getMessage();
            Log.e(Constants.TAG, "setContactsBasicDataByContactsIds() -> ERROR: " + message);
            return contact;
        }
    }

    /**
     * Sets Contact Email data (if any) into the passed Contact
     * @author str_oan
     * @param id (String)-> contact id
     * @param contact (Contact) -> contact where data will be added
     * @return (Contact) -> the input Contact with new info
     */
    private Contact setContactsEmailDataByContactsIds(String id, Contact contact)
    {
        try
        {
            Cursor cursor = getContactEmailDataByContactId(id);
            if(cursor.moveToFirst())
            {
                do
                {
                    contact.setEmails(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)));
                }
                while(cursor.moveToNext());
            }
            cursor.close();
            return contact;
        }
        catch (Exception ex)
        {
            String message = ex.getMessage();
            Log.e(Constants.TAG, "setContactsEmailDataByContactsIds() -> ERROR: " + message);
            return contact;
        }
    }

    /**
     * Sets Contact Phone data (if any) into the passed Contact
     * @author str_oan
     * @param id (String)-> contact id
     * @param contact (Contact) -> contact where data will be added
     * @return (Contact) -> the input Contact with new info
     */
    private Contact setContactsPhoneDataByContactsIds(String id, Contact contact)
    {
        try
        {
            Cursor cursor = getContactPhoneDataByContactId(id);
            if(cursor.moveToFirst())
            {
                do
                {
                    String phone = cursor.getString(cursor.getColumnIndex(ContactsContract
                            .CommonDataKinds.Phone.NORMALIZED_NUMBER)).replace(" ", "");
                    contact.setPhones(phone);
                }
                while(cursor.moveToNext());
            }
            cursor.close();
            return contact;
        }
        catch (Exception ex)
        {
            String message = ex.getMessage();
            Log.e(Constants.TAG, "setContactsPhoneDataByContactsIds() -> ERROR: " + message);
            return contact;
        }
    }

    /**
     * Gets distinct contact ids from different kind of Cursor such as given by Contact Name,
     * Contact Email, Contact Company by given key word
     * @author str_oan
     * @param keyWord (String) -> -> key word passed as a parameter
     * @return (ArrayList String ) -> list of unique contact IDS
     */
    private ArrayList<String> getContactsIds(String keyWord)
    {
        Cursor cursorByName = getContactsIdsByKeyWordFromName(keyWord);
        Cursor cursorByCompanyName = getContactsIdsByKeyWordFromCompanyName(keyWord);
        Cursor cursorByEmailAddress = getContactsIdsByKeyWordFromEmail(keyWord);

        ArrayList<String> distinctIds = new ArrayList<>();

        if(null != cursorByName && cursorByName.moveToFirst())
        {
            do
            {
                String id = cursorByName
                        .getString(cursorByName.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                if(!distinctIds.contains(id))
                {
                    distinctIds.add(id);
                }
            }
            while (cursorByName.moveToNext());
        }
        if(null != cursorByName && !cursorByName.isClosed())
        {
            cursorByName.close();
        }


        if(null != cursorByEmailAddress && cursorByEmailAddress.moveToFirst())
        {
            do
            {
                String id = cursorByEmailAddress
                        .getString(cursorByEmailAddress.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                if(!distinctIds.contains(id))
                {
                    distinctIds.add(id);
                }
            }
            while (cursorByEmailAddress.moveToNext());
        }
        if(null != cursorByEmailAddress && !cursorByEmailAddress.isClosed())
        {
            cursorByEmailAddress.close();
        }


        if(null != cursorByCompanyName && cursorByCompanyName.moveToFirst())
        {
            do
            {
                String id = cursorByCompanyName
                        .getString(cursorByCompanyName.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                if(!distinctIds.contains(id))
                {
                    distinctIds.add(id);
                }
            }
            while (cursorByCompanyName.moveToNext());
        }
        if(null != cursorByCompanyName && !cursorByCompanyName.isClosed())
        {
            cursorByCompanyName.close();
        }


        return distinctIds;
    }

    /**
     * Gets Cursor of contact_ids by given key word considered as CONTACT NAME from
     * ContactsContract.Contacts table
     * @author str_oan
     * @param keyWord (String) -> key word passed as a parameter
     * @return (Cursor) -> ids of contacts if any, otherwise empty Cursor
     */
    private Cursor getContactsIdsByKeyWordFromName(String keyWord)
    {
        ContentResolver cr = this.context.getContentResolver();
        try
        {
            Uri uri = ContactsContract.Data.CONTENT_URI;
            String[] projection = new String[]
                    {
                            ContactsContract.Data.CONTACT_ID
                    };

            String selection = ContactsContract.Data.DISPLAY_NAME
                    + " like '%"+keyWord+"%' "
                    + " AND "+ContactsContract.Data.HAS_PHONE_NUMBER+" = 1"
                    + " AND "+ContactsContract.Data.IN_VISIBLE_GROUP+" = '1'"
                    ;

            return cr.query(uri, projection, selection, null, ContactsContract.Data.CONTACT_ID+" ASC");
        }
        catch (Exception ex)
        {
            String message = ex.getMessage();
            Log.e(Constants.TAG, "getContactsIdsByKeyWordFromName() -> ERROR: " + message);
            return null;
        }
    }
    /**
     * Gets Cursor of contact_ids by given key word considered as EMAIL ADDRESS from
     * ContactsContract.Contacts table
     * @author str_oan
     * @param keyWord (String) -> key word passed as a parameter
     * @return (Cursor) -> ids of contacts if any, otherwise empty Cursor
     */
    private Cursor getContactsIdsByKeyWordFromEmail(String keyWord)
    {
        ContentResolver cr = this.context.getContentResolver();
        try
        {
            Uri uri = ContactsContract.Data.CONTENT_URI;
            String[] projection = new String[]
                    {
                            ContactsContract.Data.CONTACT_ID
                    };

            String selection = ContactsContract.CommonDataKinds.Email.ADDRESS
                    + " like '%"+keyWord+"%' "
                    + " AND "+ContactsContract.Contacts.Data.MIMETYPE+" = ?"
                    + " AND "+ContactsContract.Data.CONTACT_ID+ " IS NOT NULL "
                    ;

            String[] selectionArgs = new String[]
                    {
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                    };

            return cr.query
                    (
                            uri
                            , projection
                            , selection
                            , selectionArgs
                            , ContactsContract.Data.CONTACT_ID+" ASC"
                    );
        }
        catch (Exception ex)
        {
            String message = ex.getMessage();
            Log.e(Constants.TAG, "getContactsIdsByKeyWordFromEmail() -> ERROR: " + message);
            return null;
        }
    }

    /**
     * Gets Cursor of contact_ids by given key word considered as COMPANY NAME from
     * ContactsContract.Contacts table
     * @author str_oan
     * @param keyWord (String) -> key word passed as a parameter
     * @return (Cursor) -> ids of contacts if any, otherwise empty Cursor
     */
    private Cursor getContactsIdsByKeyWordFromCompanyName(String keyWord)
    {
        ContentResolver cr = this.context.getContentResolver();
        try
        {
            Uri uri = ContactsContract.Data.CONTENT_URI;
            String[] projection = new String[]
                    {
                            ContactsContract.Data.CONTACT_ID
                    };

            String selection = ContactsContract.CommonDataKinds.Organization.COMPANY
                    + " like '%"+keyWord+"%' "
                    + " AND "+ContactsContract.Contacts.Data.MIMETYPE+" = ?";
            String[] selectionArgs = new String[]
                    {
                            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
                    };
            return cr.query
                    (
                            uri
                            , projection
                            , selection
                            , selectionArgs
                            , ContactsContract.Data.CONTACT_ID+" " + "ASC"
                    );
        }
        catch (Exception ex)
        {
            String message = ex.getMessage();
            Log.e(Constants.TAG, "getContactsIdsByKeyWordFromCompanyName() -> ERROR: " + message);
            return null;
        }
    }

    /**
     * Gets Company Info by Contact ID
     * @author str_oan
     * @param id (String) -> contact id passed as a String
     * @return (Cursor) -> company data stored in the Cursor
     */
    private Cursor getCompanyDataByContactId(String id)
    {
        ContentResolver cr = this.context.getContentResolver();
        Uri uri = ContactsContract.Data.CONTENT_URI;
        String[] projection = new String[]
                {
                          ContactsContract.CommonDataKinds.Organization.COMPANY
                        , ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION
                        , ContactsContract.CommonDataKinds.Organization.DEPARTMENT
                        , ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION
                        , ContactsContract.CommonDataKinds.Organization.TITLE
                };

        String selection = ContactsContract.Data.CONTACT_ID +" =? "
                + " AND "+ContactsContract.Data.MIMETYPE+" = ?";
        String[] selectionArgs = new String[]
                {
                        id
                        , ContactsContract.CommonDataKinds.Organization.COMPANY
                };

        return cr.query(uri, projection, selection, selectionArgs, ContactsContract.Data.CONTACT_ID + " " +
                "ASC");
    }

    /**
     * Gets Contact Data Info by Contact ID
     * @author str_oan
     * @param id (String) -> contact id passed as a String
     * @return (Cursor) -> company data stored in the Cursor
     */
    private Cursor getContactBasicDataByContactId(String id)
    {
        ContentResolver cr = this.context.getContentResolver();
        Uri uri = ContactsContract.Data.CONTENT_URI;
        String[] projection = new String[]
                {
                          ContactsContract.Data.DISPLAY_NAME
                        , ContactsContract.Data.LAST_TIME_CONTACTED
                        , ContactsContract.Data.TIMES_CONTACTED
                };

        String selection = ContactsContract.Data.CONTACT_ID +" =? ";

        String[] selectionArgs = new String[]
                {
                        id
                };
        return cr.query
                (
                        uri
                        , projection
                        , selection
                        , selectionArgs
                        , ContactsContract.Data.CONTACT_ID + " ASC"
                );
    }

    /**
     * Gets Contact Email Info by Contact ID
     * @author str_oan
     * @param id (String) -> contact id passed as a String
     * @return (Cursor) -> company data stored in the Cursor
     */
    private Cursor getContactEmailDataByContactId(String id)
    {
        ContentResolver cr = this.context.getContentResolver();
        Uri uri = ContactsContract.Data.CONTENT_URI;
        String[] projection = new String[]
                {
                          ContactsContract.CommonDataKinds.Email.ADDRESS
                };

        String selection = ContactsContract.Data.CONTACT_ID +" =? "
                + " AND "+ContactsContract.Data.MIMETYPE+" = ?";
        String[] selectionArgs = new String[]
                {
                        id
                        , ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                };

        return cr.query
                (
                        uri
                        , projection
                        , selection
                        , selectionArgs
                        , ContactsContract.Data.CONTACT_ID + " " + "ASC"
                );
    }

    /**
     * Gets Contact Phone Info by Contact ID
     * @author str_oan
     * @param id (String) -> contact id passed as a String
     * @return (Cursor) -> company data stored in the Cursor
     */
    private Cursor getContactPhoneDataByContactId(String id)
    {
        ContentResolver cr = this.context.getContentResolver();
        Uri uri = ContactsContract.Data.CONTENT_URI;
        String[] projection = new String[]
                {
                          ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
                };

        String selection = ContactsContract.Data.CONTACT_ID +" =? "
                + " AND "+ContactsContract.Data.MIMETYPE+" = ?";
        String[] selectionArgs = new String[]
                {
                        id
                        , ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                };

        return cr.query
                (
                        uri
                        , projection
                        , selection
                        , selectionArgs
                        , ContactsContract.Data.CONTACT_ID + " " + "ASC"
                );
    }
}
