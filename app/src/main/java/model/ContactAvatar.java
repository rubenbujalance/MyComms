package model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by str_vig on 28/04/2015.
 */
public class ContactAvatar extends RealmObject {

    @PrimaryKey
    private String contactId;

    private String url;
    private String path;

    private String stringField1;
    private String stringField2;
    private String stringField3;
    private String stringField4;
    private String stringField5;
    private String stringField6;
    private String stringField7;
    private String stringField8;
    private String stringField9;
    private long longField1;
    private long longField2;
    private long longField3;
    private long longField4;
    private long longField5;
    private long longField6;
    private long longField7;
    private long longField8;
    private long longField9;

    public ContactAvatar() {
    }

    public ContactAvatar(String contactId, String url, String pathAvatar) {
        this.contactId = contactId;
        this.url = url;
        this.path = pathAvatar;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String id) {
        this.contactId = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStringField1() {
        return stringField1;
    }

    public void setStringField1(String stringField1) {
        this.stringField1 = stringField1;
    }

    public String getStringField2() {
        return stringField2;
    }

    public void setStringField2(String stringField2) {
        this.stringField2 = stringField2;
    }

    public String getStringField3() {
        return stringField3;
    }

    public void setStringField3(String stringField3) {
        this.stringField3 = stringField3;
    }

    public String getStringField4() {
        return stringField4;
    }

    public void setStringField4(String stringField4) {
        this.stringField4 = stringField4;
    }

    public String getStringField5() {
        return stringField5;
    }

    public void setStringField5(String stringField5) {
        this.stringField5 = stringField5;
    }

    public String getStringField6() {
        return stringField6;
    }

    public void setStringField6(String stringField6) {
        this.stringField6 = stringField6;
    }

    public String getStringField7() {
        return stringField7;
    }

    public void setStringField7(String stringField7) {
        this.stringField7 = stringField7;
    }

    public String getStringField8() {
        return stringField8;
    }

    public void setStringField8(String stringField8) {
        this.stringField8 = stringField8;
    }

    public String getStringField9() {
        return stringField9;
    }

    public void setStringField9(String stringField9) {
        this.stringField9 = stringField9;
    }

    public long getLongField1() {
        return longField1;
    }

    public void setLongField1(long longField1) {
        this.longField1 = longField1;
    }

    public long getLongField2() {
        return longField2;
    }

    public void setLongField2(long longField2) {
        this.longField2 = longField2;
    }

    public long getLongField3() {
        return longField3;
    }

    public void setLongField3(long longField3) {
        this.longField3 = longField3;
    }

    public long getLongField4() {
        return longField4;
    }

    public void setLongField4(long longField4) {
        this.longField4 = longField4;
    }

    public long getLongField5() {
        return longField5;
    }

    public void setLongField5(long longField5) {
        this.longField5 = longField5;
    }

    public long getLongField6() {
        return longField6;
    }

    public void setLongField6(long longField6) {
        this.longField6 = longField6;
    }

    public long getLongField7() {
        return longField7;
    }

    public void setLongField7(long longField7) {
        this.longField7 = longField7;
    }

    public long getLongField8() {
        return longField8;
    }

    public void setLongField8(long longField8) {
        this.longField8 = longField8;
    }

    public long getLongField9() {
        return longField9;
    }

    public void setLongField9(long longField9) {
        this.longField9 = longField9;
    }
}
