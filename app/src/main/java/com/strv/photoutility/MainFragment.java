package com.strv.photoutility;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;


public class MainFragment extends Fragment {

	public static final String TAG = MainFragment.class.getSimpleName();

	private static final String ARG_PHOTO_URI = "photo_uri";

	private Uri mPhotoUri;
	private Intent mImageCaptureData;
	private int mImageCaptureRequestCode;


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if(savedInstanceState != null && savedInstanceState.containsKey(ARG_PHOTO_URI)) {
			mPhotoUri = savedInstanceState.getParcelable(ARG_PHOTO_URI);
		}
	}


	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_main, container, false);
	}


	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
		if(mPhotoUri != null && imageView != null) {
			Glide.with(getActivity()).load(mPhotoUri).into(imageView);
		}
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(ARG_PHOTO_URI, mPhotoUri);
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


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK) {
			handleImageActivityResult(requestCode, data);
		}
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch(requestCode) {
			case PhotoManager.REQUEST_PERMISSION_READ_EXTERNAL_STORAGE: {
				// If request is cancelled, the result arrays are empty.
				if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// permission was granted, yay! Do the
					// permission-related task you need to do.
					handleImageActivityResult(mImageCaptureRequestCode, mImageCaptureData);
					mImageCaptureRequestCode = 0;
					mImageCaptureData = null;
				} else {
					// permission denied, boo! Disable the
					// functionality that depends on this permission.
					Toast.makeText(getContext(), getString(R.string.toast_gallery_access_denied), Toast.LENGTH_LONG).show();
				}
			}
		}
	}


	private void handleImageActivityResult(int requestCode, Intent data) {
		Uri photoUri = PhotoManager.onActivityResult(this, requestCode, data, mPhotoUri, new PhotoManager.OnFileFromUriExtractedListener() {
			@Override
			public void onFileFromUriExtracted(File file) {
				Log.d(TAG, "file size: " + file.length());
			}
		});

		if(photoUri != null) {
			mPhotoUri = photoUri;
			ImageView imageView = (ImageView) getActivity().findViewById(R.id.imageView);
			Glide.with(getActivity()).load(mPhotoUri).into(imageView);
		}
		//save the request code and data to member variable, the runtime permission request was already handled in PhotoManager.onActivityResult()
		else {
			mImageCaptureData = data;
			mImageCaptureRequestCode = requestCode;
		}
	}


	private void startCamera() {
		try {
			mPhotoUri = PhotoManager.launchCameraOnly(this, R.string.choose_camera_picker);
		} catch(IOException e) {
			e.printStackTrace();
			Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_LONG).show();
		}
	}


	private void startGalleryRecent() {
		mPhotoUri = null;
		PhotoManager.launchGalleryRecentOnly(this, getString(R.string.choose_gallery_picker));
	}


	private void startGallery() {
		mPhotoUri = null;
		PhotoManager.launchGalleryOnly(this, R.string.choose_gallery_picker);
	}


	private void startCameraGallery() {
		try {
			mPhotoUri = PhotoManager.launchCameraGallery(this, R.string.choose_camera_picker);
		} catch(IOException e) {
			e.printStackTrace();
			Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_LONG).show();
		}
	}
}
