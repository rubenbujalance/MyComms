package model;

/**
 * Created by str_vig on 05/05/2015.
 */
public class RecentItem {

    private RecentItemType itemType;

    private Contact contact;

    private String recentEventTime;

    public RecentItem(Contact contact, RecentItemType itemType, String recentEventTime){
        this.setItemType(itemType);
        this.setContact(contact);
        this.setRecentEventTime(recentEventTime);
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

    public String getRecentEventTime() {
        return recentEventTime;
    }

    public void setRecentEventTime(String recentEventTime) {
        this.recentEventTime = recentEventTime;
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
