package com.ft.audio.comm;

import com.ft.recorder.Convection;


public class ACRespond {
	public int error = 0;
	public byte[] respondData;
	public int respondDataLen;

	public boolean hasWrong() {
		if (error < 0)
			return true;
		byte[] pbResp = respondData;
		pbResp[1] = (byte) (pbResp[1] & 0x03);
		int dataLen = Convection.makeWord(pbResp[1], pbResp[2]);
		if (dataLen != respondDataLen - 3 || dataLen <= 3
				|| pbResp[4] == (byte) 0xFF) {
			error = AudioToken.ERR_COMM_ERR;
			return true;
		}
		return false;
	}

	public int dataLen() {
		return respondDataLen;
	}

	public byte[] data() {
		return respondData;
	}

	@Override
	public String toString() {
		return String.format("Respond:\n\terrno:%d\n\tdataLen:%d\n\tdata:%s",
				error, respondDataLen,
				Convection.Bytes2HexString(respondData, respondDataLen));
	}

	public int errNo() {
		return error;
	}

}
