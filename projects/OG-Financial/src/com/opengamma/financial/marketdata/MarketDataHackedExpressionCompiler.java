/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.marketdata.OverrideOperation;
import com.opengamma.engine.marketdata.OverrideOperationCompiler;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.cache.MissingMarketDataSentinel;
import com.opengamma.engine.view.calc.SingleComputationCycle;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.ArgumentChecker;

/**
 * Implements the hacks previously in {@link SingleComputationCycle} using the
 * {@link OverrideOperation} mechanism.
 * 
 * @deprecated Should not be used; the EL based compiler is more flexible
 */
@Deprecated
public class MarketDataHackedExpressionCompiler implements OverrideOperationCompiler {

  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataHackedExpressionCompiler.class);

  private final SecuritySource _securitySource;

  private final class Operation implements OverrideOperation {

    private final double _shift;

    public Operation(final double shift) {
      _shift = shift;
    }

    @Override
    public Object apply(final ValueRequirement valueRequirement, final Object value) {
      final ComputationTargetSpecification targetSpec = valueRequirement.getTargetSpecification();
      // Only shift equities
      if ((targetSpec.getType() != ComputationTargetType.SECURITY) || !MarketDataRequirementNames.MARKET_VALUE.equals(valueRequirement.getValueName())) {
        return value;
      }
      final Security security;
      try {
        security = getSecuritySource().getSecurity(targetSpec.getUniqueId());
      } catch (DataNotFoundException ex) {
        return value;
      }
      if (!security.getExternalIdBundle().getValue(SecurityUtils.BLOOMBERG_TICKER).contains("Equity")) {
        return value;
      }
      if (value instanceof Number) {
        return ((Number) value).doubleValue() * _shift;
      } else if (value instanceof MissingMarketDataSentinel) {
        return value;
      } else {
        s_logger.warn("Can't shift market data {} - not a number", value);
      }
      return value;
    }

  }

  public MarketDataHackedExpressionCompiler(final SecuritySource securitySource) {
    ArgumentChecker.notNull(securitySource, "securitySource");
    _securitySource = securitySource;
  }

  @Override
  public OverrideOperation compile(final String operation) {
    try {
      return new Operation(Double.parseDouble(operation));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Market data shift " + operation + " not valid");
    }
  }

  private SecuritySource getSecuritySource() {
    return _securitySource;
  }

}
