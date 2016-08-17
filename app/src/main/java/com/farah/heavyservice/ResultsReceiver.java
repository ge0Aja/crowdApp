package com.farah.heavyservice;

        import android.annotation.SuppressLint;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.ResultReceiver;

        import java.sql.ResultSet;

/**
 * Created by Georgi on 8/15/2016.
 */
@SuppressLint("ParcelCreator")
public class ResultsReceiver extends ResultReceiver {
    private Receiver mReceiver;

    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */
    public ResultsReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }
}
