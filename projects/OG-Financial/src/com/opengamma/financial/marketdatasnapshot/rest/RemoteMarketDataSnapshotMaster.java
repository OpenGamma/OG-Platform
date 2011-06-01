/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdatasnapshot.rest;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.listener.MasterChangeManager;
import com.opengamma.master.marketdatasnapshot.ManageableMarketDataSnapshot;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of a {@link MarketDataSnapshotMaster} that connects to the {@link MarketDataSnapshotMasterResource} REST implementation.
 */
public final class RemoteMarketDataSnapshotMaster implements MarketDataSnapshotMaster {

  private final RestClient _restClient;

  private final RestTarget _targetBase;

  public RemoteMarketDataSnapshotMaster(final FudgeContext fudgeContext, final RestTarget targetBase) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(targetBase, "targetBase");
    _restClient = RestClient.getInstance(fudgeContext, null);
    _targetBase = targetBase;
  }

  private RestClient getRestClient() {
    return _restClient;
  }

  private FudgeContext getFudgeContext() {
    return getRestClient().getFudgeContext();
  }

  private FudgeSerializationContext getFudgeSerializationContext() {
    return new FudgeSerializationContext(getFudgeContext());
  }

  private FudgeDeserializationContext getFudgeDeserializationContext() {
    return new FudgeDeserializationContext(getFudgeContext());
  }

  private RestTarget getTargetBase() {
    return _targetBase;
  }

  @Override
  public MarketDataSnapshotSearchResult search(final MarketDataSnapshotSearchRequest request) {
    final FudgeMsgEnvelope response = getRestClient().post(getTargetBase().resolve("search"), getFudgeSerializationContext().objectToFudgeMsg(request));
    return getFudgeDeserializationContext().fudgeMsgToObject(MarketDataSnapshotSearchResult.class, response.getMessage());
  }

  public MarketDataSnapshotMetadataSearchResult searchMetadata(final MarketDataSnapshotSearchRequest request) {
    final FudgeMsgEnvelope response = getRestClient().post(getTargetBase().resolve("searchMetadata"), getFudgeSerializationContext().objectToFudgeMsg(request));
    return getFudgeDeserializationContext().fudgeMsgToObject(MarketDataSnapshotMetadataSearchResult.class, response.getMessage());
  }

  @Override
  public MarketDataSnapshotDocument add(final MarketDataSnapshotDocument document) {
    throw new UnsupportedOperationException();
  }

  @Override
  public MarketDataSnapshotDocument correct(final MarketDataSnapshotDocument document) {
    throw new UnsupportedOperationException();
  }

  @Override
  public MarketDataSnapshotDocument get(final UniqueIdentifier uniqueId) {
    // [PLAT-1316] Note the document returned is incomplete
    final FudgeMsg response = getRestClient().getMsg(getTargetBase().resolveBase("securities").resolve(uniqueId.toString()));
    if (response == null) {
      throw new DataNotFoundException("Unique identifier " + uniqueId + " not found");
    }
    final MarketDataSnapshotDocument document = new MarketDataSnapshotDocument();
    final FudgeDeserializationContext fdc = getFudgeDeserializationContext();
    document.setUniqueId(fdc.fieldValueToObject(UniqueIdentifier.class, response.getByName("uniqueId")));
    final StructuredMarketDataSnapshot snapshot = fdc.fieldValueToObject(StructuredMarketDataSnapshot.class, response.getByName("snapshot"));
    document.setSnapshot(new ManageableMarketDataSnapshot(snapshot));
    return document;
  }

  @Override
  public MarketDataSnapshotDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void remove(final UniqueIdentifier uniqueId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public MarketDataSnapshotDocument update(final MarketDataSnapshotDocument document) {
    throw new UnsupportedOperationException();
  }

  @Override
  public MasterChangeManager changeManager() {
    throw new UnsupportedOperationException();
  }
}
