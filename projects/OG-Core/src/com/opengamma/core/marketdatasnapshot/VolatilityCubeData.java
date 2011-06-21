/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Lists;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * A bundle of data to use to build a vol cube
 */
public class VolatilityCubeData {

  /**
   * The volatilities in the bundle
   */
  private Map<VolatilityPoint, Double> _dataPoints;
  
  /**
   * The other market data needed (spot rates etc.)
   */
  private SnapshotDataBundle _otherData;

  
  private Map<Tenor, Map<Tenor, Pair<double[], double[]>>> _smiles;
  
  
  
  /**
   * Gets The volatilities in the bundle
   * @return The volatilities in the bundle
   */
  public Map<VolatilityPoint, Double> getDataPoints() {
    return _dataPoints;
  }

  /**
   * SetsThe volatilities in the bundle
   * @param dataPoints The volatilities in the bundle
   */
  public void setDataPoints(Map<VolatilityPoint, Double> dataPoints) {
    _dataPoints = dataPoints;
  }
  
    /**
   * Gets The other market data needed (spot rates etc.)
   * @return the otherData
   */
  public SnapshotDataBundle getOtherData() {
    return _otherData;
  }

  /**
   * Sets The other market data needed (spot rates etc.)
   * @param otherData  the otherData
   */
  public void setOtherData(SnapshotDataBundle otherData) {
    _otherData = otherData;
  }

  /**
   * Gets the smiles field.
   * Swap Tenor -> Option Expiry -> (relative strikes in bps[], volatility[])
   * @return the smiles
   */
  public Map<Tenor, Map<Tenor, Pair<double[], double[]>>> getSmiles() {
    //TODO: this is slow.  Would start to matter if we got more data
    // Could avoid it on deserialization, which is the repeatead case

    _smiles = _smiles == null ? getSmiles(_dataPoints) : _smiles;
    return _smiles;
  }

  public static Map<Tenor, Map<Tenor, Pair<double[], double[]>>> getSmiles(Map<VolatilityPoint, Double> dataPoints) {
    if (dataPoints == null) {
      return null;
    }
    
    ArrayList<Entry<VolatilityPoint, Double>> entries = Lists.newArrayList(dataPoints.entrySet());

    Collections.sort(entries, new Comparator<Entry<VolatilityPoint, Double>>() {

      @Override
      public int compare(Entry<VolatilityPoint, Double> o1, Entry<VolatilityPoint, Double> o2) {
        int compareTo = o1.getKey().getSwapTenor().compareTo(o2.getKey().getSwapTenor());
        if (compareTo != 0) {
          return compareTo;
        }
        compareTo = o1.getKey().getOptionExpiry().compareTo(o2.getKey().getOptionExpiry());
        if (compareTo != 0) {
          return compareTo;
        }

        return Double.compare(o1.getKey().getRelativeStrike(), o2.getKey().getRelativeStrike());
      }
    });
    
    
    Map<Tenor, Map<Tenor, Pair<double[], double[]>>> ret = new HashMap<Tenor, Map<Tenor, Pair<double[], double[]>>>();
    
    Tenor currentSwapTenor = null;
    Tenor currentOptionExpiry = null;
    ArrayList<Double> strikes = null;
    ArrayList<Double> vols = null;
    
    for (Entry<VolatilityPoint, Double> entry : entries) {
      Tenor swapTenor = entry.getKey().getSwapTenor();
      Tenor optionExpiry = entry.getKey().getOptionExpiry();
      boolean newSwapTenor = !swapTenor.equals(currentSwapTenor);
      boolean newExpiry = !optionExpiry.equals(currentOptionExpiry);
      
      if (newSwapTenor) {
        if (!ret.containsKey(swapTenor)) {
          ret.put(swapTenor, new HashMap<Tenor, Pair<double[], double[]>>());
        }
      }
      if (newSwapTenor || newExpiry) {
        if (currentOptionExpiry != null) {
          ret.get(currentSwapTenor).put(currentOptionExpiry, getPair(strikes, vols));
        }
        
        currentSwapTenor = swapTenor;
        currentOptionExpiry = optionExpiry;
        strikes = new ArrayList<Double>();
        vols = new ArrayList<Double>();
      }
      
      strikes.add(entry.getKey().getRelativeStrike());
      vols.add(entry.getValue());
    }
    
    if (currentOptionExpiry != null) {
      ret.get(currentSwapTenor).put(currentOptionExpiry, getPair(strikes, vols));
    }
    return ret;
  }
  
  private static Pair<double[], double[]> getPair(ArrayList<Double> strikes, ArrayList<Double> vols) {
    double[] nStrikes = getNativeArray(strikes);
    double[] nVols = getNativeArray(vols);
    return Pair.of(nStrikes, nVols);
  }

  private static double[] getNativeArray(ArrayList<Double> strikes) {
    double[] ret = new double[strikes.size()];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = strikes.get(i);
    }
    return ret;
  }

  public Set<Tenor> getSwapTenors() {
    return getSmiles().keySet();
  }
}
