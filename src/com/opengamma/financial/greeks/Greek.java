package com.opengamma.financial.greeks;

public interface Greek {

  public String getName();

  public <T> T accept(GreekVisitor<T> visitor);
}
