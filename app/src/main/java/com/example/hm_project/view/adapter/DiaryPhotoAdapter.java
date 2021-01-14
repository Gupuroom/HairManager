package com.example.hm_project.view.adapter;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.example.hm_project.R;

import java.util.List;
import java.util.UUID;

/***
 * 뷰페이저2 어뎁터
 * 1. 기본 4장 아이템을 보여줌  --Original--
 * 2. 사진 선택한 갯수 만큼 1~4장을 보여줌 --현재 시스템 적용--
 * 순서
 * 1. onCreateViewHolder로 페이지를 생성한다.
 * 2. MyViewHolder로 페이지에 뷰를 집어 넣고
 * 3. onBindViewHolder에서 뷰에 알맞는 데이터를 넣는다.
 */
public class DiaryPhotoAdapter extends RecyclerView.Adapter<DiaryPhotoAdapter.MyViewHolder> {
    private Context context;
    private List<String> list;
    private List<Uri> uri;
    private int count=0;
    private int num=0;
    //Test 사진 뷰페이저 아이템 초기화
    public DiaryPhotoAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }
    //사진 URI Array 와 사진 갯수 초기화
    public DiaryPhotoAdapter(Context context, List<Uri> uri, int num){
        this.context=context;
        this.uri=uri;
        this.num=num;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_item,parent,false);
        count++;
        Log.i("페이지 생성"," 페이지가 생성되었습니다."+count);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Log.i("바인드 뷰","바인드 뷰");
        //uri NPE 오류 제거 및 예외처리
        //Original -- 블랙,그레이 순으로 아이템이 계속 생성됨
        if(uri==null){
            if(list!=null){
                int index = position % list.size();
                String item = list.get(index);
                holder.tvName.setText(String.format("%s", item));
                if (position % 2 == 0) {
                    // holder.imgBanner.setBackgroundColor(Color.BLACK);

                } else {
                   // holder.imgBanner.setBackgroundColor(Color.GRAY);
                }
            }
        }
        //사진 선택하여 골랐을 때.
        else{
            if(uri.size()==0){
               //Log.d("뷰페이저2 오류","바인드 뷰 uri.size==0 오류");
               return;
            }else{
                int index = position % uri.size();
                switch (index){
                    case 0: // Bitmap초과로 데이터가 클 경우 오류가 날 수 있음 해결방안  GlideOption에 DownsampleStrategy를 적용 아직 미적용
                        // holder.imgBanner.setImageURI(uri.get(0));
                        if(uri.get(0)==null||uri.get(0).equals("null")){
                            break;
                        }
                        Glide.with(context)
                                .load(uri.get(0)) //Load
                                .signature(new ObjectKey(UUID.randomUUID().toString())) // Glide Cache clear
                                .placeholder(R.drawable.loading) // Glide 이미지 로딩 전 보여줄 이미지
                                .error(R.drawable.error_uri) // 리소스 불러오다가 에러가 났을때 이미지
                                .fallback(R.drawable.null_load) // 로드 할 경우 URI가 Null 인경우
                                .into(holder.imgBanner); //이미지를 보여줄 이미지뷰 대상
                        break;
                    case 1:
                        //holder.imgBanner.setImageURI(uri.get(1));
                        if(uri.get(1)==null||uri.get(1).equals("null")){
                            break;
                        }
                        Glide.with(context)
                                .load(uri.get(1))
                                .signature(new ObjectKey(UUID.randomUUID().toString())) // Glide Cache clear
                                .placeholder(R.drawable.loading) // Glide 이미지 로딩 전 보여줄 이미지
                                .error(R.drawable.error_uri) // 리소스 불러오다가 에러가 났을때 이미지
                                .fallback(R.drawable.null_load) // 로드 할 경우 URI가 Null 인경우
                                .into(holder.imgBanner); //이미지를 보여줄 이미지뷰 대상
                        break;
                    case 2:
                        if(uri.get(2)==null||uri.get(2).equals("null")){
                            break;
                        }
                        //holder.imgBanner.setImageURI(uri.get(2));
                        Glide.with(context)
                                .load(uri.get(2))
                                .signature(new ObjectKey(UUID.randomUUID().toString())) // Glide Cache clear
                                .placeholder(R.drawable.loading) // Glide 이미지 로딩 전 보여줄 이미지
                                .error(R.drawable.error_uri) // 리소스 불러오다가 에러가 났을때 이미지
                                .fallback(R.drawable.null_load) // 로드 할 경우 URI가 Null 인경우
                                .into(holder.imgBanner); //이미지를 보여줄 이미지뷰 대상
                        break;
                    case 3:
                        //holder.imgBanner.setImageURI(uri.get(3));
                        if(uri.get(3)==null||uri.get(3).equals("null")){
                            break;
                        }
                        Glide.with(context)
                                .load(uri.get(3))
                                .signature(new ObjectKey(UUID.randomUUID().toString())) // Glide Cache clear
                                .placeholder(R.drawable.loading) // Glide 이미지 로딩 전 보여줄 이미지
                                .error(R.drawable.error_uri) // 리소스 불러오다가 에러가 났을때 이미지
                                .fallback(R.drawable.null_load) // 로드 할 경우 URI가 Null 인경우
                                .into(holder.imgBanner); //이미지를 보여줄 이미지뷰 대상
                        break;
                }
            }
        }
    }
    //페이지 갯수 정하는 곳  Integer.MAX 하면 무한 ViewPager 가능
    @Override
    public int getItemCount() {
        if(num==0){
            return 1; //글쓰기 작성시 첫 아이템 1개
        }else
            return  num;
    }
    //뷰홀더
    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView imgBanner;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.i("뷰홀더","뷰홀더 사이클 뷰");
            tvName = itemView.findViewById(R.id.tvName);
            imgBanner = itemView.findViewById(R.id.imgBanner);
        }
    }
}
