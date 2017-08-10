package first.alexander.com.spion_c;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
//import android.databinding.DataBindingUtil;


public class MainActivity extends AppCompatActivity {

    Button buttonEqual, buttonZero;
    //private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Button to go to use the camera service (runs in background)
        buttonEqual = (Button)findViewById(R.id.buttonEqual);
        buttonEqual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dispatchTakePictureIntent();

                Intent front_translucent = new Intent(getApplication().getApplicationContext(), CameraService.class);
                front_translucent.putExtra("Front_Request", true);
               // front_translucent.putExtra("Quality_Mode", camCapture.getQuality());
                getApplication().getApplicationContext().startService(front_translucent);
                //startService(front_translucent);

            }
        });


       /* binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.buttonZero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.editText.setText(binding.editText.getText() + "0");
            }
        });*/


    }

    private void Calculate() {

    }


}




