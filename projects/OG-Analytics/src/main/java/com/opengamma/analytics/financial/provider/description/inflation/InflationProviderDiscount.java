/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.inflation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing a "market" with discounting, forward, price index and credit curves.
 * The forward rate are computed as the ratio of discount factors stored in {@link YieldAndDiscountCurve}.
 */
public class InflationProviderDiscount implements InflationProviderInterface {

  /**
   * The multicurve provider.
   */
  private final MulticurveProviderDiscount _multicurveProvider;
  /**
   * A map with one price curve by price index.
   */
  private final Map<IndexPrice, PriceIndexCurve> _priceIndexCurves;
  /**
   * Map of all inflation curves used in the provider.
   */
  private Map<String, PriceIndexCurve> _allPriceIndexCurves;

  /**
   * Constructs an empty multi-curve provider and price index curve map.
   */
  public InflationProviderDiscount() {
    _multicurveProvider = new MulticurveProviderDiscount();
    _priceIndexCurves = new LinkedHashMap<>();
    setInflationCurves();
  }

  /**
   * Constructor with empty maps for discounting, forward and price index curves.
   * @param fxMatrix The FXMatrix, not null
   */
  public InflationProviderDiscount(final FXMatrix fxMatrix) {
    _multicurveProvider = new MulticurveProviderDiscount(fxMatrix);
    _priceIndexCurves = new LinkedHashMap<>();
    setInflationCurves();
  }

  /**
   * Constructor from an existing market. The given market maps are used for the new market (the same maps are used, not copied).
   * @param discountingCurves A map with one (discounting) curve by currency, not null
   * @param forwardIborCurves A map with one (forward) curve by Ibor index, not null
   * @param forwardONCurves A map with one (forward) curve by ON index, not null
   * @param priceIndexCurves A map with one price curve by price index, not null
   * @param fxMatrix The FXMatrix.
   */
  //TODO there is no guarantee that the maps are LinkedHashMaps, which could lead to unexpected behaviour
  public InflationProviderDiscount(final Map<Currency, YieldAndDiscountCurve> discountingCurves, final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves,
      final Map<IndexON, YieldAndDiscountCurve> forwardONCurves, final Map<IndexPrice, PriceIndexCurve> priceIndexCurves, final FXMatrix fxMatrix) {
    ArgumentChecker.notNull(priceIndexCurves, "price index curve");
    _multicurveProvider = new MulticurveProviderDiscount(discountingCurves, forwardIborCurves, forwardONCurves, fxMatrix);
    _priceIndexCurves = priceIndexCurves;
    setInflationCurves();
  }

  /**
   * Constructor from an existing market without price index (inflation) curve. The given market maps are used for the new market (the same maps are used, not copied).
   * @param discountingCurves A map with one (discounting) curve by currency, not null
   * @param forwardIborCurves A map with one (forward) curve by Ibor index, not null
   * @param forwardONCurves A map with one (forward) curve by ON index, not null
   * @param fxMatrix The FXMatrix.
   */
  public InflationProviderDiscount(final Map<Currency, YieldAndDiscountCurve> discountingCurves, final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves,
      final Map<IndexON, YieldAndDiscountCurve> forwardONCurves, final FXMatrix fxMatrix) {
    _multicurveProvider = new MulticurveProviderDiscount(discountingCurves, forwardIborCurves, forwardONCurves, fxMatrix);
    _priceIndexCurves = new LinkedHashMap<>();
    setInflationCurves();
  }

  /**
   * Constructor from existing multicurveProvider and inflation map. The given provider and map are used for the new provider (the same maps are used, not copied).
   * @param multicurve The multi-curves provider, not null
   * @param priceIndexCurves The map with price index curves, not null
   */
  //TODO there is no guarantee that the map is a LinkedHashMap, which could lead to unexpected behaviour
  public InflationProviderDiscount(final MulticurveProviderDiscount multicurve, final Map<IndexPrice, PriceIndexCurve> priceIndexCurves) {
    ArgumentChecker.notNull(multicurve, "multicurve");
    ArgumentChecker.notNull(priceIndexCurves, "priceIndexCurves");
    _multicurveProvider = multicurve;
    _priceIndexCurves = priceIndexCurves;
    setInflationCurves();
  }

  /**
   * Constructor from existing multi-curve provider. The given provider and map are used for the new provider (the same maps are used, not copied).
   * @param multicurve The multi-curves provider, not null
   */
  public InflationProviderDiscount(final MulticurveProviderDiscount multicurve) {
    ArgumentChecker.notNull(multicurve, "multicurve");
    _multicurveProvider = multicurve;
    _priceIndexCurves = new LinkedHashMap<>();
    setInflationCurves();
  }

