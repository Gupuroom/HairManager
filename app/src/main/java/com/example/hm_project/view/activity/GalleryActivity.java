package com.example.hm_project.view.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


import androidx.annotation.NonNull;

import androidx.annotation.RequiresApi;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.hm_project.Command.PopupListener;
import com.example.hm_project.Command.JsonMaker;
import com.example.hm_project.Command.SetDate;

import com.example.hm_project.R;
import com.example.hm_project.data.APIManager;
import com.example.hm_project.data.GalleryData;
import com.example.hm_project.Command.InterfaceManager;
import com.example.hm_project.data.PreferenceManager;
import com.example.hm_project.databinding.ActivityGalleryBinding;
import com.example.hm_project.util.CodeManager;
import com.example.hm_project.util.HM_Singleton;
import com.example.hm_project.util.JsonParser;
import com.example.hm_project.util.NetworkManager;
import com.example.hm_project.view.adapter.GalleryAdapter;
import com.example.hm_project.view.adapter.GalleryDeleteAdapter;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/***
 *  갤러리
 *  1- init ( 서버에서 가져온 데이터를 화면에 뿌려준다. )
 *  2- deleteMode ( 삭제 모드로 전환한다. )
 *  3- getData ( 서버에서 데이터를 받아온다. )
 *  4- scrollGetData ( 스크롤이벤트가 발생하면 getData를 호출한다. )
 *  5- onGalleryDateClick ( 사용자로부터 특정 날짜를 입력받는다. )
 *  6- onGallerySearchClick ( 4의 날짜에 저장된 데이터를 서버로부터 받아온다. )
 *  7- onPause ( pause 발생시 삭제버튼이 안보이게 한다. )
 *  8- serverCheck ( 서버 체크 )
 */

public class GalleryActivity extends Fragment {

    private Context mContext = LoginActivity.mContext;
    private JsonParser jsonParser = HM_Singleton.getInstance(new JsonParser());
    private PopupListener popupListener = new PopupListener();
    private SetDate setDate = new SetDate();
    private ActivityGalleryBinding binding;

    private ArrayList<GalleryData> gArrayList = new ArrayList<>();
    private ArrayList<GalleryData> dArrayList = new ArrayList<>();

    private GalleryDeleteAdapter gdAdapter;

    private Button deleteConfirm = MainActivity.deleteConfirm;
    private int page;
    private boolean deleteModeOn = false;

    private String Tag = "GalleryActivity 이동재";

