/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import javax.time.TimeSource;

import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.listener.BasicMasterChangeManager;
import com.opengamma.master.listener.MasterChangeManager;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbSource;
import com.opengamma.util.fudge.OpenGammaFudgeContext;

/**
 * A config master implementation using a database for persistence.
 * <p>
 * This is a full implementation of the config master using an SQL database.
 * Full details of the API are in {@link ConfigMaster}.
 * <p>
 * This class uses SQL via JDBC. The SQL may be changed by subclassing the relevant methods.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 * 
 */
public class DbConfigMaster implements ConfigMaster {
  
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbConfigMaster.class);

  /**
   * The scheme used for UniqueIdentifier objects.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbCfg";
  /**
   * The Fudge context.
   */
  protected static final FudgeContext FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();
  
  /**
   * The change manager.
   */
  private MasterChangeManager _changeManager = new BasicMasterChangeManager();
  
  private DbConfigWorker _worker;

  /**
   * Creates an instance.
   * 
   * @param dbSource  the database source combining all configuration, not null
   */
  public DbConfigMaster(DbSource dbSource) {
    _worker = new DbConfigWorker(dbSource, IDENTIFIER_SCHEME_DEFAULT);
    _worker.setChangeManager(_changeManager);
  }
  
  /**
   * Sets the time-source that determines the current time.
   * 
   * @param timeSource  the time-source, not null
   */
  public void setTimeSource(final TimeSource timeSource) {
    ArgumentChecker.notNull(timeSource, "timeSource");
    s_logger.debug("installed TimeSource: {}", timeSource);
    _worker.setTimeSource(timeSource);
  }
  
  //-------------------------------------------------------------------------
  /**
   * Gets the time-source that determines the current time.
   * 
   * @return the time-source, not null
   */
  public TimeSource getTimeSource() {
    return _worker.getTimeSource();
  }
  
  //-------------------------------------------------------------------------
  /**
   * Gets the scheme in use for unique identifier.
   * 
   * @return the scheme, not null
   */
  public String getIdentifierScheme() {
    return _worker.getIdentifierScheme();
  }
  
  /**
   * Sets the scheme in use for unique identifier.
   * 
   * @param scheme  the scheme for unique identifier, not null
   */
  public void setIdentifierScheme(final String scheme) {
    ArgumentChecker.notNull(scheme, "scheme");
    s_logger.debug("installed IdentifierScheme: {}", scheme);
    _worker.setIdentifierScheme(scheme);
  }
  
  //-------------------------------------------------------------------------
  /**
   * Gets the database source.
   * 
   * @return the database source, not null
   */
  public DbSource getDbSource() {
    return _worker.getDbSource();
  }

  @Override
  public MasterChangeManager changeManager() {
    return _changeManager;
  }
  
  @Override
  public ConfigDocument<?> get(UniqueIdentifier uniqueId) {
    return _worker.get(uniqueId);
  }

  @Override
  public ConfigDocument<?> get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    return _worker.get(objectId, versionCorrection);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> ConfigDocument<T> get(UniqueIdentifier uniqueId, Class<T> clazz) {
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
  public <T> ConfigSearchResult<T> search(ConfigSearchRequest<T> request) {
    return _worker.search(request);
  }

  @Override
  public <T> ConfigHistoryResult<T> history(ConfigHistoryRequest<T> request) {
    return _worker.history(request);
  }

  @Override
  public void remove(UniqueIdentifier uniqueId) {
    _worker.remove(uniqueId);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getIdentifierScheme() + "]";
  }
   
}
