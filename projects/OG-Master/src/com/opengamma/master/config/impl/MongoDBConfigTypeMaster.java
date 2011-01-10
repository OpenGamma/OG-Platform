/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import java.util.Collections;
import java.util.Date;

import javax.time.Instant;
import javax.time.TimeSource;
import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.ZoneOffset;

import org.bson.types.ObjectId;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.MasterChangeListener;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.config.ConfigTypeMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.MongoDBConnectionSettings;
import com.opengamma.util.RegexUtils;
import com.opengamma.util.db.Paging;
import com.opengamma.util.db.PagingRequest;
import com.opengamma.util.fudge.OpenGammaFudgeContext;

/**
 * A configuration master implemented using the Mongo database.
 * <p>
 * The {@code MongoDBConnectionSettings} specifies the connection details to use.
 * If collectionName is null, the document class name will be used.
 * 
 * @param <T>  the configuration document type
 */
public class MongoDBConfigTypeMaster<T> implements ConfigTypeMaster<T> {
  // this implementation uses multiple Mongo documents
  // without transactions, this means that it is not fully safe under load

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(MongoDBConfigTypeMaster.class);
  /**
   * The maximum instant for use in date ranges.
   */
  private static final Instant MAX_INSTANT = OffsetDateTime.of(9999, 12, 31, 23, 59, 59, ZoneOffset.UTC).toInstant();
  /**
   * Mongo key for the id.
   */
  private static final String ID_FIELD_NAME = "_id";
  /**
   * Mongo key for the oid.
   */
  private static final String OID_FIELD_NAME = "oid";
  /**
   * Mongo key for the name.
   */
  private static final String NAME_FIELD_NAME = "name";
  /**
   * Mongo key for version from.
   */
  private static final String VERSION_FROM_INSTANT_FIELD_NAME = "versionFromInstant";
  /**
   * Mongo key for version from.
   */
  private static final String VERSION_TO_INSTANT_FIELD_NAME = "versionToInstant";
  /**
   * Mongo key for the lastRead.
   */
  private static final String LAST_READ_INSTANT_FIELD_NAME = "lastReadInstant";
  /**
   * Mongo key for the value.
   */
  private static final String VALUE_FIELD_NAME = "value";
  /**
   * Mongo indices.
   */
  private static final String[] INDICES = {
    OID_FIELD_NAME, NAME_FIELD_NAME, VERSION_FROM_INSTANT_FIELD_NAME, VERSION_TO_INSTANT_FIELD_NAME};

  /**
   * The scheme used by the master by default.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "MongoConfig";

  /**
   * The configuration element type.
   */
  private final Class<T> _entityClazz;
  /**
   * The Mongo settings.
   */
  private final MongoDBConnectionSettings _mongoSettings;
  /**
   * The Mongo instance.
   */
  private final Mongo _mongo;
  /**
   * The scheme to use for unique identifiers.
   */
  private String _identifierScheme = IDENTIFIER_SCHEME_DEFAULT;
  /**
   * The time-source to use.
   */
  private TimeSource _timeSource = TimeSource.system();

  /**
   * Creates an instance.
   * @param documentClazz  the element type, not null
   * @param mongoSettings  the Mongo connection settings, not null
   */
  public MongoDBConfigTypeMaster(final Class<T> documentClazz, final MongoDBConnectionSettings mongoSettings) {
    ArgumentChecker.notNull(documentClazz, "document class");
    ArgumentChecker.notNull(mongoSettings, "MongoDB settings");
    
    _entityClazz = documentClazz;
    if (mongoSettings.getCollectionName() == null) {
      mongoSettings.setCollectionName(_entityClazz.getSimpleName());
    }
    _mongoSettings = mongoSettings;
    s_logger.info("Connecting to {}", mongoSettings);
    try {
      _mongo = new Mongo(mongoSettings.getHost(), mongoSettings.getPort());
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Unable to connect to MongoDB at " + mongoSettings, e);
    }
    
    ensureIndices();
  }

