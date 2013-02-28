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
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 *  Generator of MarketDiscountBundle from the parameters.
 */
public class GeneratorInflationProviderDiscount extends Function1D<DoubleMatrix1D, InflationProviderInterface> {

  /**
   * The map with the indexes and the related forward curves names.
   */
  private final LinkedHashMap<String, IndexPrice[]> _inflationMap;

  /**
   * The map with the names and the related inflation curves generators.
   */
  private final LinkedHashMap<String, GeneratorPriceIndexCurve> _generatorsInflationMap;

  /**
   * The market with known data (curves, model parameters, etc.).
   */
  private final InflationProviderDiscount _knownData;

  /**
   * Constructor
   * @param knownData The yield curve bundle with known data (curves).
   * @param inflationMap The discounting curves names map.
   * @param generatorsInflationMap The inflation generators map.
   */
  public GeneratorInflationProviderDiscount(final InflationProviderDiscount knownData, final LinkedHashMap<String, IndexPrice[]> inflationMap,
      final LinkedHashMap<String, GeneratorPriceIndexCurve> generatorsInflationMap) {
    ArgumentChecker.notNull(inflationMap, "Inflation curves names map");
    _knownData = knownData;
    _inflationMap = inflationMap;
    _generatorsInflationMap = generatorsInflationMap;
  }

  /**
   * Gets the know data.
   * @return The known data.
   */
  public InflationProviderDiscount getKnownData() {
    return _knownData;
  }

  /**
   * Gets the set of curves. The set order is the order in which they are build.
   * @return The set.
   */
  public Set<String> getInflationCurvesList() {
    return _generatorsInflationMap.keySet();
  }

  @Override
  public InflationProviderDiscount evaluate(DoubleMatrix1D x) {
    InflationProviderDiscount provider = _knownData.copy();
    Set<String> inflationNameSet = _generatorsInflationMap.keySet();
    int indexParam = 0;
    for (String name : inflationNameSet) {
      GeneratorPriceIndexCurve gen = _generatorsInflationMap.get(name);
      double[] paramCurve = Arrays.copyOfRange(x.getData(), indexParam, indexParam + gen.getNumberOfParameter());
      indexParam += gen.getNumberOfParameter();
      PriceIndexCurve curve = gen.generateCurve(name, provider, paramCurve);
      if (_inflationMap.containsKey(name)) {
        IndexPrice[] indexes = _inflationMap.get(name);
        for (int loopindex = 0; loopindex < indexes.length; loopindex++) {
          provider.setCurve(indexes[loopindex], curve);
        }
      }
    }
    return provider;
  }
}
