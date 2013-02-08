/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Function to classify positions by Currency.
 *
 */
public class EquityBetaAggregationFunction implements AggregationFunction<String> {
  private static final Logger s_logger = LoggerFactory.getLogger(EquityBetaAggregationFunction.class);

  private final boolean _useAttributes;
  private final boolean _includeEmptyCategories;

  private static final String MORE_THAN_1_25 = "> 1.25";
  private static final String FROM_0_9_TO_1_25 = "0.9 - 1.25";
  private static final String FROM_0_75_TO_0_9 = "0.75 - 0.9";
  private static final String FROM_0_5_TO_0_75 = "0.5 - 0.75";
  private static final String LESS_THAN_0_5 = "< 0.5";
  private static final String NAME = "Beta";
  private static final String FIELD = "APPLIED_BETA";
  private static final String RESOLUTION_KEY = "DEFAULT_TSS_CONFIG";
  private static final String NO_BETA = "N/A";

  private final boolean _caching = true;

  private final Map<UniqueId, Double> _equityBetaCache = new HashMap<UniqueId, Double>();

  private static final List<String> REQUIRED = Arrays.asList(MORE_THAN_1_25, FROM_0_9_TO_1_25, FROM_0_75_TO_0_9, FROM_0_5_TO_0_75, LESS_THAN_0_5, NO_BETA);

  private final HistoricalTimeSeriesSource _htsSource;
  private final SecuritySource _secSource;

  public EquityBetaAggregationFunction(final SecuritySource secSource, final HistoricalTimeSeriesSource htsSource, final boolean useAttributes, final boolean includeEmptyCategories) {
    _secSource = secSource;
    _htsSource = htsSource;
    _useAttributes = useAttributes;
    _includeEmptyCategories = includeEmptyCategories;
  }

  public EquityBetaAggregationFunction(final SecuritySource secSource, final HistoricalTimeSeriesSource htsSource, final boolean useAttributes) {
    this(secSource, htsSource, useAttributes, false);
  }

  public EquityBetaAggregationFunction(final SecuritySource secSource, final HistoricalTimeSeriesSource htsSource) {
    this(secSource, htsSource, false);
  }

  @Override
  public String classifyPosition(final Position position) {
    if (_useAttributes) {
      final Map<String, String> attributes = position.getAttributes();
      if (attributes.containsKey(getName())) {
        return attributes.get(getName());
      }
      return NO_BETA;
    }
    return classifyPositionWithTS(position);
  }

  /*package*/ Double getEquityBeta(final Security security) {
    if (_caching && security != null && security.getUniqueId() != null) {
      if (_equityBetaCache.containsKey(security.getUniqueId())) {
        return _equityBetaCache.get(security.getUniqueId());
      }
    }
    if (security == null) {
      return null;
    }
    final ExternalIdBundle externalIdBundle = security.getExternalIdBundle();
    final Pair<LocalDate, Double> results = _htsSource.getLatestDataPoint(FIELD, externalIdBundle, RESOLUTION_KEY);
    if (results != null && results.getFirst() != null && results.getSecond() != null) {
      final Double beta = results.getSecond();
      return beta;
    }
    return null;
  }

  /*package*/ String classifyPositionWithTS(final Position position) {
    final Security sec = resolveSecurity(position);
    final Double beta = getEquityBeta(sec);
    return classifyEquityBeta(beta);
  }

  /*package*/ Security resolveSecurity(final Position position) {
    try {
      Security sec = position.getSecurityLink().getTarget();
      if (sec == null) {
        sec = position.getSecurityLink().resolve(_secSource);
        if (sec == null) {
          s_logger.error("Position security is null");
          return null;
        }
      }
      if (sec.getSecurityType().equals(EquityOptionSecurity.SECURITY_TYPE)) {
        final EquityOptionSecurity equityOption = (EquityOptionSecurity) sec;
        sec = equityOption;
      }
      return sec;
    } catch (final UnsupportedOperationException ex) {
      return null;
    }
  }

  /*package*/ String classifyEquityBeta(final Double beta) {
    if (beta != null) {
      if (beta < 0.5) {
        return LESS_THAN_0_5;
      } else if (beta < 0.75) {
        return FROM_0_5_TO_0_75;
      } else if (beta < 0.9) {
        return FROM_0_75_TO_0_9;
      } else if (beta < 1.25) {
        return FROM_0_9_TO_1_25;
      } else {
        return MORE_THAN_1_25;
      }
    }
    return NO_BETA;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Collection<String> getRequiredEntries() {
    if (_includeEmptyCategories) {
      return REQUIRED;
    }
    return Collections.emptyList();
  }

  @Override
  public int compare(final String o1, final String o2) {
    return CompareUtils.compareByList(REQUIRED, o1, o2);
  }

  private class PositionComparator implements Comparator<Position> {

    @Override
    public int compare(final Position position1, final Position position2) {
      final Security security1 = resolveSecurity(position1);
      final Security security2 = resolveSecurity(position2);
      return CompareUtils.compareWithNullLow(getEquityBeta(security1), getEquityBeta(security2));
    }

  }

  @Override
  public Comparator<Position> getPositionComparator() {
    return new PositionComparator();
  }
}
