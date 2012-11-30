/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.underlyingpool.definition;

import com.opengamma.analytics.financial.credit.CreditSpreadTenors;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.credit.obligormodel.definition.Obligor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class to specify the composition and characteristics of a collection of Obligor objects aggregated into a common pool
 * In the credit index context the underlying pool is the set of obligors that constitute an index (e.g. CDX.NA.IG series 18)
 */
public class UnderlyingPool {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Work-in-Progress

  // TODO : Add the hashcode and equals methods
  // TODO : Add an arg checker to ensure no two obligors are the same

  // NOTE : We input the individual obligor notionals as part of the underlying pool (the total pool notional is then calculated from this).
  // NOTE : e.g. suppose we have 100 names in the pool all equally weighted. If each obligor notional is $1mm then the total pool notional is $100mm
  // NOTE : Alternatively we can specify the total pool notional to be $100mm and then calculate by hand what the appropriate obligor notionals should be (1/100)

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private final String _poolName;

  // A vector of obligors constituting the underlying pool
  private final Obligor[] _obligors;

  // The number of obligors in the underlying pool (usually 125 for CDX and iTraxx - although defaults can reduce this)
  private final int _numberOfObligors;

  // The currencies of the underlying obligors
  private final Currency[] _currency;

  // The seniority of the debt of the reference entities in the underlying pool
  private final DebtSeniority[] _debtSeniority;

  // The restructuring type in the event of a credit event deemed to be a restructuring of the reference entities debt
  private final RestructuringClause[] _restructuringClause;

  // Vector of tenors at which we have market observed par CDS spreads
  private final CreditSpreadTenors[] _creditSpreadTenors;

  // The number of tenor points used in specifying the term structure of credit spreads
  private final int _numberOfCreditSpreadTenors;

  // Matrix holding the term structure of market observed credit spreads (one term structure for each obligor in the underlying pool)
  private final double[][] _spreadTermStructures;

  // Vector holding the notional amounts of each obligor in the underlying pool
  private final double[] _obligorNotionals;

  // Vector holding the coupons to apply to the obligors in the underlying pool
  private final double[] _obligorCoupons;

  // Vector holding the recovery rates of the obligors in the underlying pool
  private final double[] _obligorRecoveryRates;

  // Vector holding the weights of the obligor in the underlying pool
  private final double[] _obligorWeights;

  // The yield curve objects (in principle each obligor can be in a different currency and therefore have a different discounting curve)
  private final ISDACurve[] _yieldCurve;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Ctor for the pool of obligor objects

  public UnderlyingPool(
      final String poolName,
      final Obligor[] obligors,
      final Currency[] currency,
      final DebtSeniority[] debtSeniority,
      final RestructuringClause[] restructuringClause,
      final CreditSpreadTenors[] creditSpreadTenors,
      final double[][] spreadTermStructures,
      final double[] obligorNotionals,
      final double[] obligorCoupons,
      final double[] obligorRecoveryRates,
      final double[] obligorWeights,
      final ISDACurve[] yieldCurve) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check the validity of the input arguments

    ArgumentChecker.notNull(poolName, "Pool name");
    ArgumentChecker.notNull(obligors, "Obligors");
    ArgumentChecker.notNull(currency, "Currency");
    ArgumentChecker.notNull(debtSeniority, "Debt Seniority");
    ArgumentChecker.notNull(restructuringClause, "Restructuring Clause");
    ArgumentChecker.notNull(creditSpreadTenors, "Credit spread tenors");
    ArgumentChecker.notNull(spreadTermStructures, "Credit spread term structures");
    ArgumentChecker.notNull(obligorNotionals, "Notionals");
    ArgumentChecker.notNull(obligorCoupons, "Coupons");
    ArgumentChecker.notNull(obligorRecoveryRates, "Recovery Rates");
    ArgumentChecker.notNull(obligorWeights, "Obligor Weights");
    ArgumentChecker.notNull(yieldCurve, "Yield curve");

