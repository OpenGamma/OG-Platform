/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import com.opengamma.engine.view.ViewComputationResultModel;

/**
 *  Writes ad hoc results into batch DB.
 */
public interface AdHocBatchResultWriter {
  
  /**
   * Writes ad hoc batch result into batch DB.
   * <p>
   * The risk is written into the database in one fell swoop. 
   *   
   * @param result the result to be written
   */
  void write(ViewComputationResultModel result);

}
