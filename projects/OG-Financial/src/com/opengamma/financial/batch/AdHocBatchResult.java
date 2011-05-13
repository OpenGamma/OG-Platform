/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.util.ArgumentChecker;

/**
 * The ad hoc batch result.
 */
public class AdHocBatchResult {

  /**
   * The batch identifier.
   */
  private final BatchId _batchId;
  /**
   * The batch result.
   */
  private final ViewComputationResultModel _result;

  /**
   * Creates instance.
   * 
   * @param batchId  the batch id, not null
   * @param result  the batch result, not null
   */
  public AdHocBatchResult(BatchId batchId, ViewComputationResultModel result) {
    ArgumentChecker.notNull(batchId, "batchId");
    ArgumentChecker.notNull(result, "result");
    _batchId = batchId;
    _result = result;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the batch id.
   * 
   * @return the batch id, not null
   */
  public BatchId getBatchId() {
    return _batchId;
  }

  /**
   * Gets the batch result.
   * 
   * @return the batch result, not null
   */
  public ViewComputationResultModel getResult() {
    return _result;
  }

}
