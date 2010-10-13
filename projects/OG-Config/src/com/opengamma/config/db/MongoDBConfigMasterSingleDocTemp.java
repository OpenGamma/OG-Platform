/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.config.db;


/**
 * A configuration master implemented using the Mongo database.
 * <p>
 * The {@code MongoDBConnectionSettings} specifies the connection details to use.
 * If collectionName is null, the document class name will be used.
 * 
 * @param <T>  the configuration document type
 */
public class MongoDBConfigMasterSingleDocTemp<T> /* implements ConfigMaster<T>*/ {

//  /** Logger. */
//  private static final Logger s_logger = LoggerFactory.getLogger(CopyOfMongoDBConfigMaster.class);
//  /**
//   * The maximum instant for use in date ranges.
//   */
//  private static final Instant MAX_INSTANT = OffsetDateTime.of(9999, 12, 31, 23, 59, 59, ZoneOffset.UTC).toInstant();
//  /**
//   * Mongo key for the id.
//   */
//  private static final String ID_FIELD_NAME = "_id";
//  /**
//   * Mongo key for the versions array.
//   */
//  private static final String VERSIONS_FIELD_NAME = "versions";
//  /**
//   * Mongo key for the version count.
//   */
//  private static final String VERSION_COUNT_FIELD_NAME = "versionCount";
//  /**
//   * Mongo key for the version.
//   */
//  private static final String VERSION_FIELD_NAME = "version";
//  /**
//   * Mongo key for the name.
//   */
//  private static final String NAME_FIELD_NAME = "name";
//  /**
//   * Mongo key for version from.
//   */
//  private static final String VERSION_FROM_INSTANT_FIELD_NAME = "versionFromInstant";
//  /**
//   * Mongo key for version from.
//   */
//  private static final String VERSION_TO_INSTANT_FIELD_NAME = "versionToInstant";
//  /**
//   * Mongo key for the lastRead.
//   */
//  private static final String LAST_READ_INSTANT_FIELD_NAME = "lastReadInstant";
//  /**
//   * Mongo key for the value.
//   */
//  private static final String VALUE_FIELD_NAME = "value";
//  /**
//   * Mongo indices.
//   */
//  private static final String[] INDICES = {
//    OID_FIELD_NAME, NAME_FIELD_NAME, VERSION_FROM_INSTANT_FIELD_NAME, VERSION_TO_INSTANT_FIELD_NAME};
//
//  /**
//   * The scheme used by the master by default.
//   */
//  public static final String IDENTIFIER_SCHEME_DEFAULT = "MongoConfig";
//
//  /**
//   * The configuration element type.
//   */
//  private final Class<T> _entityClazz;
//  /**
//   * The Mongo settings.
//   */
//  private final MongoDBConnectionSettings _mongoSettings;
//  /**
//   * The Mongo instance.
//   */
//  private final Mongo _mongo;
//  /**
//   * Whether to update the last read time.
//   */
//  private final boolean _updateLastReadTime;
//  /**
//   * The scheme to use for unique identifiers.
//   */
//  private String _identifierScheme = IDENTIFIER_SCHEME_DEFAULT;
//  /**
//   * The time-source to use.
//   */
//  private TimeSource _timeSource = TimeSource.system();
//
//  /**
//   * Creates an instance.
//   * @param documentClazz  the element type, not null
//   * @param mongoSettings  the Mongo connection settings, not null
//   */
//  public CopyOfMongoDBConfigMaster(final Class<T> documentClazz, final MongoDBConnectionSettings mongoSettings) {
//    this(documentClazz, mongoSettings, true);
//  }
//
//  /**
//   * Creates an instance.
//   * @param documentClazz  the element type, not null
//   * @param mongoSettings  the Mongo connection settings, not null
//   * @param updateLastRead  whether to update the last read flag
//   */
//  public CopyOfMongoDBConfigMaster(final Class<T> documentClazz, final MongoDBConnectionSettings mongoSettings, boolean updateLastRead) {
//    ArgumentChecker.notNull(documentClazz, "document class");
//    ArgumentChecker.notNull(mongoSettings, "MongoDB settings");
//    
//    _entityClazz = documentClazz;
//    if (mongoSettings.getCollectionName() == null) {
//      mongoSettings.setCollectionName(_entityClazz.getSimpleName());
//    }
//    _mongoSettings = mongoSettings;
//    s_logger.info("Connecting to {}", mongoSettings);
//    try {
//      _mongo = new Mongo(mongoSettings.getHost(), mongoSettings.getPort());
//    } catch (Exception e) {
//      throw new OpenGammaRuntimeException("Unable to connect to MongoDB at " + mongoSettings, e);
//    }
//    
//    ensureIndices();
//    _updateLastReadTime = updateLastRead;
//    String status = _updateLastReadTime ? "updateLastReadTime" : "readWithoutUpdate";
//    s_logger.info("creating MongoDBConfigurationRepo for {}", status);
//  }
//
//  private void ensureIndices() {
//    DBCollection dbCollection = createDBCollection();
//    for (String field : INDICES) {
//      String collectionName = getMongoConnectionSettings().getCollectionName();
//      s_logger.info("ensuring index for {} {}:{}", new Object[] {field, getMongoConnectionSettings().getDatabase(), collectionName});
//      dbCollection.ensureIndex(new BasicDBObject(field, 1), "ix_" + field + "_asc");
//      dbCollection.ensureIndex(new BasicDBObject(field, -1), "ix_" + field + "_desc");
//    }
//  }
//
//  //-------------------------------------------------------------------------
//  /**
//   * Gets the instance of Mongo.
//   * @return the Mongo instance, not null
//   */
//  protected Mongo getMongo() {
//    return _mongo;
//  }
//
//  /**
//   * Gets the connection settings.
//   * @return the Mongo settings, not null
//   */
//  protected MongoDBConnectionSettings getMongoConnectionSettings() {
//    return _mongoSettings;
//  }
//
//  /**
//   * Gets the Fudge context.
//   * @return the Fudge context, not null
//   */
//  protected FudgeContext getFudgeContext() {
//    return OpenGammaFudgeContext.getInstance();
//  }
//
//  //-------------------------------------------------------------------------
//  /**
//   * Creates a new Mongo collection wrapping the database.
//   * @return the Mongo collection, not null
//   */
//  protected DBCollection createDBMainCollection() {
//    DB db = getMongo().getDB(getMongoConnectionSettings().getDatabase());
//    return db.getCollection(getMongoConnectionSettings().getCollectionName());
//  }
//
//  /**
//   * Creates a new Mongo collection wrapping the database.
//   * @return the Mongo collection, not null
//   */
//  protected DBCollection createDBValueCollection() {
//    DB db = getMongo().getDB(getMongoConnectionSettings().getDatabase());
//    return db.getCollection(getMongoConnectionSettings().getCollectionName() + "_values");
//  }
//
//  //-------------------------------------------------------------------------
//  /**
//   * Gets the time-source that determines the current time.
//   * @return the time-source, not null
//   */
//  public TimeSource getTimeSource() {
//    return _timeSource;
//  }
//
//  /**
//   * Sets the time-source.
//   * @param timeSource  the time-source, not null
//   */
//  public void setTimeSource(final TimeSource timeSource) {
//    ArgumentChecker.notNull(timeSource, "timeSource");
//    s_logger.debug("installed TimeSource: {}", timeSource);
//    _timeSource = timeSource;
//  }
//
//  //-------------------------------------------------------------------------
//  /**
//   * Gets the scheme in use for UniqueIdentifier.
//   * @return the scheme, not null
//   */
//  public String getIdentifierScheme() {
//    return _identifierScheme;
//  }
//
//  /**
//   * Sets the scheme in use for UniqueIdentifier.
//   * @param scheme  the scheme, not null
//   */
//  public void setIdentifierScheme(final String scheme) {
//    ArgumentChecker.notNull(scheme, "scheme");
//    s_logger.debug("installed IdentifierScheme: {}", scheme);
//    _identifierScheme = scheme;
//  }
//
//  /**
//   * Checks the scheme is valid.
//   * @param uid  the unique identifier
//   */
//  protected void checkScheme(final UniqueIdentifier uid) {
//    if (getIdentifierScheme().equals(uid.getScheme()) == false) {
//      throw new IllegalArgumentException("UniqueIdentifier is not from this Config master: " + uid);
//    }
//  }
//
//  //-------------------------------------------------------------------------
//  @Override
//  public ConfigSearchResult<T> search(final ConfigSearchRequest request) {
//    ArgumentChecker.notNull(request, "request");
//    ArgumentChecker.notNull(request.getPagingRequest(), "request.pagingRequest");
//    
//    Instant now = Instant.now(getTimeSource());
//    Instant versionAsOf = Objects.firstNonNull(request.getVersionAsOfInstant(), now);
//    String name = request.getName();
//    DBObject queryObj = new BasicDBObject();
//    if (name != null) {
//      queryObj.put(NAME_FIELD_NAME, createPattern(name));
//    }
//    queryObj.put(VERSION_FROM_INSTANT_FIELD_NAME, new BasicDBObject("$lte", new Date(versionAsOf.toEpochMillisLong())));
//    queryObj.put(VERSION_TO_INSTANT_FIELD_NAME, new BasicDBObject("$gt", new Date(versionAsOf.toEpochMillisLong())));
//    BasicDBObject sortObj = new BasicDBObject(NAME_FIELD_NAME, 1);
//    return find(queryObj, sortObj, request.getPagingRequest());
//  }
//
//  /**
//   * Creates a suitable regex pattern to wildcard match the input.
//   * @param text  the text to match, not null
//   * @return the pattern, not null
//   */
//  protected Pattern createPattern(String text) {
//    StringTokenizer tkn = new StringTokenizer(text, "?*", true);
//    StringBuilder buf = new StringBuilder(text.length() + 10);
//    buf.append('^');
//    boolean lastStar = false;
//    while (tkn.hasMoreTokens()) {
//      String str = tkn.nextToken();
//      if (str.equals("?")) {
//        buf.append('.');
//        lastStar = false;
//      } else if (str.equals("*")) {
//        if (lastStar == false) {
//          buf.append(".*");
//        }
//        lastStar = true;
//      } else {
//        buf.append(Pattern.quote(str));
//        lastStar = false;
//      }
//    }
//    buf.append('$');
//    return Pattern.compile(buf.toString(), Pattern.CASE_INSENSITIVE);
//  }
//
//  //-------------------------------------------------------------------------
//  @Override
//  public ConfigDocument<T> get(final UniqueIdentifier uid) {
//    ArgumentChecker.notNull(uid, "uid");
//    checkScheme(uid);
//    
//    DBObject queryObj = new BasicDBObject(ID_FIELD_NAME, extractOid(uid));
//    BasicDBObject filterObj = null;
//    if (uid.isVersioned()) {
//      queryObj.put(VERSIONS_FIELD_NAME + "." + VERSION_FIELD_NAME, extractVersion(uid));
//    } else {
//      filterObj.put(VERSIONS_FIELD_NAME, new BasicDBObject("$slice", -1));
//    }
//    Instant now = Instant.now(getTimeSource());
//    DBObject updateObj = new BasicDBObject("$set", new BasicDBObject(LAST_READ_INSTANT_FIELD_NAME, new Date(now.toEpochMillisLong())));
//    DBCollection dbCollection = createDBCollection();
//    DBObject matched;
//    if (_updateLastReadTime) {
//      matched = dbCollection.findAndModify(queryObj, filterObj, null, false, updateObj, true, false);
//      if (matched == null) {
//        throw new DataNotFoundException("Configuration not found: " + uid);
//      }
//    } else {
//      DBCursor cursor = dbCollection.find(queryObj).sort(sortObj).skip(0).limit(1);
//      if (cursor.hasNext()) {
//        throw new DataNotFoundException("Configuration not found: " + uid);
//      }
//      matched = cursor.next();
//    }
//    return convertFromDb(matched);
//  }
//
//  //-------------------------------------------------------------------------
//  @Override
//  public ConfigDocument<T> add(final ConfigDocument<T> document) {
//    ArgumentChecker.notNull(document, "document");
//    ArgumentChecker.notNull(document.getName(), "document.name");
//    ArgumentChecker.notNull(document.getValue(), "document.value");
//    
//    // prepare
//    Instant now = Instant.now(getTimeSource());
//    ObjectId id = ObjectId.get();
//    document.setConfigId(UniqueIdentifier.of(getIdentifierScheme(), id.toString(), "1"));
//    document.setVersionNumber(1);
//    document.setVersionFromInstant(now);
//    document.setVersionToInstant(null);
//    document.setLastReadInstant(now);
//    
//    // insert
//    ObjectId valueId = insertValue(document);
//    DBObject mainObj = new BasicDBObject();
//    mainObj.put(ID_FIELD_NAME, id);
//    mainObj.put(VERSION_FIELD_NAME, 1);
//    mainObj.put(VERSION_COUNT_FIELD_NAME, 1);
//    mainObj.put(NAME_FIELD_NAME, document.getName());
//    mainObj.put(LAST_READ_INSTANT_FIELD_NAME, new Date(now.toEpochMillisLong()));
//    mainObj.put(VERSIONS_FIELD_NAME, Collections.singletonList(convertToDbVersion(document, valueId)));
//    s_logger.debug("Config add, main={}", mainObj);
//    createDBMainCollection().insert(mainObj).getLastError().throwOnError();
//    return document;
//  }
//
//  //-------------------------------------------------------------------------
//  @Override
//  public ConfigDocument<T> update(final ConfigDocument<T> document) {
//    ArgumentChecker.notNull(document, "document");
//    ArgumentChecker.notNull(document.getName(), "document.name");
//    ArgumentChecker.notNull(document.getValue(), "document.value");
//    ArgumentChecker.notNull(document.getConfigId(), "document.configId");
//    checkScheme(document.getConfigId());
//    
//    // prepare
//    ConfigDocument<T> oldDoc = get(document.getConfigId());  // checks uid exists
//    if (oldDoc.getVersionToInstant() != null) {
//      throw new IllegalArgumentException("UniqueIdentifier is not latest version: " + document.getConfigId());
//    }
//    Instant now = Instant.now(getTimeSource());
//    ObjectId id = ObjectId.get();
//    document.setConfigId(document.getConfigId().withVersion(id.toString()));
//    document.setVersionNumber(oldDoc.getVersionNumber() + 1);
//    document.setVersionFromInstant(now);
//    document.setVersionToInstant(null);
//    document.setLastReadInstant(now);
//    
//    // insert detail
//    ObjectId valueId = insertValue(document);  // insert new value, no rollback required (although it is desirable)
//    
//    // update main
//    DBObject queryObj = new BasicDBObject();
//    queryObj.put(ID_FIELD_NAME, extractOid(oldDoc.getConfigId()));
//    queryObj.put(VERSION_FIELD_NAME, extractVersion(oldDoc.getConfigId()));
//    BasicDBObject updateObj = new BasicDBObject();
//    updateObj.put("$inc", new BasicDBObject(VERSION_FIELD_NAME, 1));
//    updateObj.put("$set", new BasicDBObject(NAME_FIELD_NAME, document.getName()));
//    updateObj.put("$push", new BasicDBObject(VERSIONS_FIELD_NAME, convertToDbVersion(document, valueId)));
//    s_logger.debug("Config update, find={} action={}", queryObj, updateObj);
//    CommandResult lastError = createDBMainCollection().update(queryObj, updateObj).getLastError();
//    if (lastError.ok() == false) {
//      try {
//        // remove invalid document
//        DBObject removeQueryObj = new BasicDBObject();
//        removeQueryObj.put(ID_FIELD_NAME, id);
//        s_logger.debug("Config update, update={} action={}", queryObj, updateObj);
//        createDBValueCollection().remove(removeQueryObj).getLastError().throwOnError();
//      } catch (Exception ex) {
//        // rollback failure is OK, just wastes DB space
//      }
//      lastError.throwOnError();  // signal error from update
//    }
//    return document;
//  }
//
//  //-------------------------------------------------------------------------
//  @Override
//  public void remove(final UniqueIdentifier uid) {
//    ArgumentChecker.notNull(uid, "uid");
//    checkScheme(uid);
//    
//    // load old row
//    ConfigDocument<T> oldDoc = get(uid);  // checks uid exists
//    if (oldDoc.getVersionToInstant() != null) {
//      throw new IllegalArgumentException("UniqueIdentifier is not latest version: " + uid);
//    }
//    // update latest row, even during updates
//    Instant now = Instant.now(getTimeSource());
//    DBObject queryObj = new BasicDBObject();
//    queryObj.put(OID_FIELD_NAME, uid.getValue());
//    queryObj.put(VERSION_TO_INSTANT_FIELD_NAME, new Date(MAX_INSTANT.toEpochMillisLong()));
//    BasicDBObject sortObj = new BasicDBObject(VERSION_FROM_INSTANT_FIELD_NAME, -1);  // gets correct one during updates
//    DBObject updateObj = new BasicDBObject("$set", new BasicDBObject(VERSION_TO_INSTANT_FIELD_NAME, new Date(now.toEpochMillisLong())));
//    DBCollection dbCollection = createDBCollection();
//    s_logger.debug("Config remove, find={} action={}", queryObj, updateObj);
//    dbCollection.findAndModify(queryObj, sortObj, updateObj);
//    dbCollection.getDB().getLastError().throwOnError();
//  }
//
//  //-------------------------------------------------------------------------
//  @Override
//  public ConfigSearchHistoricResult<T> searchHistoric(final ConfigSearchHistoricRequest request) {
//    ArgumentChecker.notNull(request, "request");
//    ArgumentChecker.notNull(request.getConfigId(), "request.configId");
//    checkScheme(request.getConfigId());
//    
//    DBObject queryObj = new BasicDBObject();
//    queryObj.put(OID_FIELD_NAME, request.getConfigId().getValue());
//    if (request.getVersionsFromInstant() != null && request.getVersionsFromInstant().equals(request.getVersionsToInstant())) {
//      queryObj.put(VERSION_FROM_INSTANT_FIELD_NAME, new BasicDBObject("$lte", new Date(request.getVersionsFromInstant().toEpochMillisLong())));
//      queryObj.put(VERSION_TO_INSTANT_FIELD_NAME, new BasicDBObject("$gt", new Date(request.getVersionsFromInstant().toEpochMillisLong())));
//    } else {
//      String search = "";
//      if (request.getVersionsFromInstant() != null) {
//        String searchFrom = "new Date(" + request.getVersionsFromInstant().toEpochMillisLong() + ")";
//        search += "((this." + VERSION_FROM_INSTANT_FIELD_NAME + " <= " + searchFrom + " && " +
//                    "this." + VERSION_TO_INSTANT_FIELD_NAME + " > " + searchFrom + ") || " +
//                   "this." + VERSION_FROM_INSTANT_FIELD_NAME + " >= " + searchFrom + ") ";
//      }
//      if (request.getVersionsToInstant() != null) {
//        if (search.length() > 0) {
//          search += " && ";
//        }
//        String searchTo = "new Date(" + request.getVersionsToInstant().toEpochMillisLong() + ")";
//        search += "((this." + VERSION_FROM_INSTANT_FIELD_NAME + " <= " + searchTo + " && " +
//                    "this." + VERSION_TO_INSTANT_FIELD_NAME + " > " + searchTo + ") || " +
//                   "this." + VERSION_TO_INSTANT_FIELD_NAME + " < " + searchTo + ") ";
//      }
//      if (search.length() > 0) {
//        queryObj.put("$where", search);
//      }
//    }
//    BasicDBObject sortObj = new BasicDBObject(VERSION_FROM_INSTANT_FIELD_NAME, -1);
//    
//    ConfigSearchResult<T> configSearchResult = find(queryObj, sortObj, request.getPagingRequest());
//    ConfigSearchHistoricResult<T> result = new ConfigSearchHistoricResult<T>();
//    result.setPaging(configSearchResult.getPaging());
//    result.setDocuments(configSearchResult.getDocuments());
//    return result;
//  }
//
//  //-------------------------------------------------------------------------
//  /**
//   * Extracts the oid.
//   * @param id  the identifier to extract from, not null
//   * @return the extracted oid
//   */
//  protected ObjectId extractOid(final UniqueIdentifier id) {
//    try {
//      return new ObjectId(id.getValue());
//    } catch (IllegalArgumentException ex) {
//      throw new DataNotFoundException("Config not found: " + id);
//    }
//  }
//
//  /**
//   * Extracts the version id.
//   * @param id  the identifier to extract from, not null
//   * @return the extracted version id
//   */
//  protected int extractVersion(final UniqueIdentifier id) {
//    return Integer.parseInt(id.getVersion());
//  }
//
//  /**
//   * Finds documents.
//   * @param queryObj  the query, not null
//   * @param sortObj  the sort, may be null
//   * @param pagingRequest  the paging request, not null
//   * @return the search results, not null
//   */
//  protected ConfigSearchResult<T> find(DBObject queryObj, DBObject sortObj, PagingRequest pagingRequest) {
//    s_logger.debug("Config find, query={} sort={}", queryObj, sortObj);
//    
//    ConfigSearchResult<T> result = new ConfigSearchResult<T>();
//    Instant now = Instant.now(getTimeSource());
//    DBObject updateObj = new BasicDBObject("$set", new BasicDBObject(LAST_READ_INSTANT_FIELD_NAME, new Date(now.toEpochMillisLong())));
//    
//    // get
//    DBCollection dbCollection = createDBCollection();
//    if (pagingRequest.getPage() == 1 && pagingRequest.getPagingSize() == 1) {
//      DBObject matched = dbCollection.findAndModify(queryObj, null, sortObj, false, updateObj, true, false);
//      if (matched == null) {
//        result.setPaging(Paging.of(Collections.emptyList(), pagingRequest));
//      }
//      return result;
//    }
//    
//    // search
//    int count = dbCollection.find(queryObj).count();
//    DBCursor cursor = dbCollection.find(queryObj).sort(sortObj).skip(pagingRequest.getFirstItemIndex()).limit(pagingRequest.getPagingSize());
//    result.setPaging(new Paging(pagingRequest, count));
//    for (DBObject dbObject : cursor) {
//      result.getDocuments().add(convertFromDb(dbObject));
//    }
//    if (_updateLastReadTime) {
//      for (ConfigDocument<T> doc : result.getDocuments()) {
//        DBObject updateQueryObj = new BasicDBObject();
//        updateQueryObj.put(ID_FIELD_NAME, extractRowId(doc.getConfigId()));
//        updateQueryObj.put(LAST_READ_INSTANT_FIELD_NAME, new Date(doc.getLastReadInstant().toEpochMillisLong()));
//        dbCollection.update(updateQueryObj, updateObj).getLastError().throwOnError();
//        doc.setLastReadInstant(now);
//      }
//    }
//    return result;
//  }
//
//  /**
//   * Converts a Mongo DB object to a document.
//   * @param dbObject  the DB object, not null
//   * @return the document, not null
//   */
//  protected ConfigDocument<T> convertFromDb(DBObject dbObject) {
//    s_logger.trace("Config converting dbOject to config doc={}", dbObject);
//    DBObject valueData = (DBObject) dbObject.get(VALUE_FIELD_NAME);
//    FudgeSerializationContext fsc = new FudgeSerializationContext(getFudgeContext());
//    MutableFudgeFieldContainer dbObjecToFudge = fsc.objectToFudgeMsg(valueData);
//    FudgeDeserializationContext fdc = new FudgeDeserializationContext(getFudgeContext());
//    T value = fdc.fudgeMsgToObject(_entityClazz, dbObjecToFudge);
//    ConfigDocument<T> doc = new ConfigDocument<T>();
//    ObjectId id = (ObjectId) dbObject.get("_id");
//    String oid = (String) dbObject.get(OID_FIELD_NAME);
//    doc.setConfigId(UniqueIdentifier.of(getIdentifierScheme(), oid, id.toString()));
//    doc.setVersionNumber((Integer) dbObject.get(VERSION_FIELD_NAME));
//    doc.setName((String) dbObject.get(NAME_FIELD_NAME));
//    Date versionFromTime = (Date) dbObject.get(VERSION_FROM_INSTANT_FIELD_NAME);
//    doc.setVersionFromInstant(Instant.ofEpochMillis(versionFromTime.getTime()));
//    Date versionToTime = (Date) dbObject.get(VERSION_TO_INSTANT_FIELD_NAME);
//    Instant versionToInstant = Instant.ofEpochMillis(versionToTime.getTime());
//    doc.setVersionToInstant(versionToInstant.equals(MAX_INSTANT) ? null : versionToInstant);
//    Date lastRead = (Date) dbObject.get(LAST_READ_INSTANT_FIELD_NAME);
//    doc.setLastReadInstant(Instant.ofEpochMillis(lastRead.getTime()));
//    doc.setValue(value);
//    return doc;
//  }
//
//  //-------------------------------------------------------------------------
//  /**
//   * Converts a document to a Mongo DB object.
//   * @param document  the document, not null
//   * @return the DB object, not null
//   */
//  protected DBObject convertToDb(final ConfigDocument<T> document) {
//    FudgeMsgEnvelope msg = getFudgeContext().toFudgeMsg(document.getValue());
//    DBObject insertDoc = new BasicDBObject();
//    insertDoc.put(ID_FIELD_NAME, extractRowId(document.getConfigId()));
//    insertDoc.put(VERSION_FIELD_NAME, document.getVersionNumber());
//    insertDoc.put(NAME_FIELD_NAME, document.getName());
////    insertDoc.put(VERSION_FROM_INSTANT_FIELD_NAME, new Date(document.getVersionFromInstant().toEpochMillisLong()));
////    if (document.getVersionToInstant() != null) {
////      insertDoc.put(VERSION_TO_INSTANT_FIELD_NAME, new Date(document.getVersionToInstant().toEpochMillisLong()));
////    } else {
////      insertDoc.put(VERSION_TO_INSTANT_FIELD_NAME, new Date(MAX_INSTANT.toEpochMillisLong()));
////    }
//    insertDoc.put(LAST_READ_INSTANT_FIELD_NAME, new Date(document.getVersionFromInstant().toEpochMillisLong()));
//    insertDoc.put(VERSIONS_FIELD_NAME, Collections.singletonList(convertToDbVersion(document)));
//    s_logger.trace("Config converted config doc to object={}", insertDoc);
//    return insertDoc;
//  }
//
//  /**
//   * Converts a document version to a Mongo DB object.
//   * @param document  the document, not null
//   * @param valueId  the if of the inserted value, not null
//   * @return the DB object, not null
//   */
//  protected DBObject convertToDbVersion(final ConfigDocument<T> document, final ObjectId valueId) {
//    FudgeMsgEnvelope msg = getFudgeContext().toFudgeMsg(document.getValue());
//    DBObject obj = new BasicDBObject();
//    obj.put(VERSION_FIELD_NAME, document.getVersionNumber());
//    obj.put(VERSION_FROM_INSTANT_FIELD_NAME, new Date(document.getVersionFromInstant().toEpochMillisLong()));
//    if (document.getVersionToInstant() != null) {
//      obj.put(VERSION_TO_INSTANT_FIELD_NAME, new Date(document.getVersionToInstant().toEpochMillisLong()));
//    } else {
//      obj.put(VERSION_TO_INSTANT_FIELD_NAME, new Date(MAX_INSTANT.toEpochMillisLong()));
//    }
//    obj.put(NAME_FIELD_NAME, document.getName());
//    obj.put(VALUE_FIELD_NAME, valueId);
//    return obj;
//  }
//
//  /**
//   * Inserts the value of the document into the database.
//   * @param document  the document, not null
//   * @return the DB object, not null
//   */
//  protected ObjectId insertValue(final ConfigDocument<T> document) {
//    FudgeMsgEnvelope msg = getFudgeContext().toFudgeMsg(document.getValue());
//    DBObject insertObj = new BasicDBObject();
//    insertObj.put(VALUE_FIELD_NAME, getFudgeContext().fromFudgeMsg(DBObject.class, msg.getMessage()));
//    s_logger.trace("Config converted config doc to object={}", insertObj);
//    createDBValueCollection().insert(insertObj).getLastError().throwOnError();
//    return (ObjectId) insertObj.get(ID_FIELD_NAME);
//  }
//
//  //-------------------------------------------------------------------------
//  /**
//   * Returns a string summary of this configuration master.
//   * @return the string summary, not null
//   */
//  @Override
//  public String toString() {
//    return getClass().getSimpleName() + "[" + getIdentifierScheme() + "]";
//  }

}
