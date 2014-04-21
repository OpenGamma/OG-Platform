/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

import java.util.Map;
import java.util.Set;

import com.opengamma.DataNotFoundException;
import com.opengamma.engine.marketdata.MarketDataInjector;
import com.opengamma.engine.resource.EngineResourceReference;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.tuple.Pair;

/**
 * Represents a managed client of a specific view process in the context of a particular user. This is the unit of external interaction for accessing computation results.
 * <p>
 * The client begins detached from any particular view process. In order to receive results, it must be attached to a view process. This creates demand for the process to execute, ensuring that it
 * remains executing while the client is attached.
 * <p>
 * If the application requires asynchronous updates, it must set a result listener. This receives notifications of events from the view process. The result mode for this client may be controlled using
 * {@link #setResultMode(ViewResultMode)}; this allows unwanted types of result to be filtered out, potentially avoiding the overhead of unnecessary serialisation.
 * <p>
 * The application may poll for the latest full result by calling {@link #getLatestResult()}. Listeners can be set or changed even when the client is detached from a view process; this allows the
 * necessary listeners to be in place before results commence.
 * <p>
 * The per-client flow of results is controlled through {@link #pause()} and {@link #resume()}. By default, results will flow immediately, but the client may be paused before attaching it to a view
 * process.
 * <p>
 * When results are paused, they are incrementally batched to be delivered as a single, collapsed result when they are resumed. This result is specific to the individual client that has been paused.
 * <p>
 * Use {@link #setUpdatePeriod(long)} to throttle the frequency of updates exposed through this client.
 * <p>
 * Always call {@link #shutdown()} from any state to allow resources associated with the managed view to be released when the client is no longer required. Without this, the view process may continue
 * executing indefinitely.
 */
@PublicAPI
public interface ViewClient extends UniqueIdentifiable {

  /**
   * Gets the unique identifier of the view client
   * 
   * @return the identifier, not null
   */
  @Override
  UniqueId getUniqueId();

  /**
   * Gets the user for whom the view client was created. This user necessarily has sufficient permissions on the underlying view.
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
   * @return true if this client is attached to a view process
   */
  boolean isAttached();

  /**
   * Attaches the client to a shared view process, which might involve creating a new process.
   * <p>
   * As part of this call, the client is updated with the latest state of the view process which may result in listeners being invoked, depending on the state of the client.
   * 
   * @param definitionId the unique ID of the view definition, not null
   * @param executionOptions the view execution options, not null
   * @throws IllegalStateException if the client is already attached to a process
   * @throws DataNotFoundException if the view definition identifier is invalid
   * @throws IllegalArgumentException if there is a problem with the execution options
   */
  void attachToViewProcess(UniqueId definitionId, ViewExecutionOptions executionOptions);

  /**
   * Attaches the client to a view process, which might involve creating a new process.
   * <p>
   * As part of this call, the client is updated with the latest state of the view process which may result in listeners being invoked, depending on the state of the client.
   * 
   * @param definitionId the unique ID of the view definition, not null
   * @param executionOptions the view execution options, not null
   * @param newPrivateProcess true to attach to a new process, false to attach to a shared process
   * @throws IllegalStateException if the client is already attached to a process
   * @throws DataNotFoundException if the view definition identifier is invalid
   * @throws IllegalArgumentException if there is a problem with the execution options
   */
  void attachToViewProcess(UniqueId definitionId, ViewExecutionOptions executionOptions, boolean newPrivateProcess);

  /**
   * Attaches the client to a specific, existing view process.
   * <p>
   * As part of this call, the client is updated with the latest state of the view process which may result in listeners being invoked, depending on the state of the client.
   * 
   * @param processId the unique identifier of the view process, not null
   */
  void attachToViewProcess(UniqueId processId);

  /**
   * Detaches the client from the view process, if any, to which it is currently attached.
   */
  void detachFromViewProcess();

  // [PLAT-1174]
  // REVIEW jonathan 2011-04-07 -- providing access to the underlying live data overrides like this is bad for a few
  // reasons, and only really applies to real-time processes. They should instead be part of the execution options for
  // each cycle.
  /**
   * Gets the live data override injector for the view process to which the client is attached. This allows arbitrary live data to be overridden; the effects of doing so will be seen in future view
   * cycles.
   * 
   * @return the live data override injector, not null
   * @throws IllegalStateException if the view client is not attached to a view process
   */
  MarketDataInjector getLiveDataOverrideInjector();

