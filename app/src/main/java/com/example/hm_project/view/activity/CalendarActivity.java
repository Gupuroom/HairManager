package com.example.hm_project.view.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import android.graphics.Color;
import android.os.AsyncTask;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.hm_project.R;

import com.example.hm_project.data.PreferenceManager;
import com.example.hm_project.data.SimpleDiary;
import com.example.hm_project.data.APIManager;
import com.example.hm_project.databinding.ActivityCalendarBinding;
import com.example.hm_project.etc.ChangeDate;
import com.example.hm_project.etc.CurrentDecorator;
import com.example.hm_project.etc.EventDecorator;
import com.example.hm_project.etc.OneDayDecorator;

import com.example.hm_project.etc.PopupActivity;
import com.example.hm_project.etc.SaturdayDecorator;
import com.example.hm_project.etc.SundayDecorator;

import com.example.hm_project.util.CodeManager;
import com.example.hm_project.etc.DateTitleDialog;
import com.example.hm_project.util.HM_Singleton;
import com.example.hm_project.util.JsonParser;
import com.example.hm_project.util.NetworkManager;
import com.example.hm_project.util.NetworkTask;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;

import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import java.util.concurrent.Executors;

import static android.app.Activity.RESULT_OK;

/***
 *  캘린더
 *  1- 네트워크 통신 점검
 *  2- 서버 연결
 *  3- 일기 기록 받아오기
 *  4- 날짜 별로 빨간 점으로 표시  + 오늘 날짜 표시
 *  5- 심플뷰 보여주기
 *  기능
 *  1- 이번달 날짜 선택
 *  2- 달 변경
 *  3- 날짜 변경
 *  4- 같은 날짜 두번 클릭 시 일기 기록/기록 X (하루 일기 3개 초과 유무)
 *  5- 심플뷰 다이어리 클릭 시 상세 일기정보
 *  6- 플러스 버튼 클릭 시 일기 기록/ 기록 x (하루 일기 3개 초과 유무)
 *  서버와 통신 데이터 처리
 *  1. 서버에서 Handler Message 전달 하여 Handler로 서버에 따라 변경되는 UI 변경됨 (오류 처리 포함)
 *  2. 서버에서 오는 대량 데이터는 JSonParser를 싱글톤화 하여 JSonParser로 데이터 추출
 */
public class CalendarActivity extends Fragment {
    //Cleandar 변수
    private final OneDayDecorator oneDayDecorator = new OneDayDecorator(); //오늘 날짜 녹색 굵은 글자 표시
    EventDecorator eventDecorator = new EventDecorator(Color.RED); //빨간 점 표시
    CurrentDecorator selectDayDecorator; //선택 날짜 네모박스 그리기 이벤트 클래스
    MaterialCalendarView materialCalendarView;  //캘린더 위젯

    //JsonParser 싱글톤 화
    JsonParser jp = HM_Singleton.getInstance(new JsonParser());
    //심플뷰 다이어리
    SimpleDiary[] simpleDiary = new SimpleDiary[3];

    //오늘날짜
    CalendarDay today = oneDayDecorator.today();
    String strTodday = today.getYear() + "-" + today.getMonth() + "-" + today.getDay();

    //선택된 날짜
    String selectedDate;

    //GET URL Parameter
    final int userNO = Integer.parseInt(PreferenceManager.getString(LoginActivity.mContext,"userNO"));
    String url = APIManager.Calendar001_URL + userNO + "&month=";

    //Thread 변수
    NetworkTask networkTask; // Asynck Task Class
    public static Handler mainHandler;  // Main Thread Handler
    Message msg;

    //Server 에서 가져온 DATA 변수들
    String[] monthDiaryDate;
    String[] monthDiaryTitle;
    int[] monthDiaryID;
    int resultCode = 0; //AsyncTask 결과 값

    boolean checkThreeDiary = false; //일기 기록 3개 체크
    boolean titleChange = false; //타이틀로 달력을 변경했을 경우

