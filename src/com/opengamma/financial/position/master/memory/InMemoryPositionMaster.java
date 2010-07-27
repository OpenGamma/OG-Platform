/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master.memory;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.time.Instant;

import org.apache.commons.lang.Validate;
import org.joda.beans.MetaProperty;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Collections2;
import com.opengamma.DataNotFoundException;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.financial.position.master.FullPortfolioGetRequest;
import com.opengamma.financial.position.master.FullPortfolioNodeGetRequest;
import com.opengamma.financial.position.master.FullPositionGetRequest;
import com.opengamma.financial.position.master.PortfolioTreeDocument;
import com.opengamma.financial.position.master.PortfolioTreeSearchHistoricRequest;
import com.opengamma.financial.position.master.PortfolioTreeSearchHistoricResult;
import com.opengamma.financial.position.master.PortfolioTreeSearchRequest;
import com.opengamma.financial.position.master.PortfolioTreeSearchResult;
import com.opengamma.financial.position.master.PositionDocument;
import com.opengamma.financial.position.master.PositionMaster;
import com.opengamma.financial.position.master.PositionSearchHistoricRequest;
import com.opengamma.financial.position.master.PositionSearchHistoricResult;
import com.opengamma.financial.position.master.PositionSearchRequest;
import com.opengamma.financial.position.master.PositionSearchResult;
import com.opengamma.id.UniqueIdentifiables;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierSupplier;
import com.opengamma.util.db.Paging;

/**
 * A simple, in-memory implementation of {@code PositionMaster}.
 * <p>
 * This security master does not support versioning of portfolios or positions.
 */
public class InMemoryPositionMaster implements PositionMaster {
  // TODO: This is not hardened for production, as the data in the master can
  // be altered from outside as it is the same object

  /**
   * The default scheme used for each {@link UniqueIdentifier}.
   */
  public static final String DEFAULT_UID_SCHEME = "Memory";

  /**
   * The portfolios.
   */
  private final ConcurrentMap<UniqueIdentifier, PortfolioTreeDocument> _trees = new ConcurrentHashMap<UniqueIdentifier, PortfolioTreeDocument>();
  /**
   * A cache of positions by identifier.
   */
  private final ConcurrentMap<UniqueIdentifier, PositionDocument> _positions = new ConcurrentHashMap<UniqueIdentifier, PositionDocument>();
  /**
   * The supplied of identifiers.
   */
  private final Supplier<UniqueIdentifier> _uidSupplier;

  /**
   * Creates an empty security master using the default scheme for any {@link UniqueIdentifier}s created.
   */
  public InMemoryPositionMaster() {
    this(new UniqueIdentifierSupplier(DEFAULT_UID_SCHEME));
  }

