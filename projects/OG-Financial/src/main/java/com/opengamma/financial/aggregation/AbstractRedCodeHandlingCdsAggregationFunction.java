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
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.cds.AbstractCreditDefaultSwapSecurity;

/**
 * Abstract aggregation function for CDS reference entity data. If used with
 * non-CDS securities, all items will be classified as "N/A".
 *
 * @param <T> The type of data that this implementation will extract
 */
public abstract class AbstractRedCodeHandlingCdsAggregationFunction<T> extends AbstractCdsAggregationFunction<T> {

  /**
   * Creates the aggregation function.
   *
   * @param name the name to be used for this aggregation, not null
   * @param securitySource the security source used for resolution of the CDS security, not null
   * @param redCodeHandler the extractor which will process the red code and return the required type, not null
   */
  public AbstractRedCodeHandlingCdsAggregationFunction(String name, SecuritySource securitySource, final RedCodeHandler<T> redCodeHandler) {
    super(name, securitySource, new CdsValueExtractor<T>() {
      private final CdsRedCodeExtractor<T> _redCodeExtractor = new CdsRedCodeExtractor<>(redCodeHandler);

      @Override
      public T extract(AbstractCreditDefaultSwapSecurity cds) {
        return _redCodeExtractor.extract(cds);
      }
    });
  }

  //-------------------------------------------------------------------------
  @Override
  public Collection<String> getRequiredEntries() {
    return ImmutableList.of();
  }

  @Override
  public Comparator<Position> getPositionComparator() {
    return new SimplePositionComparator();
  }

  @Override
  public int compare(String sector1, String sector2) {
    return sector1.compareTo(sector2);
  }

}
