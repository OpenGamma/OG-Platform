/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.collect.Lists;
import com.opengamma.id.ExternalId;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * A bundle of data to use to build a volatility cube.
 * <p>
 * This class is mutable and not thread-safe.
 */
public class VolatilityCubeData {

  /**
   * The volatilities in the bundle
   */
  private Map<VolatilityPoint, Double> _dataPoints;
  /**
   * The external ids of the market data points
   */
  private Map<VolatilityPoint, ExternalId> _dataIds;
  /**
   * The other market data needed
   */
  private SnapshotDataBundle _otherData;
  /**
   * The ATM strikes
   */
  private Map<Pair<Tenor, Tenor>, Double> _strikes;
  /**
   * The ATM vols
   */
  private Map<Pair<Tenor, Tenor>, Double> _atmVolatilities;
  /**
   * The smiles.
   */
  private SortedMap<Tenor, SortedMap<Tenor, Pair<double[], double[]>>> _smiles;
  /**
   * The ids of the smiles
   */
  private SortedMap<Tenor, SortedMap<Tenor, ExternalId[]>> _smileIds;
  /**
   * Gets the volatilities in the bundle.
   * 
   * @return the volatilities in the bundle
   */
  public Map<VolatilityPoint, Double> getDataPoints() {
    return _dataPoints;
  }

  /**
   * Sets the volatilities in the bundle.
   * 
   * @param dataPoints  the volatilities in the bundle
   */
  public void setDataPoints(Map<VolatilityPoint, Double> dataPoints) {
    _dataPoints = dataPoints;
  }

  /**
   * Gets the ids of the volatilities in the bundle.
   * 
   * @return the ids of the volatilities in the bundle
   */
  public Map<VolatilityPoint, ExternalId> getDataIds() {
    return _dataIds;
  }

  /**
   * Sets the ids of the volatilities in the bundle.
   * 
   * @param dataPoints  the ids of the volatilities in the bundle
   */
  public void setDataIds(Map<VolatilityPoint, ExternalId> dataIds) {
    _dataIds = dataIds;
  }
  
  /**
  * Gets the other market data needed.
  * 
  * @return the other data
  */
  public SnapshotDataBundle getOtherData() {
    return _otherData;
  }

  /**
   * Sets the other market data needed.
   * 
   * @param otherData  the other data
   */
  public void setOtherData(SnapshotDataBundle otherData) {
    _otherData = otherData;
  }

  /**
   * Gets the ATM strikes.
   * 
   * @return the strikes
   */
  public Map<Pair<Tenor, Tenor>, Double> getStrikes() {
    return _strikes;
  }

  /**
   * Sets the ATM strikes.
   * 
   * @param strikes  the strikes
   */
  public void setStrikes(Map<Pair<Tenor, Tenor>, Double> strikes) {
    _strikes = strikes;
  }

  /**
   * Gets the ATM volatilities.
   * 
   * @return the volatilities
   */
  public Map<Pair<Tenor, Tenor>, Double> getATMVolatilities() {
    return _atmVolatilities;
  }

  /**
   * Sets the ATM volatilities.
   * 
   * @param atmVolatilities the volatilities
   */
  public void setATMVolatilities(Map<Pair<Tenor, Tenor>, Double> atmVolatilities) {
    _atmVolatilities = atmVolatilities;
  }

  /**
   * Gets the smiles field.
   * Swap Tenor -> Option Expiry -> (relative strikes in bps[], volatility[]).
   * 
   * @return the smiles
   */
  public SortedMap<Tenor, SortedMap<Tenor, Pair<double[], double[]>>> getSmiles() {
    // TODO: this is slow.  Would start to matter if we got more data
    // Could avoid it on deserialization, which is the repeated case
    _smiles = _smiles == null ? getSmiles(_dataPoints) : _smiles;
    return _smiles;
  }

