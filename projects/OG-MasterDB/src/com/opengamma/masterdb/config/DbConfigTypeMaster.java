/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import java.util.concurrent.CopyOnWriteArraySet;

import javax.time.TimeSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.MasterChangeListener;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.config.ConfigTypeMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbSource;

/**
 * A config master implementation using a database for persistence.
 * <p>
 * Full details of the API are in {@link ConfigTypeMaster}.
 * This class uses JDBC to store the data via a set of workers.
 * The workers may be replaced by configuration to allow different SQL on different databases.
 * 
 * @param <T>  the configuration element type
 */
public class DbConfigTypeMaster<T> implements ConfigTypeMaster<T> {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbConfigTypeMaster.class);

  private final CopyOnWriteArraySet<MasterChangeListener> _listeners = new CopyOnWriteArraySet<MasterChangeListener>();
  
  /**
   * The scheme used for UniqueIdentifier objects.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbCfg";

  /**
   * The class of the configuration.
   */
  private final Class<T> _clazz;

  /**
   * The database source.
   */
  private final DbSource _dbSource;
  /**
   * The time-source to use.
   */
  private TimeSource _timeSource = TimeSource.system();
  /**
   * The scheme in use for UniqueIdentifier.
   */
  private String _identifierScheme = IDENTIFIER_SCHEME_DEFAULT;
  /**
   * The workers.
   */
  private DbConfigTypeMasterWorkers<T> _workers;

  /**
   * Creates an instance.
   * @param clazz  the class of the configuration, not null
   * @param dbSource  the database source combining all configuration, not null
   */
  public DbConfigTypeMaster(final Class<T> clazz, final DbSource dbSource) {
    ArgumentChecker.notNull(clazz, "clazz");
    ArgumentChecker.notNull(dbSource, "dbSource");
    s_logger.debug("installed DbSource: {}", dbSource);
    _clazz = clazz;
    _dbSource = dbSource;
    setWorkers(new DbConfigTypeMasterWorkers<T>());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the reified generic type.
   * @return the type, non-null
   */
  public Class<T> getReifiedType() {
    return _clazz;
  }

  /**
   * Gets the database source.
   * @return the database source, non-null
   */
  public DbSource getDbSource() {
    return _dbSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the configured set of workers.
   * @return the configured workers, not null
   */
  public DbConfigTypeMasterWorkers<T> getWorkers() {
    return _workers;
  }

  /**
   * Sets the configured workers to use.
   * The workers will be {@link DbConfigTypeMasterWorkers#init initialized} as part of this method call.
   * @param workers  the configured workers, not null
   */
  public void setWorkers(final DbConfigTypeMasterWorkers<T> workers) {
    ArgumentChecker.notNull(workers, "workers");
    workers.init(this);
    s_logger.debug("installed DbPositionMasterWorkers: {}", workers);
    _workers = workers;
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
    
    return getWorkers().getSearchWorker().search(request);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument<T> get(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    checkScheme(uid);
    
    return getWorkers().getGetWorker().get(uid);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument<T> add(final ConfigDocument<T> document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getName(), "document.name");
    ArgumentChecker.notNull(document.getValue(), "document.value");
    
    ConfigDocument<T> added = getWorkers().getAddWorker().add(document);
    
    //notify listeners
    notifyDocumentAdded(added);
    return added;
  }

  private void notifyDocumentAdded(ConfigDocument<T> added) {
    for (MasterChangeListener listener : _listeners) {
      listener.added(added.getUniqueId());
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument<T> update(final ConfigDocument<T> document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getName(), "document.name");
    ArgumentChecker.notNull(document.getValue(), "document.value");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    checkScheme(document.getUniqueId());
    
    ConfigDocument<T> updated = getWorkers().getUpdateWorker().update(document);
    notifyDocumentUpdated(document.getUniqueId(), updated.getUniqueId());
    return updated;
  }

  private void notifyDocumentUpdated(final UniqueIdentifier oldItem, final UniqueIdentifier newItem) {
    for (MasterChangeListener listener : _listeners) {
      listener.updated(oldItem, newItem);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    checkScheme(uid);
    
    getWorkers().getRemoveWorker().remove(uid);
    notifyDocumentRemoved(uid);
  }

  private void notifyDocumentRemoved(final UniqueIdentifier removedItem) {
    for (MasterChangeListener listener : _listeners) {
      listener.removed(removedItem);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigHistoryResult<T> history(final ConfigHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");
    checkScheme(request.getObjectId());
    
    return getWorkers().getHistoryWorker().history(request);
  }

  //-------------------------------------------------------------------------
  @Override
  public ConfigDocument<T> correct(final ConfigDocument<T> document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getName(), "document.name");
    ArgumentChecker.notNull(document.getValue(), "document.value");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    checkScheme(document.getUniqueId());
    
    ConfigDocument<T> corrected = getWorkers().getCorrectWorker().correct(document);
    notifyDocumentCorrected(document.getUniqueId(), corrected.getUniqueId());
    return corrected;
  }

  private void notifyDocumentCorrected(final UniqueIdentifier oldItem, UniqueIdentifier newItem) {
    for (MasterChangeListener listener : _listeners) {
      listener.corrected(oldItem, newItem);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string summary of this config master.
   * @return the string summary, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getIdentifierScheme() + "]";
  }

  @Override
  public void addChangeListener(MasterChangeListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    _listeners.add(listener);
  }

  @Override
  public void removeChangeListener(MasterChangeListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    _listeners.remove(listener);
  }

}
