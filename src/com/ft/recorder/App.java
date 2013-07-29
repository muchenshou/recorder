package com.ft.recorder;

import com.ft.key.audio.comm.AudioRecorder;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;

public class App extends Activity  implements View.OnClickListener{

	AudioRecorder recorder;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		findViewById(R.id.record).setOnClickListener(this);
		findViewById(R.id.stop).setOnClickListener(this);
		recorder = new AudioRecorder(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.app, menu);
		return true;
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.record) {
			recorder.startRecord();
		}
		if (view.getId() == R.id.stop) {
			recorder.stopRecord();
		}
	}

}
