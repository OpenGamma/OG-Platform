/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdatasnapshot.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource publishing details from a {@code MarketDataSnapshotSource}.
 */
public class MarketDataSnapshotSourceResource {

  /**
   * The underlying source.
   */
  private final MarketDataSnapshotSource _source;

  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;

  /**
   * Creates a resource to expose a source over REST.
   * 
   * @param source the source to expose, not null
   * @param fudgeContext the Fudge context, not null
   */
  public MarketDataSnapshotSourceResource(final MarketDataSnapshotSource source, final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(source, "source");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _source = source;
    _fudgeContext = fudgeContext;
  }

  /**
   * Gets the underlying source.
   * 
   * @return the source
   */
  protected MarketDataSnapshotSource getSnapshotSource() {
    return _source;
  }

  /**
   * Gets the Fudge context.
   * 
   * @return the Fudge context
   */
  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @GET
  @Path("uid/{uid}")
  public FudgeMsgEnvelope getUid(@PathParam("uid") String uid) {
    final StructuredMarketDataSnapshot snapshot = getSnapshotSource().getSnapshot(UniqueId.parse(uid));
    if (snapshot == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    final FudgeSerializer serializer = new FudgeSerializer(getFudgeContext());
    final MutableFudgeMsg message = serializer.newMessage();
    serializer.addToMessageWithClassHeaders(message, "snapshot", null, snapshot, StructuredMarketDataSnapshot.class);
    return new FudgeMsgEnvelope(message);
  }

}
