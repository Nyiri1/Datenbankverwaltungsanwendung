package org.o7planning.sqlietereal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

// Produktübersicht
public class Uebersichtactivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    DatabaseHelper myDb; // Database Schnittstelle

    //start change listview to recyclerview
    private ArrayList<ExampleItem> mExampleList; //Eine Liste um die verschiedenen Items für den Recycler View zu speichern
    private int mSizeExampleList;

    private RecyclerView mRecyclerView; // Schnittstelle zum Element im UI bekommt später einen Layout Manager und einen Adapter
    private ExampleAdapter mAdapter; //
    private RecyclerView.LayoutManager mLayoutManager;

    Button btnRefresh;

    Integer[] imgid = {0};
    String[] Id = null;
    String[] Barcode = null;
    String[] Produktname = null;
    String[] Mhd = null;
    String[] notified = null;
    Integer[] level = {0};
    Date[] parsedAndEditedMHDs = null;

    Integer tempImgId;
    String tempId;
    String tempBarcode;
    String tempProduktname;
    String tempMhd;
    String tempNotified;
    Integer tempLevel;
    Date tempparsedAndEditedMHDs;

    Integer[] colors;

    SimpleDateFormat dateFormat;


    //String[] Produktname={"Eier", "Milch", "Banane", "Apfel", "Orange", "Zitrone"};
    //String[] Mhd = {"10082021", "11082021", "12082021", "13082021", "14082021", "15082021", "16082021"};
    //Integer[] imgid={R.drawable.test, R.drawable.test, R.drawable.test, R.drawable.test, R.drawable.test, R.drawable.test, R.drawable.test};

    int i;
    int dataCount;
    int mPosition;
    String ReturnedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Debug", "Uebersichtactivity.onCreate() - Start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uebersichtactivity);

        setAppStatus(1); // 1 = Aktiv 0 = Inaktiv
        initToolbar();

        mRecyclerView = findViewById(R.id.recyclerView);

        initButtons();

        dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());

        initDBandFillArrays();

        i = 0;
    }

    public void setColors(){
        colors[0] = ContextCompat.getColor(this, R.color.level_0);
        colors[1] = ContextCompat.getColor(this, R.color.level_1);
        colors[2] = ContextCompat.getColor(this, R.color.level_2);
        colors[3] = ContextCompat.getColor(this, R.color.level_3);
        colors[4] = ContextCompat.getColor(this, R.color.level_4);
        colors[5] = ContextCompat.getColor(this, R.color.level_5);
        colors[9] = ContextCompat.getColor(this, R.color.level_9);
    }

    public void initDBandFillArrays(){
        Log.d("Debug", "initDBandFillArrays - Start");
        myDb = new DatabaseHelper(this); //Calls Constructor of DatabaseHelper
        myDb.updateLevel(this); //Compaes the mhd with actual date and determines the level

        Cursor data = myDb.getAllData(); // All data of the db will be saved in cursor

        dataCount = data.getCount(); // grabs the number of elements in the database

        //Create Arrays for all the Elements in the Database
        imgid = new Integer[dataCount];
        Id = new String[dataCount];//change to int
        Barcode = new String[dataCount];
        Produktname = new String[dataCount];
        Mhd = new String[dataCount];
        level = new Integer[dataCount];
        parsedAndEditedMHDs = new Date[dataCount];
        notified = new String[dataCount];

        colors = new Integer[10];
        setColors();

        //If there are more than 0 elements in the database
        Log.d("Debug", "DataCount in db > 0 ?");
        if (dataCount == 0) {
            Log.d("Debug", "No");
            Toast.makeText(Uebersichtactivity.this, "The Database was empty  :(.", Toast.LENGTH_LONG).show();
        } else {
            Log.d("Debug", "Yes");
            while (data.moveToNext()) {
                // fill Arrays
                imgid[i] = R.drawable.obstkorb;
                Id[i] = String.valueOf(data.getInt(0));
                Barcode[i] = data.getString(1);
                Produktname[i] = data.getString(2);
                Mhd[i] = data.getString(3);
                level[i] = data.getInt(4);
                notified[i] = data.getString(5);
                try {
                    // fill sortable MHDArray and take care of level 9 exception
                    if (level[i] == 9) parsedAndEditedMHDs[i] = dateFormat.parse("01019999");
                    else parsedAndEditedMHDs[i] = dateFormat.parse(Mhd[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(i==0) Log.d("Debug", "Now listing elements of myDB");
                Log.d("Debug", "imgid:" + imgid[i] + "Id:" + Id[i] + "Barcode:" + Barcode[i] + " Produktname:" + Produktname[i] + " Mhd:" + Mhd[i] + " level:" + level[i] + " notified:" + notified[i] + "parsedAndEditedMhds:" + parsedAndEditedMHDs[i]);
                i++;
            }
            Log.d("Debug", "DB transferred");

            DoSorting(true);

            createExampleList();
            Log.d("Debug", "After filling example list");
            Testausgabe();
            buildRecyclerView();


            Log.d("Debug", "initDBandFillArrays - End");
        }
    }

    public void DoSorting(Boolean init){
        //Sort algroithm Start
        //Iterates through all items and compares 2 adjacent items
        //if the higher indexed item is an earlier date both items will be swapped
        //Then the algorithm will continue the loop but will start a new loop if there was a change in the current loop
        // The algorithm will stop if it can do a full loop without finding a wrong order
        Log.d("Debug", "Uebersichtactivity.onCreate() - Starting sort algorithm");
        boolean sorted = false;
        if(!init) dataCount = mSizeExampleList;
        while (!sorted) {
            sorted = true;
            for (int i = 0; i < dataCount - 1; i++) {
                //compare Dates and returns 1 if date1 is greater than date2
                if (parsedAndEditedMHDs[i].compareTo(parsedAndEditedMHDs[i + 1]) == 1) {
                    tempparsedAndEditedMHDs = parsedAndEditedMHDs[i];
                    parsedAndEditedMHDs[i] = parsedAndEditedMHDs[i + 1];
                    parsedAndEditedMHDs[i + 1] = tempparsedAndEditedMHDs;

                    tempImgId = imgid[i];
                    imgid[i] = imgid[i + 1];
                    imgid[i + 1] = tempImgId;

                    tempId = Id[i];
                    Id[i] = Id[i + 1];
                    Id[i + 1] = tempId;

                    tempBarcode = Barcode[i];
                    Barcode[i] = Barcode[i + 1];
                    Barcode[i + 1] = tempBarcode;

                    tempProduktname = Produktname[i];
                    Produktname[i] = Produktname[i + 1];
                    Produktname[i + 1] = tempProduktname;

                    tempMhd = Mhd[i];
                    Mhd[i] = Mhd[i + 1];
                    Mhd[i + 1] = tempMhd;

                    tempLevel = level[i];
                    level[i] = level[i + 1];
                    level[i + 1] = tempLevel;

                    tempNotified = notified[i];
                    notified[i] = notified[i + 1];
                    notified[i + 1] = tempNotified;

                    sorted = false;
                }
            }
        }
        //Sort algorithm End
        Log.d("Debug", "initDBandFillArrays - successfully sorted");
    }

    public void initToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
    }

    public void initButtons(){
        btnRefresh = (Button) findViewById(R.id.button_refresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartActivity();
            }
        });
    }

    public void Testausgabe(){
        Log.d("AUSGEBUNG", "DATACOUNT="+dataCount );
        for(i=0;i<mSizeExampleList;i++){
            Log.d("Debug", "imgid:" + imgid[i] + "Id:" + Id[i] + "Barcode:" + Barcode[i] + " Produktname:" + Produktname[i] + " Mhd:" + Mhd[i] + " level:" + level[i] + " notified:" + notified[i] + "parsedAndEditedMhds:" + parsedAndEditedMHDs[i]);
            //Log.d("AUSGEBUNG", "Mhd: " + Mhd[i]);
            //Log.d("AUSGEBUNG", "imgid: " + imgid[i]);
            //Log.d("AUSGEBUNG", "level: " + level[i]);
            //Log.d("AUSGEBUNG", "notified" + notified[i]);
            //Log.d("AUSGEBUNG", "colors: " + colors[i]);
        }
        Log.d("AUSGEBUNG", "\n");
    }

    public void changeItem(int position, String text){
        mExampleList.get(position).changeText1(text);
        mAdapter.notifyItemChanged(position);
    }

    // Method fills the ArrayList used for the recyclerView with items
    public void createExampleList(){
        Log.d("Debug", "createExampleList - Start");
        mSizeExampleList = 0;
        mExampleList = new ArrayList<>();
        for(i=0; i<Produktname.length; i++) {
            mExampleList.add(new ExampleItem(imgid[i], Produktname[i], Mhd[i], colors[level[i]]));
            mSizeExampleList++;
        }
        Log.d("Debug", "createExampleList - End");
    }

    public void adaptSizeOfExampleList(int inkDek){
        mSizeExampleList = mSizeExampleList + inkDek;
    }


    public void buildRecyclerView(){
        Log.d("Debug", "buildRecyclerView - Start");
        mRecyclerView = findViewById(R.id.recyclerView); //Grabs UI Element
        mRecyclerView.setHasFixedSize(true); //makes size constant

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new ExampleAdapter(mExampleList); // Erstellen eines Objekts der Klasse ExampleAdapter mit Übergabe der Produktliste als Parameter
        mRecyclerView.setAdapter(mAdapter); // Adapter wird dem RecyclerView Objekt im UI zugewiesen

        // Set onClick listener for PopUp Menu
        mAdapter.setOnItemClickListener(new ExampleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                mPosition = position; //
                ReturnedId = Id[position]; // to delete the item from the database by id
                Log.d("Debug", "PopUpMenuOpenedForPosition:" + mPosition);
                showPopup(v);
            }
        });
        Log.d("Debug", "buildRecyclerView - End");
    }



    private void setAppStatus(int newStatus){
        SharedPreferences prefs = this.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("App_Status", newStatus);
        editor.apply();
    }

    @Override
    protected void onResume(){
        super.onResume();

        Log.d("TEST", "onResume");
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d("TEST", "onPause");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        setAppStatus(0);
    }

    public void showPopup(View v){
        Context wrapper = new ContextThemeWrapper(this, R.style.MyPopupMenu);
        PopupMenu popup = new PopupMenu(wrapper, v); //ex (this, v)
        popup.setOnMenuItemClickListener(this);
        popup.setGravity(Gravity.END);
        popup.inflate(R.menu.example_menu);
        popup.show();
    }


    public void swapItems(int lowerIndex, int higherIndex){
        int tempIndex;

        if(higherIndex<lowerIndex) {
            tempIndex = lowerIndex;
            lowerIndex = higherIndex;
            higherIndex = tempIndex;
        }else if(lowerIndex == higherIndex) {
            return;
        }


        //temp belegen
        tempImgId = imgid[lowerIndex];
        tempId = Id[lowerIndex];
        tempBarcode = Barcode[lowerIndex];
        tempProduktname = Produktname[lowerIndex];
        tempMhd = Mhd[lowerIndex];
        tempLevel = level[lowerIndex];
        tempNotified = notified[lowerIndex];
        tempparsedAndEditedMHDs = parsedAndEditedMHDs[lowerIndex];

        //Aufschieben
        for (i = lowerIndex; i < higherIndex; i++) {
            imgid[i] = imgid[i + 1];
            Id[i] = Id[i + 1];
            Barcode[i] = Barcode[i + 1];
            Produktname[i] = Produktname[i + 1];
            Mhd[i] = Mhd[i + 1];
            level[i] = level[i + 1];
            notified[i] = notified[i + 1];
            parsedAndEditedMHDs[i] = parsedAndEditedMHDs[i+1];
        }

        //Temp einschieben
        imgid[higherIndex] = tempImgId;
        Id[higherIndex] = tempId;
        Barcode[higherIndex] = tempBarcode;
        Produktname[higherIndex] = tempProduktname;
        Mhd[higherIndex] = tempMhd;
        level[higherIndex] = tempLevel;
        notified[higherIndex] = tempNotified;
        parsedAndEditedMHDs[higherIndex] = tempparsedAndEditedMHDs;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch(item.getItemId()){
            case R.id.enablewatcher:
                Toast.makeText(this, "Watcher enabled", Toast.LENGTH_SHORT).show();
                if(level[mPosition]==9){
                    Log.d("Debug", "Testausgabe enable watcher pre");
                    Testausgabe();
                    level[mPosition] = 5;
                    try{
                        parsedAndEditedMHDs[mPosition] = dateFormat.parse(Mhd[mPosition]);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    int grabbedId = Integer.valueOf(Id[mPosition]);
                    int newPosition = 0;
                    myDb.updateData(Id[mPosition], Barcode[mPosition], Produktname[mPosition], Mhd[mPosition], "5", "1");
                    mExampleList.remove(mPosition);
                    //resort list
                    DoSorting(false);
                    Log.d("Debug", "Testausgabe nach Sortierung enableWatcher");
                    Testausgabe();
                    Log.d("Debug", "Suche nach neuer Position zum wiedereinordnen");
                    Log.d("Debug", "Zu findende ID: " + grabbedId);
                    for(i=0;i<mSizeExampleList;i++){
                        Log.d("Debug", "Index: " + i + " - Id: " + Id[i]);
                        if(Integer.valueOf(Id[i])==grabbedId){
                            newPosition = i;
                            Log.d("Debug", "Übereinstimmung für Index: " + newPosition);
                        }
                    }
                    mExampleList.add(newPosition, new ExampleItem(imgid[newPosition], Produktname[newPosition], Mhd[newPosition], colors[level[newPosition]]));
                    Log.d("Debug", "Testausgabe enable watcher post");
                    Testausgabe();
                    /*mExampleList.add(mPosition, Data);*/
                    mAdapter.notifyDataSetChanged();
                }
                return true;
            case R.id.disablewatcher:
                Toast.makeText(this, "Watcher disabled", Toast.LENGTH_SHORT).show();
                if(level[mPosition]!=9){
                    Log.d("Debug", "Testausgabe disable watcher pre");
                    Testausgabe();
                    level[mPosition] = 9;
                    try{
                        parsedAndEditedMHDs[mPosition] = dateFormat.parse("01019999");
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    myDb.updateData(Id[mPosition], Barcode[mPosition], Produktname[mPosition], Mhd[mPosition], "9", "1"); //Database update
                    mExampleList.remove(mPosition); //Element im RecyclerView entfernen
                    mSizeExampleList--; //Größe vom RecyclerView verringern
                    mExampleList.add(mSizeExampleList, new ExampleItem(imgid[mPosition], Produktname[mPosition], Mhd[mPosition], colors[level[mPosition]])); //Element am Ende wieder einfügen
                    mSizeExampleList++; //Größe von RecyclerView erhöhen
                    swapItems(mPosition, mSizeExampleList-1); //In dem temporären Speicher Array auch nach hinten verschieben für weitere Disables und Enables
                    Log.d("Debug","Testausgabe disable watcher post");
                    Testausgabe();
                    mAdapter.notifyDataSetChanged();
                }
                return true;
            case R.id.deleteitem:
                Testausgabe();
                Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
                Log.d("Debug", "Deleting ID Number:" + ReturnedId);
                myDb.deleteData(ReturnedId);

                imgid[mPosition] = R.drawable.deleted;
                Log.d("Debug", "Removing Position:" + mPosition + "from example list");
                mExampleList.remove(mPosition);
                swapItems(mPosition, mSizeExampleList-1);
                mSizeExampleList--;
                Testausgabe();
                mAdapter.notifyDataSetChanged();
                return true;
            default:
                return false;
        }
    }

    private void restartActivity(){
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}
