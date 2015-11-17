package com.strv.photoutility;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.strv.photomanager.PhotoManager;

import java.io.File;
import java.io.IOException;


public abstract class PhotoBaseFragment extends Fragment {

	private static final String ARG_PHOTO_URI = "photo_uri";
	private static final String ARG_IMAGE_CAPTURE_DATA = "image_capture_data";
	private static final String ARG_IMAGE_CAPTURE_REQUEST_CODE = "image_capture_request_code";
	private static final String ARG_IMAGE_CAPTURE_RESULT_CODE = "image_capture_result_code";

	protected Uri mPhotoUri;
	private Intent mImageCaptureData;
	private int mImageCaptureRequestCode = -1;
	private int mImageCaptureResultCode = Integer.MIN_VALUE;


	protected abstract void imageLoadedForUri(Uri photoUri);
	protected abstract void imageFileLoaded(File file);


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if(savedInstanceState != null) {
			if(savedInstanceState.containsKey(ARG_PHOTO_URI)) {
				mPhotoUri = savedInstanceState.getParcelable(ARG_PHOTO_URI);
			}
			if(savedInstanceState.containsKey(ARG_IMAGE_CAPTURE_DATA)) {
				mImageCaptureData = savedInstanceState.getParcelable(ARG_IMAGE_CAPTURE_DATA);
			}
			if(savedInstanceState.containsKey(ARG_IMAGE_CAPTURE_REQUEST_CODE)) {
				mImageCaptureRequestCode = savedInstanceState.getInt(ARG_IMAGE_CAPTURE_REQUEST_CODE, -1);
			}
			if(savedInstanceState.containsKey(ARG_IMAGE_CAPTURE_RESULT_CODE)) {
				mImageCaptureResultCode = savedInstanceState.getInt(ARG_IMAGE_CAPTURE_RESULT_CODE, Integer.MIN_VALUE);
			}
		}
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(ARG_PHOTO_URI, mPhotoUri);
		if(mImageCaptureData != null) {
			outState.putParcelable(ARG_IMAGE_CAPTURE_DATA, mImageCaptureData);
		}
		if(mImageCaptureRequestCode > 0) {
			outState.putInt(ARG_IMAGE_CAPTURE_REQUEST_CODE, mImageCaptureRequestCode);
		}
		if(mImageCaptureResultCode != Integer.MIN_VALUE) {
			outState.putInt(ARG_IMAGE_CAPTURE_RESULT_CODE, mImageCaptureResultCode);
		}
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		handleImageActivityResult(requestCode, resultCode, data);
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
					handleImageActivityResult(mImageCaptureRequestCode, mImageCaptureResultCode, mImageCaptureData);
					mImageCaptureRequestCode = 0;
					mImageCaptureData = null;
					mImageCaptureResultCode = Integer.MIN_VALUE;
				} else {
					// permission denied, boo! Disable the
					// functionality that depends on this permission.
					Toast.makeText(getContext(), getString(R.string.toast_gallery_access_denied), Toast.LENGTH_LONG).show();
				}
			}
		}
	}


	private void handleImageActivityResult(int requestCode, int resultCode, Intent data) {
		Uri photoUri = PhotoManager.onActivityResult(this, requestCode, resultCode, data, mPhotoUri, new PhotoManager.OnFileFromUriExtractedListener() {
			@Override
			public void onFileFromUriExtracted(File file) {
				if(file != null) {
					imageFileLoaded(file);
				} else {
					Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_LONG).show();
				}
			}
		});

		if(photoUri != null) {
			mPhotoUri = photoUri;
			imageLoadedForUri(photoUri);
		}
		//save the request code and data to member variable, the runtime permission request was already handled in PhotoManager.onActivityResult()
		else {
			mImageCaptureData = data;
			mImageCaptureRequestCode = requestCode;
			mImageCaptureResultCode = resultCode;
		}
	}


	protected void startCamera(@StringRes int chooserTitleStringResId) {
		try {
			mPhotoUri = PhotoManager.launchCameraOnly(this, chooserTitleStringResId);
		} catch(IOException e) {
			e.printStackTrace();
			Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_LONG).show();
		}
	}


	protected void startCamera() {
		startCamera(R.string.choose_camera_picker);
	}


	protected void startGalleryRecent(@StringRes int chooserTitleStringResId) {
		mPhotoUri = null;
		PhotoManager.launchGalleryRecentOnly(this, chooserTitleStringResId);
	}


	protected void startGalleryRecent() {
		startGalleryRecent(R.string.choose_gallery_picker);
	}


	protected void startGallery(@StringRes int chooserTitleStringResId) {
		mPhotoUri = null;
		PhotoManager.launchGalleryOnly(this, chooserTitleStringResId);
	}


	protected void startGallery() {
		startGallery(R.string.choose_gallery_picker);
	}


	protected void startCameraGallery(@StringRes int chooserTitleStringResId) {
		try {
			mPhotoUri = PhotoManager.launchCameraGallery(this, chooserTitleStringResId);
		} catch(IOException e) {
			e.printStackTrace();
			Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_LONG).show();
		}
	}


	protected void startCameraGallery() {
		startCameraGallery(R.string.choose_camera_picker);
	}

}
