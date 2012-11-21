/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.underlyingpool.definition;

import com.opengamma.analytics.financial.credit.CreditSpreadTenors;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.obligormodel.definition.Obligor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.statistics.descriptive.MeanCalculator;
import com.opengamma.analytics.math.statistics.descriptive.MedianCalculator;
import com.opengamma.analytics.math.statistics.descriptive.ModeCalculator;
import com.opengamma.analytics.math.statistics.descriptive.PercentileCalculator;
import com.opengamma.analytics.math.statistics.descriptive.SampleFisherKurtosisCalculator;
import com.opengamma.analytics.math.statistics.descriptive.SampleSkewnessCalculator;
import com.opengamma.analytics.math.statistics.descriptive.SampleStandardDeviationCalculator;
import com.opengamma.analytics.math.statistics.descriptive.SampleVarianceCalculator;
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
  // TODO : Add the arg checker for a null YieldCurve object (taken out for the purposes of testing)
  // TODO : Add an arg checker to ensure no two obligors are the same
  // TODO : Need to check the validity of the creditSpreadTenors and spreadTermStructures arguments
  // TODO : Work out a better way of looking up the correct element in the creditSpreadTenors vector

  // NOTE : We input the individual obligor notionals as part of the underlying pool (the total pool notional is then calculated from this).
  // NOTE : e.g. suppose we have 100 names in the pool all equally weighted. If each obligor notional is $1mm then the total pool notional is $100mm
  // NOTE : Alternatively we can specify the total pool notional to be $100mm and then calculate by hand what the appropriate obligor notionals should be (1/100)

  // ----------------------------------------------------------------------------------------------------------------------------------------

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
  private final YieldCurve[] _yieldCurve;

  // The total notional of all the obligors in the underlying pool
  private final double _poolNotional;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Ctor for the pool of obligor objects

  public UnderlyingPool(
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
      final YieldCurve[] yieldCurve) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check the validity of the input arguments

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
    //ArgumentChecker.notNull(yieldCurve, "Yield curve");

    ArgumentChecker.noNulls(obligors, "Obligors");
    ArgumentChecker.noNulls(currency, "Currency");
    ArgumentChecker.noNulls(debtSeniority, "Debt Seniority");
    ArgumentChecker.noNulls(restructuringClause, "Restructuring Clause");
    ArgumentChecker.noNulls(creditSpreadTenors, "Credit spread tenors");
    ArgumentChecker.noNulls(spreadTermStructures, "Credit spread term structures");
    //ArgumentChecker.noNulls(yieldCurve, "Yield curve");

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

    // Calculate the total notional amount of the obligors in the pool
    double totalNotional = 0.0;
    for (int i = 0; i < _numberOfObligors; i++) {
      totalNotional += _obligorNotionals[i];
    }

    _poolNotional = totalNotional;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

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

  public YieldCurve[] getYieldCurves() {
    return _yieldCurve;
  }

  public double getPoolNotional() {
    return _poolNotional;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public double getPoolRecoveryRateMean() {
    return calculateAverageRecoveryRate();
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public double getPoolSpreadMean(final CreditSpreadTenors creditSpreadTenor) {

    ArgumentChecker.notNull(creditSpreadTenor, "Credit spread tenor");

    return calculateCreditSpreadMean(creditSpreadTenor);
  }

  public double getPoolSpreadMedian(final CreditSpreadTenors creditSpreadTenor) {

    ArgumentChecker.notNull(creditSpreadTenor, "Credit spread tenor");

    return calculateCreditSpreadMedian(creditSpreadTenor);
  }

  public double getPoolSpreadMode(final CreditSpreadTenors creditSpreadTenor) {

    ArgumentChecker.notNull(creditSpreadTenor, "Credit spread tenor");

    return calculateCreditSpreadMode(creditSpreadTenor);
  }

  public double getPoolSpreadVariance(final CreditSpreadTenors creditSpreadTenor) {

    ArgumentChecker.notNull(creditSpreadTenor, "Credit spread tenor");

    return calculateCreditSpreadVariance(creditSpreadTenor);
  }

  public double getPoolSpreadStandardDeviation(final CreditSpreadTenors creditSpreadTenor) {

    ArgumentChecker.notNull(creditSpreadTenor, "Credit spread tenor");

    return calculateCreditSpreadStandardDeviation(creditSpreadTenor);
  }

  public double getPoolSpreadSkewness(final CreditSpreadTenors creditSpreadTenor) {

    ArgumentChecker.notNull(creditSpreadTenor, "Credit spread tenor");

    return calculateCreditSpreadSkewness(creditSpreadTenor);
  }

  public double getPoolSpreadKurtosis(final CreditSpreadTenors creditSpreadTenor) {

    ArgumentChecker.notNull(creditSpreadTenor, "Credit spread tenor");

    return calculateCreditSpreadKurtosis(creditSpreadTenor);
  }

  public double getPoolSpreadPercentile(final CreditSpreadTenors creditSpreadTenor, final double q) {

    ArgumentChecker.notNull(creditSpreadTenor, "Credit spread tenor");
    ArgumentChecker.notNegative(q, "Percentile");
    ArgumentChecker.isTrue(q <= 1.0, "Percentile must be less than or equal to 100%");

    return calculateCreditSpreadPercentile(creditSpreadTenor, q);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the average recovery rate of the obligors in the underlying pool
  private double calculateAverageRecoveryRate() {

    MeanCalculator mean = new MeanCalculator();

    return mean.evaluate(_obligorRecoveryRates);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the average (mean) spread of the obligors in the underlying pool for a given tenor
  private double calculateCreditSpreadMean(CreditSpreadTenors creditSpreadTenor) {

    MeanCalculator mean = new MeanCalculator();

    double[] spreads = getSpreads(creditSpreadTenor);

    return mean.evaluate(spreads);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the median spread of the obligors in the underlying pool for a given tenor
  private double calculateCreditSpreadMedian(CreditSpreadTenors creditSpreadTenor) {

    MedianCalculator median = new MedianCalculator();

    double[] spreads = getSpreads(creditSpreadTenor);

    return median.evaluate(spreads);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the modal spread of the obligors in the underlying pool for a given tenor
  private double calculateCreditSpreadMode(CreditSpreadTenors creditSpreadTenor) {

    ModeCalculator mode = new ModeCalculator();

    double[] spreads = getSpreads(creditSpreadTenor);

    return mode.evaluate(spreads);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the variance of the spread of the obligors in the underlying pool for a given tenor
  private double calculateCreditSpreadVariance(CreditSpreadTenors creditSpreadTenor) {

    SampleVarianceCalculator variance = new SampleVarianceCalculator();

    double[] spreads = getSpreads(creditSpreadTenor);

    return variance.evaluate(spreads);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the standard deviation of the spread of the obligors in the underlying pool for a given tenor
  private double calculateCreditSpreadStandardDeviation(CreditSpreadTenors creditSpreadTenor) {

    SampleStandardDeviationCalculator standardDeviation = new SampleStandardDeviationCalculator();

    double[] spreads = getSpreads(creditSpreadTenor);

    return standardDeviation.evaluate(spreads);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the skewness of the spread of the obligors in the underlying pool for a given tenor
  private double calculateCreditSpreadSkewness(CreditSpreadTenors creditSpreadTenor) {

    SampleSkewnessCalculator skewness = new SampleSkewnessCalculator();

    double[] spreads = getSpreads(creditSpreadTenor);

    return skewness.evaluate(spreads);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the excess kurtosis of the spread of the obligors in the underlying pool for a given tenor
  private double calculateCreditSpreadKurtosis(CreditSpreadTenors creditSpreadTenor) {

    SampleFisherKurtosisCalculator excessKurtosis = new SampleFisherKurtosisCalculator();

    double[] spreads = getSpreads(creditSpreadTenor);

    return excessKurtosis.evaluate(spreads);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the q'th percentile of the spread distribution of the obligors in the underlying pool for a given tenor
  private double calculateCreditSpreadPercentile(CreditSpreadTenors creditSpreadTenor, final double q) {

    PercentileCalculator percentile = new PercentileCalculator(q);

    double[] spreads = getSpreads(creditSpreadTenor);

    return percentile.evaluate(spreads);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to extract out the creditSpreadTenor spreads from the underlying pool
  private double[] getSpreads(CreditSpreadTenors creditSpreadTenor) {

    ArgumentChecker.notNull(creditSpreadTenor, "Credit spread tenor");

    int counter = 0;

    double[] spreads = new double[this._numberOfObligors];

    while (this.getCreditSpreadTenors()[counter] != creditSpreadTenor) {
      counter++;
    }

    for (int i = 0; i < this._numberOfObligors; i++) {
      spreads[i] = this.getSpreadTermStructures()[i][counter];
    }

    return spreads;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
