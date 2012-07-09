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

import com.opengamma.util.jms.JmsConnector;

/**
 * Provides remote access to a {@link ManageableFunctionBlacklistProvider}.
 */
public class RemoteManageableFunctionBlacklistProvider extends RemoteFunctionBlacklistProvider implements ManageableFunctionBlacklistProvider {

  public RemoteManageableFunctionBlacklistProvider(final URI baseUri, final ExecutorService backgroundTasks, final JmsConnector jmsConnector) {
    super(baseUri, backgroundTasks, jmsConnector);
  }

  @Override
  protected ManageableFunctionBlacklist createBlacklist(final FudgeDeserializer fdc, final FudgeMsg info) {
    return new RemoteManageableFunctionBlacklist(fdc, info, this);
  }

  @Override
  public ManageableFunctionBlacklist getBlacklist(final String identifier) {
    return (ManageableFunctionBlacklist) super.getBlacklist(identifier);
  }

  protected void add(final String blacklist, final FudgeMsg request) {
    accessRemote(UriBuilder.fromUri(getBaseUri()).path("name/{name}/add").build(blacklist)).post(request);
  }

  protected void remove(final String blacklist, final FudgeMsg request) {
    accessRemote(UriBuilder.fromUri(getBaseUri()).path("name/{name}/remove").build(blacklist)).post(request);
  }

}
