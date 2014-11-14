/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarGBP;
import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.instrument.NotionalProvider;
import com.opengamma.analytics.financial.instrument.annuity.AdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod;
import com.opengamma.analytics.financial.instrument.annuity.FixedAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.FloatingAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.OffsetAdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.OffsetType;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexONMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.financial.convention.rolldate.RollDateAdjuster;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class SwapVariationMarginE2ETest {
  private static final String DSC_CURVE_NAME = "DSC";
  private static final String FWD_ON_CURVE_NAME = "FWD-ON";
  private static final String FWD_LIBOR_CURVE_NAME = "FWD-LIBOR";

  private static final ZonedDateTime TRADE_DATE_US2 = DateUtils.getUTCDate(2014, 7, 3); // Thur
  private static final ZonedDateTime TRADE_DATE_US3 = DateUtils.getUTCDate(2014, 7, 7); // Mon (Fri is US holiday)

  private static final IndexON FEDFUND = IndexONMaster.getInstance().getIndex("FED FUND");
  private static final IborIndex USDLIBOR3M = IndexIborMaster.getInstance().getIndex("USDLIBOR3M");
  private static final Currency USD = USDLIBOR3M.getCurrency();

  private static final double FF_RATE1 = 0.0009;
  private static final double FF_RATE2 = 0.0009;

  private static final MulticurveProviderDiscount MULTICURVES_USD1 = new MulticurveProviderDiscount();
  private static final MulticurveProviderDiscount MULTICURVES_USD2 = new MulticurveProviderDiscount();
  private static final MulticurveProviderDiscount MULTICURVES_USD3 = new MulticurveProviderDiscount();
  private static final Interpolator1D INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  static {
    /* 02/July/2014 */
    double[] dscTimes1 = new double[] {0.01095842925, 0.01369815984, 0.03287747535, 0.09863333227, 0.18356909953,
        0.45754739846, 0.70687161084, 0.95616732666, 1.20548153719, 1.45478657684, 1.70411524190, 1.95347821566,
        2.22197632022, 2.47117682059, 2.70149208152, 3.21926342143, 4.01937825506, 5.01657988710, 6.01935959057,
        7.01919753169, 8.01933948231, 9.02498500777, 10.02235304525, 12.02141165898, 15.02664881683, 20.03377185574,
        25.02964085851, 30.03479323390, 35.03765330769, 40.03999096762, 45.04514347176, 50.04965110008 };
    double[] dscRates1 = new double[] {0.00096721737, 0.00096724351, 0.00113046259, 0.00149182053, 0.00190657926,
        0.00217203075, 0.00239894775, 0.00276643054, 0.00336425395, 0.00415917487, 0.00508621446, 0.00610706740,
        0.00727381965, 0.00840991547, 0.00949081897, 0.01185576611, 0.01490711231, 0.01828488277, 0.02095389586,
        0.02323186078, 0.02509444649, 0.02667811986, 0.02803562919, 0.03023990401, 0.03251801663, 0.03455749560,
        0.03542686100, 0.03572201485, 0.03560291379, 0.03555452747, 0.03504360102, 0.03455171787 };
    InterpolatedDoublesCurve InterpCurveDsc1 = InterpolatedDoublesCurve.from(dscTimes1, dscRates1, INTERPOLATOR);
    YieldAndDiscountCurve dscCurve1 = new YieldCurve(DSC_CURVE_NAME, InterpCurveDsc1);
    double[] ffTimes1 = new double[] {0.01095929069, 0.01369795433, 0.03287613764, 0.05205512049, 0.07123101812,
        0.09863054394, 0.18355963863, 0.26574072365, 0.35615065035, 0.43286072851, 0.51780039824, 0.60549112383,
        0.68216000732, 0.76440147461, 0.84658620996, 0.93148773628, 1.01366802156, 1.51786855258, 2.01651704088,
        3.02199197274, 4.01899015214, 5.01651289011, 6.01938001790, 7.01896174968, 8.01955779022, 9.02444190066,
        10.02222430044, 12.02222850251, 15.02796414536, 20.03358050937, 25.03039956958, 30.03433773111, 35.03952639026,
        40.04264748879, 45.04448376302, 50.04783786290 };
    double[] ffRates1 = new double[] {0.00101387482, 0.00101392944, 0.00100206274, 0.00099896502, 0.00099135231,
        0.00100511979, 0.00103254261, 0.00104259491, 0.00108192307, 0.00110205188, 0.00115170043, 0.00121163736,
        0.00127172428, 0.00135171829, 0.00145190762, 0.00158214889, 0.00172253169, 0.00315095426, 0.00507767127,
        0.00942830095, 0.01330468634, 0.01656098765, 0.01914068811, 0.02132709237, 0.02313514229, 0.02466421861,
        0.02596417201, 0.02813162612, 0.03036711143, 0.03238037601, 0.03324935834, 0.03353202579, 0.03342881283,
        0.03339114548, 0.03291343068, 0.03245198223 };
    InterpolatedDoublesCurve INTERPOLATED_CURVE_FF1 = InterpolatedDoublesCurve.from(ffTimes1, ffRates1, INTERPOLATOR);
    YieldAndDiscountCurve ffCurve1 = new YieldCurve(FWD_ON_CURVE_NAME, INTERPOLATED_CURVE_FF1);
    double[] lbrTimes1 = new double[] {0.26575223274, 0.45754473200, 0.70686011233, 0.95613674437, 1.20545871004,
        1.45479709938, 1.70404990449, 1.95350536782, 2.22188352363, 2.47134197895, 2.70128103723, 2.96997756485,
        3.21917464880, 4.01908704695, 5.01633873954, 6.01888815551, 7.01906059545, 8.01917509066, 9.02432981585,
        10.02224894096, 12.02171450229, 15.02677618758, 20.03191950562, 25.02921057805, 30.03668707370, 35.03976522697,
        40.04031288068, 45.04465198320, 50.04851734410 };
    double[] lbrRates1 = new double[] {0.00235264603, 0.00236422539, 0.00252335849, 0.00285844329, 0.00343723262,
        0.00421982086, 0.00513782974, 0.00615202660, 0.00731290261, 0.00844530343, 0.00952393208, 0.01076519596,
        0.01188403273, 0.01489747592, 0.01826888149, 0.02093130516, 0.02320042844, 0.02505600792, 0.02663093254,
        0.02798354847, 0.03017215797, 0.03243303816, 0.03445196258, 0.03531650390, 0.03561495260, 0.03551036706,
        0.03546918437, 0.03498823032, 0.03452914509 };
    InterpolatedDoublesCurve InterpCurveLbr1 = InterpolatedDoublesCurve.from(lbrTimes1, lbrRates1, INTERPOLATOR);
    YieldAndDiscountCurve lbrCurve1 = new YieldCurve(FWD_LIBOR_CURVE_NAME, InterpCurveLbr1);
    /* 03/July/2014 */
    double[] dscTimes2 = new double[] {0.00821917772, 0.01095890352, 0.03013698607, 0.09589040765, 0.18082192177,
        0.45479450332, 0.70410956895, 0.95342464167, 1.20273977727, 1.45205486034, 1.70136989363, 1.95068497391,
        2.21917817633, 2.46849325336, 2.69863018334, 3.21643836782, 4.01643821648, 5.01369873020, 6.01643819462,
        7.01643841983, 8.01643860607, 9.02191818464, 10.01917856648, 12.01917769661, 15.02465813050, 20.03013607137,
        25.02739785135, 30.03287668346, 35.03561670824, 40.03835503009, 45.04109578525, 50.04657403628 };
    double[] dscRates2 = new double[] {0.00096727597, 0.00096727088, 0.00114527931, 0.00150686760, 0.00192083350,
        0.00220871251, 0.00242363499, 0.00277252301, 0.00337042907, 0.00416579559, 0.00510107173, 0.00612125344,
        0.00729328528, 0.00843363535, 0.00951832428, 0.01188194496, 0.01494376258, 0.01829356466, 0.02107615206,
        0.02322597507, 0.02514395943, 0.02677019149, 0.02817684776, 0.03020471464, 0.03246829373, 0.03446274190,
        0.03527829146, 0.03598005480, 0.03572186875, 0.03553753595, 0.03502895597, 0.03453848851 };
    InterpolatedDoublesCurve InterpCurveDsc2 = InterpolatedDoublesCurve.from(dscTimes2, dscRates2, INTERPOLATOR);
    YieldAndDiscountCurve dscCurve2 = new YieldCurve(DSC_CURVE_NAME, InterpCurveDsc2);
    double[] ffTimes2 = new double[] {0.00821917828, 0.01095890445, 0.03013698516, 0.04931506844, 0.06849314803,
        0.09589040986, 0.18082191120, 0.26301369783, 0.35342466317, 0.43013698750, 0.51506847725, 0.60273972988,
        0.67945204778, 0.76164385905, 0.84383562002, 0.92876715103, 1.01095889043, 1.51506845939, 2.01369863660,
        3.01917814475, 4.01643845817, 5.01369864898, 6.01643821741, 7.01643815553, 8.01643818363, 9.02191755537,
        10.01917768087, 12.01917756038, 15.02465805488, 20.03013731266, 25.02739654038, 30.03287739403, 35.03561680595,
        40.03835668130, 45.04109451666, 50.04657741183 };
    double[] ffRates2 = new double[] {0.00101386937, 0.00101389014, 0.00100096737, 0.00099805366, 0.00099041232,
        0.00101381469, 0.00103280418, 0.00105266482, 0.00108249042, 0.00110261587, 0.00116236806, 0.00122250257,
        0.00127273546, 0.00135293816, 0.00145332809, 0.00158391324, 0.00172450887, 0.00315462823, 0.00508450461,
        0.00942814743, 0.01334169816, 0.01656976347, 0.01926176315, 0.02132098899, 0.02318354255, 0.02475485967,
        0.02610331797, 0.02809530808, 0.03031689217, 0.03228633037, 0.03310478454, 0.03377763732, 0.03354582386,
        0.03337429923, 0.03289880102, 0.03244115081 };
    InterpolatedDoublesCurve INTERPOLATED_CURVE_FF2 = InterpolatedDoublesCurve.from(ffTimes2, ffRates2, INTERPOLATOR);
    YieldAndDiscountCurve ffCurve2 = new YieldCurve(FWD_ON_CURVE_NAME, INTERPOLATED_CURVE_FF2);
    double[] lbrTimes2 = new double[] {0.26301370085, 0.45479451986, 0.70410960103, 0.95342464738, 1.20273976670,
        1.45205483851, 1.70136980390, 1.95068483662, 2.21917816185, 2.46849306048, 2.69863006191, 2.96712331061,
        3.21643849439, 4.01643852113, 5.01369875287, 6.01643832990, 7.01643863922, 8.01643835273, 9.02191762578,
        10.01917819126, 12.01917793189, 15.02465812116, 20.03013634616, 25.02739623547, 30.03287541803, 35.03561566812,
        40.03835575032, 45.04109714236, 50.04657731444 };
    double[] lbrRates2 = new double[] {0.00235248421, 0.00239192292, 0.00254188824, 0.00285977967, 0.00343970251,
        0.00422316121, 0.00514990004, 0.00616369553, 0.00733079196, 0.00846765190, 0.00954963325, 0.01078984869,
        0.01190761546, 0.01493386690, 0.01827531108, 0.02105270967, 0.02319556999, 0.02510435329, 0.02672271518,
        0.02812200961, 0.03013452980, 0.03238450737, 0.03435871437, 0.03517137923, 0.03586013500, 0.03562552789,
        0.03545311374, 0.03497539140, 0.03451664589 };
    InterpolatedDoublesCurve InterpCurveLbr2 = InterpolatedDoublesCurve.from(lbrTimes2, lbrRates2, INTERPOLATOR);
    YieldAndDiscountCurve lbrCurve2 = new YieldCurve(FWD_LIBOR_CURVE_NAME, InterpCurveLbr2);
    /* 07/July/2014 */
    double[] dscTimes3 = new double[] {0.00273972589, 0.00547945201, 0.02465753365, 0.09589040840, 0.17534246591,
        0.44657534279, 0.69589042959, 0.94520545477, 1.19452049684, 1.44383559491, 1.69315072177, 1.94246584632,
        2.21095892868, 2.46027401387, 2.69041100611, 3.20821913399, 4.00821937112, 5.00821940261, 6.01095873038,
        7.01095868901, 8.01643863023, 9.01369832670, 10.01369855804, 12.01369839421, 15.01643860052, 20.02191874668,
        25.02739741777, 30.03287538199, 35.03013709861, 40.03287492353, 45.03561561781, 50.04109809551 };
    double[] dscRates3 = new double[] {0.00095811880, 0.00095812287, 0.00118281208, 0.00152106268, 0.00192284607,
        0.00224264554, 0.00248421046, 0.00286021601, 0.00347552977, 0.00428367908, 0.00522089254, 0.00624266365,
        0.00741701389, 0.00855935125, 0.00964041390, 0.01199381025, 0.01506782831, 0.01830085554, 0.02095062037,
        0.02316982617, 0.02498254741, 0.02650876316, 0.02782732931, 0.03000098049, 0.03226256874, 0.03428026513,
        0.03515035800, 0.03544431408, 0.03535889934, 0.03534352896, 0.03496901832, 0.03461893225 };
    InterpolatedDoublesCurve InterpCurveDsc3 = InterpolatedDoublesCurve.from(dscTimes3, dscRates3, INTERPOLATOR);
    YieldAndDiscountCurve dscCurve3 = new YieldCurve(DSC_CURVE_NAME, InterpCurveDsc3);
    double[] ffTimes3 = new double[] {0.00273972587, 0.00547945173, 0.02465753423, 0.04383561765, 0.06301369684,
        0.09589040879, 0.17534247408, 0.25753424945, 0.34520549567, 0.42465752101, 0.50958903410, 0.59452057017,
        0.67123290373, 0.75616434808, 0.84383559799, 0.92328768538, 1.00547946584, 1.51506849564, 2.01369869642,
        3.01095880960, 4.00821921650, 5.00821908489, 6.01095917857, 7.01095856141, 8.01643846822, 9.01369883272,
        10.01369855534, 12.01369846180, 15.01643855548, 20.02191766784, 25.02739801662, 30.03287751311, 35.03013817380,
        40.03287774944, 45.03561553560, 50.04109819267 };
    double[] ffRates3 = new double[] {0.00091246760, 0.00091250310, 0.00095189514, 0.00095684520, 0.00095183058,
        0.00096984424, 0.00099097493, 0.00103149657, 0.00106198288, 0.00110236034, 0.00117288037, 0.00124357317,
        0.00130410766, 0.00138482418, 0.00150586022, 0.00162688321, 0.00178824684, 0.00328317494, 0.00523548873,
        0.00961233155, 0.01346227879, 0.01657436016, 0.01913413697, 0.02126315379, 0.02302062688, 0.02449130934,
        0.02575504490, 0.02789266251, 0.03011077486, 0.03208652882, 0.03297417205, 0.03325641167, 0.03318544248,
        0.03317855937, 0.03283078363, 0.03250420088 };
    InterpolatedDoublesCurve INTERPOLATED_CURVE_FF3 = InterpolatedDoublesCurve.from(ffTimes3, ffRates3, INTERPOLATOR);
    YieldAndDiscountCurve ffCurve3 = new YieldCurve(FWD_ON_CURVE_NAME, INTERPOLATED_CURVE_FF3);
    double[] lbrTimes3 = new double[] {0.25753424569, 0.44657534890, 0.69589038177, 0.94520552336, 1.19452053460,
        1.44383555230, 1.69315061979, 1.94246568544, 2.21095896277, 2.46027398570, 2.69041099173, 2.95890405247,
        3.20821913353, 4.00821907284, 5.00821924063, 6.01095880516, 7.01095902425, 8.01643834605, 9.01369835223,
        10.01369814232, 12.01369831859, 15.01643902856, 20.02191771749, 25.02739647661, 30.03287571771, 35.03013638151,
        40.03287718111, 45.03561624586, 50.04109804811 };
    double[] lbrRates3 = new double[] {0.00237286592, 0.00242994052, 0.00260428600, 0.00294864586, 0.00354538953,
        0.00434146960, 0.00527002768, 0.00628622524, 0.00745487124, 0.00859283305, 0.00967187872, 0.01090961796,
        0.01201972077, 0.01505724962, 0.01828377742, 0.02092648414, 0.02313896553, 0.02494257373, 0.02646253989,
        0.02777232140, 0.02993309868, 0.03217777163, 0.03417588894, 0.03503896806, 0.03533974425, 0.03526489040,
        0.03525441814, 0.03490487787, 0.03457614181 };
    InterpolatedDoublesCurve InterpCurveLbr3 = InterpolatedDoublesCurve.from(lbrTimes3, lbrRates3, INTERPOLATOR);
    YieldAndDiscountCurve lbrCurve3 = new YieldCurve(FWD_LIBOR_CURVE_NAME, InterpCurveLbr3);
    MULTICURVES_USD1.setCurve(USD, dscCurve1);
    MULTICURVES_USD1.setCurve(FEDFUND, ffCurve1);
    MULTICURVES_USD1.setCurve(USDLIBOR3M, lbrCurve1);
    MULTICURVES_USD2.setCurve(USD, dscCurve2);
    MULTICURVES_USD2.setCurve(FEDFUND, ffCurve2);
    MULTICURVES_USD2.setCurve(USDLIBOR3M, lbrCurve2);
    MULTICURVES_USD3.setCurve(USD, dscCurve3);
    MULTICURVES_USD3.setCurve(FEDFUND, ffCurve3);
    MULTICURVES_USD3.setCurve(USDLIBOR3M, lbrCurve3);
  }

  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final DayCount DC_30U_360 = DayCounts.THIRTY_U_360;
  private static final double NOTIONAL = 100000000; //100 m
  private static final NotionalProvider NOTIONAL_PROVIDER = new NotionalProvider() {
    @Override
    public double getAmount(final LocalDate date) {
      return NOTIONAL;
    }
  };

  private static final AdjustedDateParameters ADJUSTED_DATE_USDLIBOR = new AdjustedDateParameters(NYC,
      USDLIBOR3M.getBusinessDayConvention());
  private static final OffsetAdjustedDateParameters OFFSET_FIXING_USDLIBOR =
      new OffsetAdjustedDateParameters(-2, OffsetType.BUSINESS, NYC, USDLIBOR3M.getBusinessDayConvention());
  private static final OffsetAdjustedDateParameters OFFSET_PAYMENT_USDLIBOR =
      new OffsetAdjustedDateParameters(0, OffsetType.BUSINESS, NYC, USDLIBOR3M.getBusinessDayConvention());
  private static final Period P3M = Period.ofMonths(3);
  private static final double FIXED_RATE = 0.015;
  private static final RollDateAdjuster ROLL_DATE_ADJUSTER = RollConvention.NONE.getRollDateAdjuster(0);

  // Swap Fixed vs Libor3M, USD
  private static final LocalDate START_DATE1 = LocalDate.of(2014, 6, 12);
  private static final LocalDate END_DATE1 = LocalDate.of(2021, 6, 12);
  private static final AnnuityDefinition<?> LEG_FIXED1 = new FixedAnnuityDefinitionBuilder().payer(true).currency(USD)
      .notional(NOTIONAL_PROVIDER).startDate(START_DATE1).startDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR)
      .endDate(END_DATE1).endDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodFrequency(P3M)
      .accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).paymentDateAdjustmentParameters(OFFSET_PAYMENT_USDLIBOR)
      .dayCount(DC_30U_360).rate(FIXED_RATE).build();
  private static final AnnuityDefinition<?> LEG_IBOR_3M1 = new FloatingAnnuityDefinitionBuilder().payer(false)
      .notional(NOTIONAL_PROVIDER).startDate(START_DATE1).startDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR)
      .endDate(END_DATE1).endDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).dayCount(USDLIBOR3M.getDayCount())
      .accrualPeriodFrequency(P3M).resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).currency(USD)
      .paymentDateAdjustmentParameters(OFFSET_PAYMENT_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR)
      .fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).compoundingMethod(CompoundingMethod.STRAIGHT)
      .rollDateAdjuster(ROLL_DATE_ADJUSTER).index(USDLIBOR3M).build();
  private static final SwapDefinition IRS_DEFINITION1 = new SwapDefinition(LEG_FIXED1, LEG_IBOR_3M1);
  private static final ZonedDateTimeDoubleTimeSeries TS_USDLIBOR3M1 =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {DateUtils.getUTCDate(2014, 6, 10), DateUtils.getUTCDate(2014, 6, 12) },
          new double[] {0.0023185, 0.0023285 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_ARRAY_USDLIBOR3M_2X1 =
      new ZonedDateTimeDoubleTimeSeries[] {TS_USDLIBOR3M1, TS_USDLIBOR3M1 };

  // Swap Fixed vs Libor3M, USD - fixing between two business days
  private static final LocalDate START_DATE2 = LocalDate.of(2014, 7, 8);
  private static final LocalDate END_DATE2 = LocalDate.of(2021, 7, 8);
  private static final AnnuityDefinition<?> LEG_FIXED2 = new FixedAnnuityDefinitionBuilder().payer(true).currency(USD)
      .notional(NOTIONAL_PROVIDER).startDate(START_DATE2).startDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR)
      .endDate(END_DATE2).endDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodFrequency(P3M)
      .accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).paymentDateAdjustmentParameters(OFFSET_PAYMENT_USDLIBOR)
      .dayCount(DC_30U_360).rate(FIXED_RATE).build();
  private static final AnnuityDefinition<?> LEG_IBOR_3M2 = new FloatingAnnuityDefinitionBuilder().payer(false)
      .notional(NOTIONAL_PROVIDER).startDate(START_DATE2).startDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR)
      .endDate(END_DATE2).endDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).dayCount(USDLIBOR3M.getDayCount())
      .accrualPeriodFrequency(P3M).resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).currency(USD)
      .paymentDateAdjustmentParameters(OFFSET_PAYMENT_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR)
      .fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).compoundingMethod(CompoundingMethod.STRAIGHT)
      .rollDateAdjuster(ROLL_DATE_ADJUSTER).index(USDLIBOR3M).build(); // fixing on 2014/7/3
  private static final SwapDefinition IRS_DEFINITION2 = new SwapDefinition(LEG_FIXED2, LEG_IBOR_3M2);
  private static final ZonedDateTimeDoubleTimeSeries TS_USDLIBOR3M2 =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {DateUtils.getUTCDate(2014, 7, 1), DateUtils.getUTCDate(2014, 7, 3) },
          new double[] {0.0023145, 0.0023245 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_ARRAY_USDLIBOR3M_2X2 =
      new ZonedDateTimeDoubleTimeSeries[] {TS_USDLIBOR3M2, TS_USDLIBOR3M2 };
  private static final ZonedDateTimeDoubleTimeSeries TS_USDLIBOR3M2_PREV =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {DateUtils.getUTCDate(2014, 7, 1) },
          new double[] {0.0023145 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_ARRAY_USDLIBOR3M_2X2_PREV =
      new ZonedDateTimeDoubleTimeSeries[] {TS_USDLIBOR3M2_PREV, TS_USDLIBOR3M2_PREV };

  // Swap Fixed vs Libor3M, USD - Coupon payment between two business days
  private static final LocalDate START_DATE3 = LocalDate.of(2014, 4, 3);
  private static final LocalDate END_DATE3 = LocalDate.of(2021, 4, 3);
  private static final AnnuityDefinition<?> LEG_FIXED3 = new FixedAnnuityDefinitionBuilder().payer(true).currency(USD)
      .notional(NOTIONAL_PROVIDER).startDate(START_DATE3).startDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR)
      .endDate(END_DATE3).endDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).accrualPeriodFrequency(P3M)
      .accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR).paymentDateAdjustmentParameters(OFFSET_PAYMENT_USDLIBOR)
      .dayCount(DC_30U_360).rate(FIXED_RATE).build(); // first coupon payment on 2014/7/3
  private static final AnnuityDefinition<?> LEG_IBOR_3M3 = new FloatingAnnuityDefinitionBuilder().payer(false)
      .notional(NOTIONAL_PROVIDER).startDate(START_DATE3).startDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR)
      .endDate(END_DATE3).endDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).dayCount(USDLIBOR3M.getDayCount())
      .accrualPeriodFrequency(P3M).resetDateAdjustmentParameters(ADJUSTED_DATE_USDLIBOR).currency(USD)
      .paymentDateAdjustmentParameters(OFFSET_PAYMENT_USDLIBOR).accrualPeriodParameters(ADJUSTED_DATE_USDLIBOR)
      .fixingDateAdjustmentParameters(OFFSET_FIXING_USDLIBOR).compoundingMethod(CompoundingMethod.STRAIGHT)
      .rollDateAdjuster(ROLL_DATE_ADJUSTER).index(USDLIBOR3M).build(); // first coupon payment on 2014/7/3
  private static final SwapDefinition IRS_DEFINITION3 = new SwapDefinition(LEG_FIXED3, LEG_IBOR_3M3);
  private static final ZonedDateTimeDoubleTimeSeries TS_USDLIBOR3M3 =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {DateUtils.getUTCDate(2014, 4, 1), DateUtils.getUTCDate(2014, 7, 1) },
          new double[] {0.0023185, 0.0023145 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_ARRAY_USDLIBOR3M_2X3 =
      new ZonedDateTimeDoubleTimeSeries[] {TS_USDLIBOR3M3, TS_USDLIBOR3M3 };

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final double EPS = 1.0e-6;

  /**
   * Yesterday is the previous business day
   */
  @Test
  public void noNonBusinessDayTest() {
    ZonedDateTime tradeDate = TRADE_DATE_US2;
    ZonedDateTime prevDate = ScheduleCalculator.getAdjustedDate(tradeDate, -1, NYC);
    Swap<? extends Payment, ? extends Payment> irsToday = IRS_DEFINITION1.toDerivative(tradeDate,
        TS_ARRAY_USDLIBOR3M_2X1);
    Swap<? extends Payment, ? extends Payment> irsPrev = IRS_DEFINITION1
        .toDerivative(prevDate, TS_ARRAY_USDLIBOR3M_2X1);
    double pvToday = irsToday.accept(PVDC, MULTICURVES_USD2).getAmount(USD);
    double pvPrev = irsPrev.accept(PVDC, MULTICURVES_USD1).getAmount(USD);
    double pai = -pvPrev * FF_RATE1 *
        FEDFUND.getDayCount().getDayCountFraction(prevDate.toLocalDate(), tradeDate.toLocalDate());
    double vm = pvToday - pvPrev + pai;
    double expected = -4193.340074307651;
    assertRelative("IBOR3M vs FIXED, USD", expected, vm, EPS);
  }

  /**
   * 
   */
  @Test
  public void NonBusinessDayTest() {
    ZonedDateTime tradeDate = TRADE_DATE_US3;
    ZonedDateTime prevDate = ScheduleCalculator.getAdjustedDate(tradeDate, -1, NYC);
    Swap<? extends Payment, ? extends Payment> irsToday = IRS_DEFINITION1.toDerivative(tradeDate,
        TS_ARRAY_USDLIBOR3M_2X1);
    Swap<? extends Payment, ? extends Payment> irsPrev = IRS_DEFINITION1
        .toDerivative(prevDate, TS_ARRAY_USDLIBOR3M_2X1);
    double pvToday = irsToday.accept(PVDC, MULTICURVES_USD3).getAmount(USD);
    double pvPrev = irsPrev.accept(PVDC, MULTICURVES_USD2).getAmount(USD);
    double pai = -pvPrev * FF_RATE2 *
        FEDFUND.getDayCount().getDayCountFraction(prevDate.toLocalDate(), tradeDate.toLocalDate());
    double vm = pvToday - pvPrev + pai;
    double expected = -60462.62116159937;
    assertRelative("IBOR3M vs FIXED, USD", expected, vm, EPS);
  }

  /**
   * Trade date is equal to fixing date. index rate is not necessarily available, thus we have two cases.
   */
  @Test
  public void fixingBetweenBusinessDaysTest() {
    ZonedDateTime tradeDate = TRADE_DATE_US2;
    ZonedDateTime prevDate = ScheduleCalculator.getAdjustedDate(tradeDate, -1, NYC);
    Swap<? extends Payment, ? extends Payment> irsFixed = IRS_DEFINITION2.toDerivative(tradeDate,
        TS_ARRAY_USDLIBOR3M_2X2_PREV); // fixed index rate is available
    Swap<? extends Payment, ? extends Payment> irs = IRS_DEFINITION2.toDerivative(tradeDate,
        TS_ARRAY_USDLIBOR3M_2X2); // fixed index rate is not available
    Swap<? extends Payment, ? extends Payment> irsPrev = IRS_DEFINITION2.toDerivative(prevDate,
        TS_ARRAY_USDLIBOR3M_2X2_PREV);
    double pvTodayFixed = irsFixed.accept(PVDC, MULTICURVES_USD2).getAmount(USD);
    double pvToday = irs.accept(PVDC, MULTICURVES_USD2).getAmount(USD);
    double pvPrev = irsPrev.accept(PVDC, MULTICURVES_USD1).getAmount(USD);
    double pai = -pvPrev * FF_RATE1 *
        FEDFUND.getDayCount().getDayCountFraction(prevDate.toLocalDate(), tradeDate.toLocalDate());
    double vmFixed = pvTodayFixed - pvPrev + pai;
    double vm = pvToday - pvPrev + pai;
    double expectedFixed = -6685.816097728809;
    double expected = -6609.126403758203;
    assertRelative("IBOR3M vs FIXED, USD", expectedFixed, vmFixed, EPS);
    assertRelative("IBOR3M vs FIXED, USD", expected, vm, EPS);
  }

  /**
   * Trade date is equal to payment date for both of fixed and libor. 
   * Thus the coupons are included in the previous PV, but not included in the current PV. 
   */
  @Test
  public void paymentBetweenBusinessDaysTest() {
    ZonedDateTime tradeDate = TRADE_DATE_US2;
    ZonedDateTime prevDate = ScheduleCalculator.getAdjustedDate(tradeDate, -1, NYC);
    Swap<? extends Payment, ? extends Payment> irsToday = IRS_DEFINITION3.toDerivative(tradeDate,
        TS_ARRAY_USDLIBOR3M_2X3);
    Swap<? extends Payment, ? extends Payment> irsPrev = IRS_DEFINITION3
        .toDerivative(prevDate, TS_ARRAY_USDLIBOR3M_2X3);
    double pvToday = irsToday.accept(PVDC, MULTICURVES_USD2).getAmount(USD);
    double pvPrev = irsPrev.accept(PVDC, MULTICURVES_USD1).getAmount(USD);
    double pai = -pvPrev * FF_RATE1 *
        FEDFUND.getDayCount().getDayCountFraction(prevDate.toLocalDate(), tradeDate.toLocalDate());
    double vm = pvToday - pvPrev + pai;
    double expected = 8439.829471025056;
    assertRelative("IBOR3M vs FIXED, USD", expected, vm, EPS);
  }

  private static final IndexON SONIA = IndexONMaster.getInstance().getIndex("SONIA");
  private static final IborIndex GBPLIBOR3M = IndexIborMaster.getInstance().getIndex("GBPLIBOR3M");
  private static final Currency GBP = GBPLIBOR3M.getCurrency();

  private static final ZonedDateTime TRADE_DATE_GB2 = DateUtils.getUTCDate(2014, 7, 4); // Fri
  private static final ZonedDateTime TRADE_DATE_GB3 = DateUtils.getUTCDate(2014, 7, 7); // Mon 
  
  private static final MulticurveProviderDiscount MULTICURVES_GBP1 = new MulticurveProviderDiscount();
  private static final MulticurveProviderDiscount MULTICURVES_GBP2 = new MulticurveProviderDiscount();
  private static final MulticurveProviderDiscount MULTICURVES_GBP3 = new MulticurveProviderDiscount();
  static {
    /* 03/July/2014 */
    double[] dscTimes1 = new double[] {0.00273972601, 0.01917808204, 0.08767123299, 0.16986301397, 0.45753424703,
        0.70684931499, 0.95616438404, 1.20547945196, 1.45479452100, 1.70410958899, 1.95342465797, 2.22191780795,
        3.00273972597, 4.00273972600, 5.00273972605, 6.00547945204, 7.01095890398, 8.00821917798, 9.00547945204,
        10.00821918002, 12.00821918002, 15.01095889996, 20.01369863004, 25.01917808001, 30.02465753003, 35.03013699005,
        40.02739725997, 45.03013699002, 50.03561643998 };
    double[] dscRates1 = new double[] {0.00473144502, 0.00475596230, 0.00495181388, 0.00521895494, 0.00607219829,
        0.00702616012, 0.00810397256, 0.00924838595, 0.01040677012, 0.01155878770, 0.01266716068, 0.01381930970,
        0.01767943172, 0.02060042150, 0.02276998256, 0.02446304444, 0.02590979491, 0.02715684187, 0.02822263787,
        0.02914032666, 0.03059693704, 0.03210446585, 0.03340787122, 0.03359798861, 0.03344716348, 0.03309286870,
        0.03276660087, 0.03263646030, 0.03251359497 };
    InterpolatedDoublesCurve interpCurveDsc1 = InterpolatedDoublesCurve.from(dscTimes1, dscRates1, INTERPOLATOR);
    YieldAndDiscountCurve dscCurve1 = new YieldCurve(DSC_CURVE_NAME, interpCurveDsc1);
    double[] snTimes1 = new double[] {0.25205479495, 0.45753424700, 0.70684931504, 0.95616438405, 1.20547945200,
        1.45479452095, 1.70410958901, 1.95342465800, 2.22191780797, 3.00273972604, 4.00273972596, 5.00273972597,
        6.00547945202, 7.01095890401, 8.00821917802, 9.00547945195, 10.00821917996, 12.00821917996, 15.01095890001,
        20.01369862999, 25.01917807997, 30.02465753003, 35.03013698997, 40.02739725996, 45.03013698995, 50.03561644004 };
    double[] snRates1 = new double[] {0.00555215983, 0.00617192730, 0.00709090791, 0.00815201417, 0.00928650259,
        0.01043878609, 0.01158594601, 0.01269001977, 0.01384050981, 0.01677413374, 0.01960188170, 0.02166470361,
        0.02323462039, 0.02456493497, 0.02570983603, 0.02668595980, 0.02755467215, 0.02903102749, 0.03066410234,
        0.03218373741, 0.03252118534, 0.03247204528, 0.03212664110, 0.03194602083, 0.03181035911, 0.03176746157 };
    InterpolatedDoublesCurve interpCurveSn1 = InterpolatedDoublesCurve.from(snTimes1, snRates1, INTERPOLATOR);
    YieldAndDiscountCurve snCurve1 = new YieldCurve(FWD_ON_CURVE_NAME, interpCurveSn1);
    double[] lbrTimes1 = new double[] {0.00273972602, 0.01917808197, 0.03835616400, 0.08767123304, 0.16986301398,
        0.25205479504, 0.33698630105, 0.41917808198, 0.50958904103, 0.58904109597, 0.66575342503, 0.76164383602,
        0.83835616399, 0.91780821899, 0.99999999996, 1.25753424700, 1.50684931502, 1.75616438405, 2.00547945201,
        3.00273972600, 4.00273972599, 5.00273972598, 6.00547945199, 7.01095890404, 8.00821917796, 9.00547945200,
        10.00821917999, 12.00821918003, 15.01095890004, 20.01369862995, 25.01917807996, 30.02465753000, 35.03013698997,
        40.02739726004, 45.03013699004, 50.03561644005 };
    double[] lbrRates1 = new double[] {0.00423016446, 0.00432980491, 0.00432555074, 0.00434361256, 0.00447528471,
        0.00460802855, 0.00473715706, 0.00504138827, 0.00533214318, 0.00555953506, 0.00586662585, 0.00622677502,
        0.00649743493, 0.00681430128, 0.00715159572, 0.00825299439, 0.00931619329, 0.01034101770, 0.01134605567,
        0.01499896659, 0.01768618944, 0.01967317032, 0.02115349125, 0.02240782488, 0.02350055497, 0.02443665476,
        0.02529340073, 0.02677359254, 0.02853161913, 0.03036081996, 0.03095051562, 0.03107249844, 0.03069996363,
        0.03049684726, 0.03034766207, 0.03028989817 };
    InterpolatedDoublesCurve interpCurveLbr1 = InterpolatedDoublesCurve.from(lbrTimes1, lbrRates1, INTERPOLATOR);
    YieldAndDiscountCurve lbrCurve1 = new YieldCurve(FWD_LIBOR_CURVE_NAME, interpCurveLbr1);
    /* 04/July/2014 */
    double[] dscTimes2 = new double[] {0.00273972604, 0.01917808198, 0.08767123302, 0.16986301405, 0.45753424698,
        0.70684931504, 0.95616438402, 1.20547945196, 1.45479452105, 1.70410958900, 1.95342465799, 2.22191780804,
        3.00273972599, 4.00273972596, 5.00273972604, 6.00547945199, 7.01095890398, 8.00821917803, 9.00547945197,
        10.00821917998, 12.00821918002, 15.01095890002, 20.01369863004, 25.01917807998, 30.02465753000, 35.03013699000,
        40.02739725995, 45.03013699004, 50.03561643999 };
    double[] dscRates2 = new double[] {0.00473122542, 0.00475614649, 0.00495193184, 0.00521898346, 0.00607173421,
        0.00702597998, 0.00810397522, 0.00924826383, 0.01040666106, 0.01155862109, 0.01266712378, 0.01381934894,
        0.01767979631, 0.02059958726, 0.02277124291, 0.02446327964, 0.02590938050, 0.02715601275, 0.02822053760,
        0.02913955764, 0.03059738284, 0.03210738376, 0.03340676876, 0.03359557920, 0.03344707072, 0.03309222007,
        0.03276611379, 0.03263643359, 0.03251454897 };
    InterpolatedDoublesCurve interpCurveDsc2 = InterpolatedDoublesCurve.from(dscTimes2, dscRates2, INTERPOLATOR);
    YieldAndDiscountCurve dscCurve2 = new YieldCurve(DSC_CURVE_NAME, interpCurveDsc2);
    double[] snTimes2 = new double[] {0.25205479499, 0.45753424700, 0.70684931504, 0.95616438399, 1.20547945204,
        1.45479452097, 1.70410958902, 1.95342465801, 2.22191780795, 3.00273972595, 4.00273972604, 5.00273972600,
        6.00547945205, 7.01095890400, 8.00821917796, 9.00547945196, 10.00821918003, 12.00821917998, 15.01095890002,
        20.01369862996, 25.01917807995, 30.02465753004, 35.03013698997, 40.02739726003, 45.03013698995, 50.03561644003 };
    double[] snRates2 = new double[] {0.00555219363, 0.00617202331, 0.00709084457, 0.00815144565, 0.00928607117,
        0.01043831823, 0.01158525083, 0.01268981644, 0.01384035402, 0.01677474706, 0.01960187935, 0.02166298997,
        0.02323242347, 0.02456679291, 0.02571192446, 0.02668445609, 0.02755645034, 0.02903126312, 0.03066421241,
        0.03218244308, 0.03252007993, 0.03247140774, 0.03212647331, 0.03194644886, 0.03181227532, 0.03176783131 };
    InterpolatedDoublesCurve interpCurveSn2 = InterpolatedDoublesCurve.from(snTimes2, snRates2, INTERPOLATOR);
    YieldAndDiscountCurve snCurve2 = new YieldCurve(FWD_ON_CURVE_NAME, interpCurveSn2);
    double[] lbrTimes2 = new double[] {0.00273972599, 0.01917808200, 0.03835616396, 0.08767123300, 0.16986301402,
        0.25205479502, 0.33698630101, 0.41917808201, 0.50958904097, 0.58904109598, 0.66575342495, 0.76164383604,
        0.83835616405, 0.91780821905, 0.99999999999, 1.25753424699, 1.50684931496, 1.75616438399, 2.00547945195,
        3.00273972596, 4.00273972599, 5.00273972604, 6.00547945198, 7.01095890398, 8.00821917802, 9.00547945196,
        10.00821917997, 12.00821917996, 15.01095889996, 20.01369863000, 25.01917808001, 30.02465753003, 35.03013699004,
        40.02739725996, 45.03013699001, 50.03561644001 };
    double[] lbrRates2 = new double[] {0.00422983910, 0.00432963746, 0.00432572290, 0.00434376293, 0.00447531635,
        0.00460781066, 0.00473712972, 0.00504139964, 0.00533234651, 0.00555966797, 0.00586667686, 0.00622652565,
        0.00649696965, 0.00681441588, 0.00715193223, 0.00825341495, 0.00931641048, 0.01034147231, 0.01134614537,
        0.01499940290, 0.01768697281, 0.01967358250, 0.02115348123, 0.02240795814, 0.02349898684, 0.02443579969,
        0.02529450641, 0.02677312983, 0.02853314623, 0.03036013432, 0.03095029706, 0.03107135815, 0.03069982581,
        0.03049668874, 0.03034641047, 0.03028932352 };
    InterpolatedDoublesCurve interpCurveLbr2 = InterpolatedDoublesCurve.from(lbrTimes2, lbrRates2, INTERPOLATOR);
    YieldAndDiscountCurve lbrCurve2 = new YieldCurve(FWD_LIBOR_CURVE_NAME, interpCurveLbr2);
    /* 07/July/2014 */
    double[] dscTimes3 = new double[] {0.00273972599, 0.01917808202, 0.08493150703, 0.17260273999, 0.44657534202,
        0.69589041100, 0.94520547900, 1.19452054801, 1.44383561603, 1.69315068498, 1.94246575298, 2.21095890398,
        3.00273972600, 4.00821917803, 5.00547945198, 6.00547945196, 7.00547945204, 8.00547945204, 9.00547945197,
        10.01095889995, 12.00821918005, 15.01643835996, 20.01369862999, 25.01643835998, 30.02191780996, 35.02465753000,
        40.02739725997, 45.03013699000, 50.03561644005 };
    double[] dscRates3 = new double[] {0.00473122282, 0.00478722463, 0.00496794335, 0.00523125338, 0.00602097585,
        0.00700831975, 0.00807707690, 0.00921703704, 0.01037214714, 0.01150729712, 0.01261584235, 0.01375536926,
        0.01763158496, 0.02050687511, 0.02263753056, 0.02431759339, 0.02573248173, 0.02693736881, 0.02796503276,
        0.02887418936, 0.03033222015, 0.03188478966, 0.03323933585, 0.03345136183, 0.03333528461, 0.03298293234,
        0.03265438569, 0.03253335296, 0.03242167781 };
    InterpolatedDoublesCurve interpCurveDsc3 = InterpolatedDoublesCurve.from(dscTimes3, dscRates3, INTERPOLATOR);
    YieldAndDiscountCurve dscCurve3 = new YieldCurve(DSC_CURVE_NAME, interpCurveDsc3);
    double[] snTimes3 = new double[] {0.25205479504, 0.44657534202, 0.69589041098, 0.94520547900, 1.19452054796,
        1.44383561600, 1.69315068501, 1.94246575297, 2.21095890400, 3.00273972601, 4.00821917798, 5.00547945197,
        6.00547945198, 7.00547945200, 8.00547945204, 9.00547945202, 10.01095889995, 12.00821918004, 15.01643836002,
        20.01369863003, 25.01643836003, 30.02191781004, 35.02465753004, 40.02739725999, 45.03013699001, 50.03561644003 };
    double[] snRates3 = new double[] {0.00555908300, 0.00613417475, 0.00708129283, 0.00813078886, 0.00925940527,
        0.01040801723, 0.01153753714, 0.01264243746, 0.01377880333, 0.01674384093, 0.01952287185, 0.02153178909,
        0.02308793288, 0.02438815828, 0.02549240308, 0.02643124591, 0.02729206024, 0.02876488579, 0.03044014453,
        0.03201346872, 0.03237520038, 0.03235890373, 0.03201344113, 0.03183184287, 0.03170548445, 0.03167042841 };
    InterpolatedDoublesCurve interpCurveSn3 = InterpolatedDoublesCurve.from(snTimes3, snRates3, INTERPOLATOR);
    YieldAndDiscountCurve snCurve3 = new YieldCurve(FWD_ON_CURVE_NAME, interpCurveSn3);
    double[] lbrTimes3 = new double[] {0.00273972598, 0.01917808202, 0.03835616398, 0.08493150702, 0.17260274002,
        0.25205479499, 0.33698630097, 0.42191780799, 0.50410958904, 0.59452054797, 0.67123287696, 0.75068493205,
        0.83287671200, 0.92054794498, 1.00000000003, 1.25205479501, 1.50410958905, 1.75342465799, 2.00273972600,
        3.00273972603, 4.00821917804, 5.00547945199, 6.00547945197, 7.00547945199, 8.00547945197, 9.00547945195,
        10.01095889999, 12.00821918002, 15.01643836000, 20.01369863004, 25.01643835998, 30.02191781002, 35.02465752995,
        40.02739725995, 45.03013698998, 50.03561644004 };
    double[] lbrRates3 = new double[] {0.00430077739, 0.00431541024, 0.00431033912, 0.00431300593, 0.00446214308,
        0.00458221360, 0.00471407128, 0.00503618716, 0.00529095864, 0.00557347987, 0.00587805508, 0.00617489988,
        0.00646725595, 0.00682091058, 0.00713767796, 0.00822076985, 0.00929236209, 0.01032334732, 0.01132803502,
        0.01496462457, 0.01761983477, 0.01952779017, 0.02100811465, 0.02222949722, 0.02328020687, 0.02418138260,
        0.02503067033, 0.02650887884, 0.02830942881, 0.03018725403, 0.03080342754, 0.03096021181, 0.03058351639,
        0.03038200499, 0.03024016468, 0.03019184155 };
    InterpolatedDoublesCurve interpCurveLbr3 = InterpolatedDoublesCurve
        .from(lbrTimes3, lbrRates3, INTERPOLATOR);
    YieldAndDiscountCurve lbrCurve3 = new YieldCurve(FWD_LIBOR_CURVE_NAME, interpCurveLbr3);
    MULTICURVES_GBP1.setCurve(GBP, dscCurve1);
    MULTICURVES_GBP1.setCurve(SONIA, snCurve1);
    MULTICURVES_GBP1.setCurve(GBPLIBOR3M, lbrCurve1);
    MULTICURVES_GBP2.setCurve(GBP, dscCurve2);
    MULTICURVES_GBP2.setCurve(SONIA, snCurve2);
    MULTICURVES_GBP2.setCurve(GBPLIBOR3M, lbrCurve2);
    MULTICURVES_GBP3.setCurve(GBP, dscCurve3);
    MULTICURVES_GBP3.setCurve(SONIA, snCurve3);
    MULTICURVES_GBP3.setCurve(GBPLIBOR3M, lbrCurve3);
  }

  private static final double SN_RATE1 = 0.0042;
  private static final double SN_RATE2 = 0.0042;

  private static final Calendar LON = new CalendarGBP("LON");
  private static final AdjustedDateParameters ADJUSTED_DATE_GBPLIBOR = new AdjustedDateParameters(LON,
      GBPLIBOR3M.getBusinessDayConvention());
  private static final OffsetAdjustedDateParameters OFFSET_FIXING_GBPLIBOR =
      new OffsetAdjustedDateParameters(-2, OffsetType.BUSINESS, LON, GBPLIBOR3M.getBusinessDayConvention());
  private static final OffsetAdjustedDateParameters OFFSET_PAYMENT_GBPLIBOR =
      new OffsetAdjustedDateParameters(0, OffsetType.BUSINESS, LON, GBPLIBOR3M.getBusinessDayConvention());

  private static final LocalDate START_DATE4 = LocalDate.of(2014, 6, 12);
  private static final LocalDate END_DATE4 = LocalDate.of(2021, 6, 12);
  private static final AnnuityDefinition<?> LEG_FIXED4 = new FixedAnnuityDefinitionBuilder().payer(true).currency(GBP)
      .notional(NOTIONAL_PROVIDER).startDate(START_DATE4).startDateAdjustmentParameters(ADJUSTED_DATE_GBPLIBOR)
      .endDate(END_DATE4).endDateAdjustmentParameters(ADJUSTED_DATE_GBPLIBOR).accrualPeriodFrequency(P3M)
      .accrualPeriodParameters(ADJUSTED_DATE_GBPLIBOR).paymentDateAdjustmentParameters(OFFSET_PAYMENT_GBPLIBOR)
      .dayCount(DC_30U_360).rate(FIXED_RATE).build();
  private static final AnnuityDefinition<?> LEG_IBOR_3M4 = new FloatingAnnuityDefinitionBuilder().payer(false)
      .notional(NOTIONAL_PROVIDER).startDate(START_DATE4).startDateAdjustmentParameters(ADJUSTED_DATE_GBPLIBOR)
      .endDate(END_DATE4).endDateAdjustmentParameters(ADJUSTED_DATE_GBPLIBOR).dayCount(USDLIBOR3M.getDayCount())
      .accrualPeriodFrequency(P3M).resetDateAdjustmentParameters(ADJUSTED_DATE_GBPLIBOR).currency(GBP)
      .paymentDateAdjustmentParameters(OFFSET_PAYMENT_GBPLIBOR).accrualPeriodParameters(ADJUSTED_DATE_GBPLIBOR)
      .fixingDateAdjustmentParameters(OFFSET_FIXING_GBPLIBOR).compoundingMethod(CompoundingMethod.STRAIGHT)
      .rollDateAdjuster(ROLL_DATE_ADJUSTER).index(GBPLIBOR3M).build();
  private static final SwapDefinition IRS_DEFINITION4 = new SwapDefinition(LEG_FIXED4, LEG_IBOR_3M4);
  private static final ZonedDateTimeDoubleTimeSeries TS_USDLIBOR3M4 =
      ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(
          new ZonedDateTime[] {DateUtils.getUTCDate(2014, 6, 10), DateUtils.getUTCDate(2014, 6, 12) },
          new double[] {0.0045185, 0.0045285 });
  private static final ZonedDateTimeDoubleTimeSeries[] TS_ARRAY_USDLIBOR3M_2X4 =
      new ZonedDateTimeDoubleTimeSeries[] {TS_USDLIBOR3M4, TS_USDLIBOR3M4 };

  /**
   * trade date and previous business day are two consecutive days
   */
  @Test
  public void gbpTest1() {
    ZonedDateTime tradeDate = TRADE_DATE_GB2;
    ZonedDateTime prevDate = ScheduleCalculator.getAdjustedDate(tradeDate, -1, LON);
    Swap<? extends Payment, ? extends Payment> irsToday = IRS_DEFINITION4.toDerivative(tradeDate,
        TS_ARRAY_USDLIBOR3M_2X4);
    Swap<? extends Payment, ? extends Payment> irsPrev = IRS_DEFINITION4.toDerivative(prevDate,
        TS_ARRAY_USDLIBOR3M_2X4);
    double pvToday = irsToday.accept(PVDC, MULTICURVES_GBP2).getAmount(GBP);
    double pvPrev = irsPrev.accept(PVDC, MULTICURVES_GBP1).getAmount(GBP);
    double pai = -pvPrev * SN_RATE1 *
        SONIA.getDayCount().getDayCountFraction(prevDate.toLocalDate(), tradeDate.toLocalDate());
    double vm = pvToday - pvPrev + pai;
    double expected = -6279.180878941443;
    assertRelative("IBOR3M vs FIXED, GBP", expected, vm, EPS);
  }

  /**
   * Weekend between the two business days
   */
  @Test
  public void gbpTest() {
    ZonedDateTime tradeDate = TRADE_DATE_GB3;
    ZonedDateTime prevDate = ScheduleCalculator.getAdjustedDate(tradeDate, -1, LON);
    Swap<? extends Payment, ? extends Payment> irsToday = IRS_DEFINITION4.toDerivative(tradeDate,
        TS_ARRAY_USDLIBOR3M_2X4);
    Swap<? extends Payment, ? extends Payment> irsPrev = IRS_DEFINITION4.toDerivative(prevDate,
        TS_ARRAY_USDLIBOR3M_2X4);
    double pvToday = irsToday.accept(PVDC, MULTICURVES_GBP3).getAmount(GBP);
    double pvPrev = irsPrev.accept(PVDC, MULTICURVES_GBP2).getAmount(GBP);
    double pai = -pvPrev * SN_RATE2 *
        SONIA.getDayCount().getDayCountFraction(prevDate.toLocalDate(), tradeDate.toLocalDate());
    double vm = pvToday - pvPrev + pai;
    double expected = -121763.86446903751;
    assertRelative("IBOR3M vs FIXED, GBP", expected, vm, EPS);
  }

  private void assertRelative(String message, double expected, double obtained, double relTol) {
    double ref = Math.max(Math.abs(expected), 1.0);
    assertEquals(message, expected, obtained, ref * relTol);
  }
}
