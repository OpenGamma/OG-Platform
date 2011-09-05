/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.async;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.concurrent.atomic.AtomicBoolean;

import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.test.Timeout;

/**
 * Tests the {@link AsynchronousOperation} and related classes.
 */
@Test
public class AsynchronousOperationTest {

  private static final String RESULT = "Foo";

  private void asyncTask(final ResultCallback<String> callback, final boolean result) {
    if (result) {
      callback.setResult(RESULT);
    } else {
      callback.setException(new OpenGammaRuntimeException("Exception"));
    }
  }

  private String immediateSignal(final boolean result) throws AsynchronousExecution {
    final AsynchronousOperation<String> operation = new AsynchronousOperation<String>();
    asyncTask(operation.getCallback(), result);
    return operation.getResult();
  }

  public void testResultAvailable() throws AsynchronousExecution {
    assertEquals(immediateSignal(true), RESULT);
  }

  @Test(expectedExceptions = {OpenGammaRuntimeException.class })
  public void testExceptionAvailable() throws AsynchronousExecution {
    immediateSignal(false);
  }

  private void deferredSignal(final boolean listenerFirst, final boolean result) {
    final AsynchronousOperation<String> operation = new AsynchronousOperation<String>();
    try {
      operation.getResult();
      fail();
    } catch (AsynchronousExecution async) {
      final AtomicBoolean flag = new AtomicBoolean(false);
      if (!listenerFirst) {
        asyncTask(operation.getCallback(), result);
      }
      async.setResultListener(new ResultListener<String>() {
        @Override
        public void operationComplete(final AsynchronousResult<String> r) {
          if (listenerFirst) {
            assertTrue(flag.get());
          } else {
            assertFalse(flag.get());
          }
          if (result) {
            assertEquals(r.getResult(), RESULT);
          } else {
            try {
              r.getResult();
              fail();
            } catch (OpenGammaRuntimeException e) {
              // ignore
            }
          }
          if (!listenerFirst) {
            flag.set(true);
          }
        }
      });
      if (listenerFirst) {
        flag.set(true);
        asyncTask(operation.getCallback(), result);
      } else {
        assertTrue(flag.get());
      }
    }
  }

  public void testResultDeferredListenerFirst() {
    deferredSignal(true, true);
  }

  public void testExceptionDeferredListenerFirst() {
    deferredSignal(true, false);
  }

  public void testResultDeferredListenerSecond() {
    deferredSignal(false, true);
  }

  public void testExceptionDeferredListenerSecond() {
    deferredSignal(false, false);
  }

  private String blockingCall(final boolean result) throws InterruptedException {
    final AsynchronousOperation<String> operation = new AsynchronousOperation<String>();
    new Thread() {
      @Override
      public void run() {
        try {
          Thread.sleep(Timeout.standardTimeoutMillis());
        } catch (InterruptedException e) {
          // Ignore
        }
        asyncTask(operation.getCallback(), result);
      }
    }.start();
    try {
      operation.getResult();
      fail();
      return null;
    } catch (AsynchronousExecution async) {
      return async.getResult();
    }
  }

  public void testResultBlocking() throws InterruptedException {
    assertEquals(blockingCall(true), RESULT);
  }

  @Test(expectedExceptions = {OpenGammaRuntimeException.class })
  public void testExceptionBlocking() throws InterruptedException {
    blockingCall(false);
  }

}