package com.opengamma.demo.timeseries;

import org.apache.commons.lang.ObjectUtils;

public class TimeSeriesType implements Comparable<TimeSeriesType> {
  public static enum Type {
    STATISTICAL, GENERATED, DATA
  }

  private String _label;
  private Type _type;

  public TimeSeriesType(String label, Type type) {
    _label = label;
    _type = type;
  }

  public String getLabel() {
    return _label;
  }

  public Type getType() {
    return _type;
  }

  @Override
  public String toString() {
    return _label;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 0;
    result = (result * prime) + _label.hashCode();
    result = (result * prime) + _type.hashCode();
    return result;
  }

@Override
  public boolean equals(Object o) {
    if(this == o) {
      return true;
    }
    if(o == null) {
      return false;
    }
    if(!(o instanceof TimeSeriesType)) {
      return false;
    }
    TimeSeriesType other = (TimeSeriesType) o;
    if(!ObjectUtils.equals(_label, other._label)) {
      return false;
    }
    if(!ObjectUtils.equals(_type, other._type)) {
      return false;
    }
    return true;
  }

  @Override
  public int compareTo(TimeSeriesType o) {
    if (_label.equals(o._label)) return _type.toString().compareTo(o._type.toString());
    return _label.compareTo(o._label);
  }

}
