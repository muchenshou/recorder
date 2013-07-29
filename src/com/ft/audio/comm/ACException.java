package com.ft.audio.comm;

import android.util.SparseArray;

public class ACException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static SparseArray<String> infos = new SparseArray<String>();
	public static int AC_ERR_FAIL_INITRECORD = 1;
	public static int AC_ERR_FAIL_INITPLAY = 2;
	static {
		infos.append(AC_ERR_FAIL_INITRECORD, "¼����ʼ��ʧ��");
		infos.append(AC_ERR_FAIL_INITPLAY, "���ų�ʼ��ʧ��");
	}
	private int err;
	public ACException(int errno) {
		err = errno;
	}

	public String info() {
		return infos.get(err);
	}
}
