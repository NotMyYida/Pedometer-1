package com.example.pedometer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.example.pedometer.registerUI.registRunnable;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

public class InitialUI extends Activity {

	Button logIn;
	Button register;
	EditText password;
	AutoCompleteTextView username;
	ProgressDialog mProDialog;
	Vibrator mVibrator;
	Context ctx;
	CheckBox remember, autolog;
	static public String name, psw;
	private String responseMsg = "";
	public final String store = "userinfo";
	public final String state = "loginState";
	public final int mode = MODE_WORLD_READABLE + MODE_WORLD_WRITEABLE;
	private static String url = "http://104.131.156.81:8888/login";
	private static ArrayList<String> alluser = new ArrayList<String>();
	SharedPreferences ss;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.log_ui);
		ctx = this;
		mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		logIn = (Button) findViewById(R.id.log);
		register = (Button) findViewById(R.id.register);
		password = (EditText) findViewById(R.id.password_in);
		username = (AutoCompleteTextView) findViewById(R.id.username_in);
		remember = (CheckBox) findViewById(R.id.remember);
		autolog = (CheckBox) findViewById(R.id.autolog);
		ss = getSharedPreferences(state, mode);
		if (ss.getBoolean("isRemember", false)) {
			remember.setChecked(true);
			username.setText(ss.getString("username", ""));
			password.setText(ss.getString("password", ""));
			if (ss.getBoolean("isAutolog", false)) {
				name = username.getText().toString();
				psw = password.getText().toString();
				new Thread(new logRunnable(url, name, psw)).start();
			}
		}
		Map<String, ?> map = getSharedPreferences(store, mode).getAll();
		if (!map.isEmpty()) {

			// �����Զ���ʾ����
			for (Map.Entry<String, ?> entry : map.entrySet()) {
				alluser.add(entry.getKey());
			}
			ArrayAdapter<String> autoadapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_dropdown_item_1line, alluser);
			// �����Զ���ʾ
			username.setAdapter(autoadapter);
			username.setThreshold(1);
		}
		// ���ü�����
		logIn.setOnClickListener(logListener);
		register.setOnClickListener(registerListener);

		remember.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				Editor ee = ss.edit();
				ee.putBoolean("isRemember", isChecked);
				ee.commit();
			}
		});

		autolog.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				Editor ee = ss.edit();
				ee.putBoolean("isAutolog", isChecked);
				ee.commit();
			}
		});
	}

	OnClickListener logListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			name = username.getText().toString();
			if (!IsUsernameValid(name)) {
				AlertDialog.Builder mBuilder = new AlertDialog.Builder(ctx);
				mBuilder.setTitle("������Ϸ����û���!!!");
				mBuilder.setNeutralButton("ȷ��",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
						});
				username.setText("");
			} else {
				// connect database and validate user information
				psw = password.getText().toString();
				mProDialog = ProgressDialog.show(ctx, "", "���Ժ�", true);
				new Thread(new logRunnable(url, name, psw)).start();

			}
		}
	};

	OnClickListener registerListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(ctx, registerUI.class);
			// ��ת��ע�����
			startActivity(intent);
		}
	};

	Boolean IsUsernameValid(String username) {
		if (username.equals(""))
			return false;
		int len = username.length();
		for (int i = 0; i < len; i++) {
			char ch = username.charAt(i);
			if (Character.isLetter(ch))
				continue;
			if (Character.isDigit(ch))
				continue;
			return false;
		}
		return true;
	}

	String LoginValidate(String url, String username, String password) {
		// ��½

		HttpPost request = new HttpPost(url);
		// String responseMsg;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("username", username));
		params.add(new BasicNameValuePair("password", password));
		try {
			// �������������
			request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpClient client = getHttpClient();
			// ִ�����󷵻���Ӧ
			HttpResponse response = client.execute(request);

			// �ж��Ƿ�����ɹ�
			if (response.getStatusLine().getStatusCode() == 200) {
				// �����Ӧ��Ϣ
				responseMsg = EntityUtils.toString(response.getEntity());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return responseMsg;
	}

	// ��ʼ��httpClient������������ʱ�����ݳ�ʱʱ��
	private HttpClient getHttpClient() {
		// TODO Auto-generated method stub
		BasicHttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 5 * 1000);
		HttpConnectionParams.setSoTimeout(httpParams, 10 * 1000);
		HttpClient client = new DefaultHttpClient(httpParams);
		return client;
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			String res = msg.getData().getString("log");
			mProDialog.dismiss();
			if (res.equals("login successful")) {
				// ��ת������������
				// �����û�������
				SharedPreferences mSP = getSharedPreferences(store, mode);
				// �����û��ĵ�½״̬���Ƿ��ס���룬 �Ƿ��Զ���¼��
				SharedPreferences sp = getSharedPreferences(state, mode);
				Editor ee = mSP.edit();
				if (!mSP.contains(name)) {
					ee.putString(name, psw);
					ee.commit();
				}
				Editor ee2 = sp.edit();
				ee2.putString("username", name);
				ee2.putString("password", psw);
				ee2.commit();
				Intent intent = new Intent(ctx, Tab.class);
				startActivity(intent);
			} else if (res.equals("user doesn't exist")) {
				Toast.makeText(ctx, "�û��������ڣ����������룡��", Toast.LENGTH_LONG).show();
				// �ֻ���
				mVibrator.vibrate(new long[] { 500, 200, 500, 200 }, -1);
				username.setText("");
				password.setText("");
			} else if (res.equals("wrong password")) {
				Toast.makeText(ctx, "�������", Toast.LENGTH_LONG);
				// �ֻ���
				mVibrator.vibrate(new long[] { 500, 200, 500, 200 }, -1);
				password.setText("");
				Toast.makeText(ctx, "�������", Toast.LENGTH_LONG);
			}

		}

	};

	public class logRunnable implements Runnable {
		String url;
		String name;
		String psw;

		public logRunnable(String url, String name, String psw) {
			this.name = name;
			this.psw = psw;
			this.url = url;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String res = LoginValidate(url, name, psw);
			Bundle b = new Bundle();
			Message msg = new Message();
			b.putString("log", res);
			msg.setData(b);
			mHandler.sendMessage(msg);
		}

	}
}
