/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.connector.debug;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.ImmutableFudgeMsg;

import com.opengamma.language.connector.Client;
import com.opengamma.language.connector.ClientContext;
import com.opengamma.language.connector.UserMessagePayload;
import com.opengamma.language.context.MutableSessionContext;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.context.SessionContextInitializationEventHandler;

/**
 * Debug client deferring calls to a remote process
 */
public class DebugClient extends Client {

  private static final String SESSION_CONTEXT_STASH = "com.opengamma.language.connector.debug.StashMessage";

  protected DebugClient(final ClientContext clientContext, final String inputPipeName, final String outputPipeName,
      final SessionContext session) {
    super(clientContext, inputPipeName, outputPipeName, session);
  }

  @Override
  protected SessionContextInitializationEventHandler getSessionInitializer() {
    final SessionContextInitializationEventHandler superInitializer = super.getSessionInitializer();
    return new SessionContextInitializationEventHandler() {

      @Override
      public void initContext(final MutableSessionContext context) {
        superInitializer.initContext(context);
      }

      @Override
      public void initContextWithStash(final MutableSessionContext context, final FudgeFieldContainer stash) {
        superInitializer.initContextWithStash(context, stash);
        context.setValue(SESSION_CONTEXT_STASH, new ImmutableFudgeMsg(stash, FudgeContext.GLOBAL_DEFAULT));
      }

    };
  }

  private FudgeFieldContainer getStash() {
    return getSessionContext().getValue(SESSION_CONTEXT_STASH);
  }

  @Override
  protected UserMessagePayload dispatchUserMessage(UserMessagePayload message) {
    // TODO pass the message to the external process
    return null;
  }

}
