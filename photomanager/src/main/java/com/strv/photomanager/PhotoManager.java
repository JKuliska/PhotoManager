package com.strv.photomanager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.CheckResult;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class PhotoManager {

	public static final int REQUEST_IMAGE_CAPTURE = 168;
	public static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 169;


	/**
	 * launches a camera app that is installed on the phone, launches an app picker if more options are available
	 *
	 * @param fragment           fragment calling the camera intent
	 * @param cameraChooserTitle title of the camera app chooser
	 * @param galleryDirName     name of directory where the taken picture is supposed to be stored
	 * @return Uri of the image that where the captured image will be stored
	 * @throws IOException is thrown if the image file creation was not successful
	 */
	public static Uri launchCameraOnly(Fragment fragment, String cameraChooserTitle, String galleryDirName) throws IOException {
		Uri uri = createImageFileUri(fragment.getContext(), galleryDirName);

		if(uri == null) {
			throw new IOException();
		}

		Intent cameraLauncher = makeCameraChooserIntent(uri, cameraChooserTitle);
		fragment.startActivityForResult(cameraLauncher, REQUEST_IMAGE_CAPTURE);

		return uri;
	}


	/**
	 * launches a camera app that is installed on the phone, launches an app picker if more options are available
	 * the directory name where the image is stored is based on the package name of the app using this PhotoManager
	 *
	 * @param fragment           fragment calling the camera intent
	 * @param cameraChooserTitle title of the camera app chooser
	 * @return Uri of the image that where the captured image will be stored
	 * @throws IOException is thrown if the image file creation was not successful
	 */
	public static Uri launchCameraOnly(Fragment fragment, String cameraChooserTitle) throws IOException {
		return launchCameraOnly(fragment, cameraChooserTitle, getDefaultDir(fragment.getContext()));
	}


	/**
	 * launches a camera app that is installed on the phone, launches an app picker if more options are available
	 * the directory name where the image is stored is based on the package name of the app using this PhotoManager
	 *
	 * @param fragment                fragment calling the camera intent
	 * @param cameraChooserTitleResId string resolution id of a title of the camera app chooser
	 * @return Uri of the image that where the captured image will be stored
	 * @throws IOException is thrown if the image file creation was not successful
	 */
	public static Uri launchCameraOnly(Fragment fragment, @StringRes int cameraChooserTitleResId) throws IOException {
		return launchCameraOnly(fragment, fragment.getString(cameraChooserTitleResId));
	}


	/**
	 * launches an image picker of recently used images
	 *
	 * @param fragment            fragment calling the recent pictures intent
	 * @param galleryChooserTitle title of the gallery app chooser
	 */
	public static void launchGalleryRecentOnly(Fragment fragment, String galleryChooserTitle) {
		Intent cameraLauncher = makeGalleryRecentChooserIntent(galleryChooserTitle);
		fragment.startActivityForResult(cameraLauncher, REQUEST_IMAGE_CAPTURE);
	}


	/**
	 * launches an image picker of recently used images
	 *
	 * @param fragment                 fragment calling the recent pictures intent
	 * @param galleryChooserTitleResId string resolution id of a title of the gallery app chooser
	 */
	public static void launchGalleryRecentOnly(Fragment fragment, @StringRes int galleryChooserTitleResId) {
		launchGalleryRecentOnly(fragment, fragment.getString(galleryChooserTitleResId));
	}


	/**
	 * creates a camera chooser for camera with all the apps on the device that support taking pictures
	 *
	 * @param outputFileUri      Uri where the captured image should be saved
	 * @param cameraChooserTitle title of the camera app chooser
	 * @return Intent that when run will initiate an app chooser
	 */
	private static Intent makeCameraChooserIntent(Uri outputFileUri, String cameraChooserTitle) {
		return Intent.createChooser(makeCameraIntent(outputFileUri), cameraChooserTitle);
	}


	/**
	 * creates an Intent to capture an image with external app
	 *
	 * @param outputFileUri Uri of the file where the captured image is supposed to be stored
	 * @return Intent that when run will initiate a camera app
	 */
	private static Intent makeCameraIntent(Uri outputFileUri) {
		Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri); // set the image file name
		return captureIntent;
	}


	/**
	 * creates an app chooser for picking recent pictures from gallery
	 *
	 * @param galleryChooserTitle title of the chooser
	 * @return Intent that when run will initiate a gallery chooser for apps that can display recently used images
	 */
	private static Intent makeGalleryRecentChooserIntent(String galleryChooserTitle) {
		Intent galleryIntent = makeGalleryRecentIntent();
		return Intent.createChooser(galleryIntent, galleryChooserTitle);
	}


	/**
	 * creates an Intent that when run will start an app that lets the user pick from recently used images
	 *
	 * @return Intent that when run will start an app that can display recently used images
	 */
	private static Intent makeGalleryRecentIntent() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		// The MIME data type filter
		intent.setType("image/*");
		// Only return URIs that can be opened with ContentResolver
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		// Create the chooser Intent
		return intent;
	}


	/**
	 * launches gallery from which the user can pick an image
	 *
	 * @param fragment            fragment that started the gallery picker
	 * @param galleryChooserTitle title of the app chooser for the case when there are more gallery apps installed on the device
	 */
	public static void launchGalleryOnly(Fragment fragment, String galleryChooserTitle) {
		Intent cameraLauncher = makeGalleryChooserIntent(galleryChooserTitle);
		fragment.startActivityForResult(cameraLauncher, REQUEST_IMAGE_CAPTURE);
	}


	/**
	 * launches gallery from which the user can pick an image
	 *
	 * @param fragment                 fragment that started the gallery picker
	 * @param galleryChooserTitleResId string resolution id of the title of the app chooser for the case when there are more gallery apps installed on the device
	 */
	public static void launchGalleryOnly(Fragment fragment, @StringRes int galleryChooserTitleResId) {
		launchGalleryOnly(fragment, fragment.getString(galleryChooserTitleResId));
	}


	/**
	 * creates an app chooser for picking pictures from gallery
	 *
	 * @param galleryChooserTitle title of the chooser
	 * @return Intent that when run will initiate a gallery chooser for apps that can display recently used images
	 */
	private static Intent makeGalleryChooserIntent(String galleryChooserTitle) {
		return Intent.createChooser(makeGalleryIntent(), galleryChooserTitle);
	}


	/**
	 * creates an Intent that when run will start an app that lets the user pick an image from gallery
	 *
	 * @return Intent that when run will start an app that can display recently used images
	 */
	private static Intent makeGalleryIntent() {
		Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.setType("image/*");
		return intent;
	}


	/**
	 * launches an app picker with all camera and gallery apps installed on the device
	 *
	 * @param fragment           fragment starting the camera/gallery app
	 * @param cameraChooserTitle title of the app chooser
	 * @param galleryDirName     directory name of the gallery where the picture from camera is supposed to be stored
	 * @return Uri of the file where the captured image from camera will be stored (image from gallery will return its own Uri)
	 * @throws IOException is thrown if the file creation for captured image has failed
	 */
	public static Uri launchCameraGallery(Fragment fragment, String cameraChooserTitle, String galleryDirName) throws IOException {
		Uri uri = createImageFileUri(fragment.getContext(), galleryDirName);

		if(uri == null) {
			throw new IOException();
		}

		final Intent cameraLauncher = makeCameraGalleryChooserIntent(uri, cameraChooserTitle);
		fragment.startActivityForResult(cameraLauncher, REQUEST_IMAGE_CAPTURE);

		return uri;
	}


	/**
	 * launches an app picker with all camera and gallery apps installed on the device
	 * directory name of the gallery where the picture from camera is supposed to be stored is extracted from the app package name of the app using this PhotoManager
	 *
	 * @param fragment           fragment starting the camera/gallery app
	 * @param cameraChooserTitle title of the app chooser
	 * @return Uri of the file where the captured image from camera will be stored (image from gallery will return its own Uri)
	 * @throws IOException is thrown if the file creation for captured image has failed
	 */
	public static Uri launchCameraGallery(Fragment fragment, String cameraChooserTitle) throws IOException {
		return launchCameraGallery(fragment, cameraChooserTitle, getDefaultDir(fragment.getContext()));
	}


	/**
	 * launches an app picker with all camera and gallery apps installed on the device
	 * directory name of the gallery where the picture from camera is supposed to be stored is extracted from the app package name of the app using this PhotoManager
	 *
	 * @param fragment                fragment starting the camera/gallery app
	 * @param cameraChooserTitleResId string resolution id of the title of the app chooser
	 * @return Uri of the file where the captured image from camera will be stored (image from gallery will return its own Uri)
	 * @throws IOException is thrown if the file creation for captured image has failed
	 */
	public static Uri launchCameraGallery(Fragment fragment, @StringRes int cameraChooserTitleResId) throws IOException {
		return launchCameraGallery(fragment, fragment.getString(cameraChooserTitleResId));
	}


	/**
	 * creates an app chooser for both camera and gallery apps
	 *
	 * @param outputFileUri       Uri of the file where the captured image from camera will be stored (image from gallery will return its own Uri)
	 * @param galleryChooserTitle title of the app chooser
	 * @return Intent that when run will initiate an app chooser for all camera and gallery apps installed on the device
	 */
	private static Intent makeCameraGalleryChooserIntent(Uri outputFileUri, String galleryChooserTitle) {
		Intent cameraIntent = makeCameraIntent(outputFileUri);
		Intent galleryIntent = makeGalleryIntent();
		// Only return URIs that can be opened with ContentResolver
		Intent chooserIntent = Intent.createChooser(galleryIntent, galleryChooserTitle);
		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});
		return chooserIntent;
	}


	/**
	 * Creates file where an image will be later stored and returns an Uri for this image,
	 * the file has a unique timestamp and is stored in the directory provided in the parameter
	 *
	 * @param context context of the app/activity
	 * @param dirName name of the directory where the file should be stored
	 * @return File that was created if it succeeded, null otherwise
	 * @throws IOException is thrown if the file creation was not successful because of some I/O failure
	 */
	private static Uri createImageFileUri(Context context, String dirName) throws IOException {
		return Uri.fromFile(createImageFile(context, dirName));
	}


	/**
	 * Creates file where an image will be later stored,
	 * the file has a unique timestamp and is stored in the directory named based on the app package
	 * the file is created in external storage of the phone if it is available, in the internal otherwise
	 *
	 * @param context context of the app/activity
	 * @return File that was created if it succeeded, null otherwise
	 * @throws IOException is thrown if the file creation was not successful because of some I/O failure
	 */
	private static File createImageFile(Context context) throws IOException {
		return createImageFile(context, false);
	}


	/**
	 * Creates file where an image will be later stored,
	 * the file has a unique timestamp and is stored in the directory named based on the app package
	 *
	 * @param context context of the app/activity
	 * @param createImageInCache flag if the file should be created in cache (if set to true) or in external storage (if set to false) - in external storage it will be stored permanently, in cache not
	 * @return File that was created if it succeeded, null otherwise
	 * @throws IOException is thrown if the file creation was not successful because of some I/O failure
	 */
	private static File createImageFile(Context context, boolean createImageInCache) throws IOException {
		return createImageFile(context, context.getString(context.getApplicationInfo().labelRes), createImageInCache);
	}


	/**
	 * Creates file where an image will be later stored, the file has a unique timestamp and is stored in the directory provided in the parameter
	 *
	 * @param context context of the app/activity
	 * @param dirName name of the directory where the file should be stored
	 * @param createImageInCache flag if the file should be created in cache (if set to true) or in external storage (if set to false) - in external storage it will be stored permanently, in cache not
	 * @return File that was created if it succeeded, null otherwise
	 * @throws IOException is thrown if the file creation was not successful because of some I/O failure
	 */
	private static File createImageFile(Context context, String dirName, boolean createImageInCache) throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		final File[] picturesDir;
		final String externalStorageState = Environment.getExternalStorageState();
		if(createImageInCache) {
			picturesDir = new File[1];
			if(Environment.MEDIA_MOUNTED.equals(externalStorageState)) {
				picturesDir[0] = context.getExternalCacheDir();
			} else {
				picturesDir[0] = context.getCacheDir();
			}
		} else {
			if(Environment.MEDIA_MOUNTED.equals(externalStorageState)) {
				if(isLollipopOrHigher()) {
					picturesDir = context.getExternalMediaDirs();
				} else {
					picturesDir = new File[1];
					picturesDir[0] = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
				}
			} else {
				picturesDir = new File[1];
				picturesDir[0] = context.getFilesDir();
			}
		}

		if(picturesDir.length == 0) {
			return null;
		}

		final File storageDir = new File(picturesDir[0], dirName);
		final boolean mkDirsOk = storageDir.mkdirs();
		final boolean isDir = storageDir.isDirectory();
		if(!(mkDirsOk || isDir)) {
			return null;
		}

		return File.createTempFile(
				imageFileName,  /* prefix */
				".jpg",         /* suffix */
				storageDir      /* directory */
		);
	}


	/**
	 * Creates file where an image will be later stored, the file has a unique timestamp and is stored in the directory provided in the parameter
	 *
	 * @param context context of the app/activity
	 * @param dirName name of the directory where the file should be store, the file will be created in external storage
	 * @return File that was created if it succeeded, null otherwise
	 * @throws IOException is thrown if the file creation was not successful because of some I/O failure
	 */
	private static File createImageFile(Context context, String dirName) throws IOException {
		return createImageFile(context, dirName, false);
	}


	/**
	 * checks if the device is running on at least Lollipop version of OS
	 *
	 * @return true if the device is running at least Lollipop, false otherwise
	 */
	private static boolean isLollipopOrHigher() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
	}


	/**
	 * extracts File from a Uri that is of 'file' scheme
	 *
	 * @param uri Uri with a 'file' scheme - starting 'file://'
	 * @return File extracted from Uri if it succeeds (the permission to read the Uri is granted), null otherwise
	 */
	private static File getFileFromUri(Uri uri) {
		return new File(uri.getPath());
	}


	/**
	 * loads File from Uri and returns the result in a listener callback
	 *
	 * @param context  context of the app or activity
	 * @param uri      Uri from which the File should be extracted
	 * @param listener listener to provide the resulting File
	 * @throws SecurityException is thrown if the user doesn't have a permission to read the Uri (some gallery apps don't give your app correct permission to read the file
	 *                           on the Uri - this needs to be handled on Marshmallow and newer devices to prompt the user to grant the permission
	 */
	public static void loadFileFromUri(final Context context, Uri uri, OnFileFromUriExtractedListener listener) throws SecurityException {

		//in case of the image was saved from camera
		if(uri.getScheme().equals("file")) {
			publishPhotoToSystemGallery(context, uri);
			File file = getFileFromUri(uri);
			//if the file cannot be read, there might be a problem with read permission not being granted, this can happen e.g. when the file is picked from Dropbox
			if(!file.canRead()) {
				throw new SecurityException();
			}
			if(listener != null) {
				listener.onFileFromUriExtracted(file);
			}

		} //in case the image was saved from gallery
		else if(uri.getScheme().equals("content")) {
			InputStream i = null;
			try {
				i = context.getContentResolver().openInputStream(uri);
			} catch(FileNotFoundException e) {
				e.printStackTrace();
			}
			if(i != null) {
				BufferedInputStream bufferedInputStream = new BufferedInputStream(i);
				new LoadFileFromInputStreamAsyncTask(context, listener).execute(bufferedInputStream);
			}
		}
	}


	/**
	 * publishes photo to phone's photo gallery without a need of permission
	 *
	 * @param context      context of the app/activity
	 * @param photoFileUri uri of the file that should be made public in the phone's gallery
	 */
	private static void publishPhotoToSystemGallery(Context context, Uri photoFileUri) {
		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		mediaScanIntent.setData(photoFileUri);
		context.sendBroadcast(mediaScanIntent);
	}


	/**
	 * helper method that wraps up everything that needs to be done in fragment's onActivityResult after taking a picture/picking a picture from a gallery
	 *
	 * @param fragment    fragment that calls this method
	 * @param requestCode request code with which the onActivityResult method in fragment was called
	 * @param resultCode result code with which the onActivityResult method in fragment was called
	 * @param data        data of onActivityResult in fragment
	 * @param photoUri    photo Uri - needs to be stored in the fragment and provided from the fragment if the camera was chosen because this photo Uri is then not provided in the Intent of onActivityResult,
	 *                    only gallery returns Uri in Intent of onActivityResult
	 * @param listener    callback listener that provides the file (can be null, if the user doesn't require to
	 * @return photoUri if the file can be created from uri, null if something failed or if the permission to read uri was not granted (in this case a request for permission was automatically initiated)
	 */
	@CheckResult
	public static Uri onActivityResult(Fragment fragment, int requestCode, int resultCode, Intent data, Uri photoUri, OnFileFromUriExtractedListener listener) {
		//process request normally if the result was OK, if not, delete the temp file if it was created for a photo
		if(resultCode == Activity.RESULT_OK) {
			if(requestCode == PhotoManager.REQUEST_IMAGE_CAPTURE) {

				//this happens if the picture is chosen from the gallery
				if(data != null && data.getData() != null) {
					//delete file on this Uri because a picture was chosen from gallery and therefore the temp file where the captured photo was supposed to be saved wasn't used
					if(photoUri != null && !photoUri.equals(data.getData())) {
						deleteFileForUri(photoUri);
					}
					photoUri = data.getData();
				}

				if(photoUri == null) return null;

				try {
					loadFileFromUri(fragment.getContext(), photoUri, listener);
				} catch(SecurityException e) {
					checkReadExternalStoragePermission(fragment, REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
					return null;
				}

				return photoUri;
			} else {
				return null;
			}
		} else {
			deleteFileForUri(photoUri);
			return null;
		}
	}


	/**
	 * helper method that wraps up everything that needs to be done in fragment's onActivityResult after taking a picture/picking a picture from a gallery
	 *
	 * @param fragment    fragment that calls this method
	 * @param requestCode request code with which the onActivityResult method in fragment was called
	 * @param resultCode result code with which the onActivityResult method in fragment was called
	 * @param data        data of onActivityResult in fragment
	 * @param photoUri    photo Uri - needs to be stored in the fragment and provided from the fragment if the camera was chosen because this photo Uri is then not provided in the Intent of onActivityResult,
	 *                    only gallery returns Uri in Intent of onActivityResult
	 * @return photoUri if the file can be created from uri, null if something failed or if the permission to read uri was not granted (in this case a request for permission was automatically initiated)
	 */
	@CheckResult
	public static Uri onActivityResult(Fragment fragment, int requestCode, int resultCode, Intent data, Uri photoUri) {
		return onActivityResult(fragment, requestCode, resultCode, data, photoUri, null);
	}


	/**
	 * deletes file at a given uri if the uri is not null and if the file exists
	 *
	 * @param uri uri at which the file is saved
	 * @return true if the file was successfully deleted, false otherwise
	 */
	private static boolean deleteFileForUri(Uri uri) {
		if(uri != null) {
			File file = getFileFromUri(uri);
			if(file.exists()) {
				return file.delete();
			}
		}
		return false;
	}


	/**
	 * scales and if necessary adjusts rotation an image to required width and height and returns result in the listener callback
	 * @param context context of tha app/activity
	 * @param imageFile image file that should be scaled
	 * @param reqWidth required width of the output image
	 * @param reqHeight required height of the output image
	 * @param listener listener that will be used to provide the calling fragment the resulting scaled image
	 */
	public static void scaleImageFile(final Context context, final File imageFile, int reqWidth, int reqHeight, ScaleImageAsyncTask.OnFileScaledListener listener) {
		new ScaleImageAsyncTask(context, reqWidth, reqHeight, listener).execute(imageFile);
	}


	/**
	 * gets the app package name from application info
	 *
	 * @param context context of the application/activity
	 * @return a String containing a package name of the app using this PhotoManager
	 */
	private static String getDefaultDir(Context context) {
		return context.getString(context.getApplicationInfo().labelRes);
	}


	/**
	 * Checks permission and if it is not granted, shows a dialog to deny/grant permission, if it was denied with 'don't show again' it shows a snackbar with a button to access settings
	 *
	 * @param fragment    fragment that is calling the request to check permission (this fragment should override onRequestPermissionResult()
	 * @param requestCode request code that is then used in onRequestPermissionsResult method to identify the permission
	 * @return true if the permission is already granted, false if it is not
	 */
	public static boolean checkReadExternalStoragePermission(Fragment fragment, int requestCode) {
		if(Build.VERSION.SDK_INT > 15 && ContextCompat.checkSelfPermission(fragment.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			fragment.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode);
			return false;
		} else { //the permission is granted
			return true;
		}
	}


	public interface OnFileFromUriExtractedListener {
		void onFileFromUriExtracted(File file);
	}


	//asynchronous loading of a File from InputStream
	private static class LoadFileFromInputStreamAsyncTask extends AsyncTask<BufferedInputStream, Void, File> {

		private OnFileFromUriExtractedListener mListener;
		private Context mContext;


		public LoadFileFromInputStreamAsyncTask(Context context, OnFileFromUriExtractedListener listener) {
			mListener = listener;
			mContext = context;
		}


		@Override
		protected File doInBackground(BufferedInputStream... params) {
			return getFileFromInputStream(mContext, params[0]);
		}


		@Override
		protected void onPostExecute(File file) {
			mListener.onFileFromUriExtracted(file);
		}


		/**
		 * creates a file from an inputStream
		 *
		 * @param context     context of the app/activity necessary to create a file
		 * @param inputStream input stream from which the file is supposed to be created
		 * @return file from the input stream if the process was successful, null otherwise
		 */
		private File getFileFromInputStream(Context context, BufferedInputStream inputStream) {

			try {
				File file = createImageFile(context, true);

				OutputStream output = new FileOutputStream(file);
				byte[] buffer = new byte[4 * 1024];
				int read;

				while((read = inputStream.read(buffer)) != -1) {
					output.write(buffer, 0, read);
				}
				output.flush();
				return file;

			} catch(IOException e) {
				e.printStackTrace();
			} finally {
				if(inputStream != null) {
					try {
						inputStream.close();
					} catch(IOException e) {
						e.printStackTrace();
					}
				}
			}

			return null;
		}
	}
}