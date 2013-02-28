/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import java.nio.channels.Channels;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.wire.FudgeMsgReader;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.fudgemsg.wire.FudgeRuntimeIOException;
import org.fudgemsg.wire.FudgeStreamReader;
import org.fudgemsg.wire.FudgeStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.language.connector.ConnectorMessage.Operation;
import com.opengamma.language.context.MutableSessionContext;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.context.SessionContextInitializationEventHandler;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.async.AsynchronousResult;
import com.opengamma.util.async.ResultListener;

/**
 * Client connection thread to interface with the C++ module. This connects to the two pipe interfaces and provides user message routing.
 */
public class Client implements Runnable {

  private static final Logger s_logger = LoggerFactory.getLogger(Client.class);

  private final ClientContext _clientContext;
  private final SessionContext _sessionContext;
  private final String _inputPipeName;
  private final String _outputPipeName;
  private final BlockingQueue<FudgeMsgEnvelope> _outputMessageBuffer = new LinkedBlockingQueue<FudgeMsgEnvelope>();
  private final ExecutorService _executor;

  private FudgeStreamReader _inputPipe;
  private FudgeStreamWriter _outputPipe;
  private volatile boolean _poisoned;
  private FudgeMsg _stashMessage;

  protected Client(final ClientContext clientContext, final String inputPipeName, final String outputPipeName, final SessionContext session) {
    _clientContext = clientContext;
    _sessionContext = session;
    _executor = clientContext.createExecutor();
    _inputPipeName = inputPipeName;
    _outputPipeName = outputPipeName;
  }

  protected ClientContext getClientContext() {
    return _clientContext;
  }

