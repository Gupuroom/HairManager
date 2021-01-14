package com.example.hm_project.view.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager2.widget.ViewPager2;

import com.example.hm_project.R;
import com.example.hm_project.data.DiaryData;
import com.example.hm_project.data.PreferenceManager;
import com.example.hm_project.data.APIManager;
import com.example.hm_project.databinding.ActivityDetaildiaryBinding;
import com.example.hm_project.view.adapter.DiaryPhotoAdapter;
import com.example.hm_project.etc.PopupActivity;
import com.example.hm_project.util.CodeManager;
import com.example.hm_project.util.HM_Singleton;
import com.example.hm_project.util.JsonBuild;
import com.example.hm_project.util.JsonParser;
import com.example.hm_project.util.NetworkManager;
import com.example.hm_project.util.NetworkTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/***
 * DetailDiary
 * 주 기능
 * @. 상세 일기 조회
    * 1- 캘린더에서 userNO와 diaryID를 가져온다
    * 2- 캘린더 액티비티에서 가져온 데이터로 서버에서 상세 일기 데이터를 가져온다
    * 3- 서버에서 가져온 데이터를 View에 뿌려준다.
 * @. 일기 삭제
    * 1- 일기 삭제 성공 or 실패 (핸들러에 따라 결정)
 * @. 일기 수정
    * 1- 일기 수정페이지로 이동.
 */
public class DetailDiaryActivity extends AppCompatActivity {

    JsonParser jsonParser = HM_Singleton.getInstance(new JsonParser());
    //다이어리 변수들
    private int userNO = Integer.parseInt(PreferenceManager.getString(LoginActivity.mContext,"userNO"));
    private int diaryID;
    public String diaryDate;
    public String diaryTitle;
    public String diaryContent;
    public boolean notifyCheck;
    public String notifyDate;
    public String notifyTime;
    private String photoOne;
    private String photoTwo;
    private String photoThree;
    private String photoFour;

    List<Uri> uriArray = new ArrayList<>(); //사진 URI 리스트

    public static Handler mainHandler;  //핸들러

