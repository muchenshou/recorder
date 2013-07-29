package com.ft.audio.comm;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.SystemClock;
import android.util.Log;

import com.ft.recorder.LogFile;

public class AudioRecorder {
	private static AudioRecord mAudioRecord = null;
	int recvBufSize;

	public AudioRecorder(Context con) {

	}

	public void start() throws ACException {
		int nState = 0;
		mAudioRecord = getRecord();
		if (mAudioRecord == null) {
			throw new ACException(ACException.AC_ERR_FAIL_INITRECORD);
		}
		nState = mAudioRecord.getRecordingState();
		if (nState != AudioRecord.STATE_INITIALIZED) {
			mAudioRecord.release();
			mAudioRecord = null;
			throw new ACException(ACException.AC_ERR_FAIL_INITRECORD);
		}
		mAudioRecord.startRecording();
		while (mAudioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
			// Wait until we can start recording...
		}
	}

	private AudioRecord getRecord() {
		int[] samplingRates = { 44100, 22050, 16000, 11025, 8000 };

		for (int i = 0; i < samplingRates.length; ++i) {
			try {
				int min = AudioRecord.getMinBufferSize(samplingRates[i],
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						// AudioFormat.CHANNEL_IN_MONO,
						AudioFormat.ENCODING_PCM_16BIT);
				if (min < 4096)
					min = 4096;
				recvBufSize = min;
				AudioRecord record = new AudioRecord(
						MediaRecorder.AudioSource.MIC, samplingRates[i],
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT, min);
				if (record.getState() == AudioRecord.STATE_INITIALIZED) {
					Log.d("Recorder",
							"Audio recorder initialised at "
									+ record.getSampleRate());
					return record;
				}
				record.release();
				record = null;
			} catch (IllegalArgumentException e) {
				// Try the next one.

			}
		}
		// None worked.
		return null;
	}

	public int recv(byte buf[]) {
		byte[] pbDecode = new byte[recvBufSize];
		byte[] pbResp = buf;
		int nReadLen;
		int nRet;
		int nBit0Width = 350;
		int nBit1Width = 540;
		int nHeadWidth = 900;

		int pdwLenth[] = { 0 };
		long t1 = SystemClock.uptimeMillis();
		while (true) {
			long t2 = SystemClock.uptimeMillis();
			if (t2 - t1 > 3000)
				return AudioToken.ERR_TIMEOUT;
			pdwLenth[0] = pbResp.length;
			nReadLen = mAudioRecord.read(pbDecode, 0, pbDecode.length);
			LogFile log = new LogFile(AudioToken.fileName + ".r");
			log.writeBytes(pbDecode, 0, nReadLen);
			nRet = AudioToken.AnalyseRecvData(pbDecode, nReadLen, pbResp,
					pdwLenth, nBit0Width, nBit1Width, nHeadWidth);
			if (nRet == AudioToken.ERR_BAD_CHECKSUM
					|| nRet == AudioToken.ERR_BAD_DATA)// 需要重发命令数据
			{
				if (nRet == AudioToken.ERR_BAD_CHECKSUM) {
					return nRet;
				}

				return AudioToken.ERR_COMM_ERR;

			} else if (nRet == 0 && pdwLenth[0] > 0)// 接收到命令数据了
			{
				// OK
				Log.i("Info", "data rcved!");
				nRet = pdwLenth[0];
				if (pbResp[0] != (byte) 0x5F)// 数据长度不匹配,重发
				{
					return AudioToken.ERR_COMM_ERR;
				}
				break;

			} else {
				// 继续等待
			}
		}
		return nRet;
	}

	public void stop() {
		mAudioRecord.stop();
		mAudioRecord.release();// 该函数内部会调用stop函数
	}
}
