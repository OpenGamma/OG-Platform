/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.issuer;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import com.google.common.collect.LinkedListMultimap;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Generator of MarketDiscountBundle from the parameters.
 */
public class GeneratorIssuerProviderDiscount extends Function1D<DoubleMatrix1D, IssuerProviderInterface> {

  /**
   * The map with the currencies and the related discounting curves names.
   */
  private final LinkedHashMap<String, Currency> _discountingMap;
  /**
   * The map with the indexes and the related forward curves names.
   */
  private final LinkedHashMap<String, IndexON[]> _forwardONMap;
  /**
   * The map with the indexes and the related forward curves names.
   */
  private final LinkedHashMap<String, IborIndex[]> _forwardIborMap;
  /**
   * The map with the issuers and the related discounting curves names.
   */
  private final LinkedListMultimap<String, Pair<Object, LegalEntityFilter<LegalEntity>>> _issuerMap;
  /**
   * The map with the names and the related curves generators.
   */
  private final LinkedHashMap<String, GeneratorYDCurve> _generatorsMap;
  /**
   * The market with known data (curves, model parameters, etc.).
   */
  private final IssuerProviderDiscount _knownData;

  /**
   * Constructor
   * @param knownData The yield curve bundle with known data (curves).
   * @param discountingMap The discounting curves names map.
   * @param forwardIborMap The forward curves names map.
   * @param forwardONMap The forward curves names map.
   * @param issuerMap The issuer curve names map.
   * @param generatorsMap The generators map.
   */
  public GeneratorIssuerProviderDiscount(final IssuerProviderDiscount knownData, final LinkedHashMap<String, Currency> discountingMap, final LinkedHashMap<String, IborIndex[]> forwardIborMap,
      final LinkedHashMap<String, IndexON[]> forwardONMap, final LinkedListMultimap<String, Pair<Object, LegalEntityFilter<LegalEntity>>> issuerMap,
      final LinkedHashMap<String, GeneratorYDCurve> generatorsMap) {
    ArgumentChecker.notNull(discountingMap, "Discounting curves names map");
    ArgumentChecker.notNull(forwardIborMap, "Forward curves names map");
    ArgumentChecker.notNull(forwardONMap, "Forward curves names map");
    _knownData = knownData;
    _discountingMap = discountingMap;
    _forwardIborMap = forwardIborMap;
    _forwardONMap = forwardONMap;
    _issuerMap = issuerMap;
    _generatorsMap = generatorsMap;
  }

  /**
   * Gets the know data.
   * @return The known data.
   */
  public IssuerProviderDiscount getKnownData() {
    return _knownData;
  }

  /**
   * Gets the set of curves. The set order is the order in which they are build.
   * @return The set.
   */
  public Set<String> getCurvesList() {
    return _generatorsMap.keySet();
  }

  @Override
  public IssuerProviderDiscount evaluate(final DoubleMatrix1D x) {
    final IssuerProviderDiscount provider = _knownData.copy();
    final Set<String> nameSet = _generatorsMap.keySet();
    int indexParam = 0;
    for (final String name : nameSet) {
      final GeneratorYDCurve gen = _generatorsMap.get(name);
      final double[] paramCurve = Arrays.copyOfRange(x.getData(), indexParam, indexParam + gen.getNumberOfParameter());
      indexParam += gen.getNumberOfParameter();
      final YieldAndDiscountCurve curve = gen.generateCurve(name, provider.getMulticurveProvider(), paramCurve);
      if (_discountingMap.containsKey(name)) {
        provider.setCurve(_discountingMap.get(name), curve);
      }
      if (_forwardIborMap.containsKey(name)) {
        final IborIndex[] indexes = _forwardIborMap.get(name);
        for (final IborIndex indexe : indexes) {
          provider.setCurve(indexe, curve);
        }
      }
      if (_forwardONMap.containsKey(name)) {
        final IndexON[] indexes = _forwardONMap.get(name);
        for (final IndexON indexe : indexes) {
          provider.setCurve(indexe, curve);
        }
      }
      if (_issuerMap.containsKey(name)) {
        final List<Pair<Object, LegalEntityFilter<LegalEntity>>> issuers = _issuerMap.get(name);
        for (final Pair<Object, LegalEntityFilter<LegalEntity>> issuer : issuers) {
          provider.setCurve(issuer, curve);
        }
      }
      // TODO: Do we need to check that the curve is used at least once?
    }
    return provider;
  }

}
