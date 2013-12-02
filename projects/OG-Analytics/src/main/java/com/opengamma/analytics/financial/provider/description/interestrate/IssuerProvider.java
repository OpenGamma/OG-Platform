/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityMeta;
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
//  private final Map<Pair<String, Currency>, YieldAndDiscountCurve> _issuerCurves;
  private final Map<Pair<Object, LegalEntityMeta<LegalEntity>>, YieldAndDiscountCurve> _issuerCurves;
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
  //TODO these aren't the best names
  private final Map<String, YieldAndDiscountCurve> _issuerCurvesNames = new LinkedHashMap<>();

  /**
   * Constructs an empty multi-curve provider and issuer curves map.
   */
  public IssuerProvider() {
    _multicurveProvider = new MulticurveProviderDiscount();
    _issuerCurves = new LinkedHashMap<>();
    setAllCurves();
  }

  /**
   * Constructs a multi-curve provider with empty maps for discounting and forward curves, and an empty issuer curve map.
   * @param fxMatrix The FX matrix, not null
   */
  public IssuerProvider(final FXMatrix fxMatrix) {
    _multicurveProvider = new MulticurveProviderDiscount(fxMatrix);
    _issuerCurves = new LinkedHashMap<>();
    setAllCurves();
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
    setAllCurves();
  }

  /**
   * Constructs a multi-curve provider and sets the issuer curve map. The issuer curve map is not copied.
   * @param discountingCurves A map from currency to yield curve, not null
   * @param forwardIborCurves A map from ibor index to yield curve, not null
   * @param forwardONCurves A map from overnight index to yield curve, not null
   * @param issuerCurves An issuer curve map, not null
   * @param fxMatrix The FX matrix, not null
   */
  //TODO there is no guarantee that the map is a LinkedHashMap, which could lead to unexpected behaviour
  public IssuerProvider(final Map<Currency, YieldAndDiscountCurve> discountingCurves, final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves,
      final Map<IndexON, YieldAndDiscountCurve> forwardONCurves, final Map<Pair<Object, LegalEntityMeta<LegalEntity>>, YieldAndDiscountCurve> issuerCurves, final FXMatrix fxMatrix) {
    ArgumentChecker.notNull(issuerCurves, "issuer curves");
    _multicurveProvider = new MulticurveProviderDiscount(discountingCurves, forwardIborCurves, forwardONCurves, fxMatrix);
    _issuerCurves = issuerCurves;
    setAllCurves();
  }

  /**
   * Constructor from existing multicurve provider and issuer map. The given provider and map are used for the new provider (the same maps are used, not copied).
   * @param multicurve The multi-curves provider, not null
   * @param issuerCurves The issuer specific curves, not null
   */
  public IssuerProvider(final MulticurveProviderInterface multicurve, final Map<Pair<Object, LegalEntityMeta<LegalEntity>>, YieldAndDiscountCurve> issuerCurves) {
    ArgumentChecker.notNull(multicurve, "multicurve");
    ArgumentChecker.notNull(issuerCurves, "issuer curves");
    _multicurveProvider = multicurve;
    _issuerCurves = issuerCurves;
    setAllCurves();
  }

  /**
   * Constructs a provider from an existing multi-curve provider. The maps are not copied.
   * @param multicurve The multi-curves provider, not null
   */
  public IssuerProvider(final MulticurveProviderDiscount multicurve) {
    ArgumentChecker.notNull(multicurve, "multicurve");
    _multicurveProvider = multicurve;
    _issuerCurves = new LinkedHashMap<>();
    setAllCurves();
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
    final Map<Pair<Object, LegalEntityMeta<LegalEntity>>, YieldAndDiscountCurve> issuerCurvesNew = new LinkedHashMap<>(_issuerCurves);
    return new IssuerProvider(_multicurveProvider.copy(), issuerCurvesNew);
  }

  /**
   * Sets all curves.
   */
  protected void setAllCurves() {
    _multicurvesNames = _multicurveProvider.getAllNames();
    _allNames.addAll(_multicurvesNames);
    for (final Map.Entry<Pair<Object, LegalEntityMeta<LegalEntity>>, YieldAndDiscountCurve> entry : _issuerCurves.entrySet()) {
      _allNames.add(entry.getValue().getName());
      _issuerCurvesNames.put(entry.getValue().getName(), entry.getValue());
    }
  }

  @Override
  public double getDiscountFactor(final LegalEntity issuer, final Double time) {
    for (final Map.Entry<Pair<Object, LegalEntityMeta<LegalEntity>>, YieldAndDiscountCurve> entry : _issuerCurves.entrySet()) {
      if (entry.getKey().getFirst().equals(entry.getKey().getSecond().getMetaData(issuer))) {
        return entry.getValue().getDiscountFactor(time);
      }
    }
    throw new IllegalArgumentException("Issuer discounting curve not found for " + issuer);
  }

