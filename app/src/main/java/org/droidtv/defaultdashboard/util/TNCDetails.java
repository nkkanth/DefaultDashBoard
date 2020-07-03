package org.droidtv.defaultdashboard.util;

public class TNCDetails{
    String mISOLanguageCode;
    public String mMessageTitle;
    public String mMessageBody;

    public String getLanguageCode(){
        return mISOLanguageCode;
    }

    public String getMessageTitle(){
        return mMessageTitle;
    }

    public String getMessageBody(){
        return mMessageBody;
    }

    public TNCDetails(String language, String messageTitle, String messageBody) {
        mISOLanguageCode = language;
        mMessageTitle = messageTitle;
        mMessageBody = messageBody;
    }
}

