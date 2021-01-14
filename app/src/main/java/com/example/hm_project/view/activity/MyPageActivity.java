package com.example.hm_project.view.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.example.hm_project.Command.PopupListener;
import com.example.hm_project.Command.InterfaceManager;
import com.example.hm_project.R;
import com.example.hm_project.data.PreferenceManager;
import com.example.hm_project.data.APIManager;
import com.example.hm_project.data.UserProfileData;
import com.example.hm_project.databinding.ActivityMypageBinding;
import com.example.hm_project.etc.MyPageViewModel;
import com.example.hm_project.etc.PopupActivity;
import com.example.hm_project.util.CodeManager;
import com.example.hm_project.util.FileUploadUtils;
import com.example.hm_project.util.HM_Singleton;
import com.example.hm_project.util.JsonBuild;
import com.example.hm_project.util.JsonParser;
import com.example.hm_project.util.NetworkManager;
import com.example.hm_project.util.NetworkTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;

import static android.app.Activity.RESULT_OK;

/***
 * 회원 헤어정보
 * 1. 회원 헤어정보 가져오기
 * 기능
 * 1.회원 프로필 사진 변경 ->서버 업데이트
 * 2.
 * 3.
 * 4.
 */
public class MyPageActivity extends Fragment {

    private String Tag = "MyPageActivity 이동재";

    private PopupListener popupListener = new PopupListener();
    private ActivityMypageBinding binding;
    private MyPageViewModel myPageViewModel;
    //JsonParser 싱글톤 화
    JsonParser jp = HM_Singleton.getInstance(new JsonParser());

    Toast toast;

    private Handler mainHandler;
    private FileUploadUtils fileUploadUtils;

    //마이페이지 기본 정보 변수
    private String userName, userEmail, userProfilePhoto;
    final int userNO = Integer.parseInt(PreferenceManager.getString(LoginActivity.mContext, "userNO"));
    //네트워크 변수
    NetworkTask networkTask; // Asynck Task Class
    String url = APIManager.MyPage_001_URL;

    @SuppressLint("HandlerLeak")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        myPageViewModel = new ViewModelProvider(this).get(MyPageViewModel.class); //뷰모델 생성
        binding = DataBindingUtil.inflate(inflater, R.layout.activity_mypage, container, false);
        binding.setFragment(this);
        View root = binding.getRoot();
        setHasOptionsMenu(true);
        init(root);
        binding.setViewModel(myPageViewModel); // 뷰모델 바인딩
        binding.setLifecycleOwner(this); //꼭 해줘야 함

        mainHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        //프로필 정보를 받아서 View에 보여줌
                        viewProfile();
                        break;
                    case 2:
                        //프로필 정보를 제대로 받지 못했을 경우
                        viewPopup(CodeManager.MyPageError);
                        break;
                    case 3:
                        //프로필 사진이 정상적으로 저장된 경우
                        String path = msg.obj.toString(); //사진 주소를 받고
                        changePhoto(path); //변경된 사진 path로 프로필 사진을 보여준다
                        break;
                    case 4:
                        //API 응답 코드 이미지 데이터 크기가 너무 클 경우
                        if (msg.obj.equals(CodeManager.LargeImageDataException)) {
                            viewPopup(CodeManager.LargeImageDataException);
                        } else {
                            //프로필 사진을 서버에 저장을 못한경우
                            viewPopup(CodeManager.UpdateProfilePhotoError);
                        }
                        break;
                }
            }
        };
        //유저 이름 옵저버
        myPageViewModel.getUserName().observe(getViewLifecycleOwner(), newName -> {
            userName = newName;
        });
        //유저 이메일 옵저버
        myPageViewModel.getUserEmail().observe(getViewLifecycleOwner(), newEmail -> {
            userEmail = newEmail;
        });

        //프로필 정보 받아오기
        serverConnection("MIF-MyPage-001", url + userNO);

        return root;
    }

    //프로필 사진 변경
    private void changePhoto(String path) {
        // path는 안드로이드에 저장된 사진 경로임
        Glide.with(this)
                .load(path) //Load
                .signature(new ObjectKey(UUID.randomUUID().toString())) //  Glide Cache clear
                .placeholder(R.drawable.ic_account) // Glide 이미지 로딩 전 보여줄 이미지
                .error(R.drawable.error_uri) // 리소스 불러오다가 에러가 났을때 이미지
                .fallback(R.drawable.ic_account) // 로드 할 경우 URI가 Null 인경우
                .into(binding.ivProfile); //이미지를 보여줄 이미지뷰 대상
    }

    //프로필 정보를 받아서 View에 보여줌
    private void viewProfile() {
        if (getActivity() != null) {
            if (!getActivity().isDestroyed()) {
                UserProfileData userProfileData = jp.getUserProfile();
                myPageViewModel.getUserName().setValue(userProfileData.getUserName());
                myPageViewModel.getUserEmail().setValue(userProfileData.getUserEmail());
                userProfilePhoto = userProfileData.getUserProfilePhoto();
                //HTTP URL 검사
                if (URLUtil.isHttpUrl(userProfilePhoto)) {
                    Glide.with(this)
                            .load(userProfilePhoto) //Load
                            .signature(new ObjectKey(UUID.randomUUID().toString())) // Glide Cache clear
                            .placeholder(R.drawable.ic_account) // Glide 이미지 로딩 전 보여줄 이미지
                            .thumbnail(0.1f)
                            .error(R.drawable.error_uri) // 리소스 불러오다가 에러가 났을때 이미지
                            .fallback(R.drawable.ic_account) // 로드 할 경우 URI가 Null 인경우
                            .into(binding.ivProfile); //이미지를 보여줄 이미지뷰 대상
                } else {
                    //프로필 데이터가 기본값 일 경우
                    if (userProfilePhoto.equals("profile")) {
                        Glide.with(this)
                                .load(R.drawable.ic_account) //Load
                                //.signature(new ObjectKey(UUID.randomUUID().toString())) // Cache 지우지 않고 기존 이미지로 대채하기
                                .placeholder(R.drawable.ic_account) // Glide 이미지 로딩 전 보여줄 이미지
                                .thumbnail(0.1f)
                                .error(R.drawable.error_uri) // 리소스 불러오다가 에러가 났을때 이미지
                                .fallback(R.drawable.ic_account) // 로드 할 경우 URI가 Null 인경우
                                .into(binding.ivProfile); //이미지를 보여줄 이미지뷰 대상
                    }
                    //기본값도 아니고 HTTP URL도 아닐 경우 팝업
                    else {
                        viewPopup(CodeManager.IncorrectURL);
                    }
                }
            }
        }
    }

    private void init(View v) {
        // 툴바 표시
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(binding.toolbar);
        activity.setTitle("");

        //Obejct Value Setting
        binding.tvNickname.setText("이름");
        binding.tvEmail.setText("이메일");
    }

    //MIF-Calendar-001 데이터 가져오기 //Network Thread
    private void serverConnection(String api, String _url) {
        if (getActivity() != null) {
            //모바일 기기에서 네트워크 통신이 되면 서버 연결을 시도한다.
            if (NetworkManager.networkCheck(getActivity())) {
                Log.i("서버 연결", "서버 연결 시도");
                networkTask = new NetworkTask(api, _url, mainHandler); //Network Thread 초기화
                networkTask.executeOnExecutor(Executors.newSingleThreadExecutor()); //서버 연결
            } else {
                Log.i("네트워크 연결 오류", "네트워크 연결이 되어있지 않음");
                viewPopup(CodeManager.NewtWork_Error);
            }
        }
    }

    //팝업창 메소드
    private void viewPopup(int CODE) {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), PopupActivity.class);
            intent.putExtra("code", CODE);
            startActivityForResult(intent, 100);
        }
    }

    // 상단바 옵션 메뉴 클릭 이벤트 처리
    public boolean onOptionsItemSelected(MenuItem item) {
        //return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.action_Logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                //builder.setTitle("Dlete ");
                builder.setMessage("로그아웃 하시겠습니까 ?")
                        .setCancelable(false)
                        .setPositiveButton("로그아웃",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        PreferenceManager.clear(LoginActivity.mContext);

                                        UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
                                            @Override
                                            public void onCompleteLogout() {
                                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                            }
                                        });
                                    }
                                })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                builder.show();
                break;

            case R.id.action_PasswordChange:
                int kakaoCheck = PreferenceManager.getInt(LoginActivity.mContext,"kakaoCheck");
                if(kakaoCheck==1){
                    popupListener.popupEvent(getActivity(), "카카오로 로그인 하였습니다.", "변경할 비밀번호가 없습니다.");
                }
                else {
                    Intent intent = new Intent(getActivity(), UpdatePasswordActivity.class);
                    startActivity(intent);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // 프로필 사진 클릭했을 때
    public void onProfileClick(View view) {
        PermissionListener permissionlistener = new PermissionListener() {
            //권한 설정 허용 하면 앨범 이동
            @Override
            public void onPermissionGranted() {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.putExtra(Intent.ACTION_GET_CONTENT, true); // 삼성폰 생략 갸능/ 다른 폰 필수
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 100);
            }

            //권한 설정 거부하면 토스트 메세지
            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                if (toast == null) {
                    toast = Toast.makeText(getActivity(), "권한을 설정하셔야 합니다.", Toast.LENGTH_SHORT);
                } else {
                    toast.setText("권한을 설정하셔야 합니다.");
                }
                toast.show();
            }
        };
        //READ_EXTERNAL_STORAGE , WRITE_EXTERNAL_STORAGE 권한 허용 체크
        new TedPermission(getActivity())
                .setPermissionListener(permissionlistener)
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();
    }

    // 알람 클릭했을 때
    public void onAlarmClick(View view) {
        // 네트워크 연결 확인
        if (!NetworkManager.networkCheck(getActivity())) {
            Log.i(Tag, "네트워크 연결 문제 발생");
            popupListener.viewPopup(getActivity(), CodeManager.NewtWork_Error);
            // 서버 연결 확인
        } else if (!serverCheck()) {
            Log.i(Tag, "서버 연결 문제 발생");
            popupListener.popupEvent(getActivity(), "서버 연결 문제 발생", "알람 데이터 로드 실패");
        } else {
            Intent intent = new Intent(getActivity(), NotifyActivity.class);
            startActivity(intent);
        }
    }

    // 헤어특징 버튼 클릭했을 때
    public void onHairCharacteristicClick(View view) {
        //헤어 특징 설정 시 네트워크 체크
        if (NetworkManager.networkCheck(getActivity())) {
            Intent intent = new Intent(getActivity(), HairSetActivity.class);
            startActivity(intent);
        } else {
            Log.i("네트워크 연결 오류", "네트워크 연결이 되어있지 않음");
            viewPopup(CodeManager.NewtWork_Error);
        }
    }

    //상단바 옵션 추가
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_mypage, menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                //ClipData 또는 Uri를 가져온다
                Uri uri = data.getData();
                ClipData clipData = data.getClipData();

                //이미지 URI 를 이용하여 이미지뷰에 순서대로 세팅한다.
                if (clipData != null) {

                    fileUploadUtils = new FileUploadUtils(mainHandler);
                    String json = JsonBuild.MIF_MyPage_008(userNO);
                    Cursor c = getActivity().getContentResolver().query(Uri.parse(uri.toString()), null, null, null, null);
                    c.moveToNext();
                    String path = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
                    fileUploadUtils.changeProfilePhoto(path, json);
                }
            } else {
                // viewPopup(프로필 이미지를 가져오지 못했습니다.);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

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