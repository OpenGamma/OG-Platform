/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.time.Instant;

import org.joda.beans.JodaBeanUtils;

import com.google.common.base.Supplier;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.id.*;
import com.opengamma.master.SimpleAbstractInMemoryMaster;
import com.opengamma.master.position.*;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;

/**
 * An in-memory implementation of a position master.
 */
public class InMemoryPositionMaster extends SimpleAbstractInMemoryMaster<ManageablePosition, PositionDocument> implements PositionMaster {

  /**
   * The default scheme used for each {@link UniqueId}.
   */
  public static final String DEFAULT_OID_SCHEME = "MemPos";

  /**
   * A cache of trades by identifier.
   */
  private final ConcurrentMap<ObjectId, ManageableTrade> _storeTrades = new ConcurrentHashMap<ObjectId, ManageableTrade>();


  /**
   * Creates an instance.
   */
  public InMemoryPositionMaster() {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME));
  }

  /**
   * Creates an instance specifying the change manager.
   *
   * @param changeManager  the change manager, not null
   */
  public InMemoryPositionMaster(final ChangeManager changeManager) {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME), changeManager);
  }

  /**
   * Creates an instance specifying the supplier of object identifiers.
   *
   * @param objectIdSupplier  the supplier of object identifiers, not null
   */
  public InMemoryPositionMaster(final Supplier<ObjectId> objectIdSupplier) {
    this(objectIdSupplier, new BasicChangeManager());
  }

  /**
   * Creates an instance specifying the supplier of object identifiers and change manager.
   *
   * @param objectIdSupplier  the supplier of object identifiers, not null
   * @param changeManager  the change manager, not null
   */
  public InMemoryPositionMaster(final Supplier<ObjectId> objectIdSupplier, final ChangeManager changeManager) {
    super(objectIdSupplier, changeManager);
  }

  @Override
  protected void validateDocument(PositionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getObject(), "document.position");
  }

  @Override
  public PositionDocument get(UniqueId uniqueId) {
    return get(uniqueId, VersionCorrection.LATEST);
  }

  @Override
  public PositionDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final PositionDocument document = _store.get(objectId.getObjectId());
    if (document == null) {
      throw new DataNotFoundException("Position not found: " + objectId);
    }
    return clonePositionDocument(document);
  }

  private PositionDocument clonePositionDocument(PositionDocument document) {
    PositionDocument clone = JodaBeanUtils.clone(document);
    clone.setObject(new ManageablePosition(document.getObject()));
    return clone;
  }

  @Override
  public PositionDocument add(PositionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getObject(), "document.position");

    final ObjectId objectId = _objectIdSupplier.get();
    final UniqueId uniqueId = objectId.atVersion("");
    final Instant now = Instant.now();

    final PositionDocument clonedDoc = clonePositionDocument(document);
    setDocumentID(document, clonedDoc, uniqueId);
    setVersionTimes(document, clonedDoc, now, null, now, null);
    _store.put(objectId, clonedDoc);
    storeTrades(clonedDoc.getObject().getTrades(), document.getObject().getTrades(), uniqueId);
    _changeManager.entityChanged(ChangeType.ADDED, objectId, document.getVersionFromInstant(), document.getVersionToInstant(), now);
    return document;
  }

  private void setDocumentID(final PositionDocument document, final PositionDocument clonedDoc, final UniqueId uniqueId) {
    document.getObject().setUniqueId(uniqueId);
    clonedDoc.getObject().setUniqueId(uniqueId);
    document.setUniqueId(uniqueId);
    clonedDoc.setUniqueId(uniqueId);
  }

  private void storeTrades(List<ManageableTrade> clonedTrades, List<ManageableTrade> trades, UniqueId parentPositionId) {
    for (int i = 0; i < clonedTrades.size(); i++) {
      final ObjectId objectId = _objectIdSupplier.get();
      final UniqueId uniqueId = objectId.atVersion("");
      ManageableTrade origTrade = trades.get(i);
      ManageableTrade clonedTrade = clonedTrades.get(i);
      clonedTrade.setUniqueId(uniqueId);
      origTrade.setUniqueId(uniqueId);
      clonedTrade.setParentPositionId(parentPositionId);
      origTrade.setParentPositionId(parentPositionId);
      _storeTrades.put(objectId, clonedTrade);
    }
  }

  @Override
  public PositionDocument update(PositionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    ArgumentChecker.notNull(document.getObject(), "document.position");

    final UniqueId uniqueId = document.getUniqueId();
    final Instant now = Instant.now();
    final PositionDocument storedDocument = _store.get(uniqueId.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Position not found: " + uniqueId);
    }

    final PositionDocument clonedDoc = clonePositionDocument(document);
    removeTrades(storedDocument.getObject().getTrades());

    setVersionTimes(document, clonedDoc, now, null, now, null);

    if (_store.replace(uniqueId.getObjectId(), storedDocument, clonedDoc) == false) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    storeTrades(clonedDoc.getObject().getTrades(), document.getObject().getTrades(), uniqueId);
    _changeManager.entityChanged(ChangeType.CHANGED, document.getObjectId(), storedDocument.getVersionFromInstant(), document.getVersionToInstant(), now);
    return document;
  }

  private void setVersionTimes(PositionDocument document, final PositionDocument clonedDoc,
                               final Instant versionFromInstant, final Instant versionToInstant, final Instant correctionFromInstant, final Instant correctionToInstant) {

    clonedDoc.setVersionFromInstant(versionFromInstant);
    document.setVersionFromInstant(versionFromInstant);

    clonedDoc.setVersionToInstant(versionToInstant);
    document.setVersionToInstant(versionToInstant);

    clonedDoc.setCorrectionFromInstant(correctionFromInstant);
    document.setCorrectionFromInstant(correctionFromInstant);

    clonedDoc.setCorrectionToInstant(correctionToInstant);
    document.setCorrectionToInstant(correctionToInstant);
  }

  private void removeTrades(List<ManageableTrade> trades) {
    for (ManageableTrade trade : trades) {
      if (_storeTrades.remove(trade.getUniqueId().getObjectId()) == null) {
        throw new DataNotFoundException("Trade not found: " + trade.getUniqueId());
      }
    }
  }

  @Override
  public void remove(ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");
    PositionDocument storedDocument = _store.remove(objectIdentifiable.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Position not found: " + objectIdentifiable);
    }
    removeTrades(storedDocument.getObject().getTrades());
    _changeManager.entityChanged(ChangeType.REMOVED, objectIdentifiable.getObjectId(), null, null, Instant.now());
  }

  @Override
  public PositionDocument correct(PositionDocument document) {
    return update(document);
  }

  @Override
  public PositionSearchResult search(PositionSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final List<PositionDocument> list = new ArrayList<PositionDocument>();
    for (PositionDocument doc : _store.values()) {
      if (request.matches(doc)) {
        list.add(clonePositionDocument(doc));
      }
    }
    final PositionSearchResult result = new PositionSearchResult();
    result.setPaging(Paging.of(request.getPagingRequest(), list));
    result.getDocuments().addAll(request.getPagingRequest().select(list));
    return result;
  }

  @Override
  public PositionHistoryResult history(PositionHistoryRequest request) {
    throw new UnsupportedOperationException("History request not supported by InMemoryPositionMaster");
  }

  @Override
  public ManageableTrade getTrade(UniqueId tradeId) {
    ArgumentChecker.notNull(tradeId, "tradeId");
    ManageableTrade trade = _storeTrades.get(tradeId.getObjectId());
    if (trade == null) {
      throw new DataNotFoundException("Trade not found: " + tradeId.getObjectId());
    }
    return JodaBeanUtils.clone(trade);
  }

}
