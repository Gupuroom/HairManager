package com.example.hm_project.view.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;

import android.database.Cursor;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;

import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.annotation.RequiresApi;

import androidx.appcompat.app.AppCompatActivity;


import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.hm_project.Command.EditTextInput;
import com.example.hm_project.R;

import com.example.hm_project.data.DiaryData;
import com.example.hm_project.data.PreferenceManager;
import com.example.hm_project.databinding.ActivityWritediaryBinding;

import com.example.hm_project.etc.ChangeDate;

import com.example.hm_project.util.NetworkManager;
import com.example.hm_project.view.adapter.DiaryPhotoAdapter;

import com.example.hm_project.etc.PopupActivity;
import com.example.hm_project.etc.WriteDiaryViewModel;
import com.example.hm_project.util.CodeManager;
import com.example.hm_project.util.FileUploadUtils;

import com.example.hm_project.util.JsonBuild;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.ArrayList;
import java.util.Calendar;

import java.util.List;


/***
 * 일기 작성 페이지
 * 1- 액션바 설정
 * 2- CalendarActivity에서 보낸 Data 셋팅
 * 3- 제목, 내용 작성
 * 4- 사진첩으로 이동해서 사진 가져오기
 * 5- 알림 설정
 * 6- 날짜와 시간 보여주기
 * 7- 작성 완료
 * 8- API정의서와 맞게 JSON으로 보냄
 * 기능
 * 1- 사진 여러장 가져오기 (구글포토에서만 가능, 겔러리는 여러장 가져오는것을 지원하지 않음)
 * 2- 뷰페이저2 가져온 사진 갯수에 만큼 보여줌 (연속적으로 사진 생성 가능 설정 필요)
 * 3- 제목 및 내용 글자 제한 및 글자 체크
 * 4- 사진 byte변환
 * 5-
 *
 */
public class WriteDiaryActivity extends AppCompatActivity {
  //  String code = null; //일기 작성 결과 CODE

    final int userNO = Integer.parseInt(PreferenceManager.getString(LoginActivity.mContext,"userNO"));

    //사진 변수
    static final int pick_from_Multi_album=100;
    List<Uri> uriArray = new ArrayList<>();
    List<Uri> updateUriArray = new ArrayList<>();

    //사진 주소들
    List<String> absolutePath = null;

    //다이어리 DTO
    DiaryData diaryData;
    boolean update = false; //일기 작성 OR 수정;
    boolean updateImage = false; //일기 수정 사진 변경 False or True;

    //DTO 변수
    int diaryID = 0;
    int notifyID=0;
    private String diaryTitle, diaryContent, diaryDate, notifyDate, notifyTime;
    boolean notifyCheck;
    private String photoOne ="null", photoTwo="null", photoThree="null", photoFour="null";

    //서버 전송 X textView String
    public String notifyDateTime;

    //데이트 타임 피커 변수
    int Year,Month,Day,Hour,Minutes;

    Toast toast;
    //핸들러
    public  static Handler mainHandler;
    private MenuItem postItem; // 메뉴 버튼

    //OKHTTP 전송 클래스
    FileUploadUtils fileUploadUtils;
    //바인딩
    ActivityWritediaryBinding binding;
    WriteDiaryViewModel writeDiaryViewModel;

    @SuppressLint("HandlerLeak")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        writeDiaryViewModel = new ViewModelProvider(this).get(WriteDiaryViewModel.class); //뷰모델 생성
        binding = DataBindingUtil.setContentView(WriteDiaryActivity.this,R.layout.activity_writediary);
        binding.setWrite(this); //바인딩
        binding.setViewModel(writeDiaryViewModel); // 뷰모델 바인딩
        binding.setLifecycleOwner(this); //꼭 해줘야 함

        //매인 핸들러 -OKHTTP3 로 인해 예외처리가 좀 다름
        mainHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    //FileUploadUtils 에서 받아옴
                    case 1:
                        Message calnedarMsg = Message.obtain(CalendarActivity.mainHandler); //해당 핸들러 액티비티에 메세지 객체를 가져온다.
                        calnedarMsg.setTarget(CalendarActivity.mainHandler); //핸들러 타겟 설정
                        calnedarMsg.setAsynchronous(true); //비동기로 보내겠다.

