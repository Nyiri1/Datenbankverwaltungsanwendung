package org.o7planning.sqlietereal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.xw.repo.BubbleSeekBar;

import static android.text.Html.fromHtml;

// Optionsmenü vom Nutzer
public class SettingsActivity extends AppCompatActivity {

    BubbleSeekBar mBbubbleSeekBar;

    Button buttons[] = new Button[8];
    int[] ListDrawableButtons = new int[]{R.drawable.button_1,
            R.drawable.button_2,
            R.drawable.button_3,
            R.drawable.button_4,
            R.drawable.button_5,
            R.drawable.button_6,
            R.drawable.button_7,};

    private static final String COLORS[] = {"#ff0000", "#ff8d00", "#ffdc07", "#80de06", "#919191", "#797979", "#616161"};
    private static final String CONST_BUFFER[] = {"none", "low", "min", "default", "high"};
    private static final String CONST_BUFFER_WARNING_LEVEL_INFORMATION[] = {
            "Im Panel:  Nein  \nTon:           Nein \nPopUp:      Nein",
            "Im Panel: (Nein) \nTon:           Nein \nPopUp:      Nein",
            "Im Panel:   Ja   \nTon:           Nein \nPopUp:      Nein",
            "Im Panel:   Ja   \nTon:            Ja  \nPopUp:      Nein",
            "Im Panel:   Ja   \nTon:            Ja  \nPopUp:       Ja "
    };


    Button btnSetDefaultSettings;
    TextView tvShowInfoLevel2, tvShowInfoWarningLevel, tvShowWarningLevelInformation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        //Markierung welcher Button gewählt wurde
        buttons[0] = (Button) findViewById(R.id.button_push1);
        buttons[1] = (Button) findViewById(R.id.button_push2);
        buttons[2] = (Button) findViewById(R.id.button_push3);
        buttons[3] = (Button) findViewById(R.id.button_push4);
        buttons[4] = (Button) findViewById(R.id.button_push5);
        buttons[5] = (Button) findViewById(R.id.button_push6);
        buttons[6] = (Button) findViewById(R.id.button_push7);
        buttons[7] = (Button) findViewById(R.id.button_invis8);

        buttons[7].setBackgroundResource(R.drawable.button_1_test);
        buttons[7].setVisibility(View.INVISIBLE);

        btnSetDefaultSettings = (Button) findViewById(R.id.button_setDefaultSettings);

        tvShowInfoLevel2 = (TextView) findViewById(R.id.textView_InformationLevel2); // Infoausgabe hier ist die Auswahl für das Benachrichtigungslevel
        tvShowInfoWarningLevel = (TextView) findViewById(R.id.textView_ShowInfoWarningLevel); // Infoausgabe hier ist die Auswahl für das Warnlevel
        tvShowWarningLevelInformation = (TextView) findViewById((R.id.textView_ShowWarningLevelInformation)); // Ausgabe welches Warnlevel ausgewählt wurde
        tvShowWarningLevelInformation.setText("");

        mBbubbleSeekBar = (BubbleSeekBar) findViewById(R.id.bubbleSeekBar);

        onClickPushButtons();

        int savedValue = getSettingsLevel2(this);
        tvShowInfoLevel2.setText("Warnung ab: "+savedValue+"Tagen");
        markButtonPushed(savedValue);

        savedValue = getSettingsWarningLevel(this);
        tvShowInfoWarningLevel.setText("Warnungslevel auswählen:");
        mBbubbleSeekBar.setProgress(savedValue);

        mBbubbleSeekBar.setCustomSectionTextArray(new BubbleSeekBar.CustomSectionTextArray() {
            @NonNull
            @Override
            public SparseArray<String> onCustomize(int sectionCount, @NonNull SparseArray<String> array) {
                array.clear();
                array.put(0, "none");
                array.put(1, "min");
                array.put(2, "low");
                array.put(3, "default");
                array.put(4, "max");

                return array;
            }
        });

        SeekbarListener();
    }

    private void markButtonPushed(int id){
        id=id-1;
        for(int i = 0; i<7; i++){
            if(i==id) {
                buttons[i].setBackgroundResource(ListDrawableButtons[i]);
            }
            else{
                buttons[i].setBackgroundColor(Color.parseColor(COLORS[i]));
            }
        }
    }

    //Function begin
    private void onClickPushButtons(){
        buttons[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSettingsLevel2(1);
            }
        });

        buttons[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSettingsLevel2(2);
            }
        });

        buttons[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSettingsLevel2(3);
            }
        });

        buttons[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSettingsLevel2(4);
            }
        });

        buttons[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSettingsLevel2(5);
            }
        });

        buttons[5].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSettingsLevel2(6);
            }
        });

        buttons[6].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSettingsLevel2(7);
            }
        });

        btnSetDefaultSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSettingsLevel2(4);
                setSettingsWarningLevel(1);
                mBbubbleSeekBar.setProgress(1);
            }
        });
    }//Function end

    private void SeekbarListener(){
        mBbubbleSeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                //tvShowInfoWarningLevel.setText("Warnungslevel auswählen: current: " + CONST_BUFFER[mBbubbleSeekBar.getProgress()+2]);
                tvShowWarningLevelInformation.setText(CONST_BUFFER_WARNING_LEVEL_INFORMATION[mBbubbleSeekBar.getProgress()+2]);
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                //tvShowInfoWarningLevel.setText("getProgressOnActionUp");
                setSettingsWarningLevel(mBbubbleSeekBar.getProgress());
            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                //tvShowInfoWarningLevel.setText("getProgressOnFinally");
            }
        });
    }


    static public int getSettingsLevel2(Context context){
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return prefs.getInt("Saved_Level2_Level", 4);
    }

    private void setSettingsLevel2(Integer setting){
        SharedPreferences prefs = this.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("Saved_Level2_Level", setting);
        editor.apply();

        int savedValue = getSettingsLevel2(this);
        tvShowInfoLevel2.setText("Warnung ab: "+ savedValue +" Tagen");
        markButtonPushed(savedValue);
        //myDb.updateLevel(this); maybe for later use X13
    }

    static public int getSettingsWarningLevel(Context context){
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return prefs.getInt("Saved_Notification_Importance_Level", 1); //adapt default value!!!!
    }


    //-2 bis 2 definitionen sind oben
    private void setSettingsWarningLevel(Integer setting){
        SharedPreferences prefs = this.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("Saved_Notification_Importance_Level", setting);
        editor.apply();
    }
}
