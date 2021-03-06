package edu.unitec.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SectionActivity extends Activity
{

    private String UUID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_section);

        Intent intent = getIntent();
        UUID = intent.getStringExtra("UUID");

        loadYears();
        loadCourses();
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

    }


    /*public void onclickItem(MenuItem item)
    {
    }*/

    public void loadYears()
    {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        List<String> yearList = new ArrayList<String>();
        for (int a = -3; a <= 3; a++)
        {
            yearList.add(Integer.toString(year + a));
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, yearList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner)findViewById(R.id.spinnerYear)).setAdapter(dataAdapter);
        ((Spinner)findViewById(R.id.spinnerYear)).setSelection(3);
    }

    public void loadCourses()
    {
        List<String> courseList = new DatabaseHandler(this).getAllName_Courses(UUID);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, courseList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner)findViewById(R.id.spinnerCourse)).setAdapter(dataAdapter);
    }

    public int getCurrentSemester(){
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        if ( month <= 5 ){
            return 1;
        }
        return 2;
    }

    public int getCurrentQuarter(){
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH); //January == 0
        int currentSemester = getCurrentSemester();
        int currentQuarter = 0;

        if ( currentSemester == 1 ){
            if ( currentMonth <= 2 ){
                currentQuarter = 1;
            }else{
                currentQuarter = 2;
            }
        }else if ( currentSemester == 2 ){
            if ( currentMonth <= 8 ){
                currentQuarter = 3;
            }else{
                currentQuarter = 4;
            }
        }
        return currentQuarter;
    }

    public boolean isValidQuarter()
    {
        if ( ((RadioButton)findViewById(R.id.radioButtonQuarter1)).isChecked() &&
             ((RadioButton)findViewById(R.id.radioButtonSemester1)).isChecked() )
        {
            return true;
        }

        if ( ((RadioButton)findViewById(R.id.radioButtonQuarter2)).isChecked() &&
             ((RadioButton)findViewById(R.id.radioButtonSemester1)).isChecked() )
        {
            return true;
        }

        if ( ((RadioButton)findViewById(R.id.radioButtonQuarter3)).isChecked() &&
             ((RadioButton)findViewById(R.id.radioButtonSemester2)).isChecked() )
        {
            return true;
        }

        if ( ((RadioButton)findViewById(R.id.radioButtonQuarter4)).isChecked() &&
             ((RadioButton)findViewById(R.id.radioButtonSemester2)).isChecked() )
        {
            return true;
        }

        return false;
    }

    public void saveSection(){
        //If there are no courses
        if ( ((Spinner)findViewById(R.id.spinnerCourse)).getSelectedItemPosition() < 0 ){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("No Courses");
            alert.setMessage("There are no courses availible.");
            alert.setPositiveButton("OK", null);
            alert.show();
        }else if ( !isValidQuarter() ){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Invalid Quarter");
            alert.setMessage("Quarter is not valid.");
            alert.setPositiveButton("OK", null);
            alert.show();
        }else if ( ((EditText)findViewById(R.id.editTextSection_code)).getText().toString()=="" ){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Empty Section Code");
            alert.setMessage("Section code is required.");
            alert.setPositiveButton("OK", null);
            alert.show();
        }else{
            int quarter = 0;
            int semester = 0;
            int year = Integer.parseInt(((Spinner)findViewById(R.id.spinnerYear)).getSelectedItem().toString());
            int courseId = ((Spinner)findViewById(R.id.spinnerCourse)).getSelectedItemPosition() + 1;
            String sectionCode=((EditText) findViewById(R.id.editTextSection_code)).getText().toString();

            //Checks which quarter was selected
            if ( ((RadioButton)findViewById(R.id.radioButtonQuarter1)).isChecked() ){
                quarter = 1;
            }else if ( ((RadioButton)findViewById(R.id.radioButtonQuarter2)).isChecked() ){
                quarter = 2;
            }else if ( ((RadioButton)findViewById(R.id.radioButtonQuarter3)).isChecked() ){
                quarter = 3;
            }else{
                quarter = 4;
            }

            //Checks which semester was selected
            if ( ((RadioButton)findViewById(R.id.radioButtonSemester1)).isChecked() ){
                semester = 1;
            }else{
                semester = 2;
            }
            if(getCurrentQuarter()==quarter && getCurrentSemester()==semester) {

                //Database
                SQLiteDatabase db = openOrCreateDatabase("Participation", SQLiteDatabase.CREATE_IF_NECESSARY, null);
                db.execSQL("INSERT INTO section(SectionId,TeacherUUID,CourseId, SectionQuarter, SectionSemester, SectionYear, SectionCode) VALUES(" +
                        getSection(UUID,db)+", '"+UUID+"' ,"+courseId + ", " + quarter + ", " + semester + ", " + year + ", \"" + sectionCode + "\")");
                db.close();

                //Show a message
                CharSequence message = "Success!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(getApplicationContext(), message, duration);
                toast.show();
            }else{
                //Show a message
                CharSequence message = "quarter and semester not valid!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(getApplicationContext(), message, duration);
                toast.show();
            }
        }
    }

    int getSection(String UUID,SQLiteDatabase databases){
        SQLiteDatabase db = databases;
        String query = "SELECT SectionId FROM section WHERE TeacherUUID  = '"+UUID+"' ORDER BY SectionId DESC LIMIT 1" ;


        Cursor cursor = db.rawQuery(query,null);
        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            int retval = cursor.getInt(0) + 1;
            cursor.close();
            return retval;
        }else{
            cursor.close();
            return 1;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.section, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case android.R.id.home:
               onBackPressed();
                return true;
            case R.id.save:
                saveSection();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
