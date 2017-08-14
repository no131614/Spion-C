package first.alexander.com.spion_c;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.databinding.DataBindingUtil;

import java.text.DecimalFormat;

import first.alexander.com.spion_c.databinding.ActivityMainBinding;


/**
 * Main Activity of Spion-C
 *
 * This activity contains the Calculator implementation and service calls to CameraService.java
 *
 * @author Alexander Julianto (no131614)
 * @version
 * @since API 21
 */

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private static final char ADDITION = '+';
    private static final char SUBTRACTION = '-';
    private static final char MULTIPLICATION = '*';
    private static final char DIVISION = '/';
    private static final char NONE = '0';

    private char current_operation;

    private double firstNumber = Double.NaN;
    private double secondNumber;

    private String secondNumberString;

    private DecimalFormat decimalFormat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        decimalFormat = new DecimalFormat("#.##########");

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Begin: All button implementations

        // Button  use to get calculation result and start running the camera service (runs in background)
        binding.buttonEqual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Calculate();
                binding.infoTextView.setText(binding.infoTextView.getText().toString() +
                        decimalFormat.format(secondNumber) + " = " + decimalFormat.format(firstNumber));

                current_operation = NONE;

                // Begin: Calls CameraService.java
                Intent service_intent = new Intent(getApplication().getApplicationContext(), CameraService.class);
                service_intent.putExtra("Front_Request", true);
                getApplication().getApplicationContext().startService(service_intent);
                // End: Calls CameraService.java
            }
        });

        binding.buttonZero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.editText.setText(binding.editText.getText() + "0");
            }
        });

        binding.buttonOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.editText.setText(binding.editText.getText() + "1");
            }
        });

        binding.buttonTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.editText.setText(binding.editText.getText() + "2");
            }
        });

        binding.buttonThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.editText.setText(binding.editText.getText() + "3");
            }
        });

        binding.buttonFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.editText.setText(binding.editText.getText() + "4");
            }
        });

        binding.buttonFive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.editText.setText(binding.editText.getText() + "5");
            }
        });

        binding.buttonSix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.editText.setText(binding.editText.getText() + "6");
            }
        });

        binding.buttonSeven.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.editText.setText(binding.editText.getText() + "7");
            }
        });

        binding.buttonEight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.editText.setText(binding.editText.getText() + "8");
            }
        });

        binding.buttonNine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.editText.setText(binding.editText.getText() + "9");
            }
        });

        binding.buttonDot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.editText.setText(binding.editText.getText() + ".");
            }
        });


        binding.buttonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calculate();
                current_operation = ADDITION;
                binding.infoTextView.setText(decimalFormat.format(firstNumber) + "+");
                binding.editText.setText(null);
            }
        });

        binding.buttonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calculate();
                current_operation = SUBTRACTION;
                binding.infoTextView.setText(decimalFormat.format(firstNumber) + "-");
                binding.editText.setText(null);
            }
        });

        binding.buttonMul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calculate();
                current_operation = MULTIPLICATION;
                binding.infoTextView.setText(decimalFormat.format(firstNumber) + "*");
                binding.editText.setText(null);
            }
        });

        binding.buttonDiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calculate();
                current_operation = DIVISION;
                binding.infoTextView.setText(decimalFormat.format(firstNumber) + "/");
                binding.editText.setText(null);
            }
        });


        binding.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.editText.getText().length() > 0) {
                    CharSequence currentText = binding.editText.getText();
                    binding.editText.setText(currentText.subSequence(0, currentText.length() - 1));
                } else {
                    firstNumber = Double.NaN;
                    secondNumber = Double.NaN;
                    binding.editText.setText("");
                    binding.infoTextView.setText("");
                }
            }
        });


        binding.buttonC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firstNumber = Double.NaN;
                secondNumber = Double.NaN;
                binding.editText.setText(null);
                binding.infoTextView.setText(null);
            }
        });
        // End: All button implementations


    }


    /**
     * Main calculation method use by the calculator app
     */
    private void Calculate() {
        if (!Double.isNaN(firstNumber)) {
            secondNumberString = binding.editText.getText().toString();

            if (secondNumberString.isEmpty()) {
                return;
            }

            secondNumber = Double.parseDouble(secondNumberString);

            binding.editText.setText(null);

            if (current_operation == ADDITION) {
                firstNumber = this.firstNumber + secondNumber;
            } else if (current_operation == SUBTRACTION) {
                firstNumber = this.firstNumber - secondNumber;
            } else if (current_operation == MULTIPLICATION) {
                firstNumber = this.firstNumber * secondNumber;
            } else if (current_operation == DIVISION) {
                firstNumber = this.firstNumber / secondNumber;
            } else if (current_operation == NONE) {
                firstNumber = secondNumber;
            }

        } else {
            try {
                firstNumber = Double.parseDouble(binding.editText.getText().toString());
            } catch (Exception e) {
                Log.d("Calculating", "Error Exception in Calculate");
            }
        }

    }


}




