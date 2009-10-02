package com.opengamma.financial.greeks;

public interface GreekResult<T> {
  public boolean isMultiValued();
  public T getResult();
}
