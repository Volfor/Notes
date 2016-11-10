package com.github.volfor.notes;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileInputStream;

import timber.log.Timber;

public class Utils {

    public static String getImagePathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static void setMediaPlayerDataSource(Context context, MediaPlayer mp, String fileInfo) throws Exception {
        if (fileInfo.startsWith("content://")) {
            try {
                Uri uri = Uri.parse(fileInfo);
                fileInfo = getAudioPathFromContentUri(context, uri);
            } catch (Exception e) {
                Timber.e(e, e.getMessage());
            }
        }

        try {
            mp.reset();
            mp.setDataSource(context, Uri.parse(Uri.encode(fileInfo)));
        } catch (Exception e) {
            try {
                File file = new File(fileInfo);
                FileInputStream inputStream = new FileInputStream(file);
                mp.reset();
                mp.setDataSource(inputStream.getFD());
                inputStream.close();
            } catch (Exception ee) {
                String uri = getAudioUriFromPath(context, fileInfo);
                mp.reset();
                mp.setDataSource(uri);
            }
        }
    }

    private static String getAudioUriFromPath(Context context, String path) {
        Uri ringtonesUri = MediaStore.Audio.Media.getContentUriForPath(path);
        Cursor ringtoneCursor = context.getContentResolver().query(
                ringtonesUri, null,
                MediaStore.Audio.Media.DATA + "='" + path + "'", null, null);
        ringtoneCursor.moveToFirst();

        long id = ringtoneCursor.getLong(ringtoneCursor
                .getColumnIndex(MediaStore.Audio.Media._ID));
        ringtoneCursor.close();

        if (!ringtonesUri.toString().endsWith(String.valueOf(id))) {
            return ringtonesUri + "/" + id;
        }
        return ringtonesUri.toString();
    }

    public static String getAudioPathFromContentUri(Context context, Uri contentUri) throws Exception {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor ringtoneCursor = context.getContentResolver().query(contentUri, proj, null, null, null);

        ringtoneCursor.moveToFirst();
        String path = ringtoneCursor.getString(ringtoneCursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
        ringtoneCursor.close();

        return path;
    }

    public static RelativeLayout.LayoutParams getNoteImageParams(Context context) {
        int width = context.getResources().getDisplayMetrics().widthPixels;
        width = width / 3;

        return new RelativeLayout.LayoutParams(width, width);
    }
}
