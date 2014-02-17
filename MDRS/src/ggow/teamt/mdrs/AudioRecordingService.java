package ggow.teamt.mdrs;

import java.io.File;
import java.io.IOException;

import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

public class AudioRecordingService extends IntentService implements SurfaceHolder.Callback {

	private static MediaRecorder mRecorder;
	private static String folderTime;
	public static String AudioPath;
	private static WindowManager windowManager;
	private static SurfaceView surfaceView;
	private static final String LOG_TAG = "MDRS - Background Audio Recording";

	/**
	 * A constructor is required, and must call the super IntentService(String)
	 * constructor with a name for the worker thread.
	 */
	public AudioRecordingService() {
		super("AudioRecordingService");
	}

	/**
	 * The IntentService calls this method from the default worker thread with
	 * the intent that started the service. When this method returns, IntentService
	 * stops the service, as appropriate.
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		//TODO This is where the work is done. I think
		//Audio Recording setup
		folderTime = String.valueOf(System.currentTimeMillis());
		AudioPath = folderTime + "/audio.3gp";
		Log.v(LOG_TAG, "path before prep is: " + AudioPath);
		PathPrep(AudioPath);
		Log.v(LOG_TAG, "path after prep is: " + AudioPath);
		try {
			directoryCheck();
		} catch (IOException e) {
			Log.e("LOG_TAG", "Dir check fail in service.");
			e.printStackTrace();
		}

		//FIXME pretty sure this isn't needed anymore
		//try {
		//	surfaceCreated(); //TODO change to surfaceCreated
		//} catch (IOException e) {
		//	e.printStackTrace();
		//}	

		//Background recording
		Notification notification = new Notification.Builder(this)
		.setContentTitle("MDRS Recording...")
		.setContentText("")
		.setSmallIcon(R.drawable.ic_launcher)
		.build();
		startForeground(1234, notification);

		windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		surfaceView = new SurfaceView(this);
		LayoutParams layoutParams = new WindowManager.LayoutParams(  //Don't know what to set this as
				1,1,
				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
				PixelFormat.TRANSLUCENT);
		layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
		windowManager.addView(surfaceView, layoutParams);
		surfaceView.getHolder().addCallback(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, " recording service starting", Toast.LENGTH_SHORT).show();
		return super.onStartCommand(intent,flags,startId);
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {

		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		//mRecorder.setAudioEncodingBitRate(16);  //TODO Enable these if want to improve
		//mRecorder.setAudioSamplingRate(44100);  //audio quality
		mRecorder.setOutputFile(AudioPath);
		//TODO add in higher sampling and encoding rates

		try {
			mRecorder.prepare();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() for recording failed");
		}
		mRecorder.start();
	}

	@Override
	public void onDestroy() {
		mRecorder.stop();
		mRecorder.reset();
		mRecorder.release();

		windowManager.removeView(surfaceView);
		Toast.makeText(this, "Service done", Toast.LENGTH_LONG).show();
	}

	public void PathPrep(String path) {
		AudioPath = sanitisePath(path);
	}

	private String sanitisePath(String path) {
		if(!path.startsWith("/")){
			path = "/" + path;
		}
		if (!path.contains(".")) {
			path += ".3gp";
		}
		return Environment.getExternalStorageDirectory().getAbsolutePath() + path;
	}

	public boolean directoryCheck() throws IOException {
		String state = android.os.Environment.getExternalStorageState();
		if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
			throw new IOException("SD Card is causing issues");
		}

		File directory = new File(AudioPath).getParentFile();
		if(!directory.exists() && !directory.mkdirs()) {
			throw new IOException("Path to file could not be created");
		}
		return true;
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder){}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
