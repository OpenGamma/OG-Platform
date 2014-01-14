/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fxforwardcurve;

import com.opengamma.core.config.Config;
import com.opengamma.core.config.ConfigGroups;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * 
 */
@Config(description = "FX forward curve specification", group = ConfigGroups.CURVES_LEGACY)
public class FXForwardCurveSpecification {
  /** The type of the FX forward quote */
  public enum QuoteType {
    /** Outright */
    Outright,
    /** Points */
    Points
  }
  private final FXForwardCurveInstrumentProvider _curveInstrumentProvider;
  private final String _name;
  private final UnorderedCurrencyPair _target;
  private final QuoteType _quoteType;
  private final boolean _isRegularQuote;

  public FXForwardCurveSpecification(final String name, final UnorderedCurrencyPair target, final FXForwardCurveInstrumentProvider curveInstrumentProvider) {
    this(name, target, curveInstrumentProvider, QuoteType.Points, true);
  }

  public FXForwardCurveSpecification(final String name, final UnorderedCurrencyPair target, final FXForwardCurveInstrumentProvider curveInstrumentProvider,
      final QuoteType quoteType) {
    this(name, target, curveInstrumentProvider, quoteType, true);
  }
  
  public FXForwardCurveSpecification(final String name, final UnorderedCurrencyPair target, final FXForwardCurveInstrumentProvider curveInstrumentProvider,
      final QuoteType quoteType, final boolean isRegularQuote) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(target, "target");
    ArgumentChecker.notNull(curveInstrumentProvider, "curve instrument provider");
    ArgumentChecker.notNull(quoteType, "quote type");
    _name = name;
    _target = target;
    _curveInstrumentProvider = curveInstrumentProvider;
    _quoteType = quoteType;
    _isRegularQuote = isRegularQuote;
  }

  public String getName() {
    return _name;
  }

  public UnorderedCurrencyPair getTarget() {
    return _target;
  }

  public FXForwardCurveInstrumentProvider getCurveInstrumentProvider() {
    return _curveInstrumentProvider;
  }

  public QuoteType getQuoteType() {
    return _quoteType;
  }

  public boolean isMarketQuoteConvention() {
    return _isRegularQuote;  
  }
  
  @Override
  public int hashCode() {
    return getName().hashCode() + getTarget().hashCode() + getQuoteType().hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof FXForwardCurveSpecification)) {
      return false;
    }
    final FXForwardCurveSpecification other = (FXForwardCurveSpecification) obj;
    return getName().equals(other.getName()) &&
        getTarget().equals(other.getTarget()) &&
        getCurveInstrumentProvider().equals(other.getCurveInstrumentProvider()) &&
        getQuoteType().equals(other.getQuoteType()) &&
        isMarketQuoteConvention() == other.isMarketQuoteConvention();
  }


}
