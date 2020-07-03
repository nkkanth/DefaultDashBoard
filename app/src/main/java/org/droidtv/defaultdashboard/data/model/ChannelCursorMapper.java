package org.droidtv.defaultdashboard.data.model;

import android.database.Cursor;

import org.droidtv.htv.provider.HtvContract.HtvChannelList;

import androidx.leanback.database.CursorMapper;

/**
 * Created by sandeep.kumar on 20/11/2017.
 */

public class ChannelCursorMapper extends CursorMapper {

    private static int idIndex;
    private static int mappedIdIndex;
    private static int inputIdIndex;
    private static int mediaTypeIndex;
    private static int serviceTypeIndex;
    private static int typeIndex;
    private static int displayNameIndex;
    private static int displayNumberIndex;
    private static int descriptionIndex;
    private static int logoIndex;
    private static int myChoiceFreePkgIndex;
    private static int myChoicePayPkg1Index;
    private static int myChoicePayPkg2Index;

    @Override
    protected void bindColumns(Cursor cursor) {
        idIndex = cursor.getColumnIndex(HtvChannelList._ID);
        mappedIdIndex = cursor.getColumnIndex(HtvChannelList.COLUMN_MAPPED_ID);
        inputIdIndex = cursor.getColumnIndex(HtvChannelList.COLUMN_INPUT_ID);
        mediaTypeIndex = cursor.getColumnIndex(HtvChannelList.COLUMN_MEDIA_TYPE);
        serviceTypeIndex = cursor.getColumnIndex(HtvChannelList.COLUMN_SERVICE_TYPE);
        typeIndex = cursor.getColumnIndex(HtvChannelList.COLUMN_TYPE);
        displayNameIndex = cursor.getColumnIndex(HtvChannelList.COLUMN_DISPLAY_NAME);
        displayNumberIndex = cursor.getColumnIndex(HtvChannelList.COLUMN_DISPLAY_NUMBER);
        descriptionIndex = cursor.getColumnIndex(HtvChannelList.COLUMN_DESCRIPTION);
        logoIndex = cursor.getColumnIndex(HtvChannelList.COLUMN_LOGO_URL);
        myChoiceFreePkgIndex = cursor.getColumnIndex(HtvChannelList.COLUMN_FREEPKG);
        myChoicePayPkg1Index = cursor.getColumnIndex(HtvChannelList.COLUMN_PAYPKG1);
        myChoicePayPkg2Index = cursor.getColumnIndex(HtvChannelList.COLUMN_PAYPKG2);
    }

    @Override
    protected Object bind(Cursor cursor) {
        int id = cursor.getInt(idIndex);
        int mappedId = cursor.getInt(mappedIdIndex);
        String displayNumber = cursor.getString(displayNumberIndex);
        String inputId = cursor.getString(inputIdIndex);
        String description = cursor.getString(descriptionIndex);
        String serviceType = cursor.getString(serviceTypeIndex);
        String mediaType = cursor.getString(mediaTypeIndex);
        String type = cursor.getString(typeIndex);
        String displayName = cursor.getString(displayNameIndex);
        String logoUrl = cursor.getString(logoIndex);
        int myChoiceFreePkg = cursor.getInt(myChoiceFreePkgIndex);
        int myChoicePayPkg1 = cursor.getInt(myChoicePayPkg1Index);
        int myChoicePayPkg2 = cursor.getInt(myChoicePayPkg2Index);

        return new Channel.Builder(id).
                setMappedId(mappedId).
                setDisplayNumber(displayNumber).
                setInputId(inputId).
                setScrambled(description != null && description.toLowerCase().contains("scrambled")).
                setServiceType(serviceType).
                setMediaType(mediaType).
                setType(type).
                setDisplayName(displayName).
                setLogoUrl(logoUrl).
                setTifChannel(!inputId.contains(Source.NON_THIRD_PARTY_INPUT_ID_PREFIX)).
                setMyChoiceFreePackage(myChoiceFreePkg == 1).
                setMyChoicePackage1(myChoicePayPkg1 == 1).
                setMyChoicePackage2(myChoicePayPkg2 == 1).
                build();
    }
}
