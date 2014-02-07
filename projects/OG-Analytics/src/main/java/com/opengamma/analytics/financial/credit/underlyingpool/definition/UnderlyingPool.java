/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.underlyingpool.definition;

import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.obligor.definition.Obligor;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
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

  // NOTE : The market data associated with the obligors in the pool (credit spread term structures and yield curves) are not part of this object

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // The name of the pool e.g. "Pool_1"
  private final String _poolName;

  // A vector of obligors constituting the underlying pool
  private final LegalEntity[] _obligors;

  // The number of obligors in the underlying pool (usually 125 for CDX and iTraxx - although defaults can reduce this)
  private final int _numberOfObligors;

  // The number of obligors in the underlying pool marked as having not previously defaulted
  private final int _numberOfNonDefaultedObligors;

  // The number of obligors in the underlying pool marked as having previously defaulted
  private final int _numberOfDefaultedObligors;

  // The currencies of the underlying obligors
  private final Currency[] _currency;

  // The seniority of the debt of the reference entities in the underlying pool
  private final DebtSeniority[] _debtSeniority;

  // The restructuring type in the event of a credit event deemed to be a restructuring of the reference entities debt
  private final RestructuringClause[] _restructuringClause;

  // Vector holding the notional amounts of each obligor in the underlying pool
  private final double[] _obligorNotionals;

  // Vector holding the coupons to apply to the obligors in the underlying pool
  private final double[] _obligorCoupons;

  // Vector holding the recovery rates of the obligors in the underlying pool
  private final double[] _obligorRecoveryRates;

  // Vector holding the weights of the obligor in the underlying pool
  private final double[] _obligorWeights;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Ctor for the pool of obligor objects

  public UnderlyingPool(
      final String poolName,
      final LegalEntity[] obligors,
      final Currency[] currency,
      final DebtSeniority[] debtSeniority,
      final RestructuringClause[] restructuringClause,
      final double[] obligorNotionals,
      final double[] obligorCoupons,
      final double[] obligorRecoveryRates,
      final double[] obligorWeights) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check the validity of the input arguments

    ArgumentChecker.notNull(poolName, "Pool name");
    ArgumentChecker.notNull(obligors, "Obligors");
    ArgumentChecker.notNull(currency, "Currency");
    ArgumentChecker.notNull(debtSeniority, "Debt Seniority");
    ArgumentChecker.notNull(restructuringClause, "Restructuring Clause");
    ArgumentChecker.notNull(obligorNotionals, "Notionals");
    ArgumentChecker.notNull(obligorCoupons, "Coupons");
    ArgumentChecker.notNull(obligorRecoveryRates, "Recovery Rates");
    ArgumentChecker.notNull(obligorWeights, "Obligor Weights");

    ArgumentChecker.noNulls(obligors, "Obligors");
    ArgumentChecker.noNulls(currency, "Currency");
    ArgumentChecker.noNulls(debtSeniority, "Debt Seniority");
    ArgumentChecker.noNulls(restructuringClause, "Restructuring Clause");

    ArgumentChecker.isTrue(obligors.length == currency.length, "Number of obligors and number of obligor currencies should be equal");
    ArgumentChecker.isTrue(obligors.length == debtSeniority.length, "Number of obligors and number of obligor debt seniorities should be equal");
    ArgumentChecker.isTrue(obligors.length == restructuringClause.length, "Number of obligors and number of obligor restructuring clauses should be equal");
    ArgumentChecker.isTrue(obligors.length == obligorCoupons.length, "Number of obligors and number of obligor coupons should be equal");
    ArgumentChecker.isTrue(obligors.length == obligorRecoveryRates.length, "Number of obligors and number of obligor recovery rates should be equal");
    ArgumentChecker.isTrue(obligors.length == obligorWeights.length, "Number of obligors and number of obligor weights should be equal");

    double totalObligorWeightings = 0.0;

    for (int i = 0; i < obligorCoupons.length; i++) {
      ArgumentChecker.notNegative(obligorCoupons[i], "Coupons for obligor " + i);

      ArgumentChecker.notNegative(obligorRecoveryRates[i], "Recovery Rate for obligor " + i);
      ArgumentChecker.isTrue(obligorRecoveryRates[i] <= 1.0, "Recovery rate for obligor " + i + " should be less than or equal to 100%");

      ArgumentChecker.notNegative(obligorWeights[i], "Index weighting for obligor " + i);
      ArgumentChecker.isTrue(obligorWeights[i] <= 1.0, "Index weighting for obligor " + i + " should be less than or equal to 100%");

      totalObligorWeightings += obligorWeights[i];
    }

    // TODO : Need to get this check working ArgumentChecker.isTrue(Double.doubleToLongBits(totalObligorWeightings) == 1.0, "Index constituent weights must sum to unity");
    ArgumentChecker.isTrue(totalObligorWeightings == 1.0, "Index constituent weights must sum to unity");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    _poolName = poolName;

    _obligors = obligors;

    _numberOfObligors = obligors.length;

    int numberOfDefaultedObligors = 0;

    for (int i = 0; i < _numberOfObligors; i++) {
      if (obligors[i].isHasDefaulted() == true) {
        numberOfDefaultedObligors++;
      }
    }

    _numberOfDefaultedObligors = numberOfDefaultedObligors;
    _numberOfNonDefaultedObligors = _numberOfObligors - _numberOfDefaultedObligors;

    _currency = currency;
    _debtSeniority = debtSeniority;
    _restructuringClause = restructuringClause;

    _obligorNotionals = obligorNotionals;
    _obligorCoupons = obligorCoupons;
    _obligorRecoveryRates = obligorRecoveryRates;
    _obligorWeights = obligorWeights;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public String getPoolName() {
    return _poolName;
  }

  public LegalEntity[] getObligors() {
    return _obligors;
  }

  public int getNumberOfObligors() {
    return _numberOfObligors;
  }

  public int getNumberOfNonDefaultedObligors() {
    return _numberOfNonDefaultedObligors;
  }

  public int getNumberOfDefaultedObligors() {
    return _numberOfDefaultedObligors;
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

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public UnderlyingPool withRecoveryRates(final double[] recoveryRates) {
    return new UnderlyingPool(
        getPoolName(),
        getObligors(),
        getCurrency(),
        getDebtSeniority(),
        getRestructuringClause(),
        getObligorNotionals(),
        getCoupons(),
        recoveryRates,
        getObligorWeights());
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
