/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;
import org.joda.beans.BeanDefinition;
import org.joda.beans.PropertyDefinition;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.FirstThenSecondPairComparator;
import com.opengamma.util.tuple.FirstThenSecondThenThirdTripleComparator;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * Data structure to hold a particular volatility cube's data points.
 * Note no interpolation or fitting is done in this code.
 * 
 * @param <X> Type of the x-data
 * @param <Y> Type of the y-data
 * @param <Z> Type of the z-data
 */
@BeanDefinition
public class VolatilityCubeData<X, Y, Z> implements Serializable {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * Default name for the x axis.
   */
  public static final String DEFAULT_X_LABEL = "x";

  /**
   * Default name for the y axis.
   */
  public static final String DEFAULT_Y_LABEL = "y";

  /**
   * Default name for the z axis.
   */
  public static final String DEFAULT_Z_LABEL = "z";

  /** 
   * Comparator for the triples 
   */
  private static final Comparator<Triple<?, ?, ?>> TRIPLE_COMPARATOR = FirstThenSecondThenThirdTripleComparator.INSTANCE;

  /**
   * Comparator for the pairs.
   */
  private static final Comparator<Pair<?, ?>> COMPARATOR = FirstThenSecondPairComparator.INSTANCE;

  /**
   * The definition name.
   */
  @PropertyDefinition(validate = "notNull")
  private String _definitionName;

  /**
   * The specification name.
   */
  @PropertyDefinition(validate = "notNull")
  private String _specificationName;

  /**
   * A (x, y, z) to volatility map.
   */
  @PropertyDefinition(validate = "notNull", set = "manual")
  private Map<Triple<X, Y, Z>, Double> _values;

  /**
   * The x axis label.
   */
  @PropertyDefinition(validate = "notNull")
  private String _xLabel;

  /**
   * The y axis label.
   */
  @PropertyDefinition(validate = "notNull")
  private String _yLabel;

  /**
   * The z axis label
   */
  @PropertyDefinition(validate = "notNull")
  private String _zLabel;

  /**
   * The x values
   */
  private X[] _xs;

  /**
   * The y values
   */
  private Y[] _ys;

  /**
   * The z values
   */
  private Z[] _zs;

  /**
   * The volatilities
   */
  private Double[] _vs;

  /**
   * A set of unique x values
   */
  private SortedSet<X> _uniqueXs;

  /**
   * A set of unique y values
   */
  private SortedSet<Y> _uniqueYs;

  /**
   * A set of strips in the xy plane
   */
  private Map<X, Map<Y, List<ObjectsPair<Z, Double>>>> _strips;

  /**
   * For the builder.
   */
  /* package */VolatilityCubeData() {
  }

  /**
   * Constructor that uses the default axis labels.
   * @param definitionName The definition name, not null
   * @param specificationName The specification name, not null
   * @param values The (x, y, z, volatility) points, not null
   */
  public VolatilityCubeData(final String definitionName,
      final String specificationName,
      final Map<Triple<X, Y, Z>, Double> values) {
    this(definitionName, specificationName, DEFAULT_X_LABEL, DEFAULT_Y_LABEL, DEFAULT_Z_LABEL, values);
  }

  /**
   * @param definitionName The definition name, not null
   * @param specificationName The specification name, not null
   * @param xLabel The x axis label, not null
   * @param yLabel The y axis label, not null
   * @param zLabel The z axis label, not null
   * @param values The (x, y, z volatility) points, not null
   */
  public VolatilityCubeData(final String definitionName,
      final String specificationName,
      final String xLabel,
      final String yLabel,
      final String zLabel,
      final Map<Triple<X, Y, Z>, Double> values) {
    ArgumentChecker.notNull(values, "values");
    setDefinitionName(definitionName);
    setSpecificationName(specificationName);
    setXLabel(xLabel);
    setYLabel(yLabel);
    setZLabel(zLabel);
    init(values);
  }

  /**
   * Initializes data structures and divides the cube into x-y strips.
   * @param values The values, not null
   */
  private void init(final Map<Triple<X, Y, Z>, Double> values) {
    _values = values;
    _strips = new HashMap<>();
    _uniqueXs = new TreeSet<>();
    _uniqueYs = new TreeSet<>();
    final List<X> xs = new ArrayList<>();
    final List<Y> ys = new ArrayList<>();
    final List<Z> zs = new ArrayList<>();
    final List<Double> vs = new ArrayList<>();
    for (final Map.Entry<Triple<X, Y, Z>, Double> entries : values.entrySet()) {
      final X x = entries.getKey().getFirst();
      final Y y = entries.getKey().getSecond();
      final Z z = entries.getKey().getThird();
      if (!_strips.containsKey(x)) {
        final Map<Y, List<ObjectsPair<Z, Double>>> map = new HashMap<>();
        _strips.put(x, map);
        _uniqueXs.add(x);
      }
      if (!_strips.get(x).containsKey(y)) {
        final List<ObjectsPair<Z, Double>> list = new ArrayList<>();
        _strips.get(x).put(y, list);
        _uniqueYs.add(y);
      }
      xs.add(x);
      ys.add(y);
      zs.add(z);
      vs.add(entries.getValue());
      _strips.get(x).get(y).add(ObjectsPair.of(z, entries.getValue()));
    }
    _xs = (X[]) xs.toArray();
    _ys = (Y[]) ys.toArray();
    _zs = (Z[]) zs.toArray();
    _vs = vs.toArray(new Double[vs.size()]);
  }

