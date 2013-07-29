package com.ft.audio.comm;

import android.content.Context;
import android.util.Log;

import com.ft.recorder.LogFile;

public class AudioToken {
	static {
		System.loadLibrary("audiopassjni");
	}
	public static String fileName = "";// �ļ���
	private AudioComm mComInstant;

	public AudioToken(Context con) {
		mComInstant = AudioComm.Instance(con);
		try {
			mComInstant.Connect();
		} catch (ACException e) {
			e.printStackTrace();
		}
	}

	public native static int AssembleSendData(byte[] lpSourceBuf,
			short wBufLen, byte[] lpOutBuf, int dwOutBufLen, short wFreq,
			short wHeadSquareWavNum, short wTailSquareWavNum,
			byte bHeadReduNum, short wHeadReduTime, int wBit0Time,
			int wBit1Time, byte bTailReduNum, short wTailReduTime,
			boolean blSetPara);

	public native static int AnalyseRecvData(byte[] lpData, int dataLen,
			byte[] lpOutBuf, int[] pdwOutBufLen, int nBit0Width,
			int nBit1Width, int nHeadWidth);

	// ��ͨ�˲�
	public native static void LowPassFilter(byte bytes[], int len, int alpha);

	/**
	 * ȡ��һ��ָ�����ı���
	 * 
	 * @return
	 */
	private static byte[] dataEncoded(int type) {
		int nEncodeLen = 0;
		byte[] pbEncode = null;
		byte sendData[] = new byte[8];
		sendData[0] = (byte) 0xF5;// ֡ͷ,����ΪF5,����Ϊ5F
		sendData[1] = (byte) 0x54;// ���ȸ��ֽ�,���6λ�̶�Ϊ"010101"
		sendData[2] = (byte) 0x05;// ���ȵ��ֽ�
		sendData[3] = (byte) 0x12;// 0x10��ʾ֡���Ϊ1,0x03��ʾ���ն���Key��������
		sendData[4] = 0x00;// ���δ���� APDU ����Ҫ��֡����
		sendData[5] = 0x01;// ȥ��һ�����
		sendData[6] = (byte) type;// 0x01/0x02
		sendData[7] = 0;

		for (int i = 3; i < 7; i++) {
			sendData[7] ^= sendData[i];
		}
		nEncodeLen = AssembleSendData(sendData, (short) sendData.length,
				pbEncode, 0, (short) 0, (short) 0, (short) 0, (byte) 0,
				(short) 0, 0, 0, (byte) 0, (short) 0, false);

		pbEncode = new byte[nEncodeLen];
		nEncodeLen = AssembleSendData(sendData, (short) sendData.length,
				pbEncode, pbEncode.length, (short) 0, (short) 0, (short) 0,
				(byte) 0, (short) 0, 0, 0, (byte) 0, (short) 0, false);

		return pbEncode;
	}

	public static int sendToAudioKey(byte[] cmdEncode, int cmdLength,
			byte[] recvBuff, int recvLen, int timeout) {
		AudioComm comm = AudioComm.Instance();
		ACInstruct ins = new ACInstruct(cmdEncode, 0, cmdLength);
		ACRespond respond = null;
		int count = 0;

		LogFile log = new LogFile(fileName + ".s");
		log.writeBytes(cmdEncode, 0, cmdLength);
		ACInstruct sendins = ins;
		int type = 0;
		while (count < 3) {
			try {
				respond = comm.Transmit(sendins);
			} catch (ACException e) {
				e.printStackTrace();
			}
			Log.i("hello", respond.toString());
			if (respond.hasWrong()) {
				switch (respond.errNo()) {
				case ERR_TIMEOUT:
				case ERR_COMM_ERR:
					count++;
					continue;
				case ERR_BAD_CHECKSUM:
					byte[] last = dataEncoded(type++ % 2 + 1);
					sendins = new ACInstruct(last, 0, last.length);
				}
			}

			if (recvLen < respond.dataLen()) {
				return AudioToken.ERR_COMM_ERR;
			}
			System.arraycopy(respond.data(), 0, recvBuff, 0, respond.dataLen());
			return 0;
		}
		return respond.errNo();
	}

	public static int getMobileInfo(byte[] byManufacturer, byte[] byModel) {
		String strManufacturer = android.os.Build.MANUFACTURER;
		String strModel = android.os.Build.MODEL;

		byte[] manufacturer = strManufacturer.getBytes();
		byte[] model = strModel.getBytes();

		System.arraycopy(manufacturer, 0, byManufacturer, 0,
				manufacturer.length);
		System.arraycopy(model, 0, byModel, 0, model.length);

		return 0;
	}

	public void Release() {
		mComInstant.DisConnect();
	}

	public static final int ERR_SYSTEM = -1;
	public static final int ERR_COMM_ERR = -2;
	public static final int ERR_TIMEOUT = -3;
	public static final int ERR_INVALID_PARAMETER = 0x20020001; // �������Ϸ�
	public static final int ERR_INSUFFICIENT_BUFFER = 0x20020002; // �������ռ䲻��
	public static final int ERR_DATA_INCOMPLETE = 0x20020003; // ���ݲ�����
	public static final int ERR_NO_HEADER = 0x20020004; // û�ҵ���������
	public static final int ERR_HEADER_INCOMPLETE = 0x20020005; // ͷ��������
	public static final int ERR_BAD_DATA = 0x20020006; // ���ݲ��Ϸ�
	public static final int ERR_BAD_CHECKSUM = 0x20020007; // У�鲻�Ϸ�

}
