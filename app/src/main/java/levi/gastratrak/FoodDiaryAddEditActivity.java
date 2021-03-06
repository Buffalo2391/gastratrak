package levi.gastratrak;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;

public class FoodDiaryAddEditActivity extends AppCompatActivity {
    private FoodItem oldItem;
    private final DatabaseController db = new DatabaseController(this);
    private boolean isEdit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_diary_add_edit);
        Button deleteButton = findViewById(R.id.DeleteButton);
        EditText foodName = findViewById(R.id.FoodNameInput);
        TimePicker foodTime = findViewById(R.id.timePicker);
        Intent intent = getIntent();

        String oldItemName = intent.getStringExtra("foodName");
        long oldItemTime = intent.getLongExtra("foodTime", System.currentTimeMillis());
        this.isEdit = intent.getBooleanExtra("isEdit", true);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(oldItemTime);
        oldItem = new FoodItem(oldItemName, cal);
        foodTime.setIs24HourView(Boolean.TRUE);
        foodName.setText(oldItemName);
        foodTime.setHour(cal.get(Calendar.HOUR_OF_DAY));
        foodTime.setMinute(cal.get(Calendar.MINUTE));
        if (isEdit) {
            deleteButton.setOnClickListener(view -> deleteButtonPressed());
        } else {
            deleteButton.setEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.return_to_main_saved, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_accept:
                saveButtonPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }
    private  void deleteButtonPressed() {
        db.removeFoodItem(this.oldItem);
        setResult(RESULT_CANCELED);
        finish();
    }
    private void saveButtonPressed() {
        EditText foodName = findViewById(R.id.FoodNameInput);
        TimePicker foodTime = findViewById(R.id.timePicker);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, foodTime.getHour());
        cal.set(Calendar.MINUTE, foodTime.getMinute());
        cal.set(Calendar.SECOND, 0);
        FoodItem item = new FoodItem(foodName.getText().toString(),cal);
        if(isEdit){
            db.removeFoodItem(this.oldItem);
        }
        if(db.addFoodItem(item)) {
            setResult(RESULT_OK);
            finish();
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage("Food Type cannot be blank")
                    .setTitle("Error");
            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }
}
