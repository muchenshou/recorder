package com.ft.recorder;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class LogFile {
	private String LogPath;

	public LogFile(String fullpath) {
		File file = new File(fullpath);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		LogPath = fullpath;
	}

	public void writeBytes(byte[] data, int off, int len) {
		RandomAccessFile random_file = null;
		try {
			random_file = new RandomAccessFile(LogPath, "rw");
			long length = random_file.length();
			random_file.seek(length);
			random_file.write(data, 0, len);
			random_file.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
