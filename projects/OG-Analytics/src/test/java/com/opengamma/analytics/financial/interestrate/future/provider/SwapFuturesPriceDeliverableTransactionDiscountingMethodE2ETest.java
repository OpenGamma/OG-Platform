/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.util.LinkedHashMap;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.NotionalProvider;
import com.opengamma.analytics.financial.instrument.annuity.AdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.DateRelativeTo;
import com.opengamma.analytics.financial.instrument.annuity.FixedAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.FloatingAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.OffsetAdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.OffsetType;
import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceMulticurveCalculator;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableTransaction;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.calculator.discounting.PV01CurveParametersCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * 
 */
public class SwapFuturesPriceDeliverableTransactionDiscountingMethodE2ETest {
  private static final IndexIborMaster INDEX_MASTER = IndexIborMaster.getInstance();
  private static final Calendar CALENDAR_EUR = new MondayToFridayCalendar("EUR");
  private static final Calendar CALENDAR_GBP = new MondayToFridayCalendar("GBP");
  private static final Calendar CALENDAR_USD = new MondayToFridayCalendar("USD");
  private static final Currency EUR = Currency.EUR;
  private static final Currency GBP = Currency.GBP;
  private static final Currency USD = Currency.USD;
  private static final String DSC_CURVE_NAME_EUR = "EURDSFDisc-Definition";
  private static final String DSC_CURVE_NAME_GBP = "GBPDSFDisc-Definition";
  private static final String DSC_CURVE_NAME_USD = "USDDSFDisc-Definition";
  private static final String FWD_CURVE_NAME_EUR = "EURDSFIndex-Definition";
  private static final String FWD_CURVE_NAME_GBP = "GBPDSFIndex-Definition";
  private static final String FWD_CURVE_NAME_USD = "USDDSFIndex-Definition";
  private static final IborIndex INDEX_EUR = INDEX_MASTER.getIndex(IndexIborMaster.EURIBOR6M);
  private static final IborIndex INDEX_GBP = INDEX_MASTER.getIndex(IndexIborMaster.GBPLIBOR6M);
  private static final IborIndex INDEX_USD = INDEX_MASTER.getIndex(IndexIborMaster.USDLIBOR3M);