  /**
   * Adds all inflation curves to a map with (curve name, curve) elements.
   */
  private void setInflationCurves() {
    _allPriceIndexCurves = new LinkedHashMap<>();
    final Set<IndexPrice> inflationIndexSet = _priceIndexCurves.keySet();
    for (final IndexPrice index : inflationIndexSet) {
      final String name = _priceIndexCurves.get(index).getName();
      _allPriceIndexCurves.put(name, _priceIndexCurves.get(index));
    }
  }

  @Override
  public InflationProviderDiscount copy() {
    final MulticurveProviderDiscount multicurveProvider = _multicurveProvider.copy();
    final LinkedHashMap<IndexPrice, PriceIndexCurve> priceIndexCurves = new LinkedHashMap<>(_priceIndexCurves);
    return new InflationProviderDiscount(multicurveProvider, priceIndexCurves);
  }

  @Override
  public double getPriceIndex(final IndexPrice index, final Double time) {
    if (_priceIndexCurves.containsKey(index)) {
      return _priceIndexCurves.get(index).getPriceIndex(time);
    }
    throw new IllegalArgumentException("Price index curve not found: " + index);
  }

  @Override
  public String getName(final IndexPrice index) {
    if (_priceIndexCurves.containsKey(index)) {
      return _priceIndexCurves.get(index).getName();
    }
    throw new IllegalArgumentException("Price index curve not found: " + index);
  }

  /**
   * Gets the price index curve associated to a given price index in the market.
   * @param index The Price index.
   * @return The curve.
   */
  public PriceIndexCurve getCurve(final IndexPrice index) {
    if (_priceIndexCurves.containsKey(index)) {
      return _priceIndexCurves.get(index);
    }
    throw new IllegalArgumentException("Price index curve not found: " + index);
  }

  /**
   * Gets the price index curve associated to a given name.
   * @param name The name of the Price index.
   * @return The curve.
   */
  public PriceIndexCurve getCurve(final String name) {
    return _allPriceIndexCurves.get(name);
  }

  @Override
  public Set<IndexPrice> getPriceIndexes() {
    return _priceIndexCurves.keySet();
  }

  /**
   * Gets the price index curve map. keys are PriceIndex
   * @return An unmodifiable copy of the price index curve map
   */
  public Map<IndexPrice, PriceIndexCurve> getPriceIndexCurves() {
    return Collections.unmodifiableMap(_priceIndexCurves);
  }

  /**
   * Sets the price index curve for a price index.
   * @param index The price index.
   * @param curve The curve.
   */
  public void setCurve(final IndexPrice index, final PriceIndexCurve curve) {
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(curve, "curve");
    if (_priceIndexCurves.containsKey(index)) {
      throw new IllegalArgumentException("Price index curve already set: " + index.toString());
    }
    _priceIndexCurves.put(index, curve);
    setInflationCurves();
  }

  @Override
  public MulticurveProviderDiscount getMulticurveProvider() {
    return _multicurveProvider;
  }

  /**
   * Returns a new provider with the curve for a price index replaced by the input curve.
   * @param index The index, not null
   * @param replacement The replacement curve, not null
   * @return A new provider with the curve replaced
   */
  public InflationProviderDiscount withPriceIndex(final IndexPrice index, final PriceIndexCurve replacement) {
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(replacement, "replacement");
    final Map<IndexPrice, PriceIndexCurve> newPriceIndexCurves = new LinkedHashMap<>(_priceIndexCurves);
    newPriceIndexCurves.put(index, replacement);
    final InflationProviderDiscount decorated = new InflationProviderDiscount(_multicurveProvider, newPriceIndexCurves);
    return decorated;
  }

  //     =====     Methods related to MulticurveProvider     =====

  @Override
  public double getDiscountFactor(final Currency ccy, final Double time) {
    return _multicurveProvider.getDiscountFactor(ccy, time);
  }

  @Override
  public String getName(final Currency ccy) {
    return _multicurveProvider.getName(ccy);
  }

  @Override
  public Set<Currency> getCurrencies() {
    return _multicurveProvider.getCurrencies();
  }

  @Override
  public double getForwardRate(final IborIndex index, final double startTime, final double endTime, final double accrualFactor) {
    return _multicurveProvider.getSimplyCompoundForwardRate(index, startTime, endTime, accrualFactor);
  }

  @Override
  public String getName(final IborIndex index) {
    return _multicurveProvider.getName(index);
  }

  @Override
  public Set<IborIndex> getIndexesIbor() {
    return _multicurveProvider.getIndexesIbor();
  }

