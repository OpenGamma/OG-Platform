/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master.mongo;

import javax.time.Instant;
import javax.time.TimeSource;

import org.fudgemsg.FudgeContext;
import org.joda.beans.Property;
import org.joda.beans.mongo.BeanMongoDBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.ObjectId;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.position.Portfolio;
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
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.MongoDBConnectionSettings;
import com.opengamma.util.db.Paging;

/**
 * A position master using MongoDB for storage.
 */
public class MongoPositionMaster implements PositionMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(MongoPositionMaster.class);

//  /** Indexes . */
//  private static final String[] INDICES = {
//    PortfolioTreeDocument.meta().portfolioId().name(),
//  };

  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;
  /**
   * The MongoDB host.
   */
  private final String _mongoHost;
  /**
   * The MongoDB port.
   */
  private final int _mongoPort;
  /**
   * The MongoDB instance.
   */
  private final Mongo _mongo;
  /**
   * The MongoDB database trees collection.
   */
  private final DBCollection _treeCollection;
  /**
   * The MongoDB database positions collection.
   */
  private final DBCollection _positionCollection;

//  private final boolean _updateLastReadTime;
  /**
   * The time-source to use.
   */
  private final TimeSource _timeSource = TimeSource.system();

  /**
   * Creates a position master.
   * @param mongoSettings  the MongoDB settings, not null
   * @param fudgeContext  the Fudge context, not null
   */
  public MongoPositionMaster(final MongoDBConnectionSettings mongoSettings, final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(mongoSettings, "MongoDB settings");
    ArgumentChecker.notNull(fudgeContext, "FudgeContext");
    
    _fudgeContext = fudgeContext;
    
    _mongoHost = mongoSettings.getHost();
    _mongoPort = mongoSettings.getPort();
    mongoSettings.setCollectionName("");
    s_logger.info("connecting to {}", mongoSettings);
    try {
      _mongo = new Mongo(_mongoHost, _mongoPort);
      DB db = _mongo.getDB(mongoSettings.getDatabase());
      _treeCollection = db.getCollection("portfolioTrees");
      _positionCollection = db.getCollection("positions");
    } catch (Exception ex) {
      throw new OpenGammaRuntimeException("Unable to connect to MongoDB at " + mongoSettings, ex);
    }
    
//    ensureIndices();
//    _updateLastReadTime = updateLastRead;
//    String status = _updateLastReadTime ? "updateLastReadTime" : "readWithoutUpdate";
//    s_logger.info("creating MongoDBConfigurationRepo for {}", status);
  }

//  /**
//   * 
//   */
//  private void ensureIndices() {
//    //create necessary indices
//    DBCollection dbCollection = _mongoDB.getCollection(_collectionName);
//    for (String field : INDICES) {
//      s_logger.info("creating index for {} {}:{}", new Object[] {field, getMongoDB().getName(), getCollectionName()});
//      //create ascending and descending index
//      dbCollection.ensureIndex(new BasicDBObject(field, 1), "ix_" + getCollectionName() + "_" + field + "_asc");
//      dbCollection.ensureIndex(new BasicDBObject(field, -1), "ix_" + getCollectionName() + "_" + field + "_desc");
//    }
//  }

