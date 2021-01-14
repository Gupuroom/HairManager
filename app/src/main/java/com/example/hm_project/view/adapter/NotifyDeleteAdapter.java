package com.example.hm_project.view.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hm_project.Command.EditTextInput;
import com.example.hm_project.R;
import com.example.hm_project.data.NotifyData;
import com.example.hm_project.view.activity.MainActivity;

import java.util.ArrayList;
import java.util.List;

/***
 *  알람 어댑터
 *  1 - 생성자
 *  2 - onCreateViewHolder ( 뷰홀더 객체화 )
 *  3 - NotifyViewHolder 정의
 *  4 - onBindViewHolder 정의 ( 뷰홀더에서 정의한 내용을 실제로 적용함 )
 *  5 - onBindViewHolder 정의 ( 삭제모드에서 한개의 뷰를 클릭했을 때 그 한개의 뷰만 바인드해주는 메서드 )
 *  6 - getItemCount ( 뷰가 0개인지 아닌지 판별해준다. )
 */

public class NotifyDeleteAdapter extends RecyclerView.Adapter<NotifyDeleteAdapter.NotifyDeleteViewHolder> {

    private ArrayList<NotifyData> nList;

    public interface OnItemClickListener {
        void onItemClick(View v, int pos);
    }

    // 리스너 객체 참조를 저장하는 변수
    private NotifyDeleteAdapter.OnItemClickListener mListener = null;


    public void setOnItemClickListener(NotifyDeleteAdapter.OnItemClickListener listener) {
        this.mListener = listener;
    }


    // 1 - 생성자
    public NotifyDeleteAdapter(ArrayList<NotifyData> list) {
        this.nList = list;
    }

    // 2 - onCreateViewHolder ( 뷰홀더 객체화 )
    @NonNull
    @Override
    public NotifyDeleteViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.notify_delete_item, viewGroup, false);

        return new NotifyDeleteViewHolder(view);
    }

    // 3 - NotifyViewHolder 정의
    public class NotifyDeleteViewHolder extends RecyclerView.ViewHolder {

        public ConstraintLayout NLayout;
        public CheckBox NCheck;
        public TextView NTitle;
        public TextView NDate;
        public TextView NTime;

        public NotifyDeleteViewHolder(View view) {
            super(view);

            this.NLayout = view.findViewById(R.id.notifyLayout);
            this.NCheck = view.findViewById(R.id.notifyCheck);
            this.NTitle = view.findViewById(R.id.notifyTitle);
            this.NDate = view.findViewById(R.id.notifyDate);
            this.NTime = view.findViewById(R.id.notifyTime);


            view.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    if (mListener != null) {
                        mListener.onItemClick(v, pos);
                    }
                }
            });
        }
    }

    // 4 - onBindViewHolder 정의 ( 뷰홀더에서 정의한 내용을 실제로 적용함 )
    @Override
    public void onBindViewHolder(@NonNull NotifyDeleteViewHolder viewHolder, int position) {

        // 알람 창 크기 조절
        ViewGroup.LayoutParams params = viewHolder.NLayout.getLayoutParams();
        int height = (MainActivity.height / 8);
        int width;
        width = MainActivity.width;
        params.width = width;
        params.height = height;
        viewHolder.NLayout.setLayoutParams(params);

        // 전체선택을 클릭했을 때 바인드에서 적용해주기 위해서 필요함.
        viewHolder.NCheck.setChecked(nList.get(position).isNCheck());
        //체크박스 보이게 설정
        viewHolder.NCheck.setVisibility(View.VISIBLE);

        // 체크박스 크기 조절
        ViewGroup.LayoutParams params2 = viewHolder.NCheck.getLayoutParams();
        int width2 = (MainActivity.width / 2) / 100 * 15;
        params2.width = width2;
        params2.height = width2;
        viewHolder.NCheck.setLayoutParams(params2);

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
    }

    // 5 - onBindViewHolder 정의 ( 삭제모드에서 한개의 뷰를 클릭했을 때 그 한개의 뷰만 바인드해주는 메서드 )
    @Override
    public void onBindViewHolder(@NonNull NotifyDeleteAdapter.NotifyDeleteViewHolder viewHolder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(viewHolder, position, payloads);
        } else {
            for (Object payload : payloads) {
                if (payload instanceof String) {
                    String type = (String) payload;
                    if (TextUtils.equals(type, "click")) {
                        viewHolder.NCheck.setChecked(nList.get(position).isNCheck());
                    }
                }
            }
        }
    }

    // 6 - getItemCount ( 뷰가 0개인지 아닌지 판별해준다. )
    @Override
    public int getItemCount() {
        return (null != nList ? nList.size() : 0);
    }
}
