package com.projects.kquicho.uwatm8;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.net.Uri;

import java.lang.ref.WeakReference;

/**
 * Created by Kevin Quicho on 3/23/2016.
 */
public class AsyncHandler extends AsyncQueryHandler {
    private WeakReference<AsyncCRUDListener> mListener;

    public interface AsyncCRUDListener {
        void onInsertComplete(int token, Object cookie, Uri uri);

        void onDeleteComplete(int token, Object cookie, int result);
    }

    public AsyncHandler(ContentResolver cr, AsyncCRUDListener listener) {
        super(cr);
        mListener = new WeakReference<AsyncCRUDListener>(listener);
    }

    protected void onInsertComplete(int token, Object cookie, Uri uri) {
        final AsyncCRUDListener listener = mListener.get();
        if (listener != null) {
            listener.onInsertComplete(token, cookie, uri);
        }
    }
    protected void onDeleteComplete(int token, Object cookie, int result) {
        final AsyncCRUDListener listener = mListener.get();
        if (listener != null) {
            listener.onDeleteComplete(token, cookie, result);
        }
    }
}
