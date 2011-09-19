/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * This should be pulled from the configuration.
 */
public class SyntheticIdentifierCurveInstrumentProvider implements CurveInstrumentProvider {
  private Currency _ccy;
  private StripInstrumentType _type;
  private StripInstrumentType _idType;
  private ExternalScheme _scheme;
  public SyntheticIdentifierCurveInstrumentProvider(Currency ccy, StripInstrumentType type, ExternalScheme scheme) {
    Validate.notNull(ccy, "currency");
    Validate.notNull(type, "instrument type");
    Validate.notNull(scheme, "generated identifier scheme");
    _ccy = ccy;
    _type = type;
    _scheme = scheme;
    
    switch (type) {
      case SWAP_3M:
      case SWAP_6M:
        _idType = StripInstrumentType.SWAP;
        break;
      case FRA_3M:
      case FRA_6M:
        _idType = StripInstrumentType.FRA;
        break;
      default:
        _idType = type;
        break;
    }
  }
  @Override
  public ExternalId getInstrument(LocalDate curveDate, Tenor tenor) {
    return ExternalId.of(_scheme, _ccy.getCode() + _idType.name() + tenor.getPeriod().toString());
  }
  
  @Override
  public ExternalId getInstrument(LocalDate curveDate, Tenor tenor, int numQuarterlyFuturesFromTenor) {
    return ExternalId.of(_scheme, _ccy.getCode() + _idType.name() + tenor.getPeriod().toString());
  }
  
  public Currency getCurrency() {
    return _ccy;
  }
  
  public StripInstrumentType getType() {
    return _type;
  }
  
  public ExternalScheme getScheme() {
    return _scheme;
  }
  
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof SyntheticIdentifierCurveInstrumentProvider)) {
      return false;
    }
    SyntheticIdentifierCurveInstrumentProvider other = (SyntheticIdentifierCurveInstrumentProvider) o;
    return _ccy.equals(other._ccy) &&
           _type.equals(other._type) &&
           _scheme.equals(other._scheme);
  }
  
  public int hashCode() {
    return _ccy.hashCode() ^ (_type.hashCode() * 64);
  }
  
}
