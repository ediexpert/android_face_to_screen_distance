package com.tec.fontsize;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.FloatMath;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.tec.fontsize.messages.MeasurementStepMessage;
import com.tec.fontsize.messages.MessageHUB;
import com.tec.fontsize.messages.MessageListener;
import com.tec.fontsize.utils.MyService;

public class MainActivity extends Activity implements MessageListener {

	private PowerManager powerManager;
	private PowerManager.WakeLock wakeLock;

	public static final String CAM_SIZE_WIDTH = "intent_cam_size_width";
	public static final String CAM_SIZE_HEIGHT = "intent_cam_size_height";
	public static final String AVG_NUM = "intent_avg_num";
	public static final String PROBANT_NAME = "intent_probant_name";
	private float screen_to_face_dist;

	private CameraSurfaceView _mySurfaceView;
	Camera _cam;

	private final static DecimalFormat _decimalFormater = new DecimalFormat(
			"0.0");

	private float _currentDevicePosition;

	private int _cameraHeight;
	private int _cameraWidth;
	private int _avgNum;

	TextView _currentDistanceView;
	Button _calibrateButton;
	SurfaceHolder surfaceHolder;
	Button _button;
	TextView _checkDistance;
	private  int count=0;
	/**
	 * Abusing the media controls to create a remote control
	 */
	// ComponentName _headSetButtonReceiver;
	// AudioManager _audioManager;


	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}




	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.measurement_activity);
		_mySurfaceView = (CameraSurfaceView) findViewById(R.id.surface_camera);

		RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(
				(int) (0.95 * this.getResources().getDisplayMetrics().widthPixels),
				(int) (0.6 * this.getResources().getDisplayMetrics().heightPixels));

		layout.setMargins(0, (int) (0.05 * this.getResources().getDisplayMetrics().heightPixels), 0, 0);

		_mySurfaceView.setLayoutParams(layout);
		_currentDistanceView = (TextView) findViewById(R.id.currentDistance);
		_checkDistance = (TextView) findViewById(R.id.textView2);

		_calibrateButton = (Button) findViewById(R.id.calibrateButton);

		onClickActionListner();


			Intent intetn = new Intent(this, MyService.class);
			startService(intetn);


		SharedPreferences mPrefs = getSharedPreferences("label",0);
//		String mstr = sp.getString("tag","20");
		SharedPreferences.Editor medit = mPrefs.edit();
		medit.putFloat("dist",20.0f).commit();
		_checkDistance.setText(Float.toString(mPrefs.getFloat("dist",26.0f)));
//		Float fl = mPrefs.getFloat("dist",25.0f);

		// _audioManager = (AudioManager) this
		// .getSystemService(Context.AUDIO_SERVICE);
	}
	// send to setting activity

	public void setDistance(int dist){
		screen_to_face_dist = (float)dist;
	}
	public void setCheckDistance(int n ){
		_checkDistance.setText(Integer.toString(n));
	}
	public float getDistance(){
		return screen_to_face_dist;
	}
	public void onClickActionListner(){
		Log.d("ButtonClick","you clcked");
		Toast.makeText(this,"clicked",Toast.LENGTH_SHORT).show();
		Button btn;
		btn = (Button)findViewById(R.id.button);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent("com.tec.fontsize.MainPageActivity");
				startActivity(intent);
			}
		});
	}
	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences mPrefs = getSharedPreferences("label",0);
		_checkDistance.setText(Float.toString(mPrefs.getFloat("dist",27.0f)));
		MessageHUB.get().registerListener(this);
		// _audioManager.registerMediaButtonEventReceiver(_headSetButtonReceiver);

		// 1 for front cam. No front cam ? Not my fault!
		_cam = Camera.open(1);
		Camera.Parameters param = _cam.getParameters();

		// Find the best suitable camera picture size for your device. Competent
		// research has shown that a smaller size gets better results up to a
		// certain point.
		// http://ieeexplore.ieee.org/xpl/login.jsp?tp=&arnumber=6825217&url=http%3A%2F%2Fieeexplore.ieee.org%2Fiel7%2F6816619%2F6825201%2F06825217.pdf%3Farnumber%3D6825217
		List<Size> pSize = param.getSupportedPictureSizes();
		double deviceRatio = (double) this.getResources().getDisplayMetrics().widthPixels
				/ (double) this.getResources().getDisplayMetrics().heightPixels;

		Size bestSize = pSize.get(0);
		double bestRation = (double) bestSize.width / (double) bestSize.height;

		for (Size size : pSize) {
			double sizeRatio = (double) size.width / (double) size.height;

			if (Math.abs(deviceRatio - bestRation) > Math.abs(deviceRatio
					- sizeRatio)) {
				bestSize = size;
				bestRation = sizeRatio;
			}
		}
		_cameraHeight = bestSize.height;
		_cameraWidth = bestSize.width;

		Log.d("PInfo", _cameraWidth + " x " + _cameraHeight);

		param.setPreviewSize(_cameraWidth, _cameraHeight);
		_cam.setParameters(param);

		_mySurfaceView.setCamera(_cam);


