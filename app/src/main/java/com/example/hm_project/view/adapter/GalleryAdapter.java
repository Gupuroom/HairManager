package com.example.hm_project.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hm_project.Command.EditTextInput;
import com.example.hm_project.R;
import com.example.hm_project.data.GalleryData;
import com.example.hm_project.data.ImageLoadTask;
import com.example.hm_project.view.activity.GalleryActivity;
import com.example.hm_project.view.activity.MainActivity;

import java.util.ArrayList;

/***
 *  갤러리 어댑터
 *  1 - 생성자
 *  2 - onCreateViewHolder ( 뷰홀더 객체화 )
 *  3 - GalleryViewHolder 정의 ( 실제로 뷰에 어떤 객체가 생성될지 정의 )
 *  4 - onBindViewHolder 정의 ( 뷰홀더에서 정의한 내용을 실제로 적용함 )
 *  5 - getItemCount ( 뷰가 0개인지 아닌지 판별해준다. )
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    // 클릭 리스너
    public interface OnItemClickListener {
        void onItemClick(View v, int pos);
    }

    // 롱클릭 리스너
    public interface OnItemLongClickListener {
        void onItemLongClick(View v, int pos);
    }

    // 서버에서 온 데이터 담을 리스트
    private ArrayList<GalleryData> gList;

    // 리스너 객체 참조를 저장하는 변수
    private GalleryAdapter.OnItemClickListener mListener = null;
    private GalleryAdapter.OnItemLongClickListener mLongListener = null;

    public void setOnItemClickListener(GalleryAdapter.OnItemClickListener listener) {
        this.mListener = listener;
    }

    public void setOnItemLongClickListener(GalleryAdapter.OnItemLongClickListener listener) {
        this.mLongListener = listener;
    }

    // 1 - 생성자
    public GalleryAdapter(ArrayList<GalleryData> list) {
        this.gList = list;
    }

    // 2 - onCreateViewHolder
    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.gallery_item, viewGroup, false);

        GalleryViewHolder viewHolder = new GalleryViewHolder(view);

        return viewHolder;
    }

    // 3 - GalleryViewHolder 정의
    public class GalleryViewHolder extends RecyclerView.ViewHolder {

        public FrameLayout GLayout;
        public CheckBox GCheck;
        public ImageView GPhoto;
        public TextView GTitle;

        public GalleryViewHolder(View view) {
            super(view);

            this.GLayout = view.findViewById(R.id.GLayout);
            this.GCheck = view.findViewById(R.id.GCheck);
            this.GPhoto = view.findViewById(R.id.GPhoto);
            this.GTitle = view.findViewById(R.id.GTitle);

            // 뷰 클릭시 이벤트
            view.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    if (mListener != null) {
                        mListener.onItemClick(v, pos);
                    }
                }
            });

            // 뷰 롱클릭시 이벤트
            view.setOnLongClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    mLongListener.onItemLongClick(v, pos);
                }
                return true;
            });
        }
    }

    // 4 - onBindViewHolder 정의
    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder viewHolder, int position) {

        // 이미지 로드
        if (!EditTextInput.checkNPE(gList.get(position).getGPhoto())) {
            ImageLoadTask task = new ImageLoadTask(gList.get(position).getGPhoto(), viewHolder.GPhoto);
            task.execute();
        }

        // 갤러리 제목 로드
        if (!EditTextInput.checkNPE(gList.get(position).getGTitle())) {
            viewHolder.GTitle.setText(gList.get(position).getGTitle());
        }

        // 체크박스 안 보이게 설정
        viewHolder.GCheck.setVisibility(View.INVISIBLE);

        // 이미지뷰 테두리 둥글게
        viewHolder.GPhoto.setClipToOutline(true);
        // 이미지뷰 크기 조절
        ViewGroup.LayoutParams params = viewHolder.GLayout.getLayoutParams();
        int width = (MainActivity.width / 2);
        params.width = width;
        params.height = width;
        viewHolder.GLayout.setLayoutParams(params);
    }

    // 5 - getItemCount ( 뷰가 0개인지 아닌지 판별해준다. )
    @Override
    public int getItemCount() {
        return (null != gList ? gList.size() : 0);
    }
}
