package com.ft.audio.comm;

public class ACInstruct {
	public byte[] sendData;
	public int timeoutTime = 3000;

	public ACInstruct(byte[] send, int off, int len) {
		sendData = new byte[len];
		System.arraycopy(send, 0, sendData, 0, len);
	}

	public byte[] toBytes() {
		// ��Ҫһ����֯apdu���ݵ��࣬ת��Ϊ��Ƶ����,�����ֽ����飬
		return sendData;
	}
}
