package com.example.voicetest;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;

public class RecordThread extends Thread {
	
	//用于抛出实时声音检测结果
	private Handler mHandler = null;
	
	//用于读取音频缓冲区的数据
	private AudioRecord mAudioRecoder;
	//读取的缓冲区的数据大小
	private int mBuffer;
	
	//线程开关
	private boolean isRunning = false;
	
	//上次声音达到要求的时间
	private long lastTime = 0;
	//本次声音达到要求的时间
	private long nowTime = 0;
	
	//声音采样频率
	private static int SAMPLE_RATE_IN_HZ = 44100;

	public RecordThread() {
		super();
		
		//获取缓冲区的大小
		mBuffer = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		
		mAudioRecoder = new AudioRecord(MediaRecorder.AudioSource.MIC, 
				SAMPLE_RATE_IN_HZ,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, mBuffer);
		//条件准备好，可以开始在线程分析声音
		isRunning = true;
	}


	private static final int VOICE_ACTION_NONE = 10;
	private static final int VOICE_ACTION_LONG = 11;
	private static final int VOICE_ACTION_ONCE = 12;
	private static final int VOICE_ACTION_DOUBLE = 13;
	
	private int mLastVoiceAction = VOICE_ACTION_NONE;

	//声音的检测结果
	private int values = 0;
	// soundBuffer 用于读取缓存区的声音
	
	public void run() {
		super.run();
		
		mAudioRecoder.startRecording();
		while (isRunning) {
			//读取缓存区数据
			byte[] soundBuffer = new byte[mBuffer];
			int readResult = mAudioRecoder.read(soundBuffer, 0, mBuffer);
			
			if(AudioRecord.ERROR_INVALID_OPERATION == readResult ){//硬件初始化失败
				isRunning = false;
			}
			else if( AudioRecord.ERROR_BAD_VALUE == readResult  ){//参数不能解析成有效的数据或索引
				isRunning = false;
			}
			
			values = 0;
			// 将 soundBuffer 内容取出，进行平方和运算
			for (int i = 0; i < soundBuffer.length; i++) {
				// 这里没有做运算的优化，为了更加清晰的展示代码
				values += soundBuffer[i] * soundBuffer[i];
			}
			// 平方和除以数据总长度，得到音量大小。可以获取白噪声值，然后对实际采样进行标准化。
			values /= readResult;
			// 如果想利用这个数值进行操作，建议用 sendMessage 将其抛出，在 Handler 里进行处理。
			int what = VoiceTest.VOICE_TEST_TING;
			int click = 0;
			if(null != mHandler && values > 3000 ){//value > 7000 表示捕获到一次有效的吹气
				nowTime = System.currentTimeMillis();//获取该次有效吹气的时间
				long dTime = nowTime - lastTime;//比较最近两次有效吹气时间的间隔
				if(dTime < 1000){ //间隔很短，表示一直在吹
					click = 3;
					mLastVoiceAction = VOICE_ACTION_LONG;
					if(VOICE_ACTION_LONG != mLastVoiceAction){
						mHandler.sendMessage( Message.obtain(mHandler, what, click, values) );
					}
					
				}
				else if(dTime <= 2000){//间隔不长不短，表示连着吹了两次
					click = 2;
				}
				if( dTime > 2000 ){//间隔时间较长，表示吹了一次
					click = 1;
				}
				lastTime = nowTime;//检测结束，更新上次有效吹气时间
			}
			else {
				what = VoiceTest.VOICE_TEST_NO;
			}
		}
		mAudioRecoder.stop();
	}
	
	public void stopThread() {
		// 在调用本线程的 Activity 的 onPause 里调用，以便 Activity 暂停时释放麦克风
		isRunning = false;
	}

	public void startThread() {
		// 在调用本线程的 Activity 的 onResume 里调用，以便 Activity 恢复后继续获取麦克风输入音量
		isRunning = true;
		super.start();
	}

	public Handler getHandler() {
		return mHandler;
	}

	public void setHandler(Handler mHandler) {
		this.mHandler = mHandler;
	}
	
}