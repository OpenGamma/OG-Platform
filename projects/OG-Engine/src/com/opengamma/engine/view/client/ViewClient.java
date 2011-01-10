/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

import com.opengamma.engine.view.ComputationResultListener;
import com.opengamma.engine.view.DeltaComputationResultListener;
import com.opengamma.engine.view.View;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.PublicAPI;

/**
 * Represents a managed client of a specific view in the context of a particular user. Provides access to properties of
 * the view, and adds client-oriented functionality. This is the unit of external interaction for accessing computation
 * results.
 * <p>
 * Typically, a {@link ViewClient} will be created for each view in each instance of a client application. If the
 * application requires asynchronous updates, it must set a full or delta result listener (or both) as required: any
 * new computation results will be delivered through these listeners. The application may poll for the latest full
 * result by calling {@link #getLatestResult()}.
 * <p>
 * The per-client flow of results is controlled through {@link #pauseLive()}, {@link #startLive()} and
 * {@link #stopLive()}. Initially, results are stopped. A stopped client does not cause the underlying view to be
 * computed, but other, active clients may keep the computation running. When results are paused, computation takes
 * place but the results are incrementally batched to be delivered as a single, collapsed result when they are resumed.
 * Use {@link #setLiveUpdatePeriod(long)} to throttle the frequency of updates exposed through this client.
 * <p>
 * Always call {@link #shutdown()} from any state to allow resources associated with the managed view to be released
 * when the client is no longer required.
 */
@PublicAPI
public interface ViewClient extends UniqueIdentifiable {

  /**
   * Gets the unique identifier for the view client, to make it easier to refer to the view client externally.
   * 
   * @return the unique identifier, not null
   */
  UniqueIdentifier getUniqueId();

  /**
   * Gets the view which backs this view client.
   * 
   * @return the view which backs this view client, not null
   */
  View getView();

  /**
   * Gets the user for whom the view client was created. This user necessarily has sufficient permissions on the
   * underlying view.
   * 
   * @return the user, not null
   */
  UserPrincipal getUser();

  /**
   * Indicates whether the result of a completed computation cycle is available yet. This is consistent with any data
   * flow restrictions being applied through this view client, so does not necessarily reflect the live state of the
   * view.
   * 
   * @return <code>true</code> if a computation result is available, <code>false</code> otherwise
   */
  boolean isResultAvailable();

  /**
   * Gets the full result from the last computation cycle. This is consistent with any data flow restrictions being
   * applied through this view client, so does not necessarily represent the live state of the view.
   *  
   * @return the latest result, or <code>null</code> if no result yet exists
   * @see #isResultAvailable()
   */
  ViewComputationResultModel getLatestResult();

  /**
   * Sets (or replaces) the result listener.
   * 
   * @param resultListener  the result listener, or <code>null</code> to remove any existing listener.
   */
  void setResultListener(ComputationResultListener resultListener);

  /**
   * Sets (or replaces) the delta result listener.
   * 
   * @param deltaResultListener  the new listener, or <code>null</code> to remove any existing listener.
   */
  void setDeltaResultListener(DeltaComputationResultListener deltaResultListener);

  /**
   * Gets the state of this view client.
   * 
   * @return the state of this view client, not null
   */
  ViewClientState getState();

  /**
   * Sets the minimum time between successive live update notifications to listeners, thus providing the ability to
   * throttle the rate of updates. This is achieved by merging any updates which arrive in between the minimum period,
   * and releasing only a single, merged update at the correct time. Set this to 0 to specify no minimum period between
   * updates; this is the only setting for which updates may be passed straight through synchronously.
   * 
   * @param periodMillis  the minimum time between updates, or 0 to specify unlimited updates.
   */
  void setLiveUpdatePeriod(long periodMillis);

  /**
   * Starts or resumes the flow of live computation updates exposed through this client.
   */
  void startLive();

  /**
   * Pauses the flow of live computation updates exposed through this client. Updates continue to be received
   * internally, and these are delivered as a merged result when updates are resumed.
   */
  void pauseLive();

  /**
   * Stops the flow of live computation updates exposed through this client. In this state, single results can be
   * obtained on demand by calling {@link #runOneCycle()}.
   */
  void stopLive();

  /**
   * Causes a single, on-demand calculation to be performed asynchronously. The result will be delivered through the
   * listeners. This operation is only possible if live computation is stopped. 
   */
  void runOneCycle();

  /**
   * Terminates this client, disconnecting it from any listeners and releasing any resources. This method <b>must</b>
   * be called to avoid resource leaks. A terminated client is no longer useful and must be discarded.
   */
  void shutdown();

}
