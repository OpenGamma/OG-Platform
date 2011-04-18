/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * This should be pulled from the configuration.
 */
public class SyntheticIdentifierCurveInstrumentProvider implements CurveInstrumentProvider {
  private Currency _ccy;
  private StripInstrumentType _type;
  private IdentificationScheme _scheme;
  public SyntheticIdentifierCurveInstrumentProvider(Currency ccy, StripInstrumentType type, IdentificationScheme scheme) {
    Validate.notNull(ccy, "currency");
    Validate.notNull(type, "instrument type");
    Validate.notNull(scheme, "generated identifier scheme");
    _ccy = ccy;
    _type = type;
    _scheme = scheme;
  }
  @Override
  public Identifier getInstrument(LocalDate curveDate, Tenor tenor) {
    return Identifier.of(_scheme, _ccy.getCode() + _type.name() + tenor.getPeriod().toString());
  }
  
  @Override
  public Identifier getInstrument(LocalDate curveDate, Tenor tenor, int numQuarterlyFuturesFromTenor) {
    return Identifier.of(_scheme, _ccy.getCode() + _type.name() + tenor.getPeriod().toString());
  }
  
  public Currency getCurrency() {
    return _ccy;
  }
  
  public StripInstrumentType getType() {
    return _type;
  }
  
  public IdentificationScheme getScheme() {
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
