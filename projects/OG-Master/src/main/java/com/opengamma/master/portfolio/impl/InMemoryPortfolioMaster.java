/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.portfolio.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.joda.beans.JodaBeanUtils;
import org.threeten.bp.Instant;

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
import com.opengamma.master.SimpleAbstractInMemoryMaster;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.RegexUtils;
import com.opengamma.util.paging.Paging;

/**
 * An in-memory implementation of a portfolio master.
 */
public class InMemoryPortfolioMaster extends SimpleAbstractInMemoryMaster<PortfolioDocument> implements PortfolioMaster {

  /**
   * The default scheme used for each {@link UniqueId}.
   */
  public static final String DEFAULT_OID_SCHEME = "MemPrt";

  /**
   * A cache of portfolio nodes by identifier.
   */
  private final ConcurrentMap<ObjectId, ManageablePortfolioNode> _storeNodes = new ConcurrentHashMap<ObjectId, ManageablePortfolioNode>();
  private final ConcurrentMap<String, Set<PortfolioDocument>> _portfoliosByName = new ConcurrentHashMap<String, Set<PortfolioDocument>>();
  
  /**
   * Creates an instance.
   */
  public InMemoryPortfolioMaster() {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME));
  }
  
  /**
   * Creates an instance specifying the change manager.
   * 
   * @param changeManager  the change manager, not null
   */
  public InMemoryPortfolioMaster(final ChangeManager changeManager) {
    this(new ObjectIdSupplier(DEFAULT_OID_SCHEME), changeManager);
  }
  
  /**
   * Creates an instance specifying the supplier of object identifiers.
   * 
   * @param objectIdSupplier  the supplier of object identifiers, not null
   */
  public InMemoryPortfolioMaster(final Supplier<ObjectId> objectIdSupplier) {
    this(objectIdSupplier, new BasicChangeManager());
  }
  
  /**
   * Creates an instance specifying the supplier of object identifiers and change manager.
   * 
   * @param objectIdSupplier  the supplier of object identifiers, not null
   * @param changeManager  the change manager, not null
   */
  public InMemoryPortfolioMaster(final Supplier<ObjectId> objectIdSupplier, final ChangeManager changeManager) {
    super(objectIdSupplier, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void updateCaches(ObjectIdentifiable replacedObject, PortfolioDocument updatedDocument) {
    PortfolioDocument replacedPortfolio = (PortfolioDocument) replacedObject;
    if (replacedPortfolio != null) {
      Set<PortfolioDocument> docsWithName = _portfoliosByName.get(replacedPortfolio.getValue().getName());
      if (docsWithName != null) {
        docsWithName.remove(replacedPortfolio);
      }
    }
    if (updatedDocument != null) {
      Set<PortfolioDocument> docsWithName = _portfoliosByName.get(updatedDocument.getValue().getName());
      if (docsWithName == null) {
        docsWithName = new HashSet<PortfolioDocument>();
        _portfoliosByName.put(updatedDocument.getValue().getName(), docsWithName);
      }
      docsWithName.add(updatedDocument);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  protected void validateDocument(PortfolioDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioDocument get(UniqueId uniqueId) {
    return get(uniqueId, VersionCorrection.LATEST);
  }

  @Override
  public PortfolioDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final PortfolioDocument document = _store.get(objectId.getObjectId());
    if (document == null) {
      throw new DataNotFoundException("Portfolio not found: " + objectId);
    }
    return clonePortfolioDocument(document);
  }
  
  private PortfolioDocument clonePortfolioDocument(PortfolioDocument document) {
    if (isCloneResults()) {
      PortfolioDocument clone = JodaBeanUtils.clone(document);
      ManageablePortfolio portfolioClone = JodaBeanUtils.clone(document.getPortfolio());
      portfolioClone.setRootNode(clonePortfolioNode(portfolioClone.getRootNode()));
      clone.setPortfolio(portfolioClone);
      return clone;
    } else {
      return document;
    }
  }
  
  private ManageablePortfolioNode clonePortfolioNode(ManageablePortfolioNode node) {
    if (isCloneResults()) {
      ManageablePortfolioNode clone = JodaBeanUtils.clone(node);
      List<ManageablePortfolioNode> childNodes = new ArrayList<ManageablePortfolioNode>(node.getChildNodes().size());
      for (ManageablePortfolioNode child : node.getChildNodes()) {
        childNodes.add(clonePortfolioNode(child));
      }
      clone.setChildNodes(childNodes);
      return clone;
    } else {
      return node;
    }
  }

  @Override
  public PortfolioDocument add(PortfolioDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    
    final ObjectId objectId = _objectIdSupplier.get();
    final UniqueId uniqueId = objectId.atVersion("");
    final Instant now = Instant.now();
    
    final PortfolioDocument clonedDoc = clonePortfolioDocument(document);
    setDocumentId(document, clonedDoc, uniqueId);
    setVersionTimes(document, clonedDoc, now, null, now, null);
    _store.put(objectId, clonedDoc);
    storeNodes(clonedDoc.getPortfolio().getRootNode(), document.getPortfolio().getRootNode(), uniqueId, null);
    _changeManager.entityChanged(ChangeType.ADDED, objectId, document.getVersionFromInstant(), document.getVersionToInstant(), now);
    updateCaches(null, clonedDoc);
    return document;
  }
  
  private void setDocumentId(final PortfolioDocument document, final PortfolioDocument clonedDoc, final UniqueId uniqueId) {
    document.getPortfolio().setUniqueId(uniqueId);
    clonedDoc.getPortfolio().setUniqueId(uniqueId);
    document.setUniqueId(uniqueId);
    clonedDoc.setUniqueId(uniqueId);
  }
  
  private void storeNodes(final ManageablePortfolioNode clonedNode, final ManageablePortfolioNode origNode, final UniqueId portfolioId, final UniqueId parentNodeId) {
    final ObjectId objectId = _objectIdSupplier.get();
    final UniqueId uniqueId = objectId.atVersion("");
    clonedNode.setUniqueId(uniqueId);
    origNode.setUniqueId(uniqueId);
    clonedNode.setParentNodeId(parentNodeId);
    origNode.setParentNodeId(parentNodeId);
    clonedNode.setPortfolioId(portfolioId);
    origNode.setPortfolioId(portfolioId);
    _storeNodes.put(objectId, clonedNode);
    for (int i = 0; i < clonedNode.getChildNodes().size(); i++) {
      storeNodes(clonedNode.getChildNodes().get(i), origNode.getChildNodes().get(i), portfolioId, uniqueId);
    }
  }
  
  private void setVersionTimes(PortfolioDocument document, final PortfolioDocument clonedDoc, 
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

  @Override
  public PortfolioDocument update(PortfolioDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    
    final UniqueId uniqueId = document.getUniqueId();
    final Instant now = Instant.now();
    final PortfolioDocument storedDocument = _store.get(uniqueId.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Portfolio not found: " + uniqueId);
    }
    
    final PortfolioDocument clonedDoc = clonePortfolioDocument(document);
    removeNodes(storedDocument.getPortfolio().getRootNode());
    
    setVersionTimes(document, clonedDoc, now, null, now, null);
    
    if (_store.replace(uniqueId.getObjectId(), storedDocument, clonedDoc) == false) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    storeNodes(clonedDoc.getPortfolio().getRootNode(), document.getPortfolio().getRootNode(), uniqueId, null);
    _changeManager.entityChanged(ChangeType.CHANGED, document.getObjectId(), document.getVersionFromInstant(), document.getVersionToInstant(), now);
    updateCaches(storedDocument, clonedDoc);
    return document;
  }

  private void removeNodes(ManageablePortfolioNode node) {
    if (_storeNodes.remove(node.getUniqueId().getObjectId()) == null) {
      throw new DataNotFoundException("Node not found: " + node.getUniqueId());
    }
    for (ManageablePortfolioNode childNode : node.getChildNodes()) {
      removeNodes(childNode);
    }
  }
  
  @Override
  public void remove(ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");
    PortfolioDocument storedDocument = _store.remove(objectIdentifiable.getObjectId());
    if (storedDocument == null) {
      throw new DataNotFoundException("Portfolio not found " + objectIdentifiable);
    }
    removeNodes(storedDocument.getPortfolio().getRootNode());
    _changeManager.entityChanged(ChangeType.REMOVED, objectIdentifiable.getObjectId(), null, null, Instant.now());
    updateCaches(storedDocument, null);
  }

  @Override
  public PortfolioDocument correct(PortfolioDocument document) {
    return update(document);
  }

  @Override
  public PortfolioSearchResult search(PortfolioSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    Collection<PortfolioDocument> docsToCheck = null;
    
    if ((request.getName() != null) && !RegexUtils.containsWildcard(request.getName())) {
      docsToCheck = _portfoliosByName.get(request.getName());
    } else {
      docsToCheck = _store.values();
    }
    if (docsToCheck == null) {
      docsToCheck = Collections.emptySet();
    }
    
    final List<PortfolioDocument> list = new ArrayList<PortfolioDocument>();
    for (PortfolioDocument doc : docsToCheck) {
      if (request.matches(doc)) {
        PortfolioDocument docToAdd = isCloneResults() ? clonePortfolioDocument(doc) : doc;
        list.add(docToAdd);
      }
    }
    final PortfolioSearchResult result = new PortfolioSearchResult();
    result.setPaging(Paging.of(request.getPagingRequest(), list));
    result.getDocuments().addAll(request.getPagingRequest().select(list));
    return result;
  }

  @Override
  public PortfolioHistoryResult history(PortfolioHistoryRequest request) {
    throw new UnsupportedOperationException("History request not supported by " + getClass().getSimpleName());
  }

  @Override
  public ManageablePortfolioNode getNode(UniqueId nodeId) {
    ArgumentChecker.notNull(nodeId, "nodeId");
    ManageablePortfolioNode node = _storeNodes.get(nodeId.getObjectId());
    if (node == null) {
      throw new DataNotFoundException("Node not found: " + nodeId);
    }
    if (isCloneResults()) {
      return clonePortfolioNode(node);
    } else {
      return node;
    }
  }
}
