/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.inflation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * Class describing a "market" with discounting, forward, price index and credit curves.
 * The forward rate are computed as the ratio of discount factors stored in YieldAndDiscountCurve.
 */
public class InflationIssuerProviderDiscount implements InflationIssuerProviderInterface {
  private static final Logger s_logger = LoggerFactory.getLogger(InflationIssuerProviderDiscount.class);
  /**
   * The multicurve provider.
   */
  private final InflationProviderDiscount _inflationProvider;
  /**
   * A map with issuer discounting curves.
   */
  private final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> _issuerCurves;

  /**
   * The set of names of all curves used in the inflation provider.
   */
  private Set<String> _inflationNames;
  /**
   * The set of all curves names. If contains the names of the inflation provider curves names and the issuer curves names.
   */
  private final Set<String> _allNames = new TreeSet<>();
  /**
   * The map between the curves names and the curves themselves.
   */
  //TODO these aren't the best names
  private final Map<String, YieldAndDiscountCurve> _issuerCurvesNames = new LinkedHashMap<>();

  /**
   * Constructor with empty maps for discounting, forward and price index.
   */
  public InflationIssuerProviderDiscount() {
    _inflationProvider = new InflationProviderDiscount();
    _issuerCurves = new LinkedHashMap<>();
    setAllCurves();
  }

  /**
   * Constructor with empty maps for discounting, forward and price index.
   * @param fxMatrix The FXMatrix.
   */
  public InflationIssuerProviderDiscount(final FXMatrix fxMatrix) {
    _inflationProvider = new InflationProviderDiscount(fxMatrix);
    _issuerCurves = new LinkedHashMap<>();
    setAllCurves();
  }

  /**
   * Constructor from an existing market. The given market maps are used for the new market (the same maps are used, not copied).
   * @param discountingCurves A map with one (discounting) curve by currency.
   * @param forwardIborCurves A map with one (forward) curve by Ibor index.
   * @param forwardONCurves A map with one (forward) curve by ON index.
   * @param priceIndexCurves A map with one price curve by price index.
   * @param issuerCurves A map with issuer discounting curves.
   * @param fxMatrix The FXMatrix.
   */
  public InflationIssuerProviderDiscount(final Map<Currency, YieldAndDiscountCurve> discountingCurves, final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves,
      final Map<IndexON, YieldAndDiscountCurve> forwardONCurves, final Map<IndexPrice, PriceIndexCurve> priceIndexCurves,
      final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> issuerCurves, final FXMatrix fxMatrix) {
    _inflationProvider = new InflationProviderDiscount(discountingCurves, forwardIborCurves, forwardONCurves, priceIndexCurves, fxMatrix);
    _issuerCurves = issuerCurves;
    setAllCurves();
  }

  /**
   * Constructor from exiting multicurveProvider and inflation map. The given provider and map are used for the new provider (the same maps are used, not copied).
   * @param inflation The inflation provider.
   * @param issuerCurves A map with issuer discounting curves.
   */
  public InflationIssuerProviderDiscount(final InflationProviderDiscount inflation, final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> issuerCurves) {
    _inflationProvider = inflation;
    _issuerCurves = issuerCurves;
    setAllCurves();
  }

  /**
   * Constructor from exiting multicurveProvider and inflation map. The given provider and map are used for the new provider (the same maps are used, not copied).
   * @param inflation The inflation provider.
   * @param issuerProvider A map with issuer discounting curves.
   */
  public InflationIssuerProviderDiscount(final InflationProviderDiscount inflation, final IssuerProviderDiscount issuerProvider) {
    _inflationProvider = inflation;
    _issuerCurves = issuerProvider.getIssuerCurves();
    setAllCurves();
  }

  /**
   * Constructor from exiting issuerProvider. The given provider and map are used for the new provider (the same maps are used, not copied).
   * @param issuerProvider A map with issuer discounting curves.
   */
  public InflationIssuerProviderDiscount(final IssuerProviderDiscount issuerProvider) {
    _inflationProvider = new InflationProviderDiscount();
    _inflationProvider.setAll(new InflationProviderDiscount(issuerProvider.getMulticurveProvider()));
    _issuerCurves = issuerProvider.getIssuerCurves();
    setAllCurves();
  }

  @Override
  public InflationIssuerProviderDiscount copy() {
    final InflationProviderDiscount inflationProvider = _inflationProvider.copy();
    final LinkedHashMap<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> issuerCurves = new LinkedHashMap<>(_issuerCurves);
    return new InflationIssuerProviderDiscount(inflationProvider, issuerCurves);
  }

