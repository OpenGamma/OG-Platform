package com.opengamma.financial.securities.keys;

public interface SecurityKeyVisitor<T> {
  public T visitEquityKey(EquityKey key);
  public T visitIndexKey(IndexKey key);
  public T visitFXKey(FXKey key);
  public T visitFutureKey(FutureKey key);
  public T visitCashKey(CashKey key);
}