  /**
   * Creates an instance specifying the supplier of unique identifiers.
   * 
   * @param uidSupplier  the supplier of unique identifiers, not null
   */
  public InMemoryPositionMaster(final Supplier<UniqueIdentifier> uidSupplier) {
    Validate.notNull(uidSupplier, "uidSupplier");
    _uidSupplier = uidSupplier;
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioTreeSearchResult searchPortfolioTrees(final PortfolioTreeSearchRequest request) {
    Validate.notNull(request, "request");
    final PortfolioTreeSearchResult result = new PortfolioTreeSearchResult();
    Collection<PortfolioTreeDocument> docs = _trees.values();
    if (request.getName() != null) {
      docs = Collections2.filter(docs, new Predicate<PortfolioTreeDocument>() {
        @Override
        public boolean apply(final PortfolioTreeDocument doc) {
          return request.getName().equals(doc.getPortfolio().getName());
        }
      });
    }
    if (request.getDepth() == 0) {
      Collections2.transform(docs, new Function<PortfolioTreeDocument, PortfolioTreeDocument>() {
        @Override
        public PortfolioTreeDocument apply(final PortfolioTreeDocument doc) {
          PortfolioTreeDocument trimmedDoc = new PortfolioTreeDocument();
          for (MetaProperty<Object> mp : doc.metaBean().metaPropertyIterable()) {
            mp.set(trimmedDoc, mp.get(doc));
          }
          trimmedDoc.setPortfolio(new PortfolioImpl(doc.getPortfolio().getUniqueIdentifier(), doc.getPortfolio().getName()));
          return trimmedDoc;
        }
      });
    }
    result.getDocuments().addAll(docs);
    result.setPaging(Paging.of(docs));
    return result;
  }

  @Override
  public PortfolioTreeDocument getPortfolioTree(final UniqueIdentifier uid) {
    Validate.notNull(uid, "uid");
    return _trees.get(uid);
  }

  @Override
  public PortfolioTreeDocument addPortfolioTree(final PortfolioTreeDocument document) {
    Validate.notNull(document, "document");
    Validate.notNull(document.getPortfolio(), "document.portfolio");
    
    final Portfolio portfolio = document.getPortfolio();
    final UniqueIdentifier uid = _uidSupplier.get();
    final Instant now = Instant.nowSystemClock();
    UniqueIdentifiables.setInto(portfolio, uid);
    final PortfolioTreeDocument doc = new PortfolioTreeDocument(portfolio);
    doc.setPortfolioId(uid);
    doc.setValidFromInstant(now);
    doc.setLastModifiedInstant(now);
    buildPositionCounts(doc, portfolio.getRootNode());
    _trees.put(uid, doc);  // unique identifier should be unique
    return doc;
  }

  private static void buildPositionCounts(final PortfolioTreeDocument doc, final PortfolioNode node) {
    doc.getPositionCounts().put(node.getUniqueIdentifier(), node.getPositions().size());
    for (PortfolioNode child : node.getChildNodes()) {
      buildPositionCounts(doc, child);
    }
  }

  @Override
  public PortfolioTreeDocument updatePortfolioTree(final PortfolioTreeDocument document) {
    Validate.notNull(document, "document");
    Validate.notNull(document.getPortfolio(), "document.portfolio");
    Validate.notNull(document.getPortfolioId(), "document.portfolioId");
    
    final UniqueIdentifier uid = document.getPortfolioId();
    final Instant now = Instant.nowSystemClock();
    final PortfolioTreeDocument storedDocument = _trees.get(uid);
    if (storedDocument == null) {
      throw new DataNotFoundException("Portfolio not found: " + uid);
    }
    document.setValidFromInstant(storedDocument.getValidFromInstant());
    document.setValidToInstant(storedDocument.getValidToInstant());
    document.setLastModifiedInstant(now);
    if (_trees.replace(uid, storedDocument, document) == false) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    return document;
  }

  @Override
  public void removePortfolioTree(final UniqueIdentifier uid) {
    Validate.notNull(uid, "uid");
    
    PortfolioTreeDocument doc = _trees.remove(uid);
    if (doc == null) {
      throw new DataNotFoundException("Portfolio not found: " + uid);
    }
    for (Iterator<PositionDocument> it = _positions.values().iterator(); it.hasNext(); ) {
      if (uid.equals(it.next().getPortfolioId())) {
        it.remove();
      }
    }
  }

  @Override
  public PortfolioTreeSearchHistoricResult searchPortfolioTreeHistoric(final PortfolioTreeSearchHistoricRequest request) {
    Validate.notNull(request, "request");
    Validate.notNull(request.getPortfolioId(), "request.portfolioId");
    
    final PortfolioTreeSearchHistoricResult result = new PortfolioTreeSearchHistoricResult();
    final PortfolioTreeDocument doc = getPortfolioTree(request.getPortfolioId());
    if (doc != null) {
      result.getDocuments().add(doc);
    }
    result.setPaging(Paging.of(result.getDocuments()));
    return result;
  }

  @Override
  public PortfolioTreeDocument correctPortfolioTree(final PortfolioTreeDocument document) {
    return updatePortfolioTree(document);
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionSearchResult searchPositions(final PositionSearchRequest request) {
    Validate.notNull(request, "request");
    final PositionSearchResult result = new PositionSearchResult();
    Collection<PositionDocument> docs = _positions.values();
    if (request.getMinQuantity() != null) {
      docs = Collections2.filter(docs, new Predicate<PositionDocument>() {
        @Override
        public boolean apply(final PositionDocument doc) {
          if (doc.getPosition().getQuantity() == null) {
            return false;
          }
          return doc.getPosition().getQuantity().compareTo(request.getMinQuantity()) >= 0;
        }
      });
    }
    if (request.getMaxQuantity() != null) {
      docs = Collections2.filter(docs, new Predicate<PositionDocument>() {
        @Override
        public boolean apply(final PositionDocument doc) {
          if (doc.getPosition().getQuantity() == null) {
            return false;
          }
          return doc.getPosition().getQuantity().compareTo(request.getMaxQuantity()) < 0;
        }
      });
    }
    if (request.getSecurityKey() != null) {
      docs = Collections2.filter(docs, new Predicate<PositionDocument>() {
        @Override
        public boolean apply(final PositionDocument doc) {
          if (doc.getPosition().getSecurityKey() == null) {
            return false;
          }
          return doc.getPosition().getSecurityKey().containsAny(request.getSecurityKey());
        }
      });
    }
    result.getDocuments().addAll(docs);
    result.setPaging(Paging.of(docs));
    return result;
  }

  @Override
  public PositionDocument getPosition(final UniqueIdentifier uid) {
    Validate.notNull(uid, "uid");
    return _positions.get(uid);
  }

  @Override
  public PositionDocument addPosition(final PositionDocument document) {
    Validate.notNull(document, "document");
    Validate.notNull(document.getPosition(), "document.position");
    
    final Position positoin = document.getPosition();
    final UniqueIdentifier uid = _uidSupplier.get();
    final Instant now = Instant.nowSystemClock();
    UniqueIdentifiables.setInto(positoin, uid);
    final PositionDocument doc = new PositionDocument(positoin);
    doc.setPortfolioId(uid);
    doc.setValidFromInstant(now);
    doc.setLastModifiedInstant(now);
    _positions.put(uid, doc);  // unique identifier should be unique
    return doc;
  }

  @Override
  public PositionDocument updatePosition(final PositionDocument document) {
    Validate.notNull(document, "document");
    Validate.notNull(document.getPosition(), "document.position");
    Validate.notNull(document.getPositionId(), "document.positionId");
    
    final UniqueIdentifier uid = document.getPortfolioId();
    final Instant now = Instant.nowSystemClock();
    final PositionDocument storedDocument = _positions.get(uid);
    if (storedDocument == null) {
      throw new DataNotFoundException("Portfolio not found: " + uid);
    }
    document.setValidFromInstant(storedDocument.getValidFromInstant());
    document.setValidToInstant(storedDocument.getValidToInstant());
    document.setLastModifiedInstant(now);
    if (_positions.replace(uid, storedDocument, document) == false) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    return document;
  }

  @Override
  public void removePosition(final UniqueIdentifier uid) {
    Validate.notNull(uid, "uid");
    
    if (_positions.remove(uid) == null) {
      throw new DataNotFoundException("Position not found: " + uid);
    }
  }

  @Override
  public PositionSearchHistoricResult searchPositionHistoric(final PositionSearchHistoricRequest request) {
    Validate.notNull(request, "request");
    Validate.notNull(request.getPositionId(), "request.positionId");
    
    final PositionSearchHistoricResult result = new PositionSearchHistoricResult();
    final PositionDocument doc = getPosition(request.getPositionId());
    if (doc != null) {
      result.getDocuments().add(doc);
    }
    result.setPaging(Paging.of(result.getDocuments()));
    return result;
  }

  @Override
  public PositionDocument correctPosition(final PositionDocument document) {
    return updatePosition(document);
  }

  //-------------------------------------------------------------------------
  @Override
  public Portfolio getFullPortfolio(final FullPortfolioGetRequest request) {
    Validate.notNull(request, "request");
    Validate.notNull(request.getPortfolioId(), "request.portfolioId");
    
    final PortfolioTreeDocument doc = _trees.get(request.getPortfolioId());
    if (doc == null) {
      return null;
    }
    return doc.getPortfolio();  // positions stored in tree
  }

  @Override
  public PortfolioNode getFullPortfolioNode(final FullPortfolioNodeGetRequest request) {
    Validate.notNull(request, "request");
    Validate.notNull(request.getPortfolioNodeId(), "request.portfolioNodeId");
    
    final UniqueIdentifier uid = request.getPortfolioNodeId();
    for (PortfolioTreeDocument doc : _trees.values()) {
      PortfolioNode node = findNode(doc.getPortfolio().getRootNode(), uid);
      if (node != null) {
        return node;
      }
    }
    return null;
  }

  private static PortfolioNode findNode(final PortfolioNode node, final UniqueIdentifier uid) {
    if (uid.equals(node.getUniqueIdentifier())) {
      return node;
    }
    for (PortfolioNode child : node.getChildNodes()) {
      findNode(child, uid);
    }
    return null;
  }

  @Override
  public Position getFullPosition(final FullPositionGetRequest request) {
    Validate.notNull(request, "request");
    Validate.notNull(request.getPositionId(), "request.positionId");
    
    final PositionDocument doc = _positions.get(request.getPositionId());
    if (doc == null) {
      return null;
    }
    return doc.getPosition();
  }

}
