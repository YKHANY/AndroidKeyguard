package com.android.internal.policy.impl;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.android.internal.R;
public class AddressDialog  extends Dialog{

	private final static String TAG ="AddressDialog";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("AddressDialog","onCreate");
		super.onCreate(savedInstanceState);
		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();   
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.8f;
		getWindow().setAttributes(lpWindow);
		getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);


		//   setTitle(mTitle);
		setContentView(R.layout.address_dialog);
		setLayout();


		setClickListener(mLeftClickListener , mRightClickListener);
	}

	public AddressDialog(Context context) {
		// Dialog 배경을 투명 처리 해준다.
		super(context , android.R.style.Theme_Translucent_NoTitleBar);
		final Context mContext = context;
		mLeftClickListener = new View.OnClickListener() {
			// conn button
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d(TAG,"addrEt :"+addrEt.getText());
				Log.d(TAG,"addrEt :"+timeEt.getText());
				String data[] = split_IP_Port(addrEt.getText().toString());
				if(data != null){
					Intent ipIntent = new Intent("org.secmem.intent.SELECT_CONN");
					ipIntent.putExtra("ip", data[0]);
					ipIntent.putExtra("port", data[1]);
					
					if(timeEt.getText().toString().equals("")){
						ipIntent.putExtra("time","600");
					}else{
						ipIntent.putExtra("time",timeEt.getText().toString());
					}
					mContext.sendBroadcast(ipIntent);
				}
				
				dismiss();

			}
		};
		mRightClickListener = new View.OnClickListener() {
			// cancel button
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				dismiss();

			}
		};
	}

	public AddressDialog(Context context , String title ,
			View.OnClickListener singleListener) {
		super(context , android.R.style.Theme_Translucent_NoTitleBar);
		this.mTitle = title;
		this.mLeftClickListener = singleListener;
	}

	public AddressDialog(Context context , String title , 
			View.OnClickListener leftListener , View.OnClickListener rightListener) {
		super(context , android.R.style.Theme_Translucent_NoTitleBar);
		this.mTitle = title;
		this.mLeftClickListener = leftListener;
		this.mRightClickListener = rightListener;
	}

	/*  private void setTitle(String title){
        mTitleView.setText(title);
    }*/

	protected void setClickListener(View.OnClickListener left , View.OnClickListener right){


		if(left!=null && right!=null){
			Log.d("AddressDialog","if");
			mLeftButton.setOnClickListener(left);
			mRightButton.setOnClickListener(right);
		}else if(left!=null && right==null){
			mLeftButton.setOnClickListener(left);
		}else {

		}
	}

	protected EditText addrEt;
	protected EditText timeEt;
	//  private TextView mTitleView;
	private Button mLeftButton;
	private Button mRightButton;

	private String mTitle;

	private View.OnClickListener mLeftClickListener;
	private View.OnClickListener mRightClickListener;

	/*
	 * Layout
	 */
	private void setLayout(){
		Log.d("AddressDialog","setLayout");
		Log.d("AddressDialog","tlqkf");
		addrEt = (EditText) findViewById(R.id.addrEt);
		timeEt = (EditText) findViewById(R.id.timeEt);
		mLeftButton = (Button) findViewById(R.id.bt_left);
		Log.d("AddressDialog",""+mLeftButton);
		mRightButton = (Button) findViewById(R.id.bt_right);
		Log.d("AddressDialog",""+mRightButton);
	}

	private String[] split_IP_Port(String data){
		//result[0] : ip , result[1] : port
		String result[] = new String[2];
		int idx = data.indexOf(":");
		if(idx == -1){
			return null;
		}
		result[0] = data.substring(0, idx);
		result[1] = data.substring(idx+1);
		if(result[0].equals("") || result[1].equals("")){
			// return null if ip or port is empyt
			return null;
		}
		return result;
	}

}
