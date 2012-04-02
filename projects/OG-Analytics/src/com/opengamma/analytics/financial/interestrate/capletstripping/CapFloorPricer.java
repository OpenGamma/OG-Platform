/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.SimpleOptionData;
import com.opengamma.analytics.financial.model.volatility.VolatilityModel1D;

/**
 * 
 */
public class CapFloorPricer {
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();

  private final double[] _fwds;
  private final double[] _t;
  private final double[] _df;
  private final double _k;
  private final int _n;

  /**
   * Decomposes a cap (floor) down to relevant information about its caplets (floorlets), i.e. the forward (ibor) values, the fixing times and
   * the discount factors. Each caplet (floorlet), and hence the whole cap (floor) can then be priced by suppling a VolatilityModel1D 
   * (which gives a Black vol for a particular forward/strike/expiry) to the method price 
   * @param cap a cap or floor 
   * @param ycb The relevant yield curves 
   */
  public CapFloorPricer(final CapFloor cap, final YieldCurveBundle ycb) {
    Validate.notNull(cap, "null cap");
    Validate.notNull(ycb, "null yield curves");
    _k = cap.getStrike();
    CapFloorIbor[] caplets = cap.getPayments();
    _n = caplets.length;
    _fwds = new double[_n];
    _t = new double[_n];
    _df = new double[_n];
    YieldAndDiscountCurve discountCurve = ycb.getCurve(cap.getDiscountCurve());
    for (int i = 0; i < _n; i++) {
      _fwds[i] = PRC.visit(caplets[i], ycb);
      _t[i] = caplets[i].getFixingPeriodStartTime();
      _df[i] = discountCurve.getDiscountFactor(caplets[i].getPaymentTime()); //Vol is at fixing time, discounting from payment
    }
  }

  /**
   * Price a cap (floor) with a VolatilityModel1D. This allows the same cap to be prices with different models (different models include different 
   * parameters for the same model), with repeating calculations (e.g. as part of a caplet stripping routine) 
   * @param volModel VolatilityModel1D  which gives a Black vol for a particular forward/strike/expiry
   * @return The cap (floor) price 
   */
  public double price(VolatilityModel1D volModel) {
    double sum = 0;
    for (int i = 0; i < _n; i++) {
      double vol = volModel.getVolatility(_fwds[i], _k, _t[i]);
      sum += _df[i] * BlackFormulaRepository.price(_fwds[i], _k, _t[i], vol, true); //TODO assuming cap, payment year fraction?????
    }
    return sum;
  }

 
  public double vega(VolatilityModel1D volModel) {
    double sum = 0;
    double vol = impliedVol(volModel);
    for (int i = 0; i < _n; i++) {
      sum += _df[i] * BlackFormulaRepository.vega(_fwds[i], _k, _t[i], vol); //TODO assuming cap, payment year fraction?????
    }
    return sum;
  }

  public double impliedVol(VolatilityModel1D volModel) {
    final double price = price(volModel);
    SimpleOptionData[] data = new SimpleOptionData[_n];
    for (int i = 0; i < _n; i++) {
      data[i] = new SimpleOptionData(_fwds[i], _k, _t[i], _df[i], true); //TODO this should be in the constructor 
    }
    return BlackFormulaRepository.impliedVolatility(data, price);
  }

  /**
   * Gets the fwds.
   * @return the fwds
   */
  protected double[] getForwards() {
    return _fwds;
  }

  /**
   * Gets the t.
   * @return the t
   */
  protected double[] getExpiries() {
    return _t;
  }

  /**
   * Gets the df.
   * @return the df
   */
  protected double[] getDiscountFactors() {
    return _df;
  }

  /**
   * Gets the k.
   * @return the k
   */
  protected double getStrike() {
    return _k;
  }

  /**
   * Gets the n.
   * @return the n
   */
  protected int getNumberCaplets() {
    return _n;
  }
  
  

}
