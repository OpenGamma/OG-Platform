/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDAInstrumentTypes;
import com.opengamma.analytics.financial.model.BumpType;
import com.opengamma.analytics.financial.schedule.NoHolidayCalendar;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ParellelCS01Test {
  private static final SpreadSensitivityCalculator CS01_CAL = new SpreadSensitivityCalculator();

  private static final Calendar DEFAULT_CALENDAR = new MondayToFridayCalendar("Weekend_Only");
  // private static final Calendar DEFAULT_CALENDAR = new NoHolidayCalendar();
  private static final DayCount ACT365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final DayCount ACT360 = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  private static final DayCount D30360 = DayCountFactory.INSTANCE.getDayCount("30/360");

  private static final BusinessDayConvention FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final BusinessDayConvention MOD_FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");

  private static final LocalDate TODAY = LocalDate.of(2013, 6, 4);
  private static final LocalDate EFFECTIVE_DATE = TODAY.plusDays(1); // AKA stepin date
  private static final LocalDate CASH_SETTLE_DATE = addWorkDays(TODAY, 3, DEFAULT_CALENDAR); // AKA valuation date
  private static final LocalDate STARTDATE = LocalDate.of(2013, 2, 2);
  private static final LocalDate[] MATURITIES = new LocalDate[] {LocalDate.of(2013, 6, 20), LocalDate.of(2013, 9, 20), LocalDate.of(2013, 12, 20), LocalDate.of(2014, 3, 20),
      LocalDate.of(2014, 6, 20), LocalDate.of(2014, 9, 20), LocalDate.of(2014, 12, 20), LocalDate.of(2015, 3, 20), LocalDate.of(2015, 6, 20), LocalDate.of(2015, 9, 20), LocalDate.of(2015, 12, 20),
      LocalDate.of(2016, 3, 20), LocalDate.of(2016, 6, 20), LocalDate.of(2016, 9, 20), LocalDate.of(2016, 12, 20), LocalDate.of(2017, 3, 20), LocalDate.of(2017, 6, 20), LocalDate.of(2017, 9, 20),
      LocalDate.of(2017, 12, 20), LocalDate.of(2018, 3, 20), LocalDate.of(2018, 6, 20), LocalDate.of(2018, 9, 20), LocalDate.of(2018, 12, 20), LocalDate.of(2019, 3, 20), LocalDate.of(2019, 6, 20),
      LocalDate.of(2019, 9, 20), LocalDate.of(2019, 12, 20), LocalDate.of(2020, 3, 20), LocalDate.of(2020, 6, 20), LocalDate.of(2020, 9, 20), LocalDate.of(2020, 12, 20), LocalDate.of(2021, 3, 20),
      LocalDate.of(2021, 6, 20), LocalDate.of(2021, 9, 20), LocalDate.of(2021, 12, 20), LocalDate.of(2022, 3, 20), LocalDate.of(2022, 6, 20), LocalDate.of(2022, 9, 20), LocalDate.of(2022, 12, 20),
      LocalDate.of(2023, 3, 20), LocalDate.of(2023, 6, 20)};
  private static final double[] FLAT_SPREADS = new double[] {8.97, 9.77, 10.7, 11.96, 13.17, 15.59, 17.8, 19.66, 21.35, 23.91, 26.54, 28.56, 30.63, 32.41, 34.08, 35.33, 36.74, 38.9, 40.88, 42.71,
      44.49, 46.92, 49.2, 51.36, 53.5, 55.58, 57.59, 59.49, 61.4, 62.76, 64.11, 65.35, 66.55, 67.58, 68.81, 69.81, 70.79, 71.65, 72.58, 73.58, 74.2};

  // These numbers come from The ISDA excel plugin
  private static final double[] PARELLEL_CS01 = new double[] {4.44275669542324, 30.0292310963296, 55.3868828464654, 80.4711784871091, 106.115178267918, 131.768346646544, 157.154795933765,
      182.274611106283, 207.957754964565, 233.488139351017, 258.616674295747, 283.692918485109, 308.9181603095, 334.009883131127, 358.675097865475, 382.992574496952, 407.651615499056,
      431.854306207759, 455.571341165587, 478.807345690023, 502.320875404118, 525.221886681367, 547.56958798809, 569.366146839778, 591.319055971737, 612.907283712702, 633.900434684163,
      654.581372006954, 675.104344433526, 695.667761357141, 715.649374946677, 735.150221910334, 754.800017771881, 774.265323151377, 792.955421728157, 811.335431749015, 829.837914594809,
      848.145638350457, 865.827578890886, 882.881604614791, 900.543748901181};

  private static final double[][] BUCKETED_CS01 = new double[][] {
      {4.44275669542324, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
      {30.0292310963296, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
      {55.3868828464654, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
      {28.2168461525276, 52.2591747114526, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
      {0.419986404378084, 105.700057807618, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
      {0.40710101592567, 79.4106031728071, 51.965757893048, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
      {0.39524299172769, 53.3359988692059, 103.444251990699, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
      {0.385103854857322, 27.4962192421332, 154.415044508374, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
      {0.375772666229984, 1.03952603476565, 206.560705835027, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
      {0.362195456835024, 1.0029496921235, 155.167320932584, 77.0027594144858, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
      {0.348308455723478, 0.965438932753332, 104.344793660723, 153.020398416158, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
      {0.337419317535115, 0.936140150557818, 53.6022607828049, 228.881332393738, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
      {0.326300478085267, 0.906159887771596, 2.37638560744366, 305.362165353675, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
      {0.316619326437562, 0.880103549417283, 2.30697476944972, 228.800105351902, 101.811912697064, 0, 0, 0, 0, 0, 0, 0, 0, 0},
      {0.30751147132857, 0.855579898796643, 2.24164079537861, 153.309056168012, 202.092704070483, 0, 0, 0, 0, 0, 0, 0, 0, 0},
      {0.300471730815821, 0.836741745016689, 2.19120535705636, 78.933117539641, 300.861694691366, 0, 0, 0, 0, 0, 0, 0, 0, 0},
      {0.292663938715465, 0.815751593826602, 2.13518302589685, 3.17521579367916, 401.33617326029, 0, 0, 0, 0, 0, 0, 0, 0, 0},
      {0.281289593565687, 0.784807105101704, 2.05328307813918, 3.04799330208549, 300.180769637337, 125.693189995439, 0, 0, 0, 0, 0, 0, 0, 0},
      {0.270844686487554, 0.756382908525394, 1.97803401839197, 2.93107551149058, 200.705603209946, 249.154127416648, 0, 0, 0, 0, 0, 0, 0, 0},
      {0.261180249577081, 0.730072868309478, 1.90837060650567, 2.82283902493935, 102.925801921998, 370.377571447027, 0, 0, 0, 0, 0, 0, 0, 0},
      {0.251787019359129, 0.704482806122148, 1.84062138396113, 2.71766640087129, 3.62286318756483, 493.351427038571, 0, 0, 0, 0, 0, 0, 0, 0},
      {0.239386707651112, 0.670449115634797, 1.75096484996903, 2.58070903820171, 3.43500112475609, 368.365451232078, 148.469048295494, 0, 0, 0, 0, 0, 0, 0},
      {0.227791715504133, 0.638593796911668, 1.66706396278679, 2.4527102489777, 3.25960725556032, 245.746001006103, 293.918510426105, 0, 0, 0, 0, 0, 0, 0},
      {0.216854533340721, 0.608512239486103, 1.58785976536041, 2.33207196656537, 3.09451337750233, 125.505620909457, 436.346668532012, 0, 0, 0, 0, 0, 0, 0},
      {0.206079508669604, 0.578838076897153, 1.50976478922871, 2.21337127900498, 2.93234639231477, 3.69871733817834, 580.424395217643, 0, 0, 0, 0, 0, 0, 0},
      {0.195672510666656, 0.550138315819071, 1.43427360704135, 2.09888676754033, 2.7762277134899, 3.49708608032157, 433.176400358885, 169.588706987196, 0, 0, 0, 0, 0, 0},
      {0.185686052922573, 0.522558909851756, 1.36177161932233, 1.98920322624918, 2.62695605411645, 3.30460384377618, 288.981421040571, 335.40592130283, 0, 0, 0, 0, 0, 0},
      {0.17629007625225, 0.496586120030118, 1.29350935523798, 1.88606917724682, 2.48674697526929, 3.12395521570585, 146.293928095009, 499.275645178793, 0, 0, 0, 0, 0, 0},
      {0.166929044975728, 0.470666465919589, 1.22544067716196, 1.78354477896325, 2.34772585669396, 2.9452078520939, 3.59479635291676, 662.901188698849, 0, 0, 0, 0, 0, 0},
      {0.160124679071671, 0.451895553706116, 1.17599484551062, 1.7083721335423, 2.24498154734187, 2.81223483850762, 3.42852916767472, 493.945168638647, 190.290079229972, 0, 0, 0, 0, 0},
      {0.153433158081584, 0.433403804400712, 1.12733098057982, 1.63464240063305, 2.14449714009551, 2.682484712975, 3.26657231688315, 328.626408782007, 376.215656638007, 0, 0, 0, 0, 0},
      {0.14729718848111, 0.416441417454283, 1.08269009983786, 1.56702404038173, 2.05235736740889, 2.56352147722655, 3.1180842657505, 166.945071880191, 557.852222912963, 0, 0, 0, 0, 0},
      {0.141379918855433, 0.400072916136029, 1.0396207593133, 1.50185031796285, 1.96361982831433, 2.44902188208107, 2.9752299902791, 3.54678616468901, 741.209707486222, 0, 0, 0, 0, 0},
      {0.136262781759433, 0.385935441007956, 1.00237882200843, 1.44530399388137, 1.88640706079435, 2.3491548484314, 2.85039555217997, 3.39519167089075, 551.990589717222, 209.529602470913, 0, 0, 0, 0},
      {0.130327676241271, 0.369455056922763, 0.959112100150372, 1.38034493969014, 1.79854773060123, 2.23640057597785, 2.71030112871523, 3.22574059441472, 366.903034046337, 414.053506416767, 0, 0, 0,
          0},
      {0.125451561479306, 0.355939941257266, 0.923574619872414, 1.32673634858221, 1.72574351351568, 2.14264817649523, 2.59349978835172, 3.0841973819376, 186.077411391948, 613.733480971913, 0, 0, 0, 0},
      {0.120695531610759, 0.342747066436888, 0.888896827686003, 1.27449962238069, 1.6548871965838, 2.05149137262606, 2.48001273323722, 2.94672767932325, 3.44985047621288, 815.158828780443, 0, 0, 0, 0},
      {0.116498619286143, 0.331116062159131, 0.858297479408576, 1.22828721135781, 1.59206314555987, 1.97051788657782, 2.37905387087678, 2.82430573157744, 3.30482847203661, 606.753913155463,
          227.664412485055, 0, 0, 0},
      {0.112048626585426, 0.31874285805894, 0.82581626904138, 1.17958978262944, 1.52627109708398, 1.88615123395547, 2.27428062105073, 2.69758834506306, 3.15495004793442, 403.079420980251,
          449.778345152398, 0, 0, 0},
      {0.107350377517856, 0.305639610301278, 0.791486117410201, 1.12846426840452, 1.45759648435828, 1.79850963977213, 2.16584766038475, 2.56676785306831, 3.00044455388282, 204.100161117243,
          666.386020341983, 0, 0, 0},
      {0.104286288313271, 0.297165789434062, 0.7691460192083, 1.09452285262934, 1.41121769341168, 1.73847695020268, 2.09074267459863, 2.47547555579097, 2.8921185821762, 3.34477564953306,
          884.9662862076, 0, 0, 0}};

  private static final double DEAL_SPREAD = 100.0;
  private static final boolean PAY_ACC_ON_DEFAULT = true;
  private static final Period TENOR = Period.ofMonths(3);
  private static final StubType STUB = StubType.FRONTSHORT;
  private static final boolean PROCTECTION_START = true;

  private static final LocalDate[] PAR_SPREAD_DATES = new LocalDate[] {LocalDate.of(2013, 12, 20), LocalDate.of(2014, 6, 20), LocalDate.of(2015, 6, 20), LocalDate.of(2016, 6, 20),
      LocalDate.of(2017, 6, 20), LocalDate.of(2018, 6, 20), LocalDate.of(2019, 6, 20), LocalDate.of(2020, 6, 20), LocalDate.of(2021, 6, 20), LocalDate.of(2022, 6, 20), LocalDate.of(2023, 6, 20),
      LocalDate.of(2028, 6, 20), LocalDate.of(2033, 6, 20), LocalDate.of(2043, 6, 20)};

  private static final double RECOVERY_RATE = 0.4;
  private static final double NOTIONAL = 1e6;

  // yield curve
  private static final LocalDate SPOT_DATE = LocalDate.of(2013, 6, 6);
  private static final ISDACompliantYieldCurveBuild YIELD_CURVE_BUILDER = new ISDACompliantYieldCurveBuild();
  private static final ISDACompliantYieldCurve YIELD_CURVE;

  static {
    final int nMoneyMarket = 5;
    final int nSwaps = 14;
    final int nInstruments = nMoneyMarket + nSwaps;

    final ISDAInstrumentTypes[] types = new ISDAInstrumentTypes[nInstruments];
    Period[] tenors = new Period[nInstruments];
    final int[] mmMonths = new int[] {1, 2, 3, 6, 12};
    final int[] swapYears = new int[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 20, 25, 30};
    // check
    ArgumentChecker.isTrue(mmMonths.length == nMoneyMarket, "mmMonths");
    ArgumentChecker.isTrue(swapYears.length == nSwaps, "swapYears");

    for (int i = 0; i < nMoneyMarket; i++) {
      types[i] = ISDAInstrumentTypes.MoneyMarket;
      tenors[i] = Period.ofMonths(mmMonths[i]);
    }
    for (int i = nMoneyMarket; i < nInstruments; i++) {
      types[i] = ISDAInstrumentTypes.Swap;
      tenors[i] = Period.ofYears(swapYears[i - nMoneyMarket]);
    }

    final double[] rates = new double[] {0.00194, 0.002292, 0.002733, 0.004153, 0.006902, 0.004575, 0.006585, 0.00929, 0.012175, 0.0149, 0.01745, 0.019595, 0.02144, 0.023045, 0.02567, 0.02825,
        0.03041, 0.031425, 0.03202};

    final DayCount moneyMarketDCC = ACT360;
    final DayCount swapDCC = D30360;
    final DayCount curveDCC = ACT365;
    final Period swapInterval = Period.ofMonths(6);

    YIELD_CURVE = YIELD_CURVE_BUILDER.build(TODAY, SPOT_DATE, types, tenors, rates, moneyMarketDCC, swapDCC, swapInterval, curveDCC, MOD_FOLLOWING);

    // YIELD_CURVE = new ISDACompliantYieldCurve(1.0, 0.05);
  }

  @Test
  public void parellelCS01test() {
    final double bumpAmount = 1e-4; // 1pb

    final int m = PAR_SPREAD_DATES.length;
    final CDSAnalytic[] curveCDSs = new CDSAnalytic[m];
    for (int i = 0; i < m; i++) {
      curveCDSs[i] = new CDSAnalytic(TODAY, EFFECTIVE_DATE, CASH_SETTLE_DATE, TODAY, PAR_SPREAD_DATES[i], PAY_ACC_ON_DEFAULT, TENOR, STUB, PROCTECTION_START, RECOVERY_RATE);
    }

    final double fracSpread = DEAL_SPREAD / 10000;
    final double scale = NOTIONAL / 10000;

    final int n = MATURITIES.length;
    for (int i = 0; i < n; i++) {
      final CDSAnalytic cds = new CDSAnalytic(TODAY, EFFECTIVE_DATE, CASH_SETTLE_DATE, STARTDATE, MATURITIES[i], PAY_ACC_ON_DEFAULT, TENOR, STUB, PROCTECTION_START, RECOVERY_RATE);
      final double[] flatSpreads = new double[m];
      Arrays.fill(flatSpreads, FLAT_SPREADS[i] / 10000);
      double cs01 = scale * CS01_CAL.parallelCreditDV01(cds, fracSpread, PriceType.DIRTY, YIELD_CURVE, curveCDSs, flatSpreads, bumpAmount, BumpType.ADDITIVE);
      // System.out.println(MATURITIES[i].toString() + "\t" + cs01);
      assertEquals(MATURITIES[i].toString(), PARELLEL_CS01[i], cs01, 1e-14 * NOTIONAL);
    }
  }

  @Test
  public void bucketedCS01test() {
    final double bumpAmount = 1e-4; // 1pb

    final int m = PAR_SPREAD_DATES.length;
    final CDSAnalytic[] curveCDSs = new CDSAnalytic[m];
    for (int i = 0; i < m; i++) {
      curveCDSs[i] = new CDSAnalytic(TODAY, EFFECTIVE_DATE, CASH_SETTLE_DATE, TODAY, PAR_SPREAD_DATES[i], PAY_ACC_ON_DEFAULT, TENOR, STUB, PROCTECTION_START, RECOVERY_RATE);
    }

    final double fracSpread = DEAL_SPREAD / 10000;
    final double scale = NOTIONAL / 10000;

    final int n = MATURITIES.length;
    for (int i = 0; i < n; i++) {
      final CDSAnalytic cds = new CDSAnalytic(TODAY, EFFECTIVE_DATE, CASH_SETTLE_DATE, STARTDATE, MATURITIES[i], PAY_ACC_ON_DEFAULT, TENOR, STUB, PROCTECTION_START, RECOVERY_RATE);
      final double[] flatSpreads = new double[m];
      Arrays.fill(flatSpreads, FLAT_SPREADS[i] / 10000);
      double[] cs01 = CS01_CAL.bucketedCreditDV01(cds, fracSpread, PriceType.DIRTY, YIELD_CURVE, curveCDSs, flatSpreads, bumpAmount, BumpType.ADDITIVE);

      for (int j = 0; j < m; j++) {
        cs01[j] *= scale;
        assertEquals(MATURITIES[i].toString() + "\t" + PAR_SPREAD_DATES[j], BUCKETED_CS01[i][j], cs01[j], 1e-13 * NOTIONAL);
      }

    }
  }
}
