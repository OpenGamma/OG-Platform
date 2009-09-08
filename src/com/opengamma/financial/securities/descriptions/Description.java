package com.opengamma.financial.securities.descriptions;

public interface Description<K> {
  public <T> T accept(DescriptionVisitor<T> visitor);
  public K toKey();
}
