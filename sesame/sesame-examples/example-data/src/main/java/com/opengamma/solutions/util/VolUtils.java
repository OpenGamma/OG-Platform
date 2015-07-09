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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import com.google.common.primitives.Doubles;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.math.surface.NodalDoublesSurface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Utility class for volatility surfaces
 */
public final class VolUtils {

  private VolUtils() { /* private constructor */ }

  private static final Logger s_logger = LoggerFactory.getLogger(VolUtils.class);

  /**
   * Reads and parses surface data into in memory raw representation
   * The format of the file is simple csv with a header row:
   * name,strike,vol,maturity
   * CALL_NK225,4000,258.68,31D
   * CALL_NK225,4500,239.8297,31D
   * PUT_NK225,16000,25.101,94D
   * PUT_NK225,16250,24.457,94D
   *
   * or
   * name,strike,price,maturity
   * CALL_NK225,4000,15665,31D
   *
   * @param file the data to be parsed
   * @return the parsed curve data by curve name
   */
  public static Map<String, SurfaceRawData> parseSurface(String file) throws IOException {

    s_logger.info("Creating curves from {}.", file);
    Map<String, SurfaceRawData> curveData = new HashMap<>();

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

        String tenor = line[3];
        if (tenor.endsWith("W")) {
          int weeks = Integer.parseInt(tenor.substring(0, tenor.length() - 1));
          tenor = (weeks * 7) + "D";
        } else if (tenor.endsWith("M")) {
          int months = Integer.parseInt(tenor.substring(0, tenor.length() - 1));
          tenor = Math.round(months / 12d * 365) + "D";
        } else if (tenor.endsWith("Y")) {
          int years = Integer.parseInt(tenor.substring(0, tenor.length() - 1));
          tenor = (years * 365) + "D";
        }

        Tenor tenorValue;
        try {
          tenorValue = Tenor.parse("P" + tenor);
        } catch (NumberFormatException e) {
          s_logger.error("Invalid tenor {} for {} in file {}. Input tenor values should contain and end with one of the following D, W, M and Y",
                         tenor, name, file);
          continue;
        }

        String strike = line[1];
        double strikeValue;
        try {
          strikeValue = Double.parseDouble(strike);
        } catch (NumberFormatException e) {
          s_logger.error("Invalid strike {} for {} at {}", strike, name, tenor);
          continue;
        }

        String value = line[2];
        double surfaceValue;
        try {
          surfaceValue = Double.parseDouble(value);
        } catch (NumberFormatException e) {
          s_logger.error("Invalid value {} for {} at {} and {}", value, name, tenor, strike);
          continue;
        }


        if (!curveData.containsKey(name)) {
          curveData.put(name, new SurfaceRawData());
        }
        curveData.get(name).add(tenorValue, strikeValue, surfaceValue);
      }
    } catch (IOException e) {
      s_logger.error("Failed to parse curve data ", e);
    }
    return curveData;
  }

  /**
   * Create instance of {@link VolatilitySurface}
   * @param data the {@link SurfaceRawData} to create the surface from
   * @return a VolatilitySurface
   */
  public static VolatilitySurface createVolatilitySurface(SurfaceRawData data) {
    Interpolator1D linearFlat =
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR,
                                                                Interpolator1DFactory.FLAT_EXTRAPOLATOR,
                                                                Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    GridInterpolator2D interpolator2D = new GridInterpolator2D(linearFlat, linearFlat);

    double[] strikes = Doubles.toArray(data.getStrikes());
    int points = strikes.length;
    double[] times = new double[points];
    double[] vols = new double[points];

    List<Tenor> tenorList = data.getTimes();
    List<Double> volList = data.getzData();
    for (int i = 0; i < points; i++) {
      Tenor tenor = tenorList.get(i);
      times[i] = tenor.getPeriod().getDays() / 365d;
      vols[i] = volList.get(i) / 100d;
    }

    InterpolatedDoublesSurface surface = InterpolatedDoublesSurface.from(times, strikes, vols, interpolator2D);
    return new VolatilitySurface(surface);
  }

  /**
   * Create instance of {@link NodalDoublesSurface}
   * @param data the {@link SurfaceRawData} to create the surface from
   * @return a VolatilitySurface
   */
  public static NodalDoublesSurface createPriceSurface(SurfaceRawData data) {
    double[] strikes = Doubles.toArray(data.getStrikes());
    int points = strikes.length;
    double[] times = new double[points];
    double[] prices = new double[points];

    List<Tenor> tenorList = data.getTimes();
    List<Double> priceList = data.getzData();
    for (int i = 0; i < points; i++) {
      Tenor tenor = tenorList.get(i);
      times[i] = tenor.getPeriod().getDays() / 365d;
      prices[i] = priceList.get(i);
    }

    return new NodalDoublesSurface(times, strikes, prices);
  }

  /**
   * Helper class to store raw curve data
   */
  static class SurfaceRawData {
    private List<Tenor> _times = new ArrayList<>();
    private List<Double> _strikes = new ArrayList<>();
    private List<Double> _zData = new ArrayList<>();

    /**
     * Add a curve point
     *
     * @param time tenor at this point - x
     * @param strike value at this point - y
     * @param point value at this point - z
     */
    public void add(Tenor time, Double strike, Double point) {
      ArgumentChecker.notNull(time, "time");
      ArgumentChecker.notNull(strike, "strike");
      ArgumentChecker.notNull(point, "point");
      _times.add(time);
      _strikes.add(strike);
      _zData.add(point);
    }

    /**
     * @return all time points available on the surface
     */
    public List<Tenor> getTimes() {
      return _times;
    }

    /**
     * @return all strike points available on the surface
     */
    public List<Double> getStrikes() {
      return _strikes;
    }

    /**
     * @return all points available on the surface
     */
    public List<Double> getzData() {
      return _zData;
    }

  }

}