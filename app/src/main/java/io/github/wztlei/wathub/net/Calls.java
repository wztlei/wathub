package io.github.wztlei.wathub.net;

import android.os.Handler;
import android.os.Looper;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Calls {

    private Calls() {
        throw new AssertionError();
    }

    public static <T> Call<T> wrap(final T obj) {
        return new InternalCall<>(obj);
    }

    public static <T> T unwrap(final Call<T> call) {
        try {
            return call.execute().body();
        } catch (Exception e) {
            return null;
        }
    }

    private static final class InternalCall<T> implements Call<T> {

        private static final Handler sHandler = new Handler(Looper.getMainLooper());

        private final T mObj;

        private InternalCall(final T obj) {
            mObj = obj;
        }

        @Override
        public Response<T> execute() {
            return Response.success(mObj);
        }

        @Override
        public void enqueue(final Callback<T> callback) {
            // Yield execution to caller before returning success
            sHandler.post(() -> callback.onResponse(InternalCall.this, execute()));
        }

        @Override
        public boolean isExecuted() {
            return true;
        }

        @Override
        public void cancel() {}

        @Override
        public boolean isCanceled() {
            return false;
        }

        @SuppressWarnings("CloneDoesntCallSuperClone")
        @Override
        public Call<T> clone() {
            return new InternalCall<>(mObj);
        }

        @Override
        public Request request() {
            return new Request.Builder().build();
        }
    }
}
