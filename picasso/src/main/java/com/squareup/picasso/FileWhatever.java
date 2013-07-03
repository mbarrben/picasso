package com.squareup.picasso;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import java.io.IOException;

class FileWhatever extends ContentStreamWhatever {

  FileWhatever(Dispatcher dispatcher, Request request, Context context) {
    super(dispatcher, request, context);
  }

  @Override Bitmap load(Uri uri, PicassoBitmapOptions options) throws IOException {
    options.exifRotation = Utils.getFileExifRotation(uri.getPath());
    return super.load(uri, options);
  }
}
