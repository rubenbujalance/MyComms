package model;

/**
 * Created by str_vig on 05/05/2015.
 */
public class RecentItem {

    private RecentItemType itemType;

    private Contact contact;

    public RecentItem(Contact contact, RecentItemType itemType){
        this.setItemType(itemType);
        this.setContact(contact);
    }

    public RecentItemType getItemType() {
        return itemType;
    }

    public void setItemType(RecentItemType itemType) {
        this.itemType = itemType;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public enum RecentItemType {MAIL, CHAT, CALL}

    boolean isMail(){
        if(this.getItemType() == RecentItemType.MAIL){
            return true;
        }
        return false;
    }

    boolean isChat(){
        if(this.getItemType() == RecentItemType.CHAT){
            return true;
        }
        return false;
    }


    boolean isCall(){
        if(this.getItemType() == RecentItemType.CALL){
            return true;
        }
        return false;
    }

}
