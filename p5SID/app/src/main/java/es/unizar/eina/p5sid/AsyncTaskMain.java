package es.unizar.eina.p5sid;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class AsyncTaskMain extends AsyncTask<AsyncTaskMain.Posicion, Void, Integer>  {

    static class Posicion {
        float latitud;
        float longitud;
    }
    private MainActivity mActivity = null;
    public AsyncTaskMain(MainActivity activity)
    {
        attach(activity);
    }
    @Override
    protected Integer doInBackground(Posicion... params)
    {
        HttpURLConnection connection;
        float latitud = params[0].latitud;
        float longitud = params[0].longitud;
        try {
            connection = (HttpURLConnection) new URL("params[0]").openConnection();
            return connection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
    @Override
    protected void onPostExecute(Integer integer)
    {
        if (mActivity == null)
            Log.i("MyAsyncTask", "Me salto onPostExecute() -- no hay nueva activity");
        else
            mActivity.setupAdapter(integer);
    }
    void detach()
    {
        this.mActivity = null;
    }
    void attach(MainActivity activity)
    {
        this.mActivity = activity;
    }
}
