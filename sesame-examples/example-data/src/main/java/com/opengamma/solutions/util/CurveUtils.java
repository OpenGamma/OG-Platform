/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import com.google.common.collect.Lists;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Utility class for curves
 */
public final class CurveUtils {

  private CurveUtils() { /* private constructor */ }

  private static final Logger s_logger = LoggerFactory.getLogger(CurveUtils.class);

  /**
   * Reads and parses curve data into in memory raw representation
   * The format of the file is simple csv with a header row:
   * Name,Tenor,Value
   * USD-Federal Funds,1D,0.05918
   * USD-Federal Funds,1W,0.06263
   * USD-Federal Funds,2M,0.06415
   * USD-Federal Funds,1Y,0.07234
   *
   * @param file the data to be parsed
   * @return the parsed curve data by curve name
   */
  public static Map<String, CurveRawData> parseCurves(String file) throws IOException {

    s_logger.info("Creating curves from {}.", file);
    Map<String, CurveRawData> curveData = new HashMap<>();

    Reader curveReader = new BufferedReader(
        new InputStreamReader(
            new ClassPathResource(file).getInputStream()
        )
    );

    try {
      CSVReader csvReader = new CSVReader(curveReader);
      String[] line;
      csvReader.readNext(); // skip headers
      while ((line = csvReader.readNext()) != null) {
        String name = line[0];

        String tenor = line[1];
        if (tenor.endsWith("W")) {
          int weeks = Integer.parseInt(tenor.substring(0, tenor.length() - 1));
          tenor = (weeks * 7) + "D";
        } else if (tenor.endsWith("M")) {
          int months = Integer.parseInt(tenor.substring(0, tenor.length() - 1));
          tenor = (months / 12 * 365) + "D";
        } else if (tenor.endsWith("Y")) {
          int years = Integer.parseInt(tenor.substring(0, tenor.length() - 1));
          tenor = (years * 365) + "D";
        }

        Tenor tenorValue;
        try {
          tenorValue = Tenor.parse("P" + tenor);
        } catch (NumberFormatException e) {
          s_logger.error("Invalid tenor {} for {}", tenor, name);
          continue;
        }

        String quote = line[2];
        double quoteValue;
        try {
          quoteValue = Double.parseDouble(quote);
        } catch (NumberFormatException e) {
          s_logger.error("Invalid quote {} for {} at {}", quote, name, tenor);
          continue;
        }
        if (!curveData.containsKey(name)) {
          curveData.put(name, new CurveRawData());
        }
        curveData.get(name).add(tenorValue, quoteValue);
      }
    } catch (IOException e) {
      s_logger.error("Failed to parse curve data ", e);
    }
    return curveData;
  }

  /**
   * Create instance of a {@link ForwardCurve}
   * @param data the {@link CurveRawData} to create the curve from
   * @return a ForwardCurve
   */
  public static ForwardCurve createForwardCurve(CurveRawData data) {
    List<Tenor> tenors = Lists.newArrayList(data.getCurvePoint());
    double[] maturities = new double[tenors.size()];
    double[] prices = new double[tenors.size()];

    for (int i = 0; i < tenors.size(); i++) {
      Tenor tenor = tenors.get(i);
      maturities[i] = tenor.getPeriod().getDays() / 365d;
      prices[i] = data.getValue(tenor);
    }

    Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
        Interpolator1DFactory.LINEAR,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR);

    return new ForwardCurve(InterpolatedDoublesCurve.from(maturities, prices, interpolator));
  }

  /**
   * Create instance of {@link YieldAndDiscountCurve}
   * @param name name of the curve
   * @param data the {@link CurveRawData} to create the curve from
   * @return a YieldAndDiscountCurve
   */
  public static YieldAndDiscountCurve createYieldCurve(String name, CurveRawData data) {
    List<Tenor> tenors = Lists.newArrayList(data.getCurvePoint());
    double[] ttm = new double[tenors.size()];
    double[] rates = new double[tenors.size()];

    for (int i = 0; i < tenors.size(); i++) {
      Tenor tenor = tenors.get(i);
      ttm[i] = tenor.getPeriod().getDays() / 365d;
      rates[i] = data.getValue(tenor) / 100d;
    }

    Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
        Interpolator1DFactory.LINEAR,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR);

    InterpolatedDoublesCurve interpolatedCurve = InterpolatedDoublesCurve.from(ttm, rates, interpolator);
    return new YieldCurve(name, interpolatedCurve);
  }


  /**
   * Helper class to store raw curve data
   */
  static class CurveRawData {
    private TreeMap<Tenor, Double> _doublesCurve = new TreeMap<>();

    /**
     * Add a curve point
     *
     * @param curvePoint tenor identifying this point
     * @param curveValue value at that point as a zero rate
     */
    public void add(Tenor curvePoint, Double curveValue) {
      ArgumentChecker.notNull(curvePoint, "curvePoint");
      ArgumentChecker.notNull(curveValue, "curveValue");
      _doublesCurve.put(curvePoint, curveValue);
    }

    /**
     * @return points available on the curve
     */
    public Collection<Tenor> getCurvePoint() {
      return _doublesCurve.keySet();
    }

    /**
     * @return all of the raw data points in the curve
     */
    public Collection<Double> getCurveValue() {
      return _doublesCurve.values();
    }

    /**
     * @param tenor point on the curve
     * @return rate of the specified point
     */
    public Double getValue(Tenor tenor) {
      return _doublesCurve.get(tenor);
    }

  }


}