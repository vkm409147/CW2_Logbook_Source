package com.example.eventmanager;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner unitSpinner;
    private EditText weightInput;
    private EditText heightInput;
    private TextView weightLabel;
    private TextView heightLabel;
    private TextView bmiResult;
    private TextView categoryResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        unitSpinner = findViewById(R.id.unitSpinner);
        weightInput = findViewById(R.id.weightInput);
        heightInput = findViewById(R.id.heightInput);
        weightLabel = findViewById(R.id.weightLabel);
        heightLabel = findViewById(R.id.heightLabel);
        bmiResult = findViewById(R.id.bmiResult);
        categoryResult = findViewById(R.id.categoryResult);

        ArrayAdapter<String> units = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Metric (kg, cm)", "Imperial (lb, in)"});
        units.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(units);
        unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                boolean metric = position == 0;
                weightLabel.setText(metric ? "Weight (kg)" : "Weight (lb)");
                heightLabel.setText(metric ? "Height (cm)" : "Height (in)");
                clearResult();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                clearResult();
            }
        });

        Button calculateButton = findViewById(R.id.calculateButton);
        calculateButton.setOnClickListener(view -> calculateBmi());
    }

    private void calculateBmi() {
        String weightText = weightInput.getText().toString().trim();
        String heightText = heightInput.getText().toString().trim();
        if (weightText.isEmpty()) {
            weightInput.setError("Please enter your weight.");
            weightInput.requestFocus();
            return;
        }
        if (heightText.isEmpty()) {
            heightInput.setError("Please enter your height.");
            heightInput.requestFocus();
            return;
        }

        final double weight;
        final double height;
        try {
            weight = Double.parseDouble(weightText);
            height = Double.parseDouble(heightText);
        } catch (NumberFormatException exception) {
            weightInput.setError("Enter measurements as numbers.");
            weightInput.requestFocus();
            return;
        }

        BmiCalculator.UnitSystem unitSystem = unitSpinner.getSelectedItemPosition() == 0
                ? BmiCalculator.UnitSystem.METRIC
                : BmiCalculator.UnitSystem.IMPERIAL;
        try {
            BmiCalculator.Result result = BmiCalculator.calculate(weight, height, unitSystem);
            bmiResult.setText(String.format(Locale.getDefault(), "BMI: %.1f", result.getBmi()));
            categoryResult.setText(result.getCategory().getLabel());
            categoryResult.setTextColor(categoryColor(result.getCategory()));
        } catch (IllegalArgumentException exception) {
            if (exception.getMessage().startsWith("Weight")) {
                weightInput.setError(exception.getMessage());
                weightInput.requestFocus();
            } else {
                heightInput.setError(exception.getMessage());
                heightInput.requestFocus();
            }
            clearResult();
        }
    }

    private int categoryColor(BmiCalculator.Category category) {
        switch (category) {
            case UNDERWEIGHT:
                return 0xFF1565C0;
            case NORMAL:
                return 0xFF2E7D32;
            case OVERWEIGHT:
                return 0xFFEF6C00;
            case OBESITY:
                return 0xFFC62828;
            default:
                throw new IllegalStateException("Unknown BMI category.");
        }
    }

    private void clearResult() {
        bmiResult.setText("");
        categoryResult.setText("");
    }
}
