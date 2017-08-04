package first.alexander.com.spion_c;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;


/**
 * Created by Alexander Julianto on 7/29/2017.
 */

public class ImageProcess extends AppCompatActivity {


    public static final String KEY_IMAGE = "key_image";
    public static final String UPLOAD_URL = "http://dbtest07.000webhostapp.com/upload.php";


    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 75, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }



    public void uploadImage(Bitmap bmp){
        final String image = getStringImage(bmp);

        class UploadImage extends AsyncTask<Void,Void,String>{
           // ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
               // loading = ProgressDialog.show(ImageProcess.this,"Please wait...","uploading",false,false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
               // loading.dismiss();
               // Toast.makeText(ImageProcess.this,s,Toast.LENGTH_LONG).show();
            }

            @Override
            protected String doInBackground(Void... params) {
                RequestHandler rh = new RequestHandler();
                HashMap<String,String> param = new HashMap<String,String>();
                param.put(KEY_IMAGE,image);
                System.out.println("START SEND POST REQUEST");
                String result = rh.sendPostRequest(UPLOAD_URL, param);
                System.out.println("FINISH SEND POST REQUEST");
                return result;
            }
        }
        UploadImage u = new UploadImage();
        u.execute();
    }





}
