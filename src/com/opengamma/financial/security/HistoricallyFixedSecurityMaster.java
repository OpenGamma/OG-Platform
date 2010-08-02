/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.Collection;
import java.util.List;

import javax.time.Instant;
import javax.time.InstantProvider;

import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * This SecuritySource retrieves all securities as of a fixed historical date from an underlying SecurityMaster.
 */
public class HistoricallyFixedSecurityMaster implements SecuritySource {
  
  private final SecurityMaster _delegate;
  private final Instant _fixInstant;
  private final Instant _correctionInstant;

  public HistoricallyFixedSecurityMaster(final SecurityMaster delegate, final InstantProvider fixInstant) {
    this(delegate, fixInstant, null);
  }

  /**
   * 
   * @param delegate delegate security master, not {@code null}
   * @param fixInstant historical instant to retrieve data from, not {@code null}
   * @param correctionInstant instant to correct to, or {@code null} or most recent corrected data
   */
  public HistoricallyFixedSecurityMaster(final SecurityMaster delegate, final InstantProvider fixInstant, final InstantProvider correctionInstant) {
    ArgumentChecker.notNull(delegate, "Delegate security master");
    ArgumentChecker.notNull(fixInstant, "Fix instant");
    _delegate = delegate;
    _fixInstant = fixInstant.toInstant();
    _correctionInstant = (correctionInstant != null) ? correctionInstant.toInstant() : null;
  }

  protected SecurityMaster getDelegate() {
    return _delegate;
  }

  protected Instant getFixInstant() {
    return _fixInstant;
  }

  protected Instant getCorrectionInstant() {
    return _correctionInstant;
  }

  protected SecuritySearchHistoricRequest createRequest() {
    final SecuritySearchHistoricRequest request = new SecuritySearchHistoricRequest();
    request.setCorrectedToInstant(getCorrectionInstant());
    request.setVersionFromInstant(getFixInstant());
    request.setVersionToInstant(getFixInstant());
    return request;
  }

  protected Security returnOne(final SecuritySearchHistoricRequest request) {
    final SecuritySearchHistoricResult result = getDelegate().searchHistoric(request);
    final List<Security> securities = result.getSecurities();
    if (securities.size() > 0) {
      return securities.get(0);
    } else {
      return null;
    }
  }

  protected Collection<Security> returnAll(final SecuritySearchHistoricRequest request) {
    final SecuritySearchHistoricResult result = getDelegate().searchHistoric(request);
    return result.getSecurities();
  }

  @Override
  public Security getSecurity(UniqueIdentifier uid) {
    final SecuritySearchHistoricRequest request = createRequest();
    request.setObjectIdentifier(uid);
    return returnOne(request);
  }

  @Override
  public Collection<Security> getSecurities(IdentifierBundle secKey) {
    final SecuritySearchHistoricRequest request = createRequest();
    // TODO: populate search parameters for the bundle
    return returnAll(request);
  }

  @Override
  public Security getSecurity(IdentifierBundle secKey) {
    final SecuritySearchHistoricRequest request = createRequest();
    // TODO: populate search parameters for the bundle
    return returnOne(request);
  }

}
