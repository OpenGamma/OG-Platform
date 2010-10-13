/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.config.db;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

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

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.WriteResult;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.config.ConfigDocument;
import com.opengamma.config.ConfigMaster;
import com.opengamma.config.ConfigSearchHistoricRequest;
import com.opengamma.config.ConfigSearchHistoricResult;
import com.opengamma.config.ConfigSearchRequest;
import com.opengamma.config.ConfigSearchResult;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.MongoDBConnectionSettings;
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
public class MongoDBConfigMasterSingleDoc<T> implements ConfigMaster<T> {
  // this implementation uses one document per version

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(MongoDBConfigMasterSingleDoc.class);
  /**
   * The maximum instant for use in date ranges.
   */
  private static final Instant MAX_INSTANT = OffsetDateTime.of(9999, 12, 31, 23, 59, 59, ZoneOffset.UTC).toInstant();
  /**
   * Mongo key for the id.
   */
  private static final String ID_FIELD_NAME = "_id";
  /**
   * Mongo key for the versions array.
   */
  private static final String VERSIONS_FIELD_NAME = "versions";
  /**
   * Mongo key for the version count.
   */
  private static final String VERSIONS_COUNT_FIELD_NAME = "versionsCount";
  /**
   * Mongo key for the version.
   */
  private static final String VERSION_FIELD_NAME = "version";
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
    NAME_FIELD_NAME, VERSION_FROM_INSTANT_FIELD_NAME, VERSION_TO_INSTANT_FIELD_NAME};

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
   * Whether to update the last read time.
   */
  private final boolean _updateLastReadTime;
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
  public MongoDBConfigMasterSingleDoc(final Class<T> documentClazz, final MongoDBConnectionSettings mongoSettings) {
    this(documentClazz, mongoSettings, true);
  }

  /**
   * Creates an instance.
   * @param documentClazz  the element type, not null
   * @param mongoSettings  the Mongo connection settings, not null
   * @param updateLastRead  whether to update the last read flag
   */
  public MongoDBConfigMasterSingleDoc(final Class<T> documentClazz, final MongoDBConnectionSettings mongoSettings, boolean updateLastRead) {
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
    _updateLastReadTime = updateLastRead;
    String status = _updateLastReadTime ? "updateLastReadTime" : "readWithoutUpdate";
    s_logger.info("creating MongoDBConfigurationRepo for {}", status);
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
   * Gets whether this instance updates the last read instant.
   * @return the last read flag
   */
  protected boolean isUpdateLastRead() {
    return _updateLastReadTime;
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
    // TODO
    
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getPagingRequest(), "request.pagingRequest");
    
//    Instant now = Instant.now(getTimeSource());
    String name = request.getName();
    DBObject queryObj = new BasicDBObject();
    DBObject filterObj = new BasicDBObject();
    if (request.getVersionAsOfInstant() == null) {
      if (name != null) {
        queryObj.put(NAME_FIELD_NAME, createPattern(name));
      }
      filterObj = new BasicDBObject(VERSIONS_FIELD_NAME, new BasicDBObject("$slice", -1));
    } else {
      if (name != null) {
        queryObj.put("$elemMatch", new BasicDBObject(NAME_FIELD_NAME, createPattern(name)));
      }
//      
//      
//      if (request.getVersionsFromInstant() != null && request.getVersionsFromInstant().equals(request.getVersionsToInstant())) {
//        queryObj.put(VERSION_FROM_INSTANT_FIELD_NAME, new BasicDBObject("$lte", new Date(request.getVersionsFromInstant().toEpochMillisLong())));
//        queryObj.put(VERSION_TO_INSTANT_FIELD_NAME, new BasicDBObject("$gt", new Date(request.getVersionsFromInstant().toEpochMillisLong())));
//      } else {
//        String search = "";
//        if (request.getVersionsFromInstant() != null) {
//          String searchFrom = "new Date(" + request.getVersionAsOfInstant().toEpochMillisLong() + ")";
//          search += "((this." + VERSIONS_FIELD_NAME +  + VERSION_FROM_INSTANT_FIELD_NAME + " <= " + searchFrom + " && " +
//                      "this." + VERSION_TO_INSTANT_FIELD_NAME + " > " + searchFrom + ") || " +
//                     "this." + VERSION_FROM_INSTANT_FIELD_NAME + " >= " + searchFrom + ") ";
//        }
//        queryObj.put("$where", search);
//      }
//      Instant versionAsOf = Objects.firstNonNull(request.getVersionAsOfInstant(), now);
//      queryObj.put(VERSION_FROM_INSTANT_FIELD_NAME, new BasicDBObject("$lte", new Date(request.getVersionAsOfInstant().toEpochMillisLong())));
//      queryObj.put(VERSION_TO_INSTANT_FIELD_NAME, new BasicDBObject("$gt", new Date(request.getVersionAsOfInstant().toEpochMillisLong())));
    }
    
    BasicDBObject sortObj = new BasicDBObject(NAME_FIELD_NAME, 1);
    return doSearch(queryObj, filterObj, sortObj, request.getPagingRequest());
  }

  /**
   * Creates a suitable regex pattern to wildcard match the input.
   * @param text  the text to match, not null
   * @return the pattern, not null
   */
  protected Pattern createPattern(String text) {
    StringTokenizer tkn = new StringTokenizer(text, "?*", true);
    StringBuilder buf = new StringBuilder(text.length() + 10);
    buf.append('^');
    boolean lastStar = false;
    while (tkn.hasMoreTokens()) {
      String str = tkn.nextToken();
      if (str.equals("?")) {
        buf.append('.');
        lastStar = false;
      } else if (str.equals("*")) {
        if (lastStar == false) {
          buf.append(".*");
        }
        lastStar = true;
      } else {
        buf.append(Pattern.quote(str));
        lastStar = false;
      }
    }
    buf.append('$');
    return Pattern.compile(buf.toString(), Pattern.CASE_INSENSITIVE);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument<T> get(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    checkScheme(uid);
    Instant now = Instant.now(getTimeSource());
    DBCollection dbColl = createDBCollection();
    
    DBObject queryObj = new BasicDBObject(ID_FIELD_NAME, extractOid(uid));
    BasicDBObject filterObj = null;
    if (uid.isVersioned()) {
      queryObj.put(VERSIONS_FIELD_NAME, new BasicDBObject("$elemMatch", new BasicDBObject(VERSION_FIELD_NAME, extractVersion(uid))));
    } else {
      queryObj.put(VERSIONS_FIELD_NAME + "." + VERSION_TO_INSTANT_FIELD_NAME, new Date(MAX_INSTANT.toEpochMillisLong()));
      filterObj = new BasicDBObject(VERSIONS_FIELD_NAME, new BasicDBObject("$slice", -1));
    }
    DBObject updateObj = new BasicDBObject("$set", new BasicDBObject(LAST_READ_INSTANT_FIELD_NAME, new Date(now.toEpochMillisLong())));
    DBObject matchedObj;
    s_logger.debug("Config get, query={} filter={}", queryObj, filterObj);
    if (isUpdateLastRead()) {
      matchedObj = dbColl.findAndModify(queryObj, filterObj, null, false, updateObj, true, false);
    } else {
      matchedObj = dbColl.findOne(queryObj, filterObj);
    }
    if (matchedObj == null) {
      throw new DataNotFoundException("Configuration not found: " + uid);
    }
    return convertFromDb(matchedObj, 0);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument<T> add(final ConfigDocument<T> document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getName(), "document.name");
    ArgumentChecker.notNull(document.getValue(), "document.value");
    Instant now = Instant.now(getTimeSource());
    DBCollection dbColl = createDBCollection();
    
    // prepare
    ObjectId id = ObjectId.get();
    document.setConfigId(UniqueIdentifier.of(getIdentifierScheme(), id.toString(), "1"));
    document.setVersionNumber(1);
    document.setVersionFromInstant(now);
    document.setVersionToInstant(null);
    document.setLastReadInstant(now);
    
    // insert
    DBObject mainObj = new BasicDBObject();
    mainObj.put(ID_FIELD_NAME, id);
    mainObj.put(VERSION_FIELD_NAME, 1);
    mainObj.put(VERSIONS_COUNT_FIELD_NAME, 1);
    mainObj.put(NAME_FIELD_NAME, document.getName());
    mainObj.put(LAST_READ_INSTANT_FIELD_NAME, new Date(now.toEpochMillisLong()));
    mainObj.put(VERSIONS_FIELD_NAME, Collections.singletonList(convertToDbVersion(document)));
    s_logger.debug("Config add, main={}", mainObj);
    dbColl.insert(mainObj).getLastError().throwOnError();
    return document;
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument<T> update(final ConfigDocument<T> document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getName(), "document.name");
    ArgumentChecker.notNull(document.getValue(), "document.value");
    ArgumentChecker.notNull(document.getConfigId(), "document.configId");
    checkScheme(document.getConfigId());
    Instant now = Instant.now(getTimeSource());
    DBCollection dbColl = createDBCollection();
    
    // find document to update
    UniqueIdentifier uid = document.getConfigId();
    DBObject queryObj = new BasicDBObject(ID_FIELD_NAME, extractOid(uid));
    DBObject matchedObj = dbColl.findOne(queryObj);
    if (matchedObj == null) {
      throw new DataNotFoundException("Configuration not found: " + uid);
    }
    @SuppressWarnings("unchecked")
    List<DBObject> versionsObj = (List<DBObject>) matchedObj.get(VERSIONS_FIELD_NAME);
    if (versionsObj.get(0).get(VERSION_TO_INSTANT_FIELD_NAME).equals(new Date(MAX_INSTANT.toEpochMillisLong())) == false) {
      throw new IllegalArgumentException("UniqueIdentifier is not latest version: " + uid);
    }
    
    // makes changes locally and update
    int newVersion = ((Integer) matchedObj.get(VERSION_FIELD_NAME)) + 1;
    document.setConfigId(uid.withVersion(Integer.toString(newVersion)));
    document.setVersionNumber(newVersion);
    document.setVersionFromInstant(now);
    document.setVersionToInstant(null);
    document.setLastReadInstant(now);
    matchedObj.put(VERSION_FIELD_NAME, newVersion);
    matchedObj.put(VERSIONS_COUNT_FIELD_NAME, ((Integer) matchedObj.get(VERSIONS_COUNT_FIELD_NAME)) + 1);
    matchedObj.put(NAME_FIELD_NAME, document.getName());
    versionsObj.get(0).put(VERSION_TO_INSTANT_FIELD_NAME, new Date(now.toEpochMillisLong()));
    versionsObj.add(convertToDbVersion(document));
    WriteResult result = dbColl.update(queryObj, matchedObj);
    if (result.getN() != 1) {
      throw new IllegalArgumentException("UniqueIdentifier is not latest version: " + uid);
    }
    result.getLastError().throwOnError();
    return document;
    
//    ConfigDocument<T> oldDoc = get(uid);  // checks uid exists
//    if (oldDoc.getVersionToInstant() != null) {
//      throw new IllegalArgumentException("UniqueIdentifier is not latest version: " + uid);
//    }
//    int newVersion = oldDoc.getVersionNumber() + 1;
//    Instant now = Instant.now(getTimeSource());
//    document.setConfigId(uid.withVersion(Integer.toString(newVersion)));
//    document.setVersionNumber(newVersion);
//    document.setVersionFromInstant(now);
//    document.setVersionToInstant(null);
//    document.setLastReadInstant(now);
//    
//    // update
//    DBObject queryObj = new BasicDBObject(ID_FIELD_NAME, extractOid(uid));
//    int version = extractVersion(uid);
//    queryObj.put(VERSION_FIELD_NAME, version);
//    queryObj.put(VERSIONS_FIELD_NAME + "." + VERSION_FIELD_NAME, version);
////    queryObj.put(VERSIONS_FIELD_NAME, new BasicDBObject("$elemMatch", new BasicDBObject(VERSION_FIELD_NAME, version)));
//    DBObject updateObj = new BasicDBObject();
//    BasicDBObject setObj = new BasicDBObject();
//    setObj.put(VERSION_FIELD_NAME, newVersion);
//    setObj.put(NAME_FIELD_NAME, document.getName());
//    setObj.put(VERSIONS_FIELD_NAME + ".$." + VERSION_TO_INSTANT_FIELD_NAME, new Date(now.toEpochMillisLong()));
//    updateObj.put("$set", setObj);
//    updateObj.put("$inc", new BasicDBObject(VERSIONS_COUNT_FIELD_NAME, 1));
//    updateObj.put("$push", new BasicDBObject(VERSIONS_FIELD_NAME, convertToDbVersion(document)));
//    s_logger.debug("Config update, query={} update={}", queryObj, updateObj);
//    WriteResult result = createDBCollection().update(queryObj, updateObj);
//    if (result.getN() != 1) {
//      throw new IllegalArgumentException("UniqueIdentifier is not latest version: " + uid);
//    }
//    result.getLastError().throwOnError();
//    return document;
//////updateObj.put(VERSIONS_FIELD_NAME + ".$." + VERSION_TO_INSTANT_FIELD_NAME, new BasicDBObject());
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    checkScheme(uid);
    Instant now = Instant.now(getTimeSource());
    DBCollection dbColl = createDBCollection();
    
    // load old row
    ConfigDocument<T> oldDoc = get(uid);  // checks uid exists
    if (oldDoc.getVersionToInstant() != null) {
      throw new IllegalArgumentException("UniqueIdentifier is not latest version: " + uid);
    }
    // update latest row
    DBObject queryObj = new BasicDBObject();
    queryObj.put(ID_FIELD_NAME, extractOid(uid));
    queryObj.put(VERSION_FIELD_NAME, extractVersion(uid));
    queryObj.put(VERSIONS_FIELD_NAME + "." + VERSION_FIELD_NAME, extractVersion(uid));
    DBObject updateObj = new BasicDBObject("$set",
        new BasicDBObject(VERSIONS_FIELD_NAME + ".$." + VERSION_TO_INSTANT_FIELD_NAME, new Date(now.toEpochMillisLong())));
    s_logger.debug("Config remove, find={} action={}", queryObj, updateObj);
    WriteResult result = dbColl.update(queryObj, updateObj);
    if (result.getN() != 1) {
      throw new IllegalArgumentException("UniqueIdentifier is not latest version: " + uid);
    }
    result.getLastError().throwOnError();
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigSearchHistoricResult<T> searchHistoric(final ConfigSearchHistoricRequest request) {
    // TODO
    
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getConfigId(), "request.configId");
    checkScheme(request.getConfigId());
    
    DBObject queryObj = new BasicDBObject();
//    queryObj.put(OID_FIELD_NAME, request.getConfigId().getValue());
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
    
    ConfigSearchResult<T> configSearchResult = doSearch(queryObj, null, sortObj, request.getPagingRequest());
    ConfigSearchHistoricResult<T> result = new ConfigSearchHistoricResult<T>();
    result.setPaging(configSearchResult.getPaging());
    result.setDocuments(configSearchResult.getDocuments());
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Extracts the oid.
   * @param id  the identifier to extract from, not null
   * @return the extracted oid
   */
  protected ObjectId extractOid(final UniqueIdentifier id) {
    try {
      return new ObjectId(id.getValue());
    } catch (IllegalArgumentException ex) {
      throw new DataNotFoundException("Config not found: " + id);
    }
  }

  /**
   * Extracts the version id.
   * @param id  the identifier to extract from, not null
   * @return the extracted version id
   */
  protected int extractVersion(final UniqueIdentifier id) {
    return Integer.parseInt(id.getVersion());
  }

  /**
   * Finds documents.
   * @param queryObj  the query, not null
   * @param filterObj  the filter, not null
   * @param sortObj  the sort, may be null
   * @param pagingRequest  the paging request, not null
   * @return the search results, not null
   */
  protected ConfigSearchResult<T> doSearch(final DBObject queryObj, final DBObject filterObj, final DBObject sortObj, final PagingRequest pagingRequest) {
    s_logger.debug("Config find, query={} sort={}", queryObj, sortObj);
    DBCollection dbColl = createDBCollection();
    
    ConfigSearchResult<T> result = new ConfigSearchResult<T>();
//    Instant now = Instant.now(getTimeSource());
//    DBObject updateObj = new BasicDBObject("$set", new BasicDBObject(LAST_READ_INSTANT_FIELD_NAME, new Date(now.toEpochMillisLong())));
    
//    // get
//    if (pagingRequest.getPage() == 1 && pagingRequest.getPagingSize() == 1) {
//      DBObject matched = dbCollection.findAndModify(queryObj, filterObj, sortObj, false, updateObj, true, false);
//      if (matched == null) {
//        result.setPaging(Paging.of(Collections.emptyList(), pagingRequest));
//      }
//      return result;
//    }
    
    // search
    int count = dbColl.find(queryObj).count();
    DBCursor cursor = dbColl.find(queryObj, filterObj).sort(sortObj).skip(pagingRequest.getFirstItemIndex()).limit(pagingRequest.getPagingSize());
    result.setPaging(new Paging(pagingRequest, count));
    for (DBObject dbObject : cursor) {
      result.getDocuments().add(convertFromDb(dbObject, 0));
    }
//    if (isUpdateLastRead()) {
//      for (ConfigDocument<T> doc : result.getDocuments()) {
//        DBObject updateQueryObj = new BasicDBObject();
//        updateQueryObj.put(ID_FIELD_NAME, extractOid(doc.getConfigId()));
//        updateQueryObj.put(LAST_READ_INSTANT_FIELD_NAME, new Date(doc.getLastReadInstant().toEpochMillisLong()));
//        dbCollection.update(updateQueryObj, updateObj).getLastError().throwOnError();
//        doc.setLastReadInstant(now);
//      }
//    }
    return result;
  }

  /**
   * Converts a Mongo DB object to a document.
   * @param dbObject  the DB object, not null
   * @param index  the index to parse
   * @return the document, not null
   */
  protected ConfigDocument<T> convertFromDb(DBObject dbObject, int index) {
    s_logger.debug("Config converting dbOject to config doc={}", dbObject);
    ConfigDocument<T> doc = new ConfigDocument<T>();
    ObjectId id = (ObjectId) dbObject.get(ID_FIELD_NAME);
    doc.setName((String) dbObject.get(NAME_FIELD_NAME));
    Date lastRead = (Date) dbObject.get(LAST_READ_INSTANT_FIELD_NAME);
    doc.setLastReadInstant(Instant.ofEpochMillis(lastRead.getTime()));
    
    @SuppressWarnings("unchecked")
    List<DBObject> versionsData = (List<DBObject>) dbObject.get(VERSIONS_FIELD_NAME);
    DBObject versionData = versionsData.get(index);
    Integer version = (Integer) versionData.get(VERSION_FIELD_NAME);
    doc.setConfigId(UniqueIdentifier.of(getIdentifierScheme(), id.toString(), version.toString()));
    doc.setVersionNumber(version);
    DBObject valueData = (DBObject) versionData.get(VALUE_FIELD_NAME);
    FudgeSerializationContext fsc = new FudgeSerializationContext(getFudgeContext());
    MutableFudgeFieldContainer dbObjecToFudge = fsc.objectToFudgeMsg(valueData);
    FudgeDeserializationContext fdc = new FudgeDeserializationContext(getFudgeContext());
    T value = fdc.fudgeMsgToObject(_entityClazz, dbObjecToFudge);
    Date versionFrom = (Date) versionData.get(VERSION_FROM_INSTANT_FIELD_NAME);
    doc.setVersionFromInstant(Instant.ofEpochMillis(versionFrom.getTime()));
    Date versionTo = (Date) versionData.get(VERSION_TO_INSTANT_FIELD_NAME);
    Instant versionToInstant = Instant.ofEpochMillis(versionTo.getTime());
    doc.setVersionToInstant(versionToInstant.equals(MAX_INSTANT) ? null : versionToInstant);
    doc.setValue(value);
    return doc;
  }

  //-------------------------------------------------------------------------
  /**
   * Converts a document version to a Mongo DB object.
   * @param document  the document, not null
   * @return the DB object, not null
   */
  protected DBObject convertToDbVersion(final ConfigDocument<T> document) {
    DBObject obj = new BasicDBObject();
    obj.put(VERSION_FIELD_NAME, document.getVersionNumber());
    obj.put(VERSION_FROM_INSTANT_FIELD_NAME, new Date(document.getVersionFromInstant().toEpochMillisLong()));
    if (document.getVersionToInstant() != null) {
      obj.put(VERSION_TO_INSTANT_FIELD_NAME, new Date(document.getVersionToInstant().toEpochMillisLong()));
    } else {
      obj.put(VERSION_TO_INSTANT_FIELD_NAME, new Date(MAX_INSTANT.toEpochMillisLong()));
    }
    obj.put(NAME_FIELD_NAME, document.getName());
    FudgeMsgEnvelope msg = getFudgeContext().toFudgeMsg(document.getValue());
    obj.put(VALUE_FIELD_NAME, getFudgeContext().fromFudgeMsg(DBObject.class, msg.getMessage()));
    return obj;
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

}
