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
import java.util.Collection;
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
import com.opengamma.sesame.marketdata.VolatilitySurfaceId;
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
   * Reads and parses volatility data into in memory raw representation
   * The format of the file is simple csv with a header row:
   * name,strike,vol,maturity
   * CALL_NK225,4000,258.68,31D
   * CALL_NK225,4500,239.8297,31D
   * CALL_NK225,13000,30.268,304D
   * CALL_NK225,13250,29.2263,304D
   * PUT_NK225,16000,25.101,94D
   * PUT_NK225,16250,24.457,94D
   *
   * @param file the data to be parsed
   * @return the parsed curve data by curve name
   */
  public static Map<String, VolRawData> parseVols(String file) throws IOException {

    s_logger.info("Creating curves from {}.", file);
    Map<String, VolRawData> curveData = new HashMap<>();

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
          s_logger.error("Invalid tenor {} for {}", tenor, name);
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

        String vol = line[2];
        double volValue;
        try {
          volValue = Double.parseDouble(vol);
        } catch (NumberFormatException e) {
          s_logger.error("Invalid vol {} for {} at {} and {}", vol, name, tenor, strike);
          continue;
        }


        if (!curveData.containsKey(name)) {
          curveData.put(name, new VolRawData());
        }
        curveData.get(name).add(tenorValue, strikeValue, volValue);
      }
    } catch (IOException e) {
      s_logger.error("Failed to parse curve data ", e);
    }
    return curveData;
  }

  /**
   * Create instance of {@link VolatilitySurface}
   * @param data the {@link VolRawData} to create the surface from
   * @return a VolatilitySurface
   */
  public static VolatilitySurface createVolatilitySurface(VolRawData data) {
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
    List<Double> volList = data.getVols();
    for (int i = 0; i < points; i++) {
      Tenor tenor = tenorList.get(i);
      times[i] = tenor.getPeriod().getDays() / 365d;
      vols[i] = volList.get(i) / 100d;
    }

    InterpolatedDoublesSurface surface = InterpolatedDoublesSurface.from(times, strikes, vols, interpolator2D);
    return new VolatilitySurface(surface);
  }

  /**
   * Helper class to store raw curve data
   */
  static class VolRawData {
    private List<Tenor> _times = new ArrayList<>();
    private List<Double> _strikes = new ArrayList<>();
    private List<Double> _vols = new ArrayList<>();

    /**
     * Add a curve point
     *
     * @param time tenor at this point - x
     * @param strike value at this point - y
     * @param vol value at this point -z
     */
    public void add(Tenor time, Double strike, Double vol) {
      ArgumentChecker.notNull(time, "time");
      ArgumentChecker.notNull(strike, "strike");
      ArgumentChecker.notNull(vol, "vol");
      _times.add(time);
      _strikes.add(strike);
      _vols.add(vol);
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
     * @return all vol points available on the surface
     */
    public List<Double> getVols() {
      return _vols;
    }

  }

}