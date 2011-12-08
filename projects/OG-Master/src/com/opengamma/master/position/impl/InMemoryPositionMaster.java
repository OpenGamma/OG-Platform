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
    return document;
  }

  @Override
  public PositionDocument add(PositionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPosition(), "document.position");
    
    final ObjectId objectId = _objectIdSupplier.get();
    final UniqueId uniqueId = objectId.atVersion("");
    final PositionDocument cloned = JodaBeanUtils.clone(document);
    final ManageablePosition position = cloned.getPosition();
    position.setUniqueId(uniqueId);
    final Instant now = Instant.now();
    cloned.setVersionFromInstant(now);
    cloned.setCorrectionFromInstant(now);
    cloned.setUniqueId(uniqueId);
    _storePositions.put(objectId, cloned);
    storeTrades(position.getTrades(), uniqueId);
    _changeManager.entityChanged(ChangeType.ADDED, null, uniqueId, now);
    return cloned;
  }

  private void storeTrades(List<ManageableTrade> trades, UniqueId parentPositionId) {
    for (ManageableTrade trade : trades) {
      final ObjectId objectId = _objectIdSupplier.get();
      final UniqueId uniqueId = objectId.atVersion("");
      trade.setUniqueId(uniqueId);
      trade.setParentPositionId(parentPositionId);
      _storeTrades.put(objectId, trade);
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
    removeTrades(storedDocument.getPosition().getTrades());
    document.setVersionFromInstant(now);
    document.setVersionToInstant(null);
    document.setCorrectionFromInstant(now);
    document.setCorrectionToInstant(null);
    if (_storePositions.replace(uniqueId.getObjectId(), storedDocument, document) == false) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    storeTrades(document.getPosition().getTrades(), uniqueId);
    _changeManager.entityChanged(ChangeType.UPDATED, uniqueId, document.getUniqueId(), now);
    return document;
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
        list.add(doc);
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
    ManageableTrade manageableTrade = _storeTrades.get(tradeId.getObjectId());
    if (manageableTrade == null) {
      throw new DataNotFoundException("Trade not found: " + tradeId.getObjectId());
    }
    return manageableTrade;
  }

}
