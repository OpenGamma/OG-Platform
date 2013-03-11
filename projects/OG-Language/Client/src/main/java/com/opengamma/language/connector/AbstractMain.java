/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.language.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.language.context.NullSessionContextEventHandler;
import com.opengamma.language.context.SessionContext;

/**
 * Base class for a main method as part of a utility that requires a session context, for example to produce documentation.
 */
public abstract class AbstractMain {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractMain.class);

  /**
   * Implementation of a client factory that allows the session context to be grabbed during the svcAccept all.
   */
  public static final class DummyClientFactory extends ClientFactory {

    private static SessionContext s_sessionContext;

    private DummyClientFactory(final ClientContext clientContext) {
      super(clientContext);
    }

    public static ClientFactoryFactory getFactory() {
      return new ClientFactoryFactory() {
        @Override
        public ClientFactory createClientFactory(final ClientContext clientContext) {
          return new DummyClientFactory(clientContext);
        }
      };
    }

    @Override
    public Client createClient(final String inputPipeName, final String outputPipeName,
        final SessionContext sessionContext) {
      s_sessionContext = sessionContext;
      return null;
    }

    public static SessionContext getSessionContext() {
      return s_sessionContext;
    }

  }

  protected abstract boolean main(SessionContext context, String[] args);

  private boolean main(final String languageId, final String[] args) {
    s_logger.debug("Starting infrastructure");
    System.setProperty(LanguageSpringContext.CLIENT_FACTORY_CLASS_PROPERTY, DummyClientFactory.class.getName());
    if (Main.svcStart() != null) {
      s_logger.error("Couldn't start infrastructure");
      return false;
    }
    s_logger.info("Infrastructure started, creating context for {}", languageId);
    if (!Main.svcAccept("main", null, null, languageId, false)) {
      s_logger.error("Couldn't accept dummy connection");
      return false;
    }
    final SessionContext context = DummyClientFactory.getSessionContext();
    s_logger.debug("Initializing context");
    context.initContext(new NullSessionContextEventHandler());
    s_logger.info("Calling user main method with context for {}", languageId);
    if (!main(context, args)) {
      s_logger.error("Couldn't call user main method");
      return false;
    }
    s_logger.debug("Stopping infrastructure");
    if (!Main.svcStop()) {
      s_logger.error("Couldn't stop infrastructure");
      // ignore
    }
    return true;
  }

  protected static void main(final AbstractMain instance, final String languageId, final String[] args) {
    if (!instance.main(languageId, args)) {
      System.exit(1);
    }
  }

  public final void runMain(final String languageId) {
    runMain(languageId, new String[0]);
  }

  public final void runMain(final String languageId, final String[] args) {
    main(this, languageId, args);
  }

}