    @RequiresApi(api = Build.VERSION_CODES.N)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.activity_gallery, container, false);
        binding.setFragment(this);
        View root = binding.getRoot();

        page = 0;  // 갤러리 화면이 클릭될 때마다 페이지를 초기화한다.
        getData(); // 서버에서 데이터를 가져온다.
        scrollGetData();  // 스코롤이벤트가 발생할 때 실행된다.
        init(); // 서버에서 가져온 데이터를 화면에 뿌려준다.

        return root;
    }

    // 1 - init ( 서버에서 가져온 데이터를 화면에 뿌려준다. )
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void init() {
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(getActivity(), 2);
        binding.recyclerview.setLayoutManager(mGridLayoutManager);

        GalleryAdapter gAdapter = new GalleryAdapter(gArrayList); // 어댑터 객체 생성
        binding.recyclerview.setAdapter(gAdapter);  // 리사이클러뷰에 리스트 뿌려준다

        // 아이템 클릭하면 해당 아이템의 상세일기 다이어리로 이동한다.
        gAdapter.setOnItemClickListener((v, position) -> {
            GalleryData gd = gArrayList.get(position);
            Intent intent = new Intent(getActivity(), DetailDiaryActivity.class);
            intent.putExtra("diaryID", Integer.parseInt(gd.getDiaryNO()));
            intent.putExtra("diaryDate", gd.getGDate());
            startActivity(intent);
        });

        // 뷰를 길게 누를시 이벤트 처리 ( 삭제 모드 )
        gAdapter.setOnItemLongClickListener((v, position) -> {
            deleteMode(); // 삭제 모드
            deleteModeOn = true;
        });
    }

    // 2 - deleteMode ( 삭제 모드로 전환한다. )
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void deleteMode() {

        deleteConfirm.setVisibility(View.VISIBLE); // 삭제 확인 박스가 보이게 한다.
        binding.allCheck.setVisibility(View.VISIBLE); // 체크박스가 보이게 한다. ( 전체 선택 )
        binding.checkTitle.setVisibility(View.VISIBLE); // TextBox가 보이게 한다. ( 전체 선택 )

        gdAdapter = new GalleryDeleteAdapter(gArrayList);
        binding.recyclerview.setAdapter(gdAdapter);

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
                for (GalleryData gData : gArrayList) {
                    gData.setGcheck(true);
                    gdAdapter.notifyItemChanged(i, "click");
                    i++;
                }
            } else {
                for (GalleryData gData : gArrayList) {
                    gData.setGcheck(false);
                    gdAdapter.notifyItemChanged(i, "click");
                    i++;
                }
            }
        });

        // 전체선택 클릭버튼 클릭시 이벤트
        binding.allCheck.setOnClickListener(v12 -> {
            int i = 0;
            if (binding.allCheck.isChecked()) {
                for (GalleryData gData : gArrayList) {
                    gData.setGcheck(true);
                    gdAdapter.notifyItemChanged(i, "click");
                    i++;
                }
            } else {
                for (GalleryData gData : gArrayList) {
                    gData.setGcheck(false);
                    gdAdapter.notifyItemChanged(i, "click");
                    i++;
                }
            }
        });

        // 리사이클러뷰의 아이템을 클릭시 이벤트
        gdAdapter.setOnItemClickListener((v13, position1) -> {
            GalleryData gData = gArrayList.get(position1);
            // 클릭한 view의 클릭값이 true일 경우 false로 바꾼다
            if (gData.isGcheck()) {
                gData.setGcheck(false);
            }// 클릭한 view의 클릭값이 false일 경우 true로 바꾼다
            else {
                gData.setGcheck(true);
            }
            gdAdapter.notifyItemChanged(position1, "click");
        });

        // 삭제 확인 버튼 클릭시 이벤트 발생
        deleteConfirm.setOnClickListener(view -> {

            // 삭제 확인 알림을 화면에 띄운다.
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            //builder.setTitle("Dlete ");
            builder.setMessage("삭제하시겠습니까 ?")
                    .setCancelable(false)
                    .setPositiveButton("삭제",
                            (dialog, id) -> {
                                // 네트워크 연결 확인
                                if (!NetworkManager.networkCheck(getContext())) {
                                    Log.i(Tag, "네트워크 연결 문제 발생");
                                    Toast.makeText(getActivity(), "네트워크 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
                                    // 서버 연결 확인
                                } else if (!serverCheck()) {
                                    Log.i(Tag, "서버 연결 문제 발생");
                                    Toast.makeText(getActivity(), "서버연결오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                                } else {
                                    // gArrayList의 체크리스트의 값이 false인 경우 위치를 저장하는 리스트
                                    ArrayList<Integer> deleteNumber = new ArrayList<>();
                                    int i = 0;
                                    for (GalleryData gData : gArrayList) {
                                        if (gData.isGcheck()) {
                                            deleteNumber.add(i);
                                            dArrayList.add(gData); // 서버 전송을 위한 리스트
                                        }
                                        i++;
                                    }

                                    // 아무것도 선택하지 않고 삭제확인을 눌렀을 경우
                                    if (dArrayList.isEmpty()) {
                                        Toast.makeText(getActivity(), "삭제데이터를 선택하지 않으셨습니다.", Toast.LENGTH_SHORT).show();
                                        deleteConfirm.setVisibility(View.INVISIBLE); // 삭제박스가 안보이게 한다.
                                        binding.allCheck.setVisibility(View.INVISIBLE);  // 체크박스가 안 보이게 한다. ( 전체 선택 )
                                        binding.allCheck.setChecked(false); // 체크박스 값을 false로 변경해준다. ( 전체 선택 )
                                        binding.checkTitle.setVisibility(View.INVISIBLE); // TextBox가 안 보이게 한다. ( 전체 선택 )
                                        deleteModeOn = false;
                                        init();
                                    }

                                    // 내림차순 정렬 ( 앞순서부터 삭제시 뒤에 순서가 변경되어 제대로 된 삭제가 되지 않는다. 따라서 뒤에서부터 삭제해주어야 한다. )
                                    deleteNumber.sort(Comparator.reverseOrder());
                                    // 리스트에서 데이터 삭제
                                    for (int l : deleteNumber) {
                                        gArrayList.remove(l);
                                    }
                                    // 리사이클러뷰 업데이트
                                    binding.recyclerview.setAdapter(gdAdapter);
                                    // 서버 업데이트
                                    try {
                                        // 서버에 diaryNO를 보내면 diaryNO에 해당하는 일기기록, 사진, 알람을 삭제한다.
                                        URL url = new URL(APIManager.GalleryDelete_URL);
                                        InterfaceManager task = new InterfaceManager(url);
                                        String json = JsonMaker.jsonArrayMaker(dArrayList);
                                        task.execute(json).get();
                                        dArrayList.clear();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    deleteConfirm.setVisibility(View.INVISIBLE); // 삭제박스가 안보이게 한다.
                                    binding.allCheck.setVisibility(View.INVISIBLE);  // 체크박스가 안 보이게 한다. ( 전체 선택 )
                                    binding.allCheck.setChecked(false); // 체크박스 값을 false로 변경해준다. ( 전체 선택 )
                                    binding.checkTitle.setVisibility(View.INVISIBLE); // TextBox가 안 보이게 한다. ( 전체 선택 )
                                    deleteModeOn = false;
                                    init();
                                }
                            })
                    .setNegativeButton("취소", (dialog, id) -> {
                    });
            builder.show();
        });
    }

    // 3 - getData ( 서버에서 데이터를 받아온다. )
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getData() {
        // 네트워크 연결 확인
        if (!NetworkManager.networkCheck(getContext())) {
            Log.i(Tag, "네트워크 연결 문제 발생");
            popupListener.viewPopup(getActivity(), CodeManager.NewtWork_Error);
            // 서버 연결 확인
        } else if (!serverCheck()) {
            Log.i(Tag, "서버 연결 문제 발생");
            popupListener.popupEvent(getActivity(), "서버 연결 오류", "갤러리 데이터 수신 실패");
        } else {
            try {
                // 서버에 userNO를 보내면 userNO에 해당하는 대표사진, 제목, 저장날짜, 일기 고유번호를 가져온다.
                URL url = new URL(APIManager.Gallery_URL);
                InterfaceManager task = new InterfaceManager(url);
                String userNO = PreferenceManager.getString(mContext, "userNO");
                String json = JsonMaker.jsonObjectMaker("", "", "", "", "", "", "", Integer.toString(page), userNO);
                String returns = task.execute(json).get(); // 9

                // 서버에서 온 값이 비어있지 않을 경우
                if (!returns.equals("[]")) {
                    Log.i(Tag, "서버에서 온 갤러리 데이터: " + returns);
                    List<GalleryData> GList = jsonParser.jsonParsingGallery(returns);

                    // 서버에서 받은 데이터를 파싱한 후 gArrayList 객체에 담는다.
                    for (GalleryData gd : GList) {
                        gArrayList.add(new GalleryData(gd.getGPhoto(), gd.getGTitle(), gd.getGDate(), gd.getDiaryNO()));
                    }

                    // 삭제모드일 경우 스크롤 이벤트가 발생했을 때 삭제모드를 유지한다.
                    if (deleteModeOn) {
                        deleteMode();
                    } else {
                        init();
                    }

                } else { // 서버에서 빈 값이 넘어올 경우
                    Log.i(Tag, "서버에서 빈 값이 넘어옴: ");
                    if (deleteModeOn) { // 삭제모드일 경우 스크롤 이벤트가 발생했을 때 삭제모드를 유지한다.
                        deleteMode();
                    }
                    Toast.makeText(getContext(), "저장된 일기가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 4 - scrollGetData ( 스크롤이벤트가 발생하면 getData를 호출한다. )
    private void scrollGetData() {
        binding.NScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (!binding.NScrollView.canScrollVertically(1)) {
                    deleteConfirm.setVisibility(View.INVISIBLE);
                    page++;
                    getData();
                }
            }
        });
    }

    // 5 - onGalleryDateClick ( 사용자로부터 특정 날짜를 입력받는다. )
    public void onGalleryDateClick(View view) {
        deleteConfirm.setVisibility(View.INVISIBLE);
        setDate.setDate(getActivity(), binding.galleryDate, binding.needNot);
    }

    // 6 - onGallerySearchClick ( 4의 날짜에 저장된 데이터를 서버로부터 받아온다. )
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onGallerySearchClick(View view) {
        mContext = LoginActivity.mContext;
        // 검색창이 비었을 경우 알림팝업을 호출한다.
        if ("".equals(binding.galleryDate.getText().toString())) {
            popupListener.popupEvent(getActivity(), "검색 실패", "검색날짜를 선택해주세요.");
        } else {
            // 네트워크 연결 확인
            if (!NetworkManager.networkCheck(getContext())) {
                Log.i(Tag, "네트워크 연결 문제 발생");
                popupListener.viewPopup(getActivity(), CodeManager.NewtWork_Error);
                // 서버 연결 확인
            } else if (!serverCheck()) {
                Log.i(Tag, "서버 연결 문제 발생");
                popupListener.popupEvent(getActivity(), "서버 연결 오류", "검색 실패");
            } else {
                try {
                    // 서버에 검색날짜, userNO를 보내면 검색날짜와 userNO에 해당하는 대표사진, 제목, 저장날짜를 가져온다.
                    URL url = new URL(APIManager.Gallery_URL);

                    InterfaceManager task = new InterfaceManager(url);
                    String json = JsonMaker.jsonObjectMaker("", "", "", "", binding.galleryDate.getText().toString(), "", "", "",
                            PreferenceManager.getString(mContext, "userNO"));
                    String returns = task.execute(json).get(); // 9

                    if (!returns.equals("[]")) {   // DB에서 해당 날짜에 해당하는 값이 있을 경우 리스트를 비운다.
                        gArrayList.clear();   // 리스트를 비운다.
                        List<GalleryData> Glist = jsonParser.jsonParsingGallery(returns);

                        for (GalleryData gd : Glist) {   // 서버에서 받은 데이터를 파싱한 후 gArrayList 객체에 담는다.
                            gArrayList.add(new GalleryData(gd.getGPhoto(), gd.getGTitle(), gd.getGDate(), gd.getDiaryNO()));
                        }
                        init();   // 화면에 뿌려준다.
                        binding.galleryDate.setText("");  // 검색 텍스트바, 검색 값을 비워준다.

                    } else { // DB에서 해당 날짜에 해당하는 값이 없을 경우 안내 팝업을 띄운다. 가장최신 글을 불러온다.
                        popupListener.popupEvent(getActivity(), "검색 실패", "해당 날짜에 데이터가 없습니다.");  // 팝업창을 띄운다.
                        gArrayList.clear();   // 리스트를 비운다.
                        page = 0; // 페이지를 첫화면으로 변경해준다.
                        getData();  // 가장 최신데이터를 가져와서 화면에 뿌려준다.
                        binding.galleryDate.setText("");  // 검색 텍스트바, 검색 값을 비워준다.
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 7 - 갤러리 화면을 나가게 될 경우 삭제박스가 안 보이게 한다.
    @Override
    public void onPause() {
        super.onPause();
        deleteConfirm.setVisibility(View.INVISIBLE); // 삭제박스가 안보이게 한다.
        binding.allCheck.setVisibility(View.INVISIBLE);  // 체크박스가 안 보이게 한다. ( 전체 선택 )
        binding.checkTitle.setVisibility(View.INVISIBLE); // TextBox가 안 보이게 한다. ( 전체 선택 )

        deleteModeOn = false;

        binding.allCheck.setChecked(false);
        for (GalleryData gData : gArrayList) {
            gData.setGcheck(false);
        }
    }

    // 8 - 서버 체크
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
