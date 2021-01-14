package com.example.hm_project.view.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hm_project.Command.EditTextInput;
import com.example.hm_project.R;
import com.example.hm_project.data.NotifyData;
import com.example.hm_project.view.activity.MainActivity;

import java.util.ArrayList;


/***
 *  알람 어댑터
 *  1 - 생성자
 *  2 - onCreateViewHolder ( 뷰홀더 객체화 )
 *  3 - NotifyViewHolder 정의
 *  4 - onBindViewHolder 정의 ( 뷰홀더에서 정의한 내용을 실제로 적용함 )
 *  5 - getItemCount ( 뷰가 0개인지 아닌지 판별해준다. )
 *  6 - getnList ( nList 반환하는 메서드 최종적으로 결과값을 서버로 보낼 때 사용한다. )
 */

public class NotifyAdapter extends RecyclerView.Adapter<NotifyAdapter.NotifyViewHolder> {

    private ArrayList<NotifyData> nList;

    public interface OnItemClickListener {
        void onItemClick(View v, int pos);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View v, int pos);
    }

    // 리스너 객체 참조를 저장하는 변수
    private NotifyAdapter.OnItemClickListener mListener = null;
    private NotifyAdapter.OnItemLongClickListener mLongListener = null;


    public void setOnItemClickListener(NotifyAdapter.OnItemClickListener listener) {
        this.mListener = listener;
    }

    public void setOnItemLongClickListener(NotifyAdapter.OnItemLongClickListener listener) {
        this.mLongListener = listener;
    }

    // 1 - 생성자
    public NotifyAdapter(ArrayList<NotifyData> list) {
        this.nList = list;

    }

    // 2 -  onCreateViewHolder ( 뷰홀더 객체화 )
    @NonNull
    @Override
    public NotifyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.notify_item, viewGroup, false);

        return new NotifyViewHolder(view);
    }

    // 3 - NotifyViewHolder 정의
    public class NotifyViewHolder extends RecyclerView.ViewHolder {

        public ConstraintLayout NLayout;
        public TextView NTitle;
        public TextView NDate;
        public TextView NTime;
        public Switch NSwitch;

        public NotifyViewHolder(View view) {
            super(view);

            this.NLayout = (ConstraintLayout) view.findViewById(R.id.notifyLayout);
            this.NTitle = (TextView) view.findViewById(R.id.notifyTitle);
            this.NDate = (TextView) view.findViewById(R.id.notifyDate);
            this.NTime = (TextView) view.findViewById(R.id.notifyTime);
            this.NSwitch = (Switch) view.findViewById(R.id.notifySwitch);

            NSwitch.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    if (NSwitch.isChecked())
                        nList.get(pos).setNonoff("1");
                    else {
                        nList.get(pos).setNonoff("0");
                    }
                    Log.i("스위치버튼 클릭합니다~~~~ : ", Integer.toString(pos));
                }
            });
            view.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    if (mListener != null) {
                        mListener.onItemClick(v, pos);
                    }
                }
            });
            view.setOnLongClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    mLongListener.onItemLongClick(v, pos);
                }
                return true;
            });
        }
    }

    // 4 - onBindViewHolder 정의 ( 뷰홀더에서 정의한 내용을 실제로 적용함 )
    @Override
    public void onBindViewHolder(@NonNull NotifyViewHolder viewHolder, int position) {

        // 알람 창 크기 조절
        ViewGroup.LayoutParams params = viewHolder.NLayout.getLayoutParams();
        int height = (MainActivity.height / 8);
        params.width = MainActivity.width;
        params.height = height;
        viewHolder.NLayout.setLayoutParams(params);

        // 알람 제목 로드
        if (!EditTextInput.checkNPE(nList.get(position).getNTitle())) {
            viewHolder.NTitle.setText(nList.get(position).getNTitle());
        }
        // 알람 날짜 로드
        if (!EditTextInput.checkNPE(nList.get(position).getNDate())) {
            viewHolder.NDate.setText(nList.get(position).getNDate());
        }
        // 알람 시간 로드
        if (!EditTextInput.checkNPE(nList.get(position).getNTime())) {
            viewHolder.NTime.setText(nList.get(position).getNTime());
        }
        // on/off 버튼 설정
        if (nList.get(position).getNonoff().equals("1")) {
            viewHolder.NSwitch.setChecked(true);
        } else {
            viewHolder.NSwitch.setChecked(false);
        }


    }

    // 5 - getItemCount ( 뷰가 0개인지 아닌지 판별해준다. )
    @Override
    public int getItemCount() {
        return (null != nList ? nList.size() : 0);
    }

    // 6 - getnList ( nList 반환하는 메서드 최종적으로 결과값을 서버로 보낼 때 사용한다. )
    public ArrayList<NotifyData> getnList() {
        return nList;
    }
}