                        calnedarMsg.what=3;
                        calnedarMsg.obj=diaryDate;
                        CalendarActivity.mainHandler.sendMessage(calnedarMsg);
                        //액티비티 종료
                        if (android.os.Build.VERSION.SDK_INT >= 21) {
                            finishAndRemoveTask();
                        } else {
                            finish();
                        }
                        break;
                    case 2:
                        //정상적인 오류가 아닐 경우
                        viewPopup(CodeManager.ConnectionException);
                        postItem.setEnabled(true);
                        break;
                    case 3:
                        //CodeManager에서 받아온 코드로 에러 메시지 띄움
                        viewPopup(msg.arg1);
                        postItem.setEnabled(true);
                        break;
                    case 4:

                        break;
                }
            }
        };

        //핸들러 넘기기
        fileUploadUtils = new FileUploadUtils(mainHandler);

        /*
        //일기 작성 날짜 라이브 데이터
        writeDiaryViewModel.getLiveDiaryDate().observe(this, newDiaryDate-> {
           // diaryDate = newDiaryDate;
        });
        */
        //일기 제목 라이브 데이터
        writeDiaryViewModel.getLiveDiaryTitle().observe(this, newEditTilte-> {
            diaryTitle = newEditTilte;

            if(EditTextInput.checkTitle(diaryTitle)){
                binding.writeTitleCheckTV.setVisibility(View.VISIBLE);
            }else{
                binding.writeTitleCheckTV.setVisibility(View.INVISIBLE);
            }
        });

        //일기 내용 라이브 데이터
        writeDiaryViewModel.getLiveDiaryContent().observe(this, newEditContent-> {
            diaryContent = newEditContent;
            if(EditTextInput.checkContent(diaryContent)){
                binding.writeContentCheckTV.setVisibility(View.VISIBLE);
            }else{
                binding.writeContentCheckTV.setVisibility(View.INVISIBLE);
            }
        });

        //알림 Date Time 라이브 데이터
        writeDiaryViewModel.getliveNotifyDateTime().observe(this, newNotifyDateTime-> {
            notifyDateTime = newNotifyDateTime;
        });

        //알림 Date Time 라이브 데이터
        writeDiaryViewModel.getliveNotifyCheck().observe(this, newNotifyCheck-> {
            notifyCheck = newNotifyCheck;
            //알림 체크박스 체크 검사
            if(notifyCheck){
                binding.notifyDateTimeTV.setVisibility(View.VISIBLE); // 알림 날짜 시간 TextView 보이도록
                notifyDate = diaryDate;
                //시간 분 받고
                Hour = binding.writeNotifyTimePicker.getHour();
                Minutes = binding.writeNotifyTimePicker.getMinute();
                // 포맷 변경
                notifyTime = ChangeDate.time(Hour,Minutes);
                notifyDateTime = ChangeDate.DateTime(notifyDate, notifyTime);
                //셋팅
                binding.writeNotifyDatePicker.updateDate(ChangeDate.Year(notifyDate), ChangeDate.Month(notifyDate), ChangeDate.Day(notifyDate));
                writeDiaryViewModel.getliveNotifyDateTime().setValue(notifyDateTime);

            }else {
                binding.writeDiaryNotifyLayout.setVisibility(View.GONE); // 알림 날짜 시간 숨기기
                binding.notifyDateTimeTV.setVisibility(View.GONE); //알림 피커있는 레이아웃 숨기기
                binding.notiyDateTabButton.setBackground(ContextCompat.getDrawable(WriteDiaryActivity.this, R.drawable.tabdownbutton));

                notifyDate = null;
                notifyTime = null;
                notifyDateTime = null;
            }
        });

        init(); //액션바 설정 및 UI설정
        DateTimeListener(); //날짜와 시간 리스너 (다이어리 , 알림)
        expendAni(); //날짜 작성 UI 확장
    }

    //WriteAcitivity 기본 init
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void init() {
        //액션바 설정
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  //뒤로가기 버튼 만들기

        //다이어리 작성 날짜 최소 최대 정하기
        Calendar min = Calendar.getInstance();
        Calendar max = Calendar.getInstance();
        min.set(2000, 0, 1); //최소 2000년 부터 캘린더 조회 일기 작성 가능
        binding.writediaryDatePicker.setMinDate(min.getTimeInMillis());
        max.set(CalendarDay.today().getYear() + 20, 11, 31); //최대 올해부터+20년까지 캘린더 조회 일기 작성 가능
        binding.writediaryDatePicker.setMaxDate(max.getTimeInMillis());

        //알림 날짜 최소 최대 정하기
        Calendar min2 = Calendar.getInstance();
        Calendar max2 = Calendar.getInstance();
        min.set(2000, 0, 1); //최소 2000년 부터 캘린더 조회 일기 작성 가능
        binding.writeNotifyDatePicker.setMinDate(min.getTimeInMillis());
        max.set(CalendarDay.today().getYear() + 20, 11, 31); //최대 올해부터+20년까지 캘린더 조회 일기 작성 가능
        binding.writeNotifyDatePicker.setMaxDate(max.getTimeInMillis());

        //알림 시간 설정
        binding.writeNotifyTimePicker.setIs24HourView(true); //24시간 모드

        Intent intent = getIntent(); //DetailActivity에서 보낸 Data 받기.
        diaryData = (DiaryData) intent.getSerializableExtra("diaryData"); //
        //일기 처음부터 작성할 경우
        if(diaryData==null){
            getSupportActionBar().setTitle("일기 작성"); //액션바 타이틀 설정
            wirteInit();
            viewpager2Adapter(); //기본 사진 표시
        }
        //일기 수정으로 작성할 경우
        else{
            getSupportActionBar().setTitle("일기 수정"); //액션바 타이틀 설정
            diaryUpdateInit();
            if(updateUriArray.size()==0||updateUriArray==null){
                viewpager2Adapter(); //기본 사진 표시
            }else {
                viewpager2Adapter2(); //이미 선택된 사진 표시
            }

        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void wirteInit(){
        update = false; //일기 작성으로 한다.
        Intent intent = getIntent(); //CalendarActivity에서 보낸 Data 받기.
        diaryDate = intent.getStringExtra("diaryDate");  //날짜를 받는다.

        //데이트피커 선택된 날짜로 셋팅하기
        binding.writeNotifyDatePicker.updateDate(ChangeDate.Year(diaryDate), ChangeDate.Month(diaryDate), ChangeDate.Day(diaryDate));
        binding.writediaryDatePicker.updateDate(ChangeDate.Year(diaryDate), ChangeDate.Month(diaryDate), ChangeDate.Day(diaryDate));
        //알림 날짜 년 월 일 받기
        Year = binding.writeNotifyDatePicker.getYear();
        Month = binding.writeNotifyDatePicker.getMonth();
        Day = binding.writeNotifyDatePicker.getDayOfMonth();
        //알림 시간  시 분 받기
        Hour = binding.writeNotifyTimePicker.getHour();
        Minutes = binding.writeNotifyTimePicker.getMinute();

        //초기 설정
        writeDiaryViewModel.getliveNotifyCheck().setValue(false);
        notifyDate = diaryDate;
        notifyTime = ChangeDate.time(Hour,Minutes);

        //라이브 데이터 변하는 날짜들 보여주기
        display(Year, Month + 1, Day, 0, 0, 1); //다이어리 날짜와  보여주기
        display(Year, Month + 1, Day, Hour, Minutes, 2);  //알림 날짜와 시간 보여주기
    }
    //일기 수정 Init
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void diaryUpdateInit(){
        update = true; //일기 수정으로 한다.
        //일기 수정 데이터 받기
        diaryID = diaryData.getDiaryID(); //상세 일기에서 넣어줌
        diaryDate = diaryData.getDiaryDate();
        diaryTitle = diaryData.getDiaryTitle();
        diaryContent = diaryData.getDiaryContent();
        notifyCheck = diaryData.isNotifyCheck();

        //일기 작성 위젯 셋팅
        writeDiaryViewModel.getliveNotifyCheck().setValue(notifyCheck);
        if(notifyCheck){
            notifyID = diaryData.getNotifyID();
            notifyDate = diaryData.getNotifyDate();
            notifyTime = diaryData.getNotifyTime();
            notifyDateTime = ChangeDate.DateTime(notifyDate, notifyTime);
            writeDiaryViewModel.getliveNotifyDateTime().setValue(notifyDateTime);
            //notify 위젯 셋팅
            binding.writeNotifyTimePicker.setHour(ChangeDate.Hour(notifyTime));
            binding.writeNotifyTimePicker.setMinute(ChangeDate.Minute(notifyTime));
            binding.writeNotifyDatePicker.updateDate(ChangeDate.Year(notifyDate), ChangeDate.Month(notifyDate), ChangeDate.Day(notifyDate));
        }

        writeDiaryViewModel.getLiveDiaryTitle().setValue(diaryTitle);  //제목 설정
        writeDiaryViewModel.getLiveDiaryContent().setValue(diaryContent);   //내용 설정

        String date = ChangeDate.strYear(diaryDate)+"년 "+ChangeDate.strMonth(diaryDate)+"월 "+ChangeDate.strDay(diaryDate)+"일"; //작성 날짜 String
        writeDiaryViewModel.getLiveDiaryDate().setValue(date); //작성 날짜 View 설정
        binding.writediaryDatePicker.updateDate(ChangeDate.Year(diaryDate), ChangeDate.Month(diaryDate), ChangeDate.Day(diaryDate)); //작성 날짜 위젯 설정

        updateUriArray.clear();
        photoUriCheck(diaryData.getPhotoOne(),diaryData.getPhotoTwo(),diaryData.getPhotoThree(),diaryData.getPhotoFour());

    }
    //상세일기 이미지 URI 가져왔을 경우 예외처리
    private void photoUriCheck(String uri1, String uri2, String uri3, String uri4) {
        if(uri1!=null){
            if(!uri1.trim().isEmpty()){
                if(!uri1.equals("null")){
                    updateUriArray.add(Uri.parse(uri1));
                    Log.i(" 사진1", "하하"+uri1);
                }
            }
        }
        if(uri2!=null){
            if(!uri2.trim().isEmpty()){
                if(!uri2.equals("null")){
                    updateUriArray.add(Uri.parse(uri2));
                }
            }
        }
        if(uri3!=null){
            if(!uri3.trim().isEmpty()){
                if(!uri3.equals("null")){
                    updateUriArray.add(Uri.parse(uri3));
                }
            }
        }
        if(uri4!=null){
            if(!uri4.trim().isEmpty()){
                if(!uri4.equals("null")){
                    updateUriArray.add(Uri.parse(uri4));
                }
            }
        }
    }
    //피커 UI 확장
    private void expendAni(){
        //작성 날짜 피커 숨기기 보이기
        binding.wirteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.writediaryDatePicker.getVisibility()==View.GONE) {
                    binding.writediaryDatePicker.setVisibility(View.VISIBLE); // 피커들 보이기
                    binding.writeDateTabButton.setBackground(ContextCompat.getDrawable(WriteDiaryActivity.this, R.drawable.tabupbutton));
                }
                else {
                    binding.writediaryDatePicker.setVisibility(View.GONE); // 피커들 숨기기
                    binding.writeDateTabButton.setBackground(ContextCompat.getDrawable(WriteDiaryActivity.this, R.drawable.tabdownbutton));
                }
            }
        });
        //알림 피커 숨기기 보이기
        binding.notifyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    if(binding.writeDiaryNotifyLayout.getVisibility()==View.GONE){
                        binding.writeDiaryNotifyLayout.setVisibility(View.VISIBLE); // 피커들 보이기
                        binding.notiyDateTabButton.setBackground(ContextCompat.getDrawable(WriteDiaryActivity.this, R.drawable.tabupbutton));

                    }else{
                        binding.writeDiaryNotifyLayout.setVisibility(View.GONE); // 피커들 숨기기
                        binding.notiyDateTabButton.setBackground(ContextCompat.getDrawable(WriteDiaryActivity.this, R.drawable.tabdownbutton));
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O) //26API
    private void DateTimeListener(){
        //다이어리 날짜 변경 리스너
        binding.writediaryDatePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int _year, int _monthOfYear, int _dayOfMonth) {
                //다이어리 작성 날짜 받아서 DB에 받는 변수 추가하기
                // -> 추가하는 곳
                //작성 날짜 보여주기
                display(_year,_monthOfYear+1,_dayOfMonth,0,0,1);
            }
        });

        //알림 날짜 변경 리스너
        binding.writeNotifyDatePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker _view, int _year, int _monthOfYear, int _dayOfMonth) {
                Year = _year;
                Month = _monthOfYear+1;
                Day = _dayOfMonth;
                notifyDate = ChangeDate.set(Year,Month,Day);
                //알림 날짜 보여주기
                display(Year,Month,Day,Hour,Minutes,2);
            }
        });
        //알림 시간 변경 리스너
        binding.writeNotifyTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker _view, int _hourOfDay, int _minute) {
                Hour = _hourOfDay;
                Minutes = _minute;
                notifyTime = ChangeDate.time(Hour,Minutes);
                //알림 시간 보여주기
                display(Year,Month,Day,Hour,Minutes,2);
            }
        });
    }

    //알림 날짜와 시간을 텍스트 뷰로 보여준다.
    private void display(int _year, int _month, int _day, int _hour, int _minute, int num) {
        switch (num){
            // 다이어리 작성 날짜
            case 1:
                String date = _year+"년 "+_month+"월 "+_day+"일";
                diaryDate = ChangeDate.set(_year,_month,_day);
                writeDiaryViewModel.getLiveDiaryDate().setValue(date);
                break;
            // 알림 날짜 시간
            case 2:
                String dateTime = _year+"년 "+_month+"월 "+_day+"일   "+_hour+"시 "+_minute+"분";
                writeDiaryViewModel.getliveNotifyDateTime().setValue(dateTime);
                break;
        }
    }

    //사진첩으로 이동한다.
    public void goPhotoAlbum(View v){
        PermissionListener permissionlistener = new PermissionListener() {
            //권한 설정 허용 하면 앨범 이동
            @Override
            public void onPermissionGranted() {
                Intent intent = new Intent(Intent.ACTION_PICK);
                //intent.setType("image/*");
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                // intent.addCategory(Intent.CATEGORY_OPENABLE); //생략 가능 삼성, LG , 일반폰
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true); // 삼성폰 생략 갸능/ 다른 폰 필수
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"), pick_from_Multi_album);
            }
            //권한 설정 거부하면 토스트 메세지
            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                if(toast == null) {
                    toast = Toast.makeText(WriteDiaryActivity.this,"권한을 설정하셔야 합니다.",Toast.LENGTH_SHORT);
                } else {
                    toast.setText("권한을 설정하셔야 합니다.");
                }
                toast.show();
            }
        };
        //READ_EXTERNAL_STORAGE , WRITE_EXTERNAL_STORAGE 권한 허용 체크
        new TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();
    }
    //처음 사진 안골랐을 때 보여지는 ViewPager2
    public void viewpager2Adapter(){
        List<String> items = new ArrayList<>();
        items.add("(0/4)");

        //아이템 전달 후 생성
        DiaryPhotoAdapter diaryPhotoAdapter = new DiaryPhotoAdapter(this,items);

        //viewPager 초기화
        ViewPager2 viewPager2= findViewById(R.id.writeViewPager2);
        viewPager2.setAdapter(diaryPhotoAdapter);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager2.setOffscreenPageLimit(1);  //페이지 4개 초기화  페이지 4개 초과시 계속 생성 삭제 됨
        viewPager2.setCurrentItem(1);//처음에 몇번째 아이템을 보여줄 것이냐

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

    //사진 고르고나서 보여지는 ViewPager2
    public void viewpager2Adapter2(){
        ViewPager2 viewPager2= findViewById(R.id.writeViewPager2);
        DiaryPhotoAdapter diaryPhotoAdapter;
        //사진 전달 후 생성
        if(update){
            diaryPhotoAdapter = new DiaryPhotoAdapter(this,updateUriArray,updateUriArray.size());
        }else{
            diaryPhotoAdapter = new DiaryPhotoAdapter(this,uriArray,uriArray.size());
        }

        //viewPager 초기화
        viewPager2.setAdapter(diaryPhotoAdapter); //Viewpager2 셋팅
        viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL); //Horizontal 기준 셋팅
        viewPager2.setOffscreenPageLimit(1);  //페이지 4개 초기화  페이지 4개 초과시 계속 생성 삭제 됨
        viewPager2.setCurrentItem(1);//처음에 몇번째 아이템을 보여줄 것이냐

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == pick_from_Multi_album) {
            if (resultCode == RESULT_OK) {
                //ClipData 또는 Uri를 가져온다
                Uri uri = data.getData();
                ClipData clipData = data.getClipData();
                //이미지 URI 를 이용하여 이미지뷰에 순서대로 세팅한다.
                if (clipData != null) {
                    if(clipData.getItemCount()>4){
                        Toast.makeText(this,"사진을 4개 이상 선택할 수 없습니다.",Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(update){
                        updateUriArray.clear();
                        updateImage=true;
                    }
                    uriArray.clear();
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        if (i < clipData.getItemCount()) {
                            Log.i("언제 뜨는가 Test3","###Test 3");
                            Uri urione = clipData.getItemAt(i).getUri();
                            switch (i) {
                                case 0:
                                    if(update){
                                        updateUriArray.add(urione);
                                        photoOne = urione.toString();
                                    }else{
                                        uriArray.add(urione);
                                    }
                                    Log.i("앨범 선택 1","###Select Photo in Album1");
                                    break;
                                case 1:
                                    if(update){
                                        updateUriArray.add(urione);
                                        photoTwo = urione.toString();
                                    }else{
                                        uriArray.add(urione);
                                    }
                                    Log.i("앨범 선택 2","###Select Photo in Album 2");
                                    break;
                                case 2:
                                    if(update){
                                        updateUriArray.add(urione);
                                        photoThree = urione.toString();
                                    }else{
                                        uriArray.add(urione);
                                    }
                                    Log.i("앨범 선택 3","###Select Photo in Album 3");
                                    break;
                                case 3:
                                    if(update){
                                        updateUriArray.add(urione);
                                        photoFour = urione.toString();
                                    }else{
                                        uriArray.add(urione);
                                    }
                                    Log.i("앨범 선택 4","###Select Photo in Album 4");
                                    break;
                            }
                        }
                    }
                    viewpager2Adapter2();
                } else if (uri != null) {
                    Log.i("언제 뜨는가 Test4","###Test 4");
                }
            }
        }
    }

    //팝업창 메소드
    private void viewPopup(int CODE) {
        Intent intent = new Intent(this, PopupActivity.class);
        intent.putExtra("code", CODE);
        startActivityForResult(intent, 100);
    }

    //액션바 메뉴 생성
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_writepage, menu);
        return true;
    }
    //메뉴 아이템 클릭
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            //글쓰기 완료 시
            case R.id.writePost:
                postItem= item;
                postItem.setEnabled(false); //연속 버튼 눌렀을 경우 실행 방지

                //NetWork 연결 오류 검사
                if (!NetworkManager.networkCheck(this)) {
                    viewPopup(CodeManager.NewtWork_Error);
                    postItem.setEnabled(true); //버튼 활성화
                    return super.onOptionsItemSelected(item);
                }
                if (uriArray != null || updateUriArray != null) {
                    //DB에 보낼 String 검사하기
                    if (EditTextInput.checkNPE(diaryTitle)) {
                        viewPopup(CodeManager.TitleNull);
                        postItem.setEnabled(true); //버튼 활성화
                        return super.onOptionsItemSelected(item);
                    }
                    // 제목 크기 검사
                    else if (EditTextInput.checkTitle(diaryTitle)) {
                        viewPopup(CodeManager.TitleOver);
                        postItem.setEnabled(true); //버튼 활성화
                        return super.onOptionsItemSelected(item);
                    }
                    //DB에 보낼 String 검사하기
                    if (EditTextInput.checkNPE(diaryContent)) {
                        viewPopup(CodeManager.ContentNull);
                        postItem.setEnabled(true); //버튼 활성화
                        return super.onOptionsItemSelected(item);
                    }
                    //내용 길이 검사
                    else if (EditTextInput.checkContent(diaryContent)) {
                        viewPopup(CodeManager.ContentOver);
                        postItem.setEnabled(true); //버튼 활성화
                        return super.onOptionsItemSelected(item);
                    }

                    diaryData = new DiaryData();
                    diaryData.setUserNO(userNO);
                    diaryData.setDiaryDate(diaryDate);
                    diaryData.setDiaryTitle(diaryTitle);
                    diaryData.setDiaryContent(diaryContent);
                    diaryData.setNotifyCheck(notifyCheck);
                    diaryData.setNotifySwitch(notifyCheck); //같은 값
                    diaryData.setNotifyDate(notifyDate);
                    diaryData.setNotifyTime(notifyTime);
                    String json;

                    //일기 수정 - uri 파싱
                    if (update&& updateImage) {
                        absolutePath = new ArrayList<>();
                        for (int i = 0; i < updateUriArray.size(); i++) {
                            Cursor c = getContentResolver().query(Uri.parse(updateUriArray.get(i).toString()), null, null, null, null);
                            c.moveToNext();
                            String path = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
                            absolutePath.add(path);
                        }
                    }
                    //일기 작성 - uri 파싱
                    else {
                        absolutePath = new ArrayList<>();
                        for (int i = 0; i < uriArray.size(); i++) {
                            Cursor c = getContentResolver().query(Uri.parse(uriArray.get(i).toString()), null, null, null, null);
                            c.moveToNext();
                            String path = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
                            absolutePath.add(path);
                        }
                    }

                    //일기 수정 앨범에서 사진 선택을 안했을때  //일기 수정시  사진은 그대로 일 때 (사진이 선택된게 없거나,그대로일 때)
                    if (updateUriArray.size() == 0 || absolutePath == null||!updateImage ||update) {
                        for (int i = 0; i < updateUriArray.size(); i++) {
                            switch (i) {
                                case 0:
                                    photoOne = updateUriArray.get(i).toString();
                                    break;
                                case 1:
                                    photoTwo = updateUriArray.get(i).toString();
                                    break;
                                case 2:
                                    photoThree = updateUriArray.get(i).toString();
                                    break;
                                case 3:
                                    photoFour = updateUriArray.get(i).toString();
                                    break;
                            }
                        }
                    }
                    //서버 전송 방법
                    //일기 작성일 경우
                    if (!update) {
                        json = JsonBuild.MIF_Calendar_003(diaryData);
                        //선택된 사진이 없을 경우
                        if (absolutePath.size() == 0 || absolutePath == null) {
                            fileUploadUtils.noImageSendServer(json, update);
                        }
                        //선택된 사진을 서버에 저장할 경우
                        else {
                            fileUploadUtils.sendServer(absolutePath, json, update);
                        }
                    }
                    //일기 수정일 경우
                    else {
                        diaryData.setDiaryID(diaryID); //일기 ID 넣어주기
                        diaryData.setNotifyID(notifyID); //알림 ID 넣어주기
                        //기존 이미지를 그대로 보냈을 경우가 있을 수 있기 때문에.
                        diaryData.setPhotoOne(photoOne);
                        diaryData.setPhotoTwo(photoTwo);
                        diaryData.setPhotoThree(photoThree);
                        diaryData.setPhotoFour(photoFour);
                        json = JsonBuild.MIF_Calendar_004(diaryData);

                        if (absolutePath.size() == 0 || absolutePath == null||!updateImage) {
                            fileUploadUtils.noImageSendServer(json, update);
                        }
                        //선택된 사진을 서버에 저장할 경우
                        else {
                            json = JsonBuild.MIF_Calendar_004(diaryData);
                            fileUploadUtils.sendServer(absolutePath, json, update);
                        }
                    }
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}