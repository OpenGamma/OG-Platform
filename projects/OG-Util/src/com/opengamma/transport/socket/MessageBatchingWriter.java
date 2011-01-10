/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeDataOutputStreamWriter;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgWriter;

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
  private Queue<FudgeFieldContainer> _messages;

  public MessageBatchingWriter() {
    _out = null;
  }

  public MessageBatchingWriter(final FudgeMsgWriter out) {
    ArgumentChecker.notNull(out, "out");
    _out = out;
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

  public void write(FudgeFieldContainer message) {
    Queue<FudgeFieldContainer> messages = null;
    synchronized (this) {
      if (_writingThreadActive) {
        if (_messages != null) {
          // Another thread is already blocked, so tag onto that and return
          _messages.add(message);
          return;
        } else {
          // Another thread is already writing
          messages = new LinkedList<FudgeFieldContainer>();
          _messages = messages;
        }
      } else {
        _writingThreadActive = true;
      }
    }
    if (messages == null) {
      try {
        beforeWrite();
        getFudgeMsgWriter().writeMessage(message);
        getFudgeMsgWriter().flush();
      } finally {
        synchronized (this) {
          if (_messages != null) {
            // Another thread is blocking to be the active thread
            _writingLock.release();
          } else {
            // No other messages have been attempted
            _writingThreadActive = false;
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
        getFudgeMsgWriter().flush();
      } finally {
        synchronized (this) {
          if (_messages != null) {
            // Another thread is blocking to be the active thread
            _writingLock.release();
          } else {
            // No other messages have been attempted
            _writingThreadActive = false;
          }
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
