/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import com.opengamma.util.ArgumentChecker;

public class MultipleGreekResult implements GreekResult<Map<String, Double>> {
  // REVIEW kirk 2010-05-21 -- Something in Fastutil instead?
  private final Map<String, Double> _result;

  public MultipleGreekResult(final Map<String, Double> result) {
    ArgumentChecker.notNull(result, "Results");
    if (result.isEmpty())
      throw new IllegalArgumentException("Result was empty");
    // REVIEW kirk 2010-05-21 -- Why not take a copy and make it unmodifiable?
    _result = result;
  }

  @Override
  public Map<String, Double> getResult() {
    return _result;
  }

  @Override
  public boolean isMultiValued() {
    return true;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MultipleGreekResult)) {
      return false;
    }
    final MultipleGreekResult other = (MultipleGreekResult) obj;
    return ObjectUtils.equals(_result, other._result);
  }

  @Override
  public int hashCode() {
    return _result.hashCode();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("MultipleGreekResult[");
    final List<String> elements = new LinkedList<String>();
    for (final Map.Entry<String, Double> entry : _result.entrySet()) {
      final StringBuilder sbElem = new StringBuilder();
      sbElem.append(entry.getKey()).append("=").append(entry.getValue());
      elements.add(sbElem.toString());
    }
    sb.append(StringUtils.join(elements, ", "));
    sb.append("]");
    return sb.toString();
  }

}
