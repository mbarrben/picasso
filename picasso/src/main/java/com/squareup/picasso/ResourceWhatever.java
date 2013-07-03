package com.squareup.picasso;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import java.io.IOException;

import static com.squareup.picasso.Utils.calculateInSampleSize;

class ResourceWhatever extends Whatever {

  private final int resourceId;
  private final Context context;

  ResourceWhatever(Dispatcher dispatcher, Request request, Context context) {
    super(dispatcher, request);
    this.resourceId = request.resourceId;
    this.context = context;
  }

  @Override Bitmap load(Uri uri, PicassoBitmapOptions options) throws IOException {
    return decodeResource(context.getResources(), resourceId, options);
  }

  Bitmap decodeResource(Resources resources, int resourceId, PicassoBitmapOptions bitmapOptions) {
    if (bitmapOptions != null && bitmapOptions.inJustDecodeBounds) {
      BitmapFactory.decodeResource(resources, resourceId, bitmapOptions);
      calculateInSampleSize(bitmapOptions);
    }
    return BitmapFactory.decodeResource(resources, resourceId, bitmapOptions);
  }
}
