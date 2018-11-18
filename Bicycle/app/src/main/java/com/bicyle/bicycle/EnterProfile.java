package com.bicyle.bicycle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EnterProfile extends AppCompatActivity {
    ProfDataSet prof = new ProfDataSet();
    private String uid;
    private String devToken;

    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_profile);

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        devToken = intent.getStringExtra("devToken");

        final EditText nicknameEtext = (EditText) findViewById(R.id.nicknameEtext);

        RadioGroup genderRgroup = (RadioGroup) findViewById(R.id.genderRgroup);
        genderRgroup.clearCheck();

        Button submitBtn = (Button) findViewById(R.id.submitBtn);

        genderRgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.maleRbtn: prof.setGender("M");
                        break;
                    case R.id.femaleRbtn: prof.setGender("F");
                        break;
                }
            }
        });

        final String[] locList = getResources().getStringArray(R.array.locList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, locList);
        Spinner locSpinner = (Spinner)findViewById(R.id.locSpinner);
        locSpinner.setAdapter(adapter);
        locSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prof.setLocation(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nicknameEtext.getText().toString().isEmpty() || prof.getLocation().equals("") || prof.getGender().equals("")) {
                    Toast.makeText(EnterProfile.this, "필요한 정보가 부족합니다.", Toast.LENGTH_SHORT).show();
                }
                else {
                    prof.setNickname(nicknameEtext.getText().toString());
                    prof.setUid(uid);
                    prof.setDeviceToken(devToken);
                    mDatabase.child("Profiles").child(uid).setValue(prof);
                    Intent intent = new Intent(getApplicationContext(), MainScreen.class);
                    intent.putExtra("prof", prof);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

}
