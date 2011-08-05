/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.Map;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalId;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class CurveSpecificationBuilderConfiguration {
  private Map<Tenor, CurveInstrumentProvider> _cashInstrumentProviders;
  private Map<Tenor, CurveInstrumentProvider> _fraInstrumentProviders;
  private Map<Tenor, CurveInstrumentProvider> _rateInstrumentProviders;
  private Map<Tenor, CurveInstrumentProvider> _futureInstrumentProviders;
  private Map<Tenor, CurveInstrumentProvider> _swapInstrumentProviders;
  private Map<Tenor, CurveInstrumentProvider> _basisSwapInstrumentProviders;
  private Map<Tenor, CurveInstrumentProvider> _tenorSwapInstrumentProviders;
  private Map<Tenor, CurveInstrumentProvider> _oisSwapInstrumentProviders;

  /**
   * A curve specification builder configuration for a particular currency
   * @param cashInstrumentProviders a map of tenor to instrument providers for Cash curve instrument e.g. (StaticCurveInstrumentProvider)
   * @param fraInstrumentProviders a map of tenor to instrument providers for FRA curve instruments
   * @param rateInstrumentProviders a map of tenor to instrument providers for rate (e.g. LIBOR) curve instruments
   * @param futureInstrumentProviders a map of tenor to instrument providers for future curve instruments e.g. (BloombergFutureInstrumentProvider)
   * @param swapInstrumentProviders a map of tenor to instrument providers for swap curve instruments
   * @param basisSwapInstrumentProviders a map of tenor to instrument providers for basis swap instruments
   * @param tenorSwapInstrumentProviders a map of tenor swap to instrument providers for tenor swap curve 
   * @param oisSwapInstrumentProviders a map of OIS swap to instrument providers for OIS swap curve instruments
   */
  public CurveSpecificationBuilderConfiguration(final Map<Tenor, CurveInstrumentProvider> cashInstrumentProviders,
                                                final Map<Tenor, CurveInstrumentProvider> fraInstrumentProviders,
                                                final Map<Tenor, CurveInstrumentProvider> rateInstrumentProviders,
                                                final Map<Tenor, CurveInstrumentProvider> futureInstrumentProviders,
                                                final Map<Tenor, CurveInstrumentProvider> swapInstrumentProviders,
                                                final Map<Tenor, CurveInstrumentProvider> basisSwapInstrumentProviders,
                                                final Map<Tenor, CurveInstrumentProvider> tenorSwapInstrumentProviders,
                                                final Map<Tenor, CurveInstrumentProvider> oisSwapInstrumentProviders) {
    _cashInstrumentProviders = cashInstrumentProviders;
    _fraInstrumentProviders = fraInstrumentProviders;
    _rateInstrumentProviders = rateInstrumentProviders;
    _futureInstrumentProviders = futureInstrumentProviders;
    _swapInstrumentProviders = swapInstrumentProviders;
    _basisSwapInstrumentProviders = basisSwapInstrumentProviders;
    _tenorSwapInstrumentProviders = tenorSwapInstrumentProviders;
    _oisSwapInstrumentProviders = oisSwapInstrumentProviders;
  }

  private ExternalId getStaticSecurity(final Map<Tenor, CurveInstrumentProvider> instrumentMappers, final LocalDate curveDate, final Tenor tenor) {
    final CurveInstrumentProvider mapper = instrumentMappers.get(tenor);
    if (mapper != null) {
      return mapper.getInstrument(curveDate, tenor);
    } else {
      throw new OpenGammaRuntimeException("can't find instrument mapper definition for " + tenor);
    }
  }

  /**
   * Build a cash security identifier for a curve node point
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @return the identifier of the security to use
   */
  public ExternalId getCashSecurity(final LocalDate curveDate, final Tenor tenor) {
    return getStaticSecurity(_cashInstrumentProviders, curveDate, tenor);
  }

  /**
   * Build a FRA security identifier for a curve node point 
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @return the identifier of the security to use
   */
  public ExternalId getFRASecurity(final LocalDate curveDate, final Tenor tenor) {
    return getStaticSecurity(_fraInstrumentProviders, curveDate, tenor);
  }

  /**
   * Build a Swap security identifier for a curve node point 
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @return the identifier of the security to use
   */
  public ExternalId getSwapSecurity(final LocalDate curveDate, final Tenor tenor) {
    return getStaticSecurity(_swapInstrumentProviders, curveDate, tenor);
  }

  /**
   * Build a Basis Swap security identifier for a curve node point
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @return identifier of the security to use
   */
  public ExternalId getBasisSwapSecurity(final LocalDate curveDate, final Tenor tenor) {
    return getStaticSecurity(_basisSwapInstrumentProviders, curveDate, tenor);
  }

  /**
   * Build a Tenor Swap security identifier for a curve node point
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @return identifier of the security to use
   */
  public ExternalId getTenorSwapSecurity(final LocalDate curveDate, final Tenor tenor) {
    return getStaticSecurity(_tenorSwapInstrumentProviders, curveDate, tenor);
  }

  /**
   * Build a OIS swap security identifier for a curve node point
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @return identifier of the security to use
   */
  public ExternalId getOISSwapSecurity(final LocalDate curveDate, final Tenor tenor) {
    return getStaticSecurity(_oisSwapInstrumentProviders, curveDate, tenor);
  }

  /**
   * Build a rate security identifier for a curve node point 
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @return the identifier of the security to use
   */
  public ExternalId getRateSecurity(final LocalDate curveDate, final Tenor tenor) {
    return getStaticSecurity(_rateInstrumentProviders, curveDate, tenor);
  }

  /**
   * Build a Future security identifier for a curve node point 
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @param numberQuarterlyFuturesFromTenor the number of quarterly IR futures to traverse from (curveDate + tenor) 
   * @return the identifier of the security to use
   */
  public ExternalId getFutureSecurity(final LocalDate curveDate, final Tenor tenor, final int numberQuarterlyFuturesFromTenor) {
    final CurveInstrumentProvider mapper = _futureInstrumentProviders.get(tenor);
    if (mapper != null) {
      return mapper.getInstrument(curveDate, tenor, numberQuarterlyFuturesFromTenor);
    } else {
      throw new OpenGammaRuntimeException("can't find instrument mapper definition for " + tenor);
    }
  }

  /**
   * Gets the cashInstrumentProviders field for serialisation
   * @return the cashInstrumentProviders
   */
  public Map<Tenor, CurveInstrumentProvider> getCashInstrumentProviders() {
    return _cashInstrumentProviders;
  }

  /**
   * Gets the fraInstrumentProviders field for serialisation
   * @return the fraInstrumentProviders
   */
  public Map<Tenor, CurveInstrumentProvider> getFraInstrumentProviders() {
    return _fraInstrumentProviders;
  }

  /**
   * Gets the rateInstrumentProviders field for serialisation
   * @return the rateInstrumentProviders
   */
  public Map<Tenor, CurveInstrumentProvider> getRateInstrumentProviders() {
    return _rateInstrumentProviders;
  }

  /**
   * Gets the futureInstrumentProviders field for serialisation
   * @return the futureInstrumentProviders
   */
  public Map<Tenor, CurveInstrumentProvider> getFutureInstrumentProviders() {
    return _futureInstrumentProviders;
  }

  /**
   * Gets the swapInstrumentProviders field for serialisation
   * @return the swapInstrumentProviders
   */
  public Map<Tenor, CurveInstrumentProvider> getSwapInstrumentProviders() {
    return _swapInstrumentProviders;
  }

  /**
   * Gets the basisSwapInstrumentProviders field for serialisation
   * @return the basisSwapInstrumentProviders
   */
  public Map<Tenor, CurveInstrumentProvider> getBasisSwapInstrumentProviders() {
    return _basisSwapInstrumentProviders;
  }

  /**
   * Gets the tenorSwapInstrumentProviders field for serialisation
   * @return the swapInstrumentProviders
   */
  public Map<Tenor, CurveInstrumentProvider> getTenorSwapInstrumentProviders() {
    return _tenorSwapInstrumentProviders;
  }

  /**
   * Gets the oisSwapInstrumentProviders field for serialisation
   * @return the oisSwapInstrumentProviders
   */
  public Map<Tenor, CurveInstrumentProvider> getOISSwapInstrumentProviders() {
    return _oisSwapInstrumentProviders;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof CurveSpecificationBuilderConfiguration)) {
      return false;
    }
    final CurveSpecificationBuilderConfiguration other = (CurveSpecificationBuilderConfiguration) o;
    return (ObjectUtils.equals(getCashInstrumentProviders(), other.getCashInstrumentProviders()) &&
            ObjectUtils.equals(getFraInstrumentProviders(), other.getFraInstrumentProviders()) &&
            ObjectUtils.equals(getFutureInstrumentProviders(), other.getFutureInstrumentProviders()) &&
            ObjectUtils.equals(getRateInstrumentProviders(), other.getRateInstrumentProviders()) &&
            ObjectUtils.equals(getSwapInstrumentProviders(), other.getSwapInstrumentProviders()) &&
            ObjectUtils.equals(getBasisSwapInstrumentProviders(), other.getBasisSwapInstrumentProviders()) &&
            ObjectUtils.equals(getTenorSwapInstrumentProviders(), other.getTenorSwapInstrumentProviders())) &&
            ObjectUtils.equals(getOISSwapInstrumentProviders(), other.getOISSwapInstrumentProviders());
  }

  @Override
  public int hashCode() {
    return getCashInstrumentProviders().hashCode(); // bit dodgy really, but should only be used for testing.
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
