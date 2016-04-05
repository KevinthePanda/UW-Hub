package com.projects.kquicho.uwatm8;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;


/**
 * Created by Kevin Quicho on 4/5/2016.
 */
public class RemoveInfoSessionDBService extends IntentService {
    public final static String TAG = "RemoveInfoSessionDBService";
    public final static String ID = "id";


    public RemoveInfoSessionDBService(){
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent){
        Log.i(TAG, "onHandleIntent");
        int id = intent.getIntExtra(ID, 0);
        InfoSessionDBHelper dbHelper = InfoSessionDBHelper.getInstance(getApplicationContext());
        dbHelper.deleteInfoSession(id);
        dbHelper.deleteToRemove(id);
    }

}
