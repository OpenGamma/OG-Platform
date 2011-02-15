/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;

/**
 * RESTful backend for {@link AdHocBatchDbManager}.
 */
@Path("adHocBatchDbManager")
public class AdHocBatchDbManagerService {
  
  private final AdHocBatchDbManager _underlying;
  private final FudgeContext _fudgeContext;
  
  public AdHocBatchDbManagerService(AdHocBatchDbManager underlying, FudgeContext fudgeContext) {
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }
  
  protected AdHocBatchDbManager getUnderlying() {
    return _underlying;
  }
  
  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }
  
  @POST
  public void post(final FudgeMsgEnvelope payload) {
    final FudgeDeserializationContext dctx = new FudgeDeserializationContext(getFudgeContext());
    final AdHocBatchResult batchResult = dctx.fieldValueToObject(AdHocBatchResult.class, payload.getMessage().getByName("batch"));
    
    getUnderlying().write(batchResult);
  }

}
