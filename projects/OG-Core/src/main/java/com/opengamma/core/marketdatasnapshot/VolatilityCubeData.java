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
import com.google.common.collect.Maps;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

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
  private Map<VolatilityPoint, ExternalIdBundle> _dataIds;
  /**
   * The relative strikes of the market data points;
   */
  private Map<VolatilityPoint, Double> _relativeStrikes;
  /**
   * The other market data needed
   */
  private SnapshotDataBundle _otherData;
  /**
   * The ATM strikes
   */
  private Map<Pair<Tenor, Tenor>, Double> _atmStrikes;
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
   * The relative strikes of the smiles
   */
  private SortedMap<Tenor, SortedMap<Tenor, Double[]>> _smileRelativeStrikes;

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
   * @param dataPoints the volatilities in the bundle
   */
  public void setDataPoints(final Map<VolatilityPoint, Double> dataPoints) {
    _dataPoints = dataPoints;
  }

  /**
   * Gets the ids of the volatilities in the bundle.
   * 
   * @return the ids of the volatilities in the bundle
   */
  public Map<VolatilityPoint, ExternalIdBundle> getDataIds() {
    return _dataIds;
  }

  /**
   * Sets the ids of the volatilities in the bundle.
   * 
   * @param dataIds the ids of the volatilities in the bundle
   */
  public void setDataIds(final Map<VolatilityPoint, ExternalIdBundle> dataIds) {
    _dataIds = dataIds;
  }

  /**
   * Sets the relative strikes of the volatilities in the bundle.
   * 
   * @param relativeStrikes the relative strikes of the volatilities in the bundle
   */
  public void setRelativeStrikes(final Map<VolatilityPoint, Double> relativeStrikes) {
    _relativeStrikes = relativeStrikes;
  }

  /**
   * Gets the relative strikes of the volatilities in the bundle.
   * 
   * @return the relative strikes of the volatilities in the bundle
   */
  public Map<VolatilityPoint, Double> getRelativeStrikes() {
    return _relativeStrikes;
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
   * @param otherData the other data
   */
  public void setOtherData(final SnapshotDataBundle otherData) {
    _otherData = otherData;
  }

  /**
   * Gets the ATM strikes.
   * 
   * @return the ATM strikes
   */
  public Map<Pair<Tenor, Tenor>, Double> getATMStrikes() {
    return _atmStrikes;
  }

  /**
   * Sets the ATM strikes.
   * 
   * @param atmStrikes the ATM strikes
   */
  public void setATMStrikes(final Map<Pair<Tenor, Tenor>, Double> atmStrikes) {
    _atmStrikes = atmStrikes;
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
  public void setATMVolatilities(final Map<Pair<Tenor, Tenor>, Double> atmVolatilities) {
    _atmVolatilities = atmVolatilities;
  }

  /**
   * Gets the smiles field. Swap Tenor -> Option Expiry -> (relative strikes in bps[], volatility[]).
   * 
   * @return the smiles
   */
  @SuppressWarnings("unchecked")
  public SortedMap<Tenor, SortedMap<Tenor, Pair<double[], double[]>>> getSmiles() {
    // TODO: this is slow.  Would start to matter if we got more data
    // Could avoid it on deserialization, which is the repeated case
    if (_smiles == null) {
      @SuppressWarnings("rawtypes")
      final Map[] smileArray = createSmiles(_dataPoints, _dataIds, _relativeStrikes);
      _smiles = (SortedMap<Tenor, SortedMap<Tenor, Pair<double[], double[]>>>) smileArray[0];
      _smileIds = (SortedMap<Tenor, SortedMap<Tenor, ExternalId[]>>) smileArray[1];
      _smileRelativeStrikes = (SortedMap<Tenor, SortedMap<Tenor, Double[]>>) smileArray[2];
    }
    return _smiles;
  }

  /**
   * Gets the smile ids field. Swap Tenor -> Option Expiry -> (External Id[]).
   * 
   * @return the smiles
   */
  @SuppressWarnings("unchecked")
  public SortedMap<Tenor, SortedMap<Tenor, ExternalId[]>> getSmileIds() {
    // TODO: this is slow.  Would start to matter if we got more data
    // Could avoid it on deserialization, which is the repeated case
    if (_dataIds == null) {
      throw new UnsupportedOperationException("Data ids were not set");
    }
    if (_smiles == null) {
      @SuppressWarnings("rawtypes")
      final Map[] smileArray = createSmiles(_dataPoints, _dataIds, _relativeStrikes);
      _smiles = (SortedMap<Tenor, SortedMap<Tenor, Pair<double[], double[]>>>) smileArray[0];
      _smileIds = (SortedMap<Tenor, SortedMap<Tenor, ExternalId[]>>) smileArray[1];
      _smileRelativeStrikes = (SortedMap<Tenor, SortedMap<Tenor, Double[]>>) smileArray[2];
    }
    return _smileIds;
  }

  /**
   * Gets the smile relative strikes field. Swap Tenor -> Option Expiry -> (relative strike[]).
   * 
   * @return the smiles
   */
  @SuppressWarnings("unchecked")
  public SortedMap<Tenor, SortedMap<Tenor, Double[]>> getSmileRelativeStrikes() {
    // TODO: this is slow.  Would start to matter if we got more data
    // Could avoid it on deserialization, which is the repeated case
    if (_relativeStrikes == null) {
      throw new UnsupportedOperationException("Relative strikes data were not set");
    }
    if (_smiles == null) {
      @SuppressWarnings("rawtypes")
      final Map[] smileArray = createSmiles(_dataPoints, _dataIds, _relativeStrikes);
      _smiles = (SortedMap<Tenor, SortedMap<Tenor, Pair<double[], double[]>>>) smileArray[0];
      _smileIds = (SortedMap<Tenor, SortedMap<Tenor, ExternalId[]>>) smileArray[1];
      _smileRelativeStrikes = (SortedMap<Tenor, SortedMap<Tenor, Double[]>>) smileArray[2];
    }
    return _smileRelativeStrikes;
  }

  @SuppressWarnings("rawtypes")
  private static Map[] createSmiles(final Map<VolatilityPoint, Double> dataPoints, final Map<VolatilityPoint, ExternalIdBundle> dataIds, final Map<VolatilityPoint, Double> relativeStrikePoints) {
    if (dataPoints == null) {
      return new Map[] {null, null, null };
    }
    final ArrayList<Entry<VolatilityPoint, Double>> entries = Lists.newArrayList(dataPoints.entrySet());
    Collections.sort(entries, new Comparator<Entry<VolatilityPoint, Double>>() {

      @Override
      public int compare(final Entry<VolatilityPoint, Double> o1, final Entry<VolatilityPoint, Double> o2) {
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

    final SortedMap<Tenor, SortedMap<Tenor, Pair<double[], double[]>>> smiles = Maps.newTreeMap();
    final SortedMap<Tenor, SortedMap<Tenor, ExternalId[]>> smileIds = Maps.newTreeMap();
    final SortedMap<Tenor, SortedMap<Tenor, Double[]>> smileRelativeStrikes = Maps.newTreeMap();
    Tenor currentSwapTenor = null;
    Tenor currentOptionExpiry = null;
    ArrayList<Double> strikes = null;
    ArrayList<Double> vols = null;
    ArrayList<ExternalId> ids = null;
    ArrayList<Double> relativeStrikes = null;

    for (final Entry<VolatilityPoint, Double> entry : entries) {
      final Tenor swapTenor = entry.getKey().getSwapTenor();
      final Tenor optionExpiry = entry.getKey().getOptionExpiry();
      final boolean newSwapTenor = !swapTenor.equals(currentSwapTenor);
      final boolean newExpiry = !optionExpiry.equals(currentOptionExpiry);

      if (newSwapTenor) {
        if (!smiles.containsKey(swapTenor)) {
          smiles.put(swapTenor, new TreeMap<Tenor, Pair<double[], double[]>>());
          smileIds.put(swapTenor, new TreeMap<Tenor, ExternalId[]>());
          smileRelativeStrikes.put(swapTenor, new TreeMap<Tenor, Double[]>());
        }
      }
      if (newSwapTenor || newExpiry) {
        if (currentOptionExpiry != null) {
          smiles.get(currentSwapTenor).put(currentOptionExpiry, Pairs.of(getNativeArray(strikes), getNativeArray(vols)));
          smileIds.get(currentSwapTenor).put(currentOptionExpiry, ids.toArray(new ExternalId[ids.size()]));
          smileRelativeStrikes.get(currentSwapTenor).put(currentOptionExpiry, relativeStrikes.toArray(new Double[relativeStrikes.size()]));
        }

        currentSwapTenor = swapTenor;
        currentOptionExpiry = optionExpiry;
        strikes = Lists.newArrayList();
        vols = Lists.newArrayList();
        ids = Lists.newArrayList();
        relativeStrikes = Lists.newArrayList();
      }
      strikes.add(entry.getKey().getRelativeStrike());
      vols.add(entry.getValue());
      if (dataIds != null) {
        final ExternalIdBundle identifiers = dataIds.get(entry.getKey());
        if (identifiers != null) {
          ids.addAll(identifiers.getExternalIds());
        }
      }
      if (relativeStrikePoints != null) {
        relativeStrikes.add(relativeStrikePoints.get(entry.getKey()));
      }
    }

    if (currentOptionExpiry != null) {
      smiles.get(currentSwapTenor).put(currentOptionExpiry, Pairs.of(getNativeArray(strikes), getNativeArray(vols)));
      if (dataIds != null) {
        smileIds.get(currentSwapTenor).put(currentOptionExpiry, ids.toArray(new ExternalId[ids.size()]));
      }
      if (relativeStrikePoints != null) {
        smileRelativeStrikes.get(currentSwapTenor).put(currentOptionExpiry, relativeStrikes.toArray(new Double[relativeStrikes.size()]));
      }
    }
    return new Map[] {smiles, smileIds, smileRelativeStrikes };
  }

  public static SortedMap<Tenor, SortedMap<Tenor, Pair<double[], double[]>>> getSmiles(final Map<VolatilityPoint, Double> dataPoints) {
    if (dataPoints == null) {
      return null;
    }
    final ArrayList<Entry<VolatilityPoint, Double>> entries = Lists.newArrayList(dataPoints.entrySet());
    Collections.sort(entries, new Comparator<Entry<VolatilityPoint, Double>>() {

      @Override
      public int compare(final Entry<VolatilityPoint, Double> o1, final Entry<VolatilityPoint, Double> o2) {
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

    final SortedMap<Tenor, SortedMap<Tenor, Pair<double[], double[]>>> ret = Maps.newTreeMap();
    Tenor currentSwapTenor = null;
    Tenor currentOptionExpiry = null;
    ArrayList<Double> strikes = null;
    ArrayList<Double> vols = null;

    for (final Entry<VolatilityPoint, Double> entry : entries) {
      final Tenor swapTenor = entry.getKey().getSwapTenor();
      final Tenor optionExpiry = entry.getKey().getOptionExpiry();
      final boolean newSwapTenor = !swapTenor.equals(currentSwapTenor);
      final boolean newExpiry = !optionExpiry.equals(currentOptionExpiry);

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
        strikes = Lists.newArrayList();
        vols = Lists.newArrayList();
      }

      strikes.add(entry.getKey().getRelativeStrike());
      vols.add(entry.getValue());
    }

    if (currentOptionExpiry != null) {
      ret.get(currentSwapTenor).put(currentOptionExpiry, getPair(strikes, vols));
    }
    return ret;
  }

  private static Pair<double[], double[]> getPair(final ArrayList<Double> strikes, final ArrayList<Double> vols) {
    final double[] nStrikes = getNativeArray(strikes);
    final double[] nVols = getNativeArray(vols);
    return Pairs.of(nStrikes, nVols);
  }

  private static double[] getNativeArray(final ArrayList<Double> strikes) {
    final double[] ret = new double[strikes.size()];
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
    final StringBuilder sb = new StringBuilder();
    final SortedMap<Tenor, SortedMap<Tenor, Pair<double[], double[]>>> smiles = getSmiles();
    for (final SortedMap.Entry<Tenor, SortedMap<Tenor, Pair<double[], double[]>>> e1 : smiles.entrySet()) {
      sb.append("Swap tenor = " + e1.getKey() + "\n");
      for (final Map.Entry<Tenor, Pair<double[], double[]>> e2 : e1.getValue().entrySet()) {
        sb.append("\t" + e2.getKey() + "\t");
        final double[] strikes = e2.getValue().getFirst();
        final double[] vols = e2.getValue().getSecond();
        for (final double strike : strikes) {
          sb.append("\t f=" + strike);
        }
        sb.append("\n");
        sb.append("\t\t\t\t");
        for (final double vol : vols) {
          sb.append("\t sigma=" + vol);
        }
        sb.append("\n");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

}
