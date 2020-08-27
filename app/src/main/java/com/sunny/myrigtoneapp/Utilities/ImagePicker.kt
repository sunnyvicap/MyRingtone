package com.sunny.myrigtoneapp.Utilities

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ClipData
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Parcelable
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Arrays
import java.util.Date
import java.util.Locale

object ImagePicker {

    private val DEFAULT_REQUEST_CODE = 234


    val AUTHORITY = ".com.sunny.myrigtoneapp.provider"


    private var mChooserTitle: String? = null
    private var mPickImageRequestCode = DEFAULT_REQUEST_CODE
    private var mGalleryOnly = false
    private var mGalleryMultiple = false
    private var mCurrentPhotoPath: String? = null



    fun pickImage(fragment: Fragment, chooserTitle: String, requestCode: Int, galleryOnly: Boolean, galleryMultiple: Boolean) {
        mGalleryOnly = galleryOnly
        mGalleryMultiple = galleryMultiple
        mPickImageRequestCode = requestCode
        mChooserTitle = chooserTitle
        startChooser(fragment)
    }


    fun pickImage(activity: AppCompatActivity, chooserTitle: String, requestCode: Int, galleryOnly: Boolean, galleryMultiple: Boolean) {
        mGalleryOnly = galleryOnly
        mGalleryMultiple = galleryMultiple
        mPickImageRequestCode = requestCode
        mChooserTitle = chooserTitle
        startChooser(activity)
    }

    private fun startChooser(fragmentContext: Fragment) {
        val chooseImageIntent = getPickImageIntent(fragmentContext.context, mChooserTitle)
        fragmentContext.startActivityForResult(chooseImageIntent, mPickImageRequestCode)
    }

    private fun startChooser(activityContext: AppCompatActivity) {
        val chooseImageIntent = getPickImageIntent(activityContext, mChooserTitle)
        activityContext.startActivityForResult(chooseImageIntent, mPickImageRequestCode)
    }


