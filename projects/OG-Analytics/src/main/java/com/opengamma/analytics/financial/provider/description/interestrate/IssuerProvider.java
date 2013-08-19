/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * Class describing a provider with multi-curves and issuer-specific curves.
 */
public class IssuerProvider implements IssuerProviderInterface {

  /**
   * The multicurve provider.
   */
  private final MulticurveProviderInterface _multicurveProvider;
  /**
   * A map with issuer discounting curves.
   */
  private final Map<Pair<String, Currency>, YieldAndDiscountCurve> _issuerCurves;
  /**
   * The set of names of all curves used in the multicurves provider.
   */
  private Set<String> _multicurvesNames;
  /**
   * The set of all curves names. If contains the names of the multi-curves provider curves names and the issuer curves names.
   */
  private final Set<String> _allNames = new TreeSet<>();
  /**
   * The map between the curves names and the curves themselves.
   */
  private final Map<String, YieldAndDiscountCurve> _issuerCurvesNames = new LinkedHashMap<>();

  /**
   * Constructs an empty multi-curve provider and issuer curves map.
   */
  public IssuerProvider() {
    _multicurveProvider = new MulticurveProviderDiscount();
    _issuerCurves = new LinkedHashMap<>();
    init();
  }

  /**
   * Constructs a multi-curve provider with empty maps for discounting and forward curves, and an empty issuer curve map.
   * @param fxMatrix The FX matrix, not null
   */
  public IssuerProvider(final FXMatrix fxMatrix) {
    _multicurveProvider = new MulticurveProviderDiscount(fxMatrix);
    _issuerCurves = new LinkedHashMap<>();
    init();
  }

  /**
   * Constructs a multi-curve provider and an empty issuer curve map.
   * @param discountingCurves A map from currency to yield curve, not null
   * @param forwardIborCurves A map from ibor index to yield curve, not null
   * @param forwardONCurves A map from overnight index to yield curve, not null
   * @param fxMatrix The FX matrix, not null
   */
  //TODO there is no guarantee that the maps are LinkedHashMaps, which could lead to unexpected behaviour
  public IssuerProvider(final Map<Currency, YieldAndDiscountCurve> discountingCurves, final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves,
      final Map<IndexON, YieldAndDiscountCurve> forwardONCurves, final FXMatrix fxMatrix) {
    _multicurveProvider = new MulticurveProviderDiscount(discountingCurves, forwardIborCurves, forwardONCurves, fxMatrix);
    _issuerCurves = new LinkedHashMap<>();
    init();
  }

  /**
   * Constructs a multi-curve provider and sets the issuer curve map. The issuer curve map is not copied
   * @param discountingCurves A map from currency to yield curve, not null
   * @param forwardIborCurves A map from ibor index to yield curve, not null
   * @param forwardONCurves A map from overnight index to yield curve, not null
   * @param issuerCurve An issuer curve map
   * @param fxMatrix The FX matrix, not null
   */
  //TODO there is no guarantee that the map is a LinkedHashMap, which could lead to unexpected behaviour
  public IssuerProvider(final Map<Currency, YieldAndDiscountCurve> discountingCurves, final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves,
      final Map<IndexON, YieldAndDiscountCurve> forwardONCurves, final Map<Pair<String, Currency>, YieldAndDiscountCurve> issuerCurve, final FXMatrix fxMatrix) {
    _multicurveProvider = new MulticurveProviderDiscount(discountingCurves, forwardIborCurves, forwardONCurves, fxMatrix);
    _issuerCurves = issuerCurve;
    init();
  }

  /**
   * Constructor from existing multicurve provider and issuer map. The given provider and map are used for the new provider (the same maps are used, not copied).
   * @param multicurve The multi-curves provider.
   * @param issuerCurves The issuer specific curves.
   */
  public IssuerProvider(final MulticurveProviderInterface multicurve, final Map<Pair<String, Currency>, YieldAndDiscountCurve> issuerCurves) {
    ArgumentChecker.notNull(multicurve, "multicurve");
    ArgumentChecker.notNull(issuerCurves, "issuer curves");
    _multicurveProvider = multicurve;
    _issuerCurves = issuerCurves;
    init();
  }

  /**
   * Constructs a provider from an existing multi-curve provider. The maps are not copied.
   * @param multicurve The multi-curves provider, not null
   */
  public IssuerProvider(final MulticurveProviderDiscount multicurve) {
    ArgumentChecker.notNull(multicurve, "multicurve");
    _multicurveProvider = multicurve;
    _issuerCurves = new LinkedHashMap<>();
    init();
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _multicurveProvider;
  }

  @Override
  public IssuerProviderInterface getIssuerProvider() {
    return this;
  }

  @Override
  public IssuerProvider copy() {
    final Map<Pair<String, Currency>, YieldAndDiscountCurve> issuerCurvesNew = new HashMap<>(_issuerCurves);
    return new IssuerProvider(_multicurveProvider.copy(), issuerCurvesNew);
  }

