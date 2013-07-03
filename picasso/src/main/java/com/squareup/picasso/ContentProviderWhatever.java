package com.squareup.picasso;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import java.io.IOException;

class ContentProviderWhatever extends ContentStreamWhatever {

  ContentProviderWhatever(Dispatcher dispatcher, Request request, Context context) {
    super(dispatcher, request, context);
  }

  @Override Bitmap load(Uri uri, PicassoBitmapOptions options) throws IOException {
    options.exifRotation = Utils.getContentProviderExifRotation(context.getContentResolver(), uri);
    return super.load(uri, options);
  }
}