  /**
   * Gets the smiles and smile ids fields.
   * smile = Swap Tenor -> Option Expiry -> (relative strikes in bps[], volatility[]).
   * smileId = Swap Tenor -> Option Expiry -> ExternalIds[]
   * @return the smiles and smile ids
   */
  public Pair<SortedMap<Tenor, SortedMap<Tenor, Pair<double[], double[]>>>, SortedMap<Tenor, SortedMap<Tenor, ExternalId[]>>> getSmilesAndSmileIds() {
    // TODO: this is slow.  Would start to matter if we got more data
    // Could avoid it on deserialization, which is the repeated case    
    final Pair<SortedMap<Tenor, SortedMap<Tenor, Pair<double[], double[]>>>, SortedMap<Tenor, SortedMap<Tenor, ExternalId[]>>> result = _smiles == null ? getSmilesAndIds(_dataPoints, _dataIds) : 
      Pair.of(_smiles, _smileIds);
    _smiles = result.getFirst();
    _smileIds = result.getSecond();
    return result;
  }
  
  public static Pair<SortedMap<Tenor, SortedMap<Tenor, Pair<double[], double[]>>>, SortedMap<Tenor, SortedMap<Tenor, ExternalId[]>>> getSmilesAndIds(final Map<VolatilityPoint, Double> dataPoints,
      final Map<VolatilityPoint, ExternalId> dataIds) {
    if (dataPoints == null) {
      return null;
    }
    ArrayList<Entry<VolatilityPoint, Double>> entries = Lists.newArrayList(dataPoints.entrySet());
    Collections.sort(entries, new Comparator<Entry<VolatilityPoint, Double>>() {

      @Override
      public int compare(Entry<VolatilityPoint, Double> o1, Entry<VolatilityPoint, Double> o2) {
        int compareTo = compare(o1.getKey().getSwapTenor(), o2.getKey().getSwapTenor());
        if (compareTo != 0) {
          return compareTo;
        }
        compareTo = compare(o1.getKey().getOptionExpiry(), o2.getKey().getOptionExpiry());
        if (compareTo != 0) {
          return compareTo;
        }

        return Double.compare(o1.getKey().getRelativeStrike(), o2.getKey().getRelativeStrike());
      }

      private int compare(Tenor a, Tenor b) {
        //TODO [PLAT-1013] Tenors should be comparable, but they're not
        int compareTo = a.getPeriod().toString().compareTo(b.getPeriod().toString());
        return compareTo;
      }
    });
    
    SortedMap<Tenor, SortedMap<Tenor, Pair<double[], double[]>>> smiles = new TreeMap<Tenor, SortedMap<Tenor, Pair<double[], double[]>>>();
    SortedMap<Tenor, SortedMap<Tenor, ExternalId[]>> smileIds = new TreeMap<Tenor, SortedMap<Tenor, ExternalId[]>>();
    Tenor currentSwapTenor = null;
    Tenor currentOptionExpiry = null;
    ArrayList<Double> strikes = null;
    ArrayList<Double> vols = null;
    ArrayList<ExternalId> ids = null;
    
    for (Entry<VolatilityPoint, Double> entry : entries) {
      Tenor swapTenor = entry.getKey().getSwapTenor();
      Tenor optionExpiry = entry.getKey().getOptionExpiry();
      boolean newSwapTenor = !swapTenor.equals(currentSwapTenor);
      boolean newExpiry = !optionExpiry.equals(currentOptionExpiry);
      
      if (newSwapTenor) {
        if (!smiles.containsKey(swapTenor)) {
          smiles.put(swapTenor, new TreeMap<Tenor, Pair<double[], double[]>>());
          smileIds.put(swapTenor, new TreeMap<Tenor, ExternalId[]>());
        }
      }
      if (newSwapTenor || newExpiry) {
        if (currentOptionExpiry != null) {
          ObjectsPair<ObjectsPair<double[], double[]>, ExternalId[]> pairsAndIds = getPair(strikes, vols, ids);
          smiles.get(currentSwapTenor).put(currentOptionExpiry, pairsAndIds.getFirst());
          smileIds.get(currentSwapTenor).put(currentOptionExpiry, pairsAndIds.getSecond());
        }
        
        currentSwapTenor = swapTenor;
        currentOptionExpiry = optionExpiry;
        strikes = new ArrayList<Double>();
        vols = new ArrayList<Double>();
        ids = new ArrayList<ExternalId>();
      }
      
      strikes.add(entry.getKey().getRelativeStrike());
      vols.add(entry.getValue());
      ids.add(dataIds.get(entry.getKey()));
    }
    
    if (currentOptionExpiry != null) {
      ObjectsPair<ObjectsPair<double[], double[]>, ExternalId[]> pairsAndIds = getPair(strikes, vols, ids);
      smiles.get(currentSwapTenor).put(currentOptionExpiry, pairsAndIds.getFirst());
      smileIds.get(currentSwapTenor).put(currentOptionExpiry, pairsAndIds.getSecond());
    }
    return Pair.of(smiles, smileIds);
  }