//  @Override
//  public String getName(final Pair<String, Currency> issuerCcy) {
//    return _issuerCurves.get(issuerCcy).getName();
//  }
//
  @Override
  public String getName(final Pair<Object, LegalEntityMeta<LegalEntity>> issuer) {
    return _issuerCurves.get(issuer).getName();
  }

  @Override
  public String getName(final LegalEntity issuer) {
    for (final Map.Entry<Pair<Object, LegalEntityMeta<LegalEntity>>, YieldAndDiscountCurve> entry : _issuerCurves.entrySet()) {
      if (entry.getKey().getFirst().equals(entry.getKey().getSecond().getMetaData(issuer))) {
        return entry.getValue().getName();
      }
    }
    throw new IllegalArgumentException("Issuer discounting curve not found: " + issuer);
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

//  @Override
//  public Set<Pair<String, Currency>> getIssuersCurrencies() {
//    return _issuerCurves.keySet();
//  }

  @Override
  public Set<Pair<Object, LegalEntityMeta<LegalEntity>>> getIssuers() {
    return _issuerCurves.keySet();
  }

  public Map<Pair<Object, LegalEntityMeta<LegalEntity>>, YieldAndDiscountCurve> getIssuerCurves() {
    return _issuerCurves;
  }

//  public YieldAndDiscountCurve getCurve(final Pair<String, Currency> ic) {
//    return _issuerCurves.get(ic);
//  }

  public YieldAndDiscountCurve getCurve(final Object key) {
    return _issuerCurves.get(key);
  }

//  public <T extends Obligor> YieldAndDiscountCurve getCurveForObligorMeta(final Pair<T, ObligorMeta<T>> key) {
//    final Object meta = key.getSecond().getMetaData(key.getFirst());
//    return _issuerCurves.get(meta);
//  }

//  /**
//   * Sets the discounting curve for a given issuer/currency.
//   * @param issuerCcy The issuer/currency.
//   * @param curve The yield curve used for discounting.
//   */
//  public void setCurve(final Pair<String, Currency> issuerCcy, final YieldAndDiscountCurve curve) {
//    ArgumentChecker.notNull(issuerCcy, "Issuer/currency");
//    ArgumentChecker.notNull(curve, "curve");
//    if (_issuerCurves.containsKey(issuerCcy)) {
//      throw new IllegalArgumentException("Currency discounting curve already set: " + issuerCcy.toString());
//    }
//    _issuerCurves.put(issuerCcy, curve);
//    setAllCurves();
//  }

  /**
   * Sets the discounting curve for a given issuer/currency.
   * @param issuerCcy The issuer/currency.
   * @param curve The yield curve used for discounting.
   */
  public void setCurve(final Pair<Object, LegalEntityMeta<LegalEntity>> issuerCcy, final YieldAndDiscountCurve curve) {
    ArgumentChecker.notNull(issuerCcy, "Issuer/currency");
    ArgumentChecker.notNull(curve, "curve");
    if (_issuerCurves.containsKey(issuerCcy)) {
      throw new IllegalArgumentException("Currency discounting curve already set: " + issuerCcy.toString());
    }
    _issuerCurves.put(issuerCcy, curve);
    setAllCurves();
  }

//  public IssuerProvider withIssuerCurrency(final Pair<String, Currency> ic, final YieldAndDiscountCurve replacement) {
//    final Map<Pair<String, Currency>, YieldAndDiscountCurve> newIssuerCurves = new LinkedHashMap<>(_issuerCurves);
//    newIssuerCurves.put(ic, replacement);
//    return new IssuerProvider(_multicurveProvider, newIssuerCurves);
//  }

  public IssuerProvider withIssuerCurrency(final Pair<Object, LegalEntityMeta<LegalEntity>> ic, final YieldAndDiscountCurve replacement) {
    final Map<Pair<Object, LegalEntityMeta<LegalEntity>>, YieldAndDiscountCurve> newIssuerCurves = new LinkedHashMap<>(_issuerCurves);
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