  protected void init() {
    _multicurvesNames = _multicurveProvider.getAllNames();
    _allNames.addAll(_multicurvesNames);
    final Set<Pair<String, Currency>> issuerSet = _issuerCurves.keySet();
    for (final Pair<String, Currency> issuer : issuerSet) {
      _allNames.add(_issuerCurves.get(issuer).getName());
    }
    final Set<Pair<String, Currency>> icSet = _issuerCurves.keySet();
    for (final Pair<String, Currency> ic : icSet) {
      final String name = _issuerCurves.get(ic).getName();
      _issuerCurvesNames.put(name, _issuerCurves.get(ic));
    }
  }

  @Override
  public double getDiscountFactor(final Pair<String, Currency> issuerCcy, final Double time) {
    if (_issuerCurves.containsKey(issuerCcy)) {
      return _issuerCurves.get(issuerCcy).getDiscountFactor(time);
    }
    throw new IllegalArgumentException("Issuer discounting curve not found: " + issuerCcy);
  }

  @Override
  public String getName(final Pair<String, Currency> issuerCcy) {
    return _issuerCurves.get(issuerCcy).getName();
  }

  @Override
  /**
   * Returns all curves names. The order is ???
   */
  public Set<String> getAllNames() {
    return _allNames;
  }

  @Override
  public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    if (_multicurvesNames.contains(name)) {
      return _multicurveProvider.parameterSensitivity(name, pointSensitivity);
    }
    // TODO: re-factor the code below (to store it in YieldAndDiscountCurve?)
    final YieldAndDiscountCurve curve = _issuerCurvesNames.get(name);
    final int nbParameters = curve.getNumberOfParameters();
    final double[] result = new double[nbParameters];
    if (pointSensitivity != null && !pointSensitivity.isEmpty()) {
      for (final DoublesPair timeAndS : pointSensitivity) {
        final double[] sensi1Point = curve.getInterestRateParameterSensitivity(timeAndS.getFirst());
        for (int loopparam = 0; loopparam < nbParameters; loopparam++) {
          result[loopparam] += timeAndS.getSecond() * sensi1Point[loopparam];
        }
      }
    }
    return result;
  }

  @Override
  public double[] parameterForwardSensitivity(final String name, final List<ForwardSensitivity> pointSensitivity) {
    return _multicurveProvider.parameterForwardSensitivity(name, pointSensitivity);
  }

  @Override
  public Integer getNumberOfParameters(final String name) {
    if (_multicurvesNames.contains(name)) {
      return _multicurveProvider.getNumberOfParameters(name);
    }
    return _issuerCurvesNames.get(name).getNumberOfParameters();
  }

  @Override
  public List<String> getUnderlyingCurvesNames(final String name) {
    if (_multicurvesNames.contains(name)) {
      return _multicurveProvider.getUnderlyingCurvesNames(name);
    }
    return _issuerCurvesNames.get(name).getUnderlyingCurvesNames();
  }

  @Override
  public Set<Pair<String, Currency>> getIssuersCurrencies() {
    return _issuerCurves.keySet();
  }

  public Map<Pair<String, Currency>, YieldAndDiscountCurve> getIssuerCurves() {
    return _issuerCurves;
  }

  public YieldAndDiscountCurve getCurve(final Pair<String, Currency> ic) {
    return _issuerCurves.get(ic);
  }

  /**
   * Sets the discounting curve for a given issuer/currency.
   * @param issuerCcy The issuer/currency.
   * @param curve The yield curve used for discounting.
   */
  public void setCurve(final Pair<String, Currency> issuerCcy, final YieldAndDiscountCurve curve) {
    ArgumentChecker.notNull(issuerCcy, "Issuer/currency");
    ArgumentChecker.notNull(curve, "curve");
    if (_issuerCurves.containsKey(issuerCcy)) {
      throw new IllegalArgumentException("Currency discounting curve already set: " + issuerCcy.toString());
    }
    _issuerCurves.put(issuerCcy, curve);
    init();
  }

  public IssuerProvider withIssuerCurrency(final Pair<String, Currency> ic, final YieldAndDiscountCurve replacement) {
    final Map<Pair<String, Currency>, YieldAndDiscountCurve> newIssuerCurves = new LinkedHashMap<>(_issuerCurves);
    newIssuerCurves.put(ic, replacement);
    return new IssuerProvider(_multicurveProvider, newIssuerCurves);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _issuerCurves.hashCode();
    result = prime * result + _multicurveProvider.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof IssuerProvider)) {
      return false;
    }
    final IssuerProvider other = (IssuerProvider) obj;
    if (!ObjectUtils.equals(_issuerCurves, other._issuerCurves)) {
      return false;
    }
    if (!ObjectUtils.equals(_multicurveProvider, other._multicurveProvider)) {
      return false;
    }
    return true;
  }

}
