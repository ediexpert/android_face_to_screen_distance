package com.tec.fontsize;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainPageActivity extends Activity {
    private int distValue;
    private static Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        onClickOnSaveButton();
        // SEEKBAR listener
        SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar);
        final TextView seekBarValue = (TextView)findViewById(R.id.textView);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
//                msg = String.valueOf(progress);
                seekBarValue.setText(String.valueOf(progress)+" cm");
                distValue = progress;


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });
    }

    public void onClickOnSaveButton(){
        // setting face to screen distance to seekbar value
        saveButton = (Button)findViewById(R.id.button2);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity= new MainActivity();
                mainActivity.setDistance(distValue);
                Toast.makeText(getApplicationContext(), "Distance set to "+ distValue+ " cm", Toast.LENGTH_SHORT).show();
            }
        });

    }

}
