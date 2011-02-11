/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.connector;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.FudgeMsgReader;
import org.fudgemsg.FudgeMsgWriter;
import org.fudgemsg.FudgeRuntimeIOException;
import org.fudgemsg.FudgeStreamReader;
import org.fudgemsg.FudgeStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.transport.FudgeMessageReceiver;

/**
 * Client connection thread to interface with the C++ module. This connects to the two
 * pipe interfaces and provides user message routing.
 */
public final class Client implements Runnable {

  private static final Logger s_logger = LoggerFactory.getLogger(Client.class);

  /**
   * Represents constant state held by most clients.
   */
  public static class Context {

    private final FudgeContext _fudgeContext;
    private final ScheduledExecutorService _scheduler;
    private final ClientExecutor _executor;
    private final int _heartbeatTimeout;
    private final int _terminationTimeout;

    /* package */Context(final FudgeContext fudgeContext, final ScheduledExecutorService scheduler,
        final ClientExecutor executor, final int heartbeatTimeout, final int terminationTimeout) {
      _fudgeContext = fudgeContext;
      _scheduler = scheduler;
      _executor = executor;
      _heartbeatTimeout = heartbeatTimeout;
      _terminationTimeout = terminationTimeout;
    }

    public FudgeContext getFudgeContext() {
      return _fudgeContext;
    }

    public ScheduledExecutorService getScheduler() {
      return _scheduler;
    }

    public ExecutorService createExecutor() {
      return _executor.createClientExecutor();
    }

    public int getHeartbeatTimeout() {
      return _heartbeatTimeout;
    }

    public int getTerminationTimeout() {
      return _terminationTimeout;
    }

  }

  private final Context _context;
  private final String _inputPipeName;
  private final String _outputPipeName;
  private final BlockingQueue<FudgeMsgEnvelope> _inputMessageBuffer = new LinkedBlockingQueue<FudgeMsgEnvelope>();
  private final BlockingQueue<FudgeMsgEnvelope> _outputMessageBuffer = new LinkedBlockingQueue<FudgeMsgEnvelope>();
  private final ExecutorService _executor;

  private FudgeStreamReader _inputPipe;
  private FudgeStreamWriter _outputPipe;

  // BEGIN-ORIGINAL

  private FudgeMessageReceiver _receiver;
  private volatile boolean _poisoned;
  private FudgeFieldContainer _stash;
  private Runnable _onConnect;
  private Runnable _onDisconnect;
  private boolean _running;

  // END-ORIGINAL

  /* package */Client(final Context context, final String inputPipeName, final String outputPipeName) {
    _context = context;
    _executor = context.createExecutor();
    _inputPipeName = inputPipeName;
    _outputPipeName = outputPipeName;
  }

  private Context getContext() {
    return _context;
  }

  private String getInputPipeName() {
    return _inputPipeName;
  }

  private String getOutputPipeName() {
    return _outputPipeName;
  }

  private ExecutorService getExecutor() {
    return _executor;
  }

  private FudgeStreamReader getInputPipe() {
    return _inputPipe;
  }

  private FudgeStreamWriter getOutputPipe() {
    return _outputPipe;
  }

  private BlockingQueue<FudgeMsgEnvelope> getInputMessageBuffer() {
    return _inputMessageBuffer;
  }

  private BlockingQueue<FudgeMsgEnvelope> getOutputMessageBuffer() {
    return _outputMessageBuffer;
  }

