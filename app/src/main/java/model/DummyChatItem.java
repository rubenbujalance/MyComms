package model;

/**
 * Created by str_vig on 05/05/2015.
 */
public class DummyChatItem {

    private String lastMessage = "Me: And I say Hello Hello Hello!";
    private Contact contact;
    private String lastEventDate;

    public String getLastEventDate() {
        return lastEventDate;
    }

    public void setLastEventDate(String lastEventDate) {
        this.lastEventDate = lastEventDate;
    }

    public DummyChatItem(Contact contact, String lastEventDate){
        this.setContact(contact);
        this.setLastEventDate(lastEventDate);
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
