package levi.gastratrak;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final ArrayList<FoodItem> foodsList = new ArrayList<>();
    private FoodItemAdapter foodAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        foodAdapter = new FoodItemAdapter(MainActivity.this, foodsList);
        ListView foodDiaryView = findViewById(R.id.foodDiary_list);
        foodDiaryView.setAdapter(foodAdapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFoodEmpty();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        foodAdapter.updateFromDatabase();
    }


    private void painScaleOpener() {
        Intent intent = new Intent(this, PainScaleActivity.class);
        startActivityForResult(intent, 1);
    }
    private void graphModeOpener() {
        Intent intent = new Intent(this, GraphActivity.class);
        startActivity(intent);
    }

    private void stoolRecordOpener() {
        Intent intent = new Intent(this, StoolRecordActivity.class);
        startActivity(intent);
    }

    public void editFoodItem(FoodItem item) {
        Intent intent = new Intent(this, FoodDiaryAddEditActivity.class);
        intent.putExtra("foodName", item.getFoodItem());
        intent.putExtra("foodTime", item.getFoodTime().getTime());
        intent.putExtra("isEdit", true);
        startActivityForResult(intent, 1);
    }

    private void addFoodEmpty() {
        Intent intent = new Intent(this, FoodDiaryAddEditActivity.class);
        intent.putExtra("isEdit", false);
        startActivityForResult(intent, 3);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar oldItem clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view oldItem clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_food_diary) {
            // return to the main, not sure if going to implement

        } else if (id == R.id.nav_new_pain) {
            painScaleOpener();
        } else if (id == R.id.nav_stool) {
            stoolRecordOpener();
        } else if (id == R.id.nav_stats) {
            //TODO make stats
        } else if (id == R.id.nav_graphing) {
            graphModeOpener();
        } else if (id == R.id.nav_send) {
            //TODO export database
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