  /**
   * The main connection thread must always be in "READ" mode to avoid deadlocking against the
   * alternating C/C++ one. Therefore we have a separate dispatch thread (or pool of them) for
   * when messages arrive.
   */
  @Override
  public void run() {
    if (!connectPipes()) {
      s_logger.error("Couldn't connect to client");
      return;
    }
    final Runnable poison = new Runnable() {
      @Override
      public void run() {
        s_logger.info("Queuing poison message and disconnecting pipes");
        // Poison the other threads with empty Fudge message
        _poisoned = true;
        getInputMessageBuffer().add(FudgeContext.EMPTY_MESSAGE_ENVELOPE);
        getOutputMessageBuffer().add(FudgeContext.EMPTY_MESSAGE_ENVELOPE);
        disconnectPipes();
      }
    };
    final Watchdog watchdog = new Watchdog(poison);
    // Message sender - writes outgoing messages to C++
    final Thread sender = new Thread() {
      private final FudgeMsgWriter _writer = new FudgeMsgWriter(getOutputPipe());

      @Override
      public void run() {
        s_logger.info("Starting message writer thread");
        while (true) {
          FudgeMsgEnvelope msg;
          try {
            s_logger.debug("Waiting for message to write");
            msg = getOutputMessageBuffer().take();
          } catch (InterruptedException e) {
            s_logger.warn("Interrupted receiving message from output queue");
            continue;
          }
          if (msg.getMessage().getNumFields() > 0) {
            try {
              _writer.writeMessageEnvelope(msg);
            } catch (Throwable t) {
              s_logger.error("Exception during message write", t);
            }
          } else {
            s_logger.info("Poison message found on output queue");
            getOutputMessageBuffer().add(msg);
            break;
          }
        }
        s_logger.info("Message writer thread terminated");
      }

    };
    sender.setName(Thread.currentThread().getName() + "-Writer");
    sender.start();
    synchronized (this) {
      _running = true;
    }
    // TODO: call the on-connect handler(s)
    final ScheduledFuture<?> watchdogFuture = getContext().getScheduler().scheduleWithFixedDelay(watchdog,
        getContext().getHeartbeatTimeout(), getContext().getHeartbeatTimeout() * 2, TimeUnit.MILLISECONDS);
    try {
      final FudgeMsgReader reader = new FudgeMsgReader(getInputPipe());
      s_logger.info("Starting message read loop");
      messageLoop: while (reader.hasNext()) {
        final FudgeMsgEnvelope messageEnvelope = reader.nextMessageEnvelope();
        watchdog.stillAlive();
        s_logger.debug("message envelope read: {}", messageEnvelope);
        switch (messageEnvelope.getProcessingDirectives()) {
          case MessageDirectives.USER:
            s_logger.error("TODO: handle user message {}", messageEnvelope.getMessage());
            // TODO: handle the user message
            //getExecutor().execute(dispatchUserMessage(messageEnvelope));
            break;
          case MessageDirectives.CLIENT:
            s_logger.error("TODO: handle client message {}", messageEnvelope.getMessage());
            // TODO: handle the client message
            /*
            if (!pluginConnectorMessage(new ConnectorMessage(messageEnvelope.getMessage()))) {
              break messageLoop;
            }
            */
            break;
          default:
            s_logger.warn("Unexpected processing directive {}", messageEnvelope.getProcessingDirectives());
            break;
        }
        s_logger.debug("Waiting for Fudge message");
      }
    } catch (FudgeRuntimeIOException e) {
      s_logger.warn("Error reading message {}", e.toString());
    }
    poison.run();
    synchronized (this) {
      _running = false;
    }
    // TODO: call the on-disconnect handler(s)
    getExecutor().shutdown();
    watchdogFuture.cancel(true);
    try {
      s_logger.debug("Joining thread(s)");
      sender.join();
      if (!getExecutor().awaitTermination(getContext().getTerminationTimeout(), TimeUnit.MILLISECONDS)) {
        dumpThreads();
      }
      getInputMessageBuffer().clear();
      getOutputMessageBuffer().clear();
    } catch (InterruptedException e) {
      s_logger.warn("Interrupted joining threads, {}", e.toString());
    }
  }

  private static class In extends InputStream {

    private final InputStream _underlying;

    public In(final InputStream underlying) {
      _underlying = underlying;
    }

    @Override
    public int read() throws IOException {
      s_logger.debug("READING");
      int c = _underlying.read();
      s_logger.debug("READ {}", c);
      return c;
    }

  }

  private boolean connectPipes() {
    s_logger.debug("Connecting to input pipe: {}", getInputPipeName());
    try {
      _inputPipe = getContext().getFudgeContext().createReader(
          (DataInput) new DataInputStream(new BufferedInputStream(new In(new FileInputStream(getInputPipeName())))));
    } catch (FileNotFoundException e) {
      s_logger.warn("Couldn't connect to pipe: {} ({})", getInputPipeName(), e.toString());
      return false;
    }
    s_logger.debug("Connecting to output pipe: {}", getOutputPipeName());
    try {
      _outputPipe = getContext().getFudgeContext().createWriter(
          (DataOutput) new DataOutputStream(new BufferedOutputStream(new FileOutputStream(getOutputPipeName()))));
    } catch (FileNotFoundException e) {
      s_logger.warn("Couldn't connect to pipe: {} ({})", getOutputPipeName(), e.toString());
    }
    if ((_inputPipe != null) && (_outputPipe != null)) {
      return true;
    } else {
      disconnectPipes();
      return false;
    }
  }

  private void disconnectPipes() {
    FudgeStreamReader in = getInputPipe();
    _inputPipe = null;
    FudgeStreamWriter out = getOutputPipe();
    _outputPipe = null;
    if (in != null) {
      try {
        s_logger.debug("Closing input pipe");
        in.close();
      } catch (FudgeRuntimeIOException e) {
        s_logger.warn("Error closing input pipe: {}", e.toString());
      }
    }
    if (out != null) {
      try {
        s_logger.debug("Closing output pipe");
        out.close();
      } catch (FudgeRuntimeIOException e) {
        s_logger.warn("Error closing output pipe: {}", e.toString());
      }
    }
  }

  private static void dumpThreads() {
    final Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
    for (Map.Entry<Thread, StackTraceElement[]> entry : stacks.entrySet()) {
      System.out.println("Thread: " + entry.getKey());
      for (StackTraceElement frame : entry.getValue()) {
        System.out.println("\t" + frame);
      }
    }
  }

  // ORIGINAL

