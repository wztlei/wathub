package io.github.wztlei.wathub.net.feedback;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

// Rolf Li, July 2019

public interface FeedbackInterface {
    /* for future reference (in case of forum change)
    *  after google forum creation, click preview
    *  inspect page url, we find 3 'entry.' fields for the 3 inputs
    * find 'formResponse' to find the input URL
    * in this first case: https://docs.google.com/forms/d/e/1FAIpQLSc3qx_rg_v6uZbzX6c0tw8uhOpsCRgAfH2-FiB1j0hLbIw_mA/formResponse
    * remove everything up to and including e/
    */
    @POST("1FAIpQLSc3qx_rg_v6uZbzX6c0tw8uhOpsCRgAfH2-FiB1j0hLbIw_mA/formResponse")
    @FormUrlEncoded
    Call<Void> fbSend(
            @Field("entry.1283738959") String fb,
            @Field("entry.1667532236") String n,
            @Field("entry.481583657") String em);
}
