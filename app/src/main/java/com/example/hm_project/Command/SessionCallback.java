package com.example.hm_project.Command;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.hm_project.data.APIManager;
import com.example.hm_project.data.PreferenceManager;
import com.example.hm_project.view.activity.LoginActivity;
import com.example.hm_project.view.activity.MainActivity;
import com.kakao.auth.ISessionCallback;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.ApiErrorCode;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.OptionalBoolean;
import com.kakao.util.exception.KakaoException;

import java.net.URL;

/***
 *  카카오 로그인을 위한 ( 세션 관리 )
 *  1 - 생성자
 *  2 - 세션 생성되었을 때
 *  3 - 세션 생성에 실패했을 때
 */


public class SessionCallback implements ISessionCallback {
    Activity mActivity;

    // 1 - 생성자
    public SessionCallback(Activity mActivity) {
        this.mActivity = mActivity;
    }

    // 2 - 세션 생성되었을 때
    @Override
    public void onSessionOpened() {
        UserManagement.getInstance().me(new MeV2ResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                int result = errorResult.getErrorCode();

                if (result == ApiErrorCode.CLIENT_ERROR_CODE) {
                    Toast.makeText(mActivity, "네트워크 연결이 불안정합니다.\n 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                    mActivity.finish();
                } else {
                    Toast.makeText(mActivity, "로그인 도중 오류가 발생했습니다: " + errorResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Toast.makeText(mActivity, "세션이 닫혔습니다.\n 다시 시도해 주세요: " + errorResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSuccess(MeV2Response result) {
                String needsScopeAutority = ""; // 정보 제공이 허용되지 않은 항목의 이름을 저장하는 변수

                // 이메일, 성별, 연령대, 생일 정보를 제공하는 것에 동의했는지 체크
                if (result.getKakaoAccount().needsScopeAccountEmail()) {
                    needsScopeAutority = needsScopeAutority + "이메일";
                }
                if (result.getKakaoAccount().needsScopeGender()) {
                    needsScopeAutority = needsScopeAutority + ", 성별";
                }
                if (result.getKakaoAccount().needsScopeAgeRange()) {
                    needsScopeAutority = needsScopeAutority + ", 연령대";
                }
                if (result.getKakaoAccount().needsScopeBirthday()) {
                    needsScopeAutority = needsScopeAutority + ", 생일";
                }

                if (needsScopeAutority.length() != 0) { // 정보 제공이 허용되지 않은 항목이 있다면 -> 허용되지 않은 항목을 안내하고 회원탈퇴 처리
                    if (needsScopeAutority.charAt(0) == ',') {
                        needsScopeAutority = needsScopeAutority.substring(2);
                    }
                    Toast.makeText(mActivity, needsScopeAutority + "에 대한 권한이 허용되지 않았습니다.\n 개인정보 제공에 동의해주세요.", Toast.LENGTH_SHORT).show(); // 개인정보 제공에 동의해달라는 Toast 메세지 띄움

                    // 회원탈퇴 처리
                    UserManagement.getInstance().requestUnlink(new UnLinkResponseCallback() {
                        @Override
                        public void onFailure(ErrorResult errorResult) {
                            int result = errorResult.getErrorCode();

                            if (result == ApiErrorCode.CLIENT_ERROR_CODE) {
                                Toast.makeText(mActivity, "네트워크 연결이 불안정합니다.\n 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(mActivity, "오류가 발생했습니다.\n 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onSessionClosed(ErrorResult errorResult) {
                            Toast.makeText(mActivity, "로그인 세션이 닫혔습니다.\n 다시 로그인해 주세요.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onNotSignedUp() {
                            Toast.makeText(mActivity, "가입되지 않은 계정입니다.\n 다시 로그인해 주세요.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onSuccess(Long result) {
                        }
                    });
                } else { // 모든 항목에 동의했다면 -> 유저 정보를 가져와서 MainActivity에 전달하고 MainActivity 실행.
                    try { // 카카오서버에서 받은 파일을 db에 저장한다. 프로필의 경우 여기에서만 user_password에 저장하여 보낸다.
                        String email, name, sex, profile;
                        URL url = new URL(APIManager.KaKaoMember_URL);

                        if (result.getKakaoAccount().hasEmail() == OptionalBoolean.TRUE)
                            email = result.getKakaoAccount().getEmail();
                        else
                            email = null;

                        name = result.getNickname();

                        if (result.getKakaoAccount().hasGender() == OptionalBoolean.TRUE)
                            sex = result.getKakaoAccount().getGender().toString();
                        else
                            sex = null;

                        profile = result.getProfileImagePath();


                        InterfaceManager task = new InterfaceManager(url);
                        String json = JsonMaker.jsonObjectMaker(email, "", name, "", "", sex, "", profile, "");
                        String retruns = task.execute(json).get();// 9
                        int userNO = Integer.parseInt(retruns);
                        if (userNO != 0) {
                            Log.i("이미 저장된 데이터입니다. : ", retruns);
                            PreferenceManager.setString(LoginActivity.mContext, "userNO", Integer.toString(userNO));
                        } else {
                            Log.i("카카오 정보 저장 성공 : ", "성공적으로 저장하였습니다.");
                        }
                        PreferenceManager.setInt(LoginActivity.mContext, "kakaoCheck", 1);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(mActivity, MainActivity.class);

                    mActivity.startActivity(intent);
                    mActivity.finish();
                }
            }
        });
    }

    // 3 - 세션 생성에 실패했을 때
    @Override
    public void onSessionOpenFailed(KakaoException e) {
    }
}