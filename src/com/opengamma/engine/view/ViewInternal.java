/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import com.opengamma.engine.view.calc.SingleComputationCycle;
import com.opengamma.engine.view.compilation.ViewEvaluationModel;
import com.opengamma.engine.view.permission.ViewPermissionProvider;

/**
 * Exposes engine-level access to a view, including access to data structures which should not be available externally.
 */
public interface ViewInternal extends View {
    
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