  /**
   * Gets the number of points in this cube.
   * @return The number of points
   */
  public int size() {
    return _values.size();
  }

  /**
   * Gets the x values.
   * @return The x values.
   */
  public X[] getXs() {
    return _xs;
  }

  /**
   * Gets the y values.
   * @return The y values
   */
  public Y[] getYs() {
    return _ys;
  }

  /**
   * Gets the z values.
   * @return The z values
   */
  public Z[] getZs() {
    return _zs;
  }

  /**
   * Gets the volatilities.
   * @return The volatilities
   */
  public Double[] getVs() {
    return _vs;
  }

  /**
   * Gets the volatility for a particular x, y, z point.
   * @param x The x value
   * @param y The y value
   * @param z The z value
   * @return The volatility
   */
  public Double getVolatility(final X x, final Y y, final Z z) {
    return _values.get(Triple.of(x, y, z));
  }

  /**
   * Gets a sorted set of unique x values.
   * @return The unique x values
   */
  public SortedSet<X> getUniqueXValues() {
    return _uniqueXs;
  }

  /**
   * Gets a sorted set of unique y values.
   * @return The unique y values
   */
  public SortedSet<Y> getUniqueYValues() {
    return _uniqueYs;
  }

  /**
   * Gets a slice through the surface in the x, y plane.
   * @param x The x value, not null
   * @param y The y value
   * @return A slice of the cube
   */
  public List<ObjectsPair<Z, Double>> getZValuesForXandY(final X x, final Y y) {
    ArgumentChecker.notNull(x, "x");
    if (!_strips.containsKey(x)) {
      throw new OpenGammaRuntimeException("Could not get strip for x&y value " + x + ":" + y);
    }
    final Map<Y, List<ObjectsPair<Z, Double>>> result = _strips.get(x);
    if (result != null) {
      final List<ObjectsPair<Z, Double>> rr = result.get(y);
      Collections.sort(rr, COMPARATOR);
      return rr;
    }
    return Collections.emptyList();
  }

  /**
   * Gets the cube data as a map.
   * @return The cube data
   */
  public Map<Triple<X, Y, Z>, Double> asMap() {
    return _values;
  }

  /**
   * Sets a (x, y, z) to volatility map.
   * @param values  the new value of the property, not null
   */
  public void setValues(final Map<Triple<X, Y, Z>, Double> values) {
    ArgumentChecker.notNull(values, "values");
    init(values);
  }

  /**
   * Gets the definition name.
   * @return the definition name
   */
  public String getDefinitionName() {
    return _definitionName;
  }

  /**
   * Sets the definition name.
   * @param definitionName  the definition name
   */
  public void setDefinitionName(final String definitionName) {
    ArgumentChecker.notNull(definitionName, "definitionName");
    _definitionName = definitionName;
  }

  /**
   * Gets the specification name.
   * @return the specification name
   */
  public String getSpecificationName() {
    return _specificationName;
  }

  /**
   * Sets the specification name.
   * @param specificationName  the specification name
   */
  public void setSpecificationName(final String specificationName) {
    ArgumentChecker.notNull(specificationName, "specificationName");
    _specificationName = specificationName;
  }

  /**
   * Gets the x label.
   * @return the x label
   */
  public String getXLabel() {
    return _xLabel;
  }

  /**
   * Sets the x label.
   * @param xLabel  the x label
   */
  public void setXLabel(final String xLabel) {
    ArgumentChecker.notNull(xLabel, "xLabel");
    _xLabel = xLabel;
  }

  /**
   * Gets the y label.
   * @return the y label
   */
  public String getYLabel() {
    return _yLabel;
  }

  /**
   * Sets the y label.
   * @param yLabel  the y label
   */
  public void setYLabel(final String yLabel) {
    ArgumentChecker.notNull(yLabel, "yLabel");
    _yLabel = yLabel;
  }

  /**
   * Gets the z label.
   * @return the z label
   */
  public String getZLabel() {
    return _zLabel;
  }

  /**
   * Sets the z label.
   * @param zLabel  the z label
   */
  public void setZLabel(final String zLabel) {
    ArgumentChecker.notNull(zLabel, "zLabel");
    _zLabel = zLabel;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _definitionName.hashCode();
    result = prime * result + _specificationName.hashCode();
    result = prime * result + _values.hashCode();
    result = prime * result + _xLabel.hashCode();
    result = prime * result + _yLabel.hashCode();
    result = prime * result + _zLabel.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof VolatilityCubeData)) {
      return false;
    }
    final VolatilityCubeData<?, ?, ?> other = (VolatilityCubeData<?, ?, ?>) obj;
    if (!ObjectUtils.equals(_definitionName, other._definitionName)) {
      return false;
    }
    if (!ObjectUtils.equals(_specificationName, other._specificationName)) {
      return false;
    }
    if (!ObjectUtils.equals(_xLabel, other._xLabel)) {
      return false;
    }
    if (!ObjectUtils.equals(_yLabel, other._yLabel)) {
      return false;
    }
    if (!ObjectUtils.equals(_zLabel, other._zLabel)) {
      return false;
    }
    if (!ObjectUtils.equals(_values, other._values)) {
      return false;
    }
    return true;
  }

}
