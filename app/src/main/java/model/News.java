package model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class News extends RealmObject{

    @PrimaryKey
    private String uuid;

    private String title;
    private String html;
    private String image;
    private String link;
    private String author_name;
    private String author_avatar;
    private String stringField1;
    private String stringField2;
    private String stringField3;
    private String stringField4;
    private String stringField5;
    private String stringField6;
    private String stringField7;
    private String stringField8;
    private String stringField9;
    private long created_at;
    private long updated_at;
    private long published_at;
    private long longField1;
    private long longField2;
    private long longField3;
    private long longField4;
    private long longField5;
    private long longField6;
    private long longField7;
    private long longField8;
    private long longField9;

    public News(){
        this.uuid = uuid;
        this.title = title;
        this.html = html;
        this.image = image;
        this.link = link;
        this.author_name = author_name;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.published_at = published_at;
        this.stringField1 = stringField1;
        this.stringField2 = stringField2;
        this.stringField3 = stringField3;
        this.stringField4 = stringField4;
        this.stringField5 = stringField5;
        this.stringField6 = stringField6;
        this.stringField7 = stringField7;
        this.stringField8 = stringField8;
        this.stringField9 = stringField9;
        this.longField1 = longField1;
        this.longField2 = longField2;
        this.longField3 = longField3;
        this.longField4 = longField4;
        this.longField5 = longField5;
        this.longField6 = longField6;
        this.longField7 = longField7;
        this.longField8 = longField8;
        this.longField9 = longField9;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getAuthor_name() {
        return author_name;
    }

    public void setAuthor_name(String author_name) {
        this.author_name = author_name;
    }

    public String getAuthor_avatar() {
        return author_avatar;
    }

    public void setAuthor_avatar(String author_avatar) {
        this.author_avatar = author_avatar;
    }

    public long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    public long getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(long updated_at) {
        this.updated_at = updated_at;
    }

    public long getPublished_at() {
        return published_at;
    }

    public void setPublished_at(long published_at) {
        this.published_at = published_at;
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
