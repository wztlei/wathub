package com.deange.uwaterlooapi.model.important_dates;

import android.os.Parcel;
import android.os.Parcelable;

import com.deange.uwaterlooapi.model.BaseModel;
import com.deange.uwaterlooapi.utils.DateUtils;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class ImportantDatesDetails extends BaseModel implements Parcelable, Comparable<ImportantDatesDetails> {

    @SerializedName("id")
    int mId;

    @SerializedName("title")
    String mTitle;

    @SerializedName("body")
    String mBody;

    @SerializedName("body_raw")
    String mBodyRaw;

    @SerializedName("special_notes")
    String mSpecialNotes;

    @SerializedName("special_notes_raw")
    String mSpecialNotesRaw;

    @SerializedName("audience")
    List<String> mAudience;

    @SerializedName("term")
    String mTerm;

    @SerializedName("term_id")
    int mTermId;

    @SerializedName("start_date")
    String mStartDate;

    @SerializedName("end_date")
    String mEndDate;

    @SerializedName("date_tbd")
    Boolean mDateTbd;

    @SerializedName("date_na")
    Boolean mDateNa;

    @SerializedName("link")
    String mLink;

    @SerializedName("site")
    String mSite;

    @SerializedName("vid")
    int mVid;

    @SerializedName("updated")
    String mUpdated;


    protected ImportantDatesDetails(final Parcel in) {
        super(in);
        mId = in.readInt();
        mTitle = in.readString();
        mBody = in.readString();
        mBodyRaw = in.readString();
        mSpecialNotes = in.readString();
        mSpecialNotesRaw = in.readString();
        mAudience = in.createStringArrayList();
        mTerm = in.readString();
        mTermId = in.readInt();
        mStartDate = in.readString();
        mEndDate = in.readString();
// two more things
        mLink = in.readString();
        mSite = in.readString();
        mVid = in.readInt();
        mUpdated = in.readString();

    }

    public static final Creator<ImportantDatesDetails> CREATOR = new Creator<ImportantDatesDetails>() {
        @Override
        public ImportantDatesDetails createFromParcel(final Parcel in) {
            return new ImportantDatesDetails(in);
        }

        @Override
        public ImportantDatesDetails[] newArray(final int size) {
            return new ImportantDatesDetails[size];
        }
    };


    // resolve
    public int compareTo(final ImportantDatesDetails another) {
        return 1;
    }

}
