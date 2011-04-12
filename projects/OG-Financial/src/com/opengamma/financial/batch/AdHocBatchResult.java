/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.util.ArgumentChecker;

/**
 * Ad hoc batch result.
 */
public class AdHocBatchResult {
  
  private final BatchId _batchId;
  private final ViewComputationResultModel _result;
  
  public AdHocBatchResult(BatchId batchId,
      ViewComputationResultModel result) {
    ArgumentChecker.notNull(batchId, "batchId");
    ArgumentChecker.notNull(result, "result");
    
    _batchId = batchId;
    _result = result;
  }

  public BatchId getBatchId() {
    return _batchId;
  }

  public ViewComputationResultModel getResult() {
    return _result;
  }
  
}
