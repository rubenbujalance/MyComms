package model;

/**
 * Created by str_vig on 05/05/2015.
 */
public class DummyChatItem {

    private String lastMessage = "Me: And I say Hello Hello Hello!";
    private Contact contact;

    public DummyChatItem(Contact contact){
        this.setContact(contact);
    }



    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public String getLastMessage() {
        return lastMessage;
    }


}
