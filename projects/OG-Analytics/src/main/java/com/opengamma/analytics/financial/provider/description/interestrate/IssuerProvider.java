/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
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
  private static final Logger s_logger = LoggerFactory.getLogger(IssuerProvider.class);

  /**
   * The multicurve provider.
   */
  private final MulticurveProviderInterface _multicurveProvider;
  /**
   * A map with issuer discounting curves.
   */
  private final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> _issuerCurves;
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
      final Map<IndexON, YieldAndDiscountCurve> forwardONCurves, final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> issuerCurves, final FXMatrix fxMatrix) {
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
  public IssuerProvider(final MulticurveProviderInterface multicurve, final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> issuerCurves) {
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
    final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> issuerCurvesNew = new LinkedHashMap<>(_issuerCurves);
    return new IssuerProvider(_multicurveProvider.copy(), issuerCurvesNew);
  }

  /**
   * Sets all curves.
   */
  protected void setAllCurves() {
    _multicurvesNames = _multicurveProvider.getAllNames();
    _allNames.addAll(_multicurvesNames);
    for (final Map.Entry<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> entry : _issuerCurves.entrySet()) {
      if (entry.getValue() == null) {
        throw new OpenGammaRuntimeException("Curve with key " + entry.getValue() + " was null");
      }
      _allNames.add(entry.getValue().getName());
      _issuerCurvesNames.put(entry.getValue().getName(), entry.getValue());
    }
  }

  @Override
  public double getDiscountFactor(final LegalEntity issuer, final Double time) {
    for (final Map.Entry<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> entry : _issuerCurves.entrySet()) {
      if (entry.getKey().getFirst().equals(entry.getKey().getSecond().getFilteredData(issuer))) {
        return entry.getValue().getDiscountFactor(time);
      }
    }
    s_logger.error("Could not find issuer discounting curve for {}. There are {} curve available", issuer, _issuerCurves.size());
    for (final Map.Entry<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> entry : _issuerCurves.entrySet()) {
      s_logger.error("matching key = {}, filter {} matches = {}", entry.getKey().getFirst(), issuer, entry.getKey().getSecond().getFilteredData(issuer));
    }
    throw new IllegalArgumentException("Issuer discounting curve not found for " + issuer);
  }

  @Override
  public String getName(final Pair<Object, LegalEntityFilter<LegalEntity>> issuer) {
    return _issuerCurves.get(issuer).getName();
  }

  @Override
  public String getName(final LegalEntity issuer) {
    for (final Map.Entry<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> entry : _issuerCurves.entrySet()) {
      if (entry.getKey().getFirst().equals(entry.getKey().getSecond().getFilteredData(issuer))) {
        return entry.getValue().getName();
      }
    }
    s_logger.error("Could not find issuer discounting curve for {}. There are {} curve available", issuer, _issuerCurves.size());
    for (final Map.Entry<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> entry : _issuerCurves.entrySet()) {
      s_logger.error("matching key = {}, filter {} matches = {}", entry.getKey().getFirst(), issuer, entry.getKey().getSecond().getFilteredData(issuer));
    }
    throw new IllegalArgumentException("Issuer discounting curve not found: " + issuer);
  }

  @Override
  public Set<String> getAllNames() {
    return getAllCurveNames();
  }

  @Override
  public Set<String> getAllCurveNames() {
    return Collections.unmodifiableSortedSet(new TreeSet<>(_allNames));
  }

  @Override
  public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    if (_multicurvesNames.contains(name)) {
      return _multicurveProvider.parameterSensitivity(name, pointSensitivity);
    }
    final YieldAndDiscountCurve curve = _issuerCurvesNames.get(name);
    if (curve == null) {
      throw new IllegalArgumentException("Could not get curve called " + name);
    }
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
    if (_multicurvesNames.contains(name)) {
      return _multicurveProvider.parameterForwardSensitivity(name, pointSensitivity);
    }
    final YieldAndDiscountCurve curve = _issuerCurvesNames.get(name);
    if (curve == null) {
      throw new IllegalArgumentException("Could not get curve called " + name);
    }
    final int nbParameters = curve.getNumberOfParameters();
    final double[] result = new double[nbParameters];
    if (pointSensitivity != null && pointSensitivity.size() > 0) {
      for (final ForwardSensitivity timeAndS : pointSensitivity) {
        final double startTime = timeAndS.getStartTime();
        final double endTime = timeAndS.getEndTime();
        final double forwardBar = timeAndS.getValue();
        // Implementation note: only the sensitivity to the forward is available. The sensitivity to the pseudo-discount factors need to be computed.
        final double dfForwardStart = curve.getDiscountFactor(startTime);
        final double dfForwardEnd = curve.getDiscountFactor(endTime);
        final double dFwddyStart = timeAndS.derivativeToYieldStart(dfForwardStart, dfForwardEnd);
        final double dFwddyEnd = timeAndS.derivativeToYieldEnd(dfForwardStart, dfForwardEnd);
        final double[] sensiPtStart = curve.getInterestRateParameterSensitivity(startTime);
        final double[] sensiPtEnd = curve.getInterestRateParameterSensitivity(endTime);
        for (int loopparam = 0; loopparam < nbParameters; loopparam++) {
          result[loopparam] += dFwddyStart * sensiPtStart[loopparam] * forwardBar;
          result[loopparam] += dFwddyEnd * sensiPtEnd[loopparam] * forwardBar;
        }
      }
    }
    return result;
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
  public Set<Pair<Object, LegalEntityFilter<LegalEntity>>> getIssuers() {
    return _issuerCurves.keySet();
  }

  /**
   * Gets all issuer curves.
   * @return The issuer curves
   */
  public Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> getIssuerCurves() {
    return _issuerCurves;
  }

  /**
   * Gets an issuer curve.
   * @param key The key
   * @return The curve.
   */
  public YieldAndDiscountCurve getIssuerCurve(final Pair<Object, LegalEntityFilter<LegalEntity>> key) {
    return _issuerCurves.get(key);
  }

  /**
   * Gets a named issuer curve.
   * @param name The name
   * @return The curve.
   */
  public YieldAndDiscountCurve getIssuerCurve(final String name) {
    return _issuerCurvesNames.get(name);
  }

  /**
   * Sets the discounting curve for a given issuer.
   * @param issuerCcy The issuer/currency.
   * @param curve The yield curve used for discounting.
   */
  public void setCurve(final Pair<Object, LegalEntityFilter<LegalEntity>> issuerCcy, final YieldAndDiscountCurve curve) {
    ArgumentChecker.notNull(issuerCcy, "Issuer/currency");
    ArgumentChecker.notNull(curve, "curve");
    if (_issuerCurves.containsKey(issuerCcy)) {
      throw new IllegalArgumentException("Currency discounting curve already set: " + issuerCcy.toString());
    }
    _issuerCurves.put(issuerCcy, curve);
    setAllCurves();
  }

  /**
   * Replaces an issuer curve.
   * @param ic The key of the curve to replace
   * @param replacement The replacement curve
   * @return A new provider with the curve replaced.
   */
  public IssuerProvider withIssuerCurve(final Pair<Object, LegalEntityFilter<LegalEntity>> ic, final YieldAndDiscountCurve replacement) {
    final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> newIssuerCurves = new LinkedHashMap<>(_issuerCurves);
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
