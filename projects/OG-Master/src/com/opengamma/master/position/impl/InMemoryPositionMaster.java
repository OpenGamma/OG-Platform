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
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;

/**
 * An in-memory implementation of a position master.
 */
public class InMemoryPositionMaster implements PositionMaster {
  
  /**
   * The default scheme used for each {@link UniqueId}.
   */
  public static final String DEFAULT_OID_SCHEME = "MemPos";

  /**
   * A cache of position by identifier.
   */
  private final ConcurrentMap<ObjectId, PositionDocument> _storePositions = new ConcurrentHashMap<ObjectId, PositionDocument>();
  /**
   * A cache of time-series points by identifier.
   */
  private final ConcurrentMap<ObjectId, ManageableTrade> _storeTrades = new ConcurrentHashMap<ObjectId, ManageableTrade>();
  /**
   * The supplied of identifiers.
   */
  private final Supplier<ObjectId> _objectIdSupplier;
  /**
   * The change manager.
   */
  private final ChangeManager _changeManager;

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
    ArgumentChecker.notNull(objectIdSupplier, "objectIdSupplier");
    ArgumentChecker.notNull(changeManager, "changeManager");
    _objectIdSupplier = objectIdSupplier;
    _changeManager = changeManager;
  }

  @Override
  public PositionDocument get(UniqueId uniqueId) {
    return get(uniqueId, VersionCorrection.LATEST);
  }

  @Override
  public PositionDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final PositionDocument document = _storePositions.get(objectId.getObjectId());
    if (document == null) {
      throw new DataNotFoundException("Position not found: " + objectId);
    }
    return clonePositionDocument(document);
  }

  private PositionDocument clonePositionDocument(PositionDocument document) {
    PositionDocument clone = JodaBeanUtils.clone(document);
    clone.setPosition(new ManageablePosition(document.getPosition()));
    return clone;
  }

  @Override
  public PositionDocument add(PositionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPosition(), "document.position");
    
    final ObjectId objectId = _objectIdSupplier.get();
    final UniqueId uniqueId = objectId.atVersion("");
    final Instant now = Instant.now();
    
    final PositionDocument clonedDoc = clonePositionDocument(document);
    setDocumentID(document, clonedDoc, uniqueId);    
    setVersionTimes(document, clonedDoc, now, null, now, null);
    _storePositions.put(objectId, clonedDoc);
    storeTrades(clonedDoc.getPosition().getTrades(), document.getPosition().getTrades(), uniqueId);
    _changeManager.entityChanged(ChangeType.ADDED, null, uniqueId, now);
    return document;
  }

  private void setDocumentID(final PositionDocument document, final PositionDocument clonedDoc, final UniqueId uniqueId) {
    document.getPosition().setUniqueId(uniqueId);
    clonedDoc.getPosition().setUniqueId(uniqueId);
    clonedDoc.setUniqueId(uniqueId);
    document.setUniqueId(uniqueId);
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
    ArgumentChecker.notNull(document.getPosition(), "document.position");
    
    final UniqueId uniqueId = document.getUniqueId();
    final Instant now = Instant.now();
    final PositionDocument storedDocument = _storePositions.get(uniqueId.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Position not found: " + uniqueId);
    }
    
    final PositionDocument clonedDoc = clonePositionDocument(document);
    removeTrades(storedDocument.getPosition().getTrades());
    
    setVersionTimes(document, clonedDoc, now, null, now, null);
    
    if (_storePositions.replace(uniqueId.getObjectId(), storedDocument, clonedDoc) == false) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    storeTrades(clonedDoc.getPosition().getTrades(), document.getPosition().getTrades(), uniqueId);
    _changeManager.entityChanged(ChangeType.UPDATED, uniqueId, document.getUniqueId(), now);
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
  public void remove(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    if (_storePositions.remove(uniqueId.getObjectId()) == null) {
      throw new DataNotFoundException("Position not found: " + uniqueId);
    }
  }

  @Override
  public PositionDocument correct(PositionDocument document) {
    return update(document);
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  @Override
  public PositionSearchResult search(PositionSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    final List<PositionDocument> list = new ArrayList<PositionDocument>();
    for (PositionDocument doc : _storePositions.values()) {
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
