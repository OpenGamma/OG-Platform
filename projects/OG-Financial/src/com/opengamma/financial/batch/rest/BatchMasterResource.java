/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch.rest;

import com.opengamma.DataNotFoundException;
import com.opengamma.financial.batch.BatchMaster;
import com.opengamma.financial.batch.BatchSearchRequest;
import com.opengamma.financial.batch.BatchSearchResult;
import com.opengamma.id.UniqueId;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * REST resource wrapper for a {@link BatchMaster}.
 */
public class BatchMasterResource extends AbstractResource<BatchMaster> {

  public BatchMasterResource(FudgeContext fudgeContext, BatchMaster batchMaster) {
    super(fudgeContext, batchMaster);
  }

  /**
   * Resource for time series objects referenced by UID
   */
  public class BatchRunResource {

    private final UniqueId _uid;

    private BatchRunResource(final UniqueId uid) {
      _uid = uid;
    }

    @GET
    public FudgeMsgEnvelope get() {
      try {
        return new FudgeMsgEnvelope(getFudgeSerializer().objectToFudgeMsg(getUnderlying().get(_uid)));
      } catch (DataNotFoundException e) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
    }

    @DELETE
    public FudgeMsgEnvelope delete() {
      try {        
        getUnderlying().delete(_uid);        
        return null;
      } catch (DataNotFoundException e) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
    }
  }

  @POST
  @Path("search")
  public FudgeMsgEnvelope search(final FudgeMsgEnvelope payload) {
    final BatchSearchRequest request = getFudgeDeserializer().fudgeMsgToObject(BatchSearchRequest.class, payload.getMessage());
    final BatchSearchResult result = getUnderlying().search(request);
    return new FudgeMsgEnvelope(getFudgeSerializer().objectToFudgeMsg(result));
  }

  @GET
  @Path("batchRun")
  public FudgeMsgEnvelope batchRun() {
    final BatchSearchResult result = getUnderlying().search(new BatchSearchRequest());
    return new FudgeMsgEnvelope(getFudgeSerializer().objectToFudgeMsg(result));
  }

  @GET
  @Path("batchRun/{uniqueId}")
  public BatchRunResource batchRun(@PathParam("uniqueId") final String uidString) {
    return new BatchRunResource(UniqueId.parse(uidString));
  }

}
