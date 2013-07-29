package com.ft.audio.comm;

import android.content.Context;
import android.media.AudioManager;

public class AudioComm {
	AudioPlayer mPlayer;
	AudioRecorder mRecord;
	private static AudioComm mComm = null;
	private AudioManager am;
	boolean blNeedRestoreVolume = true;
	int nCurMusicVolume;
	private boolean isRunning = false;

	private AudioComm(Context con) {
		am = (AudioManager) con.getSystemService(Context.AUDIO_SERVICE);
		mPlayer = new AudioPlayer(con);
		mRecord = new AudioRecorder(con);
	}

	private void setam() {
		am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		am.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
				AudioManager.VIBRATE_SETTING_OFF);
		am.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,
				AudioManager.VIBRATE_SETTING_OFF);
		am.setMode(AudioManager.MODE_NORMAL);
		// 调节播放时的音量
		nCurMusicVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		if (nCurMusicVolume != am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
			blNeedRestoreVolume = true;
			int maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			int setVol = maxVol * 1;
			am.setStreamVolume(AudioManager.STREAM_MUSIC, setVol,
					AudioManager.FLAG_ALLOW_RINGER_MODES);
			// am.setStreamVolume(AudioManager.STREAM_MUSIC,
			// maxVol,AudioManager.FLAG_ALLOW_RINGER_MODES);
		}
	}

	private void restore() {
		if (blNeedRestoreVolume) {
			am.setStreamVolume(AudioManager.STREAM_MUSIC, nCurMusicVolume,
					AudioManager.FLAG_ALLOW_RINGER_MODES);
		}
	}

	public void Connect() throws ACException {
		if (isRunning) {
			return;
		}
		isRunning = true;
		setam();
		mPlayer.start();
		mRecord.start();
	}

	public ACRespond Transmit(ACInstruct instruct)throws ACException  {
		ACRespond respond = new ACRespond();
		byte pcm[] = instruct.toBytes();
		byte recvBuf[] = new byte[1024];

		mPlayer.send(pcm, pcm.length);
		int rtn = mRecord.recv(recvBuf);
		respond.error = rtn;
		respond.respondData = recvBuf;
		respond.respondDataLen = rtn > 0 ? rtn : 0;
		// 处理接收回来的数据
		return respond;
	}

	public void DisConnect() {
		if (isRunning) {
			mPlayer.stop();
			mRecord.stop();
			restore();
			isRunning = false;
		}
	}

	public static AudioComm Instance(Context con) {
		if (mComm == null) {
			mComm = new AudioComm(con);
		}
		return mComm;
	}

	public static AudioComm Instance() {
		if (mComm == null) {
			throw new RuntimeException("no initial");
		}
		return mComm;
	}
}