  /* multi-curves */
  private static final CombinedInterpolatorExtrapolator INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory
      .getInterpolator(Interpolator1DFactory.LINEAR,
          Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  private static final MulticurveProviderDiscount MULTI_CURVE_EUR = new MulticurveProviderDiscount();
  static {
    double[] time1 = new double[] {0.0027397260273972603, 0.019178082191780823, 0.038356164383561646,
        0.057534246575342465, 0.0821917808219178, 0.1643835616438356, 0.2493150684931507, 0.3315068493150685,
        0.41643835616438357, 0.4986301369863014, 0.5808219178082191, 0.6657534246575343, 0.7479452054794521,
        0.8328767123287671, 0.915068493150685, 1.0, 1.2493150684931507, 1.4986301369863013, 1.747945205479452, 2.0,
        3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 12.0, 15.0, 20.0, 25.0, 30.0, 35.0, 40.0, 45.0, 50.0 };
    double[] rate1 = new double[] {0.001520831, 0.001686041, 0.001780783, 0.001724785, 0.001656764, 0.001514997,
        0.001409537, 0.001308517, 0.001278126, 0.001203197, 0.001162491, 0.001129328, 0.001111826, 0.001070508,
        0.001098017, 0.001083745, 0.001104411, 0.001161028, 0.0012405, 0.001416104, 0.002709918, 0.004619998,
        0.006737195, 0.008906654, 0.010991985, 0.01295629, 0.014749177, 0.016368837, 0.019106592, 0.022002805,
        0.024189871, 0.024885653, 0.025030866, 0.025014315, 0.02518553, 0.025169289, 0.025275248 };
    InterpolatedDoublesCurve interpolatedCurve1 = InterpolatedDoublesCurve.from(time1, rate1, INTERPOLATOR,
        DSC_CURVE_NAME_EUR);
    YieldCurve yieldCurve1 = YieldCurve.from(interpolatedCurve1);
    MULTI_CURVE_EUR.setCurve(EUR, yieldCurve1);
    double[] time2 = new double[] {0.0027397260273972603, 0.019178082191780823, 0.0821917808219178, 0.1643835616438356,
        0.2493150684931507, 0.4986301369863014, 0.7479452054794521, 1.0, 1.4986301369863013, 2.0, 3.0, 4.0, 5.0, 6.0,
        7.0, 8.0, 9.0, 10.0, 12.0, 15.0, 20.0, 25.0, 30.0, 35.0, 40.0, 45.0, 50.0 };
    double[] rate2 = new double[] {0.001047686, 0.001618447, 0.002203446, 0.00231025, 0.002420615, 0.002498383,
        0.002491481, 0.00252944, 0.002705515, 0.004412506, 0.005898884, 0.008002973, 0.010199302, 0.012357847,
        0.01437717, 0.01622519, 0.017901075, 0.01939867, 0.021848319, 0.024328134, 0.025990946, 0.026347234,
        0.026269751, 0.026279792, 0.026332547, 0.026339695, 0.02636908 };
    InterpolatedDoublesCurve interpolatedCurve2 = InterpolatedDoublesCurve.from(time2, rate2, INTERPOLATOR,
        FWD_CURVE_NAME_EUR);
    YieldCurve yieldCurve2 = YieldCurve.from(interpolatedCurve2);
    MULTI_CURVE_EUR.setCurve(INDEX_EUR, yieldCurve2);
  }
  private static final MulticurveProviderDiscount MULTI_CURVE_GBP = new MulticurveProviderDiscount();
  static {
    double[] time1 = new double[] {0.0027397260273972603, 0.019178082191780823, 0.038356164383561646,
        0.057534246575342465, 0.0821917808219178, 0.1643835616438356, 0.2493150684931507, 0.3315068493150685,
        0.41643835616438357, 0.4986301369863014, 0.5808219178082191, 0.6657534246575343, 0.7479452054794521,
        0.8328767123287671, 0.915068493150685, 1.0, 1.2493150684931507, 1.4986301369863013, 1.747945205479452, 2.0,
        3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 12.0, 15.0, 20.0, 25.0, 30.0, 35.0, 40.0, 45.0, 50.0 };
    double[] rate1 = new double[] {0.004314975, 0.004232328, 0.004234656, 0.004224652, 0.004214515, 0.004213025,
        0.004227271, 0.004237123, 0.004240462, 0.004257812, 0.004307273, 0.004374733, 0.004446931, 0.004560015,
        0.004669298, 0.004792498, 0.005293175, 0.005908394, 0.006614075, 0.007400271, 0.01084908, 0.014103243,
        0.016838812, 0.019066765, 0.020958033, 0.022607147, 0.024051441, 0.025266833, 0.02722904, 0.029292804,
        0.031089389, 0.031738138, 0.031882506, 0.031460508, 0.031223231, 0.030944059, 0.031049522 };
    InterpolatedDoublesCurve interpolatedCurve1 = InterpolatedDoublesCurve.from(time1, rate1, INTERPOLATOR,
        DSC_CURVE_NAME_GBP);
    YieldCurve yieldCurve1 = YieldCurve.from(interpolatedCurve1);
    MULTI_CURVE_GBP.setCurve(GBP, yieldCurve1);
    double[] time2 = new double[] {0.0027397260273972603, 0.019178082191780823, 0.0821917808219178, 0.1643835616438356,
        0.2493150684931507, 0.4986301369863014, 0.7479452054794521, 1.0, 1.4986301369863013, 2.0, 3.0, 4.0, 5.0, 6.0,
        7.0, 8.0, 9.0, 10.0, 12.0, 15.0, 20.0, 25.0, 30.0, 35.0, 40.0, 45.0, 50.0 };
    double[] rate2 = new double[] {0.00462497, 0.00467479, 0.004830131, 0.004920495, 0.00501387, 0.005242587,
        0.005564089, 0.006060153, 0.007416189, 0.009985379, 0.013820317, 0.017361736, 0.020314939, 0.022737604,
        0.024759126, 0.026500577, 0.027982762, 0.029235558, 0.031154631, 0.032956585, 0.034131747, 0.034304467,
        0.034116588, 0.033723836, 0.033294286, 0.033041469, 0.033038905 };
    InterpolatedDoublesCurve interpolatedCurve2 = InterpolatedDoublesCurve.from(time2, rate2, INTERPOLATOR,
        FWD_CURVE_NAME_GBP);
    YieldCurve yieldCurve2 = YieldCurve.from(interpolatedCurve2);
    MULTI_CURVE_GBP.setCurve(INDEX_GBP, yieldCurve2);
  }
  private static final MulticurveProviderDiscount MULTI_CURVE_USD = new MulticurveProviderDiscount();
  static {
    double[] time1 = new double[] {0.0027397260273972603, 0.019178082191780823, 0.038356164383561646,
        0.057534246575342465, 0.0821917808219178, 0.1643835616438356, 0.2493150684931507, 0.3315068493150685,
        0.41643835616438357, 0.4986301369863014, 0.5808219178082191, 0.6657534246575343, 0.7479452054794521,
        0.8328767123287671, 0.915068493150685, 1.0, 1.2493150684931507, 1.4986301369863013, 1.747945205479452, 2.0,
        3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 12.0, 15.0, 20.0, 25.0, 30.0, 35.0, 40.0, 45.0, 50.0 };
    double[] rate1 = new double[] {6.08333E-4, 6.67474E-4, 7.01688E-4, 7.1555E-4, 7.07676E-4, 7.46728E-4, 7.57005E-4,
        7.67452E-4, 7.77672E-4, 7.98007E-4, 8.08362E-4, 8.23328E-4, 8.52634E-4, 8.74259E-4, 9.06998E-4, 9.48031E-4,
        0.001204219, 0.00146341, 0.001985715, 0.002516715, 0.00592515, 0.009970836, 0.013883528, 0.017349819,
        0.020245655, 0.022683101, 0.024764926, 0.02648729, 0.029289453, 0.032052003, 0.034523481, 0.035541588,
        0.035910913, 0.035828342, 0.035832452, 0.035395082, 0.034987264 };
    InterpolatedDoublesCurve interpolatedCurve1 = InterpolatedDoublesCurve.from(time1, rate1, INTERPOLATOR,
        DSC_CURVE_NAME_USD);
    YieldCurve yieldCurve1 = YieldCurve.from(interpolatedCurve1);
    MULTI_CURVE_USD.setCurve(USD, yieldCurve1);
    double[] time2 = new double[] {0.0027397260273972603, 0.019178082191780823, 0.0821917808219178, 0.1643835616438356,
        0.2493150684931507, 0.4986301369863014, 0.7479452054794521, 1.0, 1.4986301369863013, 2.0, 3.0, 4.0, 5.0, 6.0,
        7.0, 8.0, 9.0, 10.0, 12.0, 15.0, 20.0, 25.0, 30.0, 35.0, 40.0, 45.0, 50.0 };
    double[] rate2 = new double[] {8.67888E-4, 0.001050381, 0.0015198, 0.001732796, 0.001952891, 0.002253123,
        0.002378392, 0.002540154, 0.003185149, 0.004375999, 0.008068901, 0.012401796, 0.016506039, 0.020075813,
        0.023076569, 0.025560429, 0.027689546, 0.02945934, 0.032294993, 0.035060511, 0.037481639, 0.038446592,
        0.038769311, 0.03867287, 0.038672132, 0.038199935, 0.037758216 };
    InterpolatedDoublesCurve interpolatedCurve2 = InterpolatedDoublesCurve.from(time2, rate2, INTERPOLATOR,
        FWD_CURVE_NAME_USD);
    YieldCurve yieldCurve2 = YieldCurve.from(interpolatedCurve2);
    MULTI_CURVE_USD.setCurve(INDEX_USD, yieldCurve2);
  }

  /* common setup */
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2014, 2, 17, 0, 0);
  private static final long QUANTITY = 1;
  private static final double TRADE_PRICE = 0.0;
  private static final double LASTMARG_PRICE = 1.0;
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2008, 12, 26, 1, 0);
  private static final double NOTIONAL = 100000.0;
  private static final NotionalProvider NOTIONAL_PROV = new NotionalProvider() {
    @Override
    public double getAmount(final LocalDate date) {
      return 1.0;
    }
  };

  /* EUR */
  private static final SwapFuturesPriceDeliverableTransaction TRANSACTION_EUR;
  static {
    boolean payer = true;
    LocalDate startDate = LocalDate.of(2014, 6, 18);
    LocalDate endDate = LocalDate.of(2019, 6, 18);
    DayCount fixedDc = DayCounts.THIRTY_360;
    AdjustedDateParameters accrualPeriodParameters = new AdjustedDateParameters(CALENDAR_EUR,
        INDEX_EUR.getBusinessDayConvention());
    OffsetAdjustedDateParameters paymentDateParameters = new OffsetAdjustedDateParameters(0, OffsetType.BUSINESS,
        CALENDAR_EUR, INDEX_EUR.getBusinessDayConvention());
    OffsetAdjustedDateParameters resetDateParameters = new OffsetAdjustedDateParameters(0, OffsetType.BUSINESS,
        CALENDAR_EUR, INDEX_EUR.getBusinessDayConvention());
    OffsetAdjustedDateParameters fixingDateParameters = new OffsetAdjustedDateParameters(-2, OffsetType.BUSINESS,
        CALENDAR_EUR, BusinessDayConventions.PRECEDING);
    double rate = 0.015;
    AnnuityDefinition<?> fixedLeg = new FixedAnnuityDefinitionBuilder().payer(!payer).currency(EUR)
        .notional(NOTIONAL_PROV).startDate(startDate).endDate(endDate).dayCount(fixedDc)
        .rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).accrualPeriodFrequency(Period.ofYears(1)).
        accrualPeriodParameters(accrualPeriodParameters).paymentDateRelativeTo(DateRelativeTo.END).
        paymentDateAdjustmentParameters(paymentDateParameters).rate(rate).build();
    AnnuityDefinition<? extends PaymentDefinition> iborLeg = new FloatingAnnuityDefinitionBuilder().payer(payer).
        currency(EUR).notional(NOTIONAL_PROV).startDate(startDate).endDate(endDate).dayCount(INDEX_EUR.getDayCount())
        .rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).accrualPeriodFrequency(Period.ofMonths(6)).
        accrualPeriodParameters(accrualPeriodParameters).paymentDateRelativeTo(DateRelativeTo.END).
        paymentDateAdjustmentParameters(paymentDateParameters).index(INDEX_EUR)
        .resetDateAdjustmentParameters(resetDateParameters).resetRelativeTo(DateRelativeTo.START).
        fixingDateAdjustmentParameters(fixingDateParameters).build();
    SwapFixedIborDefinition swapDefinition = new SwapFixedIborDefinition(toFixedLeg(fixedLeg), toIborLeg(iborLeg));
    ZonedDateTime lastTradingDate = DateUtils.getUTCDate(2014, 6, 16, 0, 0);
    SwapFuturesPriceDeliverableSecurityDefinition underlyingSwapFuture = new SwapFuturesPriceDeliverableSecurityDefinition(
        lastTradingDate, swapDefinition, NOTIONAL);
    SwapFuturesPriceDeliverableTransactionDefinition swapFutureTransaction = new SwapFuturesPriceDeliverableTransactionDefinition(
        underlyingSwapFuture, QUANTITY, TRADE_DATE, TRADE_PRICE);
    TRANSACTION_EUR = swapFutureTransaction.toDerivative(REFERENCE_DATE, LASTMARG_PRICE);
  }

  /* GBP */
  private static final SwapFuturesPriceDeliverableTransaction TRANSACTION_GBP;
  static {
    boolean payer = true;
    LocalDate startDate = LocalDate.of(2014, 6, 18);
    LocalDate endDate = LocalDate.of(2024, 6, 18);
    DayCount fixedDc = DayCounts.ACT_360;
    AdjustedDateParameters accrualPeriodParameters = new AdjustedDateParameters(CALENDAR_GBP,
        INDEX_GBP.getBusinessDayConvention());
    OffsetAdjustedDateParameters paymentDateParameters = new OffsetAdjustedDateParameters(0, OffsetType.BUSINESS,
        CALENDAR_GBP, INDEX_GBP.getBusinessDayConvention());
    OffsetAdjustedDateParameters resetDateParameters = new OffsetAdjustedDateParameters(0, OffsetType.BUSINESS,
        CALENDAR_GBP, INDEX_GBP.getBusinessDayConvention());
    OffsetAdjustedDateParameters fixingDateParameters = new OffsetAdjustedDateParameters(-2, OffsetType.BUSINESS,
        CALENDAR_GBP, BusinessDayConventions.PRECEDING);
    double rate = 0.03;
    AnnuityDefinition<?> fixedLeg = new FixedAnnuityDefinitionBuilder().payer(!payer).currency(GBP).
        notional(NOTIONAL_PROV).startDate(startDate).endDate(endDate).dayCount(fixedDc).
        rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).accrualPeriodFrequency(Period.ofMonths(6)).
        accrualPeriodParameters(accrualPeriodParameters).paymentDateRelativeTo(DateRelativeTo.END).
        paymentDateAdjustmentParameters(paymentDateParameters).rate(rate).build();
    AnnuityDefinition<? extends PaymentDefinition> iborLeg = new FloatingAnnuityDefinitionBuilder().
        payer(payer).currency(GBP).notional(NOTIONAL_PROV).startDate(startDate).
        endDate(endDate).dayCount(INDEX_GBP.getDayCount()).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0))
        .accrualPeriodFrequency(Period.ofMonths(6)).accrualPeriodParameters(accrualPeriodParameters).
        paymentDateRelativeTo(DateRelativeTo.END).paymentDateAdjustmentParameters(paymentDateParameters).
        index(INDEX_GBP).resetDateAdjustmentParameters(resetDateParameters).resetRelativeTo(DateRelativeTo.START).
        fixingDateAdjustmentParameters(fixingDateParameters).build();
    SwapFixedIborDefinition swapDefinition = new SwapFixedIborDefinition(toFixedLeg(fixedLeg), toIborLeg(iborLeg));
    ZonedDateTime lastTradingDate = DateUtils.getUTCDate(2014, 6, 18, 0, 0);
    SwapFuturesPriceDeliverableSecurityDefinition underlyingSwapFuture = new SwapFuturesPriceDeliverableSecurityDefinition(
        lastTradingDate, swapDefinition, NOTIONAL);
    SwapFuturesPriceDeliverableTransactionDefinition swapFutureTransaction = new SwapFuturesPriceDeliverableTransactionDefinition(
        underlyingSwapFuture, QUANTITY, TRADE_DATE, TRADE_PRICE);
    TRANSACTION_GBP = swapFutureTransaction.toDerivative(REFERENCE_DATE, LASTMARG_PRICE);
  }

  /* USD */
  private static final SwapFuturesPriceDeliverableTransaction TRANSACTION_USD;
  static {
    boolean payer = true;
    LocalDate startDate = LocalDate.of(2014, 6, 18);
    LocalDate endDate = LocalDate.of(2044, 6, 20);
    DayCount fixedDc = DayCounts.THIRTY_U_360;
    AdjustedDateParameters accrualPeriodParameters = new AdjustedDateParameters(CALENDAR_USD,
        INDEX_USD.getBusinessDayConvention());
    OffsetAdjustedDateParameters paymentDateParameters = new OffsetAdjustedDateParameters(0, OffsetType.BUSINESS,
        CALENDAR_USD, INDEX_USD.getBusinessDayConvention());
    double rate = 0.0375;
    AnnuityDefinition<?> fixedLeg = new FixedAnnuityDefinitionBuilder().payer(!payer).currency(USD)
        .notional(NOTIONAL_PROV).startDate(startDate).endDate(endDate).dayCount(fixedDc)
        .rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).accrualPeriodFrequency(Period.ofMonths(6)).
        accrualPeriodParameters(accrualPeriodParameters).paymentDateRelativeTo(DateRelativeTo.END).
        paymentDateAdjustmentParameters(paymentDateParameters).rate(rate).build();
    OffsetAdjustedDateParameters resetDateParameters = new OffsetAdjustedDateParameters(0, OffsetType.BUSINESS,
        CALENDAR_USD, INDEX_USD.getBusinessDayConvention());
    OffsetAdjustedDateParameters fixingDateParameters = new OffsetAdjustedDateParameters(-2, OffsetType.BUSINESS,
        CALENDAR_USD, BusinessDayConventions.PRECEDING);
    AnnuityDefinition<? extends PaymentDefinition> iborLeg = new FloatingAnnuityDefinitionBuilder().payer(payer)
        .currency(USD).notional(NOTIONAL_PROV).startDate(startDate).endDate(endDate).dayCount(INDEX_USD.getDayCount()).
        rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).accrualPeriodFrequency(Period.ofMonths(3)).
        accrualPeriodParameters(accrualPeriodParameters).paymentDateRelativeTo(DateRelativeTo.END)
        .paymentDateAdjustmentParameters(paymentDateParameters).index(INDEX_USD)
        .resetDateAdjustmentParameters(resetDateParameters).resetRelativeTo(DateRelativeTo.START).
        fixingDateAdjustmentParameters(fixingDateParameters).build();
    SwapFixedIborDefinition swapDefinition = new SwapFixedIborDefinition(toFixedLeg(fixedLeg), toIborLeg(iborLeg));
    ZonedDateTime lastTradingDate = DateUtils.getUTCDate(2014, 6, 16, 0, 0);
    SwapFuturesPriceDeliverableSecurityDefinition underlyingSwapFuture = new SwapFuturesPriceDeliverableSecurityDefinition(
        lastTradingDate, swapDefinition, NOTIONAL);
    SwapFuturesPriceDeliverableTransactionDefinition swapFutureTransaction = new SwapFuturesPriceDeliverableTransactionDefinition(
        underlyingSwapFuture, QUANTITY, TRADE_DATE, TRADE_PRICE);
    TRANSACTION_USD = swapFutureTransaction.toDerivative(REFERENCE_DATE, LASTMARG_PRICE);
  }


  private static final FuturesPriceMulticurveCalculator FPMC = FuturesPriceMulticurveCalculator.getInstance();
  private static final PresentValueDiscountingCalculator PVC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator
      .getInstance();
  private static final PV01CurveParametersCalculator<ParameterProviderInterface> PV01C = new PV01CurveParametersCalculator<>(
      PVCSDC);
  private static final ParameterSensitivityParameterCalculator<ParameterProviderInterface> PVSC = new ParameterSensitivityParameterCalculator<>(
      PVCSDC);

  private static final double BASIS_POINT = 1.0E-4;
  private static final double HUNDRED = 100.0;
  private static final double TOL = 1.0e-12;

  /**
   * EUR Test
   */
  @Test
  public void EURTest() {
    double price = TRANSACTION_EUR.accept(FPMC, MULTI_CURVE_EUR) * HUNDRED;
    MultipleCurrencyAmount pv = TRANSACTION_EUR.accept(PVC, MULTI_CURVE_EUR);
    ReferenceAmount<Pair<String, Currency>> pv01 = TRANSACTION_EUR.accept(PV01C, MULTI_CURVE_EUR);
    MultipleCurrencyParameterSensitivity bucketedPv01 = PVSC.calculateSensitivity(TRANSACTION_EUR, MULTI_CURVE_EUR)
        .multipliedBy(BASIS_POINT);

    double[] fwd = new double[] {0.0, 0.0, 0.0, 0.0, 2.224004998051028, 1.0937729498611615, -0.0024340263653379224,
        0.004105708454739943, 0.004334541835107666, -0.052013939841404504, -0.20521668056167838, -0.47863759268134165,
        -35.038664488799554, -17.175207328536573, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
    double[] dsc = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.05763679246195801, 0.0, 0.0, 0.0, 0.0, 0.0,
        0.010619418938792513, 0.0, 0.0, -0.12090902010823751, -0.059463452512248084, 0.04277998877591835,
        -0.12089376949471899, -0.12710288992082436, 0.026209100483393446, 0.14404291506708877, -0.07252746527971765,
        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
    LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivityM = new LinkedHashMap<>();
    sensitivityM.put(ObjectsPair.of(MULTI_CURVE_EUR.getName(INDEX_EUR), EUR), new DoubleMatrix1D(fwd));
    sensitivityM.put(ObjectsPair.of(MULTI_CURVE_EUR.getName(EUR), EUR), new DoubleMatrix1D(dsc));
    MultipleCurrencyParameterSensitivity bucketedPv01Exp = new MultipleCurrencyParameterSensitivity(sensitivityM);

    assertRelative("EURTest", 101.73863051641443, price, TOL);
    assertRelative("EURTest", 1738.6305164144287, pv.getAmount(EUR), TOL);
    assertRelative("EURTest", -49.62595585858386,
        pv01.getMap().get(Pairs.of(MULTI_CURVE_EUR.getName(INDEX_EUR), EUR)), TOL);
    assertRelative("EURTest", -0.21960838158859544,
        pv01.getMap().get(Pairs.of(MULTI_CURVE_EUR.getName(EUR), EUR)), TOL);
    AssertSensitivityObjects.assertEquals("EURTest", bucketedPv01Exp, bucketedPv01, TOL);
  }

  /**
   * GBP Test
   */
  @Test
  public void GBPTest() {
    double price = TRANSACTION_GBP.accept(FPMC, MULTI_CURVE_GBP) * HUNDRED;
    MultipleCurrencyAmount pv = TRANSACTION_GBP.accept(PVC, MULTI_CURVE_GBP);
    ReferenceAmount<Pair<String, Currency>> pv01 = TRANSACTION_GBP.accept(PV01C, MULTI_CURVE_GBP);
    MultipleCurrencyParameterSensitivity bucketedPv01 = PVSC.calculateSensitivity(TRANSACTION_GBP, MULTI_CURVE_GBP)
        .multipliedBy(BASIS_POINT);

    double[] fwd = new double[] {0.0, 0.0, 0.0, 0.0, 2.2237292670953432, 1.0936373444731198, -0.011648945266015653,
        -0.016263558184294744, -0.05230461397269246, -0.20921501881377483, -0.5716084841592151, -0.9616739944033951,
        -1.3163009548242264, -1.6684009963556077, -2.004684213156582, -2.334942500895381, -2.625361794468877,
        -69.19450857676361, -13.4912151440719, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
    double[] dsc = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.01740893660254634, 0.0, 0.0, 0.0, 0.0, 0.0,
        -0.10102693296666299, 0.0, 0.0, -0.09458842302982506, -0.04651889657204522, -0.09324673654613515,
        -0.15268987752566845, -0.19604298614940063, -0.038623713891763646, 0.12773776691358166, 0.28481019838588767,
        0.44090350106077525, 0.6015709062808676, 0.7272592335788854, 0.7948794223061727, 0.06522225444788256, 0.0, 0.0,
        0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
    LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivityM = new LinkedHashMap<>();
    sensitivityM.put(ObjectsPair.of(MULTI_CURVE_GBP.getName(INDEX_GBP), GBP), new DoubleMatrix1D(fwd));
    sensitivityM.put(ObjectsPair.of(MULTI_CURVE_GBP.getName(GBP), GBP), new DoubleMatrix1D(dsc));
    MultipleCurrencyParameterSensitivity bucketedPv01Exp = new MultipleCurrencyParameterSensitivity(sensitivityM);

    assertRelative("GBPTest", 100.52514560825863, price, TOL);
    assertRelative("GBPTest", 525.1456082586228, pv.getAmount(GBP), TOL);
    assertRelative("GBPTest", -91.14076218376711,
        pv01.getMap().get(Pairs.of(MULTI_CURVE_GBP.getName(INDEX_GBP), GBP)), TOL);
    assertRelative("GBPTest", 2.337054652895098,
        pv01.getMap().get(Pairs.of(MULTI_CURVE_GBP.getName(GBP), GBP)), TOL);
    AssertSensitivityObjects.assertEquals("GBPTest", bucketedPv01Exp, bucketedPv01, TOL);

  }

  /**
   * USD Test
   */
  @Test
  public void USDTest() {
    double price = TRANSACTION_USD.accept(FPMC, MULTI_CURVE_USD) * HUNDRED;
    MultipleCurrencyAmount pv = TRANSACTION_USD.accept(PVC, MULTI_CURVE_USD);
    ReferenceAmount<Pair<String, Currency>> pv01 = TRANSACTION_USD.accept(PV01C, MULTI_CURVE_USD);
    MultipleCurrencyParameterSensitivity bucketedPv01 = PVSC.calculateSensitivity(TRANSACTION_USD, MULTI_CURVE_USD)
        .multipliedBy(BASIS_POINT);

    double[] fwd = new double[] {0.0, 0.0, 0.0, 0.0, 2.2341729563375754, 1.1247207962261387, -0.21731877495047303,
        0.24729110752094968, -0.04737275895735693, -0.1957045849596627, -0.4941071910486, -0.9690918913056125,
        -1.483785773436915, -1.941095916164625, -2.349760064001363, -2.720168442370897, -3.0261624004197403,
        -5.210166215922626, -8.877477030785501, -16.124887606936415, -20.676096470872203, -20.061496843580134,
        -106.67209531976185, -6.994907297508757, 0.0, 0.0, 0.0 };
    double[] dsc = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.003444023821918926, -4.217549300573078E-5, 0.0,
        0.0033636138437441506, 6.468488161046462E-4, 0.0, -0.13352862023619286, -0.020542864651721875,
        0.005874469297784665, -0.14490518196124216, -0.07409506555979266, -0.18471836014345192, -0.3267225760840528,
        -0.5247792849123576, -0.3408063903707827, -0.10909334281992829, 0.1082151875601287, 0.2754266953339004,
        0.4315221301355966, 0.5604032237595145, 0.984121106233526, 1.887198932994591, 2.9637167063138534,
        3.2811449487467224, 2.1657999502129868, 0.45916736327544677, -0.04468623577721326, 0.0, 0.0, 0.0 };
    LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivityM = new LinkedHashMap<>();
    sensitivityM.put(ObjectsPair.of(MULTI_CURVE_USD.getName(INDEX_USD), USD), new DoubleMatrix1D(fwd));
    sensitivityM.put(ObjectsPair.of(MULTI_CURVE_USD.getName(USD), USD), new DoubleMatrix1D(dsc));
    MultipleCurrencyParameterSensitivity bucketedPv01Exp = new MultipleCurrencyParameterSensitivity(sensitivityM);

    assertRelative("USDTest", 99.91455744642417, price, TOL);
    assertRelative("USDTest", -85.44255357583461, pv.getAmount(USD), TOL);
    assertRelative("USDTest", -194.45550972289809,
        pv01.getMap().get(Pairs.of(MULTI_CURVE_USD.getName(INDEX_USD), USD)), TOL);
    assertRelative("USDTest", 11.21923705469224,
        pv01.getMap().get(Pairs.of(MULTI_CURVE_USD.getName(USD), USD)), TOL);
    AssertSensitivityObjects.assertEquals("USDTest", bucketedPv01Exp, bucketedPv01, TOL);

  }

  static private AnnuityCouponFixedDefinition toFixedLeg(final AnnuityDefinition<?> leg) {
    return new AnnuityCouponFixedDefinition((CouponFixedDefinition[]) leg.getPayments(), leg.getCalendar());
  }

  static private AnnuityCouponIborDefinition toIborLeg(final AnnuityDefinition<?> leg) {
    return new AnnuityCouponIborDefinition((CouponIborDefinition[]) leg.getPayments(),
        ((CouponIborDefinition) leg.getNthPayment(0)).getIndex(), leg.getCalendar());
  }

  private void assertRelative(String message, double expected, double obtained, double relativeTol) {
    double ref = Math.max(Math.abs(expected), 1.0);
    assertEquals(message, expected, obtained, ref * relativeTol);
  }
}
