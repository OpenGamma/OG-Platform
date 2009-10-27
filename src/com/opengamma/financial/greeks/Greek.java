package com.opengamma.financial.greeks;

public enum Greek {

  /**
   * 
   * Zeroth order greeks
   * 
   */
  PRICE {
    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitPrice();
    }
  },
  ZETA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

  },

  /**
   * 
   * First order greeks
   * 
   */
  CARRY_RHO {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

  },
  DELTA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitDelta();
    }

  },
  DRIFTLESS_THETA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

  },
  DZETA_DVOL {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

  },
  ELASTICITY {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

  },
  PHI {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

  },
  RHO {
    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitRho();
    }
  },
  STRIKE_DELTA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
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
  },
  VARIANCE_VEGA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

  },
  VEGA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVega();
    }

  },
  VEGA_P {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

  },
  ZETA_BLEED {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

  },

  /**
   * 
   * Second order greeks
   * 
   */
  DDELTA_DVAR {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

  },
  DELTA_BLEED {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

  },
  GAMMA {
    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitGamma();
    }
  },
  GAMMA_P {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

  },
  STRIKE_GAMMA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

  },
  VANNA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }
  },
  VARIANCE_VOMMA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

  },
  VEGA_BLEED {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

  },
  VOMMA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

  },
  VOMMA_P {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

  },

  /**
   * 
   * Third order greeks
   * 
   */
  GAMMA_BLEED {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

  },
  GAMMA_P_BLEED {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

  },
  SPEED {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

  },
  SPEED_P {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

  },
  ULTIMA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

  },
  VARIANCE_ULTIMA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

  },
  ZOMMA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

  },
  ZOMMA_P {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      // TODO Auto-generated method stub
      return null;
    }

  };

  public abstract <T> T accept(GreekVisitor<T> visitor);
}
