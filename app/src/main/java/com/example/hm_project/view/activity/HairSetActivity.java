package com.example.hm_project.view.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.divyanshu.colorseekbar.ColorSeekBar;
import com.example.hm_project.R;
import com.example.hm_project.data.APIManager;
import com.example.hm_project.data.DiaryData;
import com.example.hm_project.data.PreferenceManager;
import com.example.hm_project.data.UserHairData;
import com.example.hm_project.databinding.ActivityHairsetBinding;
import com.example.hm_project.etc.PopupActivity;
import com.example.hm_project.util.CodeManager;
import com.example.hm_project.util.HM_Singleton;
import com.example.hm_project.util.JsonBuild;
import com.example.hm_project.util.JsonParser;
import com.example.hm_project.util.NetworkManager;
import com.example.hm_project.util.NetworkTask;

import java.util.ArrayList;
import java.util.concurrent.Executors;

import petrov.kristiyan.colorpicker.ColorPicker;

/***
 * 회원 헤어 정보
 * 1. 헤어 정보 가져오기
 * 2. 헤어 정보 셋팅
 * 기능
 * 1. 헤어 정보 변경
 *
 */
public class HairSetActivity extends AppCompatActivity{
    //HairCode  --DB 코드임
    final int ThinningLarge = 1001;
    final int Thinningmiddle = 1002;
    final int Thinningsmall = 1003;

    final int QualityCurly = 2001;
    final int QualityHalCurly = 2002;
    final int QualityStraight = 2003;

    final int ShapeLong = 3001;
    final int ShapeSmall = 3002;
    final int ShapeSide = 3003;

    final int userNO = Integer.parseInt(PreferenceManager.getString(LoginActivity.mContext,"userNO"));
    public static Handler mainHandler;
    private static Toast toast;
    private JsonParser jp = HM_Singleton.getInstance(new JsonParser());
    //코드 기본값
    int thinningCode=0;
    int qualityCode=0;
    int shapeCode=0;
    String hairColor="#000000";

    NetworkTask networkTask; // Asynck Task Class

