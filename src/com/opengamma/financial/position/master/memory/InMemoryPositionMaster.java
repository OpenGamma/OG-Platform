/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master.memory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.time.Instant;

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
import com.opengamma.util.ArgumentChecker;
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
  private final Map<UniqueIdentifier, PortfolioTreeDocument> _trees = new HashMap<UniqueIdentifier, PortfolioTreeDocument>();
  /**
   * The nodes.
   */
  private final Map<UniqueIdentifier, PortfolioNode> _nodes = new HashMap<UniqueIdentifier, PortfolioNode>();
  /**
   * A cache of positions by identifier.
   */
  private final Map<UniqueIdentifier, PositionDocument> _positions = new HashMap<UniqueIdentifier, PositionDocument>();
  /**
   * The nodes.
   */
  private final Map<UniqueIdentifier, UniqueIdentifier> _portfolioByNode = new HashMap<UniqueIdentifier, UniqueIdentifier>();
  /**
   * The lock for synchronization.
   */
  private final Object _lock = new Object();
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
    ArgumentChecker.notNull(uidSupplier, "uidSupplier");
    _uidSupplier = uidSupplier;
  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioTreeSearchResult searchPortfolioTrees(final PortfolioTreeSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    synchronized (_lock) {
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
  }

  @Override
  public PortfolioTreeDocument getPortfolioTree(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    
    synchronized (_lock) {
      return _trees.get(uid);
    }
  }

  @Override
  public PortfolioTreeDocument addPortfolioTree(final PortfolioTreeDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    
    synchronized (_lock) {
      // set uid
      final Portfolio portfolio = document.getPortfolio();
      final UniqueIdentifier portfolioUid = _uidSupplier.get();
      UniqueIdentifiables.setInto(portfolio, portfolioUid);
      addNode(portfolio.getRootNode(), portfolioUid);
      // build document
      final PortfolioTreeDocument newDoc = createPortfolioTreeDocument(portfolio, portfolioUid);
      _trees.put(portfolioUid, newDoc);  // unique identifier should be unique
      return newDoc;
    }
  }

  /**
   * Adds a node and its children.
   * @param node  the node, not null
   * @param portfolioUid  the portfolio uid, not null
   */
  protected void addNode(final PortfolioNode node, final UniqueIdentifier portfolioUid) {
    final UniqueIdentifier nodeUid = _uidSupplier.get();
    
    synchronized (_lock) {
      UniqueIdentifiables.setInto(node, nodeUid);
      _nodes.put(nodeUid, node);
      _portfolioByNode.put(nodeUid, portfolioUid);
      for (PortfolioNode child : node.getChildNodes()) {
        addNode(child, portfolioUid);
      }
    }
  }

  @Override
  public PortfolioTreeDocument updatePortfolioTree(final PortfolioTreeDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    ArgumentChecker.notNull(document.getPortfolioId(), "document.portfolioId");
    
    synchronized (_lock) {
      // get old
      final UniqueIdentifier portfolioId = document.getPortfolioId();
      final PortfolioTreeDocument oldDoc = _trees.get(portfolioId);
      if (oldDoc == null) {
        throw new DataNotFoundException("Portfolio not found: " + portfolioId);
      }
      // update document
      final PortfolioTreeDocument newDoc = createPortfolioTreeDocument(document.getPortfolio(), portfolioId);
      _trees.put(portfolioId, newDoc);
      return document;
    }
  }

  @Override
  public void removePortfolioTree(final UniqueIdentifier portfolioUid) {
    ArgumentChecker.notNull(portfolioUid, "portfolioUid");
    
    synchronized (_lock) {
      // remove portfolio
      PortfolioTreeDocument doc = _trees.remove(portfolioUid);
      if (doc == null) {
        throw new DataNotFoundException("Portfolio not found: " + portfolioUid);
      }
      // remove associated positions
      for (Iterator<PositionDocument> it = _positions.values().iterator(); it.hasNext(); ) {
        PositionDocument positionDoc = it.next();
        if (portfolioUid.equals(positionDoc.getPortfolioId())) {
          it.remove();
        }
      }
    }
  }

  @Override
  public PortfolioTreeSearchHistoricResult searchPortfolioTreeHistoric(final PortfolioTreeSearchHistoricRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPortfolioId(), "request.portfolioId");
    
    synchronized (_lock) {
      final PortfolioTreeSearchHistoricResult result = new PortfolioTreeSearchHistoricResult();
      final PortfolioTreeDocument doc = getPortfolioTree(request.getPortfolioId());
      if (doc != null) {
        result.getDocuments().add(doc);
      }
      result.setPaging(Paging.of(result.getDocuments()));
      return result;
    }
  }

  @Override
  public PortfolioTreeDocument correctPortfolioTree(final PortfolioTreeDocument document) {
    return updatePortfolioTree(document);
  }

  /**
   * Creates the tree document.
   * @param portfolio  the position, not null
   * @param uid  the position uid, not null
   * @return the position document, not null
   */
  protected PortfolioTreeDocument createPortfolioTreeDocument(final Portfolio portfolio, final UniqueIdentifier uid) {
    final Instant now = Instant.nowSystemClock();
    final PortfolioTreeDocument doc = new PortfolioTreeDocument();
    doc.setPortfolio(portfolio);
    doc.setPortfolioId(uid);
    doc.setVersionFromInstant(now);
    doc.setCorrectionFromInstant(now);
    return doc;
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionSearchResult searchPositions(final PositionSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    synchronized (_lock) {
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
  }

  @Override
  public PositionDocument getPosition(final UniqueIdentifier positionUid) {
    ArgumentChecker.notNull(positionUid, "positionUid");
    synchronized (_lock) {
      PositionDocument doc = _positions.get(positionUid);
      if (doc == null) {
        throw new DataNotFoundException("Position not found: " + positionUid);
      }
      PositionDocument result = new PositionDocument();
      result.setPosition(doc.getPosition());
      result.setPositionId(doc.getPositionId());
      result.setPortfolioId(doc.getPositionId());
      result.setParentNodeId(doc.getPositionId());
      result.setVersionFromInstant(doc.getVersionFromInstant());
      result.setVersionToInstant(doc.getVersionToInstant());
      result.setCorrectionFromInstant(doc.getCorrectionFromInstant());
      result.setCorrectionToInstant(doc.getCorrectionToInstant());
      return result;
    }
  }

  @Override
  public PositionDocument addPosition(final PositionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPosition(), "document.position");
    ArgumentChecker.notNull(document.getParentNodeId(), "document.parentNodeId");
    
    synchronized (_lock) {
      // find parent
      final PortfolioNode parentNode = _nodes.get(document.getParentNodeId());
      if (parentNode == null) {
        throw new IllegalArgumentException("Parent node not found: " + document.getParentNodeId());
      }
      // set uid
      final Position position = document.getPosition();
      final UniqueIdentifier positionUid = _uidSupplier.get();
      UniqueIdentifiables.setInto(position, positionUid);
      // add to parent
      parentNode.getPositions().add(position);
      // build document
      final PositionDocument newDoc = createPositionDocument(position, positionUid, document.getParentNodeId());
      _positions.put(positionUid, newDoc);  // unique identifier should be unique
      return newDoc;
    }
  }

  @Override
  public PositionDocument updatePosition(final PositionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getPosition(), "document.position");
    ArgumentChecker.notNull(document.getPositionId(), "document.positionId");
    
    synchronized (_lock) {
      // get old
      final UniqueIdentifier positionUid = document.getPositionId();
      final PositionDocument oldDoc = _positions.get(positionUid);
      if (oldDoc == null) {
        throw new DataNotFoundException("Position not found: " + positionUid);
      }
      // update document
      final PositionDocument newDoc = createPositionDocument(document.getPosition(), positionUid, oldDoc.getParentNodeId());
      _positions.put(positionUid, newDoc);
      return document;
    }
  }

  @Override
  public void removePosition(final UniqueIdentifier positionUid) {
    ArgumentChecker.notNull(positionUid, "positionUid");
    
    synchronized (_lock) {
      // get old
      final PositionDocument oldDoc = _positions.get(positionUid);
      if (oldDoc == null) {
        throw new DataNotFoundException("Position not found: " + positionUid);
      }
      // remove position
      _positions.remove(positionUid);
      // update portfolio
      _nodes.get(oldDoc.getParentNodeId()).getPositions().remove(oldDoc.getPosition());  // remove by id?
    }
  }

  @Override
  public PositionSearchHistoricResult searchPositionHistoric(final PositionSearchHistoricRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPositionId(), "request.positionId");
    
    synchronized (_lock) {
      final PositionSearchHistoricResult result = new PositionSearchHistoricResult();
      final PositionDocument doc = getPosition(request.getPositionId());
      if (doc != null) {
        result.getDocuments().add(doc);
      }
      result.setPaging(Paging.of(result.getDocuments()));
      return result;
    }
  }

  @Override
  public PositionDocument correctPosition(final PositionDocument document) {
    return updatePosition(document);
  }

  /**
   * Creates the position document.
   * @param position  the position, not null
   * @param uid  the position uid, not null
   * @param parentNodeUid  the parent uid, not null
   * @return the position document, not null
   */
  protected PositionDocument createPositionDocument(final Position position, final UniqueIdentifier uid, final UniqueIdentifier parentNodeUid) {
    final Instant now = Instant.nowSystemClock();
    final PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    doc.setPositionId(uid);
    doc.setParentNodeId(parentNodeUid);
    doc.setPortfolioId(_portfolioByNode.get(parentNodeUid));
    doc.setVersionFromInstant(now);
    doc.setCorrectionFromInstant(now);
    return doc;
  }

  //-------------------------------------------------------------------------
  @Override
  public Portfolio getFullPortfolio(final FullPortfolioGetRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPortfolioId(), "request.portfolioId");
    
    synchronized (_lock) {
      final PortfolioTreeDocument doc = _trees.get(request.getPortfolioId());
      if (doc == null) {
        return null;
      }
      return doc.getPortfolio();  // positions stored in tree
    }
  }

  @Override
  public PortfolioNode getFullPortfolioNode(final FullPortfolioNodeGetRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPortfolioNodeId(), "request.portfolioNodeId");
    
    synchronized (_lock) {
      return _nodes.get(request.getPortfolioNodeId());
    }
  }

  @Override
  public Position getFullPosition(final FullPositionGetRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPositionId(), "request.positionId");
    
    synchronized (_lock) {
      final PositionDocument doc = _positions.get(request.getPositionId());
      if (doc == null) {
        return null;
      }
      return doc.getPosition();
    }
  }

}
