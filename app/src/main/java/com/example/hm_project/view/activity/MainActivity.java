package com.example.hm_project.view.activity;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;

import com.example.hm_project.Command.InterfaceManager;
import com.example.hm_project.Command.JsonMaker;
import com.example.hm_project.R;
import com.example.hm_project.data.PreferenceManager;
import com.example.hm_project.databinding.ActivityMainBinding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import java.net.URL;

/***
 *  Main 화면 ( 3가지 프레그먼트를 포함한다. 캘린더 , 갤러리 , 마이페이지 )
 *  1- 뒤로 가기 버튼 클릭시 이벤트
 *  2- FCM 알람을 위한 토큰 값 서버로 전송
 */

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    public static Button deleteConfirm;

    public static int width,height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setActivity(this);

        // 어플이 돌아가는 기기의 화면크기를 구한다. ( 리사이클러뷰의 한개의 객체 크기를 지정해주기 위해서 )
        Display display = getWindowManager().getDefaultDisplay();  // in Activity
        /* getActivity().getWindowManager().getDefaultDisplay() */ // in Fragment
        Point size = new Point();
        display.getRealSize(size); // or getSize(size)
        width = size.x;
        height = size.y;

        deleteConfirm = binding.deleteConfirm;   // 갤러리화면에서 쓸 삭제확인버튼 정의

        // 하단 네비게이션 바 정의
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment) ;
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.navView, navController);

    }

    // 1 - 뒤로 가기 버튼 클릭시 이벤트
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        deleteConfirm.setVisibility(View.INVISIBLE);
    }

    // 2 - FCM 알람을 위한 토큰 값 서버로 전송
    @Override
    protected void onStart() {
        try {
            URL url = new URL("http://218.234.77.97:8080/HairManager/Mypage/InputFCMToken.jsp");

            InterfaceManager task = new InterfaceManager(url);
            String userNO = PreferenceManager.getString(LoginActivity.mContext,"userNO");
            String token = PreferenceManager.getString(LoginActivity.mContext, "token");
            String json = JsonMaker.jsonObjectMaker("", "", "", "", "", "", token, "", userNO);
            task.execute(json).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStart();
    }
}