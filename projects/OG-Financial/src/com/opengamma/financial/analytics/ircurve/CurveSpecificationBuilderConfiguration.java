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
  private final Map<Tenor, CurveInstrumentProvider> _cashInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _fra3MInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _fra6MInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _liborInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _euriborInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _futureInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _swap6MInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _swap3MInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _basisSwapInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _tenorSwapInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _oisSwapInstrumentProviders;

  /**
   * A curve specification builder configuration for a particular currency
   * @param cashInstrumentProviders a map of tenor to instrument providers for Cash curve instrument e.g. (StaticCurveInstrumentProvider)
   * @param fra3MInstrumentProviders a map of tenor to instrument providers for 3M FRA curve instruments (e.g. 3M x 6M)
   * @param fra6MInstrumentProviders a map of tenor to instrument providers for 6M FRA curve instruments (e.g. 3M x 9M)
   * @param liborInstrumentProviders a map of tenor to instrument providers for Libor curve instruments
   * @param euriborInstrumentProviders a map of tenor to instrument providers for Euribor curve instruments
   * @param futureInstrumentProviders a map of tenor to instrument providers for future curve instruments e.g. (BloombergFutureInstrumentProvider)
   * @param swap6MInstrumentProviders a map of tenor to instrument providers for 6M swap curve instruments where 6M is the floating tenor
   * @param swap3MInstrumentProviders a map of tenor to instrument providers for 3M swap curve instruments where 3M is the floating tenor
   * @param basisSwapInstrumentProviders a map of tenor to instrument providers for basis swap instruments
   * @param tenorSwapInstrumentProviders a map of tenor swap to instrument providers for tenor swap curve 
   * @param oisSwapInstrumentProviders a map of OIS swap to instrument providers for OIS swap curve instruments
   */
  public CurveSpecificationBuilderConfiguration(final Map<Tenor, CurveInstrumentProvider> cashInstrumentProviders, final Map<Tenor, CurveInstrumentProvider> fra3MInstrumentProviders,
      final Map<Tenor, CurveInstrumentProvider> fra6MInstrumentProviders, final Map<Tenor, CurveInstrumentProvider> liborInstrumentProviders,
      final Map<Tenor, CurveInstrumentProvider> euriborInstrumentProviders, final Map<Tenor, CurveInstrumentProvider> futureInstrumentProviders,
      final Map<Tenor, CurveInstrumentProvider> swap6MInstrumentProviders, final Map<Tenor, CurveInstrumentProvider> swap3MInstrumentProviders,
      final Map<Tenor, CurveInstrumentProvider> basisSwapInstrumentProviders, final Map<Tenor, CurveInstrumentProvider> tenorSwapInstrumentProviders,
      final Map<Tenor, CurveInstrumentProvider> oisSwapInstrumentProviders) {
    _cashInstrumentProviders = cashInstrumentProviders;
    _fra3MInstrumentProviders = fra3MInstrumentProviders;
    _fra6MInstrumentProviders = fra6MInstrumentProviders;
    _liborInstrumentProviders = liborInstrumentProviders;
    _euriborInstrumentProviders = euriborInstrumentProviders;
    _futureInstrumentProviders = futureInstrumentProviders;
    _swap6MInstrumentProviders = swap6MInstrumentProviders;
    _swap3MInstrumentProviders = swap3MInstrumentProviders;
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
    if (_cashInstrumentProviders == null) {
      throw new OpenGammaRuntimeException("Cannot get cash instrument provider");
    }
    return getStaticSecurity(_cashInstrumentProviders, curveDate, tenor);
  }

  /**
   * Build a 3M FRA security identifier for a curve node point 
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @return the identifier of the security to use
   */
  public ExternalId getFRA3MSecurity(final LocalDate curveDate, final Tenor tenor) {
    if (_fra3MInstrumentProviders == null) {
      throw new OpenGammaRuntimeException("Cannot get 3M FRA instrument provider");
    }
    return getStaticSecurity(_fra3MInstrumentProviders, curveDate, tenor);
  }

  /**
   * Build a 6M FRA security identifier for a curve node point 
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @return the identifier of the security to use
   */
  public ExternalId getFRA6MSecurity(final LocalDate curveDate, final Tenor tenor) {
    if (_fra6MInstrumentProviders == null) {
      throw new OpenGammaRuntimeException("Cannot get 6M FRA instrument provider");
    }
    return getStaticSecurity(_fra6MInstrumentProviders, curveDate, tenor);
  }

  /**
   * Build a Swap security identifier for a curve node point 
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @return the identifier of the security to use
   */
  public ExternalId getSwap6MSecurity(final LocalDate curveDate, final Tenor tenor) {
    if (_swap6MInstrumentProviders == null) {
      throw new OpenGammaRuntimeException("Cannot get swap instrument provider");
    }
    return getStaticSecurity(_swap6MInstrumentProviders, curveDate, tenor);
  }

  /**
   * Build a 3M swap security identifier for a curve node point 
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @return the identifier of the security to use
   */
  public ExternalId getSwap3MSecurity(final LocalDate curveDate, final Tenor tenor) {
    if (_swap3MInstrumentProviders == null) {
      throw new OpenGammaRuntimeException("Cannot get 3M swap instrument provider");
    }
    return getStaticSecurity(_swap3MInstrumentProviders, curveDate, tenor);
  }

  /**
   * Build a Basis Swap security identifier for a curve node point
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @return identifier of the security to use
   */
  public ExternalId getBasisSwapSecurity(final LocalDate curveDate, final Tenor tenor) {
    if (_basisSwapInstrumentProviders == null) {
      throw new OpenGammaRuntimeException("Cannot get basis swap instrument provider");
    }
    return getStaticSecurity(_basisSwapInstrumentProviders, curveDate, tenor);
  }

  /**
   * Build a Tenor Swap security identifier for a curve node point
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @return identifier of the security to use
   */
  public ExternalId getTenorSwapSecurity(final LocalDate curveDate, final Tenor tenor) {
    if (_tenorSwapInstrumentProviders == null) {
      throw new OpenGammaRuntimeException("Cannot get tenor swap instrument provider");
    }
    return getStaticSecurity(_tenorSwapInstrumentProviders, curveDate, tenor);
  }

  /**
   * Build a OIS swap security identifier for a curve node point
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @return identifier of the security to use
   */
  public ExternalId getOISSwapSecurity(final LocalDate curveDate, final Tenor tenor) {
    if (_oisSwapInstrumentProviders == null) {
      throw new OpenGammaRuntimeException("Cannot get OIS swap instrument provider");
    }
    return getStaticSecurity(_oisSwapInstrumentProviders, curveDate, tenor);
  }

  /**
   * Build a Libor security identifier for a curve node point 
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @return the identifier of the security to use
   */
  public ExternalId getLiborSecurity(final LocalDate curveDate, final Tenor tenor) {
    if (_liborInstrumentProviders == null) {
      throw new OpenGammaRuntimeException("Cannot get Libor instrument provider");
    }
    return getStaticSecurity(_liborInstrumentProviders, curveDate, tenor);
  }

  /**
   * Build a Euribor security identifier for a curve node point 
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @return the identifier of the security to use
   */
  public ExternalId getEuriborSecurity(final LocalDate curveDate, final Tenor tenor) {
    if (_euriborInstrumentProviders == null) {
      throw new OpenGammaRuntimeException("Cannot get Euribor instrument provider");
    }
    return getStaticSecurity(_euriborInstrumentProviders, curveDate, tenor);
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
   * Gets the fra3MInstrumentProviders field for serialisation
   * @return the fra3MInstrumentProviders
   */
  public Map<Tenor, CurveInstrumentProvider> getFra3MInstrumentProviders() {
    return _fra3MInstrumentProviders;
  }

  /**
   * Gets the fra6MInstrumentProviders field for serialisation
   * @return the fra6MInstrumentProviders
   */
  public Map<Tenor, CurveInstrumentProvider> getFra6MInstrumentProviders() {
    return _fra6MInstrumentProviders;
  }

  /**
   * Gets the liborInstrumentProviders field for serialisation
   * @return the rateInstrumentProviders
   */
  public Map<Tenor, CurveInstrumentProvider> getLiborInstrumentProviders() {
    return _liborInstrumentProviders;
  }

  /**
   * Gets the euriborInstrumentProviders field for serialisation
   * @return the rateInstrumentProviders
   */
  public Map<Tenor, CurveInstrumentProvider> getEuriborInstrumentProviders() {
    return _euriborInstrumentProviders;
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
  public Map<Tenor, CurveInstrumentProvider> getSwap6MInstrumentProviders() {
    return _swap6MInstrumentProviders;
  }

  /**
   * Gets the swap3MInstrumentProviders field for serialisation
   * @return the swap3MInstrumentProviders
   */
  public Map<Tenor, CurveInstrumentProvider> getSwap3MInstrumentProviders() {
    return _swap3MInstrumentProviders;
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
    return (ObjectUtils.equals(getCashInstrumentProviders(), other.getCashInstrumentProviders()) && ObjectUtils.equals(getFra3MInstrumentProviders(), other.getFra3MInstrumentProviders())
        && ObjectUtils.equals(getFra6MInstrumentProviders(), other.getFra6MInstrumentProviders()) && ObjectUtils.equals(getFutureInstrumentProviders(), other.getFutureInstrumentProviders())
        && ObjectUtils.equals(getLiborInstrumentProviders(), other.getLiborInstrumentProviders()) && ObjectUtils.equals(getEuriborInstrumentProviders(), other.getEuriborInstrumentProviders())
        && ObjectUtils.equals(getSwap6MInstrumentProviders(), other.getSwap6MInstrumentProviders()) && ObjectUtils.equals(getSwap3MInstrumentProviders(), other.getSwap3MInstrumentProviders())
        && ObjectUtils.equals(getBasisSwapInstrumentProviders(), other.getBasisSwapInstrumentProviders()) && ObjectUtils.equals(getTenorSwapInstrumentProviders(),
        other.getTenorSwapInstrumentProviders()))
        && ObjectUtils.equals(getOISSwapInstrumentProviders(), other.getOISSwapInstrumentProviders());
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
