/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;

import com.opengamma.transport.EndPointDescriptionProvider;

/**
 * A configuration resource detailing the connection end-points for the services remote calculation nodes
 * need to connect to the view processor.
 */
public class CalcNodeSocketConfiguration {

  /**
   * The end point for the computation cache.
   */
  public static final String CACHE_SERVER_KEY = "cacheServer";
  /**
   * The end point for the remote node job dispatcher.
   */
  public static final String JOB_SERVER_KEY = "jobServer";

  private EndPointDescriptionProvider _cacheServer;
  private EndPointDescriptionProvider _jobServer;

  public void setCacheServer(final EndPointDescriptionProvider cacheServer) {
    _cacheServer = cacheServer;
  }

  public EndPointDescriptionProvider getCacheServer() {
    return _cacheServer;
  }

  public void setJobServer(final EndPointDescriptionProvider jobServer) {
    _jobServer = jobServer;
  }

  public EndPointDescriptionProvider getJobServer() {
    return _jobServer;
  }

  public FudgeMsg toFudgeMsg(final FudgeContext fudgeContext) {
    final MutableFudgeMsg message = fudgeContext.newMessage();
    if (getCacheServer() != null) {
      message.add(CACHE_SERVER_KEY, getCacheServer().getEndPointDescription(fudgeContext));
    }
    if (getJobServer() != null) {
      message.add(JOB_SERVER_KEY, getJobServer().getEndPointDescription(fudgeContext));
    }
    return message;
  }

}
