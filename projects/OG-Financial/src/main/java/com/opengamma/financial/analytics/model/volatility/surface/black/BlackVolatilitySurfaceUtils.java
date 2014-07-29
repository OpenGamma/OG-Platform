/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleLinkedOpenHashSet;
import it.unimi.dsi.fastutil.doubles.DoubleList;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Period;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.ForexSmileDeltaSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.StandardSmileSurfaceDataBundle;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;
import com.opengamma.util.tuple.Triple;

/**
 *
 */
public class BlackVolatilitySurfaceUtils {
  private static final Logger s_logger = LoggerFactory.getLogger(BlackVolatilitySurfaceUtils.class);

  public static double[] getUniqueExpiries(final VolatilitySurfaceData<Object, Object> volatilitySurface) {
    final double[] expiries = getArrayOfDoubles(volatilitySurface.getXs());
    final DoubleLinkedOpenHashSet expirySet = new DoubleLinkedOpenHashSet(expiries);
    final double[] uniqueExpiries = expirySet.toDoubleArray();
    Arrays.sort(uniqueExpiries);
    return uniqueExpiries;
  }

  public static Object[] getUniqueExpiriesWithData(final VolatilitySurfaceData<Object, Object> volatilitySurface) {
    final SortedSet<Object> uniqueXValues = volatilitySurface.getUniqueXValues();
    return uniqueXValues.toArray(new Object[uniqueXValues.size()]);
  }

  public static double[] getUniqueStrikes(final VolatilitySurfaceData<Object, Object> volatilitySurface) {
    final double[] strikes = getArrayOfDoubles(volatilitySurface.getYs());
    final DoubleLinkedOpenHashSet strikeSet = new DoubleLinkedOpenHashSet(strikes);
    final double[] uniqueStrikes = strikeSet.toDoubleArray();
    Arrays.sort(uniqueStrikes);
    return uniqueStrikes;
  }

  public static Pair<double[][], double[][]> getStrikesAndValues(final double[] expiries, final double[] strikes, final VolatilitySurfaceData<Object, Object> volatilitySurface) {
    final int nExpiries = expiries.length;
    final int nStrikes = strikes.length;
    final double[][] fullStrikes = new double[nExpiries][];
    final double[][] fullValues = new double[nExpiries][];
    for (int i = 0; i < nExpiries; i++) {
      final DoubleList availableStrikes = new DoubleArrayList();
      final DoubleList availableVols = new DoubleArrayList();
      for (int j = 0; j < nStrikes; j++) {
        final Double vol = volatilitySurface.getVolatility(expiries[i], strikes[j]);
        if (vol != null) {
          availableStrikes.add(strikes[j]);
          availableVols.add(vol);
        }
      }
      if (availableVols.size() == 0) {
        throw new OpenGammaRuntimeException("No volatility values found for expiry " + expiries[i]);
      }
      fullStrikes[i] = availableStrikes.toDoubleArray();
      fullValues[i] = availableVols.toDoubleArray();
    }
    return Pairs.of(fullStrikes, fullValues);
  }

  public static Triple<double[], double[][], double[][]> getStrikesAndValues(final double[] expiries, final double[] strikes, final VolatilitySurfaceData<Object, Object> volatilitySurface,
      final int minNumberOfStrikes) {
    final int nExpiries = expiries.length;
    final int nStrikes = strikes.length;
    final List<double[]> fullStrikes = new ArrayList<>();
    final List<double[]> fullValues = new ArrayList<>();
    final DoubleList availableExpiries = new DoubleArrayList();
    for (int i = 0; i < nExpiries; i++) {
      final DoubleList availableStrikes = new DoubleArrayList();
      final DoubleList availableVols = new DoubleArrayList();
      for (int j = 0; j < nStrikes; j++) {
        final Double vol = volatilitySurface.getVolatility(expiries[i], strikes[j]);
        if (vol != null) {
          availableStrikes.add(strikes[j]);
          availableVols.add(vol);
        }
      }
      if (availableVols.size() == 0) {
        throw new OpenGammaRuntimeException("No volatility values found for expiry " + expiries[i]);
      } else if (availableVols.size() >= minNumberOfStrikes) {
        availableExpiries.add(expiries[i]);
        fullStrikes.add(availableStrikes.toDoubleArray());
        fullValues.add(availableVols.toDoubleArray());
      }
    }
    return Triple.of(availableExpiries.toDoubleArray(), fullStrikes.toArray(new double[0][]), fullValues.toArray(new double[0][]));
  }

