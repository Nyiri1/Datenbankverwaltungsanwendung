package org.o7planning.sqlietereal;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity {

    private static final int LEVEL_0_THRESHHOLD = -3;//Abgelaufen seit mehr als 3 Tagen
    private static final int LEVEL_1_THRESHHOLD = -3;//Abgelaufen seit weniger als 3 Tagen
    private int LEVEL_2_THRESHHOLD = 4;//Kurz vor dem Ablaufen 1-7 Tage (Default: 4)
    private static final int LEVEL_3_THRESHHOLD = 10;//Läuft innerhalb von 10 Tagen ab
    private static final int LEVEL_4_THRESHHOLD = 10;//Hat noch mehr als 10 Tage Haltbarkeit
    private static final int LEVEL_9_THRESHHOLD = 0;//Benachrichtigungen manuell deaktiviert

    EditText editBarcode, editProduktname, editMhd, editLevel, editId, editNotified;
    Button btnAddData, btnViewAll, btnUpdate, btnDelete, btnFindId, btnUebersicht , btnCustom, btnScheduleJob, btnCancelJob;

    DatabaseHelper myDb;

    String[] listItems;
    String[] IdArray;

    Date currentDate;
    SimpleDateFormat dateFormat;

    View view;

    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context context = this;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        scheduleJob();

        myDb = new DatabaseHelper(this); //Calls Construktor of DatabaseHelper

        currentDate = Calendar.getInstance().getTime();
        dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());

        // Texteingabemöglichkeiten
        editBarcode = (EditText) findViewById(R.id.editText_barcode);
        editProduktname = (EditText) findViewById(R.id.editText_produktname);
        editMhd = (EditText) findViewById(R.id.editText_mhd);
        editLevel = (EditText) findViewById(R.id.editText_level);
        editId = (EditText) findViewById(R.id.editText_id);
        editNotified = (EditText) findViewById(R.id.editText_notified);

        // Buttons
        btnAddData = (Button) findViewById(R.id.button_add);
        btnViewAll = (Button) findViewById(R.id.button_viewAll);
        btnUpdate = (Button) findViewById(R.id.button_update);
        btnDelete = (Button) findViewById(R.id.button_delete);
        btnFindId = (Button) findViewById(R.id.button_deleteBarcode);
        btnUebersicht = (Button) findViewById(R.id.button_uebersicht);
        btnCustom = (Button) findViewById(R.id.button_custom);

        // Initialisierung der onClick Listener
        onClickAddData();
        onClickViewAllPopUp();
        onClickUpdateData();
        onClickDeleteData();
        onClickDeleteByBarcode();
        onClickViewItems();
        onClickGoOptions(); //customize
    }


    @Override
    protected void onStop(){
        super.onStop();
        Log.d(TAG, "onStop: ");
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        setAppStatus(0);
        scheduleJob();
    }

    public class Message{
        public void displayMessage(int resultCode, Bundle resultData){
            String message = resultData.getString("message");
            Toast.makeText(MainActivity.this, resultCode + " " + message, Toast.LENGTH_SHORT).show();
        }
    }

    private void setAppStatus(int newStatus){
        SharedPreferences prefs = this.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("App_Status", newStatus);
        editor.apply();
    }

    // Suche Produkt in der Database nach Barcode
    public void onClickFindByBarcode() {
        btnFindId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                if(editBarcode.length()!=0) {
                    String barcode = editBarcode.getText().toString();
                    Cursor res = myDb.getAllData();
                    if (res.getCount() == 0) {
                        showMessage("Error", "Nothing found");
                    } else {
                        StringBuffer buffer = new StringBuffer();
                        while (res.moveToNext()) {
                            if (res.getString(1).equals(barcode)) {
                                buffer.append("Id :" + res.getString(0) + "\n");
                                buffer.append("Barcode :" + res.getString(1) + "\n");
                                buffer.append("Produktname :" + res.getString(2) + "\n");
                                buffer.append("Mhd :" + res.getString(3) + "\n");
                                buffer.append("Level :" + res.getString(4) + "\n\n");
                            }
                        }
                        showMessage("Data", buffer.toString());
                    }
                    editBarcode.setText("");
                }else{
                    Toast.makeText(MainActivity.this,"You must put something in the Barcode field", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Anzeige einer alternativen Übersicht
    public void onClickViewItems() {
        btnUebersicht.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Uebersichtactivity.class);
                startActivity(intent);
            }
        });
    }

    // Ein Produkt durch Eingabe von MHD und Barcode löschen
    public void onClickDeleteByBarcodeAndMhd(){
        btnCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editBarcode.length()!=0 && editMhd.length() !=0){
                    String barcode = editBarcode.getText().toString();
                    String mhd = editMhd.getText().toString();
                    Cursor res = myDb.getAllData();
                    if(res.getCount() == 0) {
                        showMessage("Error", "Nothing found");
                    } else{
                        while(res.moveToNext()){
                            if(res.getString(1).equals(barcode) && res.getString(3).equals(mhd)){
                                myDb.deleteData(res.getString(0));
                            }
                        }
                    }
                }
            }
        });
    }

    //delete by Barcode
    public void onClickDeleteByBarcode() {
        btnFindId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editBarcode.length()!=0) {
                    String barcode = editBarcode.getText().toString();
                    Cursor res = myDb.getAllData();
                    if (res.getCount() == 0) {
                        showMessage("Error", "Nothing found");
                    } else {
                        int i = 0;
                        int cnt = 0;
                        int size = 1;
                        String[] buffer = new String[size];
                        IdArray = new String[size];
                        while (res.moveToNext()) {
                            if (res.getString(1).equals(barcode)) {
                                buffer[i] = ("Id :" + res.getString(0) + "\n");
                                buffer[i] = buffer[i] + "Barcode :" + res.getString(1) + "\n";
                                buffer[i] = buffer[i] + "Produktname :" + res.getString(2) + "\n";
                                buffer[i] = buffer[i] + "Mhd :" + res.getString(3) + "\n";
                                buffer[i] = buffer[i] + "Level :" + res.getString(4) + "\n\n";

                                IdArray[i] = res.getString(0);

                                i++;
                                // Wenn der benötigte Speicher anfangs nicht bekannt ist
                                if (i >= size) {
                                    String[] temp = new String[size * 2];
                                    String[] tempint = new String[size * 2];

                                    for (cnt = 0; cnt < size; cnt++) {
                                        temp[cnt] = buffer[cnt];
                                        tempint[cnt] = IdArray[cnt];
                                    }
                                    buffer = temp;
                                    IdArray = tempint;
                                    size = size * 2;
                                }
                            }
                        }
                        // Nachdem Array gefüllt ist Speicher freigeben
                        if (i < size) {
                            String[] temp2 = new String[i];
                            for (cnt = 0; cnt < i; cnt++) {
                                temp2[cnt] = buffer[cnt];
                            }
                            buffer = temp2;
                            size = i;
                        }
                        listItems = new String[size];
                        for (i = 0; i < size; i++) {
                            listItems[i] = buffer[i];
                        }
                        //create single choice alert dialog
                        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                        mBuilder.setTitle("Which one");
                        mBuilder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                myDb.deleteData(IdArray[which]);
                                dialogInterface.dismiss();
                            }
                        });
                        mBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        //Show Alert dialog
                        AlertDialog mDialog = mBuilder.create();
                        mDialog.show();
                    }
                    editBarcode.setText("");
                }else{
                    Toast.makeText(MainActivity.this,"You must put something in the Barcode field", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void onClickDeleteData(){
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editId.length()!=0) {
                    Integer deletedRows = myDb.deleteData(editId.getText().toString());
                    if (deletedRows > 0)
                        Toast.makeText(MainActivity.this, "Data deleted", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(MainActivity.this, "Data not deleted", Toast.LENGTH_LONG).show();
                    editId.setText("");
                }else{
                    Toast.makeText(MainActivity.this,"You must put something in the Id field", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    public void onClickUpdateData(){
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editId.length()!=0 & editBarcode.length()!=0 & editProduktname.length()!=0 & editMhd.length()!=0 & levelViable(editLevel.getText().toString()) & editNotified.length()!=0) {
                    boolean isUpdate = myDb.updateData(editId.getText().toString(), editBarcode.getText().toString(), editProduktname.getText().toString(), editMhd.getText().toString(), editLevel.getText().toString(), editNotified.getText().toString());
                    if (isUpdate) {
                        Toast.makeText(MainActivity.this, "Data Updated", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Data not Updated", Toast.LENGTH_LONG).show();
                    }
                    editId.setText("");
                    editBarcode.setText("");
                    editProduktname.setText("");
                    editMhd.setText("");
                    editLevel.setText("");
                    editNotified.setText("");
                }else{
                    Toast.makeText(MainActivity.this,"You must put something in the Name, Barcode, Mhd, Level, Id and Notified field", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public boolean levelViable(String level){
        if(level.length()!=0){
            int levelInt = parseInt(level);
            if((levelInt >=0 & levelInt<=5) || levelInt == 9 ) {
                return true;
            }
        }else{
            Toast.makeText(MainActivity.this, "Level not valid", Toast.LENGTH_LONG).show();
            return false;
        }
        return false;
    }

    public void onClickViewAllPopUp(){
        btnViewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDb.updateLevel(MainActivity.this);
                Cursor res = myDb.getAllData();
                if(res.getCount() == 0){
                    showMessage("Error", "Nothing found");
                    return;
                }
                else{
                    StringBuffer buffer = new StringBuffer();
                    while(res.moveToNext()){
                        buffer.append("Id :"+ res.getString(0)+"\n");
                        buffer.append("Barcode :"+ res.getString(1)+"\n");
                        buffer.append("Produktname :"+ res.getString(2)+"\n");
                        buffer.append("Mhd :"+ res.getString(3)+"\n");
                        buffer.append("Level :"+ res.getString(4)+"\n\n");
                    }

                    showMessage("Data", buffer.toString());
                }
            }
        });
    }


    public void showMessage(String title, String Message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this); // Using this builder we can create an alert dialog
        builder.setCancelable(true); // We can cancel it after its use
        builder.setTitle(title); // Sets the title
        builder.setMessage(Message); // Sets the message
        builder.show(); // This will show our Dialog
    }


    public void onClickAddData(){
        btnAddData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editBarcode.length()!=0 & editProduktname.length()!=0 & editMhd.length()!=0 & levelViable(editLevel.getText().toString())) {
                    boolean isInserted = myDb.insertData(editBarcode.getText().toString(), editProduktname.getText().toString(), editMhd.getText().toString(), editLevel.getText().toString());
                    if (isInserted == true)
                        Toast.makeText(MainActivity.this, "Data inserted", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(MainActivity.this, "Data not inserted", Toast.LENGTH_LONG).show();
                    editBarcode.setText("");
                    editProduktname.setText("");
                    editMhd.setText("");
                    editLevel.setText("");
                }else{
                    Toast.makeText(MainActivity.this,"You must put something in the Name, Barcode, Mhd and Level field", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void onClickGoOptions(){
        btnCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    public void scheduleJob(){
        ComponentName componentName = new ComponentName(this, ExampleJobService.class);
        JobInfo info = new JobInfo.Builder(123, componentName)
                .setPersisted(true)
                .setPeriodic(15 * 60 * 1000)
                .build();
        //.setRequiredNetwork(WLAN)
        //.setRequiredNetworkType(WLAN)
        //,setRequiredBatteryNotLow(true)
        //.setRequiresDeviceIdle(true)
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(info);
        if(resultCode == JobScheduler.RESULT_SUCCESS){
            Log.d(TAG, "Job scheduled");
        }else{
            Log.d(TAG, "Job scheduling failed");
        }
    }

    public void cancelJob(){
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancel(123);
        Log.d(TAG, "Job cancelled");
    }
}