  protected SessionContext getSessionContext() {
    return _sessionContext;
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

  private BlockingQueue<FudgeMsgEnvelope> getOutputMessageBuffer() {
    return _outputMessageBuffer;
  }

  private Runnable createPoisoner() {
    return new Runnable() {
      @Override
      public void run() {
        doPoison();
      }
    };
  }

  private Runnable createMessageWriter() {
    return new Runnable() {
      private final FudgeMsgWriter _writer = new FudgeMsgWriter(getOutputPipe());

      @Override
      public void run() {
        s_logger.info("Starting message writer thread");
        while (true) {
          FudgeMsgEnvelope msg;
          try {
            s_logger.debug("Waiting for message to write");
            msg = getOutputMessageBuffer().take();
          } catch (final InterruptedException e) {
            s_logger.warn("Interrupted receiving message from output queue");
            continue;
          }
          if (msg.getMessage().getNumFields() > 0) {
            try {
              s_logger.debug("Writing message {}", msg);
              _writer.writeMessageEnvelope(msg);
            } catch (final Throwable t) {
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
  }

  protected void sendUserMessage(final UserMessage message) {
    sendUserMessage(message.toFudgeMsg(new FudgeSerializer(getClientContext().getFudgeContext())));
  }

  protected void sendUserMessage(final FudgeMsg msg) {
    getOutputMessageBuffer().add(new FudgeMsgEnvelope(msg, 0, MessageDirectives.USER));
  }

  protected SessionContextInitializationEventHandler getSessionInitializer() {
    return new SessionContextInitializationEventHandler() {

      @Override
      public void initContext(final MutableSessionContext context) {
        context.setMessageSender(new MessageSender() {

          @Override
          public UserMessagePayload call(final UserMessagePayload message, final long timeoutMillis)
              throws TimeoutException {
            // TODO: implement this if/when we need it
            throw new UnsupportedOperationException();
          }

          @Override
          public long getDefaultTimeout() {
            return getClientContext().getMessageTimeout();
          }

          @Override
          public void send(final UserMessagePayload payload) {
            sendUserMessage(new UserMessage(payload));
          }

          @Override
          public void sendAndWait(final UserMessagePayload message, final long timeoutMillis) throws TimeoutException {
            // TODO: implement this if/when we need it
            throw new UnsupportedOperationException();
          }

        });
        context.setStashMessage(new StashMessage() {
          @Override
          public FudgeMsg get() {
            return getStashMessage();
          }

          @Override
          public void put(final FudgeMsg message) {
            setStashMessage(message);
          }
        });
      }

      @Override
      public void initContextWithStash(final MutableSessionContext context, final FudgeMsg stash) {
        initContext(context);
      }

    };
  }

  private void initializeContext(final FudgeMsg stash) {
    s_logger.info("Initializing session context");
    synchronized (this) {
      _stashMessage = stash;
    }
    if (stash != null) {
      getSessionContext().initContextWithStash(getSessionInitializer(), stash);
    } else {
      getSessionContext().initContext(getSessionInitializer());
    }
    s_logger.debug("Session context initialized");
  }

  /**
   * The main connection thread must always be in "READ" mode to avoid deadlocking against the alternating C/C++ one. Therefore we have a separate dispatch thread (or pool of them) for when messages
   * arrive.
   */
  @Override
  public final void run() {
    if (!connectPipes()) {
      s_logger.error("Couldn't connect to client");
      return;
    }
    final Runnable poison = createPoisoner();
    final Watchdog watchdog = new Watchdog(poison);
    // Message sender - writes outgoing messages to C++
    final Thread sender = new Thread(createMessageWriter());
    sender.setName(Thread.currentThread().getName() + "-Writer");
    sender.start();
    // The "termination" timeout is used for the startup delay before the watchdog thread starts as the first connection can
    // be rather slow while waiting (for example) for the function list to be built if it is not already cached
    final ScheduledFuture<?> watchdogFuture = getClientContext().getHousekeepingScheduler().scheduleWithFixedDelay(watchdog,
        getClientContext().getTerminationTimeout(), getClientContext().getHeartbeatTimeout() * 2, TimeUnit.MILLISECONDS);
    // Main read loop - this thread is always available to read to avoid process deadlock. The loop will
    // abort on I/O error or if the watchdog fires (triggering an I/O error)
    try {
      boolean contextInitialized = false;
      Queue<Runnable> deferredMessages = null;
      @SuppressWarnings("resource" /* pipe gets closed during disconnect call, don't want it closed when reader falls out of scope */)
      final FudgeMsgReader reader = new FudgeMsgReader(getInputPipe());
      final FudgeDeserializer deserializer = new FudgeDeserializer(getClientContext().getFudgeContext());
      s_logger.info("Starting message read loop");
      while (!_poisoned && reader.hasNext()) {
        final FudgeMsgEnvelope messageEnvelope = reader.nextMessageEnvelope();
        watchdog.stillAlive();
        switch (messageEnvelope.getProcessingDirectives()) {
          case MessageDirectives.USER: {
            final Runnable dispatch = dispatchUserMessage(messageEnvelope.getMessage());
            if (deferredMessages == null) {
              getExecutor().execute(dispatch);
            } else {
              synchronized (deferredMessages) {
                if (deferredMessages.isEmpty()) {
                  s_logger.debug("Dispatching post-initialisation user message");
                  getExecutor().execute(dispatch);
                  deferredMessages = null;
                } else {
                  s_logger.debug("Deferring user message dispatch until after context initialisation");
                  deferredMessages.add(dispatch);
                }
              }
            }
            break;
          }
          case MessageDirectives.CLIENT: {
            final ConnectorMessage message = new ConnectorMessage(deserializer, messageEnvelope.getMessage());
            switch (message.getOperation()) {
              case HEARTBEAT:
                if (getOutputMessageBuffer().isEmpty()) {
                  s_logger.debug("Sending heartbeat");
                  getOutputMessageBuffer().add(getClientContext().getHeartbeatMessage());
                } else {
                  s_logger.debug("Ignoring heartbeat request - other messages pending");
                }
                if (!contextInitialized) {
                  deferredMessages = new LinkedList<Runnable>();
                  initializeContext(message.getStash(), deferredMessages);
                  contextInitialized = true;
                }
                break;
              case POISON:
                s_logger.info("Poison message received");
                poison.run();
                break;
              default:
                s_logger.warn("Unexpected connector operation {}", message.getOperation());
                break;
            }
            break;
          }
          default:
            s_logger.warn("Unexpected processing directive {}", messageEnvelope.getProcessingDirectives());
            break;
        }
        s_logger.debug("Waiting for Fudge message");
      }
    } catch (final FudgeRuntimeIOException e) {
      s_logger.warn("Error reading message {}", e.toString());
      s_logger.debug("Caught exception", e);
    } catch (final Throwable t) {
      s_logger.error("Unexpected exception thrown during read loop", t);
      s_logger.debug("Caught exception", t);
    }
    watchdogFuture.cancel(true);
    // Shutdown (poison may have already been called by a watchdog - no harm in calling it twice though)
    poison.run();
    getExecutor().shutdown();
    try {
      s_logger.info("Waiting for client thread(s) to terminate");
      sender.join();
      if (!getExecutor().awaitTermination(getClientContext().getTerminationTimeout(), TimeUnit.MILLISECONDS)) {
        dumpThreads();
      }
      s_logger.debug("Client thread(s) terminated");
    } catch (final InterruptedException e) {
      s_logger.warn("Interrupted joining threads, {}", e.toString());
    }
    s_logger.info("Destroying session context");
    getSessionContext().doneContext();
    s_logger.debug("Session context destroyed");
  }

  private boolean connectPipes() {
    s_logger.debug("Connecting to input pipe: {}", getInputPipeName());
    try {
      // Go via the File channel so that it is interruptible. This might not be necessary on all JVMs
      // but just using a FileInputStream was not releasing the blocked reader thread on my Linux workstation
      // at pipe closure.
      _inputPipe = getClientContext().getFudgeContext().createReader(
          (DataInput) new DataInputStream(new BufferedInputStream(new InputStreamWrapper(Channels.newInputStream(new FileInputStream(getInputPipeName()).getChannel())))));
    } catch (final FileNotFoundException e) {
      s_logger.warn("Couldn't connect to pipe: {} ({})", getInputPipeName(), e.toString());
      return false;
    }
    s_logger.debug("Connecting to output pipe: {}", getOutputPipeName());
    try {
      _outputPipe = getClientContext().getFudgeContext().createWriter((DataOutput) new DataOutputStream(new BufferedOutputStream(new FileOutputStream(getOutputPipeName()))));
    } catch (final FileNotFoundException e) {
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
    final FudgeStreamReader in = getInputPipe();
    _inputPipe = null;
    final FudgeStreamWriter out = getOutputPipe();
    _outputPipe = null;
    if (in != null) {
      try {
        s_logger.debug("Closing input pipe");
        in.close();
      } catch (final FudgeRuntimeIOException e) {
        s_logger.warn("Error closing input pipe: {}", e.toString());
      }
    }
    if (out != null) {
      try {
        s_logger.debug("Closing output pipe");
        out.close();
      } catch (final FudgeRuntimeIOException e) {
        s_logger.warn("Error closing output pipe: {}", e.toString());
      }
    }
  }

  private static void dumpThreads() {
    final Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
    for (final Map.Entry<Thread, StackTraceElement[]> entry : stacks.entrySet()) {
      System.out.println("Thread: " + entry.getKey());
      for (final StackTraceElement frame : entry.getValue()) {
        System.out.println("\t" + frame);
      }
    }
  }

  private Runnable dispatchUserMessage(final FudgeMsg msg) {
    return new Runnable() {
      @Override
      public void run() {
        doDispatchUserMessage(msg);
      }
    };
  }

  private void initializeContext(final FudgeMsg stash, final Queue<Runnable> deferredDispatches) {
    deferredDispatches.add(null);
    getExecutor().execute(new Runnable() {
      @Override
      public void run() {
        initializeContext(stash);
        synchronized (deferredDispatches) {
          deferredDispatches.remove(); // First item is the NULL posted above
          while (!deferredDispatches.isEmpty()) {
            s_logger.debug("Dispatching deferred user message");
            getExecutor().execute(deferredDispatches.remove());
          }
        }
      }
    });
  }

  private void sendUserMessageResponse(final UserMessage userMessage, UserMessagePayload response) {
    if (response == null) {
      s_logger.error("User message handler returned null for synchronous message call {}", userMessage);
      response = UserMessagePayload.EMPTY_PAYLOAD;
    }
    s_logger.debug("Response {}", response);
    userMessage.setPayload(response);
    sendUserMessage(userMessage);
  }

  protected void doDispatchUserMessage(final FudgeMsg msg) {
    final FudgeDeserializer deserializer = new FudgeDeserializer(getClientContext().getFudgeContext());
    final UserMessage userMessage = new UserMessage(deserializer, msg);
    UserMessagePayload response = null;
    try {
      s_logger.debug("Dispatching user message {}", msg);
      response = userMessage.getPayload().accept(getClientContext().getMessageHandler(), getSessionContext());
    } catch (final AsynchronousExecution e) {
      // Only bother with the result if we're going to use it
      if (userMessage.getHandle() != null) {
        e.setResultListener(new ResultListener<UserMessagePayload>() {
          @Override
          public void operationComplete(final AsynchronousResult<UserMessagePayload> result) {
            UserMessagePayload response = null;
            try {
              response = result.getResult();
            } catch (final Throwable t) {
              s_logger.error("Error in user message handler", t);
              response = UserMessagePayload.EMPTY_PAYLOAD;
            }
            sendUserMessageResponse(userMessage, response);
          }
        });
      }
      return;
    } catch (final Throwable t) {
      s_logger.error("Error in user message handler", t);
      response = UserMessagePayload.EMPTY_PAYLOAD;
    }
    if (userMessage.getHandle() != null) {
      sendUserMessageResponse(userMessage, response);
    }
  }

  protected void setStashMessage(final FudgeMsg stashMessage) {
    synchronized (this) {
      _stashMessage = stashMessage;
    }
    final ConnectorMessage msg = new ConnectorMessage(Operation.STASH, stashMessage);
    getOutputMessageBuffer().add(new FudgeMsgEnvelope(msg.toFudgeMsg(
        new FudgeSerializer(getClientContext().getFudgeContext())), 0, MessageDirectives.CLIENT));
  }

  protected synchronized FudgeMsg getStashMessage() {
    return _stashMessage;
  }

  protected void doPoison() {
    s_logger.info("Queuing poison message and disconnecting pipes");
    _poisoned = true;
    // Poison the writer thread with an empty Fudge message
    getOutputMessageBuffer().add(FudgeContext.EMPTY_MESSAGE_ENVELOPE);
    // Poison the reader thread by forcing an I/O exception when the pipes close
    disconnectPipes();
  }

  protected boolean isPoisoned() {
    return _poisoned;
  }

}
