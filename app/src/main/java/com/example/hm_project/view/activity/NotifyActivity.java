package com.example.hm_project.view.activity;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.hm_project.Command.PopupListener;
import com.example.hm_project.Command.InterfaceManager;
import com.example.hm_project.Command.JsonMaker;
import com.example.hm_project.R;
import com.example.hm_project.data.APIManager;
import com.example.hm_project.data.NotifyData;
import com.example.hm_project.data.PreferenceManager;
import com.example.hm_project.databinding.ActivityNotifyBinding;
import com.example.hm_project.util.CodeManager;
import com.example.hm_project.util.HM_Singleton;
import com.example.hm_project.util.JsonParser;
import com.example.hm_project.util.NetworkManager;
import com.example.hm_project.view.adapter.NotifyAdapter;
import com.example.hm_project.view.adapter.NotifyDeleteAdapter;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/***
 *  알람
 *  1- init ( 서버에서 가져온 데이터를 화면에 뿌려준다. )
 *  2- getData ( 서버에서 데이터를 받아온다. )
 *  3- scrollGetData ( 스크롤이벤트가 발생하면 getData를 호출한다. )
 *  4- onOptionsItemSelected ( 툴바에 위치한 뒤로가기 버튼 클릭시 이벤트를 정의한다. )
 *  5- onBackPressed ( 핸드폰의 뒤로가기 버튼 클릭시 이벤트를 정의한다. )
 *  6- onPause ( pause 발생시 알람 상태 변경을 DB에 보낸다. )
 *  7- serverCheck ( 서버 체크 )
 */

public class NotifyActivity extends AppCompatActivity {

    private Context mContext = LoginActivity.mContext;
    private JsonParser jsonParser = HM_Singleton.getInstance(new JsonParser());
    private ActivityNotifyBinding binding;
    private PopupListener popupListener = new PopupListener();

    private ArrayList<NotifyData> nArrayList = new ArrayList<>();
    private ArrayList<NotifyData> dArrayList = new ArrayList<>();

    private NotifyAdapter nAdapter;
    private NotifyDeleteAdapter ndAdapter;

    public static int width;
    private int page;

    private boolean deleteModeOn = false; // 삭제모드인지 아닌지 구분하기 위한 변수

