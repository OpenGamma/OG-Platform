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
  private static final double NOTIONAL = 1000.0;
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
    SwapFuturesPriceDeliverableTransactionDefinition SwapFutureTransaction = new SwapFuturesPriceDeliverableTransactionDefinition(
        underlyingSwapFuture, QUANTITY, TRADE_DATE, TRADE_PRICE);
    TRANSACTION_EUR = SwapFutureTransaction.toDerivative(REFERENCE_DATE, LASTMARG_PRICE);
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
    SwapFuturesPriceDeliverableTransactionDefinition SwapFutureTransaction = new SwapFuturesPriceDeliverableTransactionDefinition(
        underlyingSwapFuture, QUANTITY, TRADE_DATE, TRADE_PRICE);
    TRANSACTION_GBP = SwapFutureTransaction.toDerivative(REFERENCE_DATE, LASTMARG_PRICE);
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
    SwapFuturesPriceDeliverableTransactionDefinition SwapFutureTransaction = new SwapFuturesPriceDeliverableTransactionDefinition(
        underlyingSwapFuture, QUANTITY, TRADE_DATE, TRADE_PRICE);
    TRANSACTION_USD = SwapFutureTransaction.toDerivative(REFERENCE_DATE, LASTMARG_PRICE);
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

    double[] fwd = new double[] {0.0, 0.0, 0.0, 0.0, 0.02224004998051028, 0.010937729498611616, -2.4340263653380135E-5,
        4.105708454738988E-5, 4.3345418351054834E-5, -5.201393984140168E-4, -0.0020521668056167758,
        -0.004786375926813412, -0.35038664488799554, -0.1717520732853657, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
        0.0, 0.0, 0.0, 0.0 };
    double[] dsc = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 5.763679246195801E-4, 0.0, 0.0, 0.0, 0.0, 0.0,
        1.0619418938792514E-4, 0.0, 0.0, -0.0012090902010823748, -5.946345251224808E-4, 4.277998877591835E-4,
        -0.0012089376949471905, -0.0012710288992082437, 2.6209100483393506E-4, 0.0014404291506708892,
        -7.252746527971766E-4, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
    LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivityM = new LinkedHashMap<>();
    sensitivityM.put(ObjectsPair.of(MULTI_CURVE_EUR.getName(INDEX_EUR), EUR), new DoubleMatrix1D(fwd));
    sensitivityM.put(ObjectsPair.of(MULTI_CURVE_EUR.getName(EUR), EUR), new DoubleMatrix1D(dsc));
    MultipleCurrencyParameterSensitivity bucketedPv01Exp = new MultipleCurrencyParameterSensitivity(sensitivityM);

    assertRelative("EURTest", 101.73863051641443, price, TOL);
    assertRelative("EURTest", 17.386305164144346, pv.getAmount(EUR), TOL);
    assertRelative("EURTest", -0.4962595585858385,
        pv01.getMap().get(Pairs.of(MULTI_CURVE_EUR.getName(INDEX_EUR), EUR)), TOL);
    assertRelative("EURTest", -0.002196083815885953,
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

    double[] fwd = new double[] {0.0, 0.0, 0.0, 0.0, 0.022237292670953434, 0.010936373444731199, -1.164894526601529E-4,
        -1.626355818429431E-4, -5.230461397269096E-4, -0.002092150188137771, -0.00571608484159226,
        -0.009616739944033907, -0.013163009548242259, -0.016684009963556127, -0.020046842131565722,
        -0.023349425008953813, -0.026253617944688903, -0.691945085767636, -0.134912151440719, 0.0, 0.0, 0.0, 0.0, 0.0,
        0.0, 0.0, 0.0 };
    double[] dsc = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.7408936602546343E-4, 0.0, 0.0, 0.0, 0.0, 0.0,
        -0.00101026932966663, 0.0, 0.0, -9.458842302982505E-4, -4.651889657204522E-4, -9.324673654613515E-4,
        -0.0015268987752566849, -0.001960429861494007, -3.86237138917636E-4, 0.0012773776691358198,
        0.0028481019838588737, 0.0044090350106077494, 0.006015709062808674, 0.007272592335788852, 0.007948794223061722,
        6.522225444788259E-4, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
    LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivityM = new LinkedHashMap<>();
    sensitivityM.put(ObjectsPair.of(MULTI_CURVE_GBP.getName(INDEX_GBP), GBP), new DoubleMatrix1D(fwd));
    sensitivityM.put(ObjectsPair.of(MULTI_CURVE_GBP.getName(GBP), GBP), new DoubleMatrix1D(dsc));
    MultipleCurrencyParameterSensitivity bucketedPv01Exp = new MultipleCurrencyParameterSensitivity(sensitivityM);

    assertRelative("GBPTest", 100.52514560825863, price, TOL);
    assertRelative("GBPTest", 5.251456082586287, pv.getAmount(GBP), TOL);
    assertRelative("GBPTest", -0.9114076218376711,
        pv01.getMap().get(Pairs.of(MULTI_CURVE_GBP.getName(INDEX_GBP), GBP)), TOL);
    assertRelative("GBPTest", 0.02337054652895097,
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

    double[] fwd = new double[] {0.0, 0.0, 0.0, 0.0, 0.022341729563375753, 0.01124720796226138, -0.0021731877495047244,
        0.0024729110752094415, -4.7372758957355075E-4, -0.0019570458495965625, -0.004941071910485945,
        -0.009690918913055959, -0.014837857734369403, -0.019410959161646132, -0.023497600640013577,
        -0.02720168442370931, -0.030261624004197747, -0.05210166215922652, -0.08877477030785445, -0.1612488760693649,
        -0.206760964708722, -0.20061496843580034, -1.0667209531976187, -0.06994907297508755, 0.0, 0.0, 0.0 };
    double[] dsc = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -3.4440238219189264E-5, -4.217549300573077E-7, 0.0,
        3.36361384374415E-5, 6.468488161046461E-6, 0.0, -0.0013352862023619284, -2.0542864651721873E-4,
        5.874469297784665E-5, -0.0014490518196124218, -7.409506555979266E-4, -0.001847183601434519,
        -0.0032672257608405285, -0.005247792849123575, -0.003408063903707826, -0.0010909334281992778,
        0.0010821518756012854, 0.002754266953339003, 0.004315221301355959, 0.0056040322375951425, 0.009841211062335271,
        0.018871989329945896, 0.02963716706313854, 0.03281144948746717, 0.021657999502129734, 0.004591673632754511,
        -4.468623577721324E-4, 0.0, 0.0, 0.0 };
    LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivityM = new LinkedHashMap<>();
    sensitivityM.put(ObjectsPair.of(MULTI_CURVE_USD.getName(INDEX_USD), USD), new DoubleMatrix1D(fwd));
    sensitivityM.put(ObjectsPair.of(MULTI_CURVE_USD.getName(USD), USD), new DoubleMatrix1D(dsc));
    MultipleCurrencyParameterSensitivity bucketedPv01Exp = new MultipleCurrencyParameterSensitivity(sensitivityM);

    assertRelative("USDTest", 99.91455744642417, price, TOL);
    assertRelative("USDTest", -0.8544255357583097, pv.getAmount(USD), TOL);
    assertRelative("USDTest", -1.9445550972289807,
        pv01.getMap().get(Pairs.of(MULTI_CURVE_USD.getName(INDEX_USD), USD)), TOL);
    assertRelative("USDTest", 0.11219237054692224,
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
