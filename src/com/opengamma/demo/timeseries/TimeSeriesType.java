package com.opengamma.demo.timeseries;

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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TimeSeriesType)) return false;
    TimeSeriesType other = (TimeSeriesType) o;
    return other._label.equals(_label) && other._type.equals(_type);
  }

  @Override
  public int compareTo(TimeSeriesType o) {
    if (_label.equals(o._label)) return _type.toString().compareTo(o._type.toString());
    return _label.compareTo(o._label);
  }
}
