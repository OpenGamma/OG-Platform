/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.currency.CurrencyMatrixSource;

/**
 * 
 */
public class CurrencyMatrixSourceResource {

  private final CurrencyMatrixSource _underlying;
  private final FudgeContext _fudgeContext;

  public CurrencyMatrixSourceResource(final CurrencyMatrixSource underlying, final FudgeContext fudgeContext) {
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  protected CurrencyMatrixSource getUnderlying() {
    return _underlying;
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  protected FudgeMsgEnvelope matrixToMsgEnvelope(final CurrencyMatrix matrix) {
    if (matrix == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    final FudgeSerializationContext context = new FudgeSerializationContext(getFudgeContext());
    final MutableFudgeFieldContainer msg = context.newMessage();
    context.objectToFudgeMsgWithClassHeaders(msg, "matrix", null, matrix, CurrencyMatrix.class);
    return new FudgeMsgEnvelope(msg);
  }

  @GET
  @Path("{name}")
  public FudgeMsgEnvelope getDefinition(@PathParam("name") String name) {
    return matrixToMsgEnvelope(getUnderlying().getCurrencyMatrix(name));
  }

}
