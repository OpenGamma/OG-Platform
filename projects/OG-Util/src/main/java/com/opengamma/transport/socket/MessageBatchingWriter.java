/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.LockSupport;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.wire.FudgeDataOutputStreamWriter;
import org.fudgemsg.wire.FudgeMsgWriter;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * Allows messages being written concurrently to be batched onto a smaller number of threads.
 * If a thread is already writing messages, the next thread will be blocked and batch all
 * further messages allowing other threads to complete quickly. This reduces the number of
 * threads created but blocked (i.e. consuming memory resources) when processing a large
 * number of incoming requests concurrently.
 */
public class MessageBatchingWriter {

  private final Semaphore _writingLock = new Semaphore(0);
  private FudgeMsgWriter _out;

  private boolean _writingThreadActive;
  private boolean _flushRequired;
  private Queue<FudgeMsg> _messages;
  private long _nanoFlushDelay;

  public MessageBatchingWriter() {
    _out = null;
  }

  public MessageBatchingWriter(final FudgeMsgWriter out) {
    ArgumentChecker.notNull(out, "out");
    _out = out;
  }

  public void setFlushDelay(final int microseconds) {
    _nanoFlushDelay = microseconds * 1000;
  }

  private static FudgeMsgWriter createFudgeMsgWriter(final FudgeContext fudgeContext, final OutputStream out) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(out, "out");
    final FudgeDataOutputStreamWriter writer = new FudgeDataOutputStreamWriter(fudgeContext, out);
    writer.setFlushOnEnvelopeComplete(false);
    return new FudgeMsgWriter(writer);
  }

  public MessageBatchingWriter(final FudgeContext fudgeContext, final OutputStream out) {
    this(createFudgeMsgWriter(fudgeContext, out));
  }

  public void setFudgeMsgWriter(final FudgeMsgWriter out) {
    _out = out;
  }

  public void setFudgeMsgWriter(final FudgeContext fudgeContext, final OutputStream out) {
    setFudgeMsgWriter(createFudgeMsgWriter(fudgeContext, out));
  }

  protected FudgeMsgWriter getFudgeMsgWriter() {
    return _out;
  }

  public void write(FudgeMsg message) {
    Queue<FudgeMsg> messages = null;
    synchronized (this) {
      if (_writingThreadActive) {
        if (_messages != null) {
          // Another thread is already blocked, so tag onto that and return
          _messages.add(message);
          return;
        } else {
          // Another thread is already writing
          messages = new LinkedList<FudgeMsg>();
          _messages = messages;
        }
      } else {
        _writingThreadActive = true;
      }
    }
    boolean waitForOtherThreads = false;
    if (messages == null) {
      try {
        beforeWrite();
        getFudgeMsgWriter().writeMessage(message);
      } finally {
        synchronized (this) {
          if (_messages != null) {
            // Another thread is blocking to be the active thread
            _writingLock.release();
          } else {
            // No other messages have been attempted
            _writingThreadActive = false;
            if (_nanoFlushDelay > 0) {
              if (!_flushRequired) {
                waitForOtherThreads = true;
                _flushRequired = true;
              }
            } else {
              final FudgeMsgWriter writer = getFudgeMsgWriter();
              if (writer != null) {
                writer.flush();
              }
            }
          }
        }
      }
    } else {
      try {
        _writingLock.acquire();
      } catch (InterruptedException e) {
        throw new OpenGammaRuntimeException("Interrupted", e);
      }
      synchronized (this) {
        // Stop other threads from queuing onto us
        _messages = null;
      }
      try {
        beforeWrite();
        do {
          getFudgeMsgWriter().writeMessage(message);
          message = messages.poll();
        } while (message != null);
      } finally {
        synchronized (this) {
          if (_messages != null) {
            // Another thread is blocking to be the active thread
            _writingLock.release();
          } else {
            // No other messages have been attempted
            _writingThreadActive = false;
            if (_nanoFlushDelay > 0) {
              if (!_flushRequired) {
                waitForOtherThreads = true;
                _flushRequired = true;
              }
            } else {
              getFudgeMsgWriter().flush();
            }
          }
        }
      }
    }
    // TODO: it would be better if this could be offloaded to another thread so that
    // we don't block the caller and only have one thread doing the park.
    if (waitForOtherThreads) {
      // Can't reliably do a sub-millisecond precision sleep, so use park
      LockSupport.parkNanos(_nanoFlushDelay);
      synchronized (this) {
        if (_flushRequired) {
          if (!_writingThreadActive) {
            // No other threads have become active to write data so flush
            getFudgeMsgWriter().flush();
          }
          _flushRequired = false;
        }
      }
    }
  }

  /**
   * Called before a message (or batch of messages) will be written by this writer. Use this
   * to e.g. initialize or create the FudgeMsgWriter object.
   */
  protected void beforeWrite() {
  }

}