  public static Triple<double[], double[][], double[][]> getStrippedStrikesAndValues(final VolatilitySurfaceData<Object, Object> volatilitySurface) {
    final Object[] expiries = getUniqueExpiriesWithData(volatilitySurface);
    final Object[] strikeValues = volatilitySurface.getYs();
    final int nExpiries = expiries.length;
    final int nStrikes = strikeValues.length;
    final double[][] strikes = new double[nExpiries][];
    final double[][] values = new double[nExpiries][];
    for (int i = 0; i < nExpiries; i++) {
      final DoubleList availableStrikes = new DoubleArrayList();
      final DoubleList availableVols = new DoubleArrayList();
      for (int j = 0; j < nStrikes; j++) {
        final Double vol = volatilitySurface.getVolatility(expiries[i], strikeValues[j]);
        if (vol != null) {
          availableStrikes.add((Double) strikeValues[j]);
          availableVols.add(vol);
        }
      }
      strikes[i] = availableStrikes.toDoubleArray();
      values[i] = availableVols.toDoubleArray();
    }
    return Triple.of(getArrayOfDoubles(expiries), strikes, values);
  }

  public static SmileSurfaceDataBundle getDataFromStandardQuotes(final ForwardCurve forwardCurve, final VolatilitySurfaceData<Object, Object> volatilitySurface) {
    final double[] uniqueExpiries = getUniqueExpiries(volatilitySurface);
    final double[] uniqueStrikes = getUniqueStrikes(volatilitySurface);
    final Pair<double[][], double[][]> strikesAndValues = getStrikesAndValues(uniqueExpiries, uniqueStrikes, volatilitySurface);
    // Convert vols and strikes to double[][],
    // noting that different expiries may have different populated strikes
    return new StandardSmileSurfaceDataBundle(forwardCurve, uniqueExpiries, strikesAndValues.getFirst(), strikesAndValues.getSecond());
  }

  public static SmileSurfaceDataBundle getDataFromStandardQuotes(final ForwardCurve forwardCurve, final VolatilitySurfaceData<Object, Object> volatilitySurface,
      final int minNumberOfStrikes) {
    final double[] uniqueExpiries = getUniqueExpiries(volatilitySurface);
    final double[] uniqueStrikes = getUniqueStrikes(volatilitySurface);
    final Triple<double[], double[][], double[][]> strikesAndValues = getStrikesAndValues(uniqueExpiries, uniqueStrikes, volatilitySurface, minNumberOfStrikes);
    // Convert vols and strikes to double[][],
    // noting that different expiries may have different populated strikes
    return new StandardSmileSurfaceDataBundle(forwardCurve, strikesAndValues.getFirst(), strikesAndValues.getSecond(), strikesAndValues.getThird());
  }

