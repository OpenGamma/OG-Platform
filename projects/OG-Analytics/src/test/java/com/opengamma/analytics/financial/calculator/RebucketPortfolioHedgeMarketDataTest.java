/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.calculator;

import static org.testng.AssertJUnit.assertEquals;

import java.util.LinkedHashSet;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexONMaster;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.CalendarNoHoliday;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * 
 */
public class RebucketPortfolioHedgeMarketDataTest {

  private static final IndexIborMaster MASTER_IBOR_INDEX = IndexIborMaster.getInstance();
  private static final IndexONMaster MASTER_ON_INDEX = IndexONMaster.getInstance();
  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<ParameterProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);

  private static final MulticurveProviderDiscount ALL_CURVES = new MulticurveProviderDiscount();
  private static final double GBPEUR = 1.257862;
  private static final double GBPCHF = 1.528302;
  private static final double GBPPLN = 5.189937;
  private static final double GBPDKK = 9.375094;
  private static final double GBPCZK = 34.483019;
  private static final double GBPJPY = 174.377358;
  private static final double GBPSEK = 11.648428;
  private static final double GBPCAD = 1.827421;
  private static final double GBPNOK = 10.55673;
  private static final double GBPSGD = 2.130063;
  private static final double GBPHKD = 13.297358;
  private static final double GBPHUF = 389.396226;
  private static final double GBPAUD = 1.822264;
  private static final double GBPNZD = 1.943899;
  private static final double GBPZAR = 18.29522;
  private static final double GBPUSD = 1.715849;

  private static final FXMatrix FX_MATRIX = new FXMatrix(Currency.GBP, Currency.USD, GBPUSD);
  static {
    FX_MATRIX.addCurrency(Currency.ZAR, Currency.GBP, 1. / GBPZAR);
    FX_MATRIX.addCurrency(Currency.NZD, Currency.GBP, 1. / GBPNZD);
    FX_MATRIX.addCurrency(Currency.AUD, Currency.GBP, 1. / GBPAUD);
    FX_MATRIX.addCurrency(Currency.HUF, Currency.GBP, 1. / GBPHUF);
    FX_MATRIX.addCurrency(Currency.HKD, Currency.GBP, 1. / GBPHKD);
    FX_MATRIX.addCurrency(Currency.SGD, Currency.GBP, 1. / GBPSGD);
    FX_MATRIX.addCurrency(Currency.NOK, Currency.GBP, 1. / GBPNOK);
    FX_MATRIX.addCurrency(Currency.CAD, Currency.GBP, 1. / GBPCAD);
    FX_MATRIX.addCurrency(Currency.SEK, Currency.GBP, 1. / GBPSEK);
    FX_MATRIX.addCurrency(Currency.JPY, Currency.GBP, 1. / GBPJPY);
    FX_MATRIX.addCurrency(Currency.CZK, Currency.GBP, 1. / GBPCZK);
    FX_MATRIX.addCurrency(Currency.DKK, Currency.GBP, 1. / GBPDKK);
    FX_MATRIX.addCurrency(Currency.PLN, Currency.GBP, 1. / GBPPLN);
    FX_MATRIX.addCurrency(Currency.CHF, Currency.GBP, 1. / GBPCHF);
    FX_MATRIX.addCurrency(Currency.EUR, Currency.GBP, 1. / GBPEUR);
    ALL_CURVES.setForexMatrix(FX_MATRIX);
  }

  private static final double[] TIME_1 = new double[] {0.002739726, 0.019178082, 0.082191781, 0.164383562, 0.249315068, 0.498630137, 0.747945205, 1, 1.498630137, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15,
      20, 25, 30 };
  private static final double[] RATE_1 = new double[] {0.024999144, 0.026476612, 0.026513388, 0.026492233, 0.02642002, 0.026181125, 0.025942755, 0.02585582, 0.026125426, 0.026726767, 0.028451091,
      0.030824526, 0.032602585, 0.034223497, 0.035843982, 0.037022049, 0.038197679, 0.039373309, 0.041258783, 0.042991307, 0.044394545, 0.04482745, 0.044697136 };
  private static final IborIndex INDEX_1 = MASTER_IBOR_INDEX.getIndex("AUDBB3M");
  private static final String AUD_DSC_NAME = "AUD Dsc";
  private static final YieldAndDiscountCurve AUD_DSC = new YieldCurve(AUD_DSC_NAME, new InterpolatedDoublesCurve(TIME_1, RATE_1, LINEAR_FLAT, true, AUD_DSC_NAME));
  private static final String AUD_FWD_IBOR_NAME = "AUD BBSW 3M";
  private static final YieldAndDiscountCurve AUD_FWD_IBOR = new YieldCurve(AUD_FWD_IBOR_NAME, new InterpolatedDoublesCurve(TIME_1, RATE_1, LINEAR_FLAT, true, AUD_FWD_IBOR_NAME));
  static {
    ALL_CURVES.setCurve(Currency.AUD, AUD_DSC);
    ALL_CURVES.setCurve(INDEX_1, AUD_FWD_IBOR);
  }

  private static final double[] TIME_2 = new double[] {0.002739726, 0.019178082, 0.082191781, 0.164383562, 0.249315068, 0.498630137, 0.747945205, 1, 1.498630137, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15,
      20, 25, 30 };
  private static final double[] RATE_2 = new double[] {0.009624873, 0.010061529, 0.012194722, 0.012485798, 0.012511592, 0.012579628, 0.012632979, 0.012758447, 0.013453337, 0.014322626, 0.01628399,
      0.018115404, 0.019830404, 0.021466679, 0.023013962, 0.024500077, 0.025867267, 0.027126542, 0.029366091, 0.031740579, 0.033388338, 0.033432759, 0.03325666 };
  private static final IborIndex INDEX_2 = MASTER_IBOR_INDEX.getIndex("CADCDOR3M");
  private static final String CAD_DSC_NAME = "CAD Dsc";
  private static final YieldAndDiscountCurve CAD_DSC = new YieldCurve(CAD_DSC_NAME, new InterpolatedDoublesCurve(TIME_2, RATE_2, LINEAR_FLAT, true, CAD_DSC_NAME));
  private static final String CAD_FWD_IBOR_NAME = "CAD CDOR 3M";
  private static final YieldAndDiscountCurve CAD_FWD_IBOR = new YieldCurve(CAD_FWD_IBOR_NAME, new InterpolatedDoublesCurve(TIME_2, RATE_2, LINEAR_FLAT, true, CAD_FWD_IBOR_NAME));
  static {
    ALL_CURVES.setCurve(Currency.CAD, CAD_DSC);
    ALL_CURVES.setCurve(INDEX_2, CAD_FWD_IBOR);
  }

  private static final double[] TIME_3 = new double[] {0.002739726, 0.019178082, 0.038356164, 0.057534247, 0.082191781, 0.164383562, 0.249315068, 0.331506849, 0.416438356, 0.498630137, 0.580821918,
      0.665753425, 0.747945205, 0.832876712, 0.915068493, 1, 1.249315068, 1.498630137, 1.747945205, 2 };
  private static final double[] RATE_3 = new double[] {0.009985863, 0.010160715, 0.010139681, 0.010081142, 0.010005879, 0.0099771, 0.009989754, 0.009979469, 0.01000217, 0.009987489, 0.010019558,
      0.010045333, 0.010068335, 0.010104419, 0.010149039, 0.010176654, 0.010490242, 0.010806457, 0.011314748, 0.011837554 };
  private static final IndexON INDEX_3 = new IndexON("CADCORRA", Currency.CAD, DayCounts.ACT_365, 0);
  private static final String CAD_FWD_ON_NAME = "CAD CORRA";
  private static final YieldAndDiscountCurve CAD_FWD_ON = new YieldCurve(CAD_FWD_ON_NAME, new InterpolatedDoublesCurve(TIME_3, RATE_3, LINEAR_FLAT, true, CAD_FWD_ON_NAME));
  static {
    ALL_CURVES.setCurve(INDEX_3, CAD_FWD_ON);
  }

  private static final double[] TIME_4 = new double[] {0.002739726, 0.019178082, 0.082191781, 0.164383562, 0.249315068, 0.498630137, 0.747945205, 1, 1.498630137, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15,
      20, 25, 30 };
  private static final double[] RATE_4 = new double[] {-9.125E-05, -9.125E-05, -2.45791E-05, 7.25654E-05, 7.17154E-05, 7.52137E-06, -0.000106442, -0.000183338, 8.76116E-05, 0.000586682, 0.001080566,
      0.001928105, 0.003100958, 0.004578546, 0.006141706, 0.007683566, 0.00910266, 0.010362698, 0.012495917, 0.01476901, 0.016883678, 0.017667345, 0.017966257 };
  private static final IborIndex INDEX_4 = new IborIndex(Currency.CHF, Period.ofMonths(3), 2, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, true, "CHFLIBOR3M");
  private static final String CHF_DSC_NAME = "CHF Dsc";
  private static final YieldAndDiscountCurve CHF_DSC = new YieldCurve(CHF_DSC_NAME, new InterpolatedDoublesCurve(TIME_4, RATE_4, LINEAR_FLAT, true, CHF_DSC_NAME));
  private static final String CHF_FWD_IBOR_NAME = "CHF LIBOR 3M";
  private static final YieldAndDiscountCurve CHF_FWD_IBOR = new YieldCurve(CHF_FWD_IBOR_NAME, new InterpolatedDoublesCurve(TIME_4, RATE_4, LINEAR_FLAT, true, CHF_FWD_IBOR_NAME));
  static {
    ALL_CURVES.setCurve(Currency.CHF, CHF_DSC);
    ALL_CURVES.setCurve(INDEX_4, CHF_FWD_IBOR);
  }

  private static final double[] TIME_5 = new double[] {0.002739726, 0.019178082, 0.038356164, 0.057534247, 0.082191781, 0.164383562, 0.249315068, 0.331506849, 0.416438356, 0.498630137, 0.580821918,
      0.665753425, 0.747945205, 0.832876712, 0.915068493, 1, 1.249315068, 1.498630137, 1.747945205, 2 };
  private static final double[] RATE_5 = new double[] {0.000430903, 0.000414772, 0.000392189, 0.000369606, 0.000340571, 0.000276841, 0.000225481, 0.000160642, 5.16845E-05, -1.51594E-05, -3.02371E-05,
      -7.83352E-05, -0.000163533, -0.000173798, -0.000264852, -0.000270553, -0.000333424, -0.000394821, -0.000388256, -0.000329473 };
  private static final IndexON i5 = new IndexON("CHFTOIS", Currency.CHF, DayCounts.ACT_360, 1);
  private static final String CHF_FWD_ON_NAME = "CHF TOIS";
  private static final YieldAndDiscountCurve CHF_FWD_ON = new YieldCurve(CHF_FWD_ON_NAME, new InterpolatedDoublesCurve(TIME_5, RATE_5, LINEAR_FLAT, true, CHF_FWD_ON_NAME));
  static {
    ALL_CURVES.setCurve(i5, CHF_FWD_ON);
  }

  private static final double[] TIME_6 = new double[] {0.002739726, 0.019178082, 0.082191781, 0.164383562, 0.249315068, 0.498630137, 0.747945205, 1, 1.498630137, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15,
      20, 25, 30 };
  private static final double[] RATE_6 = new double[] {0.00152083, 0.00163347, 0.002540346, 0.002865306, 0.003409534, 0.003368047, 0.003302729, 0.003266638, 0.003768455, 0.004285917, 0.004999282,
      0.005913002, 0.00717423, 0.008428457, 0.009859334, 0.011377777, 0.01278361, 0.014026324, 0.016331222, 0.018877253, 0.020974956, 0.020599283, 0.020477636 };
  private static final IborIndex INDEX_6 = new IborIndex(Currency.CZK, Period.ofMonths(3), 2, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, true, "CZKPRIBOR3M");
  private static final String CZK_DSC_NAME = "CZK Dsc";
  private static final YieldAndDiscountCurve CZK_DSC = new YieldCurve(CZK_DSC_NAME, new InterpolatedDoublesCurve(TIME_6, RATE_6, LINEAR_FLAT, true, CZK_DSC_NAME));
  private static final String CZK_FWD_IBOR_NAME = "CZK PRIBOR 3M";
  private static final YieldAndDiscountCurve CZK_FWD_IBOR = new YieldCurve(CZK_FWD_IBOR_NAME, new InterpolatedDoublesCurve(TIME_6, RATE_6, LINEAR_FLAT, true, CZK_FWD_IBOR_NAME));
  static {
    ALL_CURVES.setCurve(Currency.CZK, CZK_DSC);
    ALL_CURVES.setCurve(INDEX_6, CZK_FWD_IBOR);
  }

  private static final double[] TIME_7 = new double[] {0.002739726, 0.019178082, 0.082191781, 0.164383562, 0.249315068, 0.498630137, 0.747945205, 1, 1.498630137, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15,
      20, 25, 30 };
  private static final double[] RATE_7 = new double[] {0.000709722, 0.00130114, 0.002292261, 0.002873369, 0.003088973, 0.003493153, 0.003600012, 0.003672628, 0.003827529, 0.005822266, 0.006655149,
      0.007879433, 0.009423236, 0.011151298, 0.012982308, 0.014766513, 0.016415864, 0.017890014, 0.020308707, 0.022936662, 0.025030612, 0.025647327, 0.025614217 };
  private static final IborIndex INDEX_7 = MASTER_IBOR_INDEX.getIndex("DKKCIBOR3M");
  private static final String DKK_DSC_NAME = "DKK Dsc";
  private static final YieldAndDiscountCurve DKK_DSC = new YieldCurve(DKK_DSC_NAME, new InterpolatedDoublesCurve(TIME_7, RATE_7, LINEAR_FLAT, true, DKK_DSC_NAME));
  private static final String DKK_FWD_IBOR_NAME = "DKK CIBOR 3M";
  private static final YieldAndDiscountCurve DKK_FWD_IBOR = new YieldCurve(DKK_FWD_IBOR_NAME, new InterpolatedDoublesCurve(TIME_7, RATE_7, LINEAR_FLAT, true, DKK_FWD_IBOR_NAME));
  static {
    ALL_CURVES.setCurve(Currency.DKK, DKK_DSC);
    ALL_CURVES.setCurve(INDEX_7, DKK_FWD_IBOR);
  }

  private static final double[] TIME_8 = new double[] {0.002739726, 0.019178082, 0.038356164, 0.057534247, 0.082191781, 0.164383562, 0.249315068, 0.331506849, 0.416438356, 0.498630137, 0.580821918,
      0.665753425, 0.747945205, 0.832876712, 0.915068493, 1, 1.249315068, 1.498630137, 1.747945205, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 20, 25, 30, 35, 40, 45, 50 };
  private static final double[] RATE_8 = new double[] {0.000334583, 0.000437379, 0.00049483, 0.000497119, 0.000512835, 0.000594629, 0.000596361, 0.000581119, 0.000574232, 0.000585568, 0.000565055,
      0.00054299, 0.000534119, 0.000522603, 0.000510084, 0.000494785, 0.000464981, 0.00046958, 0.000514347, 0.00059466, 0.00121841, 0.002255263, 0.003654604, 0.005302097, 0.007053575, 0.008800557,
      0.010410484, 0.011900107, 0.01449288, 0.017429603, 0.020100783, 0.021178005, 0.021549444, 0.021603861, 0.021843222, 0.021707328, 0.021679467 };
  private static final IndexON INDEX_8 = MASTER_ON_INDEX.getIndex("EONIA");
  private static final String EUR_FWD_ON_NAME = "EUR EONIA";
  private static final YieldAndDiscountCurve EUR_FWD_ON = new YieldCurve(EUR_FWD_ON_NAME, new InterpolatedDoublesCurve(TIME_8, RATE_8, LINEAR_FLAT, true, EUR_FWD_ON_NAME));
  static {
    ALL_CURVES.setCurve(INDEX_8, EUR_FWD_ON);
  }

  private static final double[] TIME_9 = new double[] {0.002739726, 0.019178082, 0.082191781, 0.164383562, 0.249315068, 0.498630137, 0.747945205, 1, 1.498630137, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15,
      20, 25, 30, 35, 40, 45, 50 };
  private static final double[] RATE_9 = new double[] {0.000202778, 0.00038753, 0.000860301, 0.001434677, 0.001569408, 0.001720972, 0.001719345, 0.001719689, 0.001759391, 0.001917859, 0.002615784,
      0.004984265, 0.00647732, 0.008145407, 0.009896156, 0.01161775, 0.01318812, 0.014614331, 0.017062463, 0.019700842, 0.021932634, 0.022713516, 0.022855409, 0.022925594, 0.023037435, 0.022915603,
      0.022810794 };
  private static final IborIndex INDEX_9 = MASTER_IBOR_INDEX.getIndex("EURIBOR6M");
  private static final String EUR_DSC_NAME = "EUR Dsc";
  private static final YieldAndDiscountCurve EUR_DSC = new YieldCurve(EUR_DSC_NAME, new InterpolatedDoublesCurve(TIME_9, RATE_9, LINEAR_FLAT, true, EUR_DSC_NAME));
  private static final String EUR_FWD_IBOR_NAME = "EUR IBOR 6M";
  private static final YieldAndDiscountCurve EUR_FWD_IBOR = new YieldCurve(EUR_FWD_IBOR_NAME, new InterpolatedDoublesCurve(TIME_9, RATE_9, LINEAR_FLAT, true, EUR_FWD_IBOR_NAME));
  static {
    ALL_CURVES.setCurve(Currency.EUR, EUR_DSC);
    ALL_CURVES.setCurve(INDEX_9, EUR_FWD_IBOR);
  }

  private static final double[] TIME_10 = new double[] {0.002739726, 0.019178082, 0.082191781, 0.164383562, 0.249315068, 0.498630137, 0.747945205, 1, 1.498630137, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15,
      20, 25, 30, 35, 40, 45, 50 };
  private static final double[] RATE_10 = new double[] {0.00469377, 0.004749784, 0.004925919, 0.005241009, 0.005490346, 0.006259777, 0.007235384, 0.008313334, 0.010562246, 0.012757999, 0.017329704,
      0.02012808, 0.022158435,
      0.023774663, 0.025147677, 0.026317843, 0.027334911, 0.02822793, 0.029700384, 0.031288368, 0.032742092, 0.033043528, 0.033001161, 0.032669711, 0.032364476, 0.032243068, 0.032132041 };
  private static final IborIndex INDEX_10 = MASTER_IBOR_INDEX.getIndex("GBPLIBOR3M");
  private static final String GBP_DSC_NAME = "GBP Dsc";
  private static final YieldAndDiscountCurve GBP_DSC = new YieldCurve(GBP_DSC_NAME, new InterpolatedDoublesCurve(TIME_10, RATE_10, LINEAR_FLAT, true, GBP_DSC_NAME));
  private static final String GBP_FWD_IBOR_NAME = "GBP LIBOR 3M";
  private static final YieldAndDiscountCurve GBP_FWD_IBOR = new YieldCurve(GBP_FWD_IBOR_NAME, new InterpolatedDoublesCurve(TIME_10, RATE_10, LINEAR_FLAT, true, GBP_FWD_IBOR_NAME));
  static {
    ALL_CURVES.setCurve(Currency.GBP, GBP_DSC);
    ALL_CURVES.setCurve(INDEX_10, GBP_FWD_IBOR);
  }

  private static final double[] TIME_11 = new double[] {0.002739726, 0.019178082, 0.038356164, 0.057534247, 0.082191781, 0.164383562, 0.249315068, 0.331506849, 0.416438356, 0.498630137, 0.580821918,
      0.665753425, 0.747945205, 0.832876712, 0.915068493, 1, 1.249315068, 1.498630137, 1.747945205, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 20, 25, 30, 35, 40, 45, 50 };
  private static final double[] RATE_11 = new double[] {0.004314974, 0.004309822, 0.004309644, 0.004325547, 0.004345993, 0.004483597, 0.004597087, 0.004746555, 0.005049037, 0.005287656, 0.00552396,
      0.005848599, 0.006142157, 0.006428136, 0.006769328, 0.007101723, 0.008143256, 0.009187015, 0.010203674, 0.011176239, 0.014695569, 0.017268367, 0.019088807, 0.020492241, 0.021674612,
      0.022692464, 0.023580853, 0.024413368, 0.025906373, 0.027754194, 0.029742613, 0.030457662, 0.030685179, 0.030333585, 0.030151253, 0.030008337, 0.029961235 };
  private static final IndexON i11 = MASTER_ON_INDEX.getIndex("SONIA");
  private static final String GBP_FWD_ON_NAME = "GBP SONIA";
  private static final YieldAndDiscountCurve GBP_FWD_ON = new YieldCurve(GBP_FWD_ON_NAME, new InterpolatedDoublesCurve(TIME_11, RATE_11, LINEAR_FLAT, true, GBP_FWD_ON_NAME));
  static {
    ALL_CURVES.setCurve(i11, GBP_FWD_ON);
  }

  private static final double[] TIME_12 = new double[] {0.002739726, 0.019178082, 0.082191781, 0.164383562, 0.249315068, 0.498630137, 0.747945205, 1, 1.498630137, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
  private static final double[] RATE_12 = new double[] {0.000678599, 0.001464279, 0.002144367, 0.003002577, 0.003752281, 0.005379177, 0.006952102, 0.00854122, 0.008220696, 0.007898412, 0.012113555,
      0.015904969, 0.018692774, 0.020629527, 0.022563949, 0.023783236, 0.024998583, 0.02621393 };
  private static final IborIndex INDEX_12 = new IborIndex(Currency.HKD, Period.ofMonths(3), 0, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, true, "HKDHIBOR3M");
  private static final String HKD_DSC_NAME = "HKD Dsc";
  private static final YieldAndDiscountCurve HKD_DSC = new YieldCurve(HKD_DSC_NAME, new InterpolatedDoublesCurve(TIME_12, RATE_12, LINEAR_FLAT, true, HKD_DSC_NAME));
  private static final String HKD_FWD_IBOR_NAME = "HKD HIBOR 3M";
  private static final YieldAndDiscountCurve HKD_FWD_IBOR = new YieldCurve(HKD_FWD_IBOR_NAME, new InterpolatedDoublesCurve(TIME_12, RATE_12, LINEAR_FLAT, true, HKD_FWD_IBOR_NAME));
  static {
    ALL_CURVES.setCurve(Currency.HKD, HKD_DSC);
    ALL_CURVES.setCurve(INDEX_12, HKD_FWD_IBOR);
  }

  private static final double[] TIME_13 = new double[] {0.002739726, 0.019178082, 0.082191781, 0.164383562, 0.249315068, 0.498630137, 0.747945205, 1, 1.498630137, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
  private static final double[] RATE_13 = new double[] {0.020277215, 0.021964446, 0.023054144, 0.023171189, 0.023436304, 0.023189999, 0.02315058, 0.023209129, 0.023456759, 0.023708516, 0.024680012,
      0.026015323, 0.027777509, 0.029941085, 0.032277517, 0.034527049, 0.036394481, 0.037685273 };
  private static final IborIndex INDEX_13 = new IborIndex(Currency.HUF, Period.ofMonths(3), 2, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, true, "HUFBUBOR3M");
  private static final String HUF_DSC_NAME = "HUF Dsc";
  private static final YieldAndDiscountCurve HUF_DSC = new YieldCurve(HUF_DSC_NAME, new InterpolatedDoublesCurve(TIME_13, RATE_13, LINEAR_FLAT, true, HUF_DSC_NAME));
  private static final String HUF_FWD_IBOR_NAME = "HUF BUBOR 3M";
  private static final YieldAndDiscountCurve HUF_FWD_IBOR = new YieldCurve(HUF_FWD_IBOR_NAME, new InterpolatedDoublesCurve(TIME_13, RATE_13, LINEAR_FLAT, true, HUF_FWD_IBOR_NAME));
  static {
    ALL_CURVES.setCurve(Currency.HUF, HUF_DSC);
    ALL_CURVES.setCurve(INDEX_13, HUF_FWD_IBOR);
  }

  private static final double[] TIME_14 = new double[] {0.002739726, 0.019178082, 0.082191781, 0.164383562, 0.249315068, 0.498630137, 0.747945205, 1, 1.498630137, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15,
      20, 25, 30, 40 };
  private static final double[] RATE_14 = new double[] {0.000506944, 0.000571324, 0.000893202, 0.001185426, 0.001310036, 0.001765062, 0.001759088, 0.001719345, 0.001702464, 0.001686886, 0.001771322,
      0.002048026, 0.002548076, 0.003245425, 0.00408218, 0.005013994, 0.00596081, 0.006917459, 0.008872748, 0.011736672, 0.015375778, 0.017392865, 0.018588669, 0.020118966 };
  private static final IborIndex INDEX_14 = MASTER_IBOR_INDEX.getIndex("JPYLIBOR3M");
  private static final String JPY_DSC_NAME = "JPY Dsc";
  private static final YieldAndDiscountCurve JPY_DSC = new YieldCurve(JPY_DSC_NAME, new InterpolatedDoublesCurve(TIME_14, RATE_14, LINEAR_FLAT, true, JPY_DSC_NAME));
  private static final String JPY_FWD_IBOR_NAME = "JPY LIBOR 3M";
  private static final YieldAndDiscountCurve JPY_FWD_IBOR = new YieldCurve(JPY_FWD_IBOR_NAME, new InterpolatedDoublesCurve(TIME_14, RATE_14, LINEAR_FLAT, true, JPY_FWD_IBOR_NAME));
  static {
    ALL_CURVES.setCurve(Currency.JPY, JPY_DSC);
    ALL_CURVES.setCurve(INDEX_14, JPY_FWD_IBOR);
  }

  private static final double[] TIME_15 = new double[] {0.002739726, 0.019178082, 0.082191781, 0.164383562, 0.249315068, 0.498630137, 0.747945205, 1, 1.498630137, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15,
      20, 25, 30 };
  private static final double[] RATE_15 = new double[] {0.015208017, 0.016051609, 0.016513004, 0.016812746, 0.016832717, 0.016687305, 0.016330836, 0.016092029, 0.016395094, 0.017078588, 0.017980658,
      0.019306123, 0.020916424, 0.022608048, 0.024168687, 0.025571073, 0.026807431, 0.027811076, 0.029334299, 0.030773005, 0.031146142, 0.030966733, 0.030855412 };
  private static final IborIndex INDEX_15 = new IborIndex(Currency.NOK, Period.ofMonths(6), 2, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, true, "NOKNIBOR6M");
  private static final String NOK_DSC_NAME = "NOK Dsc";
  private static final YieldAndDiscountCurve NOK_DSC = new YieldCurve(NOK_DSC_NAME, new InterpolatedDoublesCurve(TIME_15, RATE_15, LINEAR_FLAT, true, NOK_DSC_NAME));
  private static final String NOK_FWD_IBOR_NAME = "NOK NIBOR 6M";
  private static final YieldAndDiscountCurve NOK_FWD_IBOR = new YieldCurve(NOK_FWD_IBOR_NAME, new InterpolatedDoublesCurve(TIME_15, RATE_15, LINEAR_FLAT, true, NOK_FWD_IBOR_NAME));
  static {
    ALL_CURVES.setCurve(Currency.NOK, NOK_DSC);
    ALL_CURVES.setCurve(INDEX_15, NOK_FWD_IBOR);
  }

  private static final double[] TIME_16 = new double[] {0.002739726, 0.019178082, 0.082191781, 0.164383562, 0.249315068, 0.498630137, 0.747945205, 1, 1.498630137, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15,
      20 };
  private static final double[] RATE_16 = new double[] {0.032998508, 0.033439275, 0.035123756, 0.035852658, 0.036414149, 0.037326362, 0.038212482, 0.039107931, 0.040542411, 0.041984772, 0.043974898,
      0.045366999, 0.046394015, 0.047169861, 0.047945021, 0.048431056, 0.048915498, 0.04939994, 0.050809529, 0.051595433, 0.052594629 };
  private static final IborIndex INDEX_16 = new IborIndex(Currency.NZD, Period.ofMonths(3), 0, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, true, "NZDBKBM3M");
  private static final String NZD_DSC_NAME = "NZD Dsc";
  private static final YieldAndDiscountCurve NZD_DSC = new YieldCurve(NZD_DSC_NAME, new InterpolatedDoublesCurve(TIME_16, RATE_16, LINEAR_FLAT, true, NZD_DSC_NAME));
  private static final String NZD_FWD_IBOR_NAME = "NZD BKBM 3M";
  private static final YieldAndDiscountCurve NZD_FWD_IBOR = new YieldCurve(NZD_FWD_IBOR_NAME, new InterpolatedDoublesCurve(TIME_16, RATE_16, LINEAR_FLAT, true, NZD_FWD_IBOR_NAME));
  static {
    ALL_CURVES.setCurve(Currency.NZD, NZD_DSC);
    ALL_CURVES.setCurve(INDEX_16, NZD_FWD_IBOR);
  }

  private static final double[] TIME_17 = new double[] {0.002739726, 0.019178082, 0.082191781, 0.164383562, 0.249315068, 0.498630137, 0.747945205, 1, 1.498630137, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15,
      20, 25, 30 };
  private static final double[] RATE_17 = new double[] {0.024949147, 0.025540465, 0.025974927, 0.026305978, 0.02662163, 0.025300316, 0.024528509, 0.024128365, 0.024366983, 0.024629315, 0.025732114,
      0.027163583, 0.02861239, 0.029749209, 0.030828042, 0.031784392, 0.032549309, 0.033225614, 0.034248493, 0.035106019, 0.034867568, 0.034360166, 0.033388707 };
  private static final IborIndex INDEX_17 = new IborIndex(Currency.PLN, Period.ofMonths(6), 2, DayCounts.ACT_ACT_ISDA, BusinessDayConventions.MODIFIED_FOLLOWING, true, "PLNWIBOR6M");
  private static final String PLN_DSC_NAME = "PLN Dsc";
  private static final YieldAndDiscountCurve PLN_DSC = new YieldCurve(PLN_DSC_NAME, new InterpolatedDoublesCurve(TIME_17, RATE_17, LINEAR_FLAT, true, PLN_DSC_NAME));
  private static final String PLN_FWD_IBOR_NAME = "PLN WIBOR 6M";
  private static final YieldAndDiscountCurve PLN_FWD_IBOR = new YieldCurve(PLN_FWD_IBOR_NAME, new InterpolatedDoublesCurve(TIME_17, RATE_17, LINEAR_FLAT, true, PLN_FWD_IBOR_NAME));
  static {
    ALL_CURVES.setCurve(Currency.PLN, PLN_DSC);
    ALL_CURVES.setCurve(INDEX_17, PLN_FWD_IBOR);
  }

  private static final double[] TIME_18 = new double[] {0.002739726, 0.019178082, 0.082191781, 0.164383562, 0.249315068, 0.498630137, 0.747945205, 1, 1.498630137, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15,
      20, 25, 30 };
  private static final double[] RATE_18 = new double[] {0.003092348, 0.00315426, 0.004069285, 0.004490861, 0.004579519, 0.004701814, 0.004736725, 0.004828856, 0.00527087, 0.006055598, 0.007830963,
      0.009714998, 0.011755211, 0.013734681, 0.015622406, 0.017314942, 0.018720076, 0.019897905, 0.021741736, 0.023725451, 0.025551037, 0.025825729, 0.02587991 };
  private static final IborIndex INDEX_18 = new IborIndex(Currency.SEK, Period.ofMonths(6), 2, DayCounts.THIRTY_360, BusinessDayConventions.MODIFIED_FOLLOWING, true, "SEKSTIBOR6M");
  private static final String SEK_DSC_NAME = "SEK Dsc";
  private static final YieldAndDiscountCurve SEK_DSC = new YieldCurve(SEK_DSC_NAME, new InterpolatedDoublesCurve(TIME_18, RATE_18, LINEAR_FLAT, true, SEK_DSC_NAME));
  private static final String SEK_FWD_IBOR_NAME = "SEK STIBOR 6M";
  private static final YieldAndDiscountCurve SEK_FWD_IBOR = new YieldCurve(SEK_FWD_IBOR_NAME, new InterpolatedDoublesCurve(TIME_18, RATE_18, LINEAR_FLAT, true, SEK_FWD_IBOR_NAME));
  static {
    ALL_CURVES.setCurve(Currency.SEK, SEK_DSC);
    ALL_CURVES.setCurve(INDEX_18, SEK_FWD_IBOR);
  }

  private static final double[] TIME_19 = new double[] {0.002739726, 0.019178082, 0.082191781, 0.164383562, 0.249315068, 0.498630137, 0.747945205, 1, 1.498630137, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
  private static final double[] RATE_19 = new double[] {-0.0003474, -0.000173921, 0.001051807, 0.001613805, 0.002049592, 0.002902862, 0.003080563, 0.003221935, 0.00480019, 0.00641646, 0.010506873,
      0.014142919, 0.017187136, 0.019513716, 0.021834389, 0.023336123, 0.024826483, 0.026316843 };
  private static final IborIndex INDEX_19 = new IborIndex(Currency.SGD, Period.ofMonths(6), 2, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, true, "SGDSOR6M");
  private static final String SGD_DSC_NAME = "SGD Dsc";
  private static final YieldAndDiscountCurve SGD_DSC = new YieldCurve(SGD_DSC_NAME, new InterpolatedDoublesCurve(TIME_19, RATE_19, LINEAR_FLAT, true, SGD_DSC_NAME));
  private static final String SGD_FWD_IBOR_NAME = "SGD SOR 6M";
  private static final YieldAndDiscountCurve SGD_FWD_IBOR = new YieldCurve(SGD_FWD_IBOR_NAME, new InterpolatedDoublesCurve(TIME_19, RATE_19, LINEAR_FLAT, true, SGD_FWD_IBOR_NAME));
  static {
    ALL_CURVES.setCurve(Currency.SGD, SGD_DSC);
    ALL_CURVES.setCurve(INDEX_19, SGD_FWD_IBOR);
  }

  private static final double[] TIME_20 = new double[] {0.002739726, 0.019178082, 0.038356164, 0.057534247, 0.082191781, 0.164383562, 0.249315068, 0.331506849, 0.416438356, 0.498630137, 0.580821918,
      0.665753425, 0.747945205, 0.832876712, 0.915068493, 1, 1.249315068, 1.498630137, 1.747945205, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 20, 25, 30, 35, 40, 45, 50 };
  private static final double[] RATE_20 = new double[] {0.000912499, 0.000936434, 0.000945826, 0.000950086, 0.000970938, 0.000989464, 0.001020092, 0.001055873, 0.001098219, 0.001145006, 0.001211707,
      0.001298363, 0.001389704, 0.001507979, 0.001634315, 0.00178816, 0.002492378, 0.003207882, 0.004111423, 0.005033634, 0.009111522, 0.012911497, 0.01596567, 0.018485333, 0.020555902, 0.0223006,
      0.023762466, 0.025018688, 0.027124305, 0.029330865, 0.031283094, 0.032158586, 0.03247424, 0.032418719, 0.032423203, 0.032083934, 0.031764575 };
  private static final IndexON INDEX_20 = MASTER_ON_INDEX.getIndex("FED FUND");
  private static final String USD_FWD_ON_NAME = "USD FEDFUND";
  private static final YieldAndDiscountCurve USD_FWD_ON = new YieldCurve(USD_FWD_ON_NAME, new InterpolatedDoublesCurve(TIME_20, RATE_20, LINEAR_FLAT, true, USD_FWD_ON_NAME));
  static {
    ALL_CURVES.setCurve(INDEX_20, USD_FWD_ON);
  }

  private static final double[] TIME_21 = new double[] {0.002739726, 0.019178082, 0.082191781, 0.164383562, 0.249315068, 0.498630137, 0.747945205, 1, 1.498630137, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15,
      20, 25, 30, 35, 40, 45, 50 };
  private static final double[] RATE_21 = new double[] {0.000955082, 0.00111448, 0.001470842, 0.001889059, 0.002022375, 0.002281223, 0.002558665, 0.002984053, 0.00441012, 0.006294129, 0.010624259,
      0.014530218, 0.01770414, 0.020306362, 0.02246159, 0.024260979, 0.025777515, 0.027089519, 0.0292326, 0.03148151, 0.033491981, 0.034346858, 0.034677823, 0.034607086, 0.034602839, 0.034238296,
      0.033895022 };
  private static final IborIndex INDEX_21 = MASTER_IBOR_INDEX.getIndex("USDLIBOR6M");
  private static final String USD_DSC_NAME = "USD Dsc";
  private static final YieldAndDiscountCurve USD_DSC = new YieldCurve(USD_DSC_NAME, new InterpolatedDoublesCurve(TIME_21, RATE_21, LINEAR_FLAT, true, USD_DSC_NAME));
  private static final String USD_FWD_IBOR_NAME = "USD LIBOR 6M";
  private static final YieldAndDiscountCurve USD_FWD_IBOR = new YieldCurve(USD_FWD_IBOR_NAME, new InterpolatedDoublesCurve(TIME_21, RATE_21, LINEAR_FLAT, true, USD_FWD_IBOR_NAME));
  static {
    ALL_CURVES.setCurve(Currency.USD, USD_DSC);
    ALL_CURVES.setCurve(INDEX_21, USD_FWD_IBOR);
  }

  private static final double[] TIME_22 = new double[] {0.002739726, 0.019178082, 0.082191781, 0.164383562, 0.249315068, 0.498630137, 0.747945205, 1, 1.498630137, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15,
      20, 25, 30 };
  private static final double[] RATE_22 = new double[] {0.052796181, 0.05360681, 0.056699014, 0.057435515, 0.05781431, 0.060037883, 0.061699248, 0.06333527, 0.065675951, 0.068029493, 0.071260211,
      0.073597713, 0.075684846, 0.077630339, 0.079073655, 0.080513126, 0.081848748, 0.083002396, 0.085451336, 0.087975085, 0.088502787, 0.086303293, 0.082951117 };
  private static final IborIndex INDEX_22 = new IborIndex(Currency.ZAR, Period.ofMonths(3), 0, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, true, "ZARJIBAR3M");
  private static final String ZAR_DSC_NAME = "ZAR Dsc";
  private static final YieldAndDiscountCurve ZAR_DSC = new YieldCurve(ZAR_DSC_NAME, new InterpolatedDoublesCurve(TIME_22, RATE_22, LINEAR_FLAT, true, ZAR_DSC_NAME));
  private static final String ZAR_FWD_IBOR_NAME = "ZAR JIBAR 3M";
  private static final YieldAndDiscountCurve ZAR_FWD_IBOR = new YieldCurve(ZAR_FWD_IBOR_NAME, new InterpolatedDoublesCurve(TIME_22, RATE_22, LINEAR_FLAT, true, ZAR_FWD_IBOR_NAME));
  static {
    ALL_CURVES.setCurve(Currency.ZAR, ZAR_DSC);
    ALL_CURVES.setCurve(INDEX_22, ZAR_FWD_IBOR);
  }

  /**
   * 
   */
  @Test
  public void FRATest() {
    Calendar CALENDAR_GBP = new MondayToFridayCalendar("GBP");
    //    FRA 1x4 2x5 3x6 4x7 5x8
    ZonedDateTime[] fixingDates = new ZonedDateTime[] {DateUtils.getUTCDate(2010, 11, 3), DateUtils.getUTCDate(2010, 12, 3), DateUtils.getUTCDate(2011, 1, 3), DateUtils.getUTCDate(2011, 2, 3),
        DateUtils.getUTCDate(2011, 3, 3) };
    ZonedDateTime[] accStartDates = new ZonedDateTime[] {DateUtils.getUTCDate(2010, 11, 6), DateUtils.getUTCDate(2010, 12, 6), DateUtils.getUTCDate(2011, 1, 6),
        DateUtils.getUTCDate(2011, 2, 6), DateUtils.getUTCDate(2011, 3, 6) };
    ZonedDateTime[] accEndDates = new ZonedDateTime[] {DateUtils.getUTCDate(2011, 2, 4), DateUtils.getUTCDate(2011, 3, 4), DateUtils.getUTCDate(2011, 4, 4), DateUtils.getUTCDate(2011, 5, 4),
        DateUtils.getUTCDate(2011, 6, 4) };
    ZonedDateTime[] paymentDates = new ZonedDateTime[] {DateUtils.getUTCDate(2010, 11, 7), DateUtils.getUTCDate(2010, 12, 7), DateUtils.getUTCDate(2011, 1, 7), DateUtils.getUTCDate(2011, 2, 7),
        DateUtils.getUTCDate(2011, 3, 7) };
    int nFRA = fixingDates.length;

    DayCount dayCountPayment = DayCounts.ACT_365;
    ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 10, 9);
    double notinal = 1000000; //1m
    ForwardRateAgreement[] FRAs = new ForwardRateAgreement[nFRA];
    MultipleCurrencyParameterSensitivity[] pvpsDepositExact = new MultipleCurrencyParameterSensitivity[nFRA];
    IborIndex index = INDEX_10;
    for (int i = 0; i < nFRA; ++i) {
      double paymentAccFactor = dayCountPayment.getDayCountFraction(accStartDates[i], accEndDates[i]);
      double rate = 0.05;
      ForwardRateAgreementDefinition fraDef = new ForwardRateAgreementDefinition(index.getCurrency(), paymentDates[i], accStartDates[i], accEndDates[i], paymentAccFactor, notinal,
          fixingDates[i], index, rate, CALENDAR_GBP);
      FRAs[i] = (ForwardRateAgreement) fraDef.toDerivative(REFERENCE_DATE);
      pvpsDepositExact[i] = PSC.calculateSensitivity(FRAs[i], ALL_CURVES, ALL_CURVES.getAllNames());
    }

    LinkedHashSet<Pair<String, Integer>> order = new LinkedHashSet<>();
    // Curves with nonzero sensitivities
    order.add(Pairs.of(GBP_DSC_NAME, TIME_10.length));
    order.add(Pairs.of(GBP_FWD_IBOR_NAME, TIME_10.length));
    ParameterSensitivityWeightMatrixCalculator matrixCalc = new ParameterSensitivityWeightMatrixCalculator();
    MultipleCurrencyParameterSensitivity sum = pvpsDepositExact[0].plus(pvpsDepositExact[1]).plus(pvpsDepositExact[2]).plus(pvpsDepositExact[3]).plus(pvpsDepositExact[4]);
    double[] objectiveNodes = new double[] {2, 5, 10, 30 };
    DoubleMatrix2D matrix = matrixCalc.projectCurveNodes(ALL_CURVES, order, objectiveNodes);
    //    DoubleMatrix2D matrixSmall = matrixCalc.projectCurvesAndNodes(ALL_CURVES, order, objectiveNodes); //Summing up for each maturity over all the curves
    double[] res = PortfolioHedgingCalculator.hedgeQuantity(sum, pvpsDepositExact, matrix, order, FX_MATRIX);
    /*
     * Consistency check
     */
    for (int i = 0; i < res.length; ++i) {
      assertEquals(-1.0, res[i], 5.0e-2);
    }

    /*
     * Print test  
     */
    boolean printTest = false;
    if (printTest) {
      double[] res1 = PortfolioHedgingCalculator.hedgeQuantity(pvpsDepositExact[2], new MultipleCurrencyParameterSensitivity[] {pvpsDepositExact[0], pvpsDepositExact[1], pvpsDepositExact[3],
          pvpsDepositExact[4] }, matrix, order, FX_MATRIX);
      System.out.println(new DoubleMatrix1D(res1));
      double[] res2 = PortfolioHedgingCalculator.hedgeQuantity(pvpsDepositExact[2], new MultipleCurrencyParameterSensitivity[] {pvpsDepositExact[1], pvpsDepositExact[3] }, matrix, order, FX_MATRIX);
      System.out.println(new DoubleMatrix1D(res2));
    }
  }

  /**
   * 
   */
  @Test
  public void SwapTest() {
    Calendar cldr = new MondayToFridayCalendar("EUR");
    IborIndex index = INDEX_9;
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 11, 5);
    boolean isEom = true;
    double rate = 0.05;
    double notinal = 100000000; //100m
    Period fixedPrd = Period.ofMonths(6);
    DayCount fixedDcc = DayCounts.THIRTY_U_360;

    ZonedDateTime[] startDates = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 9, 9), DateUtils.getUTCDate(2013, 9, 9), DateUtils.getUTCDate(2013, 9, 9), DateUtils.getUTCDate(2015, 9, 9) };
    Period[] tenor = new Period[] {Period.ofYears(3), Period.ofYears(10), Period.ofYears(15), Period.ofYears(7) };
    int nSwaps = startDates.length;
    MultipleCurrencyParameterSensitivity[] pvpsDepositExact = new MultipleCurrencyParameterSensitivity[nSwaps];
    for (int i = 0; i < nSwaps; ++i) {
      ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDates[i], INDEX_9.getTenor(), INDEX_9, cldr);
      ZonedDateTime settleDate = DateUtils.getUTCDate(2013, 9, 12);
      CouponIborDefinition cpnDfn = CouponIborDefinition.from(startDates[i], endDate, notinal, index, cldr);
      AnnuityDefinition<CouponDefinition> annIborDfn = new AnnuityDefinition<CouponDefinition>(new CouponIborDefinition[] {cpnDfn }, cldr);
      AnnuityCouponFixedDefinition annFixed = AnnuityCouponFixedDefinition.from(index.getCurrency(), settleDate, tenor[i], fixedPrd, cldr, fixedDcc, index.getBusinessDayConvention(),
          isEom, notinal, rate, true);

      SwapDefinition swapDfn = new SwapDefinition(annFixed, annIborDfn);
      Swap<? extends Payment, ? extends Payment> swap = swapDfn.toDerivative(referenceDate);
      pvpsDepositExact[i] = PSC.calculateSensitivity(swap, ALL_CURVES, ALL_CURVES.getAllNames());
    }

    LinkedHashSet<Pair<String, Integer>> order = new LinkedHashSet<>();
    order.add(Pairs.of(EUR_DSC_NAME, TIME_9.length));
    order.add(Pairs.of(EUR_FWD_IBOR_NAME, TIME_9.length));
    ParameterSensitivityWeightMatrixCalculator matrixCalc = new ParameterSensitivityWeightMatrixCalculator();
    double[] objectiveNodes = new double[] {2, 5, 10, 30 };
    DoubleMatrix2D matrix = matrixCalc.projectCurveNodes(ALL_CURVES, order, objectiveNodes);
    MultipleCurrencyParameterSensitivity sum = pvpsDepositExact[0].plus(pvpsDepositExact[1]).plus(pvpsDepositExact[2]).plus(pvpsDepositExact[3]);
    double[] res = PortfolioHedgingCalculator.hedgeQuantity(sum, pvpsDepositExact, matrix, order, FX_MATRIX);

    /*
     * Consistency check
     */
    for (int i = 0; i < res.length; ++i) {
      assertEquals(-1.0, res[i], 1.0e-8);
    }

    /*
     * Print test
     */
    boolean printTest = false;
    if (printTest) {
      double[] res1 = PortfolioHedgingCalculator.hedgeQuantity(pvpsDepositExact[3], new MultipleCurrencyParameterSensitivity[] {pvpsDepositExact[0], pvpsDepositExact[1], pvpsDepositExact[2] },
          matrix, order, FX_MATRIX);
      System.out.println(new DoubleMatrix1D(res1));
      double[] res2 = PortfolioHedgingCalculator.hedgeQuantity(pvpsDepositExact[1], new MultipleCurrencyParameterSensitivity[] {pvpsDepositExact[0], pvpsDepositExact[2] }, matrix, order, FX_MATRIX);
      System.out.println(new DoubleMatrix1D(res2));
    }
  }

  /**
   * 
   */
  @Test
  public void OISTest() {
    Calendar baseCalendar = new CalendarNoHoliday("No Holidays");
    GeneratorSwapFixedON swapEonia = new GeneratorSwapFixedON("EUR1YEONIA", INDEX_8, Period.ofMonths(12), INDEX_8.getDayCount(), BusinessDayConventions.MODIFIED_FOLLOWING, true, 2,
        2, baseCalendar);
    IndexON eonia = INDEX_8;
    Calendar cal = new MondayToFridayCalendar("EUR");
    double notinal = 100000000; // 100m
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2010, 12, 27);

    ZonedDateTime[] effectiveDates = new ZonedDateTime[] {DateUtils.getUTCDate(2011, 5, 23), DateUtils.getUTCDate(2011, 3, 23), DateUtils.getUTCDate(2011, 6, 23), DateUtils.getUTCDate(2011, 6, 23) };
    Period[] tenors = new Period[] {Period.ofMonths(3), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(12) };
    int nCpnOis = effectiveDates.length;
    MultipleCurrencyParameterSensitivity[] pvpsDepositExact = new MultipleCurrencyParameterSensitivity[nCpnOis];
    for (int i = 0; i < nCpnOis; ++i) {
      CouponONDefinition cpnOisDfn = CouponONDefinition.from(eonia, effectiveDates[i], tenors[i], notinal, 2, swapEonia.getBusinessDayConvention(),
          swapEonia.isEndOfMonth(), cal);
      CouponON cpnOis = cpnOisDfn.toDerivative(referenceDate);
      pvpsDepositExact[i] = PSC.calculateSensitivity(cpnOis, ALL_CURVES, ALL_CURVES.getAllNames());
    }
    LinkedHashSet<Pair<String, Integer>> order = new LinkedHashSet<>();
    // all curves for EUR
    order.add(Pairs.of(EUR_DSC_NAME, TIME_9.length));
    order.add(Pairs.of(EUR_FWD_IBOR_NAME, TIME_9.length));
    order.add(Pairs.of(EUR_FWD_ON_NAME, TIME_8.length));
    ParameterSensitivityWeightMatrixCalculator matrixCalc = new ParameterSensitivityWeightMatrixCalculator();
    double[] objectiveNodes = new double[] {2, 5, 10, 30 };
    DoubleMatrix2D matrix = matrixCalc.projectCurveNodes(ALL_CURVES, order, objectiveNodes);
    double[] res = PortfolioHedgingCalculator.hedgeQuantity(pvpsDepositExact[0].plus(pvpsDepositExact[1]), new MultipleCurrencyParameterSensitivity[] {pvpsDepositExact[0], pvpsDepositExact[1] },
        matrix, order, FX_MATRIX);

    /*
     * Consistency check
     */
    for (int i = 0; i < res.length; ++i) {
      assertEquals(-1.0, res[i], 1.0e-3);
    }

    /*
     * Print test
     */
    boolean printTest = false;
    if (printTest) {
      double[] res1 = PortfolioHedgingCalculator.hedgeQuantity(pvpsDepositExact[1], new MultipleCurrencyParameterSensitivity[] {pvpsDepositExact[0], pvpsDepositExact[2], pvpsDepositExact[3] },
          matrix, order, FX_MATRIX);
      System.out.println(new DoubleMatrix1D(res1));
      double[] res2 = PortfolioHedgingCalculator.hedgeQuantity(pvpsDepositExact[2], new MultipleCurrencyParameterSensitivity[] {pvpsDepositExact[1], pvpsDepositExact[3] }, matrix, order, FX_MATRIX);
      System.out.println(new DoubleMatrix1D(res2));
    }
  }

  /**
   * 
   */
  @Test
  public void MixedTest() {
    double notinal = 1000000; //1m
    Calendar CALENDAR_GBP = new MondayToFridayCalendar("GBP");
    Calendar CALENDAR_EUR = new MondayToFridayCalendar("EUR");
    LinkedHashSet<Pair<String, Integer>> order = new LinkedHashSet<>();
    //curves for EUR, GBP, ON curves are not used
    order.add(Pairs.of(GBP_DSC_NAME, TIME_10.length));
    order.add(Pairs.of(GBP_FWD_IBOR_NAME, TIME_10.length));
    order.add(Pairs.of(GBP_FWD_ON_NAME, TIME_8.length));
    order.add(Pairs.of(EUR_DSC_NAME, TIME_9.length));
    order.add(Pairs.of(EUR_FWD_IBOR_NAME, TIME_9.length));
    order.add(Pairs.of(EUR_FWD_ON_NAME, TIME_8.length));
    ParameterSensitivityWeightMatrixCalculator matrixCalc = new ParameterSensitivityWeightMatrixCalculator();
    double[] objectiveNodes = new double[] {2, 5, 10, 30 };
    DoubleMatrix2D matrix = matrixCalc.projectCurveNodes(ALL_CURVES, order, objectiveNodes);

    ZonedDateTime[] fixingDates = new ZonedDateTime[] {DateUtils.getUTCDate(2011, 1, 3), DateUtils.getUTCDate(2011, 2, 3), DateUtils.getUTCDate(2011, 3, 3) };
    ZonedDateTime[] accStartDates = new ZonedDateTime[] {DateUtils.getUTCDate(2011, 1, 6), DateUtils.getUTCDate(2011, 2, 6), DateUtils.getUTCDate(2011, 3, 6) };
    ZonedDateTime[] accEndDates = new ZonedDateTime[] {DateUtils.getUTCDate(2011, 4, 4), DateUtils.getUTCDate(2011, 5, 4), DateUtils.getUTCDate(2011, 6, 4) };
    ZonedDateTime[] paymentDates = new ZonedDateTime[] {DateUtils.getUTCDate(2011, 1, 7), DateUtils.getUTCDate(2011, 2, 7), DateUtils.getUTCDate(2011, 3, 7) };
    int nFRA = fixingDates.length;

    DayCount dayCountPayment = DayCounts.ACT_365;
    ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 10, 9);
    ForwardRateAgreement[] FRAs = new ForwardRateAgreement[nFRA];
    MultipleCurrencyParameterSensitivity[] pvpsDepositExactFRAs = new MultipleCurrencyParameterSensitivity[nFRA];
    IborIndex index = INDEX_10;
    for (int i = 0; i < nFRA; ++i) {
      double paymentAccFactor = dayCountPayment.getDayCountFraction(accStartDates[i], accEndDates[i]);
      double rate = 0.05;
      ForwardRateAgreementDefinition fraDef = new ForwardRateAgreementDefinition(index.getCurrency(), paymentDates[i], accStartDates[i], accEndDates[i], paymentAccFactor, notinal,
          fixingDates[i], index, rate, CALENDAR_GBP);
      FRAs[i] = (ForwardRateAgreement) fraDef.toDerivative(REFERENCE_DATE);
      pvpsDepositExactFRAs[i] = PSC.calculateSensitivity(FRAs[i], ALL_CURVES, ALL_CURVES.getAllNames());
    }

    IborIndex indexS = INDEX_9;
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 11, 5);
    boolean isEom = true;
    double rate = 0.05;
    Period fixedPrd = Period.ofMonths(6);
    DayCount fixedDcc = DayCounts.THIRTY_U_360;

    ZonedDateTime[] startDates = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 9, 9), DateUtils.getUTCDate(2013, 9, 9), DateUtils.getUTCDate(2013, 9, 9) };
    Period[] tenor = new Period[] {Period.ofYears(3), Period.ofYears(10), Period.ofYears(15) };
    int nSwaps = startDates.length;
    MultipleCurrencyParameterSensitivity[] pvpsDepositExactSwaps = new MultipleCurrencyParameterSensitivity[nSwaps];
    for (int i = 0; i < nSwaps; ++i) {
      ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDates[i], INDEX_9.getTenor(), INDEX_9, CALENDAR_EUR);
      ZonedDateTime settleDate = DateUtils.getUTCDate(2013, 9, 12);
      CouponIborDefinition cpnDfn = CouponIborDefinition.from(startDates[i], endDate, notinal, indexS, CALENDAR_EUR);
      AnnuityDefinition<CouponDefinition> annIborDfn = new AnnuityDefinition<CouponDefinition>(new CouponIborDefinition[] {cpnDfn }, CALENDAR_EUR);
      AnnuityCouponFixedDefinition annFixed = AnnuityCouponFixedDefinition.from(indexS.getCurrency(), settleDate, tenor[i], fixedPrd, CALENDAR_EUR, fixedDcc, indexS.getBusinessDayConvention(),
          isEom, notinal, rate, true);

      SwapDefinition swapDfn = new SwapDefinition(annFixed, annIborDfn);
      Swap<? extends Payment, ? extends Payment> swap = swapDfn.toDerivative(referenceDate);
      pvpsDepositExactSwaps[i] = PSC.calculateSensitivity(swap, ALL_CURVES, ALL_CURVES.getAllNames());
    }
    MultipleCurrencyParameterSensitivity sum = pvpsDepositExactFRAs[0].plus(pvpsDepositExactFRAs[1]).plus(pvpsDepositExactFRAs[2]).plus(pvpsDepositExactSwaps[0]).plus(pvpsDepositExactSwaps[1])
        .plus(pvpsDepositExactSwaps[2]);
    double[] res = PortfolioHedgingCalculator.hedgeQuantity(sum, new MultipleCurrencyParameterSensitivity[] {pvpsDepositExactFRAs[0], pvpsDepositExactFRAs[1], pvpsDepositExactFRAs[2],
        pvpsDepositExactFRAs[0], pvpsDepositExactFRAs[1], pvpsDepositExactFRAs[2] }, matrix, order, FX_MATRIX);

    /*
     * Consistency check
     */
    for (int i = 0; i < res.length; ++i) {
      assertEquals(-0.5, res[i], 1.0e-2);
    }

    /*
     * Print test
     */
    boolean printTest = false;
    if (printTest) {
      double[] res1 = PortfolioHedgingCalculator.hedgeQuantity(pvpsDepositExactFRAs[1].plus(pvpsDepositExactSwaps[1]), new MultipleCurrencyParameterSensitivity[] {pvpsDepositExactFRAs[0],
          pvpsDepositExactFRAs[2], pvpsDepositExactSwaps[0], pvpsDepositExactSwaps[2] },
          matrix, order, FX_MATRIX);
      System.out.println(new DoubleMatrix1D(res1));
    }
  }
}