  /**
   * Gets the latest view definition referenced by the view process to which the client is attached. This could be a different version from the one used in the computation of the latest result as seen
   * by this client; to access a specific version use the appropriate {@link CompiledViewDefinition} or {@link ViewCycle}.
   * 
   * @return the view definition currently being operated on by the view process, not null
   * @throws IllegalStateException if the view client is not attached to a view process
   */
  // NOTE jonathan 2011-08-04 -- Some operations will need to ensure that the view definition does not change. When we
  // reference view definitions by unique identifier, these would attach to a specific version rather than the latest
  // version. At the moment, we only support attaching to the latest version.
  ViewDefinition getLatestViewDefinition();
  
  /**
   * Gets the view process to which this client is attached.
   * 
   * @return the view process to which this client is attached, not null
   * @throws IllegalStateException if the view client is not attached to a view process
   */
  ViewProcess getViewProcess();

  //-------------------------------------------------------------------------
  /**
   * Sets (or replaces) the result listener.
   * 
   * @param resultListener the result listener, or null to remove an existing listener.
   */
  void setResultListener(ViewResultListener resultListener);

  /**
   * Sets the minimum time between successive results delivered to the listener, thus providing the ability to throttle the rate of updates. Even when this is in use, an update may still require
   * multiple calls to the listener, for example the notification of errors and/or compilation results. This is achieved by merging any updates which arrive in between the minimum period, and
   * releasing only a single, merged update at the correct time. Set this to 0 to specify an unrestricted rate of updates.
   * 
   * @param periodMillis the minimum time between updates, or 0 to specify unlimited updates.
   */
  void setUpdatePeriod(long periodMillis);

  /**
   * Gets the result mode for sending full cycle results to the listener. Defaults to {@link ViewResultMode#FULL_ONLY}.
   * 
   * @return the result mode, not null
   */
  ViewResultMode getResultMode();

  /**
   * Sets the result mode for sending full cycle results to the listener. Defaults to {@link ViewResultMode#FULL_ONLY}.
   * 
   * @param resultMode the result mode, not null
   */
  void setResultMode(ViewResultMode resultMode);

  /**
   * Gets the mode for sending cycle fragment results to the listener. Defaults to {@link ViewResultMode#NONE}.
   * 
   * @return the result mode, not null
   */
  ViewResultMode getFragmentResultMode();

  /**
   * Sets the mode for sending cycle fragment results to the listener. Defaults to {@link ViewResultMode#NONE}.
   * 
   * @param fragmentResultMode the result mode, not null
   */
  void setFragmentResultMode(ViewResultMode fragmentResultMode);

  //-------------------------------------------------------------------------
  /**
   * Pauses the flow of results exposed through this client. They continue to be received internally, and these are delivered as a merged result when updates are resumed.
   */
  void pause();

  /**
   * Resumes the flow of results exposed through this client.
   */
  void resume();

  /**
   * Requests that a computation cycle be run, even if none of the other triggers have fired since the last cycle.
   */
  void triggerCycle();

  /**
   * Gets whether the attached view process has completed from the perspective of the client. This is consistent with any data flow restrictions being applied through this view client, so may occur
   * after the process actually completes. This is intended for batch processing; if the view process is running with an infinite number of evaluation times then this method will block forever.
   * 
   * @return true if the attached view process has completed
   * @throws IllegalStateException if the view client is not attached to a view process
   */
  boolean isCompleted();

  /**
   * Blocks until the view process completes from the perspective of the client. This is consistent with any data flow restrictions being applied through this view client, so may occur after the
   * process actually completes. This is intended for batch processing; if the view process is running with an infinite number of evaluation times then this method will block forever.
   * 
   * @throws IllegalStateException if the view client is not attached to a view process
   * @throws InterruptedException if the thread is interrupted while waiting for the view process to complete
   */
  void waitForCompletion() throws InterruptedException;

