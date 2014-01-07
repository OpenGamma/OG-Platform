/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.CashFlowEquivalentCalculator;
import com.opengamma.analytics.financial.interestrate.CashFlowEquivalentCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method to computes the present value and sensitivities of physical delivery European swaptions with the Libor Market Model with displaced diffusion.
 * Reference: Henrard, M. (2010). Swaptions in Libor Market Model with local volatility. Wilmott Journal, 2010, 2, 135-154
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionPhysicalFixedIborLMMDDMethod}
 */
@Deprecated
public final class SwaptionPhysicalFixedIborLMMDDMethod implements PricingMethod {

  /**
   * The method unique instance.
   */
  private static final SwaptionPhysicalFixedIborLMMDDMethod INSTANCE = new SwaptionPhysicalFixedIborLMMDDMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static SwaptionPhysicalFixedIborLMMDDMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private SwaptionPhysicalFixedIborLMMDDMethod() {
  }

  /**
   * The cash flow equivalent calculator used in computations.
   */
  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();
  /**
   * The cash flow equivalent curve sensitivity calculator used in computations.
   */
  private static final CashFlowEquivalentCurveSensitivityCalculator CFECSC = CashFlowEquivalentCurveSensitivityCalculator.getInstance();

  /**
   * The time tolerance between the dates given by the model and the dates of the instrument. To avoid rounding problems.
   */
  private static final double TIME_TOLERANCE = 1.0E-3;

