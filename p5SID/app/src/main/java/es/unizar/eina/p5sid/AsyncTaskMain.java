package es.unizar.eina.p5sid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public class AsyncTaskMain extends AsyncTask<Location, Void, AsyncTaskMain.Imagen>  {

    private MainActivity mActivity = null;

    // PARAMETRO SALIDA
    static class Imagen {
        Bitmap imagen;
        String titulo;
        String latitud;
        String longitud;
    }

    // CLASES PARA PARSEAR EL JSON CON GSON
    static class Photo {
        String id;
        String owner;
        String title;
        String url_s;
        String latitude;
        String longitude;
    }

    static class Photos {
        Integer page;
        Integer pages;
        Integer perpage;
        String total;
        List<Photo> photo;
    }

    static class Result {
        Photos photos;
        String stat;
    }

    public AsyncTaskMain(MainActivity activity)
    {
        attach(activity);
    }

    @Override
    protected Imagen doInBackground(Location... params)
    {
        HttpURLConnection connection;
        Double latitud = params[0].getLatitude();
        Double longitud = params[0].getLongitude();
        String url =  "https://www.flickr.com/services/rest/?method=flickr.photos.search&api_key="+mActivity.getString(R.string.apiFlickr)+"&lat="+latitud.toString()+"&lon="+longitud.toString()+"&radius=5&radius_units=km&sort=date-posted-desc&extras=geo,url_s&format=json&nojsoncallback=1";
        try {
            // GET LIST
            connection = (HttpURLConnection) new URL(url).openConnection();
            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            Gson gson = new Gson();
            Result resultado = gson.fromJson(reader, Result.class);

            // GET IMAGE, TITLE AND LOCATION with url_s not null
            int idPhoto = 0;
            String urlImagen = resultado.photos.photo.get(idPhoto).url_s;
            while ((urlImagen==null || urlImagen.equals("")) && idPhoto < resultado.photos.perpage){
                idPhoto ++;
                urlImagen = resultado.photos.photo.get(idPhoto).url_s;
            }

            Imagen imagen = new Imagen();
            imagen.titulo = resultado.photos.photo.get(idPhoto).title;
            imagen.latitud = resultado.photos.photo.get(idPhoto).latitude;
            imagen.longitud = resultado.photos.photo.get(idPhoto).longitude;

            InputStream in = new java.net.URL(urlImagen).openStream();
            imagen.imagen = BitmapFactory.decodeStream(in);

            return imagen;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Imagen();
    }

    @Override
    protected void onPostExecute(Imagen imagen)
    {
        if (mActivity == null)
            Log.i("MyAsyncTask", "Me salto onPostExecute() -- no hay nueva activity");
        else if (imagen.titulo == null || imagen.imagen==null || imagen.longitud == null || imagen.latitud == null)
            Toast.makeText(mActivity, "ERROR on Flickr Search", Toast.LENGTH_SHORT).show();
        else {
            mActivity.setupAdapter(imagen);
        }
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
