package com.example.pedometer;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class calorieNotifier implements stepListener {

	public interface Listener {
		public void valueChanged(double value);

		public void passValue();
	}

	private Listener mListener;

	// �������Ĳ���
	private static double METRIC_RUNNING_FACTOR = 1.02784823;
	private static double METRIC_WALKING_FACTOR = 0.708;

	// Ĭ�ϲ�����ֵ
	private double mCalories = 0.0f;
	boolean mIsRunning;
	double mStepLength;
	double mBodyWeight;

	public calorieNotifier(Listener listener, double bodyweight,
			double steplength, boolean isRunning) {
		mListener = listener;
		mIsRunning = isRunning;
		mBodyWeight = bodyweight;
		mStepLength = steplength;
		// ���¼�������
		reloadSettings();
	}

	@Override
	public void onStep() {
		// TODO Auto-generated method stub
		// ���� (cal)= ����(Kg) * ����(m) * METRIC_RUNNING_FACTOR
		if (pedometer.pedometerstatus) {
			mCalories += (mBodyWeight * (mIsRunning ? METRIC_RUNNING_FACTOR
					: METRIC_WALKING_FACTOR))
			// Distance:
					* mStepLength / 100.0;// centimeters
			Log.d("CalorieNotifier.onStep", "mCalories = " + mCalories);
			// centimeters/kilometer
			notifyListener();
		}
	}

	@Override
	public void passValue() {
		// TODO Auto-generated method stub

	}

	private void notifyListener() {
		// ���͸�����ֵ����Ϣ����������
		mListener.valueChanged(mCalories);
	}

	public void reloadSettings() {
		notifyListener();
	}

	// ������ֵ
	public void resetValues() {
		mCalories = 0;
		notifyListener();
	}

	// ���ò���
	public void setStepLength(float stepLength) {
		mStepLength = stepLength;
	}

	// �������ĵ�����ֵ
	public void setCalories(float calories) {
		mCalories = calories;
		notifyListener();
	}

}
