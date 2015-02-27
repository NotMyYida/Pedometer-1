package com.example.pedometer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.GpsStatus.Listener;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class stepService extends Service {

	private static String url = "";
	private static String responseMsg = "";
	// ����ֵ�ı仯�ļ�����
	// private stepListener mStepListener;
	private static stepNotifier mStepNotifier;
	private static distanceNotifier mDistanceNotifier;
	private static calorieNotifier mCaloriesNotifier;

	private SensorManager mSensorManager;
	private PowerManager.WakeLock wakeLock;
	private NotificationManager mNM;

	// �˶�����
	static public double mDistance; // �ƶ�����
	static public double mCalories; // ���ĵ�����
	static public int mSteps; // ���ߵĲ���
	private double mStepLength = 70; // �û�����
	private double mWeight = 65; // �û�����
	private double mHeight = 175; // �û����
	private Boolean mMode = true;

	// �������¼�������
	private stepCounter mStepCounter;

	// ��ָ��sharedPreferenced�ļ��л�ȡ�˶�����
	private final String NAME = "pedometer_preferences";
	@SuppressWarnings("deprecation")
	public static int MODE = Context.MODE_WORLD_READABLE
			+ Context.MODE_WORLD_WRITEABLE;
	SharedPreferences mSharedPreferences;

	// ����Ļ���ȱ䰵ʱ
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) { // �жϽ��յ����¼�
				// Unregisters the listener and registers it again.

			}
		}
	};

	public class StepBinder extends Binder {
		stepService getService() {
			return stepService.this;
		}
	}

	// ������ӿ�
	public interface ICallback {
		public void distanceChanged(double value);

		public void stepChanged(int value);

		public void caloriesChanged(double value);
	}

	private ICallback mCallback;

	/**
	 * Receives messages from activity.
	 */
	private final IBinder mBinder = new StepBinder();

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		if(pedometer.clickTimes == 0)
			showNotification();
		mStepCounter = new stepCounter();
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mCallback = pedometer.mCallback;
		// ע�ᴫ����ʱ�������
		registSensorEvent();

		// ��̬ע��㲥������
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		registerReceiver(mReceiver, filter);

		// ��ȡ�����ļ�
		if (pedometer.config_way) {
			mSharedPreferences = getSharedPreferences(NAME + InitialUI.name,
					MODE);
		} else {
			mSharedPreferences = getSharedPreferences(NAME, MODE);
		}
		mHeight = Double.valueOf(mSharedPreferences.getString("userWeight",
				"175"));
		mWeight = Double.valueOf(mSharedPreferences.getString("userWeight",
				"60"));
		mStepLength = Double.valueOf(mSharedPreferences.getString(
				"userStepLength", "70.0"));
		mMode = Boolean.valueOf(mSharedPreferences.getString("pedometerMode",
				"true"));

		if (pedometer.clickTimes == 0) {
			pedometer.clickTimes++;
			// �����仯������
			mStepNotifier = new stepNotifier(stepListener);
			// ��ʼ������Ϊ0
			mStepNotifier.setStep(0);
			// ��Ϊ����������stepCounter
			mStepCounter.addListener(mStepNotifier);
			// ����仯������
			mDistanceNotifier = new distanceNotifier(mDistanceListener,
					mStepLength);
			// ��ʼ��Ϊ0
			mDistanceNotifier.setDistance(0.0f);
			// ���Ӽ�����
			mStepCounter.addListener(mDistanceNotifier);
			// �����仯������
			mCaloriesNotifier = new calorieNotifier(mCalorieListener, mWeight,
					mStepLength, mMode);
			// ��ʼ������ֵΪ0
			mCaloriesNotifier.setCalories(0.0f);
			// ���Ӽ�����
			mStepCounter.addListener(mCaloriesNotifier);
		}

	}

	// ��������ʱ�������ݱ��浽���ݿ���
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		// ע���㲥������
		unregisterReceiver(mReceiver);
		// ע�������������¼�
		unregistSensorEvent();

		mNM.cancel(R.string.app_name);

		// �����ݴ洢�����ݿ���
		// new Thread(new AccesServer(mDistance + "", mSteps + "", mCalories +
		// "", mSpeed + "")).start();

		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	public void registSensorEvent() {
		// ��ȡ���ٶȼ�
		Sensor mSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		// ʹ���¼�������mStepCounter�������ٶȼ�mSensor
		mSensorManager.registerListener(mStepCounter, mSensor,
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	public void unregistSensorEvent() {
		// ȡ���������¼��������Դ������ļ���
		mSensorManager.unregisterListener(mStepCounter);
	}

	// �ش���ֵ
	private stepNotifier.Listener stepListener = new stepNotifier.Listener() {

		@Override
		public void valueChanged(int value) {
			// TODO Auto-generated method stub
			
			mSteps = value;
			passValue();
		}

		@Override
		public void passValue() {
			// TODO Auto-generated method stub
			if (mCallback != null) {
				mCallback.stepChanged(mSteps);
			}
		}
	};

	// ��distanceNotifier��ȡ������ֵ������stepService
	// ����value���Ǵ�distanceNotifier���������
	private distanceNotifier.Listener mDistanceListener = new distanceNotifier.Listener() {
		public void valueChanged(double value) {
			mDistance = value;
			passValue();
		}

		public void passValue() {
			if (mCallback != null) {
				Log.d("stepService.passvalue", "this is");
				mCallback.distanceChanged(mDistance);
			}
		}
	};

	// ʵ�ֳ���ӿ�
	private calorieNotifier.Listener mCalorieListener = new calorieNotifier.Listener() {

		@Override
		public void valueChanged(double value) {
			// TODO Auto-generated method stub
			mCalories = value;
			passValue();
		}

		@Override
		public void passValue() {
			// TODO Auto-generated method stub
			Log.d("stepService.passvalue", "this is");
			if (mCallback != null)
				mCallback.caloriesChanged(mCalories); // ������ֵ
		}

	};

	// �û���������,�����˶�����ȫ������Ϊ0
	public static void resetValues() {
		mDistanceNotifier.setDistance(0);
		mCaloriesNotifier.setCalories(0.0f);
		mStepNotifier.setStep(0);
	}

	private void showNotification() {
		Log.d("stepService", "in the show notification");
		CharSequence text = getText(R.string.app_name);
		// ����֪ͨ��Ϣ����ͼ��
		@SuppressWarnings("deprecation")
		Notification notification = new Notification(R.drawable.icon_small, null,
				System.currentTimeMillis());
		notification.flags = Notification.FLAG_NO_CLEAR
				| Notification.FLAG_ONGOING_EVENT;
		Intent pedometerIntent = new Intent();
		pedometerIntent.setComponent(new ComponentName(this, pedometer.class));
		pedometerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				pedometerIntent, 0);
		// ����֪ͨ������
		notification.setLatestEventInfo(this, text,
				"enjoy the happiness of running", contentIntent);

		mNM.notify(R.string.app_name, notification);
	}

	public void reloadSettings() {

		if (mStepCounter != null) {
			mStepCounter.setSensitivity(Float.valueOf(10));
		}
		// ���¼�������
		// if (mStepDisplayer != null) mStepDisplayer.reloadSettings();
		if (mDistanceNotifier != null)
			mDistanceNotifier.reloadSettings();
		if (mCaloriesNotifier != null)
			mCaloriesNotifier.reloadSettings();
		// if (mSpeedNotifier != null) mSpeedNotifier.reloadSettings();
	}

	public void registerCallback(ICallback cb) {
		mCallback = cb;
		Log.d("in register callback", "mCallback");
		// mStepDisplayer.passValue();
		// mPaceListener.passValue();
	}

	public class AccesServer implements Runnable {
		String distance;
		String steps;
		String calories;
		String speed;

		public AccesServer(String distance, String steps, String calories,
				String speed) {
			this.distance = distance;
			this.steps = steps;
			this.calories = calories;
			this.speed = speed;
		}

		// �����μƲ�����Ϣ�������ݿ���
		@Override
		public void run() {
			// TODO Auto-generated method stub
			HttpPost request = new HttpPost(url);
			// String responseMsg;
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("distance", distance));
			params.add(new BasicNameValuePair("steps", steps));
			params.add(new BasicNameValuePair("calories", calories));
			params.add(new BasicNameValuePair("speed", speed));
			try {
				request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			HttpClient client = getHttpClient();
			try {
				HttpResponse response = client.execute(request);
				if (response.getStatusLine().getStatusCode() == 200) // ���ӳɹ�
				{
					// �����Ӧ��Ϣ
					responseMsg = EntityUtils.toString(response.getEntity());
				}
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// �ж��Ƿ�����ɹ�

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

	}
}
