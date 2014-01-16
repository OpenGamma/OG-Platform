/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.bean;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.joda.beans.Bean;
import org.threeten.bp.Clock;

import com.codahale.metrics.MetricRegistry;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.elsql.ElSqlBundle;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractChangeProvidingMaster;
import com.opengamma.master.AbstractDocument;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.metric.MetricProducer;

/**
 * Abstract class for master implementations that delegate to {@code DbBeanMaster}.
 * <p>
 * Data is stored based on the Joda-Beans API.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 * 
 * @param <D>  the document type
 * @param <V>  the bean type
 */
public class AbstractDelegatingBeanMaster<D extends AbstractDocument, V extends Bean> 
    implements AbstractChangeProvidingMaster<D>, MetricProducer {

  /**
   * The delegate master.
   */
  private final DbBeanMaster<D, V> _delegate;

  /**
   * Creates an instance.
   * 
   * @param delegate  the delegate master, not null
   */
  public AbstractDelegatingBeanMaster(final DbBeanMaster<D, V> delegate) {
    _delegate = delegate;
  }

  @Override
  public void registerMetrics(MetricRegistry summaryRegistry, MetricRegistry detailedRegistry, String namePrefix) {
    _delegate.registerMetrics(summaryRegistry, detailedRegistry, namePrefix);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the delegated {@code DbBeanMaster}.
   * 
   * @return the delegated master, not null
   */
  protected DbBeanMaster<D, V> getDelegate() {
    return _delegate;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the maximum number of retries.
   * The default is ten.
   *
   * @return the maximum number of retries, not null
   */
  public int getMaxRetries() {
    return _delegate.getMaxRetries();
  }

  /**
   * Sets the maximum number of retries.
   * The default is ten.
   *
   * @param maxRetries  the maximum number of retries, not negative
   */
  public void setMaxRetries(final int maxRetries) {
    _delegate.setMaxRetries(maxRetries);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the database connector.
   * 
   * @return the database connector, not null
   */
  public DbConnector getDbConnector() {
    return _delegate.getDbConnector();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the external SQL bundle.
   * 
   * @return the external SQL bundle, not null
   */
  public ElSqlBundle getElSqlBundle() {
    return _delegate.getElSqlBundle();
  }

  /**
   * Sets the external SQL bundle.
   * 
   * @param bundle  the external SQL bundle, not null
   */
  public void setElSqlBundle(ElSqlBundle bundle) {
    _delegate.setElSqlBundle(bundle);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the clock that determines the current time.
   * 
   * @return the clock, not null
   */
  public Clock getClock() {
    return _delegate.getClock();
  }

  /**
   * Sets the clock that determines the current time.
   * 
   * @param clock  the clock, not null
   */
  public void setClock(final Clock clock) {
    _delegate.setClock(clock);
  }

  /**
   * Resets the clock that determines the current time to the default.
   */
  public void resetClock() {
    _delegate.resetClock();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the scheme in use for unique identifier.
   * 
   * @return the scheme, not null
   */
  public String getUniqueIdScheme() {
    return _delegate.getUniqueIdScheme();
  }

  /**
   * Sets the scheme in use for unique identifier.
   * 
   * @param scheme  the scheme for unique identifier, not null
   */
  public void setUniqueIdScheme(final String scheme) {
    _delegate.setUniqueIdScheme(scheme);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the change manager.
   *
   * @return the change manager, not null
   */
  public ChangeManager getChangeManager() {
    return _delegate.getChangeManager();
  }

  /**
   * Sets the change manager.
   *
   * @param changeManager  the change manager, not null
   */
  public void setChangeManager(final ChangeManager changeManager) {
    _delegate.setChangeManager(changeManager);
  }

  @Override
  public ChangeManager changeManager() {
    return _delegate.changeManager();
  }

  //-------------------------------------------------------------------------
  @Override
  public D get(UniqueId uniqueId) {
    return _delegate.get(uniqueId);
  }

  @Override
  public D get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    return _delegate.get(objectId, versionCorrection);
  }

  @Override
  public Map<UniqueId, D> get(Collection<UniqueId> uniqueIds) {
    return _delegate.get(uniqueIds);
  }

  @Override
  public D add(D document) {
    return _delegate.add(document);
  }

  @Override
  public D update(D document) {
    return _delegate.update(document);
  }

  @Override
  public void remove(ObjectIdentifiable oid) {
    _delegate.remove(oid);
  }

  @Override
  public D correct(D document) {
    return _delegate.correct(document);
  }

  @Override
  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<D> replacementDocuments) {
    return _delegate.replaceVersion(uniqueId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<D> replacementDocuments) {
    return _delegate.replaceAllVersions(objectId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<D> replacementDocuments) {
    return _delegate.replaceVersions(objectId, replacementDocuments);
  }

  @Override
  public UniqueId replaceVersion(D replacementDocument) {
    return _delegate.replaceVersion(replacementDocument);
  }

  @Override
  public void removeVersion(UniqueId uniqueId) {
    _delegate.removeVersion(uniqueId);
  }

  @Override
  public UniqueId addVersion(ObjectIdentifiable objectId, D documentToAdd) {
    return _delegate.addVersion(objectId, documentToAdd);
  }

  //-------------------------------------------------------------------------
  /**
   * Retrieves the version of the master schema from the database.
   *  
   * @return the schema version, or null if not found
   */
  public Integer getSchemaVersion() {
    return _delegate.getSchemaVersion();
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a string summary of this master.
   * 
   * @return the string summary, not null
   */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getUniqueIdScheme() + "]";
  }

}
