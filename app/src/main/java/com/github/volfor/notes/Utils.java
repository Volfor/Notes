package com.github.volfor.notes;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.RelativeLayout;

public class Utils {

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            return contentUri.getPath();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static RelativeLayout.LayoutParams getNoteImageParams(Context context) {
        int width = context.getResources().getDisplayMetrics().widthPixels;
        width = width / 3;

        return new RelativeLayout.LayoutParams(width, width);
    }
}