  /**
   * Sets all curves.
   */
  protected void setAllCurves() {
    _inflationNames = _inflationProvider.getAllCurveNames();
    _allNames.addAll(_inflationNames);
    _allNames.addAll(_inflationProvider.getMulticurveProvider().getAllCurveNames());
    for (final Map.Entry<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> entry : _issuerCurves.entrySet()) {
      if (entry.getValue() == null) {
        throw new OpenGammaRuntimeException("Curve with key " + entry.getValue() + " was null");
      }
      _allNames.add(entry.getValue().getName());
      _issuerCurvesNames.put(entry.getValue().getName(), entry.getValue());
    }
  }

  /**
   * Sets the price index curve for a price index.
   * @param issuerCcy The issuer/currency pair.
   * @param curve The curve.
   */
  public void setCurve(final Pair<Object, LegalEntityFilter<LegalEntity>> issuerCcy, final YieldAndDiscountCurve curve) {
    ArgumentChecker.notNull(issuerCcy, "Name-currency");
    ArgumentChecker.notNull(curve, "curve");
    if (_issuerCurves.containsKey(issuerCcy)) {
      throw new IllegalArgumentException("Issuer/currency curve already set: " + issuerCcy.toString());
    }
    _issuerCurves.put(issuerCcy, curve);
    setAllCurves();
  }

