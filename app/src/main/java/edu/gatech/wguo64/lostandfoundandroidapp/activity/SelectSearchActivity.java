package edu.gatech.wguo64.lostandfoundandroidapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import edu.gatech.wguo64.lostandfoundandroidapp.R;

public class SelectSearchActivity extends AppCompatActivity implements View.OnClickListener{
    public Button lostSearchBtn, foundSearchBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_search);
        inflateViews();
        setUIs();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lostSearchBtn:
                startLostSearchActivity();
                break;
            case R.id.foundSearchBtn:
                startFoundSearchActivity();
                break;
        }
    }

    private void inflateViews() {
        lostSearchBtn = (Button)findViewById(R.id.lostSearchBtn);
        foundSearchBtn = (Button)findViewById(R.id.foundSearchBtn);
    }

    private void setUIs() {
        lostSearchBtn.setOnClickListener(this);
        foundSearchBtn.setOnClickListener(this);
    }

    private void startLostSearchActivity() {
        Intent intent = new Intent(this, SearchLostActivity.class);
        startActivity(intent);
    }

    private void startFoundSearchActivity() {
        Intent intent = new Intent(this, SearchFoundActivity.class);
        startActivity(intent);
    }

}
