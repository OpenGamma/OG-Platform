/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableList;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.Config;
import com.opengamma.core.config.ConfigGroups;
import com.opengamma.financial.fudgemsg.CurveSpecificationBuilderConfigurationFudgeBuilder;
import com.opengamma.id.ExternalId;
import com.opengamma.util.time.Tenor;

/**
 *
 */
@Config(description = "Curve specification builder configuration", group = ConfigGroups.CURVES_LEGACY)
public class CurveSpecificationBuilderConfiguration {

  /**
   * The names of the curve instrument providers, currently used in CurveSpecificationBuilderConfiguration fudge messages and Web UI
   */
  public static final List<String> s_curveSpecNames = getCurveSpecBuilderConfigurationNames();

  private final Map<Tenor, CurveInstrumentProvider> _cashInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _fra3MInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _fra6MInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _liborInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _euriborInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _cdorInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _futureInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _swap12MInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _swap6MInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _swap3MInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _swap28DInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _basisSwapInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _tenorSwapInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _oisSwapInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _stiborInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _ciborInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _simpleZeroDepositInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _periodicZeroDepositInstrumentProviders;
  private final Map<Tenor, CurveInstrumentProvider> _continuousZeroDepositInstrumentProviders;

  /**
   * A curve specification builder configuration for a particular currency
   * @param cashInstrumentProviders a map of tenor to instrument providers for Cash curve instrument e.g. (StaticCurveInstrumentProvider)
   * @param fra3MInstrumentProviders a map of tenor to instrument providers for 3M FRA curve instruments (e.g. 3M x 6M)
   * @param fra6MInstrumentProviders a map of tenor to instrument providers for 6M FRA curve instruments (e.g. 3M x 9M)
   * @param liborInstrumentProviders a map of tenor to instrument providers for Libor curve instruments
   * @param euriborInstrumentProviders a map of tenor to instrument providers for Euribor curve instruments
   * @param cdorInstrumentProviders a map of tenor in instrument providers for CDOR curve instruments
   * @param ciborInstrumentProviders a map of tenor in instrument providers for Cibor curve instruments
   * @param stiborInstrumentProviders a map of tenor in instrument providers for Stibor curve instruments
   * @param futureInstrumentProviders a map of tenor to instrument providers for future curve instruments e.g. (BloombergFutureInstrumentProvider)
   * @param swap6MInstrumentProviders a map of tenor to instrument providers for 6M swap curve instruments where 6M is the floating tenor
   * @param swap3MInstrumentProviders a map of tenor to instrument providers for 3M swap curve instruments where 3M is the floating tenor
   * @param basisSwapInstrumentProviders a map of tenor to instrument providers for basis swap instruments
   * @param tenorSwapInstrumentProviders a map of tenor to instrument providers for tenor swap curve
   * @param oisSwapInstrumentProviders a map of tenor to instrument providers for OIS swap curve instruments
   * @param simpleZeroDepositInstrumentProviders a map of tenor to simple zero deposit instruments
   * @param periodicZeroDepositInstrumentProviders a map of tenor to periodic zero deposit instruments
   * @param continuousZeroDepositInstrumentProviders a map of tenor to continuous zero deposit instruments
   * @param swap12MInstrumentProviders a map of tenor to instrument providers for 12M swap curve instruments where 12M is the floating tenor
   * @param swap28DInstrumentProviders a map of tenor to instrument providers for 28D swap curve instruments where 28D is the floating tenor
   */
  public CurveSpecificationBuilderConfiguration(final Map<Tenor, CurveInstrumentProvider> cashInstrumentProviders, final Map<Tenor, CurveInstrumentProvider> fra3MInstrumentProviders,
      final Map<Tenor, CurveInstrumentProvider> fra6MInstrumentProviders, final Map<Tenor, CurveInstrumentProvider> liborInstrumentProviders,
      final Map<Tenor, CurveInstrumentProvider> euriborInstrumentProviders, final Map<Tenor, CurveInstrumentProvider> cdorInstrumentProviders,
      final Map<Tenor, CurveInstrumentProvider> ciborInstrumentProviders, final Map<Tenor, CurveInstrumentProvider> stiborInstrumentProviders,
      final Map<Tenor, CurveInstrumentProvider> futureInstrumentProviders, final Map<Tenor, CurveInstrumentProvider> swap6MInstrumentProviders,
      final Map<Tenor, CurveInstrumentProvider> swap3MInstrumentProviders, final Map<Tenor, CurveInstrumentProvider> basisSwapInstrumentProviders,
      final Map<Tenor, CurveInstrumentProvider> tenorSwapInstrumentProviders, final Map<Tenor, CurveInstrumentProvider> oisSwapInstrumentProviders,
      final Map<Tenor, CurveInstrumentProvider> simpleZeroDepositInstrumentProviders, final Map<Tenor, CurveInstrumentProvider> periodicZeroDepositInstrumentProviders,
      final Map<Tenor, CurveInstrumentProvider> continuousZeroDepositInstrumentProviders, final Map<Tenor, CurveInstrumentProvider> swap12MInstrumentProviders,
      final Map<Tenor, CurveInstrumentProvider> swap28DInstrumentProviders) {
    _cashInstrumentProviders = cashInstrumentProviders;
    _fra3MInstrumentProviders = fra3MInstrumentProviders;
    _fra6MInstrumentProviders = fra6MInstrumentProviders;
    _liborInstrumentProviders = liborInstrumentProviders;
    _euriborInstrumentProviders = euriborInstrumentProviders;
    _cdorInstrumentProviders = cdorInstrumentProviders;
    _ciborInstrumentProviders = ciborInstrumentProviders;
    _stiborInstrumentProviders = stiborInstrumentProviders;
    _futureInstrumentProviders = futureInstrumentProviders;
    _swap12MInstrumentProviders = swap12MInstrumentProviders;
    _swap6MInstrumentProviders = swap6MInstrumentProviders;
    _swap3MInstrumentProviders = swap3MInstrumentProviders;
    _basisSwapInstrumentProviders = basisSwapInstrumentProviders;
    _tenorSwapInstrumentProviders = tenorSwapInstrumentProviders;
    _oisSwapInstrumentProviders = oisSwapInstrumentProviders;
    _simpleZeroDepositInstrumentProviders = simpleZeroDepositInstrumentProviders;
    _periodicZeroDepositInstrumentProviders = periodicZeroDepositInstrumentProviders;
    _continuousZeroDepositInstrumentProviders = continuousZeroDepositInstrumentProviders;
    _swap28DInstrumentProviders = swap28DInstrumentProviders;
  }