  @Override
  public double getForwardRate(final IndexON index, final double startTime, final double endTime, final double accrualFactor) {
    return _multicurveProvider.getSimplyCompoundForwardRate(index, startTime, endTime, accrualFactor);
  }

  @Override
  public String getName(final IndexON index) {
    return _multicurveProvider.getName(index);
  }

  @Override
  public Set<IndexON> getIndexesON() {
    return _multicurveProvider.getIndexesON();
  }

  /**
   * Gets the discounting curve associated in a given currency in the market.
   * @param ccy The currency.
   * @return The curve.
   */
  public YieldAndDiscountCurve getCurve(final Currency ccy) {
    return _multicurveProvider.getCurve(ccy);
  }

  /**
   * Gets the forward curve associated to a given Ibor index in the market.
   * @param index The Ibor index.
   * @return The curve.
   */
  public YieldAndDiscountCurve getCurve(final IborIndex index) {
    return _multicurveProvider.getCurve(index);
  }

  /**
   * Gets the forward curve associated to a given ON index in the market.
   * @param index The ON index.
   * @return The curve.
   */
  public YieldAndDiscountCurve getCurve(final IndexON index) {
    return _multicurveProvider.getCurve(index);
  }

  @Override
  public Set<String> getAllCurveNames() {
    final TreeSet<String> allNames = new TreeSet<>(_multicurveProvider.getAllCurveNames());
    allNames.addAll(_allPriceIndexCurves.keySet());
    return Collections.unmodifiableSortedSet(allNames);
  }

  @Override
  public Set<String> getAllNames() {
    return getAllCurveNames();
  }

  /**
   * Sets the discounting curve for a given currency.
   * @param ccy The currency.
   * @param curve The yield curve used for discounting.
   */
  public void setCurve(final Currency ccy, final YieldAndDiscountCurve curve) {
    _multicurveProvider.setCurve(ccy, curve);
  }

  /**
   * Sets the curve associated to an Ibor index.
   * @param index The index.
   * @param curve The curve.
   */
  public void setCurve(final IborIndex index, final YieldAndDiscountCurve curve) {
    _multicurveProvider.setCurve(index, curve);
  }

  /**
   * Sets the curve associated to an ON index.
   * @param index The index.
   * @param curve The curve.
   */
  public void setCurve(final IndexON index, final YieldAndDiscountCurve curve) {
    _multicurveProvider.setCurve(index, curve);
  }

  /**
   * Set all the curves contains in another bundle. If a currency or index is already present in the map, the associated curve is changed.
   * @param other The other bundle.
   */
  // TODO: REVIEW: Should we check that the curve are already present?
  public void setAll(final InflationProviderDiscount other) {
    ArgumentChecker.notNull(other, "Inflation provider");
    _multicurveProvider.setAll(other.getMulticurveProvider());
    _priceIndexCurves.putAll(other._priceIndexCurves);
    setInflationCurves();
  }

  /**
   * Replaces the discounting curve for a given currency.
   * @param ccy The currency.
   * @param curve The yield curve used for discounting.
   * @throws IllegalArgumentException if curve name NOT already present
   */
  public void replaceCurve(final Currency ccy, final YieldAndDiscountCurve curve) {
    _multicurveProvider.replaceCurve(ccy, curve);
  }

  /**
   * Replaces the forward curve for a given index.
   * @param index The index.
   * @param curve The yield curve used for forward.
   * @throws IllegalArgumentException if curve name NOT already present
   */
  public void replaceCurve(final IborIndex index, final YieldAndDiscountCurve curve) {
    _multicurveProvider.replaceCurve(index, curve);
  }

  /**
   * Replaces the discounting curve for a price index.
   * @param index The price index.
   * @param curve The price curve for the index.
   * @throws IllegalArgumentException if curve name NOT already present
   */
  public void replaceCurve(final IndexPrice index, final PriceIndexCurve curve) {
    ArgumentChecker.notNull(index, "Price index");
    ArgumentChecker.notNull(curve, "curve");
    if (!_priceIndexCurves.containsKey(index)) {
      throw new IllegalArgumentException("Price index curve not in set: " + index);
    }
    _priceIndexCurves.put(index, curve);
  }

  @Override
  public Integer getNumberOfParameters(final String name) {
    final PriceIndexCurve inflationCurve = _allPriceIndexCurves.get(name);
    final YieldAndDiscountCurve curve = _multicurveProvider.getCurve(name);
    if (inflationCurve != null) {
      return inflationCurve.getNumberOfParameters();
    } else if (curve != null) {
      return curve.getNumberOfParameters();
    }
    throw new UnsupportedOperationException("Cannot return the number of parameter for a null curve");
  }

