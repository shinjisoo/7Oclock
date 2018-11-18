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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.bicyle.bicycle.Data.DataManager;
import com.bicyle.bicycle.R;
import com.bicyle.bicycle.util.MyUtil;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import java.util.ArrayList;
import java.util.Collections;

public class BoardDetailActivity extends AppCompatActivity implements View.OnClickListener {
    ListView replyListView;
    BoardDTO board;
    View writeReplyDialogView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_detail);

        TextView readTitleTV = findViewById(R.id.boardDetailTitleTV);
        TextView readWriterTV = findViewById(R.id.boardDetailWriterTV);
        TextView readBoardKindTV = findViewById(R.id.boardDetailBoardKindTV);
        TextView readBodyTV = findViewById(R.id.boardDetailBodyTV);
        TextView readLikeTV = findViewById(R.id.boardDetailLikeTV);
        replyListView= findViewById(R.id.replyListView);
        Button replyWriteBtn=findViewById(R.id.replyWriteBtn);
        Button boardDeleteBtn = findViewById(R.id.board_detail_deleteBtn);
        boardDeleteBtn.setOnClickListener(this);
        replyWriteBtn.setOnClickListener(this); //댓글입력

        final ArrayList<ReplyDTO> replyList = new ArrayList<>();
        final ReplyAdapter replyAdapter;

        board= (BoardDTO) getIntent().getSerializableExtra("board");


        readTitleTV.setText(board.getTitle());
        readWriterTV.setText(board.getWriter());
        readBoardKindTV.setText(MyUtil.getBoardKind(board.getBoardKind()));
        readBodyTV.setText(board.getBody());
        readLikeTV.setText(""+board.getLikeNum());

        replyAdapter=new ReplyAdapter(BoardDetailActivity.this,R.layout.replylist_row, replyList);
        replyListView.setAdapter(replyAdapter);


        //리플클릭시삭제가능
        replyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ReplyDTO curItem = (ReplyDTO)replyAdapter.getItem(position);

                //dialog 생성, key 비교후 삭제
                if(DataManager.getInstance().userName.equals(curItem.getWriter()))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(BoardDetailActivity.this);
                    //옵션 설정
                    builder.setTitle("댓글삭제");
                    builder.setMessage("정말 삭제하시겠습니까?");
                    //AlertDialog 모양 설정 => 확인/취소 버튼 포함하는 AlertDialog
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            DataManager.getInstance().databaseReference.child("board").child(board.getKey()).child("reply").child(curItem.getKey()).removeValue();
                            Toast.makeText(BoardDetailActivity.this,"삭제되었습니다.",Toast.LENGTH_LONG).show();
                        }
                    });
                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

                    // AlertDialog 보이기
                    builder.show();
                }
                else
                {
                    Toast.makeText(BoardDetailActivity.this,DataManager.getInstance().userName+"은 "+board.getWriter()+"의 댓글을 삭제할 수 없습니다.",Toast.LENGTH_LONG).show();
                }





            }
        });





        //board의 변화 감지
        DataManager.getInstance().databaseReference.child("board").child(board.getKey()).child("reply").addChildEventListener(new ChildEventListener() {

            @Override // 리스트아이템을 검색하거나 아이템의 추가가 있을 때 수신
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Log.d("BoardDeatilActivity", "addChildEventListener");
                Log.d("BoardDeatilActivity", dataSnapshot.getValue().toString());
                Log.d("BoardDeatilActivity", dataSnapshot.getKey());
                ReplyDTO replyData = dataSnapshot.getValue(ReplyDTO.class);//
                replyData.setKey( dataSnapshot.getKey());
                replyList.add(replyData);
                Collections.sort(replyList,DataManager.getInstance().descendingReply);
                replyAdapter.notifyDataSetChanged(); //adapter 갱신
            }

            @Override // 아이템의 변화가 있을 때 수신
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d("BoardDeatilActivity", "onChildChanged");
            }

            @Override // 아이템이 삭제되었을때 수신
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d("BoardDeatilActivity", "onChildRemoved");
                Log.d("BoardDeatilActivity", dataSnapshot.toString());
                ReplyDTO replyData = dataSnapshot.getValue(ReplyDTO.class);//
                replyData.setKey( dataSnapshot.getKey());
                //Log.d("BoardActivity", ""+boardData.getKey()+boardData.getWriter()+boardData.getDate()+boardData.getTitle());
                for(ReplyDTO reply : replyList)
                {
                    if(reply.getKey().equals(replyData.getKey())) //key가 같은지?
                    {
                        replyList.remove(reply);
                        break;
                    }
                }

                Collections.sort(replyList,DataManager.getInstance().descendingReply);
                replyAdapter.notifyDataSetChanged(); //adapter 갱신



            }

            @Override //순서 변경시 수신
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d("BoardDeatilActivity", "onChildMoved");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("BoardDeatilActivity", "onCancelled");

            }
        });

    }

    //리플작성
    public void writeReply()
    {
        //dialog 화면 view 객체
        writeReplyDialogView =View.inflate(BoardDetailActivity.this,R.layout.writereplydialog,null);

        final EditText writeReplyDialog_editText= writeReplyDialogView.findViewById(R.id.writeReplyDialog_editText);

        //AlertDialog 생성
        AlertDialog.Builder builder=new AlertDialog.Builder(BoardDetailActivity.this);

        //옵션 설정
        builder.setTitle("댓글 작성");
        //builder.setIcon(android.R.drawable.ic_menu_save);
        builder.setView(writeReplyDialogView); //dialogview를 builder에 붙임

        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //여기서 push함
                ReplyDTO replyExample= new ReplyDTO(MyUtil.getDate(),writeReplyDialog_editText.getText().toString().trim(),DataManager.getInstance().userName);
                DataManager.getInstance().databaseReference.child("board").child(board.getKey()).child("reply").push().setValue(replyExample); //board에 data push


            }
        });
        builder.setNegativeButton("취소",null);

        //보여주기
        builder.show();




    }

    //게시글 삭제
    public void deleteBoard()
    {
        if(DataManager.getInstance().userName.equals(board.getWriter()))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(BoardDetailActivity.this);
            //옵션 설정
            builder.setTitle("게시글삭제");
            builder.setMessage("정말 삭제하시겠습니까?");
            //AlertDialog 모양 설정 => 확인/취소 버튼 포함하는 AlertDialog
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    DataManager.getInstance().databaseReference.child("board").child(board.getKey()).removeValue();
                    Toast.makeText(BoardDetailActivity.this,"삭제되었습니다.",Toast.LENGTH_LONG).show();
                    BoardDetailActivity.this.finish();
                }
            });
            builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            // AlertDialog 보이기
            builder.show();


        }
        else
        {
            Toast.makeText(BoardDetailActivity.this,DataManager.getInstance().userName+"은 "+board.getWriter()+"의 글을 삭제할 수 없습니다.",Toast.LENGTH_LONG).show();
        }


    }
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.board_detail_deleteBtn:
                deleteBoard();
                break;
            case R.id.replyWriteBtn :
                writeReply();
                break;
        }


    }
}
