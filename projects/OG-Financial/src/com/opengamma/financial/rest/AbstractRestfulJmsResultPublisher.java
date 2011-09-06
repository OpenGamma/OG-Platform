/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.rest;

import javax.jms.JMSException;
import javax.time.Instant;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.fudgemsg.FudgeMsg;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.transport.jaxrs.FudgeRest;

/**
 * Base class for a RESTful resource which uses a REST+JMS pattern to publish streaming results.
 * <p>
 * Provides heartbeat listening and control of the JMS stream.
 */
public abstract class AbstractRestfulJmsResultPublisher {

  //CSOFF: just constants
  public static final String PATH_HEARTBEAT = "heartbeat";
  public static final String PATH_START_JMS_RESULT_STREAM = "startJmsResultStream";
  public static final String PATH_STOP_JMS_RESULT_STREAM = "stopJmsResultStream";
  public static final String DESTINATION_FIELD = "destination";
  //CSON: just constants
  
  private final AbstractJmsResultPublisher _resultPublisher;
  private volatile Instant _lastAccessed = Instant.now();
  
  protected AbstractRestfulJmsResultPublisher(AbstractJmsResultPublisher resultPublisher) {
    _resultPublisher = resultPublisher;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Tests whether the underlying resource has been terminated.
   * 
   * @return true if the underlying resource has been terminated, false otherwise
   */
  protected abstract boolean isTerminated();
  
  /**
   * Called to indicate that the publisher's consumer has failed to provide heartbeats, and the resource is no longer
   * required. Releases the underlying resource, stopping publication of results. 
   */
  protected abstract void expire();
  
  //-------------------------------------------------------------------------
  protected void startPublishingResults(String destination) throws Exception {
    getResultPublisher().startPublishingResults(destination);
  }
  
  protected void stopPublishingResults() throws JMSException {
    getResultPublisher().stopPublishingResults();
  }
  
  //-------------------------------------------------------------------------
  @POST
  @Path(PATH_HEARTBEAT)
  public Response heartbeat() {
    updateLastAccessed();
    return Response.ok().build();
  }
  
  public Instant getLastAccessed() {
    return _lastAccessed;
  }
  
  protected void updateLastAccessed() {
    _lastAccessed = Instant.now();
  }
  
  //-------------------------------------------------------------------------  
  @POST
  @Path(PATH_START_JMS_RESULT_STREAM)
  @Consumes(FudgeRest.MEDIA)
  public Response startResultStream(FudgeMsg msg) {
    updateLastAccessed();
    String destination = msg.getString(DESTINATION_FIELD);
    try {
      startPublishingResults(destination);
      return Response.ok(destination).build();
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Error starting result publisher", e);
    }
  }
  
  @POST
  @Path(PATH_STOP_JMS_RESULT_STREAM)
  public Response stopResultStream() {
    updateLastAccessed();
    try {
      stopPublishingResults();
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Error stopping result publisher", e);
    }
    return Response.ok().build();
  }
  
  //-------------------------------------------------------------------------
  private AbstractJmsResultPublisher getResultPublisher() {
    return _resultPublisher;
  }
  
}
