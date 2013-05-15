/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Collection;
import java.util.Comparator;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePositionComparator;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.cds.AbstractCreditDefaultSwapSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract aggregation function for CDS reference entity data. If used with
 * non-CDS securities, all items will be classified as "N/A".
 *
 * @param <T> The type of data that this implementation will extract
 */
public abstract class AbstractCdsAggregationFunction<T> implements AggregationFunction<String> {

  /**
   * Classification indicating that this aggregation does not apply to the security.
   */
  private static final String NOT_APPLICABLE = "N/A";

  /**
   * The name to be used for this aggregation, not null.
   */
  private final String _name;
  /**
   * The security source used for resolution of the CDS security, not null.
   */
  private final SecuritySource _securitySource;
  /**
   * The extractor which will process the red code and return the required type, not null.
   */
  private final CdsValueExtractor<T> _extractor;

  /**
   * Creates the aggregation function.
   *
   * @param name  the name to be used for this aggregation, not null
   * @param securitySource  the security source used for resolution of the CDS security, not null
   * @param extractor  the extractor which will process the cds and return the required type, not null
   */
  public AbstractCdsAggregationFunction(String name, SecuritySource securitySource, CdsValueExtractor<T> extractor) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(securitySource, "securitySource");
    _name = name;
    _securitySource = securitySource;
    _extractor = extractor;
  }

  //-------------------------------------------------------------------------
  @Override
  public Collection<String> getRequiredEntries() {
    return ImmutableList.of();
  }

  @Override
  public String classifyPosition(Position position) {
    Security security = resolveSecurity(position);
    if (security instanceof AbstractCreditDefaultSwapSecurity) {
      AbstractCreditDefaultSwapSecurity cds = (AbstractCreditDefaultSwapSecurity) security;
      T extracted = _extractor.extract(cds);
      if (extracted != null) {
        return handleExtractedData(extracted);
      } else {
        return NOT_APPLICABLE;
      }
    }
    return NOT_APPLICABLE;
  }

  /**
   * Handle the data which has been returned from the {@link RedCodeHandler} instance.
   *
   * @param extracted the data extracted by the handler
   * @return the string which should be used as the classifier value
   */
  protected abstract String handleExtractedData(T extracted);

  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  private Security resolveSecurity(Position position) {
    Security security = position.getSecurityLink().getTarget();
    return security != null ? security : position.getSecurityLink().resolveQuiet(_securitySource);
  }

  @Override
  public Comparator<Position> getPositionComparator() {
    return new SimplePositionComparator();
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public int compare(String sector1, String sector2) {
    return sector1.compareTo(sector2);
  }

}
