package com.example.hm_project.Command;

import android.app.Activity;
import android.content.Intent;

import com.example.hm_project.etc.PopupActivity;

/***
 *  팝업 리스너
 *  1 - 액티비티 이동
 *  2 - 팝업 메서드 ( 단순 )
 *  3 - 팝업 메서드 ( 리턴 )
 *  4 - 팝업 메서드 ( 네트워크 관련 오류 팝업 )
 */

public class PopupListener {

    // 1 - 액티비티 이동
    public void moveActivity(Activity mActivity, Class activityClass) {
        Intent intent = new Intent(mActivity, activityClass);
        mActivity.startActivity(intent);
        mActivity.finish();
    }

    //  2 - 팝업 메서드 ( 단순 )
    public void popupEvent(Activity mActivity, String title, String data) {
        Intent intent = new Intent(mActivity, PopupActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("data", data);
        mActivity.startActivity(intent);

    }

    // 3 - 팝업 메서드 ( 리턴 )
    public void popupEventReturn(Activity mActivity, String title, String data) {
        Intent intent = new Intent(mActivity, PopupActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("data", data);
        mActivity.startActivityForResult(intent, 1);
    }

    // 4 - 팝업 메서드 ( 네트워크 관련 오류 팝업 )
    public void viewPopup(Activity mActivity, int CODE) {
        Intent intent = new Intent(mActivity, PopupActivity.class);
        intent.putExtra("code", CODE);
        mActivity.startActivity(intent);
    }
}
