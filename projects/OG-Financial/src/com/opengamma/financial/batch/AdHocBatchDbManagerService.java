/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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

  /**
   * The underlying manager.
   */
  private final AdHocBatchDbManager _underlying;
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;

  /**
   * Creates an instance.
   * 
   * @param underlying  the underlying manager, not null
   * @param fudgeContext  the Fudge context, not null
   */
  public AdHocBatchDbManagerService(AdHocBatchDbManager underlying, FudgeContext fudgeContext) {
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying master.
   * 
   * @return the underlying master, not null
   */
  protected AdHocBatchDbManager getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the Fudge context.
   * 
   * @return the Fudge context, not null
   */
  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  //-------------------------------------------------------------------------
  @POST
  public void post(final FudgeMsgEnvelope payload) {
    final FudgeDeserializationContext dctx = new FudgeDeserializationContext(getFudgeContext());
    final AdHocBatchResult batchResult = dctx.fieldValueToObject(AdHocBatchResult.class, payload.getMessage().getByName("batch"));
    getUnderlying().write(batchResult);
  }

}
