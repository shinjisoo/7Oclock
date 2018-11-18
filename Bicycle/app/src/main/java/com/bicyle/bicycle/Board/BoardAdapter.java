package com.bicyle.bicycle.Board;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.bicyle.bicycle.R;
import com.bicyle.bicycle.util.MyUtil;

import java.util.ArrayList;

public class BoardAdapter extends BaseAdapter implements Filterable {
    Filter listFilter ;
    Context mContext;
    int layout;
    ArrayList<BoardDTO> boardList;
    ArrayList<BoardDTO> filteredBoardList;
    LayoutInflater inflater;

    public BoardAdapter(Context context, int layout, ArrayList<BoardDTO> boardList) //layout int는 id값
    {
        mContext=context;
        this.layout=layout;
        this.boardList=boardList;
        filteredBoardList = boardList;
        inflater= (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }
    @Override
    public int getCount() {
        return filteredBoardList.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredBoardList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        if(convertView==null)
        {
            convertView=inflater.inflate(layout,null);

        }
        TextView titleTV=convertView.findViewById(R.id.titleTV);titleTV.setSelected(true);
        TextView writerTV=convertView.findViewById(R.id.writerTV);writerTV.setSelected(true);
        TextView dateTV=convertView.findViewById(R.id.dateTV);dateTV.setSelected(true);
        TextView likeTV=convertView.findViewById(R.id.likeTV);likeTV.setSelected(true);
        TextView boardKindTV= convertView.findViewById(R.id.boardkindTV);boardKindTV.setSelected(true);


        BoardDTO dto=filteredBoardList.get(position);

        titleTV.setText(dto.getTitle());
        writerTV.setText(dto.getWriter());
        dateTV.setText(dto.getDate());
        likeTV.setText(""+dto.getLikeNum());

        String boardString=MyUtil.getBoardKind(dto.getBoardKind());
        boardKindTV.setText(boardString);


        return convertView;
    }



    @Override
    public Filter getFilter()
    {
        if (listFilter == null)
        {
            listFilter = new ListFilter() ;
        }
        return listFilter ;
    }

    public void deleteFilter()
    {
        listFilter=null;
    }

    private class ListFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults() ;

            if (constraint == null || constraint.length() == 0) {
                results.values = boardList ;
                results.count = boardList.size() ;
            } else {
                ArrayList<BoardDTO> itemList = new ArrayList<BoardDTO>() ;

                for (BoardDTO item : boardList) {
                    Log.d("BoardActivity", item.getKey());
                    String boardKind = item.getBoardKind()+"";
                    if (boardKind.trim().equals(constraint.toString().trim()))
                    {
                        itemList.add(item) ;
                    }
                }

                results.values = itemList ;
                results.count = itemList.size() ;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            // update listview by filtered data list.
            filteredBoardList = (ArrayList<BoardDTO>) results.values ;

            // notify
            if (results.count > 0) {
                notifyDataSetChanged() ;
            } else {
                notifyDataSetInvalidated() ;
            }
        }
    }


}

