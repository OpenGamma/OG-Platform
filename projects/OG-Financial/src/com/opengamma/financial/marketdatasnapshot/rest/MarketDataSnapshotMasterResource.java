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
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.marketdatasnapshot.ManageableMarketDataSnapshot;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;

/**
 * RESTful resource publishing details from a {link MarketDataSnapshotMaster}.
 */
public class MarketDataSnapshotMasterResource {
  private final MarketDataSnapshotMaster _snapshotMaster;

  private final FudgeContext _fudgeContext;

  public MarketDataSnapshotMasterResource(final MarketDataSnapshotMaster snapshotMaster, final FudgeContext fudgeContext) {
    _snapshotMaster = snapshotMaster;
    _fudgeContext = fudgeContext;
  }

  
  private FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  

  private MarketDataSnapshotMaster getSnapshotMaster() {
    return _snapshotMaster;
  }


  public FudgeSerializationContext getFudgeSerializationContext() {
    return new FudgeSerializationContext(getFudgeContext());
  }

  public FudgeDeserializationContext getFudgeDeserializationContext() {
    return new FudgeDeserializationContext(getFudgeContext());
  }
  
  
  @POST
  @Path("search")
  public FudgeMsgEnvelope search(final FudgeMsgEnvelope payload) {
    final MarketDataSnapshotSearchRequest request = getFudgeDeserializationContext().fudgeMsgToObject(MarketDataSnapshotSearchRequest.class, payload.getMessage());
    final MarketDataSnapshotSearchResult result = getSnapshotMaster().search(request);
    return new FudgeMsgEnvelope(getFudgeSerializationContext().objectToFudgeMsg(result));
  }
  
  
  @POST
  @Path("add")
  public FudgeMsgEnvelope add(final FudgeMsgEnvelope payload) {
    final FudgeDeserializationContext dctx = new FudgeDeserializationContext(getFudgeContext());
    final ManageableMarketDataSnapshot snapshotDefinition = dctx.fieldValueToObject(ManageableMarketDataSnapshot.class, payload.getMessage().getByName("snapshot"));
        
    MarketDataSnapshotDocument document = new MarketDataSnapshotDocument(snapshotDefinition);
    document = _snapshotMaster.add(document);
    if (document == null) {
      return null;
    }
    final MutableFudgeMsg resp = getFudgeContext().newMessage();
    resp.add("uniqueId", document.getUniqueId().toFudgeMsg(getFudgeContext()));
    return new FudgeMsgEnvelope(resp);
  }

  @GET
  @Path("snapshots/{uid}")
  public FudgeMsgEnvelope get(@PathParam("uid") final String uidString) {
    final UniqueIdentifier uid = UniqueIdentifier.parse(uidString);
    try {
      final MarketDataSnapshotDocument document = _snapshotMaster.get(uid);
      final FudgeSerializationContext sctx = new FudgeSerializationContext(getFudgeContext());
      final MutableFudgeMsg resp = sctx.newMessage();
      resp.add("uniqueId", document.getUniqueId().toFudgeMsg(getFudgeContext()));
      sctx.addToMessageWithClassHeaders(resp, "snapshot", null, document.getSnapshot(), StructuredMarketDataSnapshot.class);
      return new FudgeMsgEnvelope(resp);
    } catch (DataNotFoundException e) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  @DELETE
  @Path("snapshots/{uid}")
  public FudgeMsgEnvelope remove(@PathParam("uid") final String uidString) {
    final UniqueIdentifier uid = UniqueIdentifier.parse(uidString);
    try {
      _snapshotMaster.remove(uid);
      return null;
    } catch (DataNotFoundException e) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
  }

  @PUT
  @Path("snapshots/{uid}")
  public FudgeMsgEnvelope update(@PathParam("uid") final String uidString, final FudgeMsgEnvelope payload) {
    final UniqueIdentifier uid = UniqueIdentifier.parse(uidString);
    final FudgeDeserializationContext dctx = new FudgeDeserializationContext(getFudgeContext());
    final ManageableMarketDataSnapshot snapshot = dctx.fieldValueToObject(ManageableMarketDataSnapshot.class, payload.getMessage().getByName("snapshot"));
    
    MarketDataSnapshotDocument document = new MarketDataSnapshotDocument(uid, snapshot);
    try {
      document = _snapshotMaster.update(document);
      if (document == null) {
        return null;
      }
      final MutableFudgeMsg resp = getFudgeContext().newMessage();
      resp.add("uniqueId", document.getUniqueId().toFudgeMsg(getFudgeContext()));
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
    message.add("snapshotMaster", _snapshotMaster.toString());
    return new FudgeMsgEnvelope(message);
  }
}
