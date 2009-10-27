package com.opengamma.financial.greeks;

public class OrderGreekVisitor implements GreekVisitor<Order> {

  @Override
  public Order visitDelta() {
    return Order.FIRST;
  }

  @Override
  public Order visitGamma() {
    return Order.SECOND;
  }

  @Override
  public Order visitPrice() {
    return Order.ZEROTH;
  }

  @Override
  public Order visitRho() {
    return Order.FIRST;
  }

  @Override
  public Order visitTheta() {
    return Order.FIRST;
  }

  @Override
  public Order visitTimeBucketedRho() {
    return Order.FIRST;
  }

  @Override
  public Order visitVega() {
    return Order.FIRST;
  }

}