  /*
  private boolean pluginConnectorMessage(final PluginConnectorMessage message) {
    switch (message.getOperation()) {
      case HEARTBEAT:
        s_logger.debug("Heartbeat message received");
        final PluginConnectorMessage msg = new PluginConnectorMessage(Operation.HEARTBEAT);
        getOutputMessageBuffer().add(
            new FudgeMsgEnvelope(msg.toFudgeMsg(getFudgeContext()), 0, FudgeProcessingDirectives.PLUGIN_CONNECTOR));
        if (message.getStash() != null) {
          synchronized (this) {
            // First heartbeat may contain the stash message
            _stash = message.getStash();
          }
        }
        return true;
      case POISON:
        s_logger.info("Poison message received");
        return false;
      case STASH:
        s_logger.debug("Stashed data received");
        // CSOFF: checkstyle gets this wrong
        synchronized (this) {
          if (message.getStash() != null) {
            _stash = message.getStash();
          } else {
            _stash = FudgeContext.EMPTY_MESSAGE;
          }
          notifyAll();
        }
        // CSON: checkstyle gets this wrong
        return true;
      default:
        s_logger.warn("Unexpected connector message {}", message.getOperation());
        return true;
    }
  }

  private Runnable dispatchUserMessage(final FudgeMsgEnvelope msg) {
    return new Runnable() {
      @Override
      public void run() {
        s_logger.debug("Dispatching user message {}", msg.getMessage());
        final FudgeMessageReceiver receiver = getMessageReceiver();
        if (receiver != null) {
          receiver.messageReceived(getFudgeContext(), msg);
        } else {
          s_logger.warn("No receiver to dispatch message {} to", msg.getMessage());
        }
      }
    };
  }

  public FudgeMessageSender getMessageSender() {
    return new FudgeMessageSender() {
      @Override
      public void send(final FudgeFieldContainer message) {
        s_logger.debug("Sending {} to output stream", message);
        if (_poisoned) {
          s_logger.warn("Connector has been poisoned - not sending {}", message);
          throw new OpenGammaRuntimeException("Connection has been closed - message not sent");
        } else {
          getOutputMessageBuffer().add(new FudgeMsgEnvelope(message, 0, FudgeProcessingDirectives.STANDARD_DELIVERY));
        }
      }

      @Override
      public FudgeContext getFudgeContext() {
        return PluginConnector.this.getFudgeContext();
      }
    };
  }

  public synchronized void setMessageReceiver(final FudgeMessageReceiver receiver) {
    _receiver = receiver;
  }

  public synchronized FudgeMessageReceiver getMessageReceiver() {
    return _receiver;
  }
  */

  /**
   * If a message was stashed by setStashedMessage in this JVM instance or a previous one, returns it. Returns
   * the empty message if there was none.
   * 
   * @return the message
   */
  /*
  public synchronized FudgeFieldContainer getStashedMessage() {
    if (_stash == null) {
      final PluginConnectorMessage stash = new PluginConnectorMessage(Operation.STASH);
      s_logger.debug("Sending STASH fetch request");
      getOutputMessageBuffer().add(
          new FudgeMsgEnvelope(stash.toFudgeMsg(getFudgeContext()), 0, FudgeProcessingDirectives.PLUGIN_CONNECTOR));
      try {
        wait(getOperationTimeout());
      } catch (InterruptedException e) {
        s_logger.warn("Interrupted waiting on STASH");
      }
      if (_stash == null) {
        s_logger.warn("Timeout before STASH response received");
      }
    }
    return _stash;
  }
  */

  /**
   * Stores a small amount of state with the underlying connector in order to survive a JVM restart. Do not
   * pass {@code null} to clear any state - send an empty message.
   * 
   * @param message the message
   */
  /*
  public synchronized void setStashedMessage(final FudgeFieldContainer message) {
    ArgumentChecker.notNull(message, "message");
    final PluginConnectorMessage stash = new PluginConnectorMessage(Operation.STASH);
    stash.setStash(message);
    s_logger.debug("Sending STASH put request {}", message);
    getOutputMessageBuffer().add(
        new FudgeMsgEnvelope(stash.toFudgeMsg(getFudgeContext()), 0, FudgeProcessingDirectives.PLUGIN_CONNECTOR));
    _stash = message;
  }
  */

  /*
  public synchronized void setOnConnectCallback(final Runnable onConnect) {
    _onConnect = onConnect;
  }

  public synchronized Runnable getOnConnectCallback() {
    return _onConnect;
  }

  public synchronized boolean setOnDisconnectCallback(final Runnable onDisconnect) {
    if (_running) {
      _onDisconnect = onDisconnect;
      return true;
    } else {
      return false;
    }
  }

  public synchronized Runnable getOnDisconnectCallback() {
    return _onDisconnect;
  }

  private void onConnect(final ExecutorService executor) {
    Runnable onConnect = getOnConnectCallback();
    if (onConnect != null) {
      s_logger.debug("Calling onConnect listener");
      executor.execute(onConnect);
    }
  }

  private void onDisconnect(final ExecutorService executor) {
    Runnable onDisconnect = getOnDisconnectCallback();
    if (onDisconnect != null) {
      s_logger.debug("Calling onDisconnect listener");
      executor.execute(onDisconnect);
    }
  }
  */

}
