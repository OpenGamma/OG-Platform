/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.connector;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.language.connector.Test.Operation;
import com.opengamma.language.context.SessionContext;

/**
 * Responds to the Test message to allow an unit test of the messaging infrastructures.
 */
/* package */class TestMessageHandler {
  
  private static final Logger s_logger = LoggerFactory.getLogger (TestMessageHandler.class);

  /**
   * Returns the inline response to the message (or null for none), and sends asynchronous responses
   * to the supplied sender.
   * 
   * @param message received test message
   * @param sender message sender for asynchronous messages
   */
  @SuppressWarnings("deprecation")
  public static UserMessagePayload testMessage(final Test message, final SessionContext context) {
    switch (message.getOperation()) {
      case CRASH_REQUEST: {
        s_logger.info ("CRASH_REQUEST - calling system.exit");
        final MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
        msg.add("foo", null, 42);
        context.getStashMessage().put(msg);
        System.exit (1);
        return null;
      }
      case ECHO_REQUEST:
        s_logger.info("ECHO_REQUEST - returning ECHO_RESPONSE");
        message.setOperation(Operation.ECHO_RESPONSE);
        return message;
      case ECHO_REQUEST_A:
        s_logger.info("ECHO_REQUEST_A - sending ECHO_RESPONSE_A asynchronously");
        message.setOperation(Operation.ECHO_RESPONSE_A);
        context.getMessageSender().send(message.clone());
        s_logger.info("ECHO_REQUEST_A - returning ECHO_RESPONSE");
        message.setOperation(Operation.ECHO_RESPONSE);
        return message;
      case ECHO_RESPONSE:
        throw new IllegalArgumentException ("ECHO_RESPONSE should not have been sent by the server");
      case ECHO_RESPONSE_A:
        throw new IllegalArgumentException ("ECHO_RESPONSE_A should not have been sent by the server");
      case PAUSE_REQUEST:
        s_logger.info("PAUSE_REQUEST - suspending threads");
        Main.notifyPause();
        final Thread[] threads = new Thread[100];
        Thread.enumerate(threads);
        // The thread control methods are deprecated for the exact reasons we want to use them. We want
        // to induce a deadlock or some other serious fault that hangs the JVM to test the resilience
        // mechanisms.
        for (int i = 0; i < threads.length; i++) {
          if (threads[i] != null) {
            if (threads[i] != Thread.currentThread()) {
              s_logger.debug("Suspending {}", threads[i].getName());
              threads[i].suspend();
            }
          }
        }
        Thread.currentThread().suspend();
        return null;
      case STASH_REQUEST: {
        s_logger.info("STASH_REQUEST - checking stash");
        final FudgeMsg msg = context.getStashMessage().get();
        if (msg != null) {
          s_logger.debug("Stash = {}", msg);
          if (msg.getInt("foo") == 42) {
            message.setOperation(Operation.STASH_RESPONSE);
            return message;
          }
        }
        return null;
      }
      case VOID_REQUEST:
        s_logger.info("VOID_REQUEST - no response");
        return null;
      case VOID_REQUEST_A:
        s_logger.info("VOID_REQUEST_A - sending VOID_RESPONSE_A asynchronously");
        message.setOperation(Operation.VOID_RESPONSE_A);
        context.getMessageSender().send(message);
        return null;
      case VOID_RESPONSE_A:
        throw new IllegalArgumentException ("VOID_RESPONSE_A should not have been sent by the server");
      default:
        throw new IllegalArgumentException("Unexpected operation " + message.getOperation());
    }
  }

}
