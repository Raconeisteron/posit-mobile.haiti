package org.hfoss.posit.android.api;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

public interface FindDataManagerInterface {
	public String getBase64StringFromUri(Uri uri, Context context);
	public ContentValues saveBase64StringAsUri(String base64string, Context context);
}
