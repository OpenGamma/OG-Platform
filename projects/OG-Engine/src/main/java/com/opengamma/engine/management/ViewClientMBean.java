/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewClientState;
import com.opengamma.id.UniqueId;

/**
 * A management bean for a {@link ViewClient} 
 */
public interface ViewClientMBean {

  /**
   * Gets the unique identifier of the view client
   * 
   * @return the identifier, not null
   */
  UniqueId getUniqueId();
  
  /**
   * Gets the user for whom the view client was created. This user necessarily has sufficient permissions on the
   * underlying view.
   * 
   * @return the user, not null
   */
  String getUser();
  
  /**
   * Gets the state of this view client.
   * 
   * @return the state of this view client, not null
   */
  ViewClientState getState();
  
  /**
   * Gets whether this client is attached to a view process.
   * 
   * @return true if this client is attached to a view process
   */
  boolean isAttached();
  
  //-------------------------------------------------------------------------
  /**
   * Sets the minimum time between successive results delivered to the listener, thus providing the ability to throttle
   * the rate of updates. Even when this is in use, an update may still require multiple calls to the listener, for
   * example the notification of errors and/or compilation results. This is achieved by merging any updates which
   * arrive in between the minimum period, and releasing only a single, merged update at the correct time. Set this to
   * 0 to specify an unrestricted rate of updates.
   * 
   * @param periodMillis  the minimum time between updates, or 0 to specify unlimited updates.
   */
  void setUpdatePeriod(long periodMillis);
  
  /**
   * Pauses the flow of results exposed through this client. They continue to be received internally, and these are
   * delivered as a merged result when updates are resumed.
   */
  void pause();
  
  /**
   * Resumes the flow of results exposed through this client.
   */
  void resume();
  
  /**
   * Gets whether the attached view process has completed from the perspective of the client. This is consistent with
   * any data flow restrictions being applied through this view client, so may occur after the process actually
   * completes. This is intended for batch processing; if the view process is running with an infinite number of
   * evaluation times then this method will block forever. 
   * 
   * @return true if the attached view process has completed
   * @throws IllegalStateException if the view client is not attached to a view process
   */
  boolean isCompleted();
  
  /**
   * Indicates whether the result of a completed view cycle is available to this client. This is consistent with any
   * data flow restrictions being applied through this view client, so does not necessarily reflect the most recent
   * state of the view process.
   * 
   * @return true> if a computation result is available
   * @throws IllegalStateException if the view client is not attached to a view process
   */
  boolean isResultAvailable();
  
  /**
   * Gets the full result from the latest view cycle. This is consistent with any data flow restrictions being applied
   * through this view client, so does not necessarily represent the most recent state of the view process.
   *  
   * @return the latest result, null if no result yet exists
   * @throws IllegalStateException if the view client is not attached to a view process
   * @see #isResultAvailable()
   */
  ViewComputationResultModel getLatestResult();
  
  //-------------------------------------------------------------------------
  /**
   * Gets whether this client supports access to view cycles.
   *  
   * @return true if the client can provide access to view cycles
   */
  boolean isViewCycleAccessSupported();
  
  /**
   * Sets whether this client should support access to view cycles. This feature involves overheads which are best
   * avoided if it is not needed.
   * 
   * @param isViewCycleAccessSupported  true to enable access to view cycles
   */
  void setViewCycleAccessSupported(boolean isViewCycleAccessSupported);
  
  //-------------------------------------------------------------------------
  /**
   * Terminates this client, detaching it from any process, disconnecting it from any listener, and releasing any
   * resources. This method <b>must</b> be called to avoid resource leaks. A terminated client is no longer useful and
   * must be discarded.
   */
  void shutdown();
  
}
