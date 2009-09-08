package com.opengamma.financial.greeks;

public class Greek {

  /**
   * 
   * @author emcleod
   * 
   */
  public enum GreekType {
    CARRY_RHO, DDELTA_DTIME, DDELTA_DVAR, DELTA, DGAMMA_DSPOT, DGAMMA_P_DSPOT, DGAMMA_DTIME, DGAMMA_P_DTIME, DGAMMA_DVOL, DGAMMA_P_DVOL, DRIFTLESS_THETA, DVANNA_DVOL, DVEGA_DTIME, DVOMMA_DVOL, DZETA_DTIME, DZETA_DVOL, ELASTICITY, GAMMA, GAMMA_P, PHI, PRICE, RHO, STRIKE_DELTA, STRIKE_GAMMA, THETA, VANNA, VARIANCE_ULTIMA, VARIANCE_VEGA, VARIANCE_VOMMA, VEGA, VEGA_P, VOMMA, VOMMA_P, ZETA
  }

  private GreekType _type;
  private String _name;

  public Greek(GreekType type, String name) {
    _type = type;
    _name = name;
  }
}
