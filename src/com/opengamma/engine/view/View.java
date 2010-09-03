/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Set;

import com.opengamma.engine.livedata.LiveDataInjector;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.calc.SingleComputationCycle;
import com.opengamma.engine.view.compilation.ViewEvaluationModel;
import com.opengamma.livedata.msg.UserPrincipal;

/**
 * A view represents a {@link ViewDefinition} in the context of a {@link ViewProcessor}; this is everything required
 * to perform computations and listen to the output.
 */
public interface View {

  /**
   * Gets the name of the view
   * 
   * @return  the name of the view
   */
  String getName();
  
  /**
   * Gets the underlying view definition
   * 
   * @return  the underlying view definition
   */
  ViewDefinition getDefinition();
  
  /**
   * Gets the view processing context
   * 
   * @return  the view processing context
   */
  ViewProcessingContext getProcessingContext();
  
  /**
   * Gets the view evaluation model for the compiled view definition
   * 
   * @return  the view evaluation model
   */
  ViewEvaluationModel getViewEvaluationModel();
  
  /**
   * Checks that the given user has access to the live data inputs required for computation of this view.
   * 
   * @param user  the user
   */
  void assertAccessToLiveDataRequirements(UserPrincipal user);
  
  /**
   * Adds a listener to updates of the full set of computation results.
   * 
   * @param resultListener  the listener to add
   * @return  <code>true</code> if the listener was newly added, or <code>false</code> if the listener was already
   *          present. 
   */
  boolean addResultListener(ComputationResultListener resultListener);
  
  /**
   * Removes a listener from updates of the full set of computation results.
   * 
   * @param resultListener  the listener to remove
   * @return  <code>true</code> if the listener was removed, or <code>false</code> if the listener was not known.
   */
  boolean removeResultListener(ComputationResultListener resultListener);
  
  /**
   * Adds a listener to delta updates of the computation results.
   * 
   * @param deltaListener  the listener to add
   * @return  <code>true</code> if the listener was newly added, or <code>false</code> if the listener was already
   *          present. 
   */
  boolean addDeltaResultListener(DeltaComputationResultListener deltaListener);
  
  /**
   * Removes a listener from delta updates of the computation results.
   * 
   * @param deltaListener  the listener to remove
   * @return  <code>true</code> if the listener was removed, or <code>false</code> if the listener was not known.
   */  
  boolean removeDeltaResultLister(DeltaComputationResultListener deltaListener);
  
  /**
   * Notifies the view that a recalculation has been performed.
   * 
   * @param result  the result of the recalculation
   */
  void recalculationPerformed(ViewComputationResultModel result);
  
  /**
   * Creates a new computation cycle for the view, to use data from the given time.
   * 
   * @param valuationTime  the time of an existing snapshot of live data, which should be used during the computation
   *                       cycle
   * @return  the new computation cycle
   */
  SingleComputationCycle createCycle(long valuationTime);
  
  /**
   * Gets the result of the latest calculation, if applicable.
   * 
   * @return  the result of the latest calculation, or <code>null</code> if no calculation has yet completed. 
   */
  ViewComputationResultModel getLatestResult();
  
  /**
   * Gets the fully-resolved reference portfolio for the view definition.
   * 
   * @return  the fully-resolved reference portfolio for the view definition
   */
  Portfolio getPortfolio();
  
  /**
   * Gets the live data required for computation of the view
   * 
   * @return  a set of value requirements describing the live data required for computation of the view
   */
  Set<ValueRequirement> getRequiredLiveData();
  
  /**
   * Synchronously runs a single computation cycle using live data.
   */
  void runOneCycle();
  
  /**
   * Synchronously runs a single computation cycle using data snapshotted at the given time. This cannot be used while
   * live computation is running.
   * 
   * @param valuationTime  the time of an existing snapshot of live data, which should be used during the computation
   *                       cycle
   */
  void runOneCycle(long valuationTime);
  
  /**
   * Indicates whether the view has been started. A view in this state will perform a computation cycle when changes to
   * its inputs are detected.  
   * 
   * @return  <code>true</code> if the view has been started, <code>false</code> otherwise
   */
  boolean isRunning();
  
  /**
   * Gets the live data injector for storing arbitrary user live data for the view.
   * 
   * @return the live data injector, not null
   */
  LiveDataInjector getLiveDataInjector();
  
  /**
   * Starts live computation of the view.
   */
  void start();
  
  /**
   * Stops live computation of the view.
   */
  void stop();
  
}
