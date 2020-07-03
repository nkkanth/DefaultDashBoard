package org.droidtv.defaultdashboard.data.model;

import android.graphics.Bitmap;

/**
 * Created by bhargava.gugamsetty on 11-01-2018.
 */

public class GoogleAccount {

    private String mid;
    private String mName;
    private String mGivenName;
    private String mFamilyName;
    private String mLink;
    private Bitmap mPicture;
    private String mLocale;

    public String getId() {
        return mid;
    }

    public void setId(String id) {
        this.mid = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getGivenName() {
        return mGivenName;
    }

    public void setGivenName(String givenName) {
        this.mGivenName = givenName;
    }

    public String getFamilyName() {
        return mFamilyName;
    }

    public void setFamilyName(String familyName) {
        this.mFamilyName = familyName;
    }

    public String getLink() {
        return mLink;
    }

    public void setLink(String link) {
        this.mLink = link;
    }

    public Bitmap getPicture() {
        return mPicture;
    }

    public void setPicture(Bitmap picture) {
        this.mPicture = picture;
    }

    public String getLocale() {
        return mLocale;
    }

    public void setLocale(String locale) {
        this.mLocale = locale;
    }
}
