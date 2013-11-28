package com.example.voicetest;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class VoiceTest extends Activity implements View.OnClickListener{
	
	private Button mStart = null;
	private Button mStop = null;
	private TextView mResult = null;
	
	
	private RecordThread mRecordThread = null;
	
	
	private static final int START_VOICE_TEST = 0;
	private static final int STOP_VOICE_TEST = 1;
	
	public static final int VOICE_TEST_TING = 1000;
	public static final int VOICE_TEST_NO = 10001;
	
	private Handler mHandle = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int what = msg.what;
			String result = "";
			switch (what) {
			case VOICE_TEST_NO:
			case VOICE_TEST_TING:
				result = "action code is " + msg.arg1 + " -- voice is " + msg.arg2;
				break;
				
			case START_VOICE_TEST:
				result = "start voice test";
				break;
				
			case STOP_VOICE_TEST:
				result = "stop voice test";
				break;
				
			default:
				break;
			}
			mResult.setText(result);
		}
		
	};
	
	
	private static int SAMPLE_RATE_IN_HZ = 44100;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_voice_test);
		
		mStart = (Button)findViewById(R.id.start);
		mStart.setOnClickListener(this);
		mStop = (Button)findViewById(R.id.stop);
		mStop.setOnClickListener(this);
		
		mResult = (TextView)findViewById(R.id.textView1);
		
//		int bs = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
//				AudioFormat.CHANNEL_CONFIGURATION_MONO,
//				AudioFormat.ENCODING_PCM_16BIT);
//		AudioRecord ar = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ,
//				AudioFormat.CHANNEL_CONFIGURATION_MONO,
//				AudioFormat.ENCODING_PCM_16BIT, bs);
	}


	@Override
	public void onClick(View view) {
		int id = view.getId();
		switch (id) {
		case R.id.start:
			mRecordThread = new RecordThread();
			mRecordThread.setHandler(mHandle);
			mRecordThread.startThread();
			mHandle.sendEmptyMessage(START_VOICE_TEST);
			break;
		case R.id.stop:
			mRecordThread.stopThread();
			break;
		default:
			break;
		}
	}
	

	
}
