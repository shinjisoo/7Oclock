package com.bicyle.bicycle.Board;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.bicyle.bicycle.Data.DataManager;
import com.bicyle.bicycle.R;
import com.bicyle.bicycle.util.MyUtil;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;

public class BoardActivity extends AppCompatActivity implements View.OnClickListener {

    ListView boardListView;
    ListView searchListView;
    EditText searchEditText;
    TextView kindTV;
    TextView likeTV;
    Button writeBtn;
    Button searchBtn;
    Button refreshBtn;
    Button myBoardBtn;
    View writeBoardDialogView;
    ArrayList<BoardDTO> boardList = new ArrayList<>();
    BoardAdapter boardAdapter;
    ArrayList<BoardDTO> searchBoardList = new ArrayList<>();
    BoardAdapter searchBoardAdapter;
    boolean searchState = false; //search상태?
    int boardKindFilterControl = 0;
    int boardLikeFilterControl = 0;
    @Override
    protected void onDestroy() {
        Log.d("BoardActivity", "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        boardListView = findViewById(R.id.boardListView);
        writeBtn = findViewById(R.id.board_writeBtn);
        searchBtn = findViewById(R.id.board_searchBtn);
        refreshBtn = findViewById(R.id.board_refreshBtn);
        myBoardBtn = findViewById(R.id.board_myBoardBtn);
        searchListView= findViewById(R.id.searchListView);
        searchEditText=findViewById(R.id.board_searchEditText);
        kindTV = findViewById(R.id.board_kindTV);
        likeTV = findViewById(R.id.board_LikeTV);


        DataManager.getInstance().firebaseDatabase = FirebaseDatabase.getInstance();
        DataManager.getInstance().databaseReference = DataManager.getInstance().firebaseDatabase.getReference();
        boardAdapter=new BoardAdapter(getApplicationContext(),R.layout.boardlist_row, boardList); //전체게시판
        boardListView.setAdapter(boardAdapter);
        searchBoardAdapter=new BoardAdapter(getApplicationContext(),R.layout.boardlist_row, searchBoardList); //검색된게시판
        searchListView.setAdapter(searchBoardAdapter);

        searchBtn.setOnClickListener(this);
        myBoardBtn.setOnClickListener(this);
        writeBtn.setOnClickListener(this);
        refreshBtn.setOnClickListener(this);
        kindTV.setOnClickListener(this);
        likeTV.setOnClickListener(this);

        Log.d("BoardActivity", "onCreate");




        //board의 변화 감지
        DataManager.getInstance().databaseReference.child("board").addChildEventListener(new ChildEventListener() {
            /*
            기존 하위 항목마다 한 번씩 발생한 후 지정된 경로에 하위 항목이 새로 추가될 때마다 다시 발생합니다.
            새 하위 항목의 데이터를 포함하는 스냅샷이 이벤트 콜백에 전달됩니다.
            정렬을 위해 이전 하위 항목의 키를 포함하는 두 번째 인수도 전달됩니다.

             */
            @Override // 리스트아이템을 검색하거나 아이템의 추가가 있을 때 수신
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Log.d("BoardActivity", "onChildAdded");
                Log.d("BoardActivity", dataSnapshot.toString());
                BoardDTO boardData = dataSnapshot.getValue(BoardDTO.class);//
                boardData.setKey( dataSnapshot.getKey());

                boardList.add(boardData);


                Collections.sort(boardList,DataManager.getInstance().descendingBoard);
                boardAdapter.notifyDataSetChanged(); //adapter 갱신



            }

            @Override // 아이템의 변화가 있을 때 수신
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d("BoardActivity", "onChildChanged");
            }

            @Override // 아이템이 삭제되었을때 수신
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d("BoardActivity", "onChildRemoved");
                Log.d("BoardActivity", dataSnapshot.toString());
                BoardDTO boardData = dataSnapshot.getValue(BoardDTO.class);//
                boardData.setKey( dataSnapshot.getKey());
                //Log.d("BoardActivity", ""+boardData.getKey()+boardData.getWriter()+boardData.getDate()+boardData.getTitle());
                for(BoardDTO board : boardList)
                {
                    if(board.getKey().equals(boardData.getKey())) //key가 같은지?
                    {
                        boardList.remove(board);
                        break;
                    }
                }

                Collections.sort(boardList,DataManager.getInstance().descendingBoard);
                boardAdapter.notifyDataSetChanged(); //adapter 갱신
            }

            @Override //순서 변경시 수신
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d("BoardActivity", "onChildMoved");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("BoardActivity", "onCancelled");

            }
        });

        //board 클릭시 item띄우기
        boardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BoardDTO curItem= (BoardDTO)boardAdapter.getItem(position);


                Intent intent= new Intent(getApplicationContext(), BoardDetailActivity.class);
                intent.putExtra("board",curItem);
                startActivity(intent);



            }
        });
        //searchBoard 클릭시 item띄우기
        searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BoardDTO curItem= (BoardDTO)boardAdapter.getItem(position);


                Intent intent= new Intent(getApplicationContext(), BoardDetailActivity.class);
                intent.putExtra("board",curItem);
                startActivity(intent);


            }
        });


    }

    public void boadrFilter(boolean searchState, int boardKindFilterControl2)
    {
        if(searchState) //search한 상태의 listview
        {
            //search listview에서
            if(boardKindFilterControl2==0)
            {
                searchBoardAdapter.getFilter().filter("0");
            }
            else if(boardKindFilterControl2==1)
            {
                searchBoardAdapter.getFilter().filter("1");
            }
            else if(boardKindFilterControl2==2)
            {
                searchBoardAdapter.getFilter().filter("2");
            }

            else if(boardKindFilterControl2==3)
            {
                searchBoardAdapter.getFilter().filter("3");
            }
            else
            {
                searchBoardAdapter.getFilter().filter("");
                boardKindFilterControl=0;
            }
            boardKindFilterControl++;
        }
        else //기본 전체 listview
        {
            //boardAdapter.getFilter().filter(""+boardKindFilterControl);
            if(boardKindFilterControl==0)
            {
                boardAdapter.getFilter().filter("0");

            }
            else if(boardKindFilterControl==1)
            {
                boardAdapter.getFilter().filter("1");

            }
            else if(boardKindFilterControl==2)
            {
                boardAdapter.getFilter().filter("2");

            }

            else if(boardKindFilterControl==3)
            {
                boardAdapter.getFilter().filter("3");

            }
            else
            {        boardAdapter.getFilter().filter("");
                boardKindFilterControl=0;
            }
            boardKindFilterControl++;
        }
    }


    @Override
    public void onClick(View v) {

        switch(v.getId())
        {
            //검색
            case R.id.board_searchBtn :
                boardListView.setVisibility(View.GONE);
                searchListView.setVisibility(View.VISIBLE);
                String text= searchEditText.getText().toString();

                searchBoardList.clear();
                for(BoardDTO board : boardList)
                {
                    if(board.getTitle().contains(text) || board.getBody().contains(text)) //제목과 내용에 포함되어있는지?
                    {
                        searchBoardList.add(board);
                    }
                }

                searchBoardAdapter.notifyDataSetChanged(); //adapter 갱신
                searchState= true;
                break;
            //내가쓴글
            case R.id.board_myBoardBtn :
                boardListView.setVisibility(View.GONE);
                searchListView.setVisibility(View.VISIBLE);

                searchBoardList.clear();
                for(BoardDTO board : boardList)
                {
                    if(board.getWriter().contains(DataManager.getInstance().userName)) //userName과 일치하는 board
                    {
                        searchBoardList.add(board);
                    }
                }
                searchBoardAdapter.notifyDataSetChanged(); //adapter 갱신
                searchState= true;
                break;
            //작성
            case R.id.board_writeBtn :
                //dialog 화면 view 객체
                writeBoardDialogView =View.inflate(BoardActivity.this,R.layout.writeboarddialog,null);

                final EditText titleET= writeBoardDialogView.findViewById(R.id.titleET);
                final EditText boardKindET= writeBoardDialogView.findViewById(R.id.boardKindET);
                final EditText bodyET= writeBoardDialogView.findViewById(R.id.bodyET);

                //AlertDialog 생성
                AlertDialog.Builder builder=new AlertDialog.Builder(BoardActivity.this);

                //옵션 설정
                builder.setTitle("게시글 작성");
                //builder.setIcon(android.R.drawable.ic_menu_save);
                builder.setView(writeBoardDialogView); //dialogview를 builder에 붙임

                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //여기서 push함
                        BoardDTO boardExample = new BoardDTO(titleET.getText().toString(),DataManager.getInstance().userName, Integer.parseInt(boardKindET.getText().toString()),
                                bodyET.getText().toString(), MyUtil.getDate(), 0);
                        DataManager.getInstance().databaseReference.child("board").push().setValue(boardExample); //board에 data push
                        boardAdapter.notifyDataSetChanged(); //adapter 갱신
                    }
                });
                builder.setNegativeButton("취소",null);

                //보여주기
                builder.show();
                break;

            //새로고침
            case R.id.board_refreshBtn :
                boardListView.setVisibility(View.VISIBLE);
                searchListView.setVisibility(View.GONE);
                searchState= false;
                break;


            //종류 별 필터링
            case R.id.board_kindTV :
                boadrFilter(searchState,boardKindFilterControl);
                break;


            //추천수 필터링
            case R.id.board_LikeTV :
                if(searchState) //search한 상태의 listview
                {
                    if(boardLikeFilterControl ==0) //기본상태 -> 추천순으로
                    {
                        Collections.sort(searchBoardList,DataManager.getInstance().likeFilterBoard);
                        searchBoardAdapter.notifyDataSetChanged(); //adapter 갱신
                        boardLikeFilterControl =1;
                    }
                    else
                    {
                        Collections.sort(searchBoardList,DataManager.getInstance().descendingBoard);
                        searchBoardAdapter.notifyDataSetChanged(); //adapter 갱신
                        boardLikeFilterControl =0;
                    }

                }
                else //기본 전체 listview
                {
                    if(boardLikeFilterControl ==0) //기본상태 -> 추천순으로
                    {
                        Collections.sort(boardList,DataManager.getInstance().likeFilterBoard);
                        boardAdapter.notifyDataSetChanged(); //adapter 갱신
                        boardLikeFilterControl =1;
                    }
                    else
                    {
                        Collections.sort(boardList,DataManager.getInstance().descendingBoard);
                        boardAdapter.notifyDataSetChanged(); //adapter 갱신
                        boardLikeFilterControl =0;
                    }
                }

                break;
        }




    }


}