  /**
   * Return the number of intrinsic parameters for the definition of the curve. Which is the total number of parameters minus the parameters of the curves in curvesNames (If they are in curves).
   *  @param name the name of the curve.
   *  @param curvesNames The list of curves names.
   *  @return The number of parameters.
   */
  public Integer getNumberOfIntrinsicParameters(final String name, final Set<String> curvesNames) {
    final PriceIndexCurve inflationCurve = _allPriceIndexCurves.get(name);
    final YieldAndDiscountCurve curve = _multicurveProvider.getCurve(name);
    if (inflationCurve != null) {
      return inflationCurve.getNumberOfIntrinsicParameters(curvesNames);
    } else if (curve != null) {
      return curve.getNumberOfIntrinsicParameters(curvesNames);
    }
    throw new UnsupportedOperationException("Cannot return the number of parameter for a null curve");
  }

  @Override
  public List<String> getUnderlyingCurvesNames(final String name) {
    final PriceIndexCurve inflationCurve = _allPriceIndexCurves.get(name);
    final YieldAndDiscountCurve curve = _multicurveProvider.getCurve(name);
    if (inflationCurve != null) {
      return inflationCurve.getUnderlyingCurvesNames();
    } else if (curve != null) {
      return curve.getUnderlyingCurvesNames();
    }
    throw new UnsupportedOperationException("Cannot return the number of parameter for a null curve");
  }

  @Override
  public double getFxRate(final Currency ccy1, final Currency ccy2) {
    return _multicurveProvider.getFxRate(ccy1, ccy2);
  }

  /**
   * Gets the underlying FXMatrix containing the exchange rates.
   * @return The matrix.
   */
  @Override
  public FXMatrix getFxRates() {
    return _multicurveProvider.getFxRates();
  }

  @Override
  public InflationProviderDiscount withDiscountFactor(final Currency ccy, final YieldAndDiscountCurve replacement) {
    final MulticurveProviderDiscount decoratedMulticurve = _multicurveProvider.withDiscountFactor(ccy, replacement);
    return new InflationProviderDiscount(decoratedMulticurve, _priceIndexCurves);
  }

  @Override
  public InflationProviderDiscount withForward(final IborIndex index, final YieldAndDiscountCurve replacement) {
    final MulticurveProviderDiscount decoratedMulticurve = _multicurveProvider.withForward(index, replacement);
    return new InflationProviderDiscount(decoratedMulticurve, _priceIndexCurves);
  }

  @Override
  public InflationProviderDiscount withForward(final IndexON index, final YieldAndDiscountCurve replacement) {
    final MulticurveProviderDiscount decoratedMulticurve = _multicurveProvider.withForward(index, replacement);
    return new InflationProviderDiscount(decoratedMulticurve, _priceIndexCurves);
  }

  @Override
  public double[] parameterInflationSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    final PriceIndexCurve curve = _allPriceIndexCurves.get(name);
    if (curve == null) {
      return _multicurveProvider.parameterSensitivity(name, pointSensitivity);
    }
    final int nbParameters = curve.getNumberOfParameters();
    final double[] result = new double[nbParameters];
    if (pointSensitivity != null && pointSensitivity.size() > 0) {
      for (final DoublesPair timeAndS : pointSensitivity) {
        final double[] sensi1Point = curve.getPriceIndexParameterSensitivity(timeAndS.getFirst());
        for (int loopparam = 0; loopparam < nbParameters; loopparam++) {
          result[loopparam] += timeAndS.getSecond() * sensi1Point[loopparam];
        }
      }
    }
    return result;
  }

  @Override
  public InflationProviderInterface getInflationProvider() {
    return this;
  }

  @Override
  public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    return _multicurveProvider.parameterSensitivity(name, pointSensitivity);
  }

  @Override
  public double[] parameterForwardSensitivity(final String name, final List<ForwardSensitivity> pointSensitivity) {
    final PriceIndexCurve curve = _allPriceIndexCurves.get(name);
    if (curve == null) {
      return _multicurveProvider.parameterForwardSensitivity(name, pointSensitivity);
    }
    final int nbParameters = curve.getNumberOfParameters();
    final double[] result = new double[nbParameters];
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _multicurveProvider.hashCode();
    result = prime * result + _priceIndexCurves.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof InflationProviderDiscount)) {
      return false;
    }
    final InflationProviderDiscount other = (InflationProviderDiscount) obj;
    if (!ObjectUtils.equals(_multicurveProvider, other._multicurveProvider)) {
      return false;
    }
    if (!ObjectUtils.equals(_priceIndexCurves, other._priceIndexCurves)) {
      return false;
    }
    return true;
  }

}
