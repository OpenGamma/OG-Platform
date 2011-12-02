/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import javax.time.TimeSource;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMetaDataRequest;
import com.opengamma.master.config.ConfigMetaDataResult;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.masterdb.AbstractDbMaster;
import com.opengamma.util.db.DbConnector;

/**
 * A config master implementation using a database for persistence.
 * <p>
 * This is a full implementation of the config master using an SQL database.
 * Full details of the API are in {@link ConfigMaster}.
 * <p>
 * The SQL is stored externally in {@code DbConfigMaster.extsql}.
 * Alternate databases or specific SQL requirements can be handled using database
 * specific overrides, such as {@code DbConfigMaster-MySpecialDB.extsql}.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 * 
 */
public class DbConfigMaster extends AbstractDbMaster implements ConfigMaster {

  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbCfg";

  /**
   * The change manager.
   */
  private ChangeManager _changeManager = new BasicChangeManager();
  /**
   * The worker.
   */
  private DbConfigWorker _worker;

  /**
   * Creates an instance.
   * 
   * @param dbConnector  the database connector, not null
   */
  public DbConfigMaster(DbConnector dbConnector) {
    super(dbConnector, IDENTIFIER_SCHEME_DEFAULT);
    _worker = new DbConfigWorker(dbConnector, IDENTIFIER_SCHEME_DEFAULT);
    _worker.setChangeManager(_changeManager);
  }

  //-------------------------------------------------------------------------
  /**
   * Sets the time-source that determines the current time.
   * 
   * @param timeSource  the time-source, not null
   */
  public void setTimeSource(final TimeSource timeSource) {
    super.setTimeSource(timeSource);
    _worker.setTimeSource(timeSource);
  }

  /**
   * Sets the scheme in use for unique identifier.
   * 
   * @param scheme  the scheme for unique identifier, not null
   */
  public void setUniqueIdScheme(final String scheme) {
    super.setUniqueIdScheme(scheme);
    _worker.setUniqueIdScheme(scheme);
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

  @Override
  public ConfigDocument<?> get(UniqueId uniqueId) {
    return _worker.get(uniqueId);
  }

  @Override
  public ConfigDocument<?> get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    return _worker.get(objectId, versionCorrection);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigDocument<T> get(UniqueId uniqueId, Class<T> clazz) {
    ConfigDocument<?> document = _worker.get(uniqueId);
    if (!clazz.isInstance(document.getValue())) {
      throw new DataNotFoundException("Config not found: " + uniqueId.getObjectId());
    }
    return (ConfigDocument<T>) document;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigDocument<T> get(ObjectIdentifiable objectId, VersionCorrection versionCorrection, Class<T> clazz) {
    ConfigDocument<?> document = _worker.get(objectId, versionCorrection);
    if (!clazz.isInstance(document.getValue())) {
      throw new DataNotFoundException("Config not found: " + objectId);
    }
    return (ConfigDocument<T>) document;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigDocument<T> add(ConfigDocument<T> document) {
    ConfigDocument<?> added = _worker.add(document);
    return (ConfigDocument<T>) added;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigDocument<T> update(ConfigDocument<T> document) {
    ConfigDocument<?> updated = _worker.update(document);
    return (ConfigDocument<T>) updated;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigDocument<T> correct(ConfigDocument<T> document) {
    ConfigDocument<?> corrected = _worker.correct(document);
    return (ConfigDocument<T>) corrected;
  }

  @Override
  public ConfigMetaDataResult metaData(ConfigMetaDataRequest request) {
    return _worker.metaData(request);
  }

  @Override
  public <T> ConfigSearchResult<T> search(ConfigSearchRequest<T> request) {
    return _worker.search(request);
  }

  @Override
  public <T> ConfigHistoryResult<T> history(ConfigHistoryRequest<T> request) {
    return _worker.history(request);
  }

  @Override
  public void remove(UniqueId uniqueId) {
    _worker.remove(uniqueId);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getUniqueIdScheme() + "]";
  }

}
