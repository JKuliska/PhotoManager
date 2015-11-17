package com.strv.photomanager;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.IOException;


public class ScaleImageAsyncTask extends AsyncTask<File, Void, File> {

	private int mReqWidth;
	private int mReqHeight;
	private OnFileScaledListener mListener;
	private Context mContext;


	public interface OnFileScaledListener {
		void onFileScaled(File file);
	}


	/*
	this constructor should be used when you want to use the async task and call execute on it
	 */
	public ScaleImageAsyncTask(Context context, int width, int height, OnFileScaledListener listener) {
		mReqWidth = width;
		mReqHeight = height;
		mListener = listener;
		mContext = context;
	}


	@Override
	protected File doInBackground(File... params) {
		try {
			return new ScaleImageHelper(mContext, mReqWidth, mReqHeight).scaleImageFile(params[0]);
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}


	@Override
	protected void onPostExecute(File file) {
		if(mListener != null) {
			mListener.onFileScaled(file);
		}
	}
}
