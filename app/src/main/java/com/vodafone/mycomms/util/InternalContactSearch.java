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


    public InternalContactSearch()
    {
        this.context = null;
    }

    public  InternalContactSearch(Context context)
    {
        this.context = context;
    }

    private Cursor getContactsIdByKeyWordFromName(String keyWord)
    {
        ContentResolver cr = this.context.getContentResolver();
        try
        {
            Uri uri = ContactsContract.Data.CONTENT_URI;
            String[] projection = new String[]
                    {
                            ContactsContract.Data._ID
                    };

            String selection = ContactsContract.Data.DISPLAY_NAME
                    + " like '%"+keyWord+"%' "
                    + " AND "+ ContactsContract.Data.HAS_PHONE_NUMBER+" = 1"
                    ;

            return cr.query(uri, projection, selection, null, ContactsContract.Data._ID+" ASC");
        }
        catch (Exception ex)
        {
            String message = ex.getMessage();
            Log.e("NameCursor", "Error: " + message);
            return null;
        }
    }

    private Cursor getContactsIdByKeyWordFromEmail(String keyWord)
    {
        ContentResolver cr = this.context.getContentResolver();
        try
        {
            Uri uri = ContactsContract.Contacts.CONTENT_URI;
            String[] projection = new String[]
                    {
                            ContactsContract.Contacts._ID
                    };

            String selection = ContactsContract.CommonDataKinds.Email.ADDRESS
                    + " like '%"+keyWord+"%' "
                    + " AND "+ContactsContract.Contacts.Data.MIMETYPE+" = ?";

            String[] selectionArgs = new String[]
                    {
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                    };

            return cr.query(uri, projection, selection, selectionArgs, ContactsContract.Contacts._ID+" ASC");
        }
        catch (Exception ex)
        {
            String message = ex.getMessage();
            Log.e("EmailCursor", "Error: " + message);
            return null;
        }
    }

    private Cursor getContactsIdByKeyWordFromCompanyName(String keyWord)
    {
        ContentResolver cr = this.context.getContentResolver();
        try
        {
            Uri uri = ContactsContract.Contacts.CONTENT_URI;
            String[] projection = new String[]
                    {
                            ContactsContract.Contacts._ID
                    };

            String selection = ContactsContract.CommonDataKinds.Organization.COMPANY
                    + " like '%"+keyWord+"%' "
                    + " AND "+ContactsContract.Contacts.Data.MIMETYPE+" = ?";
            String[] selectionArgs = new String[]
                    {
                            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
                    };
            return cr.query(uri, projection, selection, selectionArgs, ContactsContract.Contacts._ID+" ASC");
        }
        catch (Exception ex)
        {
            String message = ex.getMessage();
            Log.e("CompanyCursor", "Error: " + message);
            return null;
        }
    }

    private ArrayList<String> getContactsIds(String keyWord)
    {
        Cursor cursorByName = getContactsIdByKeyWordFromName(keyWord);
        //Cursor cursorByCompanyName = getContactsIdByKeyWordFromCompanyName(keyWord);
        //Cursor cursorByEmailAddress = getContactsIdByKeyWordFromEmail(keyWord);

        ArrayList<String> distinctIds = new ArrayList<String>();

        if(cursorByName.moveToFirst())
        {
            do
            {
                String id = cursorByName.getString(cursorByName.getColumnIndex(ContactsContract.Data._ID));
                if(!distinctIds.contains(id))
                {
                    distinctIds.add(id);
                }
            }
            while (cursorByName.moveToNext());
        }
        cursorByName.close();

        /*
        if(cursorByEmailAddress.moveToFirst())
        {
            do
            {
                String id = cursorByEmailAddress.getString(cursorByEmailAddress.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                if(!distinctIds.contains(id))
                {
                    distinctIds.add(id);
                }
            }
            while (cursorByEmailAddress.moveToNext());
        }
        cursorByEmailAddress.close();

        if(cursorByCompanyName.moveToFirst())
        {
            do
            {
                String id = cursorByCompanyName.getString(cursorByCompanyName.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                if(!distinctIds.contains(id))
                {
                    distinctIds.add(id);
                }
            }
            while (cursorByCompanyName.moveToNext());
        }
        cursorByCompanyName.close();
        */

        return distinctIds;
    }

    private Contact setContactsCompanyByContactsIds(String id, Contact contact)
    {
        ContentResolver cr = this.context.getContentResolver();
        try
        {
            Uri uri = ContactsContract.Data.CONTENT_URI;
            String[] projection = new String[]
                    {
                            ContactsContract.CommonDataKinds.Organization.COMPANY
                    };

            String selection = ContactsContract.Data._ID +" =? "
                    + " AND "+ContactsContract.Data.MIMETYPE+" = ?";
            String[] selectionArgs = new String[]
                    {
                              id
                            , ContactsContract.CommonDataKinds.Organization.COMPANY
                    };
            Cursor cursor = cr.query(uri, projection, selection, selectionArgs, ContactsContract.Data._ID + " ASC");

            if(cursor.moveToFirst())
            {
                do
                {
                    contact.setCompany(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY)));
                }
                while(cursor.moveToNext());
            }
            return contact;
        }
        catch (Exception ex)
        {
            String message = ex.getMessage();
            Log.e("getCompany", "Error: " + message);
            return null;
        }
    }

    private Contact setContactsNameByContactsIds(String id, Contact contact)
    {
        ContentResolver cr = this.context.getContentResolver();
        try
        {
            Uri uri = ContactsContract.Data.CONTENT_URI;
            String[] projection = new String[]
                    {
                            ContactsContract.Data.DISPLAY_NAME
                    };

            String selection = ContactsContract.Data._ID +" =? ";

            String[] selectionArgs = new String[]
                    {
                            id
                    };
            Cursor cursor = cr.query(uri, projection, selection, selectionArgs, ContactsContract.Data._ID + " ASC");

            if(cursor.moveToFirst())
            {
                do
                {
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
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
                }
                while(cursor.moveToNext());
            }
            cursor.close();
            return contact;
        }
        catch (Exception ex)
        {
            String message = ex.getMessage();
            Log.e("getName", "Error: " + message);
            return null;
        }
    }

    public ArrayList<Contact> getLocalContactsByKeyWord(String keyWord)
    {
        ArrayList<String> ids = getContactsIds(keyWord);
        ArrayList<Contact> contacts = new ArrayList<Contact>();
        Contact contact = null;
        for(String id : ids)
        {
            contact = new Contact("");
            contact.setId("local_contact_"+id);
            contact = setContactsCompanyByContactsIds(id, contact);
            contact = setContactsNameByContactsIds(id, contact);
            contacts.add(contact);
        }
        return contacts;
    }
}
