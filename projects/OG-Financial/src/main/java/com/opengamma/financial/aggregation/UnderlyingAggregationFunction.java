/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePositionComparator;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.id.ExternalScheme;

/**
 * Aggregation function for bucketing securities by their underlying hedging instrument.
 * Originally, this was used to bucket equity options by GICS code of the underlying equity, but has since been expanded.
 */
public class UnderlyingAggregationFunction implements AggregationFunction<String> {

  private final boolean _useAttributes;
  private final SecuritySource _secSource;
  private final Comparator<Position> _comparator = new SimplePositionComparator();
  private final FinancialSecurityVisitor<String> _underlyingVisitor;

  private static final String NOT_APPLICABLE = "N/A";

  /* to make dep injection easier */
  public UnderlyingAggregationFunction(SecuritySource secSource, String preferredSchemeString) {
    this(secSource, ExternalScheme.of(preferredSchemeString));
  }
  
  public UnderlyingAggregationFunction(SecuritySource secSource, String preferredSchemeString, boolean useAttributes) {
    this(secSource, ExternalScheme.of(preferredSchemeString), useAttributes);
  }
  
  public UnderlyingAggregationFunction(SecuritySource secSource, ExternalScheme preferredScheme) {
    this(secSource, preferredScheme, false);
  }
  
  public UnderlyingAggregationFunction(SecuritySource secSource, ExternalScheme preferredScheme, boolean useAttributes) {
    _secSource = secSource;
    _useAttributes = useAttributes;
    _underlyingVisitor = new UnderlyingIdVisitor(preferredScheme, secSource);
  }

  @Override
  public String classifyPosition(Position position) {
    if (_useAttributes) {
      Map<String, String> attributes = position.getAttributes();
      if (attributes.containsKey(getName())) {
        return attributes.get(getName());
      } else {
        return NOT_APPLICABLE;
      } 
    } else {
      if (position.getSecurityLink().getTarget() == null) {
        position.getSecurityLink().resolve(_secSource);
      }
      FinancialSecurity security = (FinancialSecurity) position.getSecurityLink().getTarget();
      try {
        String classification = security.accept(_underlyingVisitor);
        return classification == null ? NOT_APPLICABLE : classification;
      } catch (UnsupportedOperationException uoe) {
        return NOT_APPLICABLE;
      }
    }
  }

  @Override
  public String getName() {
    return "Underlying";
  }

  @Override
  public Collection<String> getRequiredEntries() {
    return Collections.emptyList();
  }

  @Override
  public int compare(String o1, String o2) {
    if (o1.equals(NOT_APPLICABLE)) {
      if (o2.equals(NOT_APPLICABLE)) {
        return 0;
      }
      return 1;
    } else if (o2.equals(NOT_APPLICABLE)) {
      return -1;
    }
    return o1.compareTo(o2);
  }

  @Override
  public Comparator<Position> getPositionComparator() {
    return _comparator;
  }

}
