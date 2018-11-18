package com.bicyle.bicycle;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.bicyle.bicycle.Board.BoardActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainScreen extends AppCompatActivity {
    ListView listView;
    Toolbar mainToolbar;
    DrawerLayout dlDrawer;
    ActionBarDrawerToggle dtToggle;

    ListView frdListView;
    EditText frdEditText;
    ArrayList<String> frdList = new ArrayList<>();//데이터베이스에서 등록되어 있는 친구목록 저장
    Button frdSrchBtn;

    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    ProfDataSet myProf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        Intent intent = getIntent();
        myProf = (ProfDataSet) intent.getSerializableExtra("prof");

        mainToolbar = (Toolbar) findViewById(R.id.mainToolbar);
        dlDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        setSupportActionBar(mainToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        dtToggle = new ActionBarDrawerToggle(this, dlDrawer, R.string.app_name, R.string.app_name);
        dlDrawer.addDrawerListener(dtToggle);

        final String[] mainFuncList = getResources().getStringArray(R.array.mainFuncList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, mainFuncList);

        listView = (ListView) findViewById(R.id.drawer_menulist);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position) {
                    case 0:
                        Intent mapIntent = new Intent(getApplicationContext(), MapActivity.class);
                        startActivity(mapIntent);
                        break;
                    case 1:
                        Intent boardIntent = new Intent(getApplicationContext(), BoardActivity.class);
                        startActivity(boardIntent);
                        break;
                    case 2:
                        Toast.makeText(getApplicationContext(), "동호회선택", Toast.LENGTH_SHORT).show();
                        break;
                }
                dlDrawer.closeDrawer(Gravity.LEFT);
            }
        });

        frdSrchBtn = (Button) findViewById(R.id.frdSrchBtn);
        final ArrayAdapter frdAdpt = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, frdList);
        frdListView = (ListView) findViewById(R.id.frdList);
        frdListView.setAdapter(frdAdpt);

        //데이터 베이스에서 친구 목록 불러오는 부분
        mDatabase.child("FrdRelship").child(myProf.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnap : dataSnapshot.getChildren()) {
                    String frdNick = dataSnap.getValue().toString();
                    frdList.add(frdNick);
                    frdAdpt.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        frdEditText = (EditText) findViewById(R.id.frdSearch);
        frdEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            //친구 추가가 되어 있는 목록에서 친구검색
            @Override
            public void afterTextChanged(Editable s) {
                String filterText = s.toString();
                if(filterText.length() > 0) {
                    frdListView.setFilterText(filterText);
                }
                else {
                    frdListView.clearTextFilter();
                }
            }
        });

        //데이터 베이스에서 닉네임을 검색해서 친구 요청을 보내는 부분
        frdSrchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Query query = mDatabase.child("Profiles").orderByChild("nickname").equalTo(frdEditText.getText().toString());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        boolean existUser = dataSnapshot.exists();
                        if(existUser) {
//                            String snapData = dataSnapshot.getValue().toString();
//                            int indexOfToken = snapData.indexOf("deviceToken=");
//                            final String frdToken = snapData.substring(indexOfToken+12, snapData.length()-2);
                            for(final DataSnapshot dataSnap : dataSnapshot.getChildren()) {
                                AlertDialog.Builder alt_bld = new AlertDialog.Builder(v.getContext());
                                alt_bld.setMessage("친구 요청을 보내시겠습니까?").setCancelable(false)
                                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                MessageSender.getsInstance().inviteFrd(dataSnap.child("deviceToken").getValue().toString(), myProf.getUid(), myProf.getNickname(), dataSnap.child("nickname").getValue().toString());
                                                dialog.cancel();
                                            }
                                        }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                                AlertDialog alert = alt_bld.create();

                                alert.setTitle("친구요청");

                                alert.show();
                            }
                        }
                        else {
                            AlertDialog.Builder alt_bld = new AlertDialog.Builder(v.getContext());
                            alt_bld.setMessage("존재하지 않는 닉네임입니다.").setCancelable(false)
                                    .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog alert = alt_bld.create();

                            alert.setTitle("친구요청");

                            alert.show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        dtToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        dtToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (dtToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.mapsFunc:
                Toast.makeText(getApplicationContext(), "동호회선택", Toast.LENGTH_SHORT).show();
                break;
            case R.id.clubFunc:
                Toast.makeText(getApplicationContext(), "동호회선택", Toast.LENGTH_SHORT).show();
                break;
            case R.id.commuFunc:
                Toast.makeText(getApplicationContext(), "커뮤니티선택", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}