  private static List<String> getCurveSpecBuilderConfigurationNames() {
    final List<String> list = new ArrayList<>();
    for (final Field field : CurveSpecificationBuilderConfigurationFudgeBuilder.class.getDeclaredFields()) {
      if (Modifier.isStatic(field.getModifiers()) && field.isSynthetic() == false) {
        field.setAccessible(true);
        try {
          list.add((String) field.get(null));
        } catch (final Exception ex) {
          // Ignore
        }
      }
    }
    Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
    return ImmutableList.copyOf(list);
  }

  private static ExternalId getStaticSecurity(final Map<Tenor, CurveInstrumentProvider> instrumentMappers, final LocalDate curveDate, final Tenor tenor) {
    final CurveInstrumentProvider mapper = instrumentMappers.get(tenor);
    if (mapper != null) {
      return mapper.getInstrument(curveDate, tenor);
    }
    throw new OpenGammaRuntimeException("can't find instrument mapper definition for " + tenor);
  }

  private static ExternalId getStaticSecurity(final Map<Tenor, CurveInstrumentProvider> instrumentMappers, final LocalDate curveDate, final FixedIncomeStrip strip) {
    final Tenor tenor = strip.getCurveNodePointTime();
    final Tenor payTenor = strip.getPayTenor();
    final Tenor receiveTenor = strip.getReceiveTenor();
    final IndexType payIndexType = strip.getPayIndexType();
    final IndexType receiveIndexType = strip.getReceiveIndexType();
    final CurveInstrumentProvider mapper = instrumentMappers.get(tenor);
    if (mapper != null) {
      return mapper.getInstrument(curveDate, tenor, payTenor, receiveTenor, payIndexType, receiveIndexType);
    }
    throw new OpenGammaRuntimeException("can't find instrument mapper definition for " + tenor);
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
  public ExternalId getSwap12MSecurity(final LocalDate curveDate, final Tenor tenor) {
    if (_swap12MInstrumentProviders == null) {
      throw new OpenGammaRuntimeException("Cannot get 12M swap instrument provider");
    }
    return getStaticSecurity(_swap12MInstrumentProviders, curveDate, tenor);
  }

  /**
   * Build a Swap security identifier for a curve node point
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @return the identifier of the security to use
   */
  public ExternalId getSwap28DSecurity(final LocalDate curveDate, final Tenor tenor) {
    if (_swap28DInstrumentProviders == null) {
      throw new OpenGammaRuntimeException("Cannot get 28D swap instrument provider");
    }
    return getStaticSecurity(_swap28DInstrumentProviders, curveDate, tenor);
  }

  /**
   * Build a Swap security identifier for a curve node point
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @return the identifier of the security to use
   */
  public ExternalId getSwap6MSecurity(final LocalDate curveDate, final Tenor tenor) {
    if (_swap6MInstrumentProviders == null) {
      throw new OpenGammaRuntimeException("Cannot get 6M swap instrument provider");
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
   * @param strip the basis swap strip
   * @return identifier of the security to use
   */
  public ExternalId getBasisSwapSecurity(final LocalDate curveDate, final FixedIncomeStrip strip) {
    if (_basisSwapInstrumentProviders == null) {
      throw new OpenGammaRuntimeException("Cannot get basis swap instrument provider");
    }
    return getStaticSecurity(_basisSwapInstrumentProviders, curveDate, strip);
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
   * Build a CDOR security identifier for a curve node point
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @return the identifier of the security to use
   */
  public ExternalId getCDORSecurity(final LocalDate curveDate, final Tenor tenor) {
    if (_cdorInstrumentProviders == null) {
      throw new OpenGammaRuntimeException("Cannot get CDOR instrument provider");
    }
    return getStaticSecurity(_cdorInstrumentProviders, curveDate, tenor);
  }

  /**
   * Build a Cibor security identifier for a curve node point
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @return the identifier of the security to use
   */
  public ExternalId getCiborSecurity(final LocalDate curveDate, final Tenor tenor) {
    if (_ciborInstrumentProviders == null) {
      throw new OpenGammaRuntimeException("Cannot get Cibor instrument provider");
    }
    return getStaticSecurity(_ciborInstrumentProviders, curveDate, tenor);
  }

  /**
   * Build a Stibor security identifier for a curve node point
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @return the identifier of the security to use
   */
  public ExternalId getStiborSecurity(final LocalDate curveDate, final Tenor tenor) {
    if (_stiborInstrumentProviders == null) {
      throw new OpenGammaRuntimeException("Cannot get Stibor instrument provider");
    }
    return getStaticSecurity(_stiborInstrumentProviders, curveDate, tenor);
  }

  /**
   * Build a simple zero deposit security identifier for a curve node point
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @return the identifier of the security to use
   */
  public ExternalId getSimpleZeroDepositSecurity(final LocalDate curveDate, final Tenor tenor) {
    if (_simpleZeroDepositInstrumentProviders == null) {
      throw new OpenGammaRuntimeException("Cannot get simple zero deposit instrument provider");
    }
    return getStaticSecurity(_simpleZeroDepositInstrumentProviders, curveDate, tenor);
  }

  /**
   * Build a periodic zero deposit security identifier for a curve node point
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @param periodsPerYear the periods per year
   * @return the identifier of the security to use
   */
  public ExternalId getPeriodicZeroDepositSecurity(final LocalDate curveDate, final Tenor tenor, final int periodsPerYear) {
    if (_periodicZeroDepositInstrumentProviders == null) {
      throw new OpenGammaRuntimeException("Cannot get periodic zero deposit instrument provider");
    }
    final CurveInstrumentProvider mapper = _periodicZeroDepositInstrumentProviders.get(tenor);
    if (mapper != null) {
      return mapper.getInstrument(curveDate, tenor, periodsPerYear, true);
    }
    throw new OpenGammaRuntimeException("can't find instrument mapper definition for " + tenor + " (looking for periodic zero deposit strip)");
  }

  /**
   * Build a continuous security identifier for a curve node point
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @return the identifier of the security to use
   */
  public ExternalId getContinuousZeroDepositSecurity(final LocalDate curveDate, final Tenor tenor) {
    if (_continuousZeroDepositInstrumentProviders == null) {
      throw new OpenGammaRuntimeException("Cannot get continuous zero deposit instrument provider");
    }
    return getStaticSecurity(_continuousZeroDepositInstrumentProviders, curveDate, tenor);
  }

  /**
   * Build a Future security identifier for a curve node point
   * @param curveDate the date of the start of the curve
   * @param tenor the time into the curve for this security
   * @param numberQuarterlyFuturesFromTenor the number of quarterly IR futures to traverse from (curveDate + tenor)
   * @return the identifier of the security to use
   */
  public ExternalId getFutureSecurity(final LocalDate curveDate, final Tenor tenor, final int numberQuarterlyFuturesFromTenor) {
    if (_futureInstrumentProviders == null) {
      throw new OpenGammaRuntimeException("Cannot get future instrument provider");
    }
    final CurveInstrumentProvider mapper = _futureInstrumentProviders.get(tenor);
    if (mapper != null) {
      return mapper.getInstrument(curveDate, tenor, numberQuarterlyFuturesFromTenor);
    }
    throw new OpenGammaRuntimeException("can't find instrument mapper definition for " + tenor + " (looking for future strip)");
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
   * @return the liborInstrumentProviders
   */
  public Map<Tenor, CurveInstrumentProvider> getLiborInstrumentProviders() {
    return _liborInstrumentProviders;
  }

  /**
   * Gets the euriborInstrumentProviders field for serialisation
   * @return the euriborInstrumentProviders
   */
  public Map<Tenor, CurveInstrumentProvider> getEuriborInstrumentProviders() {
    return _euriborInstrumentProviders;
  }

  /**
   * Gets the cdorInstrumentProviders field for serialisation
   * @return the cdorInstrumentProviders
   */
  public Map<Tenor, CurveInstrumentProvider> getCDORInstrumentProviders() {
    return _cdorInstrumentProviders;
  }

  /**
   * Gets the ciborInstrumentProviders field for serialisation
   * @return the ciborInstrumentProviders
   */
  public Map<Tenor, CurveInstrumentProvider> getCiborInstrumentProviders() {
    return _ciborInstrumentProviders;
  }

  /**
   * Gets the stiborInstrumentProviders field for serialisation
   * @return the stiborInstrumentProviders
   */
  public Map<Tenor, CurveInstrumentProvider> getStiborInstrumentProviders() {
    return _stiborInstrumentProviders;
  }

  /**
   * Gets the futureInstrumentProviders field for serialisation
   * @return the futureInstrumentProviders
   */
  public Map<Tenor, CurveInstrumentProvider> getFutureInstrumentProviders() {
    return _futureInstrumentProviders;
  }

  /**
   * Gets the swap12MInstrumentProviders field for serialisation
   * @return the swap12MInstrumentProviders
   */
  public Map<Tenor, CurveInstrumentProvider> getSwap12MInstrumentProviders() {
    return _swap12MInstrumentProviders;
  }

  /**
   * Gets the swap28DInstrumentProviders field for serialisation
   * @return the swap12MInstrumentProviders
   */
  public Map<Tenor, CurveInstrumentProvider> getSwap28DInstrumentProviders() {
    return _swap28DInstrumentProviders;
  }

  /**
   * Gets the swap6MInstrumentProviders field for serialisation
   * @return the swap6MInstrumentProviders
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

  /**
   * Gets the simpleZeroDepositInstrumentProviders for serialisation
   * @return the simpleZeroDepositInstrumentProviders
   */
  public Map<Tenor, CurveInstrumentProvider> getSimpleZeroDepositInstrumentProviders() {
    return _simpleZeroDepositInstrumentProviders;
  }

  /**
   * Gets the periodicZeroDepositInstrumentProviders for serialisation
   * @return the periodicZeroDepositInstrumentProviders
   */
  public Map<Tenor, CurveInstrumentProvider> getPeriodicZeroDepositInstrumentProviders() {
    return _periodicZeroDepositInstrumentProviders;
  }

  /**
   * Gets the continuousZeroDepositInstrumentProviders for serialisation
   * @return the continuousZeroDepositInstrumentProviders
   */
  public Map<Tenor, CurveInstrumentProvider> getContinuousZeroDepositInstrumentProviders() {
    return _continuousZeroDepositInstrumentProviders;
  }

  /**
   * Get all available tenors
   *
   * @return the sorted tenors
   */
  public SortedSet<Tenor> getAllTenors() {
    final SortedSet<Tenor> allTenors = new TreeSet<Tenor>();
    if (getBasisSwapInstrumentProviders() != null) {
      allTenors.addAll(getBasisSwapInstrumentProviders().keySet());
    }
    if (getCashInstrumentProviders() != null) {
      allTenors.addAll(getCashInstrumentProviders().keySet());
    }
    if (getCDORInstrumentProviders() != null) {
      allTenors.addAll(getCDORInstrumentProviders().keySet());
    }
    if (getCiborInstrumentProviders() != null) {
      allTenors.addAll(getCiborInstrumentProviders().keySet());
    }
    if (getEuriborInstrumentProviders() != null) {
      allTenors.addAll(getEuriborInstrumentProviders().keySet());
    }
    if (getFra3MInstrumentProviders() != null) {
      allTenors.addAll(getFra3MInstrumentProviders().keySet());
    }
    if (getFra6MInstrumentProviders() != null) {
      allTenors.addAll(getFra6MInstrumentProviders().keySet());
    }
    if (getFutureInstrumentProviders() != null) {
      allTenors.addAll(getFutureInstrumentProviders().keySet());
    }
    if (getLiborInstrumentProviders() != null) {
      allTenors.addAll(getLiborInstrumentProviders().keySet());
    }
    if (getOISSwapInstrumentProviders() != null) {
      allTenors.addAll(getOISSwapInstrumentProviders().keySet());
    }
    if (getStiborInstrumentProviders() != null) {
      allTenors.addAll(getStiborInstrumentProviders().keySet());
    }
    if (getSwap3MInstrumentProviders() != null) {
      allTenors.addAll(getSwap3MInstrumentProviders().keySet());
    }
    if (getSwap6MInstrumentProviders() != null) {
      allTenors.addAll(getSwap6MInstrumentProviders().keySet());
    }
    if (getSwap12MInstrumentProviders() != null) {
      allTenors.addAll(getSwap12MInstrumentProviders().keySet());
    }
    if (getTenorSwapInstrumentProviders() != null) {
      allTenors.addAll(getTenorSwapInstrumentProviders().keySet());
    }
    if (getSimpleZeroDepositInstrumentProviders() != null) {
      allTenors.addAll(getSimpleZeroDepositInstrumentProviders().keySet());
    }
    if (getPeriodicZeroDepositInstrumentProviders() != null) {
      allTenors.addAll(getPeriodicZeroDepositInstrumentProviders().keySet());
    }
    if (getContinuousZeroDepositInstrumentProviders() != null) {
      allTenors.addAll(getContinuousZeroDepositInstrumentProviders().keySet());
    }
    if (getSwap28DInstrumentProviders() != null) {
      allTenors.addAll(getSwap28DInstrumentProviders().keySet());
    }
    return allTenors;
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
    return (ObjectUtils.equals(getCashInstrumentProviders(), other.getCashInstrumentProviders())
        && ObjectUtils.equals(getFra3MInstrumentProviders(), other.getFra3MInstrumentProviders())
        && ObjectUtils.equals(getFra6MInstrumentProviders(), other.getFra6MInstrumentProviders())
        && ObjectUtils.equals(getFutureInstrumentProviders(), other.getFutureInstrumentProviders())
        && ObjectUtils.equals(getLiborInstrumentProviders(), other.getLiborInstrumentProviders())
        && ObjectUtils.equals(getEuriborInstrumentProviders(), other.getEuriborInstrumentProviders())
        && ObjectUtils.equals(getCiborInstrumentProviders(), other.getCiborInstrumentProviders())
        && ObjectUtils.equals(getStiborInstrumentProviders(), other.getStiborInstrumentProviders())
        && ObjectUtils.equals(getCDORInstrumentProviders(), other.getCDORInstrumentProviders())
        && ObjectUtils.equals(getSwap12MInstrumentProviders(), other.getSwap12MInstrumentProviders())
        && ObjectUtils.equals(getSwap6MInstrumentProviders(), other.getSwap6MInstrumentProviders())
        && ObjectUtils.equals(getSwap3MInstrumentProviders(), other.getSwap3MInstrumentProviders())
        && ObjectUtils.equals(getBasisSwapInstrumentProviders(), other.getBasisSwapInstrumentProviders())
        && ObjectUtils.equals(getTenorSwapInstrumentProviders(), other.getTenorSwapInstrumentProviders()))
        && ObjectUtils.equals(getOISSwapInstrumentProviders(), other.getOISSwapInstrumentProviders())
        && ObjectUtils.equals(getSimpleZeroDepositInstrumentProviders(), other.getSimpleZeroDepositInstrumentProviders())
        && ObjectUtils.equals(getPeriodicZeroDepositInstrumentProviders(), other.getPeriodicZeroDepositInstrumentProviders())
        && ObjectUtils.equals(getContinuousZeroDepositInstrumentProviders(), other.getContinuousZeroDepositInstrumentProviders())
        && ObjectUtils.equals(getSwap28DInstrumentProviders(), other.getSwap28DInstrumentProviders());
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_basisSwapInstrumentProviders == null) ? 0 : _basisSwapInstrumentProviders.hashCode());
    result = prime * result + ((_cashInstrumentProviders == null) ? 0 : _cashInstrumentProviders.hashCode());
    result = prime * result + ((_cdorInstrumentProviders == null) ? 0 : _cdorInstrumentProviders.hashCode());
    result = prime * result + ((_ciborInstrumentProviders == null) ? 0 : _ciborInstrumentProviders.hashCode());
    result = prime * result + ((_continuousZeroDepositInstrumentProviders == null) ? 0 : _continuousZeroDepositInstrumentProviders.hashCode());
    result = prime * result + ((_euriborInstrumentProviders == null) ? 0 : _euriborInstrumentProviders.hashCode());
    result = prime * result + ((_fra3MInstrumentProviders == null) ? 0 : _fra3MInstrumentProviders.hashCode());
    result = prime * result + ((_fra6MInstrumentProviders == null) ? 0 : _fra6MInstrumentProviders.hashCode());
    result = prime * result + ((_futureInstrumentProviders == null) ? 0 : _futureInstrumentProviders.hashCode());
    result = prime * result + ((_liborInstrumentProviders == null) ? 0 : _liborInstrumentProviders.hashCode());
    result = prime * result + ((_oisSwapInstrumentProviders == null) ? 0 : _oisSwapInstrumentProviders.hashCode());
    result = prime * result + ((_periodicZeroDepositInstrumentProviders == null) ? 0 : _periodicZeroDepositInstrumentProviders.hashCode());
    result = prime * result + ((_simpleZeroDepositInstrumentProviders == null) ? 0 : _simpleZeroDepositInstrumentProviders.hashCode());
    result = prime * result + ((_stiborInstrumentProviders == null) ? 0 : _stiborInstrumentProviders.hashCode());
    result = prime * result + ((_swap12MInstrumentProviders == null) ? 0 : _swap12MInstrumentProviders.hashCode());
    result = prime * result + ((_swap28DInstrumentProviders == null) ? 0 : _swap28DInstrumentProviders.hashCode());
    result = prime * result + ((_swap3MInstrumentProviders == null) ? 0 : _swap3MInstrumentProviders.hashCode());
    result = prime * result + ((_swap6MInstrumentProviders == null) ? 0 : _swap6MInstrumentProviders.hashCode());
    result = prime * result + ((_tenorSwapInstrumentProviders == null) ? 0 : _tenorSwapInstrumentProviders.hashCode());
    return result;
  }

}
