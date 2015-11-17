package com.strv.photoutility;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;


public class MainFragment extends PhotoBaseFragment {

	public static final String TAG = MainFragment.class.getSimpleName();

	private ImageView mImageView;


	@Override
	protected void imageLoadedForUri(Uri photoUri) {
		if(mPhotoUri != null && mImageView != null) {
			Glide.with(getActivity()).load(mPhotoUri).into(mImageView);
		}
	}


	@Override
	protected void imageFileLoaded(File file) {
		if(file != null) {
			Log.d(TAG, "file size: " + file.length());
		} else {
			Log.d(TAG, "file is null");
		}
	}


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}


	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_main, container, false);
	}


	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mImageView = (ImageView) view.findViewById(R.id.imageView);
		if(mPhotoUri != null && mImageView != null) {
			Glide.with(getActivity()).load(mPhotoUri).into(mImageView);
		}
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_main, menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.action_camera:
				startCamera();
				return true;
			case R.id.action_gallery:
				startGallery();
				return true;
			case R.id.action_gallery_recent:
				startGalleryRecent();
				return true;
			case R.id.action_camera_gallery:
				startCameraGallery();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
