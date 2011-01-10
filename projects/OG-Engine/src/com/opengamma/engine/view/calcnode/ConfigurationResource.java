/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.transport.EndPointDescriptionProvider;

/**
 * A configuration resource detailing the connection end-points for the services remote calculation nodes
 * need to connect to the view processor.
 */
public class ConfigurationResource {

  /**
   * The end point for the computation cache.
   */
  public static final String CACHE_SERVER_KEY = "cacheServer";
  /**
   * The end point for dependency queries.
   */
  public static final String QUERY_SERVER_KEY = "queryServer";
  /**
   * The end point for the remote node job dispatcher.
   */
  public static final String JOB_SERVER_KEY = "jobServer";

  private EndPointDescriptionProvider _cacheServer;
  private EndPointDescriptionProvider _queryServer;
  private EndPointDescriptionProvider _jobServer;

  public void setCacheServer(final EndPointDescriptionProvider cacheServer) {
    _cacheServer = cacheServer;
  }

  public EndPointDescriptionProvider getCacheServer() {
    return _cacheServer;
  }

  public void setQueryServer(final EndPointDescriptionProvider queryServer) {
    _queryServer = queryServer;
  }

  public EndPointDescriptionProvider getQueryServer() {
    return _queryServer;
  }

  public void setJobServer(final EndPointDescriptionProvider jobServer) {
    _jobServer = jobServer;
  }

  public EndPointDescriptionProvider getJobServer() {
    return _jobServer;
  }

  public FudgeFieldContainer toFudgeMsg(final FudgeContext fudgeContext) {
    final MutableFudgeFieldContainer message = fudgeContext.newMessage();
    if (getCacheServer() != null) {
      message.add(CACHE_SERVER_KEY, getCacheServer().getEndPointDescription(fudgeContext));
    }
    if (getQueryServer() != null) {
      message.add(QUERY_SERVER_KEY, getQueryServer().getEndPointDescription(fudgeContext));
    }
    if (getJobServer() != null) {
      message.add(JOB_SERVER_KEY, getJobServer().getEndPointDescription(fudgeContext));
    }
    return message;
  }

}
