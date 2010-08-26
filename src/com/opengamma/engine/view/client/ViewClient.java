/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

import java.util.Set;

import com.opengamma.engine.livedata.LiveDataInjector;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ComputationResultListener;
import com.opengamma.engine.view.DeltaComputationResultListener;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.livedata.msg.UserPrincipal;

/**
 * Exposes a certain features of a {@link View} that are useful from a client perspective.
 * <p>
 * NOTE: The result listeners must be maintained as a set. It should be possible to add the same listener multiple times but
 * only have it receive one notification. After repeated calls to add, a single call to remove the listener should stop any
 * further notifications.
 */
public interface ViewClient {
  
  /**
   * Gets the name of the view.
   * 
   * @return the name of the view, not null
   */
  String getName();
  
  /**
   * Indicates whether the view has been started. A view in this state will perform a computation cycle when changes to
   * its inputs are detected.  
   * 
   * @return  <code>true</code> if the view has been started, <code>false</code> otherwise
   */
  boolean isLiveComputationRunning();
  
  /**
   * Gets the portfolio associated with the view, if set. Position and aggregate position requirements are implicitly
   * made with reference to this portfolio.
   * 
   * @return  the portfolio associated with the view, or <code>null</code> if no reference portfolio exists for the
   *          view
   */
  Portfolio getPortfolio();
  
  /**
   * Gets a set of all security types present in the view's dependency graph; that is, all security types on which
   * calculations must be performed.
   * 
   * @return  a set of all security types in the view's dependency graph, not null
   */
  Set<String> getAllSecurityTypes();
  
  /**
   * Gets a set containing the name of every portfolio-level (i.e. position and/or aggregate position) requirement.
   * These would form the columns of the output if the rows represented the portfolio hierarchy.
   * 
   * @return  a set containing the name of every portfolio-level requirement, not null
   */
  Set<String> getAllPortfolioRequirementNames();
  
  /**
   * Gets a set containing the name of every value required for a particular type of security.
   * 
   * @param securityType  the type of security, not null
   * @return  a set containing the name of every value required for the given security
   */
  Set<String> getRequirementNames(String securityType);
  
  /**
   * Gets a set containing details of the live data required for successful computation of the view.
   * 
   * @return  a set containing a {@link ValueRequirement} for each item of live data required
   */
  Set<ValueRequirement> getRequiredLiveData();
  
  /**
   * Indicates whether a computation cycle has completed yet
   * 
   * @return  <code>true</code> if a computation cycle has completed and the result is available, <code>false</code>
   *          otherwise
   */
  boolean isResultAvailable();
  
  /**
   * Runs a single computation cycle. This cannot be used while live computation is running.
   * 
   * @throws IllegalStateException  if individual computation cycles cannot be performed because the view is in an
   *                                incompatible state
   */
  void performComputation();
  
  /**
   * Runs a single computation cycle, using live data snapshotted data at the given time.
   * 
   * @param snapshotTime  the time of an existing snapshot of live data, which should be used during the computation
   *                      cycle
   * @throws IllegalStateException  if individual computation cycles cannot be performed because the view is in an
   *                                incompatible state  
   */
  void performComputation(long snapshotTime);
  
  /**
   * Gets the result of the latest completed computation cycle.
   *  
   * @return  the latest result, or <code>null</code> if no result yet exists
   * @see #isResultAvailable()
   */
  ViewComputationResultModel getMostRecentResult();
  
  /**
   * Adds a listener to receive notifications when new results are available.
   * <p>
   * Listeners are maintained with set semantics: they will receive no more than one notification for each event no
   * matter how many times they are added, and it is sufficient to remove a listener only once.
   *  
   * @param listener  the listener to add, not null
   */
  void addComputationResultListener(ComputationResultListener listener);
  
  /**
   * Stops a listener from receiving notifications when new results are available.
   * <p>
   * Listeners are maintained with set semantics: they will receive no more than one notification for each event no
   * matter how many times they are added, and it is sufficient to remove a listener only once.
   * 
   * @param listener  the listener to remove, not null
   */
  void removeComputationResultListener(ComputationResultListener listener);
  
  /**
   * Adds a listener to receive notifications when new and different results are available. Only the changes are
   * provided.
   * <p>
   * Listeners are maintained with set semantics: they will receive no more than one notification for each event no
   * matter how many times they are added, and it is sufficient to remove a listener only once.
   * 
   * @param listener  the listener to add, not null
   */
  void addDeltaResultListener(DeltaComputationResultListener listener);

  /**
   * Stops a listener from receiving delta notifications.
   * <p>
   * Listeners are maintained with set semantics: they will receive no more than one notification for each event no
   * matter how many times they are added, and it is sufficient to remove a listener only once.
   * 
   * @param listener  the listener to remove, not null
   */
  void removeDeltaResultListener(DeltaComputationResultListener listener);
  
  /**
   * Gets the user under which this clients operates.
   * 
   * @return the user, not null
   */
  UserPrincipal getUser();
  
  /**
   * Gets the live data injector for storing arbitrary user live data for the view.
   * 
   * @return the live data injector, not null
   */
  LiveDataInjector getLiveDataInjector();
}
