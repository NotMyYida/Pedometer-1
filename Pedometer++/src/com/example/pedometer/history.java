package com.example.pedometer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

public class history extends Activity {
	GraphicalView bargraph;
	GraphicalView piegraph;
	LinearLayout layputForbar;
	LinearLayout layputForpie;
	Handler handler;
	Context contForView;
	int[] dataforChart = new int[7];
	double[] dataforRate = new double[2];
	private String database_url = "http://104.131.156.81:8888/inquire_run_information";

	/* added from demo */

	// ��״ͼ
	protected CategorySeries buildCategoryDataset(String title, double[] values) {
		CategorySeries series = new CategorySeries(title);
		series.add("Done", values[0]);
		series.add("Remain", values[1]);
		return series;
	}

	protected DefaultRenderer buildCategoryRenderer(int[] colors) {
		DefaultRenderer renderer = new DefaultRenderer();

		renderer.setLegendTextSize(20);// �������½Ǳ�ע�����ִ�С
		// renderer.setZoomButtonsVisible(true);//������ʾ�Ŵ���С��ť
		renderer.setZoomEnabled(false);// ���ò�����Ŵ���С.
		renderer.setChartTitleTextSize(30);// ����ͼ���������ִ�С
		renderer.setChartTitle("");// ����ͼ��ı��� Ĭ���Ǿ��ж�����ʾ
		renderer.setLabelsTextSize(20);// ��ͼ�ϱ�����ֵ������С
		// renderer.setLabelsColor(Color.WHITE);//��ͼ�ϱ�����ֵ���ɫ
		renderer.setPanEnabled(false);// �����Ƿ����ƽ��
		renderer.setDisplayValues(true);// �Ƿ���ʾֵ
		renderer.setClickEnabled(false);// �����Ƿ���Ա����
		renderer.setMargins(new int[] { 20, 30, 15, 0 });
		// margins - an array containing the margin size values, in this order:
		// top, left, bottom, right
		for (int color : colors) {
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setColor(color);
			renderer.addSeriesRenderer(r);
		}
		return renderer;
	}

