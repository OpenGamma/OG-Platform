/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

import com.opengamma.financial.greeks.Greek;

/**
 * @author emcleod
 *
 */
public enum PositionGreek {

  POSITION_ZETA {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.ZETA;
    }

  },
  POSITION_DELTA {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.DELTA;
    }

  },
  POSITION_DRIFTLESS_THETA {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.DRIFTLESS_THETA;
    }

  },
  POSITION_GAMMA {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.GAMMA;
    }

  },
  POSITION_DZETA_DVOL {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.DZETA_DVOL;
    }

  },
  POSITION_ELASTICITY {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.ELASTICITY;
    }

  },
  POSITION_PHI {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.PHI;
    }

  },
  POSITION_VEGA {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.VEGA;
    }

  },
  POSITION_THETA {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.THETA;
    }

  },
  POSITION_CARRY_RHO {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.CARRY_RHO;
    }

  },
  POSITION_RHO {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.RHO;
    }
  },
  POSITION_STRIKE_DELTA {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.STRIKE_DELTA;
    }

  },
  POSITION_VARIANCE_VEGA {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.VARIANCE_VEGA;
    }

  },
  POSITION_VEGA_P {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.VEGA_P;
    }

  },
  POSITION_ZETA_BLEED {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.ZETA_BLEED;
    }

  },
  POSITION_DELTA_BLEED {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.DELTA_BLEED;
    }

  },
  POSITION_GAMMA_P {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.GAMMA_P;
    }

  },
  POSITION_STRIKE_GAMMA {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.STRIKE_GAMMA;
    }

  },
  POSITION_VANNA {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.VANNA;
    }

  },
  POSITION_VARIANCE_VANNA {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.VARIANCE_VANNA;
    }

  },
  POSITION_VARIANCE_VOMMA {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.VARIANCE_VOMMA;
    }

  },
  POSITION_VEGA_BLEED {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.VEGA_BLEED;
    }

  },
  POSITION_VOMMA {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.VOMMA;
    }

  },
  POSITION_VOMMA_P {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.VOMMA_P;
    }

  },
  POSITION_DVANNA_DVOL {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.DVANNA_DVOL;
    }

  },
  POSITION_GAMMA_BLEED {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.GAMMA_BLEED;
    }

  },
  POSITION_GAMMA_P_BLEED {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.GAMMA_P_BLEED;
    }

  },
  POSITION_SPEED {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.SPEED;
    }

  },
  POSITION_SPEED_P {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.SPEED_P;
    }

  },
  POSITION_ULTIMA {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.ULTIMA;
    }

  },
  POSITION_VARIANCE_ULTIMA {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.VARIANCE_ULTIMA;
    }

  },
  POSITION_ZOMMA {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.ZOMMA;
    }

  },
  POSITION_ZOMMA_P {

    @Override
    public Greek getUnderlyingGreek() {
      return Greek.ZOMMA_P;
    }

  }

  ;

  public abstract Greek getUnderlyingGreek();
}