  private void ensureIndices() {
    DBCollection dbCollection = createDBCollection();
    for (String field : INDICES) {
      String collectionName = getMongoConnectionSettings().getCollectionName();
      s_logger.info("ensuring index for {} {}:{}", new Object[] {field, getMongoConnectionSettings().getDatabase(), collectionName});
      dbCollection.ensureIndex(new BasicDBObject(field, 1), "ix_" + field + "_asc");
      dbCollection.ensureIndex(new BasicDBObject(field, -1), "ix_" + field + "_desc");
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the instance of Mongo.
   * @return the Mongo instance, not null
   */
  protected Mongo getMongo() {
    return _mongo;
  }

  /**
   * Gets the connection settings.
   * @return the Mongo settings, not null
   */
  protected MongoDBConnectionSettings getMongoConnectionSettings() {
    return _mongoSettings;
  }

  /**
   * Gets the Fudge context.
   * @return the Fudge context, not null
   */
  protected FudgeContext getFudgeContext() {
    return OpenGammaFudgeContext.getInstance();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a new Mongo collection wrapping the database.
   * @return the Mongo collection, not null
   */
  protected DBCollection createDBCollection() {
    DB db = getMongo().getDB(getMongoConnectionSettings().getDatabase());
    return db.getCollection(getMongoConnectionSettings().getCollectionName());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the time-source that determines the current time.
   * @return the time-source, not null
   */
  public TimeSource getTimeSource() {
    return _timeSource;
  }

  /**
   * Sets the time-source.
   * @param timeSource  the time-source, not null
   */
  public void setTimeSource(final TimeSource timeSource) {
    ArgumentChecker.notNull(timeSource, "timeSource");
    s_logger.debug("installed TimeSource: {}", timeSource);
    _timeSource = timeSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the scheme in use for UniqueIdentifier.
   * @return the scheme, not null
   */
  public String getIdentifierScheme() {
    return _identifierScheme;
  }

  /**
   * Sets the scheme in use for UniqueIdentifier.
   * @param scheme  the scheme, not null
   */
  public void setIdentifierScheme(final String scheme) {
    ArgumentChecker.notNull(scheme, "scheme");
    s_logger.debug("installed IdentifierScheme: {}", scheme);
    _identifierScheme = scheme;
  }

  /**
   * Checks the scheme is valid.
   * @param uid  the unique identifier
   */
  protected void checkScheme(final UniqueIdentifier uid) {
    if (getIdentifierScheme().equals(uid.getScheme()) == false) {
      throw new IllegalArgumentException("UniqueIdentifier is not from this Config master: " + uid);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigSearchResult<T> search(final ConfigSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPagingRequest(), "request.pagingRequest");
    
    Instant now = Instant.now(getTimeSource());
    Instant versionAsOf = Objects.firstNonNull(request.getVersionAsOfInstant(), now);
    String name = request.getName();
    DBObject queryObj = new BasicDBObject();
    if (name != null) {
      queryObj.put(NAME_FIELD_NAME, RegexUtils.wildcardsToPattern(name));
    }
    queryObj.put(VERSION_FROM_INSTANT_FIELD_NAME, new BasicDBObject("$lte", new Date(versionAsOf.toEpochMillisLong())));
    queryObj.put(VERSION_TO_INSTANT_FIELD_NAME, new BasicDBObject("$gt", new Date(versionAsOf.toEpochMillisLong())));
    BasicDBObject sortObj = new BasicDBObject(NAME_FIELD_NAME, 1);
    return find(queryObj, sortObj, request.getPagingRequest());
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument<T> get(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    checkScheme(uid);
    
    DBObject queryObj = new BasicDBObject();
    BasicDBObject sortObj = null;
    if (uid.isVersioned()) {
      queryObj.put(ID_FIELD_NAME, extractRowId(uid));
    } else {
      queryObj.put(OID_FIELD_NAME, uid.getValue());
      queryObj.put(VERSION_TO_INSTANT_FIELD_NAME, new Date(MAX_INSTANT.toEpochMillisLong()));
      sortObj = new BasicDBObject(VERSION_FROM_INSTANT_FIELD_NAME, -1);  // gets correct one during updates
    }
    Instant now = Instant.now(getTimeSource());
    DBObject updateObj = new BasicDBObject("$set", new BasicDBObject(LAST_READ_INSTANT_FIELD_NAME, new Date(now.toEpochMillisLong())));
    DBCollection dbCollection = createDBCollection();
    DBObject matched;
    matched = dbCollection.findAndModify(queryObj, null, sortObj, false, updateObj, true, false);
    if (matched == null) {
      throw new DataNotFoundException("Configuration not found: " + uid);
    }
    return convertFromDb(matched);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument<T> add(final ConfigDocument<T> document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getName(), "document.name");
    ArgumentChecker.notNull(document.getValue(), "document.value");
    
    Instant now = Instant.now(getTimeSource());
    String id = ObjectId.get().toString();
    document.setUniqueId(UniqueIdentifier.of(getIdentifierScheme(), id, id));
    document.setVersionFromInstant(now);
    document.setVersionToInstant(null);
    DBObject insertDoc = convertToDb(document);
    s_logger.debug("Config add, insert={}", insertDoc);
    createDBCollection().insert(insertDoc).getLastError().throwOnError();
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument<T> update(final ConfigDocument<T> document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getName(), "document.name");
    ArgumentChecker.notNull(document.getValue(), "document.value");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    checkScheme(document.getUniqueId());
    
    // load old row
    ConfigDocument<T> oldDoc = get(document.getUniqueId());  // checks uid exists
    if (oldDoc.getVersionToInstant() != null) {
      throw new IllegalArgumentException("UniqueIdentifier is not latest version: " + document.getUniqueId());
    }
    // not possible to do a transactional update in Mongo as there are two documents
    // http://groups.google.com/group/mongodb-user/browse_thread/thread/51765eae8c337b8c
    // main alternative of a single document for all versions is poor in other ways
    
    // prepare to insert new row
    Instant now = Instant.now(getTimeSource());
    ObjectId id = ObjectId.get();
    document.setUniqueId(document.getUniqueId().withVersion(id.toString()));
    document.setVersionFromInstant(now);
    document.setVersionToInstant(null);
    DBObject insertDoc = convertToDb(document);
    s_logger.debug("Config update, insert={}", insertDoc);
    // prepare to update old row
    DBObject updateQueryObj = new BasicDBObject();
    updateQueryObj.put(ID_FIELD_NAME, extractRowId(oldDoc.getUniqueId()));
    updateQueryObj.put(VERSION_TO_INSTANT_FIELD_NAME, new Date(MAX_INSTANT.toEpochMillisLong()));
    BasicDBObject updateObj = new BasicDBObject("$set", new BasicDBObject(VERSION_TO_INSTANT_FIELD_NAME, new Date(now.toEpochMillisLong())));
    s_logger.debug("Config update, find={} action={}", updateQueryObj, updateObj);
    // insert then update, as close as possible in Java, but still not transactional
    DBCollection dbCollection = createDBCollection();
    dbCollection.insert(insertDoc).getLastError().throwOnError();
    CommandResult lastError = dbCollection.update(updateQueryObj, updateObj).getLastError();
    if (lastError.ok() == false) {
      // remove invalid document
      DBObject removeQueryObj = new BasicDBObject();
      removeQueryObj.put(ID_FIELD_NAME, id);
      s_logger.debug("Config update, update={} action={}", updateQueryObj, updateObj);
      createDBCollection().remove(removeQueryObj).getLastError().throwOnError();  // rollback failure leaves DB invalid
      lastError.throwOnError();  // problem during update, but rollback OK
    }
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    checkScheme(uid);
    
    // load old row
    ConfigDocument<T> oldDoc = get(uid);  // checks uid exists
    if (oldDoc.getVersionToInstant() != null) {
      throw new IllegalArgumentException("UniqueIdentifier is not latest version: " + uid);
    }
    // update latest row, even during updates
    Instant now = Instant.now(getTimeSource());
    DBObject queryObj = new BasicDBObject();
    queryObj.put(OID_FIELD_NAME, uid.getValue());
    queryObj.put(VERSION_TO_INSTANT_FIELD_NAME, new Date(MAX_INSTANT.toEpochMillisLong()));
    BasicDBObject sortObj = new BasicDBObject(VERSION_FROM_INSTANT_FIELD_NAME, -1);  // gets correct one during updates
    DBObject updateObj = new BasicDBObject("$set", new BasicDBObject(VERSION_TO_INSTANT_FIELD_NAME, new Date(now.toEpochMillisLong())));
    DBCollection dbCollection = createDBCollection();
    s_logger.debug("Config remove, find={} action={}", queryObj, updateObj);
    dbCollection.findAndModify(queryObj, sortObj, updateObj);
    dbCollection.getDB().getLastError().throwOnError();
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigHistoryResult<T> history(final ConfigHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");
    checkScheme(request.getObjectId());
    
    DBObject queryObj = new BasicDBObject();
    queryObj.put(OID_FIELD_NAME, request.getObjectId().getValue());
    if (request.getVersionsFromInstant() != null && request.getVersionsFromInstant().equals(request.getVersionsToInstant())) {
      queryObj.put(VERSION_FROM_INSTANT_FIELD_NAME, new BasicDBObject("$lte", new Date(request.getVersionsFromInstant().toEpochMillisLong())));
      queryObj.put(VERSION_TO_INSTANT_FIELD_NAME, new BasicDBObject("$gt", new Date(request.getVersionsFromInstant().toEpochMillisLong())));
    } else {
      String search = "";
      if (request.getVersionsFromInstant() != null) {
        String searchFrom = "new Date(" + request.getVersionsFromInstant().toEpochMillisLong() + ")";
        search += "((this." + VERSION_FROM_INSTANT_FIELD_NAME + " <= " + searchFrom + " && " +
                    "this." + VERSION_TO_INSTANT_FIELD_NAME + " > " + searchFrom + ") || " +
                   "this." + VERSION_FROM_INSTANT_FIELD_NAME + " >= " + searchFrom + ") ";
      }
      if (request.getVersionsToInstant() != null) {
        if (search.length() > 0) {
          search += " && ";
        }
        String searchTo = "new Date(" + request.getVersionsToInstant().toEpochMillisLong() + ")";
        search += "((this." + VERSION_FROM_INSTANT_FIELD_NAME + " <= " + searchTo + " && " +
                    "this." + VERSION_TO_INSTANT_FIELD_NAME + " > " + searchTo + ") || " +
                   "this." + VERSION_TO_INSTANT_FIELD_NAME + " < " + searchTo + ") ";
      }
      if (search.length() > 0) {
        queryObj.put("$where", search);
      }
    }
    BasicDBObject sortObj = new BasicDBObject(VERSION_FROM_INSTANT_FIELD_NAME, -1);
    
    ConfigSearchResult<T> configSearchResult = find(queryObj, sortObj, request.getPagingRequest());
    ConfigHistoryResult<T> result = new ConfigHistoryResult<T>();
    result.setPaging(configSearchResult.getPaging());
    result.setDocuments(configSearchResult.getDocuments());
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument<T> correct(final ConfigDocument<T> document) {
    return update(document);  // TODO if we use Mongo again
  }

  //-------------------------------------------------------------------------
  /**
   * Extracts the row id.
   * @param id  the identifier to extract from, not null
   * @return the extracted row id
   */
  protected ObjectId extractRowId(final UniqueIdentifier id) {
    try {
      return new ObjectId(id.getVersion());
    } catch (IllegalArgumentException ex) {
      throw new DataNotFoundException("Config not found: " + id);
    }
  }

  /**
   * Extracts the oid.
   * @param id  the identifier to extract from, not null
   * @return the extracted oid
   */
  protected String extractOid(final UniqueIdentifier id) {
    return id.getValue();
  }

  /**
   * Finds documents.
   * @param queryObj  the query, not null
   * @param sortObj  the sort, may be null
   * @param pagingRequest  the paging request, not null
   * @return the search results, not null
   */
  protected ConfigSearchResult<T> find(DBObject queryObj, DBObject sortObj, PagingRequest pagingRequest) {
    s_logger.debug("Config find, query={} sort={}", queryObj, sortObj);
    
    ConfigSearchResult<T> result = new ConfigSearchResult<T>();
    Instant now = Instant.now(getTimeSource());
    DBObject updateObj = new BasicDBObject("$set", new BasicDBObject(LAST_READ_INSTANT_FIELD_NAME, new Date(now.toEpochMillisLong())));
    
    // get
    DBCollection dbCollection = createDBCollection();
    if (pagingRequest.getPage() == 1 && pagingRequest.getPagingSize() == 1) {
      DBObject matched = dbCollection.findAndModify(queryObj, null, sortObj, false, updateObj, true, false);
      if (matched == null) {
        result.setPaging(Paging.of(Collections.emptyList(), pagingRequest));
      }
      return result;
    }
    
    // search
    int count = dbCollection.find(queryObj).count();
    DBCursor cursor = dbCollection.find(queryObj).sort(sortObj).skip(pagingRequest.getFirstItemIndex()).limit(pagingRequest.getPagingSize());
    result.setPaging(new Paging(pagingRequest, count));
    for (DBObject dbObject : cursor) {
      result.getDocuments().add(convertFromDb(dbObject));
    }
    return result;
  }

  /**
   * Converts a Mongo DB object to a document.
   * @param dbObject  the DB object, not null
   * @return the document, not null
   */
  protected ConfigDocument<T> convertFromDb(DBObject dbObject) {
    s_logger.trace("Config converting dbOject to config doc={}", dbObject);
    DBObject valueData = (DBObject) dbObject.get(VALUE_FIELD_NAME);
    FudgeSerializationContext fsc = new FudgeSerializationContext(getFudgeContext());
    MutableFudgeFieldContainer dbObjecToFudge = fsc.objectToFudgeMsg(valueData);
    FudgeDeserializationContext fdc = new FudgeDeserializationContext(getFudgeContext());
    T value = fdc.fudgeMsgToObject(_entityClazz, dbObjecToFudge);
    ConfigDocument<T> doc = new ConfigDocument<T>();
    ObjectId id = (ObjectId) dbObject.get("_id");
    String oid = (String) dbObject.get(OID_FIELD_NAME);
    doc.setUniqueId(UniqueIdentifier.of(getIdentifierScheme(), oid, id.toString()));
    doc.setName((String) dbObject.get(NAME_FIELD_NAME));
    Date versionFromTime = (Date) dbObject.get(VERSION_FROM_INSTANT_FIELD_NAME);
    doc.setVersionFromInstant(Instant.ofEpochMillis(versionFromTime.getTime()));
    Date versionToTime = (Date) dbObject.get(VERSION_TO_INSTANT_FIELD_NAME);
    Instant versionToInstant = Instant.ofEpochMillis(versionToTime.getTime());
    doc.setVersionToInstant(versionToInstant.equals(MAX_INSTANT) ? null : versionToInstant);
    doc.setValue(value);
    return doc;
  }

  //-------------------------------------------------------------------------
  /**
   * Converts a document to a Mongo DB object.
   * @param document  the document, not null
   * @return the DB object, not null
   */
  protected DBObject convertToDb(final ConfigDocument<T> document) {
    FudgeMsgEnvelope msg = getFudgeContext().toFudgeMsg(document.getValue());
    DBObject insertDoc = new BasicDBObject();
    insertDoc.put(ID_FIELD_NAME, extractRowId(document.getUniqueId()));
    insertDoc.put(OID_FIELD_NAME, extractOid(document.getUniqueId()));
    insertDoc.put(NAME_FIELD_NAME, document.getName());
    insertDoc.put(VERSION_FROM_INSTANT_FIELD_NAME, new Date(document.getVersionFromInstant().toEpochMillisLong()));
    if (document.getVersionToInstant() != null) {
      insertDoc.put(VERSION_TO_INSTANT_FIELD_NAME, new Date(document.getVersionToInstant().toEpochMillisLong()));
    } else {
      insertDoc.put(VERSION_TO_INSTANT_FIELD_NAME, new Date(MAX_INSTANT.toEpochMillisLong()));
    }
    insertDoc.put(VALUE_FIELD_NAME, getFudgeContext().fromFudgeMsg(DBObject.class, msg.getMessage()));
    s_logger.trace("Config converted config doc to object={}", insertDoc);
    return insertDoc;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string summary of this configuration master.
   * @return the string summary, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getIdentifierScheme() + "]";
  }

  @Override
  public void addChangeListener(MasterChangeListener listener) {
    throw new UnsupportedOperationException("Mongo implementation doesnt support MasterChange events");
  }

  @Override
  public void removeChangeListener(MasterChangeListener listener) {
    throw new UnsupportedOperationException("Mongo implementation doesnt support MasterChange events");
  }

}