//		registerReceiver(onBroadcast, new IntentFilter("mymessage"));
	}

	@Override
	protected void onPause() {
		super.onPause();

		MessageHUB.get().unregisterListener(this);

		// _audioManager
		// .unregisterMediaButtonEventReceiver(_headSetButtonReceiver);

		resetCam();
//		unregisterReceiver(onBroadcast);

	}

	/**
	 * Sets the current eye distance to the calibration point.
	 * 
	 * @param v
	 */
	public void pressedCalibrate(final View v) {

		if (!_mySurfaceView.isCalibrated()) {

			_calibrateButton.setBackgroundResource(R.drawable.yellow_button);
			_mySurfaceView.calibrate();
		}
	}

	public void pressedReset(final View v) {

		if (_mySurfaceView.isCalibrated()) {

			_calibrateButton.setBackgroundResource(R.drawable.red_button);
			_mySurfaceView.reset();
		}
	}

	public void onShowMiddlePoint(final View view) {
		// Is the toggle on?
		boolean on = ((Switch) view).isChecked();
		_mySurfaceView.showMiddleEye(on);
	}

	public void onShowEyePoints(final View view) {
		// Is the toggle on?
		boolean on = ((Switch) view).isChecked();
		_mySurfaceView.showEyePoints(on);
	}


	public void updateUI(final MeasurementStepMessage message) {
		MyService myService = new MyService();

		String x= _decimalFormater.format(message.getDistToFace());
		Float tempF= Float.parseFloat(x);
		_currentDistanceView.setText(_decimalFormater.format(message
				.getDistToFace()) + " cm");
		SharedPreferences mPrefs = getSharedPreferences("label",0);

		_checkDistance.setText(Float.toString(mPrefs.getFloat("dist",28.0f)));

		float fontRatio = message.getDistToFace() / 29.7f;
		screen_to_face_dist = myService.getMinDistanceToCheck();
		if((tempF > 10.0f)&&(tempF <= mPrefs.getFloat("dist",29.0f))){
			count++;
			if(count > 5){
				LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
				View view = layoutInflater.inflate(R.layout.activity_black, null);
				WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
				WindowManager.LayoutParams params = new WindowManager.LayoutParams(
						WindowManager.LayoutParams.FILL_PARENT, 1200,
						WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
						WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
								| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);

				params.gravity = Gravity.CENTER;
				windowManager.addView(view, params);

				// END shut off the screen #######6

				// <!--######START create vibration
				Vibrator vibrator;
				vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				vibrator.vibrate(1000);
				// END create vibration ######--!>



			}else{
//				Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
//				vibrator.vibrate(1000);
//				Toast.makeText(this, "Your Distance is "+tempF,Toast.LENGTH_SHORT).show();


				// #####START create full screen dialog
				Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
				dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
				AlertDialog alertDialog = new AlertDialog.Builder(this)
						.setTitle("Eye Protection System")
						.setMessage("You are too close to screen")
						.create();
				alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
				alertDialog.show();


				//END full screen dialog#####
				// <!--######START create vibration
				Vibrator vibrator;
				vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				vibrator.vibrate(1000);
				// END create vibration ######--!>

			}


		}
		_currentDistanceView.setTextSize(fontRatio * 20);

	}

	private void resetCam() {
		_mySurfaceView.reset();

		_cam.stopPreview();
		_cam.setPreviewCallback(null);
		_cam.release();
	}

	@Override
	public void onMessage(final int messageID, final Object message) {

		switch (messageID) {

		case MessageHUB.MEASUREMENT_STEP:
			updateUI((MeasurementStepMessage) message);
			break;

		case MessageHUB.DONE_CALIBRATION:

			_calibrateButton.setBackgroundResource(R.drawable.green_button);

			break;
		default:
			break;
		}

	}


	///function which will run camera in background
	public boolean starMediaRecording(){
		Camera.Parameters params = _cam.getParameters();
		_cam.setParameters(params);
		Camera.Parameters p = _cam.getParameters();

		final List<Size> listSize = p.getSupportedPreviewSizes();
		Size mPreviewSize = listSize.get(2);
//		p.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
		p.setPreviewSize(100,100);
		p.setPreviewFormat(PixelFormat.YCbCr_420_SP);
		_cam.setParameters(p);

		try {
			_cam.setPreviewDisplay(surfaceHolder);

			_cam.startPreview();
		}
		catch (IOException e) {
//			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}

		_cam.unlock();


		return true;

	}




//	public void stopMediaRecorder() {
//		mServiceCamera.reconnect();
//
//		mMediaRecorder.stop();
//		mMediaRecorder.reset();
//
//		mServiceCamera.stopPreview();
//		mMediaRecorder.release();
//
//		mServiceCamera.release();
//		mServiceCamera = null;
//	}




//	private BroadcastReceiver onBroadcast = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context ctxt, Intent i) {
//			Toast.makeText(ctxt,"skasnsa",Toast.LENGTH_SHORT).show();
//		}
//	};



}