  /**
   * Computes the present value of the Physical delivery swaption.
   * @param swaption The swaption.
   * @param lmmBundle The LMM parameters and the curves.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final SwaptionPhysicalFixedIbor swaption, final LiborMarketModelDisplacedDiffusionDataBundle lmmBundle) {
    ArgumentChecker.notNull(swaption, "swaption");
    ArgumentChecker.notNull(lmmBundle, "LMM bundle");
    // 1. Swaption CFE preparation
    final AnnuityPaymentFixed cfe = swaption.getUnderlyingSwap().accept(CFEC, lmmBundle);
    final YieldAndDiscountCurve dsc = lmmBundle.getCurve(cfe.getDiscountCurve());
    final int nbCFInit = cfe.getNumberOfPayments();
    final double multFact = Math.signum(cfe.getNthPayment(0).getAmount());
    final boolean isCall = (cfe.getNthPayment(0).getAmount() < 0);
    final double[] cftInit = new double[nbCFInit];
    final double[] cfaInit = new double[nbCFInit];
    for (int loopcf = 0; loopcf < nbCFInit; loopcf++) {
      cftInit[loopcf] = cfe.getNthPayment(loopcf).getPaymentTime();
      cfaInit[loopcf] = cfe.getNthPayment(loopcf).getAmount() * -multFact;
    }
    final double timeToExpiry = swaption.getTimeToExpiry();
    // 2. Model data
    final int nbFactor = lmmBundle.getLmmParameter().getNbFactor();
    final double[][] volLMM = lmmBundle.getLmmParameter().getVolatility();
    final double[] timeLMM = lmmBundle.getLmmParameter().getIborTime();
    // 3. Link cfe dates to lmm
    final int[] indCFDate = new int[nbCFInit];
    int indStart = nbCFInit - 1;
    int indEnd = 0;
    for (int loopcf = 0; loopcf < nbCFInit; loopcf++) {
      indCFDate[loopcf] = Arrays.binarySearch(timeLMM, cftInit[loopcf]);
      if (indCFDate[loopcf] < 0) {
        if (timeLMM[-indCFDate[loopcf] - 1] - cftInit[loopcf] < TIME_TOLERANCE) {
          indCFDate[loopcf] = -indCFDate[loopcf] - 1;
        } else {
          if (cftInit[loopcf] - timeLMM[-indCFDate[loopcf] - 2] < TIME_TOLERANCE) {
            indCFDate[loopcf] = -indCFDate[loopcf] - 2;
          } else {
            ArgumentChecker.isTrue(true, "Instrument time incompatible with LMM parametrisation"); //TODO really?
          }
        }
      }
      if (indCFDate[loopcf] < indStart) {
        indStart = indCFDate[loopcf];
      }
      if (indCFDate[loopcf] > indEnd) {
        indEnd = indCFDate[loopcf];
      }
    }
    final int nbCF = indEnd - indStart + 1;
    final double[] cfa = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCFInit; loopcf++) {
      cfa[indCFDate[loopcf] - indStart] = cfaInit[loopcf];
    }
    final double[] cft = new double[nbCF];
    System.arraycopy(timeLMM, indStart, cft, 0, nbCF);

    final double[] dfLMM = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      dfLMM[loopcf] = dsc.getDiscountFactor(cft[loopcf]);
    }
    final double[][] gammaLMM = new double[nbCF - 1][nbFactor];
    final double[] deltaLMM = new double[nbCF - 1];
    System.arraycopy(lmmBundle.getLmmParameter().getAccrualFactor(), indStart, deltaLMM, 0, nbCF - 1);
    final double[] aLMM = new double[nbCF - 1];
    System.arraycopy(lmmBundle.getLmmParameter().getDisplacement(), indStart, aLMM, 0, nbCF - 1);
    final double[] liborLMM = new double[nbCF - 1];
    final double amr = lmmBundle.getLmmParameter().getMeanReversion();
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      gammaLMM[loopcf] = volLMM[indStart + loopcf];
      liborLMM[loopcf] = (dfLMM[loopcf] / dfLMM[loopcf + 1] - 1.0d) / deltaLMM[loopcf];
    }
    // TODO: 4. cfe modification (for roller coasters)
    final double[] cfaMod = new double[nbCF + 1];
    final double cfaMod0 = cfa[0];
    cfaMod[0] = cfaMod0; // modified strike
    cfaMod[1] = 0.0;
    System.arraycopy(cfa, 1, cfaMod, 2, nbCF - 1);
    // 5. Pricing algorithm
    final double[] p0 = new double[nbCF];
    final double[] dP = new double[nbCF];
    double b0 = 0;
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      p0[loopcf] = dfLMM[loopcf] / dfLMM[0];
      dP[loopcf] = cfaMod[loopcf + 1] * p0[loopcf];
      b0 += dP[loopcf];
    }
    final double bK = -cfaMod0;
    final double bM = (b0 + bK) / 2.0d;
    final double meanReversionImpact = Math.abs(amr) < 1.0E-6 ? timeToExpiry : (Math.exp(2.0d * amr * timeToExpiry) - 1.0d) / (2.0d * amr); // To handle 0 mean reversion.
    final double[] rate0Ratio = new double[nbCF - 1];
    final double[][] mu0 = new double[nbCF - 1][nbFactor];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      rate0Ratio[loopcf] = (liborLMM[loopcf] + aLMM[loopcf]) / (liborLMM[loopcf] + 1 / deltaLMM[loopcf]);
    }
    for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
      mu0[0][loopfact] = rate0Ratio[0] * gammaLMM[0][loopfact];
    }
    for (int loopcf = 1; loopcf < nbCF - 1; loopcf++) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        mu0[loopcf][loopfact] = mu0[loopcf - 1][loopfact] + rate0Ratio[loopcf] * gammaLMM[loopcf][loopfact];
      }
    }
    final double[] tau = new double[nbCF];
    final double[] tau2 = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        tau2[loopcf + 1] += mu0[loopcf][loopfact] * mu0[loopcf][loopfact];
      }
      tau2[loopcf + 1] = tau2[loopcf + 1] * meanReversionImpact;
      tau[loopcf + 1] = Math.sqrt(tau2[loopcf + 1]);
    }
    double sumNum = -bM;
    double sumDen = 0;
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      sumNum += dP[loopcf] - dP[loopcf] * tau2[loopcf] / 2.0;
      sumDen += dP[loopcf] * tau[loopcf];
    }
    final double xBar = sumNum / sumDen;
    final double[] pM = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      pM[loopcf] = p0[loopcf] * (1 - xBar * tau[loopcf] - tau2[loopcf] / 2.0);
    }
    final double[] liborM = new double[nbCF - 1];
    final double[] alphaM = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      liborM[loopcf] = (pM[loopcf] / pM[loopcf + 1] - 1.0d) / deltaLMM[loopcf];
    }
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      alphaM[loopcf] = cfaMod[loopcf + 1] * pM[loopcf] / bM;
    }
    final double[] rateMRatio = new double[nbCF - 1];
    final double[][] muM = new double[nbCF - 1][nbFactor];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      rateMRatio[loopcf] = (liborM[loopcf] + aLMM[loopcf]) / (liborM[loopcf] + 1 / deltaLMM[loopcf]);
    }
    for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
      muM[0][loopfact] = rateMRatio[0] * gammaLMM[0][loopfact];
    }
    for (int loopcf = 1; loopcf < nbCF - 1; loopcf++) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        muM[loopcf][loopfact] = muM[loopcf - 1][loopfact] + rateMRatio[loopcf] * gammaLMM[loopcf][loopfact];
      }
    }
    double normSigmaM = 0;
    final double[] sigmaM = new double[nbFactor];
    for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
      for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
        sigmaM[loopfact] += alphaM[loopcf + 1] * muM[loopcf][loopfact];
      }
      normSigmaM += sigmaM[loopfact] * sigmaM[loopfact];
    }
    final double impliedBlackVol = Math.sqrt(normSigmaM * meanReversionImpact);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(bK, 1, isCall);
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final BlackFunctionData dataBlack = new BlackFunctionData(b0, 1.0, impliedBlackVol);
    final Function1D<BlackFunctionData, Double> func = blackFunction.getPriceFunction(option);
    final double pv = dfLMM[0] * func.evaluate(dataBlack);
    return CurrencyAmount.of(swaption.getUnderlyingSwap().getFirstLeg().getCurrency(), pv * (swaption.isLong() ? 1.0 : -1.0));
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(instrument instanceof SwaptionPhysicalFixedIbor, "Physical delivery swaption");
    ArgumentChecker.isTrue(curves instanceof LiborMarketModelDisplacedDiffusionDataBundle, "Bundle should contain LMM data");
    return presentValue((SwaptionPhysicalFixedIbor) instrument, (LiborMarketModelDisplacedDiffusionDataBundle) curves);
  }

  /**
   * Computes the present value sensitivity to LMM volatility parameters.
   * @param swaption The (physical delivery) swaption.
   * @param lmmBundle The LMM parameters and the curves.
   * @return The sensitivity.
   */
  public double[][] presentValueLMMSensitivity(final SwaptionPhysicalFixedIbor swaption, final LiborMarketModelDisplacedDiffusionDataBundle lmmBundle) {
    // 1. Swaption CFE preparation
    final AnnuityPaymentFixed cfe = swaption.getUnderlyingSwap().accept(CFEC, lmmBundle);
    final YieldAndDiscountCurve dsc = lmmBundle.getCurve(cfe.getDiscountCurve());
    final int nbCFInit = cfe.getNumberOfPayments();
    final double multFact = Math.signum(cfe.getNthPayment(0).getAmount());
    final boolean isCall = (cfe.getNthPayment(0).getAmount() < 0);
    final double[] cftInit = new double[nbCFInit];
    final double[] cfaInit = new double[nbCFInit];
    for (int loopcf = 0; loopcf < nbCFInit; loopcf++) {
      cftInit[loopcf] = cfe.getNthPayment(loopcf).getPaymentTime();
      cfaInit[loopcf] = cfe.getNthPayment(loopcf).getAmount() * -multFact;
    }
    final double timeToExpiry = swaption.getTimeToExpiry();
    // 2. Model data
    final int nbFactor = lmmBundle.getLmmParameter().getNbFactor();
    final double[][] volLMM = lmmBundle.getLmmParameter().getVolatility();
    final double[] timeLMM = lmmBundle.getLmmParameter().getIborTime();
    // 3. Link cfe dates to lmm
    final int[] indCFDate = new int[nbCFInit];
    int indStart = nbCFInit - 1;
    int indEnd = 0;
    for (int loopcf = 0; loopcf < nbCFInit; loopcf++) {
      indCFDate[loopcf] = Arrays.binarySearch(timeLMM, cftInit[loopcf]);
      if (indCFDate[loopcf] < 0) {
        if (timeLMM[-indCFDate[loopcf] - 1] - cftInit[loopcf] < TIME_TOLERANCE) {
          indCFDate[loopcf] = -indCFDate[loopcf] - 1;
        } else {
          if (cftInit[loopcf] - timeLMM[-indCFDate[loopcf] - 2] < TIME_TOLERANCE) {
            indCFDate[loopcf] = -indCFDate[loopcf] - 2;
          } else {
            ArgumentChecker.isTrue(true, "Instrument time incompatible with LMM");
          }
        }
      }
      if (indCFDate[loopcf] < indStart) {
        indStart = indCFDate[loopcf];
      }
      if (indCFDate[loopcf] > indEnd) {
        indEnd = indCFDate[loopcf];
      }
    }
    final int nbCF = indEnd - indStart + 1;
    final double[] cfa = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCFInit; loopcf++) {
      cfa[indCFDate[loopcf] - indStart] = cfaInit[loopcf];
    }
    final double[] cft = new double[nbCF];
    System.arraycopy(timeLMM, indStart, cft, 0, nbCF);

