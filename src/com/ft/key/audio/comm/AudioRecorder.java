package com.ft.key.audio.comm;

import com.ft.recorder.LogFile;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

public class AudioRecorder implements Runnable {
	private AudioRecord mAudioRecord = null;
	private AudioManager am;

	public AudioRecorder(Context con) {
		am = (AudioManager) con.getSystemService(Context.AUDIO_SERVICE);
		// am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		// am.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
		// AudioManager.VIBRATE_SETTING_OFF);
		// am.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,
		// AudioManager.VIBRATE_SETTING_OFF);
		// am.setMode(AudioManager.MODE_NORMAL);
	}

	public int startRecord() {
		if (isRun)
			return 0;
		int recBufSize;
		int nState = 0;
		int frequency = 44100;
		int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
		int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
		recBufSize = AudioRecord.getMinBufferSize(frequency,
				channelConfiguration, audioEncoding);
		if (recBufSize == AudioRecord.ERROR_BAD_VALUE
				|| recBufSize == AudioRecord.ERROR) {
			return -1;
		}

		if (recBufSize < 4096) {
			recBufSize = 4096;
		}
		mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
				frequency, channelConfiguration, audioEncoding, recBufSize);
		nState = mAudioRecord.getState();

		if (nState != AudioRecord.STATE_INITIALIZED) {
			// 经测试,在有些手机上的确会走到这里
			mAudioRecord.release();
			mAudioRecord = null;
			throw new RuntimeException("未能初始化录音设备");
		}

		nState = mAudioRecord.getRecordingState();
		if (nState != AudioRecord.STATE_INITIALIZED) {
			mAudioRecord.release();
			mAudioRecord = null;
			return -1;
		}
		mAudioRecord.startRecording();
		while (mAudioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
			// Wait until we can start recording...
		}
		new Thread(this).start();
		return 0;
	}

	boolean isRun = false;

	@Override
	public void run() {
		byte[] pbDecode = new byte[4096];
		int nReadLen;
		isRun = true;
		try {
			LogFile log = new LogFile(Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ "/ft_"
					+ SystemClock.uptimeMillis()
					+ ".pcm");
			while (isRun) {
				nReadLen = mAudioRecord.read(pbDecode, 0, 4096);
				log.writeBytes(pbDecode, 0, nReadLen);
			}
		} catch (Exception e) {

		} finally {
			mAudioRecord.stop();
			mAudioRecord.release();// 该函数内部会调用stop函数
		}
	}

	public void stopRecord() {
		if (isRun == false)
			return;
		isRun = false;
	}
}
