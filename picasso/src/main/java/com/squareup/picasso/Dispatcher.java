/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.picasso;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

class Dispatcher {
  private static final int RETRY_DELAY = 500;
  static final int REQUEST_SUBMIT = 1;
  static final int REQUEST_COMPLETE = 2;
  static final int REQUEST_RETRY = 3;
  static final int REQUEST_FAILED = 4;
  static final int REQUEST_DECODE_FAILED = 5;

  private static final String DISPATCHER_THREAD_NAME = "Dispatcher";

  final Context context;
  final ExecutorService service;
  final Downloader downloader;
  final Map<String, Whatever> whateverMap;
  final Handler handler;
  final Handler mainThreadHandler;

  Dispatcher(Context context, ExecutorService service, Handler mainThreadHandler,
      Downloader downloader) {
    DispatcherThread thread = new DispatcherThread();
    thread.start();
    this.context = context;
    this.service = service;
    this.whateverMap = new LinkedHashMap<String, Whatever>();
    this.handler = new DispatcherHandler(thread.getLooper());
    this.mainThreadHandler = mainThreadHandler;
    this.downloader = downloader;
  }

  void dispatchSubmit(Request request) {
    handler.sendMessage(handler.obtainMessage(REQUEST_SUBMIT, request));
  }

  void cancel(Object target) {
  }

  void cancel(Object target, Uri uri) {
  }

  void dispatchComplete(Whatever whatever) {
    handler.sendMessage(handler.obtainMessage(REQUEST_COMPLETE, whatever));
  }

  void dispatchRetry(Whatever whatever) {
    handler.sendMessageDelayed(handler.obtainMessage(REQUEST_RETRY, whatever), RETRY_DELAY);
  }

  void dispatchFailed(Whatever whatever) {
    handler.sendMessage(handler.obtainMessage(REQUEST_DECODE_FAILED, whatever));
  }

  private void performSubmit(Request request) {
    Whatever whatever = whateverMap.get(request.key);
    if (whatever == null) {
      whatever = Whatever.forRequest(context, this, request, downloader);
      whatever.attach(request);
      whateverMap.put(request.key, whatever);
      service.submit(whatever);
    } else {
      whatever.attach(request);
    }
  }

  private void performRetry(Whatever whatever) {
    //
    //if (request.retryCancelled) return;=
    if (whatever.retryCount > 0) {
      whatever.retryCount--;
      service.submit(whatever);
    } else {
      performError(whatever);
    }
  }

  private void performComplete(Whatever whatever) {
    whateverMap.remove(whatever.key);
    mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(REQUEST_COMPLETE, whatever));
  }

  private void performError(Whatever whatever) {
    whateverMap.remove(whatever.key);
    mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(REQUEST_FAILED, whatever));
  }

  private class DispatcherHandler extends Handler {

    public DispatcherHandler(Looper looper) {
      super(looper);
    }

    @Override public void handleMessage(Message msg) {
      switch (msg.what) {
        case REQUEST_SUBMIT:
          Request request = (Request) msg.obj;
          performSubmit(request);
          break;
        case REQUEST_COMPLETE: {
          Whatever whatever = (Whatever) msg.obj;
          performComplete(whatever);
          break;
        }
        case REQUEST_RETRY: {
          Whatever whatever = (Whatever) msg.obj;
          performRetry(whatever);
          break;
        }
        case REQUEST_DECODE_FAILED: {
          Whatever whatever = (Whatever) msg.obj;
          performError(whatever);
          break;
        }
        default:
          throw new AssertionError("Unknown handler message received: " + msg.what);
      }
    }
  }

  static class DispatcherThread extends HandlerThread {

    DispatcherThread() {
      super(Utils.THREAD_PREFIX + DISPATCHER_THREAD_NAME, THREAD_PRIORITY_BACKGROUND);
    }
  }
}
