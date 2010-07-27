/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.Map;

import javax.time.calendar.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.Identifier;
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

  /**
   * A curve specification builder configuration for a particular currency
   * @param cashInstrumentProviders a map of tenor to instrument providers for Cash curve instrument e.g. (StaticCurveInstrumentProvider)
   * @param fraInstrumentProviders a map of tenor to instrument providers for FRA curve instruments
   * @param rateInstrumentProviders a map of tenor to instrument providers for rate (e.g. LIBOR) curve instruments
   * @param futureInstrumentProviders a map of tenor to instrument providers for future curve instruments e.g. (BloombergFutureInstrumentProvider)
   * @param swapInstrumentProviders a map of tenor to instrument providers for swap curve instruments
   */
  public CurveSpecificationBuilderConfiguration(Map<Tenor, CurveInstrumentProvider> cashInstrumentProviders,
                                                Map<Tenor, CurveInstrumentProvider> fraInstrumentProviders,
                                                Map<Tenor, CurveInstrumentProvider> rateInstrumentProviders,
                                                Map<Tenor, CurveInstrumentProvider> futureInstrumentProviders,
                                                Map<Tenor, CurveInstrumentProvider> swapInstrumentProviders) {
    _cashInstrumentProviders = cashInstrumentProviders;
    _fraInstrumentProviders = fraInstrumentProviders;
    _rateInstrumentProviders = rateInstrumentProviders;
    _futureInstrumentProviders = futureInstrumentProviders;
    _swapInstrumentProviders = swapInstrumentProviders;
  }
  
  private Identifier getStaticSecurity(Map<Tenor, CurveInstrumentProvider> instrumentMappers, LocalDate curveDate, Tenor tenor) {
    CurveInstrumentProvider mapper = instrumentMappers.get(tenor);
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
  public Identifier getCashSecurity(LocalDate curveDate, Tenor tenor) {
    return getStaticSecurity(_cashInstrumentProviders, curveDate, tenor);
  }
  
  /**
   * Build a FRA security identifier for a curve node point 
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @return the identifier of the security to use
   */
  public Identifier getFRASecurity(LocalDate curveDate, Tenor tenor) {
    return getStaticSecurity(_fraInstrumentProviders, curveDate, tenor);
  }

  /**
   * Build a Swap security identifier for a curve node point 
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @return the identifier of the security to use
   */
  public Identifier getSwapSecurity(LocalDate curveDate, Tenor tenor) {
    return getStaticSecurity(_swapInstrumentProviders, curveDate, tenor);
  }
  
  /**
   * Build a rate security identifier for a curve node point 
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @return the identifier of the security to use
   */
  public Identifier getRateSecurity(LocalDate curveDate, Tenor tenor) {
    return getStaticSecurity(_rateInstrumentProviders, curveDate, tenor);
  }

  /**
   * Build a Future security identifier for a curve node point 
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @param numberQuarterlyFuturesFromTenor the number of quarterly IR futures to traverse from (curveDate + tenor) 
   * @return the identifier of the security to use
   */
  public Identifier getFutureSecurity(LocalDate curveDate, Tenor tenor, int numberQuarterlyFuturesFromTenor) {
    CurveInstrumentProvider mapper = _futureInstrumentProviders.get(tenor);
    if (mapper != null) {
      return mapper.getInstrument(curveDate, tenor, numberQuarterlyFuturesFromTenor);
    } else {
      throw new OpenGammaRuntimeException("can't find instrument mapper definition for " + tenor);
    }
  }
}