  public static ForexSmileDeltaSurfaceDataBundle getDataFromStrangleRiskReversalQuote(final ForwardCurve forwardCurve,
      final VolatilitySurfaceData<Tenor, Pair<Number, FXVolQuoteType>> fxVolatilitySurface) {
    final Object[] tenors = fxVolatilitySurface.getXs();
    Arrays.sort(tenors);
    final Object[] quotes = fxVolatilitySurface.getYs();
    final Number[] deltaValues = getDeltaValues(quotes);
    final int nExpiries = tenors.length;
    final int nDeltas = deltaValues.length - 1;
    final double[] expiries = new double[nExpiries];
    final double[] deltas = new double[nDeltas];
    final double[] atms = new double[nExpiries];
    final double[][] riskReversals = new double[nDeltas][nExpiries];
    final double[][] strangle = new double[nDeltas][nExpiries];
    for (int i = 0; i < nExpiries; i++) {
      final Tenor tenor = (Tenor) tenors[i];
      final double t = getTime(tenor);
      final Double atm = fxVolatilitySurface.getVolatility(tenor, ObjectsPair.of(deltaValues[0], FXVolQuoteType.ATM));
      if (atm == null) {
        throw new OpenGammaRuntimeException("Could not get ATM volatility data for surface");
      }
      expiries[i] = t;
      atms[i] = atm;
    }
    for (int i = 0; i < nDeltas; i++) {
      final Number delta = deltaValues[i + 1];
      if (delta != null) {
        deltas[i] = delta.doubleValue() / 100.;
        final DoubleArrayList riskReversalList = new DoubleArrayList();
        final DoubleArrayList strangleList = new DoubleArrayList();
        for (int j = 0; j < nExpiries; j++) {
          final Double rr = fxVolatilitySurface.getVolatility((Tenor) tenors[j], ObjectsPair.of(delta, FXVolQuoteType.RISK_REVERSAL));
          final Double s = fxVolatilitySurface.getVolatility((Tenor) tenors[j], ObjectsPair.of(delta, FXVolQuoteType.BUTTERFLY));
          if (rr != null && s != null) {
            riskReversalList.add(rr);
            strangleList.add(s);
          } else {
            s_logger.info("Had a null value for tenor number " + j);
          }
        }
        riskReversals[i] = riskReversalList.toDoubleArray();
        strangle[i] = strangleList.toDoubleArray();
      }
    }
    final boolean isCallData = true; //TODO this shouldn't be hard-coded
    return new ForexSmileDeltaSurfaceDataBundle(forwardCurve, expiries, deltas, atms, riskReversals, strangle, isCallData);
  }

  private static double getTime(final Tenor tenor) {
    final Period period = tenor.getPeriod();
    if (period.getYears() != 0) {
      return period.getYears();
    }
    if (period.getMonths() != 0) {
      return ((double) period.getMonths()) / 12;
    }
    if (period.getDays() != 0) {
      return ((double) period.getDays()) / 365;
    }
    throw new OpenGammaRuntimeException("Should never happen");
  }

  private static Number[] getDeltaValues(final Object[] quotes) {
    final TreeSet<Object> values = new TreeSet<Object>();
    for (final Object pair : quotes) {
      values.add(((Pair<?, ?>) pair).getFirst());
    }
    return values.toArray((Number[]) Array.newInstance(Number.class, values.size()));
  }

  private static double[] getArrayOfDoubles(final Object[] arrayOfObject) {
    final double[] expiries;
    //TODO there is sometimes a problem with Fudge, where a Double[] is transported as Object[]. Needs to be fixed
    final Object[] xData = arrayOfObject;
    final int n = xData.length;
    expiries = new double[n];
    for (int i = 0; i < n; i++) {
      final Object data = xData[i];
      if (data instanceof Double) {
        expiries[i] = (Double) xData[i];
      } else if (data instanceof Float) {
        expiries[i] = ((Float) xData[i]).doubleValue();
      } else if (data instanceof Long) {
        expiries[i] = ((Long) xData[i]).doubleValue();
      } else if (data instanceof Integer) {
        expiries[i] = ((Integer) xData[i]).doubleValue();
      } else if (data instanceof Byte) {
        expiries[i] = ((Byte) xData[i]).doubleValue();
      } else {
        throw new OpenGammaRuntimeException("Cannot cast " + data.getClass() + " to double");
      }
    }
    return expiries;
  }
}
