/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.inflationissuer;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import com.google.common.collect.LinkedListMultimap;
import com.opengamma.analytics.financial.curve.inflation.generator.GeneratorPriceIndexCurve;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurve;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderInterface;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 *  Generator of MarketDiscountBundle from the parameters.
 */
public class GeneratorInflationIssuerProviderDiscount extends Function1D<DoubleMatrix1D, InflationIssuerProviderInterface> {

  /**
   * The map with the currencies and the related discounting curves names.
   */
  private final LinkedHashMap<String, Currency> _discountingMap;

  /**
   * The map with the currencies and the related discounting curves names.
   */
  private final LinkedHashMap<String, IndexON[]> _forwardONMap;

  /**
   * The map with the indexes and the related forward curves names.
   */
  private final LinkedHashMap<String, IndexPrice[]> _inflationMap;

  /**
   * The map with the issuers and the related discounting curves names.
   */
  private final LinkedListMultimap<String, Pair<Object, LegalEntityFilter<LegalEntity>>> _issuerMap;

  /**
   * The map with the names and the related curves generators.
   */
  private final LinkedHashMap<String, GeneratorCurve> _generatorsMap;

  /**
   * The market with known data (curves, model parameters, etc.).
   */
  private final InflationIssuerProviderDiscount _knownData;

  /**
   * Constructor without the discount curve.
   * @param knownData The yield curve bundle with known data (curves).
   * @param inflationMap The discounting curves names map.
   * @param generatorsInflationMap The inflation generators map.
   */
  public GeneratorInflationIssuerProviderDiscount(final InflationIssuerProviderDiscount knownData, final LinkedHashMap<String, IndexPrice[]> inflationMap,
      final LinkedHashMap<String, GeneratorCurve> generatorsInflationMap) {
    ArgumentChecker.notNull(inflationMap, "Inflation curves names map");
    _knownData = knownData;
    _discountingMap = new LinkedHashMap<>();
    _forwardONMap = new LinkedHashMap<>();
    _inflationMap = inflationMap;
    _issuerMap = LinkedListMultimap.create();
    _generatorsMap = new LinkedHashMap<>();
    _generatorsMap.putAll(generatorsInflationMap);
  }

  /**
   * Constructor with the discount curve.
   * @param knownData The yield curve bundle with known data (curves).
   * @param discountingMap The discounting curves names map.
   * @param forwardONMap The ON curves names map.
   * @param inflationMap The inflation curves names map.
   * @param issuerMap The issuer curves  legal entity map.
   * @param generatorsMap The inflation generators map.
   */
  public GeneratorInflationIssuerProviderDiscount(final InflationIssuerProviderDiscount knownData, final LinkedHashMap<String, Currency> discountingMap,
      final LinkedHashMap<String, IndexON[]> forwardONMap, final LinkedHashMap<String, IndexPrice[]> inflationMap,
      final LinkedListMultimap<String, Pair<Object, LegalEntityFilter<LegalEntity>>> issuerMap,
      final LinkedHashMap<String, GeneratorCurve> generatorsMap) {
    ArgumentChecker.notNull(inflationMap, "Inflation curves names map");
    ArgumentChecker.notNull(discountingMap, "Discount curves names map");
    _knownData = knownData;
    _discountingMap = discountingMap;
    _forwardONMap = forwardONMap;
    _inflationMap = inflationMap;
    _issuerMap = issuerMap;
    _generatorsMap = generatorsMap;
  }

  /**
   * Gets the know data.
   * @return The known data.
   */
  public InflationIssuerProviderDiscount getKnownData() {
    return _knownData;
  }

  /**
   * Gets the set of generators of curves . The set order is the order in which they are build.
   * @return The set.
   */
  public Set<String> getInflationCurvesList() {
    return _generatorsMap.keySet();
  }

  @Override
  public InflationIssuerProviderDiscount evaluate(final DoubleMatrix1D x) {
    final InflationIssuerProviderDiscount provider = _knownData.copy();
    final Set<String> nameSet = _generatorsMap.keySet();
    int indexParam = 0;
    for (final String name : nameSet) {
      final GeneratorCurve generator = _generatorsMap.get(name);
      final double[] paramCurve = Arrays.copyOfRange(x.getData(), indexParam, indexParam + generator.getNumberOfParameter());
      indexParam += generator.getNumberOfParameter();
      if (generator instanceof GeneratorYDCurve) {
        final GeneratorYDCurve discountGenerator = (GeneratorYDCurve) generator;
        final YieldAndDiscountCurve curve = discountGenerator.generateCurve(name, provider.getMulticurveProvider(), paramCurve);
        if (_discountingMap.containsKey(name)) {
          provider.getMulticurveProvider().setCurve(_discountingMap.get(name), curve);
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
      }
      if (generator instanceof GeneratorPriceIndexCurve) {
        final GeneratorPriceIndexCurve inflationGenerator = (GeneratorPriceIndexCurve) generator;
        final PriceIndexCurve inflationCurve = inflationGenerator.generateCurve(name, provider, paramCurve);

        if (_inflationMap.containsKey(name)) {
          final IndexPrice[] indexes = _inflationMap.get(name);
          for (final IndexPrice indexe : indexes) {
            provider.setCurve(indexe, inflationCurve);
          }

        }
      }
    }
    return provider;
  }
}
