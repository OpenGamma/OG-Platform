/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.inflation;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Set;

import com.opengamma.analytics.financial.curve.inflation.generator.GeneratorPriceIndexCurve;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurve;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *  Generator of MarketDiscountBundle from the parameters.
 */
public class GeneratorInflationProviderDiscount extends Function1D<DoubleMatrix1D, InflationProviderInterface> {

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
   * The map with the names and the related curves generators.
   */
  private final LinkedHashMap<String, GeneratorCurve> _generatorsMap;

  /**
   * The market with known data (curves, model parameters, etc.).
   */
  private final InflationProviderDiscount _knownData;

  /**
   * Constructor without the discount curve.
   * @param knownData The yield curve bundle with known data (curves).
   * @param inflationMap The discounting curves names map.
   * @param generatorsInflationMap The inflation generators map.
   */
  public GeneratorInflationProviderDiscount(final InflationProviderDiscount knownData, final LinkedHashMap<String, IndexPrice[]> inflationMap,
      final LinkedHashMap<String, GeneratorPriceIndexCurve> generatorsInflationMap) {
    ArgumentChecker.notNull(inflationMap, "Inflation curves names map");
    _knownData = knownData;
    _discountingMap = new LinkedHashMap<String, Currency>();
    _forwardONMap = new LinkedHashMap<String, IndexON[]>();
    _inflationMap = inflationMap;
    _generatorsMap = new LinkedHashMap<String, GeneratorCurve>();
    _generatorsMap.putAll(generatorsInflationMap);
  }

  /**
   * Constructor with the discount curve.
   * @param knownData The yield curve bundle with known data (curves).
   * @param discountingMap The discounting curves names map.
   * @param forwardONMap The ON curves names map.
   * @param inflationMap The inflation curves names map.
   * @param generatorsMap The inflation generators map.
   */
  public GeneratorInflationProviderDiscount(final InflationProviderDiscount knownData, final LinkedHashMap<String, Currency> discountingMap, LinkedHashMap<String, IndexON[]> forwardONMap,
      final LinkedHashMap<String, IndexPrice[]> inflationMap, final LinkedHashMap<String, GeneratorCurve> generatorsMap) {
    ArgumentChecker.notNull(inflationMap, "Inflation curves names map");
    ArgumentChecker.notNull(discountingMap, "Discount curves names map");
    _knownData = knownData;
    _discountingMap = discountingMap;
    _forwardONMap = forwardONMap;
    _inflationMap = inflationMap;
    _generatorsMap = generatorsMap;
  }

  /**
   * Gets the know data.
   * @return The known data.
   */
  public InflationProviderDiscount getKnownData() {
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
  public InflationProviderDiscount evaluate(DoubleMatrix1D x) {
    InflationProviderDiscount provider = _knownData.copy();
    Set<String> nameSet = _generatorsMap.keySet();
    int indexParam = 0;
    for (String name : nameSet) {
      GeneratorCurve generator = _generatorsMap.get(name);

      double[] paramCurve = Arrays.copyOfRange(x.getData(), indexParam, indexParam + generator.getNumberOfParameter());
      indexParam += generator.getNumberOfParameter();

      if (generator instanceof GeneratorYDCurve) {
        GeneratorYDCurve discountGenerator = (GeneratorYDCurve) generator;
        YieldAndDiscountCurve curve = discountGenerator.generateCurve(name, provider.getMulticurveProvider(), paramCurve);
        if (_discountingMap.containsKey(name)) {
          provider.getMulticurveProvider().setCurve(_discountingMap.get(name), curve);
        }
        if (_forwardONMap.containsKey(name)) {
          IndexON[] indexes = _forwardONMap.get(name);
          for (int loopindex = 0; loopindex < indexes.length; loopindex++) {
            provider.setCurve(indexes[loopindex], curve);
          }
        }
      }
      if (generator instanceof GeneratorPriceIndexCurve) {
        GeneratorPriceIndexCurve inflationGenerator = (GeneratorPriceIndexCurve) generator;
        PriceIndexCurve inflationCurve = inflationGenerator.generateCurve(name, provider, paramCurve);

        if (_inflationMap.containsKey(name)) {
          IndexPrice[] indexes = _inflationMap.get(name);
          for (int loopindex = 0; loopindex < indexes.length; loopindex++) {
            provider.setCurve(indexes[loopindex], inflationCurve);
          }

        }
      }
    }
    return provider;
  }
}
