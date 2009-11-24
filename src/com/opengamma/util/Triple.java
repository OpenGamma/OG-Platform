/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;

/**
 * 
 *
 * @author emcleod
 */
public class Triple<S,T,U> implements Serializable, Comparable<Triple<S, T, U>>{
  private final S _first;
  private final T _second;
  private final U _third;
  
  public Triple(S first, T second, U third) {
    _first = first;
    _second = second;
    _third = third;
  }
  
  public S getFirst() {
    return _first;
  }
  
  public T getSecond() {
    return _second;
  }
  
  public U getThird() {
    return _third;
  }
  
  public int compareTo(Triple<S, T, U> o) {
    return new CompareToBuilder().append(_first, o._first).append(_second, o._second).append(_third, o._third).toComparison();
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Triple[");
    sb.append(_first);
    sb.append(", ");
    sb.append(_second);
    sb.append(", ");
    sb.append(_third);
    sb.append("]");
    return sb.toString();
  }
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_first == null) ? 0 : _first.hashCode());
    result = prime * result + ((_second == null) ? 0 : _second.hashCode());
    result = prime * result + ((_third == null) ? 0 : _third.hashCode());
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Triple other = (Triple) obj;
    if (_first == null) {
      if (other._first != null)
        return false;
    } else if (!_first.equals(other._first))
      return false;
    if (_second == null) {
      if (other._second != null)
        return false;
    } else if (!_second.equals(other._second))
      return false;
    if (_third == null) {
      if (other._third != null)
        return false;
    } else if (!_third.equals(other._third))
      return false;
    return true;
  }
}

