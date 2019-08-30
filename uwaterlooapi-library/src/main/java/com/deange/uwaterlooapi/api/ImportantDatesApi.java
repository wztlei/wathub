package com.deange.uwaterlooapi.api;

import com.deange.uwaterlooapi.model.common.Responses;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ImportantDatesApi {
    /**
     * This method returns all important dates for the requested term.
     * @return list of important dates for the term
     */
    @GET("terms/{term_id}/importantdates.json")
    Call<Responses.ImportantDates> getImportantDates(@Path("term_id") int term_id );
}
