/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.sensitivity;

import java.util.HashMap;
import java.util.Map;

public class MultipleValueGreekResult implements ValueGreekResult<Map<String, Double>> {
  private final Map<String, Double> _result;

  public MultipleValueGreekResult(final Map<String, Double> result) {
    if (result == null)
      throw new IllegalArgumentException("Result map was null");
    if (result.isEmpty())
      throw new IllegalArgumentException("Result map was empty");
    _result = new HashMap<String, Double>();
    _result.putAll(result);
  }

  @Override
  public Map<String, Double> getResult() {
    return _result;
  }

  @Override
  public boolean isMultiValued() {
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_result == null) ? 0 : _result.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final MultipleValueGreekResult other = (MultipleValueGreekResult) obj;
    if (_result == null) {
      if (other._result != null)
        return false;
    } else if (!_result.equals(other._result))
      return false;
    return true;
  }

}
