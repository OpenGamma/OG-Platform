/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.net.URI;
import java.util.concurrent.ExecutorService;

import javax.ws.rs.core.UriBuilder;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.rest.AbstractRemoteClient;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;

/**
 * Provides remote access to a {@link FunctionBlacklistProvider}.
 */
public class RemoteFunctionBlacklistProvider extends AbstractRemoteClient implements FunctionBlacklistProvider {

  private final ExecutorService _backgroundTasks;
  private final JmsConnector _jmsConnector;

  /**
   * Creates a new remote access point.
   * 
   * @param baseUri the base URI of the remote implementation, not null
   * @param backgroundTasks the executor service to use for background activities, not null
   * @param jmsConnector the JMS connector to use for change notifications
   */
  public RemoteFunctionBlacklistProvider(final URI baseUri, final ExecutorService backgroundTasks, final JmsConnector jmsConnector) {
    super(baseUri);
    ArgumentChecker.notNull(backgroundTasks, "backgroundTasks");
    ArgumentChecker.notNull(jmsConnector, "jmsConnector");
    _backgroundTasks = backgroundTasks;
    _jmsConnector = jmsConnector;
  }

  protected ExecutorService getBackgroundTasks() {
    return _backgroundTasks;
  }

  protected JmsConnector getJmsConnector() {
    return _jmsConnector;
  }

  protected FunctionBlacklist createBlacklist(final FudgeDeserializer fdc, final FudgeMsg info) {
    return new RemoteFunctionBlacklist(fdc, info, this);
  }

  @Override
  public FunctionBlacklist getBlacklist(final String identifier) {
    ArgumentChecker.notNull(identifier, "identifier");
    try {
      final FudgeDeserializer fdc = new FudgeDeserializer(getFudgeContext());
      final FudgeMsg response = accessRemote(UriBuilder.fromUri(getBaseUri()).path("name/{name}").build(identifier)).get(FudgeMsg.class);
      return createBlacklist(fdc, response);
    } catch (UniformInterfaceException404NotFound e) {
      return null;
    }
  }

  protected FudgeMsg refresh(final String blacklist, final int expectedModificationCount) {
    return accessRemote(UriBuilder.fromUri(getBaseUri()).path("name/{name}/mod/{mod}").build(blacklist, expectedModificationCount)).get(FudgeMsg.class);
  }

}