    ArgumentChecker.noNulls(obligors, "Obligors");
    ArgumentChecker.noNulls(currency, "Currency");
    ArgumentChecker.noNulls(debtSeniority, "Debt Seniority");
    ArgumentChecker.noNulls(restructuringClause, "Restructuring Clause");
    ArgumentChecker.noNulls(creditSpreadTenors, "Credit spread tenors");
    ArgumentChecker.noNulls(spreadTermStructures, "Credit spread term structures");
    ArgumentChecker.noNulls(yieldCurve, "Yield curve");

    ArgumentChecker.isTrue(obligors.length == currency.length, "Number of obligors and number of obligor currencies should be equal");
    ArgumentChecker.isTrue(obligors.length == debtSeniority.length, "Number of obligors and number of obligor debt seniorities should be equal");
    ArgumentChecker.isTrue(obligors.length == restructuringClause.length, "Number of obligors and number of obligor restructuring clauses should be equal");
    ArgumentChecker.isTrue(obligors.length == obligorCoupons.length, "Number of obligors and number of obligor coupons should be equal");
    ArgumentChecker.isTrue(obligors.length == obligorRecoveryRates.length, "Number of obligors and number of obligor recovery rates should be equal");
    ArgumentChecker.isTrue(obligors.length == obligorWeights.length, "Number of obligors and number of obligor weights should be equal");
    ArgumentChecker.isTrue(obligors.length == yieldCurve.length, "Number of obligors and number of obligor yield curves should be equal");

    double totalObligorWeightings = 0.0;

    for (int i = 0; i < obligorCoupons.length; i++) {
      ArgumentChecker.notNegative(obligorCoupons[i], "Coupons for obligor " + i);

      ArgumentChecker.notNegative(obligorRecoveryRates[i], "Recovery Rate for obligor " + i);
      ArgumentChecker.isTrue(obligorRecoveryRates[i] <= 1.0, "Recovery rate for obligor " + i + " should be less than or equal to 100%");

      ArgumentChecker.notNegative(obligorWeights[i], "Index weighting for obligor " + i);
      ArgumentChecker.isTrue(obligorWeights[i] <= 1.0, "Index weighting for obligor " + i + " should be less than or equal to 100%");

      totalObligorWeightings += obligorWeights[i];
    }

    ArgumentChecker.isTrue(totalObligorWeightings == 1.0, "Index constituent weights must sum to unity");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    _poolName = poolName;

    _obligors = obligors;

    _numberOfObligors = obligors.length;

    _currency = currency;
    _debtSeniority = debtSeniority;
    _restructuringClause = restructuringClause;

    _creditSpreadTenors = creditSpreadTenors;
    _spreadTermStructures = spreadTermStructures;
    _numberOfCreditSpreadTenors = creditSpreadTenors.length;

    _obligorNotionals = obligorNotionals;
    _obligorCoupons = obligorCoupons;
    _obligorRecoveryRates = obligorRecoveryRates;
    _obligorWeights = obligorWeights;

    _yieldCurve = yieldCurve;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public String getPoolName() {
    return _poolName;
  }

  public Obligor[] getObligors() {
    return _obligors;
  }

  public int getNumberOfObligors() {
    return _numberOfObligors;
  }

  public Currency[] getCurrency() {
    return _currency;
  }

  public DebtSeniority[] getDebtSeniority() {
    return _debtSeniority;
  }

  public RestructuringClause[] getRestructuringClause() {
    return _restructuringClause;
  }

  public CreditSpreadTenors[] getCreditSpreadTenors() {
    return _creditSpreadTenors;
  }

  public double[][] getSpreadTermStructures() {
    return _spreadTermStructures;
  }

  public int getNumberOfCreditSpreadTenors() {
    return _numberOfCreditSpreadTenors;
  }

  public double[] getObligorNotionals() {
    return _obligorNotionals;
  }

  public double[] getCoupons() {
    return _obligorCoupons;
  }

  public double[] getRecoveryRates() {
    return _obligorRecoveryRates;
  }

  public double[] getObligorWeights() {
    return _obligorWeights;
  }

  public ISDACurve[] getYieldCurves() {
    return _yieldCurve;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
