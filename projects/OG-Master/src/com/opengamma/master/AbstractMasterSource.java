/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * An {@code ExchangeSource} implemented using an underlying {@code ExchangeMaster}.
 * <p>
 * The {@link ExchangeSource} interface provides exchanges to the application via a narrow API.
 * This class provides the source on top of a standard {@link ExchangeMaster}.
 * 
 * @param <D>  the type of the document
 * @param <M>  the type of the master
 */
public class AbstractMasterSource<D extends AbstractDocument, M extends AbstractMaster<D>> implements VersionedSource {

  /**
   * The master.
   */
  private final M _master;
  /**
   * The version-correction locator to search at, null to not override versions.
   */
  private volatile VersionCorrection _versionCorrection;

  /**
   * Creates an instance with an underlying master which does not override versions.
   * 
   * @param master  the master, not null
   */
  public AbstractMasterSource(final M master) {
    this(master, null);
  }

  /**
   * Creates an instance with an underlying master optionally overriding the requested version.
   * 
   * @param master  the master, not null
   * @param versionCorrection  the version-correction locator to search at, null to not override versions
   */
  public AbstractMasterSource(final M master, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(master, "master");
    _master = master;
    _versionCorrection = versionCorrection;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying master.
   * 
   * @return the master, not null
   */
  public M getMaster() {
    return _master;
  }

  /**
   * Gets the version-correction locator to search at.
   * 
   * @return the version-correction locator to search at, null if not overriding versions
   */
  public VersionCorrection getVersionCorrection() {
    return _versionCorrection;
  }

  /**
   * Sets the version-correction locator to search at.
   * 
   * @param versionCorrection  the version-correction locator to search at, null to not override versions
   */
  @Override
  public void setVersionCorrection(final VersionCorrection versionCorrection) {
    _versionCorrection = versionCorrection;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a document from the master by unique identifier.
   * <p>
   * This overrides the version in the unique identifier if set to do so.
   * 
   * @param uniqueId  the unique identifier
   * @return the document, null if not found
   */
  public D getDocument(UniqueIdentifier uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final VersionCorrection vc = getVersionCorrection();  // lock against change
    try {
      if (vc != null) {
        return getMaster().get(uniqueId, vc);
      } else {
        return getMaster().get(uniqueId);
      }
    } catch (DataNotFoundException ex) {
      return null;
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    String str = getClass().getSimpleName() + "[" + getMaster();
    if (getVersionCorrection() != null) {
      str += ",versionCorrection=" + getVersionCorrection();
    }
    return str + "]";
  }

}