  public static SortedMap<Tenor, SortedMap<Tenor, Pair<double[], double[]>>> getSmiles(Map<VolatilityPoint, Double> dataPoints) {
    if (dataPoints == null) {
      return null;
    }
    ArrayList<Entry<VolatilityPoint, Double>> entries = Lists.newArrayList(dataPoints.entrySet());
    Collections.sort(entries, new Comparator<Entry<VolatilityPoint, Double>>() {

      @Override
      public int compare(Entry<VolatilityPoint, Double> o1, Entry<VolatilityPoint, Double> o2) {
        int compareTo = compare(o1.getKey().getSwapTenor(), o2.getKey().getSwapTenor());
        if (compareTo != 0) {
          return compareTo;
        }
        compareTo = compare(o1.getKey().getOptionExpiry(), o2.getKey().getOptionExpiry());
        if (compareTo != 0) {
          return compareTo;
        }

        return Double.compare(o1.getKey().getRelativeStrike(), o2.getKey().getRelativeStrike());
      }

      private int compare(Tenor a, Tenor b) {
        //TODO [PLAT-1013] Tenors should be comparable, but they're not
        int compareTo = a.getPeriod().toString().compareTo(b.getPeriod().toString());
        return compareTo;
      }
    });
    
    SortedMap<Tenor, SortedMap<Tenor, Pair<double[], double[]>>> ret = new TreeMap<Tenor, SortedMap<Tenor, Pair<double[], double[]>>>();
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
          ret.put(swapTenor, new TreeMap<Tenor, Pair<double[], double[]>>());
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

  private static ObjectsPair<ObjectsPair<double[], double[]>, ExternalId[]> getPair(ArrayList<Double> strikes, ArrayList<Double> vols, ArrayList<ExternalId> ids) {
    double[] nStrikes = getNativeArray(strikes);
    double[] nVols = getNativeArray(vols);
    return Pair.of(Pair.of(nStrikes, nVols), ids.toArray(new ExternalId[ids.size()]));
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

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    final SortedMap<Tenor, SortedMap<Tenor, Pair<double[], double[]>>> smiles = getSmiles();
    for (final SortedMap.Entry<Tenor, SortedMap<Tenor, Pair<double[], double[]>>> e1 : smiles.entrySet()) {
      sb.append("Swap tenor = " + e1.getKey() + "\n");
      for (final Map.Entry<Tenor, Pair<double[], double[]>> e2 : e1.getValue().entrySet()) {
        sb.append("\t" + e2.getKey() + "\t");
        final double[] strikes = e2.getValue().getFirst();
        final double[] vols = e2.getValue().getSecond();
        for (double strike : strikes) {
          sb.append("\t f=" + strike);
        }
        sb.append("\n");
        sb.append("\t\t\t\t");
        for (double vol : vols) {
          sb.append("\t sigma=" + vol);
        }
        sb.append("\n");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

}
