package com.ft.audio.comm;

public class ACInstruct {
	public byte[] sendData;
	public int timeoutTime = 3000;

	public ACInstruct(byte[] send, int off, int len) {
		sendData = new byte[len];
		System.arraycopy(send, 0, sendData, 0, len);
	}

	public byte[] toBytes() {
		// 需要一个组织apdu数据的类，转换为音频数据,返回字节数组，
		return sendData;
	}
}
