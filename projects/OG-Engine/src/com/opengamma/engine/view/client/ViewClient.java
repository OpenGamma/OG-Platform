/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

import com.opengamma.engine.view.ComputationResultListener;
import com.opengamma.engine.view.DeltaComputationResultListener;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.calc.ViewCycleReference;
import com.opengamma.engine.view.compilation.ViewCompilationListener;
import com.opengamma.engine.view.execution.ViewProcessExecutionOptions;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.PublicAPI;

/**
 * Represents a managed client of a specific view process in the context of a particular user. This is the unit of
 * external interaction for accessing computation results.
 * <p>
 * The client begins detached from any particular view process. In order to receive results, it must be attached to a
 * view process. This may create demand for the process to begin execution.
 * <p>
 * If the application requires asynchronous updates, it must set a full or delta result listener (or both) as required:
 * any new computation results will be delivered through these listeners. The application may poll for the latest full
 * result by calling {@link #getLatestResult()}. Listeners can be set or changed even when the client is detached from
 * a view process; this allows the necessary listeners to be in place before results commence.
 * <p>
 * The per-client flow of results is controlled through {@link #pause()} and {@link #resume()}. By default, results
 * will flow immediately, but the client may be paused before attaching it to a view process.
 * <p>
 * When results are paused, they are incrementally batched to be delivered as a single, collapsed result when they are
 * resumed. This result is specific to the individual client that has been paused.
 * <p>
 * Use {@link #setUpdatePeriod(long)} to throttle the frequency of updates exposed through this client.
 * <p>
 * Always call {@link #shutdown()} from any state to allow resources associated with the managed view to be released
 * when the client is no longer required. Without this, the view process may continue executing indefinitely.
 */
@PublicAPI
public interface ViewClient extends UniqueIdentifiable {

  /**
   * Gets the unique identifier of the view client
   * 
   * @return the identifier, not null
   */
  UniqueIdentifier getUniqueId();
  
  /**
   * Gets the user for whom the view client was created. This user necessarily has sufficient permissions on the
   * underlying view.
   * 
   * @return the user, not null
   */
  UserPrincipal getUser();
  
  /**
   * Gets the view processor to which the view client belongs.
   * 
   * @return the view processor to which the view client belongs, not null
   */
  ViewProcessor getViewProcessor();
  
  /**
   * Gets the state of this view client.
   * 
   * @return the state of this view client, not null
   */
  ViewClientState getState();
  
  //-------------------------------------------------------------------------
  /**
   * Gets whether this client is attached to a view process.
   * 
   * @return {@code true} if this client is attached to a view process, {@code false} otherwise
   */
  boolean isAttached();
  
  /**
   * Attaches the client to a shared view process, which might involve creating a new process.
   * <p>
   * As part of this call, the client is updated with the latest state of the view process which may result in
   * listeners being invoked, depending on the state of the client.
   * 
   * @param viewDefinitionName  the name of the view definition, not null
   * @param executionOptions  the view execution options, not null
   * @throws IllegalStateException if the client is already attached to a process 
   */
  void attachToViewProcess(String viewDefinitionName, ViewProcessExecutionOptions executionOptions);
  
  /**
   * Attaches the client to a view process, which might involve creating a new process.
   * <p>
   * As part of this call, the client is updated with the latest state of the view process which may result in
   * listeners being invoked, depending on the state of the client.
   * 
   * @param viewDefinitionName  the name of the view definition, not null
   * @param executionOptions  the view execution options, not null
   * @param newBatchProcess  {@code true} to attach to a new batch process with this client as the batch controller,
   *                         {@code false} to connect to a normal, shared process
   * @throws IllegalStateException if the client is already attached to a process 
   */
  void attachToViewProcess(String viewDefinitionName, ViewProcessExecutionOptions executionOptions, boolean newBatchProcess);
  
  /**
   * Attaches the client to a specific, existing view process.
   * <p>
   * As part of this call, the client is updated with the latest state of the view process which may result in
   * listeners being invoked, depending on the state of the client.
   * 
   * @param processId  the unique identifier of the view process, not null
   */
  void attachToViewProcess(UniqueIdentifier processId);
  
  /**
   * Detaches the client from the view process, if any, to which it is currently attached.
   */
  void detachFromViewProcess();
  