  @Override
  public double getDiscountFactor(final Pair<Object, LegalEntityFilter<LegalEntity>> issuerCcy, final Double time) {
    return _issuerCurves.get(issuerCcy).getDiscountFactor(time);
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
  public Set<Pair<Object, LegalEntityFilter<LegalEntity>>> getIssuers() {
    return _issuerCurves.keySet();
  }

  @Override
  public MulticurveProviderDiscount getMulticurveProvider() {
    return _inflationProvider.getMulticurveProvider();
  }

  @Override
  public InflationProviderDiscount getInflationProvider() {
    return _inflationProvider;
  }

  /**
   * Gets the curve for an identifier / filter pair.
   * @param issuer The issuer
   * @return The curve, null if not found
   */
  public YieldAndDiscountCurve getCurve(final Pair<Object, LegalEntityFilter<LegalEntity>> issuer) {
    return _issuerCurves.get(issuer);
  }

  /**
   * Gets the curve for an issuer.
   * @param issuer The issuer
   * @return The curve, null if not found
   */
  public YieldAndDiscountCurve getCurve(final LegalEntity issuer) {
    for (final Map.Entry<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> entry : _issuerCurves.entrySet()) {
      if (entry.getKey().getFirst().equals(entry.getKey().getSecond().getFilteredData(issuer))) {
        return entry.getValue();
      }
    }
    throw new IllegalArgumentException("Could not get curve for " + issuer);
  }

  /**
   * Gets the curve(with a name) for an issuer .
   * @param name The name
   * @return The curve, null if not found
   */
  public YieldAndDiscountCurve getCurve(final String name) {
    for (final Entry<String, YieldAndDiscountCurve> entry : _issuerCurvesNames.entrySet()) {
      if (entry.getKey().equals(name)) {
        return entry.getValue();
      }
    }
    throw new IllegalArgumentException("Could not get curve for " + name);
  }

  @Override
  public Integer getNumberOfParameters(final String name) {
    final PriceIndexCurve inflationCurve = _inflationProvider.getCurve(name);
    final YieldAndDiscountCurve curve = _inflationProvider.getMulticurveProvider().getCurve(name);
    final YieldAndDiscountCurve issuerCurve = _issuerCurvesNames.get(name);
    if (inflationCurve != null) {
      return inflationCurve.getNumberOfParameters();
    } else if (curve != null) {
      return curve.getNumberOfParameters();
    } else if (issuerCurve != null) {
      return issuerCurve.getNumberOfParameters();
    }
    throw new UnsupportedOperationException("Cannot return the number of parameter for a null curve");
  }

  @Override
  public List<String> getUnderlyingCurvesNames(final String name) {
    final PriceIndexCurve inflationCurve = _inflationProvider.getCurve(name);
    final YieldAndDiscountCurve curve = _inflationProvider.getMulticurveProvider().getCurve(name);
    final YieldAndDiscountCurve issuerCurve = _issuerCurvesNames.get(name);
    if (inflationCurve != null) {
      return inflationCurve.getUnderlyingCurvesNames();
    } else if (curve != null) {
      return curve.getUnderlyingCurvesNames();
    } else if (issuerCurve != null) {
      return issuerCurve.getUnderlyingCurvesNames();
    }
    throw new UnsupportedOperationException("Cannot return the number of parameter for a null curve");
  }

  //     =====     Methods related to InflationProvider     =====

  @Override
  public double getPriceIndex(final IndexPrice index, final Double time) {
    return _inflationProvider.getPriceIndex(index, time);
  }

  @Override
  public String getName(final IndexPrice index) {
    return _inflationProvider.getName(index);
  }

  /**
   * Gets the price index curve associated to a given price index in the market.
   * @param index The Price index.
   * @return The curve.
   */
  public PriceIndexCurve getCurve(final IndexPrice index) {
    return _inflationProvider.getCurve(index);
  }

  @Override
  public Set<IndexPrice> getPriceIndexes() {
    return _inflationProvider.getPriceIndexes();
  }

  /**
   * Sets the price index curve for a price index.
   * @param index The price index.
   * @param curve The curve.
   */
  public void setCurve(final IndexPrice index, final PriceIndexCurve curve) {
    _inflationProvider.setCurve(index, curve);
    setAllCurves();
  }

  /**
   * Replaces the discounting curve for a price index.
   * @param index The price index.
   * @param curve The price curve for the index.
   *  @throws IllegalArgumentException if curve name NOT already present
   */
  public void replaceCurve(final IndexPrice index, final PriceIndexCurve curve) {
    _inflationProvider.replaceCurve(index, curve);
  }

  //     =====     Methods related to MulticurveProvider     =====

  @Override
  public double getDiscountFactor(final Currency ccy, final Double time) {
    return _inflationProvider.getDiscountFactor(ccy, time);
  }

  @Override
  public String getName(final Currency ccy) {
    return _inflationProvider.getName(ccy);
  }

  @Override
  public Set<Currency> getCurrencies() {
    return _inflationProvider.getCurrencies();
  }

  @Override
  public double getForwardRate(final IborIndex index, final double startTime, final double endTime, final double accrualFactor) {
    return _inflationProvider.getForwardRate(index, startTime, endTime, accrualFactor);
  }

  @Override
  public String getName(final IborIndex index) {
    return _inflationProvider.getName(index);
  }

  @Override
  public Set<IborIndex> getIndexesIbor() {
    return _inflationProvider.getIndexesIbor();
  }

  @Override
  public double getForwardRate(final IndexON index, final double startTime, final double endTime, final double accrualFactor) {
    return _inflationProvider.getForwardRate(index, startTime, endTime, accrualFactor);
  }

  @Override
  public String getName(final IndexON index) {
    return _inflationProvider.getName(index);
  }

  @Override
  public Set<IndexON> getIndexesON() {
    return _inflationProvider.getIndexesON();
  }

  /**
   * Gets the discounting curve associated in a given currency in the market.
   * @param ccy The currency.
   * @return The curve.
   */
  public YieldAndDiscountCurve getCurve(final Currency ccy) {
    return _inflationProvider.getCurve(ccy);
  }

  /**
   * Gets the forward curve associated to a given Ibor index in the market.
   * @param index The Ibor index.
   * @return The curve.
   */
  public YieldAndDiscountCurve getCurve(final IborIndex index) {
    return _inflationProvider.getCurve(index);
  }

  /**
   * Gets the forward curve associated to a given ON index in the market.
   * @param index The ON index.
   * @return The curve.
   */
  public YieldAndDiscountCurve getCurve(final IndexON index) {
    return _inflationProvider.getCurve(index);
  }

  /**
   * Sets the discounting curve for a given currency.
   * @param ccy The currency.
   * @param curve The yield curve used for discounting.
   */
  public void setCurve(final Currency ccy, final YieldAndDiscountCurve curve) {
    _inflationProvider.setCurve(ccy, curve);
    setAllCurves();
  }

  /**
   * Sets the curve associated to an Ibor index.
   * @param index The index.
   * @param curve The curve.
   */
  public void setCurve(final IborIndex index, final YieldAndDiscountCurve curve) {
    _inflationProvider.setCurve(index, curve);
    setAllCurves();
  }

  /**
   * Sets the curve associated to an ON index.
   * @param index The index.
   * @param curve The curve.
   */
  public void setCurve(final IndexON index, final YieldAndDiscountCurve curve) {
    _inflationProvider.setCurve(index, curve);
    setAllCurves();
  }

  /**
   * Replaces the discounting curve for a given currency.
   * @param ccy The currency.
   * @param curve The yield curve used for discounting.
   *  @throws IllegalArgumentException if curve name NOT already present
   */
  public void replaceCurve(final Currency ccy, final YieldAndDiscountCurve curve) {
    _inflationProvider.replaceCurve(ccy, curve);
  }

  /**
   * Replaces the forward curve for a given index.
   * @param index The index.
   * @param curve The yield curve used for forward.
   *  @throws IllegalArgumentException if curve name NOT already present
   */
  public void replaceCurve(final IborIndex index, final YieldAndDiscountCurve curve) {
    _inflationProvider.replaceCurve(index, curve);
  }

  @Override
  public double getFxRate(final Currency ccy1, final Currency ccy2) {
    return _inflationProvider.getFxRate(ccy1, ccy2);
  }

  /**
   * Gets the underlying FXMatrix containing the exchange rates.
   * @return The matrix.
   */
  @Override
  public FXMatrix getFxRates() {
    return _inflationProvider.getFxRates();
  }

  //     =====     Methods related to All     =====

  @Override
  public Set<String> getAllNames() {
    return getAllCurveNames();
  }

  @Override
  public Set<String> getAllCurveNames() {
    final Set<String> names = new TreeSet<>();
    names.addAll(_inflationProvider.getAllNames());
    final Set<Pair<Object, LegalEntityFilter<LegalEntity>>> issuerSet = _issuerCurves.keySet();
    for (final Pair<Object, LegalEntityFilter<LegalEntity>> issuer : issuerSet) {
      names.add(_issuerCurves.get(issuer).getName());
    }
    return names;
  }

  /**
   * Set all the curves contains in another bundle. If a currency or index is already present in the map, the associated curve is changed.
   * @param other The other bundle.
   */
  //  * TODO: REVIEW: Should we check that the curve are already present?
  public void setAll(final InflationIssuerProviderDiscount other) {
    ArgumentChecker.notNull(other, "Inflation provider");
    _inflationProvider.setAll(other.getInflationProvider());
    _issuerCurves.putAll(other._issuerCurves);
  }

  //     =====     Convenience methods     =====

  /**
   * Replaces an issuer curve.
   * @param ic The key of the curve to replace
   * @param replacement The replacement curve
   * @return A new provider with the curve replaced.
   */
  public InflationIssuerProviderDiscount withIssuerCurve(final Pair<Object, LegalEntityFilter<LegalEntity>> ic, final YieldAndDiscountCurve replacement) {
    final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> newIssuerCurves = new LinkedHashMap<>(_issuerCurves);
    newIssuerCurves.put(ic, replacement);
    return new InflationIssuerProviderDiscount(_inflationProvider, newIssuerCurves);
  }

  @Override
  public InflationProviderInterface withDiscountFactor(final Currency ccy, final Pair<Object, LegalEntityFilter<LegalEntity>> replacement) {
    return _inflationProvider.withDiscountFactor(ccy, _issuerCurves.get(replacement));
  }

  @Override
  public InflationProviderInterface withDiscountFactor(final Currency ccy, final LegalEntity replacement) {
    for (final Map.Entry<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> entry : _issuerCurves.entrySet()) {
      if (entry.getKey().getFirst().equals(entry.getKey().getSecond().getFilteredData(replacement))) {
        return _inflationProvider.withDiscountFactor(ccy, entry.getValue());
      }
    }
    throw new IllegalArgumentException("Issuer discounting curve not found: " + replacement);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _inflationProvider.hashCode();
    result = prime * result + _issuerCurves.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof InflationIssuerProviderDiscount)) {
      return false;
    }
    final InflationIssuerProviderDiscount other = (InflationIssuerProviderDiscount) obj;
    if (!ObjectUtils.equals(_inflationProvider, other._inflationProvider)) {
      return false;
    }
    if (!ObjectUtils.equals(_issuerCurves, other._issuerCurves)) {
      return false;
    }
    return true;
  }

  @Override
  public IssuerProviderDiscount getIssuerProvider() {
    return new IssuerProviderDiscount(this.getMulticurveProvider(), _issuerCurves);
  }

  @Override
  public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    if (_inflationProvider.getMulticurveProvider().getAllNames().contains(name)) {
      return _inflationProvider.parameterSensitivity(name, pointSensitivity);
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
    return _inflationProvider.parameterForwardSensitivity(name, pointSensitivity);
  }

  @Override
  public double[] parameterInflationSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    final YieldAndDiscountCurve curve = _issuerCurvesNames.get(name);
    if (curve == null) {
      return _inflationProvider.parameterInflationSensitivity(name, pointSensitivity);
    }
    final int nbParameters = curve.getNumberOfParameters();
    final double[] result = new double[nbParameters];
    if (pointSensitivity != null && pointSensitivity.size() > 0) {
      for (final DoublesPair timeAndS : pointSensitivity) {
        final double[] sensi1Point = curve.getInterestRateParameterSensitivity(timeAndS.getFirst());
        for (int loopparam = 0; loopparam < nbParameters; loopparam++) {
          result[loopparam] += timeAndS.getSecond() * sensi1Point[loopparam];
        }
      }
    }
    return result;
  }

}