    private fun getPickImageIntent(context: Context?, chooserTitle: String?): Intent? {
        var chooserIntent: Intent? = null


        // Check is we want gallery apps only
        if (!mGalleryOnly) {
            try {


                if (!appManifestContainsPermission(context!!, Manifest.permission.CAMERA) || hasCameraAccess(context)) {
                    val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                    val fileuri = FileProvider.getUriForFile(context, context.applicationContext.packageName + AUTHORITY, createImageFile())
                    takePhotoIntent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    takePhotoIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileuri)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        takePhotoIntent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        takePhotoIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        takePhotoIntent.putExtra("return-data", true)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val clip = ClipData.newUri(context.contentResolver, "A photo", fileuri)

                        takePhotoIntent.clipData = clip
                        takePhotoIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        takePhotoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        takePhotoIntent.putExtra("return-data", true)
                    } else {
                        val resInfoList = context.packageManager
                                .queryIntentActivities(takePhotoIntent, PackageManager.MATCH_DEFAULT_ONLY)

                        for (resolveInfo in resInfoList) {
                            val packageName = resolveInfo.activityInfo.packageName
                            context.grantUriPermission(packageName, fileuri,
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        }
                    }

                    chooserIntent = takePhotoIntent
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else if (mGalleryOnly && !mGalleryMultiple) {
            val pickIntent = Intent(Intent.ACTION_PICK)
            pickIntent.type = "image/*"
            pickIntent.action = Intent.ACTION_GET_CONTENT
            pickIntent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            pickIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

            chooserIntent = pickIntent

        } else if (mGalleryOnly && mGalleryMultiple) {

            val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

            pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            pickIntent.action = Intent.ACTION_GET_CONTENT
            pickIntent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            pickIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

            chooserIntent = pickIntent
        }



        return chooserIntent
    }

    private fun hasCameraAccess(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }


    private fun appManifestContainsPermission(context: Context, permission: String): Boolean {
        val pm = context.packageManager
        try {
            val packageInfo = pm.getPackageInfo(context.applicationContext.packageName, PackageManager.GET_PERMISSIONS)
            var requestedPermissions: Array<String>? = null
            if (packageInfo != null) {
                requestedPermissions = packageInfo.requestedPermissions
            }
            if (requestedPermissions == null) {
                return false
            }

            if (requestedPermissions.size > 0) {
                val requestedPermissionsList = Arrays.asList(*requestedPermissions)
                return requestedPermissionsList.contains(permission)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return false
    }


    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        var image: File? = null
        try {
            image = File.createTempFile(imageFileName, ".jpg", storageDir)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        assert(image != null)

        mCurrentPhotoPath = image!!.absolutePath

        return image
    }


    fun getImagePath( requestCode: Int): String? {

        return if (requestCode == mPickImageRequestCode) {

            mCurrentPhotoPath

        } else
            null
    }

    fun getImageBitmap(photoPath: String): Bitmap? {


        var bitmap: Bitmap? = null
        try {
            val f = File(photoPath)

            val exif = ExifInterface(f.path)
            val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL)

            var angle = 0

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                angle = 90
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                angle = 180
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                angle = 270
            }

            Log.e("Angle", "" + angle)
            val options = BitmapFactory.Options()
            options.inSampleSize = 3
            options.inJustDecodeBounds = false
            val mat = Matrix()
            mat.postRotate(angle.toFloat())


            val bmp = BitmapFactory.decodeStream(FileInputStream(f), null, options)


            bitmap = Bitmap.createBitmap(bmp!!, 0, 0, bmp.width, bmp.height, mat, true)


        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {

            e.printStackTrace()

        }

        return bitmap

    }

    @Throws(IOException::class)
    fun handleSamplingAndRotationBitmap(context: Context, selectedImage: Uri): Bitmap? {
        val MAX_HEIGHT = 1024
        val MAX_WIDTH = 1024

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        var imageStream = context.contentResolver.openInputStream(selectedImage)
        BitmapFactory.decodeStream(imageStream, null, options)
        imageStream!!.close()

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        imageStream = context.contentResolver.openInputStream(selectedImage)
        var img = BitmapFactory.decodeStream(imageStream, null, options)

        img = rotateImageIfRequired(context, img, selectedImage)
        return img
    }


    private fun calculateInSampleSize(options: BitmapFactory.Options,
                                      reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            val totalPixels = (width * height).toFloat()

            // Anything more than 2x the requested pixels we'll sample down further
            val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++
            }
        }
        return inSampleSize
    }


    @Throws(IOException::class)
    private fun rotateImageIfRequired(context: Context, img: Bitmap?, selectedImage: Uri): Bitmap? {
        val input = context.contentResolver.openInputStream(selectedImage)


        val ei: ExifInterface

        if (Build.VERSION.SDK_INT > 23)
            ei = ExifInterface(input)
        else
            ei = ExifInterface(selectedImage.path)


        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> return rotateImage(img, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> return rotateImage(img, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> return rotateImage(img, 270)
            else -> return img
        }
    }


    private fun rotateImage(img: Bitmap?, degree: Int): Bitmap? {
        if (degree != 0) {
            val matrix = Matrix()
            matrix.postRotate(degree.toFloat())
            val rotatedImg = Bitmap.createBitmap(img!!, 0, 0, img.width, img.height, matrix, true)
            img.recycle()
            return rotatedImg
        } else {
            return img
        }
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @SuppressLint("NewApi")
    fun getPath(context: Context, uri: Uri): String? {

        // check here to KITKAT or new version
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return (Environment.getExternalStorageDirectory().toString() + "/"
                            + split[1])
                }
            } else if (isDownloadsDocument(uri)) {

                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id))

                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                return getDataColumn(context, contentUri, selection,
                        selectionArgs)
            }// MediaProvider
            // DownloadsProvider
        } else if ("content".equals(uri.scheme!!, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(context, uri, null, null)

        } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
            return uri.path
        }// File
        // MediaStore (and general)

        return null
    }


    fun getDataColumn(context: Context, uri: Uri?,
                      selection: String?, selectionArgs: Array<String>?): String? {

        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(uri!!, projection,
                    selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }


    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri
                .authority
    }

    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri
                .authority
    }


    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri
                .authority
    }


    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri
                .authority
    }


}// not called