    ActivityDetaildiaryBinding binding; //바인딩

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_detaildiary);
        binding.setDetail(this);
        //쓰레드 실행보다 항상 먼저 선언되어 있어야 함. 아니면 Message NPE 오류 남
        mainHandler = new Handler(){
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1) //setAsynchronous
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 1:
                        //상세 일기 정보 제대로 받아 왔을 경우
                        if(msg.obj.equals("MIF-Calendar-002")){
                            //서버에서 데이터를 받아오면
                            diaryWidgetSetting(); //데이터 셋팅
                            vipager2Adapter();  //사진 처리
                        }
                        //일기 삭제가 성공했을 경우
                        else if(msg.obj.equals("MIF-Calendar-005")){
                            Log.i("일기 삭제 완료","일기가 삭제 되었다.");
                            Message calnedarMsg = Message.obtain(CalendarActivity.mainHandler); //해당 핸들러 액티비티에 메세지 객체를 가져온다.
                            calnedarMsg.setTarget(CalendarActivity.mainHandler); //핸들러 타겟 설정
                            calnedarMsg.setAsynchronous(true); //비동기로 보내겠다.

                            calnedarMsg.what=4;
                            calnedarMsg.obj=diaryDate;
                            CalendarActivity.mainHandler.sendMessage(calnedarMsg);//삭제 성공 Toast
                            //액티비티 종료
                            if (android.os.Build.VERSION.SDK_INT >= 21) {
                                finishAndRemoveTask();
                            } else {
                                finish();
                            }

                        }
                        break;
                    case 2:
                        //서버에서 일기 기록을 제대로 못 불러 왔을 시
                        if(msg.obj.equals("MIF-Calendar-002")){
                            binding.detailRootLayout.setVisibility(View.INVISIBLE);
                            //msg.arg1 받지 않음
                            viewPopup(CodeManager.DetailNotRead);
                            //액티비티 종료
                            if (android.os.Build.VERSION.SDK_INT >= 21) {
                                finishAndRemoveTask();
                            } else {
                                finish();
                            }
                        }
                        //일기 삭제가 실패했을 경우
                        else if(msg.obj.equals("MIF-Calendar-005")){
                            binding.detailRootLayout.setVisibility(View.INVISIBLE);
                            viewPopup(CodeManager.DeleteFail);
                        }
                        break;
                }
            }
        };
        init();
    }

    private void init(){
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  //뒤로가기 버튼 만들기
        //다른 액티비티에서 넘겨준 데이터 가져오기
        Intent intent= getIntent();
        diaryID = intent.getIntExtra("diaryID",0);
        diaryDate = intent.getStringExtra("diaryDate");
        serverConnection(userNO,diaryID,diaryDate,2); //

    }
    //MIF-Calendar-002 데이터 가져오기 //Network Thread
    private void serverConnection(int _userNO, int _diaryID, String _diaryDate, int api){
        //모바일 기기에서 네트워크 통신이 되면 서버 연결을 시도한다.
        if(NetworkManager.networkCheck(DetailDiaryActivity.this)){
            Log.i("서버 연결","서버 연결 시도");
            NetworkTask networkTask;
            String urlDate = null;
            String json = null;
            switch (api){
                case 2:
                    //상세 일기 불러오기 GET
                    urlDate = APIManager.Calendar002_URL+_userNO+"&diaryID="+_diaryID+"&date="+_diaryDate;networkTask = new NetworkTask("MIF-Calendar-002", urlDate, mainHandler); //Network Thread 초기화
                    networkTask.executeOnExecutor(Executors.newSingleThreadExecutor()); //서버 연결
                    break;
                case 4:
                    //일기 수정 POST
                    urlDate = APIManager.Calendar004_URL;
                    json = JsonBuild.MIF_Calendar_005(_userNO, _diaryID, _diaryDate);
                    networkTask = new NetworkTask("MIF-Calendar-004", urlDate, json, mainHandler); //Network Thread 초기화
                    networkTask.executeOnExecutor(Executors.newSingleThreadExecutor()); //서버 연결
                    break;
                case 5:
                    //일기 삭제 POST
                    urlDate = APIManager.Calendar005_URL;
                    json = JsonBuild.MIF_Calendar_005(_userNO, _diaryID, _diaryDate);
                    networkTask = new NetworkTask("MIF-Calendar-005", urlDate, json, mainHandler); //Network Thread 초기화
                    networkTask.executeOnExecutor(Executors.newSingleThreadExecutor()); //서버 연결
                    break;
                default:
                    break;
            }

        }else {
            Log.i("네트워크 연결 오류", "네트워크 연결이 되어있지 않음");
            viewPopup(CodeManager.NewtWork_Error);
        }
    }

    //팝업창 메소드
    private void viewPopup(int code){
        Intent intent = new Intent(DetailDiaryActivity.this, PopupActivity.class);
        intent.putExtra("code", code);
        startActivityForResult(intent, 100);
    }

    //상세 일기 데이터 셋팅 !!
    private void diaryWidgetSetting(){
        DiaryData diaryData = jsonParser.getDiary();
        binding.setDetail(this);
        diaryTitle = diaryData.getDiaryTitle();
        diaryContent = diaryData.getDiaryContent();
        notifyCheck = diaryData.isNotifyCheck();

        photoOne = diaryData.getPhotoOne();
        photoTwo = diaryData.getPhotoTwo();
        photoThree = diaryData.getPhotoThree();
        photoFour = diaryData.getPhotoFour();

        //상세일기 이미지 URL 가져왔을 경우 예외처리
        photoUriCheck(photoOne,photoTwo,photoThree, photoFour);

        //알림 체크 된 경우에만 실행 되도록 Null 오류 예방을 위해
        if(notifyCheck){
            binding.detailDiaryNotifyLayout.setVisibility(View.VISIBLE);
            notifyDate = diaryData.getNotifyDate();
            notifyTime = diaryData.getNotifyTime();
            Log.i("알림 내용","확인"+notifyDate +" "+notifyTime);
        }else{
            binding.detailDiaryNotifyLayout.setVisibility(View.GONE);
        }
    }

    //상세일기 이미지 URL 가져왔을 경우 예외처리
    private void photoUriCheck(String uri1, String uri2, String uri3, String uri4) {
        if(uri1!=null){
            if(!uri1.trim().isEmpty()){
                if(!uri1.equals("null")){
                    uriArray.add(Uri.parse(photoOne));
                }
            }
        }
        if(uri2!=null){
            if(!uri2.trim().isEmpty()){
                if(!uri2.equals("null")){
                    uriArray.add(Uri.parse(photoTwo));
                }
            }
        }
        if(uri3!=null){
            if(!uri3.trim().isEmpty()){
                if(!uri3.equals("null")){
                    uriArray.add(Uri.parse(photoThree));
                }
            }
        }
        if(uri4!=null){
            if(!uri4.trim().isEmpty()){
                if(!uri4.equals("null")){
                    uriArray.add(Uri.parse(photoFour));
                }
            }
        }
    }

    //사진 고르고나서 보여지는 ViewPager2
    public void vipager2Adapter(){
        DiaryPhotoAdapter diaryPhotoAdapter;
        if(uriArray.size()==0){
            /*
            List<String> items = new ArrayList<>();
            items.add("등록된 사진이 없습니다.");
            diaryPhotoAdapter = new DiaryPhotoAdapter(this,items);
             */
            binding.detailPhotoLayout.setVisibility(View.GONE);
            binding.detailViewPager2.setVisibility(View.GONE);
            binding.deatialLine1.setVisibility(View.GONE);
        }else{
            diaryPhotoAdapter = new DiaryPhotoAdapter(this,uriArray,uriArray.size());

            ViewPager2 viewPager2= findViewById(R.id.detailViewPager2);
            //사진 전달 후 생성

            //viewPager 초기화
            viewPager2.setAdapter(diaryPhotoAdapter); //Viewpager2 셋팅
            viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL); //Horizontal 기준 셋팅
            viewPager2.setOffscreenPageLimit(1);  //페이지 4개 초기화  페이지 4개 초과시 계속 생성 삭제 됨
            viewPager2.setCurrentItem(0);//처음에 몇번째 아이템을 보여줄 것이냐

            final float pageMargin= getResources().getDimensionPixelOffset(R.dimen.pageMargin);
            final float pageOffset= getResources().getDimensionPixelOffset(R.dimen.offset);
            //뷰 페이저 아이템 위치 셋팅
            viewPager2.setPageTransformer(new ViewPager2.PageTransformer() {
                @Override
                public void transformPage(@NonNull View page, float position) {
                    float myOffset = position * -(2 * pageOffset +pageMargin);
                    if(position<-1){
                        page.setTranslationX(-myOffset);
                    }else if (position <=1){
                        float sclaFactor = Math.max(0.7f,1-Math.abs(position-0.14285715f));
                        page.setTranslationX(myOffset);
                        page.setScaleY(sclaFactor);
                        page.setAlpha(sclaFactor);
                    }else {
                        page.setAlpha(0f);
                        page.setTranslationX(myOffset);
                    }
                }
            });
        }
    }

    //액션바 메뉴 생성
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detailpage, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.diaryEdit:
                Log.i("일기 수정 페이지","일기 수정 페이지 이동.");
                DiaryData diaryData = jsonParser.getDiary();
                diaryData.setDiaryID(diaryID);
                Intent intent = new Intent(getApplication(),WriteDiaryActivity.class);
                intent.putExtra("diaryData",diaryData);
                startActivity(intent);
                //액티비티 종료
                if (android.os.Build.VERSION.SDK_INT >= 21) {
                    finishAndRemoveTask();
                } else {
                    finish();
                }

                break;
            case R.id.diaryDelete:
                Log.i("일기 삭제","현재 일기 삭제 됨.");
                //BadToken Exception 예외 처리
                if(!DetailDiaryActivity.this.isFinishing()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(DetailDiaryActivity.this);
                    builder.setTitle("일기 삭제")
                            .setMessage("일기를 삭제하시겠습니까 ?")
                            .setCancelable(false)
                            .setPositiveButton("삭제",(dialog, which) -> {
                                serverConnection(userNO,diaryID,diaryDate,5); //일기 삭제
                            }).setNegativeButton("취소",(dialog, which) -> {
                        return;
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }else{
                    Toast.makeText(DetailDiaryActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                }
                break;
            case android.R.id.home:
                //액티비티 종료
                if (android.os.Build.VERSION.SDK_INT >= 21) {
                    finishAndRemoveTask();
                } else {
                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
