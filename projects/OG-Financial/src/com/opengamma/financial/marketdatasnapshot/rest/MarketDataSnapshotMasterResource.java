/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdatasnapshot.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeManagerResource;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdFudgeBuilder;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource publishing details from a {@code MarketDataSnapshotMaster}.
 */
public class MarketDataSnapshotMasterResource {

  /**
   * The underlying master.
   */
  private final MarketDataSnapshotMaster _snapshotMaster;
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;

  /**
   * Creates an instance.
   * 
   * @param snapshotMaster  the master, not null
   * @param fudgeContext  the Fudge context, not null
   */
  public MarketDataSnapshotMasterResource(final MarketDataSnapshotMaster snapshotMaster, final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(snapshotMaster, "snapshotMaster");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _snapshotMaster = snapshotMaster;
    _fudgeContext = fudgeContext;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying master..
   * 
   * @return the master
   */
  protected MarketDataSnapshotMaster getSnapshotMaster() {
    return _snapshotMaster;
  }

  /**
   * Gets the Fudge context.
   * 
   * @return the Fudge context
   */
  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the serializer from the context.
   * 
   * @return the serializer, not null
   */
  public FudgeSerializer getFudgeSerializer() {
    return new FudgeSerializer(getFudgeContext());
  }

  /**
   * Gets the deserializer from the context.
   * 
   * @return the deserializer, not null
   */
  public FudgeDeserializer getFudgeDeserializer() {
    return new FudgeDeserializer(getFudgeContext());
  }

  //-------------------------------------------------------------------------
  @POST
  @Path("search")
  public FudgeMsgEnvelope search(final FudgeMsgEnvelope payload) {
    final MarketDataSnapshotSearchRequest request = getFudgeDeserializer().fudgeMsgToObject(MarketDataSnapshotSearchRequest.class, payload.getMessage());
    final MarketDataSnapshotSearchResult result = getSnapshotMaster().search(request);
    return new FudgeMsgEnvelope(getFudgeSerializer().objectToFudgeMsg(result));
  }

  @POST
  @Path("history")
  public FudgeMsgEnvelope history(final FudgeMsgEnvelope payload) {
    final MarketDataSnapshotHistoryRequest request = getFudgeDeserializer().fudgeMsgToObject(MarketDataSnapshotHistoryRequest.class, payload.getMessage());
    final MarketDataSnapshotHistoryResult result = getSnapshotMaster().history(request);
    return new FudgeMsgEnvelope(getFudgeSerializer().objectToFudgeMsg(result));
  }

  @POST
  @Path("add")
  public FudgeMsgEnvelope add(final FudgeMsgEnvelope payload) {
    final ManageableMarketDataSnapshot snapshotDefinition = getFudgeDeserializer().fieldValueToObject(ManageableMarketDataSnapshot.class, payload.getMessage().getByName("snapshot"));
    MarketDataSnapshotDocument document = new MarketDataSnapshotDocument(snapshotDefinition);
    document = getSnapshotMaster().add(document);
    if (document == null) {
      return null;
    }
    final FudgeSerializer serializer = getFudgeSerializer();
    final MutableFudgeMsg resp = serializer.newMessage();
    resp.add("uniqueId", UniqueIdFudgeBuilder.toFudgeMsg(serializer, document.getUniqueId()));
    return new FudgeMsgEnvelope(resp);
  }

  @GET
  @Path("snapshots/{uid}")
  public FudgeMsgEnvelope get(@PathParam("uid") final String uidString) {
    final UniqueId uid = UniqueId.parse(uidString);
    try {
      final MarketDataSnapshotDocument document = getSnapshotMaster().get(uid);
      MutableFudgeMsg resp = getFudgeSerializer().objectToFudgeMsg(document);
      return new FudgeMsgEnvelope(resp);
    } catch (DataNotFoundException e) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }
  
  @Path("changeManager")
  public ChangeManagerResource getChangeManager() {
    return new ChangeManagerResource(_snapshotMaster.changeManager());
  }

  @DELETE
  @Path("snapshots/{uid}")
  public FudgeMsgEnvelope remove(@PathParam("uid") final String uidString) {
    final UniqueId uid = UniqueId.parse(uidString);
    try {
      getSnapshotMaster().remove(uid);
      return null;
    } catch (DataNotFoundException e) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  @PUT
  @Path("snapshots/{uid}")
  public FudgeMsgEnvelope update(@PathParam("uid") final String uidString, final FudgeMsgEnvelope payload) {
    final UniqueId uid = UniqueId.parse(uidString);
    final ManageableMarketDataSnapshot snapshot = getFudgeDeserializer().fieldValueToObject(ManageableMarketDataSnapshot.class, payload.getMessage().getByName("snapshot"));

    MarketDataSnapshotDocument document = new MarketDataSnapshotDocument(uid, snapshot);
    try {
      document = getSnapshotMaster().update(document);
      if (document == null) {
        return null;
      }
      final MutableFudgeMsg resp = getFudgeSerializer().newMessage();
      resp.add("uniqueId", UniqueIdFudgeBuilder.toFudgeMsg(getFudgeSerializer(), document.getUniqueId()));
      return new FudgeMsgEnvelope(resp);
    } catch (DataNotFoundException e) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  /**
   * For debugging purposes only.
   * 
   * @return some debug information about the state of this resource object; e.g. which underlying objects is it connected to.
   */
  @GET
  @Path("debugInfo")
  public FudgeMsgEnvelope getDebugInfo() {
    final MutableFudgeMsg message = getFudgeContext().newMessage();
    message.add("fudgeContext", getFudgeContext().toString());
    message.add("snapshotMaster", getSnapshotMaster().toString());
    return new FudgeMsgEnvelope(message);
  }

}
