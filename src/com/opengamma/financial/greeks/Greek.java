package com.opengamma.financial.greeks;

public enum Greek {
  PRICE {
    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitPrice();
    }
  },
  DELTA {
    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitDelta();
    }
  },
  GAMMA {
    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitGamma();
    }
  },
  RHO {
    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitRho();
    }
  },
  THETA {
    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitTheta();
    }
  },
  TIME_BUCKETED_RHO {
    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitTimeBucketedRho();
    }
  };

  public abstract <T> T accept(GreekVisitor<T> visitor);
}
