package com.bicyle.bicycle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class MapPopupActivity extends Activity {
    TextView txtText;
    Button route_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.mappopup_activity);

        //UI 객체생성
        txtText = (TextView)findViewById(R.id.map_popup_Text);
        route_button = (Button)findViewById(R.id.map_routefind_button);

        //데이터 가져오기
        Intent intent = getIntent();
        String data = intent.getStringExtra("data");
        if(!data.equals("해당 결과가 없습니다.")){
            route_button.setVisibility(View.VISIBLE);
        }
        txtText.setText(data);
    }

    //확인 버튼 클릭
    public void mOnClose(View v){
        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("result", "OK");
        setResult(RESULT_OK, intent);

        //액티비티(팝업) 닫기
        finish();
    }

    public void mOnRoute(View v){
        Intent intent = new Intent();
        intent.putExtra("result", "Find Route");
        setResult(RESULT_OK, intent);

        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }
}