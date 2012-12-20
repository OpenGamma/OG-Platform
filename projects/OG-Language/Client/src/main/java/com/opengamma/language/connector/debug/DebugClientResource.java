/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.connector.debug;

import java.util.concurrent.TimeoutException;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.language.connector.ClientContext;
import com.opengamma.language.connector.MessageSender;
import com.opengamma.language.connector.StashMessage;
import com.opengamma.language.connector.UserMessage;
import com.opengamma.language.connector.UserMessagePayload;
import com.opengamma.language.context.MutableSessionContext;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.context.SessionContextInitializationEventHandler;
import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.async.AsynchronousResult;
import com.opengamma.util.async.ResultListener;

/**
 * The back-end resource with which a {@link DebugClient} communicates. This handles user function invocations and
 * responds with the result.
 */
public class DebugClientResource implements FudgeMessageReceiver {

  private static final Logger s_logger = LoggerFactory.getLogger(DebugClientResource.class);
  
  private final FudgeConnection _fudgeConnection;
  private final ClientContext _clientContext;
  private final SessionContext _sessionContext;
  
  public DebugClientResource(FudgeConnection fudgeConnection, ClientContext clientContext, SessionContext sessionContext) {
    _fudgeConnection = fudgeConnection;
    _clientContext = clientContext;
    _sessionContext = sessionContext;
    
    sessionContext.initContext(new SessionContextInitializationEventHandler() {

      @Override
      public void initContext(MutableSessionContext context) {
        context.setMessageSender(new MessageSender() {

          @Override
          public void send(UserMessagePayload payload) {
            sendUserMessage(new UserMessage(payload));
          }
          
          @Override
          public long getDefaultTimeout() {
            return getClientContext().getMessageTimeout();
          }

          @Override
          public void sendAndWait(UserMessagePayload message, long timeoutMillis) throws TimeoutException {
            throw new UnsupportedOperationException();
          }

          @Override
          public UserMessagePayload call(UserMessagePayload message, long timeoutMillis) throws TimeoutException {
            throw new UnsupportedOperationException();
          }
          
        });
        context.setStashMessage(new StashMessage() {

          @Override
          public FudgeMsg get() {
            return FudgeContext.EMPTY_MESSAGE;
          }

          @Override
          public void put(FudgeMsg message) {
          }
          
        });
      }
      
      @Override
      public void initContextWithStash(MutableSessionContext context, FudgeMsg stash) {
        initContext(context);
      }
      
    });
  }
  
  private void sendResponse(final UserMessage userMessage, UserMessagePayload responsePayload) {
    if (responsePayload == null) {
      responsePayload = UserMessagePayload.EMPTY_PAYLOAD;
    }
    final UserMessage response = new UserMessage(responsePayload);
    response.setHandle(userMessage.getHandle());
    sendUserMessage(response);
  }
  
  @Override
  public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
    final UserMessage userMessage = fudgeContext.fromFudgeMsg(UserMessage.class, msgEnvelope.getMessage());
    s_logger.debug("User message received: {}", userMessage);
    final UserMessagePayload payload = userMessage.getPayload();
    final UserMessagePayload responsePayload;
    try {
      responsePayload = payload.accept(getClientContext().getMessageHandler(), getSessionContext());
    } catch (AsynchronousExecution e) {
      e.setResultListener(new ResultListener<UserMessagePayload>() {
        @Override
        public void operationComplete(final AsynchronousResult<UserMessagePayload> result) {
          sendResponse(userMessage, result.getResult());
        }
      });
      return;
    }
    sendResponse(userMessage, responsePayload);
  }

  protected void sendUserMessage(final UserMessage userMessage) {
    FudgeMsg fudgeMsg = userMessage.toFudgeMsg(new FudgeSerializer(getClientContext().getFudgeContext()));
    s_logger.debug("Sending user message {} as Fudge message {}", userMessage, fudgeMsg);
    getFudgeConnection().getFudgeMessageSender().send(fudgeMsg);
  }

  public FudgeConnection getFudgeConnection() {
    return _fudgeConnection;
  }
  
  public ClientContext getClientContext() {
    return _clientContext;
  }
  
  public SessionContext getSessionContext() {
    return _sessionContext;
  }
  
}
