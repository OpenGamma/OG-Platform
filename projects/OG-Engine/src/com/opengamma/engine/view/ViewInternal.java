/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import javax.time.InstantProvider;

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
   * Gets the view evaluation model for the compiled view definition
   * 
   * @return the view evaluation model
   */
  ViewEvaluationModel getViewEvaluationModel();
  
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
   * @return the new computation cycle
   */
  SingleComputationCycle createCycle(long valuationTime);
  
  /**
   * Gets the permission provider
   * 
   * @return the permission provider
   */
  ViewPermissionProvider getPermissionProvider();

}
