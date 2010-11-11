/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange.impl;

import javax.time.Instant;
import javax.time.InstantProvider;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.PagingRequest;

/**
 * An {@code ExchangeSource} implemented using an underlying {@code ExchangeMaster}.
 * <p>
 * The {@link ExchangeSource} interface provides exchanges to the application via a narrow API.
 * This class provides the source on top of a standard {@link ExchangeMaster}.
 */
public class MasterExchangeSource implements ExchangeSource {

  /**
   * The exchange master.
   */
  private final ExchangeMaster _exchangeMaster;
  /**
   * The instant to search for a version at.
   * Null is treated as the latest version.
   */
  private final Instant _versionAsOfInstant;
  /**
   * The instant to search for corrections for.
   * Null is treated as the latest correction.
   */
  private final Instant _correctedToInstant;

  /**
   * Creates an instance with an underlying exchange master.
   * @param exchangeMaster  the exchange master, not null
   */
  public MasterExchangeSource(final ExchangeMaster exchangeMaster) {
    this(exchangeMaster, null, null);
  }

  /**
   * Creates an instance with an underlying exchange master viewing the version
   * that existed on the specified instant.
   * @param exchangeMaster  the exchange master, not null
   * @param versionAsOfInstantProvider  the version instant to retrieve, null for latest version
   */
  public MasterExchangeSource(final ExchangeMaster exchangeMaster, InstantProvider versionAsOfInstantProvider) {
    this(exchangeMaster, versionAsOfInstantProvider, null);
  }

  /**
   * Creates an instance with an underlying exchange master viewing the version
   * that existed on the specified instant as corrected to the correction instant.
   * @param exchangeMaster  the exchange master, not null
   * @param versionAsOfInstantProvider  the version instant to retrieve, null for latest version
   * @param correctedToInstantProvider  the instant that the data should be corrected to, null for latest correction
   */
  public MasterExchangeSource(final ExchangeMaster exchangeMaster, InstantProvider versionAsOfInstantProvider, InstantProvider correctedToInstantProvider) {
    ArgumentChecker.notNull(exchangeMaster, "exchangeMaster");
    _exchangeMaster = exchangeMaster;
    if (versionAsOfInstantProvider != null) {
      _versionAsOfInstant = Instant.of(versionAsOfInstantProvider);
    } else {
      _versionAsOfInstant = null;
    }
    if (correctedToInstantProvider != null) {
      _correctedToInstant = Instant.of(correctedToInstantProvider);
    } else {
      _correctedToInstant = null;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying exchange master.
   * 
   * @return the exchange master, not null
   */
  public ExchangeMaster getExchangeMaster() {
    return _exchangeMaster;
  }

  /**
   * Gets the version instant to retrieve.
   * 
   * @return the version instant to retrieve, null for latest version
   */
  public Instant getVersionAsOfInstant() {
    return _versionAsOfInstant;
  }

  /**
   * Gets the instant that the data should be corrected to.
   * 
   * @return the instant that the data should be corrected to, null for latest correction
   */
  public Instant getCorrectedToInstant() {
    return _correctedToInstant;
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableExchange getExchange(UniqueIdentifier uid) {
    try {
      return getExchangeMaster().get(uid).getExchange();
    } catch (DataNotFoundException ex) {
      return null;
    }
  }

  @Override
  public ManageableExchange getSingleExchange(Identifier identifier) {
    ExchangeSearchRequest searchRequest = new ExchangeSearchRequest(identifier);
    searchRequest.setPagingRequest(PagingRequest.ONE);
    searchRequest.setVersionAsOfInstant(_versionAsOfInstant);
    searchRequest.setCorrectedToInstant(_correctedToInstant);
    return getExchangeMaster().search(searchRequest).getFirstExchange();
  }

  @Override
  public ManageableExchange getSingleExchange(IdentifierBundle identifiers) {
    ExchangeSearchRequest searchRequest = new ExchangeSearchRequest(identifiers);
    searchRequest.setPagingRequest(PagingRequest.ONE);
    searchRequest.setVersionAsOfInstant(_versionAsOfInstant);
    searchRequest.setCorrectedToInstant(_correctedToInstant);
    return getExchangeMaster().search(searchRequest).getFirstExchange();
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    String str = "MasterExchangeSource[" + getExchangeMaster();
    if (_versionAsOfInstant != null) {
      str += ",versionAsOf=" + _versionAsOfInstant;
    }
    if (_versionAsOfInstant != null) {
      str += ",correctedTo=" + _correctedToInstant;
    }
    return str + "]";
  }

}