    private String Tag = "NotifyActivity 이동재";

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notify);
        binding.setActivity(this);

        page = 0;  // 알람 화면 페이지를 초기화한다.
        getData();
        scrollGetData();
        init();
    }

    // 1- init ( 서버에서 가져온 데이터를 화면에 뿌려준다. )
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void init() {

        binding.notifyCount.setVisibility(View.VISIBLE);

        binding.allCheck.setVisibility(View.INVISIBLE);
        binding.checkTitle.setVisibility(View.INVISIBLE);
        binding.notifyDelete.setVisibility(View.INVISIBLE);

        binding.notifyCount.setText("100 / " + nArrayList.size());
        // 툴바 설정
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");

        // 리사이클러뷰를 리스트뷰 형태로 구현한다.
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(NotifyActivity.this);
        binding.recyclerview.setLayoutManager(mLinearLayoutManager);

        nAdapter = new NotifyAdapter(nArrayList); // 어댑터 객체 생성
        binding.recyclerview.setAdapter(nAdapter);  // 리사이클러뷰에 리스트 뿌려준다

        // 아이템 클릭하면 해당 아이템의 상세일기 다이어리로 이동한다.
        nAdapter.setOnItemClickListener((v, position) -> {
            NotifyData nd = nArrayList.get(position);
            Intent intent = new Intent(NotifyActivity.this, DetailDiaryActivity.class);
            intent.putExtra("diaryID", Integer.parseInt(nd.getDiaryNO()));
            intent.putExtra("diaryDate", nd.getNDate());
            startActivity(intent);
        });

        // 뷰를 길게 누를시 이벤트 처리
        nAdapter.setOnItemLongClickListener((View v, int position) -> {
            deleteMode();
            deleteModeOn = true;
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void deleteMode() {

        binding.notifyCount.setVisibility(View.INVISIBLE);

        binding.allCheck.setVisibility(View.VISIBLE);
        binding.checkTitle.setVisibility(View.VISIBLE);
        binding.notifyDelete.setVisibility(View.VISIBLE);

        ndAdapter = new NotifyDeleteAdapter(nArrayList);
        binding.recyclerview.setAdapter(ndAdapter);

        // 전체선택 텍스트뷰 클릭시 이벤트
        binding.checkTitle.setOnClickListener(v1 -> {
            // 체크박스 true,false를 보고 반대로 설정해준다.
            if (binding.allCheck.isChecked()) {
                binding.allCheck.setChecked(false);
            } else {
                binding.allCheck.setChecked(true);
            }
            // 리사이클러뷰 각 아이템의 체크박스 상태를 업데이트한다.
            int i = 0;
            if (binding.allCheck.isChecked()) {
                for (NotifyData nData : nArrayList) {
                    nData.setNCheck(true);
                    ndAdapter.notifyItemChanged(i, "click");
                    i++;
                }
            } else {
                for (NotifyData nData : nArrayList) {
                    nData.setNCheck(false);
                    ndAdapter.notifyItemChanged(i, "click");
                    i++;
                }
            }
        });

        // 전체선택 클릭버튼 클릭시 이벤트
        binding.allCheck.setOnClickListener(v12 -> {
            int i = 0;
            if (binding.allCheck.isChecked()) {
                for (NotifyData nData : nArrayList) {
                    nData.setNCheck(true);
                    ndAdapter.notifyItemChanged(i, "click");
                    i++;
                }
            } else {
                for (NotifyData nData : nArrayList) {
                    nData.setNCheck(false);
                    ndAdapter.notifyItemChanged(i, "click");
                    i++;
                }
            }
        });

        // 리사이클러뷰의 아이템을 클릭시 이벤트
        ndAdapter.setOnItemClickListener((v13, position1) -> {
            NotifyData nData = nArrayList.get(position1);
            // 클릭한 view의 클릭값이 true일 경우 false로 바꾼다
            if (nData.isNCheck()) {
                nData.setNCheck(false);
            }// 클릭한 view의 클릭값이 false일 경우 true로 바꾼다
            else {
                nData.setNCheck(true);
            }
            ndAdapter.notifyItemChanged(position1, "click");
        });

        // 삭제 확인 버튼 클릭시 이벤트 발생
        binding.notifyDelete.setOnClickListener(view -> {
            // 삭제 확인 알림을 화면에 띄운다.
            AlertDialog.Builder builder = new AlertDialog.Builder(NotifyActivity.this);
            //builder.setTitle("Dlete ");
            builder.setMessage("삭제하시겠습니까 ?")
                    .setCancelable(false)
                    .setPositiveButton("삭제",
                            (dialog, id) -> {
                                // 네트워크 연결 확인
                                if (!NetworkManager.networkCheck(getApplicationContext())) {
                                    Log.i(Tag, "네트워크 연결 문제 발생");
                                    Toast.makeText(this, "네트워크 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
                                    // 서버 연결 확인
                                } else if (!serverCheck()) {
                                    Log.i(Tag, "서버 연결 문제 발생");
                                    Toast.makeText(this, "서버연결오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                                } else {
                                    // nArrayList의 체크리스트의 값이 false인 경우 위치를 저장하는 리스트
                                    ArrayList<Integer> deleteNumber = new ArrayList<>();
                                    int i = 0;
                                    for (NotifyData nData : nArrayList) {
                                        if (nData.isNCheck()) {
                                            deleteNumber.add(i);
                                            dArrayList.add(nData); // 서버 전송을 위한 리스트
                                        }
                                        i++;
                                    }
                                    // 아무것도 선택하지 않고 삭제확인을 눌렀을 경우
                                    if (dArrayList.isEmpty()) {
                                        Toast.makeText(this, "삭제데이터를 선택하지 않으셨습니다.", Toast.LENGTH_SHORT).show();
                                        binding.notifyCount.setVisibility(View.VISIBLE);

                                        binding.allCheck.setVisibility(View.INVISIBLE);
                                        binding.checkTitle.setVisibility(View.INVISIBLE);
                                        binding.notifyDelete.setVisibility(View.INVISIBLE);
                                        binding.allCheck.setChecked(false);
                                        deleteModeOn = false;
                                        init();
                                    }

                                    // 내림차순 정렬 ( 앞순서부터 삭제시 뒤에 순서가 변경되어 제대로 된 삭제가 되지 않는다. 따라서 뒤에서부터 삭제해주어야 한다. )
                                    deleteNumber.sort(Comparator.reverseOrder());
                                    // 리스트에서 데이터 삭제
                                    for (int l : deleteNumber) {
                                        nArrayList.remove(l);
                                    }
                                    // 리사이클러뷰 업데이트
                                    binding.recyclerview.setAdapter(ndAdapter);
                                    // 서버 업데이트
                                    try {
                                        // 서버에 notifyNO를 보내면 notifyNO에 해당하는 알람데이터를 삭제한다.
                                        URL url = new URL(APIManager.NotifyDelete_URL);
                                        InterfaceManager task = new InterfaceManager(url);
                                        String json = JsonMaker.jsonNotifyArrayMaker(dArrayList);
                                        task.execute(json).get();
                                        dArrayList.clear();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    binding.notifyCount.setVisibility(View.VISIBLE);

                                    binding.allCheck.setVisibility(View.INVISIBLE);
                                    binding.checkTitle.setVisibility(View.INVISIBLE);
                                    binding.notifyDelete.setVisibility(View.INVISIBLE);
                                    binding.allCheck.setChecked(false);
                                    deleteModeOn = false;
                                    init();
                                }
                            })
                    .setNegativeButton("취소", (dialog, id) -> {
                    });
            builder.show();
        });
    }

    // 2- getData ( 서버에서 데이터를 받아온다. )
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getData() {
        // 네트워크 연결 확인
        if (!NetworkManager.networkCheck(getApplicationContext())) {
            Log.i(Tag, "네트워크 연결 문제 발생");
            popupListener.viewPopup(NotifyActivity.this, CodeManager.NewtWork_Error);
            // 서버 연결 확인
        } else if (!serverCheck()) {
            Log.i(Tag, "서버 연결 문제 발생");
            popupListener.popupEvent(NotifyActivity.this, "서버 연결 오류", "알람 데이터 수신 실패");
        } else {
            try {
                // 서버에 userNO를 보내면 userNO에 해당하는 알람 데이터를 가져온다.
                URL url = new URL(APIManager.Notify_URL);
                InterfaceManager task = new InterfaceManager(url);
                String userNO = PreferenceManager.getString(mContext, "userNO");
                String json = JsonMaker.jsonObjectMaker("", "", "", "", "", "", "", Integer.toString(page), userNO);
                String returns = task.execute(json).get(); // 9

                if (!returns.equals("[]")) {
                    Log.i(Tag, "서버에서 온 알람 데이터: " + returns);
                    List<NotifyData> NList = jsonParser.jsonParsingNotify(returns);

                    // 서버에서 받은 데이터를 파싱한 후 gArrayList 객체에 담는다.
                    for (NotifyData nd : NList) {
                        nArrayList.add(new NotifyData(nd.getNTitle(), nd.getNDate(), nd.getNTime(), nd.getDiaryNO(), nd.getNonoff(), nd.getNotifyNO()));
                    }

                    // 삭제모드일 경우 스크롤 이벤트가 발생했을 때 삭제모드를 유지한다.
                    if (deleteModeOn) {
                        deleteMode();
                    } else {
                        init();
                    }

                } else {
                    Log.i(Tag, "서버에서 빈 값이 넘어옴: ");
                    if (deleteModeOn) { // 삭제모드일 경우 스크롤 이벤트가 발생했을 때 삭제모드를 유지한다.
                        deleteMode();
                    }
                    Toast.makeText(getApplicationContext(), "저장된 알람이 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 3 - scrollGetData ( 스크롤이벤트가 발생하면 getData를 호출한다. )
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void scrollGetData() {
        binding.NScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (!binding.NScrollView.canScrollVertically(1)) {
                page++;
                getData();
            }
        });
    }

    // 4- onOptionsItemSelected ( 툴바에 위치한 뒤로가기 버튼 클릭시 이벤트를 정의한다. )
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 5- onBackPressed ( 핸드폰의 뒤로가기 버튼 클릭시 이벤트를 정의한다. )
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBackPressed() {
        // status가 false 즉 삭제상태일 때는 원래 상태로 돌려준다.
        if (deleteModeOn) {
            binding.notifyCount.setVisibility(View.VISIBLE);

            binding.allCheck.setVisibility(View.INVISIBLE);
            binding.checkTitle.setVisibility(View.INVISIBLE);
            binding.notifyDelete.setVisibility(View.INVISIBLE);
            binding.allCheck.setChecked(false);
            deleteModeOn = false;
            init();
        } else // status가 true인 상태 즉 일반 상태일때는 뒤로가기를 시켜준다.
            super.onBackPressed();
    }

    // 6- onPause ( pause 발생시 알람 상태 변경을 DB에 보낸다. )
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onPause() {
        super.onPause();
        binding.notifyCount.setVisibility(View.VISIBLE);

        binding.allCheck.setVisibility(View.INVISIBLE);
        binding.checkTitle.setVisibility(View.INVISIBLE);
        binding.notifyDelete.setVisibility(View.INVISIBLE);
        deleteModeOn = false;
        init();

        nAdapter = new NotifyAdapter(nArrayList); // 어댑터 객체 생성
        ArrayList<NotifyData> uArrayList = nAdapter.getnList();
        // 네트워크 연결 확인
        if (!NetworkManager.networkCheck(getApplicationContext())) {
            Log.i(Tag, "네트워크 연결 문제 발생");
            popupListener.popupEvent(NotifyActivity.this, "네트워크 연결 오류", "ON/OFF 변경이 적용되지 않을 수 있습니다.");
            // 서버 연결 확인
        } else if (!serverCheck()) {
            Log.i(Tag, "서버 연결 문제 발생");
            popupListener.popupEvent(NotifyActivity.this, "서버 연결 오류", "ON/OFF 변경이 적용되지 않을 수 있습니다.");
        } else {
            try {
                // 서버에 notifyNO를 보내면 notifyNO에 해당하는 알람데이터를 삭제한다.
                URL url = new URL(APIManager.NotifyUpdateOnOff_URL);
                InterfaceManager task = new InterfaceManager(url);
                String json = JsonMaker.jsonNotifyArrayMaker(uArrayList);
                task.execute(json).get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 7- serverCheck ( 서버 체크 )
    private boolean serverCheck() {
        try {
            URL url = new URL(APIManager.ServerCheck_URL);

            InterfaceManager task = new InterfaceManager(url);
            String serverData = task.execute("").get();
            if (serverData.equals("Yes")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}