    final double[] dfLMM = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      dfLMM[loopcf] = dsc.getDiscountFactor(cft[loopcf]);
    }
    final double[][] gammaLMM = new double[nbCF - 1][nbFactor];
    final double[] deltaLMM = new double[nbCF - 1];
    System.arraycopy(lmmBundle.getLmmParameter().getAccrualFactor(), indStart, deltaLMM, 0, nbCF - 1);
    final double[] aLMM = new double[nbCF - 1];
    System.arraycopy(lmmBundle.getLmmParameter().getDisplacement(), indStart, aLMM, 0, nbCF - 1);
    final double[] liborLMM = new double[nbCF - 1];
    final double amr = lmmBundle.getLmmParameter().getMeanReversion();
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      gammaLMM[loopcf] = volLMM[indStart + loopcf];
      liborLMM[loopcf] = (dfLMM[loopcf] / dfLMM[loopcf + 1] - 1.0d) / deltaLMM[loopcf];
    }
    // TODO: 4. cfe modification (for roller coasters)
    final double[] cfaMod = new double[nbCF + 1];
    final double cfaMod0 = cfa[0];
    cfaMod[0] = cfaMod0; // modified strike
    cfaMod[1] = 0.0;
    System.arraycopy(cfa, 1, cfaMod, 2, nbCF - 1);
    // 5. Pricing algorithm
    final double[] p0 = new double[nbCF];
    final double[] dP = new double[nbCF];
    double b0 = 0;
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      p0[loopcf] = dfLMM[loopcf] / dfLMM[0];
      dP[loopcf] = cfaMod[loopcf + 1] * p0[loopcf];
      b0 += dP[loopcf];
    }
    final double bK = -cfaMod0;
    final double bM = (b0 + bK) / 2.0d;
    final double[] rate0Ratio = new double[nbCF - 1];
    final double[][] mu0 = new double[nbCF - 1][nbFactor];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      rate0Ratio[loopcf] = (liborLMM[loopcf] + aLMM[loopcf]) / (liborLMM[loopcf] + 1 / deltaLMM[loopcf]);
    }
    for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
      mu0[0][loopfact] = rate0Ratio[0] * gammaLMM[0][loopfact];
    }
    for (int loopcf = 1; loopcf < nbCF - 1; loopcf++) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        mu0[loopcf][loopfact] = mu0[loopcf - 1][loopfact] + rate0Ratio[loopcf] * gammaLMM[loopcf][loopfact];
      }
    }
    final double meanReversionImpact = (Math.exp(2.0d * amr * timeToExpiry) - 1.0d) / (2.0d * amr);
    final double[] tau = new double[nbCF];
    final double[] tau2 = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        tau2[loopcf + 1] += mu0[loopcf][loopfact] * mu0[loopcf][loopfact];
      }
      tau2[loopcf + 1] = tau2[loopcf + 1] * meanReversionImpact;
      tau[loopcf + 1] = Math.sqrt(tau2[loopcf + 1]);
    }
    double sumNum = -bM;
    double sumDen = 0;
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      sumNum += dP[loopcf] - dP[loopcf] * tau2[loopcf] / 2.0;
      sumDen += dP[loopcf] * tau[loopcf];
    }
    final double xBar = sumNum / sumDen;
    final double[] pM = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      pM[loopcf] = p0[loopcf] * (1 - xBar * tau[loopcf] - tau2[loopcf] / 2.0);
    }
    final double[] liborM = new double[nbCF - 1];
    final double[] alphaM = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      liborM[loopcf] = (pM[loopcf] / pM[loopcf + 1] - 1.0d) / deltaLMM[loopcf];
    }
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      alphaM[loopcf] = cfaMod[loopcf + 1] * pM[loopcf] / bM;
    }
    final double[] rateMRatio = new double[nbCF - 1];
    final double[][] muM = new double[nbCF - 1][nbFactor];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      rateMRatio[loopcf] = (liborM[loopcf] + aLMM[loopcf]) / (liborM[loopcf] + 1 / deltaLMM[loopcf]);
    }
    for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
      muM[0][loopfact] = rateMRatio[0] * gammaLMM[0][loopfact];
    }
    for (int loopcf = 1; loopcf < nbCF - 1; loopcf++) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        muM[loopcf][loopfact] = muM[loopcf - 1][loopfact] + rateMRatio[loopcf] * gammaLMM[loopcf][loopfact];
      }
    }
    double normSigmaM = 0;
    final double[] sigmaM = new double[nbFactor];
    for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
      for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
        sigmaM[loopfact] += alphaM[loopcf + 1] * muM[loopcf][loopfact];
      }
      normSigmaM += sigmaM[loopfact] * sigmaM[loopfact];
    }
    final double impliedBlackVol = Math.sqrt(normSigmaM * meanReversionImpact);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(bK, 1, isCall);
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final BlackFunctionData dataBlack = new BlackFunctionData(b0, 1.0, impliedBlackVol);
    final double[] blkAdjoint = blackFunction.getPriceAdjoint(option, dataBlack);
    // Backward sweep
    final double pvBar = 1.0;
    final double impliedBlackVolBar = dfLMM[0] * blkAdjoint[2] * pvBar;
    final double normSigmaMBar = meanReversionImpact / (2.0 * impliedBlackVol) * impliedBlackVolBar;
    final double[] sigmaMBar = new double[nbFactor];
    for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
      sigmaMBar[loopfact] = 2 * sigmaM[loopfact] * normSigmaMBar;
    }

    final double[][] muMBar = new double[nbCF - 1][nbFactor];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        muMBar[loopcf][loopfact] = alphaM[loopcf + 1] * sigmaMBar[loopfact];
      }
    }
    for (int loopcf = nbCF - 3; loopcf >= 0; loopcf--) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        muMBar[loopcf][loopfact] += muMBar[loopcf + 1][loopfact];
      }
    }

    final double[] rateMRatioBar = new double[nbCF - 1];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        rateMRatioBar[loopcf] += gammaLMM[loopcf][loopfact] * muMBar[loopcf][loopfact];
      }
    }

    final double[] alphaMBar = new double[nbCF];
    for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
      for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
        alphaMBar[loopcf + 1] += muM[loopcf][loopfact] * sigmaMBar[loopfact];
      }
    }

    final double[] liborMBar = new double[nbCF - 1];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      liborMBar[loopcf] = ((liborM[loopcf] + 1 / deltaLMM[loopcf]) - (liborM[loopcf] + aLMM[loopcf])) / ((liborM[loopcf] + 1 / deltaLMM[loopcf]) * (liborM[loopcf] + 1 / deltaLMM[loopcf]))
          * rateMRatioBar[loopcf];
    }

    final double[] pMBar = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      pMBar[loopcf] += 1.0 / pM[loopcf + 1] / deltaLMM[loopcf] * liborMBar[loopcf];
      pMBar[loopcf + 1] += -pM[loopcf] / (pM[loopcf + 1] * pM[loopcf + 1]) / deltaLMM[loopcf] * liborMBar[loopcf];
    }
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      pMBar[loopcf] += cfaMod[loopcf + 1] / bM * alphaMBar[loopcf];
    }

    double xBarBar = 0.0;
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      xBarBar += -p0[loopcf] * tau[loopcf] * pMBar[loopcf];
    }
    final double sumNumBar = 1.0 / sumDen * xBarBar;
    final double sumDenBar = -sumNum / (sumDen * sumDen) * xBarBar;
    final double[] tauBar = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      tauBar[loopcf] = -p0[loopcf] * xBar * pMBar[loopcf];
    }
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      tauBar[loopcf] += dP[loopcf] * sumDenBar;
    }
    final double[] tau2Bar = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      tau2Bar[loopcf] = -p0[loopcf] / 2.0 * pMBar[loopcf];
    }
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      tau2Bar[loopcf] += -dP[loopcf] / 2.0 * sumNumBar;
    }
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      tau2Bar[loopcf + 1] += 1 / 2.0 / tau[loopcf + 1] * tauBar[loopcf + 1];
    }
    final double[][] mu0Bar = new double[nbCF - 1][nbFactor];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        mu0Bar[loopcf][loopfact] = 2.0 * mu0[loopcf][loopfact] * meanReversionImpact * tau2Bar[loopcf + 1];
      }
    }
    for (int loopcf = nbCF - 3; loopcf >= 0; loopcf--) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        mu0Bar[loopcf][loopfact] += mu0Bar[loopcf + 1][loopfact];
      }
    }
    final double[][] gammaLMMBar = new double[nbCF - 1][nbFactor];
    for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
      gammaLMMBar[0][loopfact] = rateMRatio[0] * muMBar[0][loopfact];
    }
    for (int loopcf = 1; loopcf < nbCF - 1; loopcf++) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        gammaLMMBar[loopcf][loopfact] += rateMRatio[loopcf] * muMBar[loopcf][loopfact];
      }
    }
    for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
      gammaLMMBar[0][loopfact] += rate0Ratio[0] * mu0Bar[0][loopfact];
    }
    for (int loopcf = 1; loopcf < nbCF - 1; loopcf++) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        gammaLMMBar[loopcf][loopfact] += rate0Ratio[loopcf] * mu0Bar[loopcf][loopfact];
      }
    }
    final double[][] volLMMBar = new double[volLMM.length][nbFactor];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      volLMMBar[indStart + loopcf] = gammaLMMBar[loopcf];
    }

    return volLMMBar;
  }

  /**
   * Computes the present value sensitivity to the displaced diffusion (shift) parameters.
   * @param swaption The (physical delivery) swaption.
   * @param lmmBundle The LMM parameters and the curves.
   * @return The sensitivity.
   */
  public double[] presentValueDDSensitivity(final SwaptionPhysicalFixedIbor swaption, final LiborMarketModelDisplacedDiffusionDataBundle lmmBundle) {
    // 1. Swaption CFE preparation
    final AnnuityPaymentFixed cfe = swaption.getUnderlyingSwap().accept(CFEC, lmmBundle);
    final YieldAndDiscountCurve dsc = lmmBundle.getCurve(cfe.getDiscountCurve());
    final int nbCFInit = cfe.getNumberOfPayments();
    final double multFact = Math.signum(cfe.getNthPayment(0).getAmount());
    final boolean isCall = (cfe.getNthPayment(0).getAmount() < 0);
    final double[] cftInit = new double[nbCFInit];
    final double[] cfaInit = new double[nbCFInit];
    for (int loopcf = 0; loopcf < nbCFInit; loopcf++) {
      cftInit[loopcf] = cfe.getNthPayment(loopcf).getPaymentTime();
      cfaInit[loopcf] = cfe.getNthPayment(loopcf).getAmount() * -multFact;
    }
    final double timeToExpiry = swaption.getTimeToExpiry();
    // 2. Model data
    final int nbFactor = lmmBundle.getLmmParameter().getNbFactor();
    final double[][] volLMM = lmmBundle.getLmmParameter().getVolatility();
    final double[] timeLMM = lmmBundle.getLmmParameter().getIborTime();
    // 3. Link cfe dates to lmm
    final int[] indCFDate = new int[nbCFInit];
    int indStart = nbCFInit - 1;
    int indEnd = 0;
    for (int loopcf = 0; loopcf < nbCFInit; loopcf++) {
      indCFDate[loopcf] = Arrays.binarySearch(timeLMM, cftInit[loopcf]);
      if (indCFDate[loopcf] < 0) {
        if (timeLMM[-indCFDate[loopcf] - 1] - cftInit[loopcf] < TIME_TOLERANCE) {
          indCFDate[loopcf] = -indCFDate[loopcf] - 1;
        } else {
          if (cftInit[loopcf] - timeLMM[-indCFDate[loopcf] - 2] < TIME_TOLERANCE) {
            indCFDate[loopcf] = -indCFDate[loopcf] - 2;
          } else {
            ArgumentChecker.isTrue(true, "Instrument time incompatible with LMM");
          }
        }
      }
      if (indCFDate[loopcf] < indStart) {
        indStart = indCFDate[loopcf];
      }
      if (indCFDate[loopcf] > indEnd) {
        indEnd = indCFDate[loopcf];
      }
    }
    final int nbCF = indEnd - indStart + 1;
    final double[] cfa = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCFInit; loopcf++) {
      cfa[indCFDate[loopcf] - indStart] = cfaInit[loopcf];
    }
    final double[] cft = new double[nbCF];
    System.arraycopy(timeLMM, indStart, cft, 0, nbCF);

    final double[] dfLMM = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      dfLMM[loopcf] = dsc.getDiscountFactor(cft[loopcf]);
    }
    final double[][] gammaLMM = new double[nbCF - 1][nbFactor];
    final double[] deltaLMM = new double[nbCF - 1];
    System.arraycopy(lmmBundle.getLmmParameter().getAccrualFactor(), indStart, deltaLMM, 0, nbCF - 1);
    final double[] aLMM = new double[nbCF - 1];
    System.arraycopy(lmmBundle.getLmmParameter().getDisplacement(), indStart, aLMM, 0, nbCF - 1);
    final double[] liborLMM = new double[nbCF - 1];
    final double amr = lmmBundle.getLmmParameter().getMeanReversion();
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      gammaLMM[loopcf] = volLMM[indStart + loopcf];
      liborLMM[loopcf] = (dfLMM[loopcf] / dfLMM[loopcf + 1] - 1.0d) / deltaLMM[loopcf];
    }
    // TODO: 4. cfe modification (for roller coasters)
    final double[] cfaMod = new double[nbCF + 1];
    final double cfaMod0 = cfa[0];
    cfaMod[0] = cfaMod0; // modified strike
    cfaMod[1] = 0.0;
    System.arraycopy(cfa, 1, cfaMod, 2, nbCF - 1);
    // 5. Pricing algorithm
    final double[] p0 = new double[nbCF];
    final double[] dP = new double[nbCF];
    double b0 = 0;
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      p0[loopcf] = dfLMM[loopcf] / dfLMM[0];
      dP[loopcf] = cfaMod[loopcf + 1] * p0[loopcf];
      b0 += dP[loopcf];
    }
    final double bK = -cfaMod0;
    final double bM = (b0 + bK) / 2.0d;
    final double[] rate0Ratio = new double[nbCF - 1];
    final double[][] mu0 = new double[nbCF - 1][nbFactor];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      rate0Ratio[loopcf] = (liborLMM[loopcf] + aLMM[loopcf]) / (liborLMM[loopcf] + 1.0 / deltaLMM[loopcf]);
    }
    for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
      mu0[0][loopfact] = rate0Ratio[0] * gammaLMM[0][loopfact];
    }
    for (int loopcf = 1; loopcf < nbCF - 1; loopcf++) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        mu0[loopcf][loopfact] = mu0[loopcf - 1][loopfact] + rate0Ratio[loopcf] * gammaLMM[loopcf][loopfact];
      }
    }
    final double meanReversionImpact = (Math.exp(2.0d * amr * timeToExpiry) - 1.0d) / (2.0d * amr);
    final double[] tau = new double[nbCF];
    final double[] tau2 = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        tau2[loopcf + 1] += mu0[loopcf][loopfact] * mu0[loopcf][loopfact];
      }
      tau2[loopcf + 1] = tau2[loopcf + 1] * meanReversionImpact;
      tau[loopcf + 1] = Math.sqrt(tau2[loopcf + 1]);
    }
    double sumNum = -bM;
    double sumDen = 0;
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      sumNum += dP[loopcf] - dP[loopcf] * tau2[loopcf] / 2.0;
      sumDen += dP[loopcf] * tau[loopcf];
    }
    final double xBar = sumNum / sumDen;
    final double[] pM = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      pM[loopcf] = p0[loopcf] * (1 - xBar * tau[loopcf] - tau2[loopcf] / 2.0);
    }
    final double[] liborM = new double[nbCF - 1];
    final double[] alphaM = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      liborM[loopcf] = (pM[loopcf] / pM[loopcf + 1] - 1.0d) / deltaLMM[loopcf];
    }
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      alphaM[loopcf] = cfaMod[loopcf + 1] * pM[loopcf] / bM;
    }
    final double[] rateMRatio = new double[nbCF - 1];
    final double[][] muM = new double[nbCF - 1][nbFactor];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      rateMRatio[loopcf] = (liborM[loopcf] + aLMM[loopcf]) / (liborM[loopcf] + 1.0 / deltaLMM[loopcf]);
    }
    for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
      muM[0][loopfact] = rateMRatio[0] * gammaLMM[0][loopfact];
    }
    for (int loopcf = 1; loopcf < nbCF - 1; loopcf++) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        muM[loopcf][loopfact] = muM[loopcf - 1][loopfact] + rateMRatio[loopcf] * gammaLMM[loopcf][loopfact];
      }
    }
    double normSigmaM = 0;
    final double[] sigmaM = new double[nbFactor];
    for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
      for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
        sigmaM[loopfact] += alphaM[loopcf + 1] * muM[loopcf][loopfact];
      }
      normSigmaM += sigmaM[loopfact] * sigmaM[loopfact];
    }
    final double impliedBlackVol = Math.sqrt(normSigmaM * meanReversionImpact);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(bK, 1, isCall);
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final BlackFunctionData dataBlack = new BlackFunctionData(b0, 1.0, impliedBlackVol);
    final double[] blkAdjoint = blackFunction.getPriceAdjoint(option, dataBlack);
    // Backward sweep
    final double pvBar = 1.0;
    final double impliedBlackVolBar = dfLMM[0] * blkAdjoint[2] * pvBar;
    final double normSigmaMBar = meanReversionImpact / (2.0 * impliedBlackVol) * impliedBlackVolBar;
    final double[] sigmaMBar = new double[nbFactor];
    for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
      sigmaMBar[loopfact] = 2 * sigmaM[loopfact] * normSigmaMBar;
    }

    final double[][] muMBar = new double[nbCF - 1][nbFactor];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        muMBar[loopcf][loopfact] = alphaM[loopcf + 1] * sigmaMBar[loopfact];
      }
    }
    for (int loopcf = nbCF - 3; loopcf >= 0; loopcf--) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        muMBar[loopcf][loopfact] += muMBar[loopcf + 1][loopfact];
      }
    }

    final double[] rateMRatioBar = new double[nbCF - 1];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        rateMRatioBar[loopcf] += gammaLMM[loopcf][loopfact] * muMBar[loopcf][loopfact];
      }
    }

    final double[] alphaMBar = new double[nbCF];
    for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
      for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
        alphaMBar[loopcf + 1] += muM[loopcf][loopfact] * sigmaMBar[loopfact];
      }
    }

    final double[] liborMBar = new double[nbCF - 1];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      liborMBar[loopcf] = ((liborM[loopcf] + 1 / deltaLMM[loopcf]) - (liborM[loopcf] + aLMM[loopcf])) / ((liborM[loopcf] + 1 / deltaLMM[loopcf]) * (liborM[loopcf] + 1 / deltaLMM[loopcf]))
          * rateMRatioBar[loopcf];
    }

    final double[] pMBar = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      pMBar[loopcf] += 1.0 / pM[loopcf + 1] / deltaLMM[loopcf] * liborMBar[loopcf];
      pMBar[loopcf + 1] += -pM[loopcf] / (pM[loopcf + 1] * pM[loopcf + 1]) / deltaLMM[loopcf] * liborMBar[loopcf];
    }
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      pMBar[loopcf] += cfaMod[loopcf + 1] / bM * alphaMBar[loopcf];
    }

    double xBarBar = 0.0;
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      xBarBar += -p0[loopcf] * tau[loopcf] * pMBar[loopcf];
    }
    final double sumNumBar = 1.0 / sumDen * xBarBar;
    final double sumDenBar = -sumNum / (sumDen * sumDen) * xBarBar;
    final double[] tauBar = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      tauBar[loopcf] = -p0[loopcf] * xBar * pMBar[loopcf];
    }
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      tauBar[loopcf] += dP[loopcf] * sumDenBar;
    }
    final double[] tau2Bar = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      tau2Bar[loopcf] = -p0[loopcf] / 2.0 * pMBar[loopcf];
    }
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      tau2Bar[loopcf] += -dP[loopcf] / 2.0 * sumNumBar;
    }
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      tau2Bar[loopcf + 1] += 1 / 2.0 / tau[loopcf + 1] * tauBar[loopcf + 1];
    }
    final double[][] mu0Bar = new double[nbCF - 1][nbFactor];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        mu0Bar[loopcf][loopfact] = 2.0 * mu0[loopcf][loopfact] * meanReversionImpact * tau2Bar[loopcf + 1];
      }
    }
    for (int loopcf = nbCF - 3; loopcf >= 0; loopcf--) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        mu0Bar[loopcf][loopfact] += mu0Bar[loopcf + 1][loopfact];
      }
    }

    final double[] rate0RatioBar = new double[nbCF - 1];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        rate0RatioBar[loopcf] += gammaLMM[loopcf][loopfact] * mu0Bar[loopcf][loopfact];
      }
    }
    final double[] aLMMBar = new double[nbCF - 1];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      aLMMBar[loopcf] = 1.0 / (liborLMM[loopcf] + 1 / deltaLMM[loopcf]) * rate0RatioBar[loopcf];
    }
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      aLMMBar[loopcf] += 1.0 / (liborM[loopcf] + 1 / deltaLMM[loopcf]) * rateMRatioBar[loopcf];
    }
    final double[] displacementBar = new double[volLMM.length];
    System.arraycopy(aLMMBar, 0, displacementBar, indStart, nbCF - 1);
    return displacementBar;
  }

  /**
   * Computes the present value curve sensitivity of the Physical delivery swaption.
   * @param swaption The swaption.
   * @param lmmBundle The LMM parameters and the curves.
   * @return The present value.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final SwaptionPhysicalFixedIbor swaption, final LiborMarketModelDisplacedDiffusionDataBundle lmmBundle) {
    // 1. Swaption CFE preparation
    final AnnuityPaymentFixed cfe = swaption.getUnderlyingSwap().accept(CFEC, lmmBundle);
    final YieldAndDiscountCurve dsc = lmmBundle.getCurve(cfe.getDiscountCurve());
    final int nbCFInit = cfe.getNumberOfPayments();
    final double multFact = Math.signum(cfe.getNthPayment(0).getAmount());
    final boolean isCall = (cfe.getNthPayment(0).getAmount() < 0);
    final double[] cftInit = new double[nbCFInit];
    final double[] cfaInit = new double[nbCFInit];
    for (int loopcf = 0; loopcf < nbCFInit; loopcf++) {
      cftInit[loopcf] = cfe.getNthPayment(loopcf).getPaymentTime();
      cfaInit[loopcf] = cfe.getNthPayment(loopcf).getAmount() * -multFact;
    }
    final double timeToExpiry = swaption.getTimeToExpiry();
    // 2. Model data
    final int nbFactor = lmmBundle.getLmmParameter().getNbFactor();
    final double[][] volLMM = lmmBundle.getLmmParameter().getVolatility();
    final double[] timeLMM = lmmBundle.getLmmParameter().getIborTime();
    // 3. Link cfe dates to lmm
    final int[] indCFDate = new int[nbCFInit];
    int indStart = nbCFInit - 1;
    int indEnd = 0;
    for (int loopcf = 0; loopcf < nbCFInit; loopcf++) {
      indCFDate[loopcf] = Arrays.binarySearch(timeLMM, cftInit[loopcf]);
      if (indCFDate[loopcf] < 0) {
        if (timeLMM[-indCFDate[loopcf] - 1] - cftInit[loopcf] < TIME_TOLERANCE) {
          indCFDate[loopcf] = -indCFDate[loopcf] - 1;
        } else {
          if (cftInit[loopcf] - timeLMM[-indCFDate[loopcf] - 2] < TIME_TOLERANCE) {
            indCFDate[loopcf] = -indCFDate[loopcf] - 2;
          } else {
            ArgumentChecker.isTrue(true, "Instrument time incompatible with LMM");
          }
        }
      }
      if (indCFDate[loopcf] < indStart) {
        indStart = indCFDate[loopcf];
      }
      if (indCFDate[loopcf] > indEnd) {
        indEnd = indCFDate[loopcf];
      }
    }
    final int nbCF = indEnd - indStart + 1;
    final double[] cfa = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCFInit; loopcf++) {
      cfa[indCFDate[loopcf] - indStart] = cfaInit[loopcf];
    }
    final double[] cft = new double[nbCF];
    System.arraycopy(timeLMM, indStart, cft, 0, nbCF);
    final double[] dfLMM = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      dfLMM[loopcf] = dsc.getDiscountFactor(cft[loopcf]);
    }
    final double[][] gammaLMM = new double[nbCF - 1][nbFactor];
    final double[] deltaLMM = new double[nbCF - 1];
    System.arraycopy(lmmBundle.getLmmParameter().getAccrualFactor(), indStart, deltaLMM, 0, nbCF - 1);
    final double[] aLMM = new double[nbCF - 1];
    System.arraycopy(lmmBundle.getLmmParameter().getDisplacement(), indStart, aLMM, 0, nbCF - 1);
    final double[] liborLMM = new double[nbCF - 1];
    final double amr = lmmBundle.getLmmParameter().getMeanReversion();
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      gammaLMM[loopcf] = volLMM[indStart + loopcf];
      liborLMM[loopcf] = (dfLMM[loopcf] / dfLMM[loopcf + 1] - 1.0d) / deltaLMM[loopcf];
    }
    final double[] cfaMod = new double[nbCF + 1];
    final double cfaMod0 = cfa[0];
    cfaMod[0] = cfaMod0; // modified strike
    cfaMod[1] = 0.0; // TODO: 4. cfe modification (for roller coasters)
    System.arraycopy(cfa, 1, cfaMod, 2, nbCF - 1);
    // 5. Pricing algorithm
    final double[] p0 = new double[nbCF];
    final double[] dP = new double[nbCF];
    double b0 = 0;
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      p0[loopcf] = dfLMM[loopcf] / dfLMM[0];
      dP[loopcf] = cfaMod[loopcf + 1] * p0[loopcf];
      b0 += dP[loopcf];
    }
    final double bK = -cfaMod[0];
    final double bM = (b0 + bK) / 2.0d;
    final double[] rate0Ratio = new double[nbCF - 1];
    final double[][] mu0 = new double[nbCF - 1][nbFactor];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      rate0Ratio[loopcf] = (liborLMM[loopcf] + aLMM[loopcf]) / (liborLMM[loopcf] + 1 / deltaLMM[loopcf]);
    }
    for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
      mu0[0][loopfact] = rate0Ratio[0] * gammaLMM[0][loopfact];
    }
    for (int loopcf = 1; loopcf < nbCF - 1; loopcf++) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        mu0[loopcf][loopfact] = mu0[loopcf - 1][loopfact] + rate0Ratio[loopcf] * gammaLMM[loopcf][loopfact];
      }
    }
    final double meanReversionImpact = (Math.exp(2.0d * amr * timeToExpiry) - 1.0d) / (2.0d * amr);
    final double[] tau = new double[nbCF];
    final double[] tau2 = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        tau2[loopcf + 1] += mu0[loopcf][loopfact] * mu0[loopcf][loopfact];
      }
      tau2[loopcf + 1] = tau2[loopcf + 1] * meanReversionImpact;
      tau[loopcf + 1] = Math.sqrt(tau2[loopcf + 1]);
    }
    double sumNum = -bM;
    double sumDen = 0;
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      sumNum += dP[loopcf] - dP[loopcf] * tau2[loopcf] / 2.0;
      sumDen += dP[loopcf] * tau[loopcf];
    }
    final double xBar = sumNum / sumDen;
    final double[] pM = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      pM[loopcf] = p0[loopcf] * (1 - xBar * tau[loopcf] - tau2[loopcf] / 2.0);
    }
    final double[] liborM = new double[nbCF - 1];
    final double[] alphaM = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      liborM[loopcf] = (pM[loopcf] / pM[loopcf + 1] - 1.0d) / deltaLMM[loopcf];
    }
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      alphaM[loopcf] = cfaMod[loopcf + 1] * pM[loopcf] / bM;
    }
    final double[] rateMRatio = new double[nbCF - 1];
    final double[][] muM = new double[nbCF - 1][nbFactor];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      rateMRatio[loopcf] = (liborM[loopcf] + aLMM[loopcf]) / (liborM[loopcf] + 1 / deltaLMM[loopcf]);
    }
    for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
      muM[0][loopfact] = rateMRatio[0] * gammaLMM[0][loopfact];
    }
    for (int loopcf = 1; loopcf < nbCF - 1; loopcf++) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        muM[loopcf][loopfact] = muM[loopcf - 1][loopfact] + rateMRatio[loopcf] * gammaLMM[loopcf][loopfact];
      }
    }
    double normSigmaM = 0;
    final double[] sigmaM = new double[nbFactor];
    for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
      for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
        sigmaM[loopfact] += alphaM[loopcf + 1] * muM[loopcf][loopfact];
      }
      normSigmaM += sigmaM[loopfact] * sigmaM[loopfact];
    }
    final double impliedBlackVol = Math.sqrt(normSigmaM * meanReversionImpact);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(bK, 1, isCall);
    final BlackPriceFunction blackFunction = new BlackPriceFunction();
    final BlackFunctionData dataBlack = new BlackFunctionData(b0, 1.0, impliedBlackVol);
    final double[] blkAdjoint = blackFunction.getPriceAdjoint(option, dataBlack);
    // Backward sweep
    final double pvBar = 1.0;
    final double impliedBlackVolBar = dfLMM[0] * blkAdjoint[2] * (swaption.isLong() ? 1.0 : -1.0) * pvBar;
    final double normSigmaMBar = meanReversionImpact / (2.0 * impliedBlackVol) * impliedBlackVolBar;
    final double[] sigmaMBar = new double[nbFactor];
    for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
      sigmaMBar[loopfact] = 2 * sigmaM[loopfact] * normSigmaMBar;
    }
    final double[][] muMBar = new double[nbCF - 1][nbFactor];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        muMBar[loopcf][loopfact] = alphaM[loopcf + 1] * sigmaMBar[loopfact];
      }
    }
    for (int loopcf = nbCF - 3; loopcf >= 0; loopcf--) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        muMBar[loopcf][loopfact] += muMBar[loopcf + 1][loopfact];
      }
    }
    final double[] rateMRatioBar = new double[nbCF - 1];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        rateMRatioBar[loopcf] += gammaLMM[loopcf][loopfact] * muMBar[loopcf][loopfact];
      }
    }
    final double[] alphaMBar = new double[nbCF];
    for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
      for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
        alphaMBar[loopcf + 1] += muM[loopcf][loopfact] * sigmaMBar[loopfact];
      }
    }
    final double[] liborMBar = new double[nbCF - 1];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      liborMBar[loopcf] = ((liborM[loopcf] + 1 / deltaLMM[loopcf]) - (liborM[loopcf] + aLMM[loopcf])) / ((liborM[loopcf] + 1 / deltaLMM[loopcf]) * (liborM[loopcf] + 1 / deltaLMM[loopcf]))
          * rateMRatioBar[loopcf];
    }
    final double[] pMBar = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      pMBar[loopcf] += 1.0 / pM[loopcf + 1] / deltaLMM[loopcf] * liborMBar[loopcf];
      pMBar[loopcf + 1] += -pM[loopcf] / (pM[loopcf + 1] * pM[loopcf + 1]) / deltaLMM[loopcf] * liborMBar[loopcf];
    }
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      pMBar[loopcf] += cfaMod[loopcf + 1] / bM * alphaMBar[loopcf];
    }
    double xBarBar = 0.0;
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      xBarBar += -p0[loopcf] * tau[loopcf] * pMBar[loopcf];
    }
    final double sumNumBar = 1.0 / sumDen * xBarBar;
    final double sumDenBar = -sumNum / (sumDen * sumDen) * xBarBar;
    final double[] tauBar = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      tauBar[loopcf] = -p0[loopcf] * xBar * pMBar[loopcf];
    }
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      tauBar[loopcf] += dP[loopcf] * sumDenBar;
    }
    final double[] tau2Bar = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      tau2Bar[loopcf] = -p0[loopcf] / 2.0 * pMBar[loopcf];
    }
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      tau2Bar[loopcf] += -dP[loopcf] / 2.0 * sumNumBar;
    }
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      tau2Bar[loopcf + 1] += 1.0 / tau[loopcf + 1] / 2.0 * tauBar[loopcf + 1];
    }
    final double[][] mu0Bar = new double[nbCF - 1][nbFactor];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        mu0Bar[loopcf][loopfact] = 2.0 * mu0[loopcf][loopfact] * meanReversionImpact * tau2Bar[loopcf + 1];
      }
    }
    for (int loopcf = nbCF - 3; loopcf >= 0; loopcf--) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        mu0Bar[loopcf][loopfact] += mu0Bar[loopcf + 1][loopfact];
      }
    }
    final double[] rate0RatioBar = new double[nbCF - 1];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      for (int loopfact = 0; loopfact < nbFactor; loopfact++) {
        rate0RatioBar[loopcf] += gammaLMM[loopcf][loopfact] * mu0Bar[loopcf][loopfact];
      }
    }
    double bMBar = -sumNumBar;
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      bMBar += -cfaMod[loopcf + 1] * pM[loopcf] / (bM * bM) * alphaMBar[loopcf];
    }
    double bKBar = bMBar / 2.0;
    bKBar += dfLMM[0] * blkAdjoint[3] * (swaption.isLong() ? 1.0 : -1.0) * pvBar;
    double b0Bar = bMBar / 2.0;
    b0Bar += dfLMM[0] * blkAdjoint[1] * (swaption.isLong() ? 1.0 : -1.0) * pvBar;
    final double[] dPBar = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      dPBar[loopcf] = b0Bar + tau[loopcf] * sumDenBar + (1.0 - tau2[loopcf] / 2.0) * sumNumBar;
    }
    final double[] p0Bar = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      p0Bar[loopcf] = cfaMod[loopcf + 1] * dPBar[loopcf] + (1 - xBar * tau[loopcf] - tau2[loopcf] / 2.0) * pMBar[loopcf];
    }

    final double[] cfaModBar = new double[nbCF + 1];
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      cfaModBar[loopcf + 1] = p0[loopcf] * dPBar[loopcf] + pM[loopcf] / bM * alphaMBar[loopcf];
    }
    cfaModBar[0] += -bKBar;

    final double[] liborLMMBar = new double[nbCF - 1];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      liborLMMBar[loopcf] = (1.0 / (liborLMM[loopcf] + 1 / deltaLMM[loopcf]) - (liborLMM[loopcf] + aLMM[loopcf])
          / ((liborLMM[loopcf] + 1 / deltaLMM[loopcf]) * (liborLMM[loopcf] + 1 / deltaLMM[loopcf])))
          * rate0RatioBar[loopcf];
    }
    final double[] dfLMMBar = new double[nbCF];
    for (int loopcf = 0; loopcf < nbCF - 1; loopcf++) {
      dfLMMBar[loopcf] += (1.0 / dfLMM[loopcf + 1]) / deltaLMM[loopcf] * liborLMMBar[loopcf];
      dfLMMBar[loopcf + 1] += -dfLMM[loopcf] / (dfLMM[loopcf + 1] * dfLMM[loopcf + 1]) / deltaLMM[loopcf] * liborLMMBar[loopcf];
    }
    for (int loopcf = 1; loopcf < nbCF; loopcf++) {
      dfLMMBar[loopcf] += 1.0 / dfLMM[0] * p0Bar[loopcf];
      dfLMMBar[0] += -dfLMM[loopcf] / (dfLMM[0] * dfLMM[0]) * p0Bar[loopcf];
    }
    dfLMMBar[0] += blkAdjoint[0] * (swaption.isLong() ? 1.0 : -1.0) * pvBar;
    final double[] cfaBar = new double[nbCF];
    cfaBar[0] = cfaModBar[0];
    System.arraycopy(cfaModBar, 2, cfaBar, 1, nbCF - 1);
    final double[] cfaInitBar = new double[nbCFInit];
    for (int loopcf = 0; loopcf < nbCFInit; loopcf++) {
      cfaInitBar[loopcf] = cfaBar[indCFDate[loopcf] - indStart];
    }

    final List<DoublesPair> listDfSensi = new ArrayList<>();
    for (int loopcf = 0; loopcf < nbCF; loopcf++) {
      final DoublesPair dfSensi = DoublesPair.of(cft[loopcf], -cft[loopcf] * dfLMM[loopcf] * dfLMMBar[loopcf]);
      listDfSensi.add(dfSensi);
    }
    final Map<String, List<DoublesPair>> pvsDF = new HashMap<>();
    pvsDF.put(cfe.getDiscountCurve(), listDfSensi);
    InterestRateCurveSensitivity sensitivity = new InterestRateCurveSensitivity(pvsDF);
    final Map<Double, InterestRateCurveSensitivity> cfeCurveSensi = swaption.getUnderlyingSwap().accept(CFECSC, lmmBundle);
    for (int loopcf = 0; loopcf < cfe.getNumberOfPayments(); loopcf++) {
      final InterestRateCurveSensitivity sensiCfe = cfeCurveSensi.get(cfe.getNthPayment(loopcf).getPaymentTime());
      if (!(sensiCfe == null)) { // There is some sensitivity to that cfe.
        sensitivity = sensitivity.plus(sensiCfe.multipliedBy(-multFact * cfaInitBar[loopcf]));
      }
    }
    return sensitivity;
  }

}
