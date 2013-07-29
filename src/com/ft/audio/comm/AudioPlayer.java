package com.ft.audio.comm;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Process;

public class AudioPlayer implements Runnable {

	AudioTrack audioTrack = null;
	public short freq = 0;// 转接器返回波形的频率
	public static final int playFreq = 44100;// 播放频率
	int playBufSize;

	public AudioPlayer(Context con) {
	}

	public void start() throws ACException {
		new Thread(this).start();
	}

	private void init() throws ACException {
		playBufSize = android.media.AudioTrack.getMinBufferSize(playFreq,
				AudioFormat.CHANNEL_CONFIGURATION_STEREO,
				// AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_8BIT);
		if (playBufSize == AudioTrack.ERROR_BAD_VALUE
				|| playBufSize == AudioTrack.ERROR) {
			throw new ACException(ACException.AC_ERR_FAIL_INITPLAY);
		}
		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
				playFreq, // 频率
				AudioFormat.CHANNEL_CONFIGURATION_STEREO,// 双声道
				AudioFormat.ENCODING_PCM_8BIT, // 8位
				dataBufLen > playBufSize ? dataBufLen : playBufSize,
				AudioTrack.MODE_STATIC);
		if (audioTrack.getPlayState() != AudioTrack.STATE_INITIALIZED) {
			audioTrack.release();
			audioTrack = null;
			throw new ACException(ACException.AC_ERR_FAIL_INITPLAY);
		}
	}

	public int send(byte[] buf, int len) throws ACException {
		synchronized (clock) {
			dataBuf = buf;
			dataBufLen = len;
			if (audioTrack != null) {
				audioTrack.stop();
				audioTrack.release();
			}
			init();
			hasData = true;
		}
		return 0;
	}

	public void stop() {
		if (audioTrack != null) {
			audioTrack.stop();
			audioTrack.release();
		}
		isClose = true;
	}

	boolean hasData = false;
	byte dataBuf[];
	int dataBufLen;
	Object clock = new Object();
	boolean isClose = false;

	private void handle() {
		audioTrack.write(dataBuf, 0, dataBufLen);
		audioTrack.play();
		while (audioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
			// Wait until we've started playing...
		}
		
	}

	@Override
	public void run() {
		Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
		while (!isClose) {
			synchronized (clock) {
				if (hasData) {
					handle();
					hasData = false;
				}
			}
		}
	}
}