	// ��״ͼ
	private XYMultipleSeriesDataset getBarDemoDataset(int[] steps) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		// final int nr = 10;
		// Random r = new Random();
		// for (int i = 0; i < 2; i++) {
		CategorySeries series = new CategorySeries("Steps by day");
		for (int step : steps)
			series.add(step);
		dataset.addSeries(series.toXYSeries());
		// }
		return dataset;
	}

	public XYMultipleSeriesRenderer getBarDemoRenderer() {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		renderer.setAxisTitleTextSize(16);
		renderer.setChartTitleTextSize(20);
		renderer.setLabelsTextSize(15);
		renderer.setLegendTextSize(15);
		renderer.setMargins(new int[] { 10, 30, 15, 10 });
		SimpleSeriesRenderer r = new SimpleSeriesRenderer();
		r.setColor(0xff00f060);
		renderer.addSeriesRenderer(r);
		// r = new SimpleSeriesRenderer();
		// r.setColor(Color.GREEN);
		// renderer.addSeriesRenderer(r);
		return renderer;
	}

	private void setChartSettings(XYMultipleSeriesRenderer renderer) {
		renderer.setChartTitle("");
		renderer.setXTitle("");
		renderer.setYTitle("");
		renderer.setXLabels(0);// ����x���ϵ��±�����
		// renderer.setYLabels(5);// ����y���ϵ��±�����
		renderer.setPanEnabled(false, false);// �����Ƿ�����ƽ��
		renderer.setBarSpacing(1f);// ������״�ļ��
		renderer.setYLabelsAlign(Align.RIGHT);// y�� ���ֱ�ʾ�����껹���ұ�
		renderer.addXTextLabel(2.0, "Mon");// ��ָ�����괦��ʾ����
		renderer.addXTextLabel(3.0, "Tue");// ��ָ�����괦��ʾ����
		renderer.addXTextLabel(4.0, "Wed");// ��ָ�����괦��ʾ����
		renderer.addXTextLabel(5.0, "Thu");// ��ָ�����괦��ʾ����
		renderer.addXTextLabel(6.0, "Fri");// ��ָ�����괦��ʾ����
		renderer.addXTextLabel(7.0, "Sat");// ��ָ�����괦��ʾ����
		renderer.addXTextLabel(1.0, "Sun");// ��ָ�����괦��ʾ����
		renderer.setLabelsTextSize(15);// ���������������ֵĴ�С
		renderer.setXLabelsColor(0xff00f060);// ����X�������ǩ��ɫ
		renderer.setApplyBackgroundColor(true);// ����
		renderer.setBackgroundColor(Color.TRANSPARENT);// ���ñ�����ɫ
		renderer.setMarginsColor(Color.WHITE);// ���ÿհ�����ɫ
		renderer.setDisplayChartValues(true);// �Ƿ���ʾֵ
		renderer.setXAxisMin(0.5);
		renderer.setXAxisMax(7.5);
		renderer.setYAxisMin(0);
		renderer.setYAxisMax(5400);
		renderer.setFitLegend(true);// �������ʵ�λ��
	}

	/* added from demo */

	private int[] transformDataFromDB(int[] d) {
		int[] res = new int[7];
		Date today = new Date();
		Calendar ca = Calendar.getInstance();
		ca.setTime(today);
		int dayOfWeek = ca.get(Calendar.DAY_OF_WEEK);
		for (int i = dayOfWeek - 1; i >= 0; i--) {
			res[i] = d[7 - (dayOfWeek - i)];
			dataforRate[0] += (double) res[i];
		}
		for (int i = dayOfWeek; i < 7; i++) {
			res[i] = 0;
		}
		return res;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);

		layputForbar = (LinearLayout) findViewById(R.id.linear2);
		layputForpie = (LinearLayout) findViewById(R.id.linear1);
		handler = new Handler();
		// start pie

		/*
		 * int[] colors = { 0xff0060f0, 0xfff00060 }; DefaultRenderer render =
		 * buildCategoryRenderer(colors);
		 * 
		 * double[] values = { 50, 50 }; CategorySeries dataset =
		 * buildCategoryDataset("Test", values);
		 * 
		 * piegraph = ChartFactory.getPieChartView(this, dataset, render);
		 * 
		 * layputForpie.removeAllViews();
		 * layputForpie.setBackgroundColor(Color.WHITE);
		 * layputForpie.addView(piegraph);
		 */

		// start bar

		/*
		 * XYMultipleSeriesRenderer render2 = getBarDemoRenderer();
		 * setChartSettings(render2); int[] stp = { 2008, 3098, 866, 1888, 0,
		 * 3662, 678 }; XYMultipleSeriesDataset dataset2 =
		 * getBarDemoDataset(stp); bargraph = ChartFactory.getBarChartView(this,
		 * dataset2, render2, Type.DEFAULT); layputForbar.removeAllViews();
		 * layputForbar.setBackgroundColor(Color.WHITE);
		 * layputForbar.addView(bargraph);
		 */

		// �������߳�
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpPost httpPost = new HttpPost(database_url);
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("username", InitialUI.name));
				// username temp
				try {
					httpPost.setEntity(new UrlEncodedFormEntity(params,
							HTTP.UTF_8));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				DefaultHttpClient httpClient = new DefaultHttpClient();
				try {
					HttpResponse response = httpClient.execute(httpPost);
					HttpEntity result = response.getEntity();
					String reqData = EntityUtils.toString(result);
					String res = reqData.substring(1, reqData.length() - 1);
					String a[] = res.split("\\, ");
					Log.d("rrrrrrrrrr", reqData);
					
					dataforRate[0] = 0;
					int[] tmp = new int[7];
					for (int i = 0; i < 7; i++) {
						Log.d(a[i], "aaaaaaaa");
						tmp[i] = Integer.parseInt(a[i]);
						// dataforChart[i] = Integer.parseInt(a[i]);
						// dataforRate[0] += (double) dataforChart[i];
					}
					dataforChart = transformDataFromDB(tmp);
					dataforRate[1] = (double) Integer.parseInt(a[7]);
					dataforRate[1] -= dataforRate[0];
				} catch (ClientProtocolException ee) {
					ee.printStackTrace();
					Log.d("error1", "bbbbbbbb");
				} catch (ParseException e) {
					e.printStackTrace();
					Log.d("error1", "ccccccc");
				} catch (IOException e) {
					e.printStackTrace();
					Log.d("error1", "ddddddd");
				}
				
				
				handler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						int[] colors = { 0xff0060f0, 0xfff00060 };
						DefaultRenderer render = buildCategoryRenderer(colors);

						// double[] values = { 50, 50 };
						CategorySeries dataset = buildCategoryDataset("Test",
								dataforRate);
						piegraph = ChartFactory.getPieChartView(history.this,
								dataset, render);
						if (layputForpie != null) {
							layputForpie.removeAllViews();
							layputForpie.setBackgroundColor(Color.WHITE);
							layputForpie.addView(piegraph);
						}
					}
				});
				handler.post(new Runnable() {

					@Override
					public void run() {
						XYMultipleSeriesRenderer render2 = getBarDemoRenderer();
						setChartSettings(render2);
						// int[] stp = { 2008, 3098, 866, 1888, 0, 3662, 678 };
						XYMultipleSeriesDataset dataset2 = getBarDemoDataset(dataforChart);
						bargraph = ChartFactory.getBarChartView(history.this,
								dataset2, render2, Type.DEFAULT);
						if (layputForbar != null) {
							layputForbar.removeAllViews();
							layputForbar.setBackgroundColor(Color.WHITE);
							layputForbar.addView(bargraph);
						}
					}
				});
			}
		}).start();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
