package com.opengamma.financial.greeks;

public class OrderGreekVisitor implements GreekVisitor<Order> {

  @Override
  public Order visitPrice() {
    return Order.ZEROTH;
  }

  @Override
  public Order visitZeta() {
    return Order.ZEROTH;
  }

  @Override
  public Order visitCarryRho() {
    return Order.FIRST;
  }

  @Override
  public Order visitDelta() {
    return Order.FIRST;
  }

  @Override
  public Order visitDriftlessTheta() {
    return Order.FIRST;
  }

  @Override
  public Order visitDZetaDVol() {
    return Order.FIRST;
  }

  public Order visitGamma() {
    return Order.SECOND;
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

  @Override
  public Order visitDDeltaDVar() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Order visitDeltaBleed() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Order visitElasticity() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Order visitGammaBleed() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Order visitGammaP() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Order visitGammaPBleed() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Order visitPhi() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Order visitSpeed() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Order visitSpeedP() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Order visitStrikeDelta() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Order visitStrikeGamma() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Order visitUltima() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Order visitVanna() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Order visitVarianceUltima() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Order visitVarianceVega() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Order visitVarianceVomma() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Order visitVegaBleed() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Order visitVegaP() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Order visitVomma() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Order visitVommaP() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Order visitZetaBleed() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Order visitZomma() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Order visitZommaP() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Order visitDVannaDVol() {
    // TODO Auto-generated method stub
    return null;
  }

}
