package github.toninhobueno.fitnesstracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class TmbActivity extends AppCompatActivity {

    private EditText editWeight;
    private EditText editHeight;
    private EditText editAge;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tmb);

        editHeight = findViewById(R.id.edit_tmb_height);
        editWeight = findViewById(R.id.edit_tmb_weight);
        editAge = findViewById(R.id.edit_tmb_age);
        spinner = findViewById(R.id.spinner_tmb_lifestyle);

        Button btnSend = findViewById(R.id.btn_tmb_send);
        btnSend.setOnClickListener((v) -> {
            if (!validate()) {
                Toast.makeText(TmbActivity.this, R.string.fields_message, Toast.LENGTH_LONG).show();
                return;
            }

            String sHeight = editHeight.getText().toString();
            String sWeight = editWeight.getText().toString();
            String sAge = editAge.getText().toString();

            int height = Integer.parseInt(sHeight);
            int weight = Integer.parseInt(sWeight);
            int age = Integer.parseInt(sAge);

            double result = calculateTmb(height, weight, age);
            double tmb = tmbResponse(result);
            Log.d("Teste", "" + tmb);

            AlertDialog alertDialog = new AlertDialog.Builder(TmbActivity.this)
                    .setTitle(getString(R.string.tmb_response, tmb))
                    .setMessage(getString(R.string.tmb_response, tmb))
                    .setNegativeButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(R.string.save, (dialog, which) -> {
                        SqlHelper sqlHelper = SqlHelper.getInstance(TmbActivity.this);

                        new Thread(() -> {
                            int updateId = 0;

                            // verifica se tem ID vindo da tela anterior quando é UPDATE
                            if (getIntent().getExtras() != null)
                                updateId = getIntent().getExtras().getInt("updateId", 0);

                            long calcId;
                            // verifica se é update ou create
                            if (updateId > 0) {
                                calcId = sqlHelper.updateItem("tmb", tmb, updateId);
                            } else {
                                calcId = sqlHelper.addItem("tmb", tmb);
                            }

                            runOnUiThread(() -> {
                                if (calcId > 0) {
                                    Toast.makeText(TmbActivity.this, R.string.calc_saved, Toast.LENGTH_LONG).show();
                                    openListCalcActivity();
                                }
                            });
                        }).start();


                    })
                    .create();

            alertDialog.show();

            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(editWeight.getWindowToken(), 0);
            mgr.hideSoftInputFromWindow(editHeight.getWindowToken(), 0);
            mgr.hideSoftInputFromWindow(editAge.getWindowToken(), 0);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_list:
                openListCalcActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private double calculateTmb(int height, double weight, int age) {
        return 66 + (13.8 * weight) + (5 * height) - (6.8 * age);
    }

    private double tmbResponse(double tmb) {
        switch (spinner.getSelectedItemPosition()) {
            case 0: return tmb * 1.2;
            case 1: return tmb * 1.375;
            case 2: return tmb * 1.55;
            case 3: return tmb * 1.725;
            case 4: return tmb * 1.9;
            default:return 0;
        }
    }

    private boolean validate() {
        return !editHeight.getText().toString().startsWith("0")
                && !editWeight.getText().toString().startsWith("0")
                && !editAge.getText().toString().startsWith("0")
                && !editHeight.getText().toString().isEmpty()
                && !editWeight.getText().toString().isEmpty()
                && !editAge.getText().toString().isEmpty();
    }

    private void openListCalcActivity() {
        Intent intent = new Intent(TmbActivity.this, ListCalcActivity.class);
        intent.putExtra("type", "tmb");
        TmbActivity.this.startActivity(intent);
    }

}