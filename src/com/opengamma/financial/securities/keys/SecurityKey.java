package com.opengamma.financial.securities.keys;

public interface SecurityKey {
  public <T> T accept(SecurityKeyVisitor<T> visitor);
}