//  /**
//   * @return the fudgeContext
//   */
//  public FudgeContext getFudgeContext() {
//    return _fudgeContext;
//  }
//
//  /**
//   * @return the mongoHost
//   */
//  public String getMongoHost() {
//    return _mongoHost;
//  }
//
//  /**
//   * @return the mongoPort
//   */
//  public int getMongoPort() {
//    return _mongoPort;
//  }
//
//  /**
//   * @return the mongo
//   */
//  public Mongo getMongo() {
//    return _mongo;
//  }
//
//  /**
//   * @return the mongoDB
//   */
//  public DB getMongoDB() {
//    return _mongoDB;
//  }
//
//  /**
//   * @return the collectionName
//   */
//  public String getCollectionName() {
//    return _collectionName;
//  }
//
//  /**
//   * Gets the time-source that determines the current time.
//   * @return the time-source, not null
//   */
//  public TimeSource getTimeSource() {
//    return _timeSource;
//  }
//
//  /**
//   * @param timeSource the timeSource to set
//   */
//  public void setTimeSource(TimeSource timeSource) {
//    Validate.notNull(timeSource, "TimeSource must not be null");
//    _timeSource = timeSource;
//  }

  //-------------------------------------------------------------------------
  @Override
  public PortfolioTreeSearchResult searchPortfolioTrees(final PortfolioTreeSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPagingRequest(), "request.pagingRequest");
    
    final DBObject query = new BasicDBObject();
    final DBObject sortOrder = new BasicDBObject();
    query.put(PortfolioTreeDocument.meta().validFromInstant().name(), -1);
    query.put(PortfolioTreeDocument.meta().lastModifiedInstant().name(), -1);
    
    s_logger.debug("search tree: query = {}, sort = {}", query, sortOrder);
    final int count = _treeCollection.find(query).count();
    final DBCursor cursor = _treeCollection.find(query).sort(sortOrder)
      .skip(request.getPagingRequest().getFirstItemIndex())
      .limit(request.getPagingRequest().getPagingSize());
    final PortfolioTreeSearchResult result = new PortfolioTreeSearchResult();
    for (DBObject dbo : cursor) {
      result.getDocuments().add(convertToPortfolioTreeDocument(dbo));
    }
    result.setPaging(new Paging(request.getPagingRequest(), count));
    return result;
  }

  @Override
  public PortfolioTreeDocument getPortfolioTree(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    
    final DBObject query = new BasicDBObject();
    query.put(PortfolioTreeDocument.meta().portfolioId().name(), uid);
    final DBObject sortOrder = new BasicDBObject();
    query.put(PortfolioTreeDocument.meta().validFromInstant().name(), -1);
    query.put(PortfolioTreeDocument.meta().lastModifiedInstant().name(), -1);
    
    s_logger.debug("get tree: query = {}, sort = {}", query, sortOrder);
    final DBCursor cursor = _treeCollection.find(query).sort(sortOrder).limit(1);
    if (cursor.hasNext() == false) {
      throw new DataNotFoundException("Portfolio not found: " + uid);
    }
    return convertToPortfolioTreeDocument(cursor.next());
  }

  @Override
  public PortfolioTreeDocument addPortfolioTree(final PortfolioTreeDocument document) {
    ArgumentChecker.notNull(document, "document");
//    ArgumentChecker.notNull(document.getPortfolio(), "document.portfolio");
    
    final String objectId = ObjectId.get().toString();
    final Instant now = Instant.now(_timeSource);
    document.setValidFromInstant(now);
    document.setLastModifiedInstant(now);
    document.setPortfolioId(UniqueIdentifier.of("MongoPositionMaster", objectId, now.toString()));
    BasicDBObject dbo = new BasicDBObject(new BeanMongoDBObject(document).toMap());
    dbo.append("_id", objectId);
//    dbo.append("_name", document.getPortfolio().getName());
    s_logger.debug("inserting new doc {}", dbo);
    _treeCollection.insert(dbo);
    DBObject lastErr = _treeCollection.getDB().getLastError();
    if (lastErr.get("err") != null) {
      throw new OpenGammaRuntimeException("Error: " + lastErr.toString());
    }
    return document;
  }

  @Override
  public PortfolioTreeDocument updatePortfolioTree(final PortfolioTreeDocument document) {
    return null;
  }

  @Override
  public void removePortfolioTree(final UniqueIdentifier uid) {
  }

  @Override
  public PortfolioTreeSearchHistoricResult searchPortfolioTreeHistoric(final PortfolioTreeSearchHistoricRequest request) {
    return null;
  }

  @Override
  public PortfolioTreeDocument correctPortfolioTree(final PortfolioTreeDocument document) {
    return null;
  }

  /**
   * Converts a Mongo DB object to a {@code PortfolioTreeDocument}.
   * @param mongoDoc  the Mongo version of the document, not null
   * @return the document, not null
   */
  private PortfolioTreeDocument convertToPortfolioTreeDocument(final DBObject mongoDoc) {
    PortfolioTreeDocument doc = new PortfolioTreeDocument();
    for (Property<Object> prop : doc.propertyMap().values()) {
      prop.set(mongoDoc.get(prop.name()));
    }
    return doc;
  }

  //-------------------------------------------------------------------------
  @Override
  public PositionSearchResult searchPositions(PositionSearchRequest request) {
    return null;
  }

  @Override
  public PositionDocument getPosition(UniqueIdentifier uid) {
    return null;
  }

  @Override
  public PositionDocument addPosition(PositionDocument document) {
    return null;
  }

  @Override
  public PositionDocument updatePosition(PositionDocument document) {
    return null;
  }

  @Override
  public void removePosition(UniqueIdentifier uid) {
  }

  @Override
  public PositionSearchHistoricResult searchPositionHistoric(PositionSearchHistoricRequest request) {
    return null;
  }

  @Override
  public PositionDocument correctPosition(PositionDocument document) {
    return null;
  }

  //-------------------------------------------------------------------------
  @Override
  public Portfolio getFullPortfolio(FullPortfolioGetRequest request) {
    return null;
  }

  @Override
  public PortfolioNode getFullPortfolioNode(FullPortfolioNodeGetRequest request) {
    return null;
  }

  @Override
  public Position getFullPosition(FullPositionGetRequest request) {
    return null;
  }

}
