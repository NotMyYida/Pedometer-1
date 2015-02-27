package com.example.pedometer;

import android.util.Log;

public class distanceNotifier implements stepListener {

	public interface Listener {
		public void valueChanged(double value);

		public void passValue();
	}

	private Listener mListener;
	// Ĭ�ϲ���
	double mDistance = 0;
	double mStepLength;

	// ���캯��
	public distanceNotifier(Listener listener, double stepLength) {
		mListener = listener;
		mStepLength = stepLength;
		reloadSettings();
	}

	@Override
	public void onStep() {
		// TODO Auto-generated method stub
		// ���߾�������һ������
		if (pedometer.pedometerstatus) {
			mDistance += mStepLength / 100.0;
			Log.d("distanceNotifier.onStep", "distance = " + mDistance);
			notifyListener();
		}
	}

	private void notifyListener() {
		// TODO Auto-generated method stub
		mListener.valueChanged(mDistance);
	}

	public void setDistance(float distance) {
		// ���������Ѿ����ߵľ���
		mDistance = distance;
		notifyListener();
	}

	@Override
	public void passValue() {
		// TODO Auto-generated method stub

	}

	// ���¼������ݵ�stepService��
	public void reloadSettings() {
		notifyListener();
	}

}
