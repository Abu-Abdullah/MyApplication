package com.test.myapplication;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import org.freedesktop.gstreamer.GStreamer;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback
{
	public static final String TAG = "MainActivity";

	private native void nativeInit(String uri);     // Initialize native code, build pipeline, etc
	private native void nativeFinalize(); // Destroy pipeline and shutdown native code
	private native void nativePlay();     // Set pipeline to PLAYING
	private native void nativePause();    // Set pipeline to PAUSED, RTSP cannot be paused !
	private static native boolean nativeClassInit(); // Initialize native class: cache Method IDs for callbacks
	private native void nativeSurfaceInit(Object surface);
	private native void nativeSurfaceFinalize();
	private static native Bitmap nativeSnapshot();
	private long native_custom_data;      // Native code will use this to keep private data

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		try {GStreamer.init(this);}
		catch (Exception e) {e.printStackTrace();}

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				final Bitmap bmp = nativeSnapshot();
				ImageView img = findViewById(R.id.snapshot);
				img.setImageBitmap(bmp);
			}
		});

		GStreamerSurfaceView sv = findViewById(R.id.surface_video);
		SurfaceHolder sh = sv.getHolder(); // To set the resolution not the displayed view
		sh.addCallback(this);

		String command = "udpsrc port=5009 ! application/x-rtp,payload=96,media=video,clock-rate=90000,encoding-name=H264,a-framerate=40,width=640,height=480 ! rtph264depay ! avdec_h264 ! videoconvert ! glimagesink name=mysink";
		nativeInit(command);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings)
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}


	// Called from native code. This sets the content of the TextView from the UI thread.
	private void setMessage(final String message)
	{
		Log.e(TAG, "setMessage:" + message);
	}

	// Called from native code. Native code calls this once it has created its pipeline and
	// the main loop is running, so it is ready to accept commands.
	private void onGStreamerInitialized()
	{
		Log.e(TAG, "onGStreamerInitialized");
		nativePlay();
	}


	static
	{
		System.loadLibrary("gstreamer_android");
		System.loadLibrary("player"); // player.c
		nativeClassInit();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		Log.e(TAG, "surfaceChanged to format " + format + " width " + width + " height " + height);
		nativeSurfaceInit(holder.getSurface());
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		Log.e(TAG, "surfaceCreated: " + holder.getSurface());
		nativeSurfaceInit(holder.getSurface());
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		Log.e(TAG, "surfaceDestroyed");
		nativeSurfaceFinalize(); // Commit this to avoid stopping the stream when the activity is paused in the background
	}

	@Override
	public void onDestroy()
	{
		Log.e(TAG, "onDestroy");
		nativeFinalize();
		super.onDestroy();
	}
}