  /**
   * Indicates whether the result of a completed view cycle is available to this client. This is consistent with any data flow restrictions being applied through this view client, so does not
   * necessarily reflect the most recent state of the view process.
   * 
   * @return true if a computation result is available
   * @throws IllegalStateException if the view client is not attached to a view process
   */
  boolean isResultAvailable();

  /**
   * Gets the latest compiled view definition. This is consistent with any data flow restrictions being applied through this view client, so does not necessarily represent the most recent state of the
   * view process.
   * 
   * @return the latest compiled view definition, null if no compilation has yet been produced
   */
  CompiledViewDefinition getLatestCompiledViewDefinition();

  /**
   * Gets the full result from the latest view cycle. This is consistent with any data flow restrictions being applied through this view client, so does not necessarily represent the most recent state
   * of the view process.
   * <p>
   * This value is consistent with the result provided to any {@link ViewResultListener} during a callback.
   * 
   * @return the latest result, null if no result yet exists
   * @throws IllegalStateException if the view client is not attached to a view process
   * @see #isResultAvailable()
   */
  ViewComputationResultModel getLatestResult();

  /**
   * Gets whether this client supports access to view cycles.
   * 
   * @return true if the client can provide access to view cycles
   */
  boolean isViewCycleAccessSupported();

  /**
   * Sets whether this client should support access to view cycles. This feature involves overheads which are best avoided if it is not needed.
   * 
   * @param isViewCycleAccessSupported true to enable access to view cycles
   */
  void setViewCycleAccessSupported(boolean isViewCycleAccessSupported);

  /**
   * Sets diagnostic information that will be sent to a view process and will be available in all logging on the view process via MDC {@see http://logback.qos.ch/manual/mdc.html}. If this method is
   * called whilst the client is already attached to a view process, then an IllegalStateException will be thrown. If connecting to an existing view process, then this data is ignored (this avoids
   * overwriting the context information which is already being written by the process).
   * 
   * @param context the context information to be logged by the view process, may be null
   * @throws IllegalStateException if called whilst attached to a view process
   */
  void setViewProcessContextMap(Map<String, String> context);

  /**
   * Creates a reference to the latest view cycle. This is consistent with any data flow restrictions being applied through this view client, so does not necessarily represent the most recent state of
   * the view process.
   * 
   * @return a reference to the latest view cycle, or null if the latest cycle is not available
   * @throws UnsupportedOperationException if this client does not support referencing computation cycles.
   * @throws IllegalStateException if the view client is not attached to a view process
   */
  EngineResourceReference<? extends ViewCycle> createLatestCycleReference();

  /**
   * Creates a reference to a specific view cycle.
   * 
   * @param cycleId the unique identifier of the view cycle, not null
   * @return a reference to the view cycle, or null if not found
   */
  EngineResourceReference<? extends ViewCycle> createCycleReference(UniqueId cycleId);

  //-------------------------------------------------------------------------
  /**
   * Ensures at least a minimum level of logging output is present in the results for the given value specifications. Changes will take effect from the next computation cycle.
   * <p>
   * If another view client connected to the same view process has already changed the level of logging for one or more results then all view clients will see the maximum level requested.
   * <p>
   * This has set-like behaviour; the last setting for a given result specification will apply regardless of the number of calls.
   * <p>
   * Results which are not terminal outputs - that is, results which were not directly requested in the view definition and are not present in the result model - may be referenced, but these can only
   * be accessed using {@link ViewCycle#queryResults(com.opengamma.engine.view.calc.ComputationCycleQuery)}.
   * 
   * @param minimumLogMode the minimum log mode to ensure, not null
   * @param targets a set of calculation configuration name and value specification pairs, not null or empty
   * @throws IllegalStateException if the view client is not attached to a view process
   */
  void setMinimumLogMode(ExecutionLogMode minimumLogMode, Set<Pair<String, ValueSpecification>> targets);

  //-------------------------------------------------------------------------
  /**
   * Terminates this client, detaching it from any process, disconnecting it from any listener, and releasing any resources. This method <b>must</b> be called to avoid resource leaks. A terminated
   * client is no longer useful and must be discarded.
   */
  void shutdown();

}