  /**
   * Gets whether this client is the controller of a batch view process. Only one such client can exist for any batch
   * process, and this client solely drives execution of the process, regardless of the actions of other clients. Any
   * other client therefore operates merely as a 'fly-on-the-wall' for monitoring the batch.
   * 
   * @return {@code true} if this client is the controller of a batch view process, {@code false} otherwise
   */
  boolean isBatchController();
  
  //-------------------------------------------------------------------------
  /**
   * Sets (or replaces) the view compilation listener.
   * <p>
   * Any evaluation model provided to the listener applies to all future computation results until further notice, and
   * replaces any object previously provided. Metadata notifications and result notifications are provided serially, never
   * concurrently.
   * 
   * @param compilationListener  the compilation listener, or {@code null} to remove any existing listener.
   */
  void setCompilationListener(ViewCompilationListener compilationListener);
  
  /**
   * Sets (or replaces) the result listener.
   * 
   * @param resultListener  the result listener, or {@code null} to remove any existing listener.
   */
  void setResultListener(ComputationResultListener resultListener);

  /**
   * Sets (or replaces) the delta result listener.
   * 
   * @param deltaResultListener  the new listener, or {@code null} to remove any existing listener.
   */
  void setDeltaResultListener(DeltaComputationResultListener deltaResultListener);

  /**
   * Sets the minimum time between successive results delivered to listeners, thus providing the ability to throttle
   * the rate of updates. This is achieved by merging any updates which arrive in between the minimum period, and
   * releasing only a single, merged update at the correct time. Set this to 0 to specify no minimum period between
   * updates; this is the only setting for which updates may be passed straight through synchronously.
   * 
   * @param periodMillis  the minimum time between updates, or 0 to specify unlimited updates.
   */
  void setUpdatePeriod(long periodMillis);

  //-------------------------------------------------------------------------
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
   * Blocks until the view process completes from the perspective of the client. This is consistent with any data flow
   * restrictions being applied through this view client, so may occur after the process actually completes. This is
   * intended for batch processing; if the view process is running with an infinite number of evaluation times then
   * this method will block forever. 
   * 
   * @throws IllegalStateException if the view client is not attached to a view process
   * @throws InterruptedException if the thread is interrupted while waiting for the view process to complete
   */
  void waitForCompletion() throws InterruptedException;
  
  /**
   * Indicates whether the result of a completed view cycle is available to this client. This is consistent with any
   * data flow restrictions being applied through this view client, so does not necessarily reflect the most recent
   * state of the view process.
   * 
   * @return <code>true</code> if a computation result is available, <code>false</code> otherwise
   * 
   * @throws IllegalStateException if the view client is not attached to a view process
   */
  boolean isResultAvailable();
  
  /**
   * Gets the full result from the latest view cycle. This is consistent with any data flow restrictions being applied
   * through this view client, so does not necessarily represent the most recent state of the view process.
   *  
   * @return the latest result, or {@code null} if no result yet exists
   * @throws IllegalStateException if the view client is not attached to a view process
   * @see #isResultAvailable()
   */
  ViewComputationResultModel getLatestResult();
  
  /**
   * Gets whether this client supports access to view cycles.
   *  
   * @return {@code true} if the client can provide access to view cycles, {@code false} otherwise
   */
  boolean isViewCycleAccessSupported();
  
  /**
   * Sets whether this client should support access to view cycles. This feature involves overheads which are best
   * avoided if it is not needed.
   * 
   * @param isViewCycleAccessSupported  {@code true} to enable access to  view cycles, {@code false} otherwise
   */
  void setViewCycleAccessSupported(boolean isViewCycleAccessSupported);
  
  /**
   * Creates a reference to the latest view cycle. This is consistent with any data flow restrictions being applied
   * through this view client, so does not necessarily represent the most recent state of the view process.
   * <p>
   * When called from a result listener, this method will return a reference to the view cycle which generated that
   * result.
   * 
   * @return a reference to the latest view cycle, or {@code null} if the latest cycle is not available
   * @throws UnsupportedOperationException if this client does not support referencing computation cycles.
   * @throws IllegalStateException if the view client is not attached to a view process
   */
  ViewCycleReference createLatestCycleReference();
  
  /**
   * Terminates this client, detaching it from any process, disconnecting it from any listeners, and releasing any
   * resources. This method <b>must</b> be called to avoid resource leaks. A terminated client is no longer useful and
   * must be discarded.
   */
  void shutdown();

}