    ActivityHairsetBinding binding;
    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_hairset);
        binding.setHairSet(this);

        //뒤로가기 설정
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //머리색설정
        colorSeek();
        //머리 특징 라디오버튼 리스너
        radioListener();

        mainHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 1:
                        //업데이트 성공시
                        if (msg.obj.equals("MIF-MyPage-002")){
                            if(toast == null) {
                                toast = Toast.makeText(HairSetActivity.this,"저장이 완료되었습니다",Toast.LENGTH_SHORT);
                            } else {
                                toast.setText("저장이 완료되었습니다");
                            }
                            toast.show();
                        }
                        //조회 성공시
                        else if(msg.obj.equals("MIF-MyPage-003")){
                            hairView();
                        }
                        break;
                    case 2:
                        if (msg.obj.equals("MIF-MyPage-002")){
                            viewPopup(CodeManager.HairUpdateError);
                        }else if(msg.obj.equals("MIF-MyPage-003")){
                            viewPopup(CodeManager.HairQueryError);
                        }
                        break;
                }
            }
        };
        //헤어 조회
        serverConnection("MIF-MyPage-003");
    }

    //헤어조회 한 후 UI 변화
    private void hairView(){
        UserHairData userHairData = new UserHairData();
        userHairData = jp.getUserHairData();
        //회원 헤어 데이터
        thinningCode = userHairData.getThinning();
        qualityCode = userHairData.getQuality();
        shapeCode = userHairData.getShape();
        hairColor = userHairData.getHairColor();
        //숱
        switch (thinningCode){
            case ThinningLarge:
                binding.thinningRadioBtn1.setChecked(true);
                break;
            case Thinningmiddle:
                binding.thinningRadioBtn2.setChecked(true);
                break;
            case Thinningsmall:
                binding.thinningRadioBtn3.setChecked(true);
                break;
        }
        //모질
        switch (qualityCode){
            case QualityCurly:
                binding.qualityRadioBtn1.setChecked(true);
                break;
            case QualityHalCurly:
                binding.qualityRadioBtn2.setChecked(true);
                break;
            case QualityStraight:
                binding.qualityRadioBtn3.setChecked(true);
                break;
        }
        //두상
        switch (shapeCode){
            case ShapeLong:
                binding.shapeRadioBtn1.setChecked(true);
                break;
            case ShapeSmall:
                binding.shapeRadioBtn2.setChecked(true);
                break;
            case ShapeSide:
                binding.shapeRadioBtn3.setChecked(true);
                break;
        }
        //머리색
        if(hairColor!=null){
            int color = Color.parseColor(hairColor);
            changeColorView(color);
        }
    }

    //라디오 버튼 그룹 리스너
    private void radioListener() {
        //머리 숱 버튼 리스너
        binding.thinningGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (R.id.thinningRadioBtn1 == checkedId) {

                    thinningCode = ThinningLarge;
                } else if (R.id.thinningRadioBtn2 == checkedId) {
                    thinningCode = Thinningmiddle;
                } else if (R.id.thinningRadioBtn3 == checkedId) {
                    thinningCode = Thinningsmall;
                }
            }
        });
        //머리 질 버튼 리스너
        binding.qualityGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (R.id.qualityRadioBtn1 == checkedId) {
                    qualityCode = QualityCurly;
                } else if (R.id.qualityRadioBtn2 == checkedId) {
                    qualityCode = QualityHalCurly;
                } else if (R.id.qualityRadioBtn3 == checkedId) {
                    qualityCode = QualityStraight;
                }
            }
        });
        //머리 모양 버튼 리스너
        binding.shapeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (R.id.shapeRadioBtn1 == checkedId) {
                    shapeCode = ShapeLong;
                } else if (R.id.shapeRadioBtn2 == checkedId) {
                    shapeCode = ShapeSmall;
                } else if (R.id.shapeRadioBtn3 == checkedId) {
                    shapeCode = ShapeSide;
                }
            }
        });
    }

    //컬러 SeekBar 리스너
    private void colorSeek(){
        binding.colorSeekBar.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
            @Override
            public void onColorChangeListener(int i) {
                //핵스 값으로 변환
                changeColorView(i);
            }
        });
    }

    //컬러 SeekBar 변화
    private void changeColorView(int i) {
        String hexColor = String.format("#%06X", (0xFFFFFF & i));
        hairColor = hexColor; //hex 값 저장
        //동그라미 Drawable 불러와 설정
        Drawable roundDrawable = getResources().getDrawable(R.drawable.circle_button);
        ColorFilter colorFilter = new PorterDuffColorFilter(i,PorterDuff.Mode.SRC_ATOP);
        roundDrawable.setColorFilter(colorFilter);
        //동그라미 색 설정
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            binding.colorSelectView.setBackgroundDrawable(roundDrawable);
        } else {
            binding.colorSelectView.setBackground(roundDrawable);
        }
    }

    //MIF-Calendar-001 데이터 가져오기 //Network Thread
    private void serverConnection(String api) {
        //모바일 기기에서 네트워크 통신이 되면 서버 연결을 시도한다.

        if (NetworkManager.networkCheck(HairSetActivity.this)) {
            Log.i("서버 연결", "서버 연결 시도");
            //헤어 특징 설정
            if(api.equals("MIF-MyPage-002")){
                String json = JsonBuild.MIF_MyPage_002(userNO, thinningCode, qualityCode, shapeCode, hairColor);
                Log.i("헤어정보 업데이트 값",json);
                networkTask = new NetworkTask("MIF-MyPage-002", APIManager.MyPage_002_URL, json, mainHandler); //Network Thread 초기화
                networkTask.executeOnExecutor(Executors.newSingleThreadExecutor()); //서버 연결
            }
            //헤어 특징 조회
            else if(api.equals("MIF-MyPage-003")) {
                String urlHair = APIManager.MyPage_003_URL+userNO;
                networkTask = new NetworkTask("MIF-MyPage-003", urlHair, mainHandler); //Network Thread 초기화
                networkTask.executeOnExecutor(Executors.newSingleThreadExecutor()); //서버 연결
            }

        } else {
            Log.i("네트워크 연결 오류", "네트워크 연결이 되어있지 않음");
            viewPopup(CodeManager.NewtWork_Error);
        }
    }

    //팝업창 메소드
    private void viewPopup(int CODE) {
        Intent intent = new Intent(HairSetActivity.this, PopupActivity.class);
        intent.putExtra("code", CODE);
        startActivityForResult(intent, 100);
    }

    //액션바 메뉴 생성
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_writepage, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.writePost:
                Log.i("헤어 설정 셋팅","유저의 헤어 정보를 설정을 합니다.");
                serverConnection("MIF-MyPage-002");
                break;
            case android.R.id.home:
                //마이 페이지 이동
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

