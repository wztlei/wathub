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
       //  mDateTbd = in.readBoo
        mLink = in.readString();
        mSite = in.readString();
        mVid = in.readInt();
        mUpdated = in.readString();

    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(mId);
        dest.writeString(mTitle);
        dest.writeString(mBody);
        dest.writeString(mBodyRaw);
        dest.writeString(mSpecialNotes);
        dest.writeString(mSpecialNotesRaw);
       //dest.writeStringArray(mAudience);
        dest.writeString(mTerm);
        dest.writeInt(mTermId);
        dest.writeString(mStartDate);
        dest.writeString(mEndDate);
        // two more things
        dest.writeString(mLink);
        dest.writeString(mSite);
        dest.writeInt(mVid);
        dest.writeString(mUpdated);
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


    public int getId() { return mId; }

    public String getTitle() { return mTitle; }

    public String getBody() { return mBody; }

    public String getStartDate() { return mStartDate; }

    public String getEndDate() { return mEndDate; }


    public int compareTo(final ImportantDatesDetails another) {
        // change this
        return getStartDate().compareTo(another.getStartDate());
    }

}
