package com.example.calcuverse;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    // Declare UI elements
    TextView resultTV, solutionTV, currencyTV;
    ImageButton buttonC, buttonB, buttonMultiply, buttonDivide, buttonPlus, buttonMinus, buttonDot,
            button0, button1, button2, button3, button4, button5, button6, button7, button8, button9;
    Spinner spinner;
    ArrayList<String> currencyList = new ArrayList<>();
    JSONObject rates;
    String selectedCurrency;
    Double rate, convertedValue;
    boolean dotEntered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        resultTV = findViewById(R.id.textview_result);
        solutionTV = findViewById(R.id.textview_solution);
        currencyTV = findViewById(R.id.textview_newcurrency);

        // Assign click listeners to buttons
        assignId(buttonC, R.id.button_clear);
        assignId(buttonB, R.id.button_backspace);
        assignId(buttonDot, R.id.button_dot);
        assignId(buttonDivide, R.id.button_divide);
        assignId(buttonMultiply, R.id.button_multiply);
        assignId(buttonPlus, R.id.button_plus);
        assignId(buttonMinus, R.id.button_minus);
        assignId(button0, R.id.button_zero);
        assignId(button1, R.id.button_one);
        assignId(button2, R.id.button_two);
        assignId(button3, R.id.button_three);
        assignId(button4, R.id.button_four);
        assignId(button5, R.id.button_five);
        assignId(button6, R.id.button_six);
        assignId(button7, R.id.button_seven);
        assignId(button8, R.id.button_eight);
        assignId(button9, R.id.button_nine);

        // Initialize default values
        solutionTV.setText("");
        resultTV.setText("0");

        // Set up the currency spinner
        spinner = findViewById(R.id.spinner_currencies);
        spinner.setOnItemSelectedListener(this);

        // Fetch currency rates from the JSON API
        jsonParse();
    }

    // Assigns the click listener to the ImageButton and initializes it
    void assignId(ImageButton btn, int id) {
        btn = findViewById(id);
        btn.setOnClickListener(this);
    }

    // Find the index of the given currency in the spinner
    private int getIndex(Spinner spinner, String s) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(s)) {
                return i;
            }
        }
        return 0;
    }

    // Parse the JSON data to fetch currency rates
    public void jsonParse() {
        // API URL to fetch currency rates
//        String URL = "https://raw.githubusercontent.com/OutrageGames/curgit/main/cur.json"; // Testing purposes
        String URL = "http://data.fixer.io/api/latest?access_key=ecbb24f252da0cdb9299e45c4d96bd8b";

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Extract the data you want from the JSON response
                            rates = response.getJSONObject("rates");

                            // Loop through each rate and add it to the currencyList
                            Iterator<String> keys = rates.keys();
                            while (keys.hasNext()) {
                                String currencyCode = keys.next();
                                currencyList.add(currencyCode);
                            }

                            // Set up the spinner with the currencyList and select USD as the default currency
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.spinner_item, currencyList);
                            adapter.setDropDownViewResource(R.layout.spinner_item);
                            spinner.setAdapter(adapter);
                            spinner.setSelection(getIndex(spinner, "USD"));

                            // Calculate and display the converted value
                            updateConvertedValue();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("JSON", "Error parsing JSON data: " + e.getMessage());
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("JSON", "Error fetching JSON data: " + error);
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

    // Get the currency rate for the selected currency
    private Double getRateForCurrency(String currency) {
        try {
            return rates.getDouble(currency);
        } catch (JSONException e) {
            e.printStackTrace();
            return 1.0; // Default rate for currencies not found in the JSON data
        }
    }

    // Handle item selection in the currency spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedCurrency = parent.getItemAtPosition(position).toString();
        rate = getRateForCurrency(selectedCurrency);

        // Update the result based on the selected currency
        updateConvertedValue();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // Do nothing when nothing is selected
    }

    // Handle button clicks for the calculator
    @Override
    public void onClick(View view) {
        // Animation to provide button feedback
        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_scale);

        ImageButton button = (ImageButton) view;
        String buttonText = button.getContentDescription().toString();
        int buttonId = view.getId();
        String dataToCalculate = solutionTV.getText().toString();

        // Clear button clicked
        if (buttonId == R.id.button_clear) {
            dotEntered = false;
            solutionTV.setText("");
            resultTV.setText("0");
            currencyTV.setText("0.0");
            button.startAnimation(anim);
            return;
        }

        // Dot button clicked
        if (buttonId == R.id.button_dot) {
            if (dotEntered) {
                return;
            }
        }

        // Check if operators are consecutive
        if (buttonId == R.id.button_plus || buttonId == R.id.button_multiply || buttonId == R.id.button_divide || buttonId == R.id.button_dot) {
            if (dataToCalculate.isEmpty() ||
                    dataToCalculate.charAt(dataToCalculate.length() - 1) == '+' ||
                    dataToCalculate.charAt(dataToCalculate.length() - 1) == '-' ||
                    dataToCalculate.charAt(dataToCalculate.length() - 1) == '*' ||
                    dataToCalculate.charAt(dataToCalculate.length() - 1) == '/' ||
                    dataToCalculate.charAt(dataToCalculate.length() - 1) == '.') {
                return;
            }
        }

        // Minus button clicked
        if (buttonId == R.id.button_minus) {
            if (!dataToCalculate.isEmpty()) {
                if (dataToCalculate.charAt(dataToCalculate.length() - 1) == '-' ||
                        dataToCalculate.charAt(dataToCalculate.length() - 1) == '.') {
                    return;
                }
            }
        }

        // Backspace button clicked
        if (buttonId == R.id.button_backspace) {
            button.startAnimation(anim);
            if (!dataToCalculate.isEmpty()) {
                if (dataToCalculate.charAt(dataToCalculate.length() - 1) == '.') {
                    dotEntered = false;
                }
            }
            if (dataToCalculate.length() > 1) {
                dataToCalculate = dataToCalculate.substring(0, dataToCalculate.length() - 1);
                updateConvertedValue();
            } else if (dataToCalculate.length() == 1) {
                solutionTV.setText("");
                resultTV.setText("0");
                currencyTV.setText("0.0");
                return;
            } else {
                resultTV.setText("0");
                currencyTV.setText("0.0");
                return;
            }
        } else {
            dataToCalculate = dataToCalculate + buttonText;
        }

        // Update dotEntered flag
        if (buttonId == R.id.button_dot) {
            dotEntered = true;
        } else if (buttonId == R.id.button_plus || buttonId == R.id.button_minus || buttonId == R.id.button_multiply || buttonId == R.id.button_divide) {
            dotEntered = false;
        }

        // Update the solution TextView and calculate the result
        solutionTV.setText(resultTV.getText());
        solutionTV.setText(dataToCalculate);
        String finalResult = getResult(dataToCalculate);

        // Update the result TextView with the final result
        if (!finalResult.equals("Error")) {
            resultTV.setText(finalResult);
        }

        // Calculate and display the converted value
        updateConvertedValue();
        button.startAnimation(anim);
    }

    // Evaluate the expression using JavaScript engine
    String getResult(String data) {
        try {
            Context context = Context.enter();
            context.setOptimizationLevel(-1);
            Scriptable scriptable = context.initSafeStandardObjects();
            String finalResult = context.evaluateString(scriptable, data, "Javascript", 1, null).toString();

            // Format the final result with appropriate notation based on its magnitude
            BigDecimal resultValue = new BigDecimal(finalResult);

            if (resultValue.abs().compareTo(BigDecimal.TEN.pow(10)) >= 0) {
                // Use scientific notation if the number is large
                DecimalFormat scientificFormat = new DecimalFormat("0.##E0");
                finalResult = scientificFormat.format(resultValue);
            } else {
                // Use regular decimal notation for smaller numbers
                DecimalFormat decimalFormat = new DecimalFormat("#.##");
                finalResult = decimalFormat.format(resultValue);
            }

            // Remove ".0" suffix if present
            if (finalResult.endsWith(".0")) {
                finalResult = finalResult.replace(".0", "");
            }

            return finalResult;
        } catch (Exception e) {
            return "Error";
        }
    }

    // Calculate and update the converted value based on the selected currency
    private void updateConvertedValue() {
        rate = getRateForCurrency(selectedCurrency);
        Double res = Double.parseDouble(resultTV.getText().toString());
        convertedValue = rate * res;
        currencyTV.setText(getResult(convertedValue.toString()));
    }
}