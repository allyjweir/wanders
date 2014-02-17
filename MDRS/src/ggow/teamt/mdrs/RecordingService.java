package ggow.teamt.mdrs;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class RecordingService extends Service {

	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;

	private static MediaRecorder mRecorder;
	private static final String LOG_TAG = "MDRS - Recording Service";


	public final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		public void handleMessage(Message msg) {
			//Do work here such as download a file
			//WTF
		}
	}

	@Override
	public void onCreate() {
		//Start the thread running the service.
		HandlerThread thread = new HandlerThread("ServiceStart");
		thread.start();

		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "Service starting", Toast.LENGTH_SHORT).show();
		//For each start request, send a message to start a job and deliver
		//the start ID so we know which request we're stopping when we finish
		//the job.
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		mServiceHandler.sendMessage(msg);

		//Notification to keep in foreground
		NotificationCompat.Builder mBuilder = 
				new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentTitle("Recording")
		.setContentText("In Progress")
		.setOngoing(true);

		//Navigation Stack stuff
		//TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		//stackBuilder.addParentStack(arg0) TODO don't know what to put here

		//Audio recording starting
		startAudioRecording();
		return START_STICKY;
	}

	private void startAudioRecording() {
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setOutputFile(RecordingActivity.getAudioPath());
		//.setAudioEncodingBitRate(16);  //TODO Enable these if want to improve
		//.setAudioSamplingRate(44100);  //audio quality

		try {
			mRecorder.prepare();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() for recording failed");
		}
		mRecorder.start();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
	}
}