    //바인딩 변수
    ActivityCalendarBinding binding; //바인딩 생성
    public String diaryDate = strTodday; //날짜 보이는 TextView 글씨
    private  FirebaseCrashlytics crashlytics;
    @SuppressLint("HandlerLeak")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.activity_calendar, container, false); //레이아웃 연결
        View v = binding.getRoot(); //레이아웃에서 생성한 변수를 사용 할 수 있도록 한다.
        crashlytics = FirebaseCrashlytics.getInstance();
       // init(v); //위젯 init
        calendarInit(v); //캘린더 기능 init
        diaryBtnSetting(); //다이어리 버튼 셋팅
        plusOnclick(v); // 플러스 버튼 기능

        //매인 핸들러
        mainHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        drawCalendar(); //JsonParser에서 데이터 받기 +일기 기록된 날짜 빨간 점 그리기
                        setSimpleView(selectedDate); //심플뷰에 다이어리 버튼을 보여준다.
                        break;
                    case 2:
                        viewPopup(msg.arg1);
                        Log.i("네트워크 통신 반환 값",""+msg.arg1);
                        break;
                    case 3:
                        //일기 작성 & 수정
                        decoratorRefresh(); // 캘린더 뷰 Decorator 초기화
                        selectedDate = msg.obj.toString(); // 작성한 날짜 받아오기
                        dayDecorator(ChangeDate.calendarDay(selectedDate)); // 선택 날짜 네모 박스 그리기

                        serverConnection(selectedDate); //작성한 날짜로 서버 업데이트 하기 --> 1 or 2
                        materialCalendarView.setCurrentDate(ChangeDate.calendarDay(selectedDate)); //작성한 날짜로 달력 이동하기
                        binding.calendarDiaryDateTV.setText(selectedDate); //선택 날짜 심플뷰애 있는 Textview에 보이도록
                        Toast.makeText(getContext(),"일기 작성 완료",Toast.LENGTH_LONG).show();
                      break;
                    case 4:
                        //일기 삭제
                        decoratorRefresh(); // 캘린더 뷰 Decorator 초기화
                        selectedDate = msg.obj.toString(); // 삭제한 날짜 받아오기
                        dayDecorator(ChangeDate.calendarDay(selectedDate)); // 선택 날짜 네모 박스 그리기

                        serverConnection(selectedDate); //삭제한 날짜로 서버 업데이트 하기 --> 1 or 2
                        materialCalendarView.setCurrentDate(ChangeDate.calendarDay(selectedDate)); //작성한 날짜로 달력 이동하기
                        Toast.makeText(getContext(),"일기 삭제 완료",Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };
        //앱 처음 접속시 이번달과 오늘날 기준으로 UI 구성과 서버 데이터를 가져온다.
        selectedDate = strTodday;
        dayDecorator(ChangeDate.calendarDay(selectedDate));
        serverConnection(strTodday); //서버와 연결 한다. + 네트워크 점검
        return v;
    }

    private void calendarInit(View v) {
        materialCalendarView = v.findViewById(R.id.calendarView);  //캘린더 위젯 초기화
        //달력 속성 셋팅
        materialCalendarView.state().edit()
                .setFirstDayOfWeek(Calendar.SUNDAY) //일요일부터 시작
                //올해로 부터 +20년도까지 일기 작성가능
                .setMinimumDate(CalendarDay.from(2000, 0, 1)) // 달력의 시작  0부터 시작해서 11까지  1~12 월임
                .setMaximumDate(CalendarDay.from(today.getYear()+20, 11, 31)) // 달력의 끝
                .setCalendarDisplayMode(CalendarMode.MONTHS) //월 기준 Display
                .commit();
        //달력 UI 셋팅
        materialCalendarView.addDecorators(
                new SundayDecorator(), //일요일 표시
                new SaturdayDecorator(), //토요일 표시
                oneDayDecorator); //오늘 날짜 표시


        //날짜를 클릭 할 때 이벤트
        materialCalendarView.setOnDateChangedListener((widget, date, selected) -> {
            int Year = date.getYear();
            int Month = date.getMonth() + 1;
            int Day = date.getDay();
            dayDecorator(date); //날짜에 사각형 그리기
            binding.calendarDiaryDateTV.setText(ChangeDate.set(Year, Month, Day)); //선택된 날짜 TextView에 날짜 보이기

            //같은 날짜 두번 클릭 체크
            if (selectedDate != null && selectedDate.equals(ChangeDate.set(Year, Month, Day))) {
                if (checkThreeDiary) { //일기 기록 초과일 경우
                    Log.i("일기 기록 초과", "하루에 일기 3개 기록을 초과하여 작성하려고 합니다.");
                    viewPopup(CodeManager.WriteDiaryOver);
                } else {   //일기 작성으로 이동
                    Log.i("일기 작성", "일기 작성으로 이동합니다." + ChangeDate.set(Year, Month, Day));
                    Intent intent = new Intent(getActivity(), WriteDiaryActivity.class);
                    intent.putExtra("diaryDate", selectedDate);
                    startActivity(intent);
                }
            }
            selectedDate = ChangeDate.set(Year, Month, Day); //선택된 날짜 입력
            // Log.i("선택 날짜 : ", selectedDate);
            setSimpleView(selectedDate); // 심플뷰 다이어리 보여주도록
        });

        //달을 바꿀 때 이벤트
        materialCalendarView.setOnMonthChangedListener((widget, date) -> {
            //setOnTitleClick 이벤트 발생하면 자동으로 onMonthChange 실행 되기 때문에 구별을 두기 위함
            if (titleChange) {
                titleChange = false;
                Log.i("qweas하핫","달변경 return");
                return;
            } else {
                //달력  체인지 하면  Day는 그 달에 라이브러리가 알아서 1일 짜로 받아옴
                int Year = date.getYear();
                int Month = date.getMonth() + 1;
                int Day = date.getDay();
                Log.i("qweas하핫","달 변경");
                if (Month == today.getMonth()) {  //이번 달 일 경우는 오늘 날짜로 대표 날짜 지정하기
                    selectedDate = ChangeDate.set(Year, Month, today.getDay()); //오늘 날짜 입력
                    dayDecorator(ChangeDate.calendarDay(selectedDate)); //날짜에 사각형 그리기
                    Log.i("하핫","오늘 날짜"+selectedDate);
                    serverConnection(selectedDate);

                } else {
                    selectedDate = ChangeDate.set(Year, Month, Day); //선택된 날짜 입력
                    dayDecorator(date); //날짜에 사각형 그리기
                    Log.i("하핫","다른 날짜"+selectedDate);
                    serverConnection(selectedDate);

                }
                binding.calendarDiaryDateTV.setText(selectedDate); //심플뷰에서 선택 날짜 보이도록
            }
        });

        //년도 클릭시 Dialog 생성
        materialCalendarView.setOnTitleClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //현재 년도 월 날짜를  기준으로 시작해서 설정 할 수 있도록 한다.
                DateTitleDialog dateTitleDialog;
                //선택된 날짜가 있을때 다이얼로그 생성
                if (selectedDate != null) {
                    dateTitleDialog = new DateTitleDialog(getActivity(), ChangeDate.Year(selectedDate), ChangeDate.Month(selectedDate), ChangeDate.Day(selectedDate), today.getYear());
                }
                //다이얼로그 처음 생성 시 NPE 오류
                else {
                    dateTitleDialog = new DateTitleDialog(getActivity(), today.getYear(), today.getMonth() - 1, today.getDay(), today.getYear());
                }
                //Callback
                dateTitleDialog.setDilalogListener(new DateTitleDialog.DialogListner() {
                    @Override //back
                    public void onPostiveClicked(int _year, int _month, int _day) {
                        dayDecorator(CalendarDay.from(_year, _month, _day)); //날짜에 사각형 그리기
                        selectedDate = ChangeDate.set(_year, _month + 1, _day); //선택된 날짜 입력
                        titleChange = true; //중요함!! 타이틀 클릭 true -> 자동으로 onMonthChange실행 되기 때문에 구별을 두기 위함
                        materialCalendarView.setCurrentDate(CalendarDay.from(_year, _month, _day)); //변경한 날짜로 달력 이동하기
                        serverConnection(selectedDate); //서버 연결
                    }
                });
            }
        });
    }

    //MIF-Calendar-001 데이터 가져오기 //Network Thread
    private void serverConnection(String date) {
        //모바일 기기에서 네트워크 통신이 되면 서버 연결을 시도한다.
        String urlDate = url + date;
        if (NetworkManager.networkCheck(getActivity())) {
            Log.i("서버 연결", "서버 연결 시도");
            //달력 이동이 계속 될 경우 서버 연결이 중복 되므로 이전 연결은 취소 시키고 새로운 서버 연결을 시도한다.
            if(networkTask!=null){
                if(networkTask.getStatus()==AsyncTask.Status.PENDING||networkTask.getStatus()==AsyncTask.Status.RUNNING){
                    networkTask.cancel(true);
                    Log.i("서버 연결중", "서버 연결 강제 취소");
                }
            }
            networkTask = new NetworkTask("MIF-Calendar-001", urlDate, mainHandler); //Network Thread 초기화
            networkTask.executeOnExecutor(Executors.newSingleThreadExecutor()); //서버 연결
        } else {
            Log.i("네트워크 연결 오류", "네트워크 연결이 되어있지 않음");
            viewPopup(CodeManager.NewtWork_Error);
            decoratorRefresh();
        }
    }
    // 캘린더뷰 디코레이터 초기화
    private void decoratorRefresh(){
        materialCalendarView.removeDecorators();
        //materialCalendarView.clearSelection();

        materialCalendarView.addDecorators(
                new SundayDecorator(), //일요일 표시
                new SaturdayDecorator(), //토요일 표시
                oneDayDecorator); //오늘 날짜 표시

        monthDiaryDate=null;
        setSimpleView(selectedDate); //심플뷰 초기화

    }
    //이번 달 전체 일기중 한 날짜만 일기 기록 가져와서 심플뷰에 보여주도록
    private void setSimpleView(@NonNull String _date) {
        int num = 0; // array point
        //NPE 에러
        if (monthDiaryDate != null) {
            for (int i = 0; i < monthDiaryDate.length; i++) {
                //더 이상 검색 필요 없기 때문에 일기 기록 3개 넘었으면  break
                if (num >= 3) {
                    break;

                } else {
                    // 날짜 한개씩 대입
                    String Date = monthDiaryDate[i];
                    //선택된 날짜와 검색된 날짜가 같을 경우 다이어리 버튼을 활성화 시켜준다.
                    if (Date.equals(_date)) {
                        Button diaryBtn = (Button) binding.diaryConstraintLayout.getChildAt(num);
                        simpleDiary[num] = new SimpleDiary(monthDiaryTitle[i], monthDiaryID[i]);
                        // Log.i("심플 뷰 체크 ",simpleDiary[num].title+" 아이디 :"+simpleDiary[num].diaryID);
                        binding.setSimpleDiary(simpleDiary[num]);
                        diaryBtn.setText(simpleDiary[num].title);
                        diaryBtn.setVisibility(View.VISIBLE);
                        // Log.i("서버 날짜 : ",Date+ " 선택 날짜: " +_date);
                        num++; //다이어리 버튼 1~3
                    }
                }
            }
        } else {
            //Toast.makeText(getActivity(), "일기 데이터가 없습니다.", Toast.LENGTH_SHORT);
        }

        //일기 기록이 0개 일 경우
        switch ((num)) {
            case 0: //일기 기록이 1개 일 경우
                binding.diary3.setVisibility(View.INVISIBLE);
                binding.diary2.setVisibility(View.INVISIBLE);
                binding.diary1.setVisibility(View.INVISIBLE);
                binding.calendarDiaryNullTV.setVisibility(View.VISIBLE);
                checkThreeDiary = false;
                break;
            case 1: //일기 기록이 1개 일 경우
                binding.diary3.setVisibility(View.INVISIBLE);
                binding.diary2.setVisibility(View.INVISIBLE);
                binding.calendarDiaryNullTV.setVisibility(View.INVISIBLE);
                checkThreeDiary = false;
                break;
            case 2: //일기 기록이 2개 일 경우
                binding.diary3.setVisibility(View.INVISIBLE);
                binding.calendarDiaryNullTV.setVisibility(View.INVISIBLE);
                checkThreeDiary = false;
                break;
            case 3: //일기 기록이 3개 일 경우
                binding.calendarDiaryNullTV.setVisibility(View.INVISIBLE);
                checkThreeDiary = true;
                break;
        }
    }

    //날짜에 사각형 그리기
    private void dayDecorator(CalendarDay date) {
        //예외 처리
        if (selectedDate != null) {
            materialCalendarView.removeDecorator(selectDayDecorator); //전 사각형 지우기
            //Log.i("사각형 지우기","성공");
        }
        // Log.i("사각형 그리기","성공");
        selectDayDecorator = new CurrentDecorator(date, getActivity());
        materialCalendarView.addDecorator(selectDayDecorator); //현재 선택된 날짜 사각형 만들기
        materialCalendarView.clearSelection(); //초기화
    }

    //플러스 버튼 클릭 리스너
    public void plusOnclick(View v) {
       binding.calendarPlusBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               Intent intent;
               if (checkThreeDiary) { //일기 3개기록 초과
                   Log.i("일기 기록 초과", "하루에 일기 3개 기록을 초과하여 작성하려고 합니다.");
                   viewPopup(CodeManager.WriteDiaryOver);
               }
               //일기 작성
               else {
                   intent = new Intent(getActivity(), WriteDiaryActivity.class);
                   intent.putExtra("diaryDate",selectedDate);
                   startActivity(intent);
               }
           }
       });
    }

    //심플뷰 버튼 -> 일기 기록 페이지로 이동
    private void diaryBtnSetting() {
        Button.OnClickListener onClickListener = new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                switch (view.getId()) {
                    case R.id.diary1:
                        intent = new Intent(getActivity(), DetailDiaryActivity.class);
                        intent.putExtra("userNO", userNO);
                        intent.putExtra("diaryDate", selectedDate);
                        intent.putExtra("diaryID", simpleDiary[0].diaryID);
                        startActivity(intent);
                        break;
                    case R.id.diary2:
                        intent = new Intent(getActivity(), DetailDiaryActivity.class);
                        intent.putExtra("userNO", userNO);
                        intent.putExtra("diaryDate", selectedDate);
                        intent.putExtra("diaryID", simpleDiary[1].diaryID);
                        startActivity(intent);
                        break;
                    case R.id.diary3:
                        intent = new Intent(getActivity(), DetailDiaryActivity.class);
                        intent.putExtra("userNO", userNO);
                        intent.putExtra("diaryDate", selectedDate);
                        intent.putExtra("diaryID", simpleDiary[2].diaryID);
                        startActivity(intent);
                        break;
                }
            }
        };
        binding.diary1.setOnClickListener(onClickListener);
        binding.diary2.setOnClickListener(onClickListener);
        binding.diary3.setOnClickListener(onClickListener);
    }

    //서버에서 캘린더 기록 받고 그리기 호출
    public void drawCalendar() {
        monthDiaryDate = jp.getMonths(); // 이번 달 일기  DATE 받기
        monthDiaryTitle = jp.getTitles(); // 이번 달 일기 달 TITLE 받기
        monthDiaryID = jp.getDiaryNOs(); // 이번 달 일기 달 Diary.ID 받기
        //그리기 호출
        if(getActivity()!=null)
            if(!getActivity().isDestroyed()){
                new ApiSimulator(monthDiaryDate).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            }
    }

    //팝업창 메소드
    private void viewPopup(int CODE) {
        if(getActivity()!=null){
            if(!getActivity().isDestroyed()){
                Intent intent = new Intent(getActivity(), PopupActivity.class);
                intent.putExtra("code", CODE);
               // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivityForResult(intent, 100);
            }
        }
    }
    //그리기 쓰레드
    private class ApiSimulator extends AsyncTask<Void, Void, List<CalendarDay>> {
        String[] Time_Result;

        ApiSimulator(String[] _Time_Result) {
            this.Time_Result = _Time_Result;
        }

        @Override
        protected List<CalendarDay> doInBackground(@NonNull Void... voids) {
            Calendar calendar = Calendar.getInstance();
            ArrayList<CalendarDay> dates = new ArrayList<>();
            /*특정날짜 달력에 점표시해주는곳*/
            /*월은 0이 1월 년,일은 그대로*/
            //string 문자열인 Time_Result 을 받아와서 ,를 기준으로짜르고 string을 int 로 변환
            if (Time_Result == null) {
                return null;
            } else {
                for (int i = 0; i < Time_Result.length; i++) {
                    if (Time_Result[i] != null) {
                        String[] time = Time_Result[i].split("-");
                        int year = Integer.parseInt(time[0]);
                        int month = Integer.parseInt(time[1]);
                        int dayy = Integer.parseInt(time[2]);

                        calendar.set(year, month - 1, dayy);
                        CalendarDay day = CalendarDay.from(calendar); //순서 중요!!
                        dates.add(day);
                    } else {
                        break;
                    }
                }
            }
            return dates;
        }

        @Override
        protected void onPostExecute(@NonNull List<CalendarDay> calendarDays) {
            super.onPostExecute(calendarDays);
            //액티비티 종료가 됐을 경우 리턴
            if (isDetached()) {
                return;
            }
            //빨간 점 표시해주는 곳
            if(calendarDays!=null){
               // materialCalendarView.addDecorator(new EventDecorator(Color.RED, calendarDays));
                eventDecorator.setDates(calendarDays);
                materialCalendarView.addDecorator(eventDecorator);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {

            }
        }
    }

}