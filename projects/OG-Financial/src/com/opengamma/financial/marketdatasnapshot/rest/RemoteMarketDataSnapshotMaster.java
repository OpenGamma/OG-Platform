/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdatasnapshot.rest;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestRuntimeException;
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

  private FudgeSerializer getFudgeSerializer() {
    return new FudgeSerializer(getFudgeContext());
  }

  private FudgeDeserializer getFudgeDeserializer() {
    return new FudgeDeserializer(getFudgeContext());
  }

  private RestTarget getTargetBase() {
    return _targetBase;
  }

  @Override
  public MarketDataSnapshotSearchResult search(final MarketDataSnapshotSearchRequest request) {
    try {
      final FudgeMsgEnvelope response = getRestClient().post(getTargetBase().resolve("search"), getFudgeSerializer().objectToFudgeMsg(request));
      return getFudgeDeserializer().fudgeMsgToObject(MarketDataSnapshotSearchResult.class, response.getMessage());
    } catch (RestRuntimeException ex) {
      throw ex.translate();
    }
  }

  @Override
  public MarketDataSnapshotHistoryResult history(final MarketDataSnapshotHistoryRequest request) {
    // TODO: this is wrong; the target path should be snapshots/uniqueId/... with a GET operation
    try {
      final FudgeMsgEnvelope response = getRestClient().post(getTargetBase().resolve("history"), getFudgeSerializer().objectToFudgeMsg(request));
      return getFudgeDeserializer().fudgeMsgToObject(MarketDataSnapshotHistoryResult.class, response.getMessage());
    } catch (RestRuntimeException ex) {
      throw ex.translate();
    }
  }

  @Override
  public MarketDataSnapshotDocument add(final MarketDataSnapshotDocument document) {
    try {
      final FudgeMsgEnvelope response = getRestClient().post(getTargetBase().resolve("add"), getFudgeSerializer().objectToFudgeMsg(document));
      UniqueId snapshotId = getFudgeDeserializer().fieldValueToObject(UniqueId.class, response.getMessage().getByName("uniqueId"));
      document.setUniqueId(snapshotId);
      document.getSnapshot().setUniqueId(snapshotId);
      return document;
    } catch (RestRuntimeException ex) {
      throw ex.translate();
    }
  }

  @Override
  public MarketDataSnapshotDocument correct(final MarketDataSnapshotDocument document) {
    throw new UnsupportedOperationException();
  }

  @Override
  public MarketDataSnapshotDocument get(final UniqueId uniqueId) {
    try {
      // [PLAT-1316] Note the document returned is incomplete
      final FudgeMsg response = getRestClient().getMsg(getTargetBase().resolveBase("snapshots").resolve(uniqueId.toString()));
      if (response == null) {
        throw new DataNotFoundException("Unique identifier " + uniqueId + " not found");
      }
      final FudgeDeserializer deserializer = getFudgeDeserializer();
      MarketDataSnapshotDocument document = deserializer.fudgeMsgToObject(MarketDataSnapshotDocument.class, response);
      return document;
    } catch (RestRuntimeException ex) {
      throw ex.translate();
    }
  }

  @Override
  public MarketDataSnapshotDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void remove(final UniqueId uniqueId) {
    try {
      getRestClient().delete(getTargetBase().resolveBase("snapshots").resolve(uniqueId.toString()));
    } catch (RestRuntimeException ex) {
      throw ex.translate();
    }
  }

  @Override
  public MarketDataSnapshotDocument update(final MarketDataSnapshotDocument document) {
    try {
      final FudgeMsgEnvelope response = getRestClient().put(getTargetBase().resolveBase("snapshots").resolve(document.getUniqueId().toString()),
          getFudgeSerializer().objectToFudgeMsg(document));
      UniqueId snapshotId = getFudgeDeserializer().fieldValueToObject(UniqueId.class, response.getMessage().getByName("uniqueId"));
      document.setUniqueId(snapshotId);
      return document;
    } catch (RestRuntimeException ex) {
      throw ex.translate();
    }
  }

  @Override
  public ChangeManager changeManager() {
    throw new UnsupportedOperationException();
  }
}
