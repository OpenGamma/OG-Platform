/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.wire.FudgeSize;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION)
public class MessageBatchingWriterTest {

  private static final class DelayingOutputStream extends OutputStream {

    private final Map<Thread, AtomicInteger> _writes = new HashMap<Thread, AtomicInteger>();
    private final AtomicBoolean _writing = new AtomicBoolean();

    @Override
    public void write(int b) throws IOException {
      assertFalse(_writing.getAndSet(true));
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        throw new OpenGammaRuntimeException("Interrupted", e);
      }
      AtomicInteger count = _writes.get(Thread.currentThread());
      if (count == null) {
        count = new AtomicInteger(1);
        _writes.put(Thread.currentThread(), count);
      } else {
        count.incrementAndGet();
      }
      assertTrue(_writing.getAndSet(false));
    }

  }

  private DelayingOutputStream _out;
  private MessageBatchingWriter _writer;

  @BeforeMethod
  public void init() throws IOException {
    _out = new DelayingOutputStream();
    _writer = new MessageBatchingWriter(FudgeContext.GLOBAL_DEFAULT, _out);
  }

  public void sequentialWritesNoBatching() {
    final int count = 10;
    for (int i = 0; i < count; i++) {
      _writer.write(FudgeContext.EMPTY_MESSAGE);
    }
    // Everything written from the calling thread
    assertEquals(1, _out._writes.size());
    assertNotNull(_out._writes.get(Thread.currentThread()));
    assertEquals(count * FudgeSize.calculateMessageEnvelopeSize(FudgeContext.EMPTY_MESSAGE), _out._writes.get(Thread.currentThread()).intValue());
  }

  @Test(invocationCount = 5, successPercentage = 19)
  public void concurrentWritesWithBatching() throws InterruptedException {
    final Thread[] threads = new Thread[6];
    for (int i = 0; i < threads.length; i++) {
      final int id = i;
      threads[i] = new Thread() {
        @Override
        public void run() {
          try {
            Thread.sleep(10);
          } catch (InterruptedException e) {
            fail();
          }
          switch (id) {
            case 0:
            case 1:
            case 3:
            case 4:
              threads[id + 1].start();
              break;
          }
          _writer.write(FudgeContext.EMPTY_MESSAGE);
          switch (id) {
            case 0:
              threads[3].start();
              break;
          }
        }
      };
    }
    threads[0].start();
    for (int i = 0; i < threads.length; i++) {
      threads[i].join();
    }
    // Thread 0 should have written 1 message.
    // Thread 1 should have batched up 2 messages.
    // Thread 2 should have offloaded its message to 1
    // Thread 3 should have been blocked while thread 1 was writing and then written 3 batched messages
    // Thread 4 should have offloaded its message to 3
    // Thread 5 should have offloaded its message to 3
    // System.out.println(_out._writes);
    assertEquals(3, _out._writes.size());
    assertNotNull(_out._writes.get(threads[0]));
    assertEquals(FudgeSize.calculateMessageEnvelopeSize(FudgeContext.EMPTY_MESSAGE), _out._writes.get(threads[0]).intValue());
    assertNotNull(_out._writes.get(threads[1]));
    assertEquals(2 * FudgeSize.calculateMessageEnvelopeSize(FudgeContext.EMPTY_MESSAGE), _out._writes.get(threads[1]).intValue());
    assertNull(_out._writes.get(threads[2]));
    assertNotNull(_out._writes.get(threads[3]));
    assertEquals(3 * FudgeSize.calculateMessageEnvelopeSize(FudgeContext.EMPTY_MESSAGE), _out._writes.get(threads[3]).intValue());
    assertNull(_out._writes.get(threads[4]));
    assertNull(_out._writes.get(threads[5]));
  }

}
