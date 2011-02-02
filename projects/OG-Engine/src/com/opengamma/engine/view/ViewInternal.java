/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import javax.time.InstantProvider;

import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.view.calc.SingleComputationCycle;
import com.opengamma.engine.view.compilation.ViewEvaluationModel;
import com.opengamma.engine.view.permission.ViewPermissionProvider;

/**
 * Exposes engine-level access to a view, including access to data structures which should not be available externally.
 */
public interface ViewInternal extends View {
    
  /**
   * Synchronously initializes the view for the given instant. Until a view is initialized, it can be used only to
   * access underlying metadata, such as the view definition. If the view has already been initialized, this method
   * does nothing and returns immediately. 
   * <p>
   * Initialization involves compiling the view definition into dependency graphs, which could be a lengthy process.
   * After initialization, the view is ready to be executed.
   * <p>
   * If initialization fails, an exception is thrown but the view remains in a consistent state from which
   * initialization may be re-attempted.
   * 
   * @param initializationInstant  the instant for which the view definition should be compiled, not null
   */
  void init(InstantProvider initializationInstant);
  
  /**
   * Synchronously reinitializes the view as of now if it has already been initialized. Otherwise does nothing.
   * <p>
   * Reinitialization happens transparently to any connected clients, allowing changes to the view definition to be
   * incorporated into future computation cycles without clients being aware that changes have occurred.
   */
  void reinit();
  
  /**
   * Synchronously reinitializes the view for the given instant if it has already been initialized. Otherwise does
   * nothing.
   * <p>
   * Reinitialization happens transparently to any connected clients, allowing changes to the view definition to be
   * incorporated into future computation cycles without clients being aware that changes have occurred.
   *  
   * @param initializationInstant  the instant for which the view definition should be complied, not null
   */
  void reinit(InstantProvider initializationInstant);
  
  /**
   * Gets the view processing context
   * 
   * @return the view processing context
   */
  ViewProcessingContext getProcessingContext();
  
  /**
   * Gets the latest view evaluation model used for live
   * mode calculations.
   * <p>
   * The return value can change over time as the function repository
   * is modified or if functions in it expire.
   * 
   * @return the current view evaluation model
   */
  ViewEvaluationModel getViewEvaluationModel();
  
  /**
   * Notifies the view that a live recalculation has been performed.
   * 
   * @param result  the result of the recalculation
   */
  void recalculationPerformed(ViewComputationResultModel result);

  /**
   * Creates a new computation cycle for the view using live data.
   * Valuation time is the current time.
   * <p>
   * This is used when the view is running in live mode.
   * 
   * @return the new computation cycle
   */
  SingleComputationCycle createCycle();
  
  /**
   * Synchronously runs a single computation cycle using live data.
   * Valuation time is the current time. The result
   * will be sent to the live listeners.
   * <p>
   * This is used in unit tests, to simulate a single
   * live calculation having been computed.
   * 
   * @throws IllegalStateException  if the view has not been initialized
   */
  void runOneCycle();
  
  /**
   * Synchronously runs a single computation cycle.
   * <p>
   * This is used when the view is in batch mode.
   * 
   * @param valuationTime  the time of an existing snapshot of live data, which should be used during the computation
   *                       cycle
   * @param snapshotProvider market data to use in the computation cycle
   * @param listener where the result of the computation cycle should be sent.
   * The result is NOT sent to the regular (live) listeners. Can be null, 
   * in which case the result is not sent anywhere. 
   * @throws IllegalStateException  if the view has not been initialized
   */
  void runOneCycle(long valuationTime, LiveDataSnapshotProvider snapshotProvider, ComputationResultListener listener);
  
  /**
   * Gets the permission provider
   * 
   * @return the permission provider
   */
  ViewPermissionProvider getPermissionProvider();

  /**
   * Suspends all operations on the view, blocking until everything is in a suspendable state. While suspended,
   * any operations which would alter the state of the view will block until {@link #resume} is called.
   */
  void suspend();

  /**
   * Resumes operations on the view suspended by {@link #suspend}.
   */
  void resume();
  
  /**
   * Gets the live data snapshot provider used for all live mode calculations.
   * 
   * @return the live data snapshot provider, not null
   */
  LiveDataSnapshotProvider getLiveDataSnapshotProvider();
  
}
