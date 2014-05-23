/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.options;

import static com.opengamma.analytics.financial.credit.options.YieldCurveProvider.ISDA_USD_20140213;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.fastcalibration.CreditCurveCalibrator;

/**
 * 
 */
public class CDSIndexPrvider {

  public static final Period[] INDEX_TENORS = new Period[] {Period.ofYears(3), Period.ofYears(5), Period.ofYears(7), Period.ofYears(10) };
  public static final Period[] CDS_TENORS = new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(7),
    Period.ofYears(10) };

  //CDS.NA.HY.21 
  public final static double CDX_NA_HY_21_RECOVERY_RATE = 0.3;
  public final static double CDX_NA_HY_21_COUPON = 0.05;
  public final static String[] CDX_NA_HY_21_NAMES = new String[] {"The AES Corp", "INTL LEASE FIN CORP", "AK Stl Corp", "Alcatel Lucent USA Inc", "Ally Finl Inc", "Advanced Micro Devices Inc",
    "Amkor Tech Inc", "ARAMARK Corp", "Avis Budget Group Inc", "Amern Axle & Mfg Inc", "Brunswick Corp", "BOMBARDIER INC.", "Boyd Gaming Corp", "Clear Channel Comms Inc", "Caesars Entmt Oper Co Inc",
    "Chesapeake Engy Corp", "CCO Hldgs LLC", "CIT Gp Inc", "CenturyLink Inc", "Cooper Tire & Rubr Co", "CSC Hldgs LLC", "Cmnty Health Sys Inc", "Dillards Inc", "Dell Inc", "D R Horton Inc",
    "DISH DBS Corp", "DELUXE CORP", "Dean Foods Co", "R R Donnelley & Sons Co", "HERTZ CORP", "1st Data Corp", "Frontier Comms Corp", "Freescale Semiconductor Inc", "Fst Oil Corp",
    "Gannett Co Inc DE", "Goodyear Tire & Rubr Co", "HCA Inc.", "Health Mgmt Assoc Inc", "H J HEINZ CO", "Host Hotels & Resorts LP", "K Hovnanian Entpers Inc", "Iron Mtn Inc.", "J C Penney Co Inc",
    "Jones Group Inc", "KB HOME", "Kinder Morgan Inc.", "L Brands Inc", "Lennar Corp", "Levi Strauss & Co", "Liberty Interactive LLC", "LA Pac Corp", "Level 3 Comms Inc", "MBIA Ins Corp",
    "McClatchy Co", "Meritor Inc", "MGIC Invt Corp", "MGM Resorts Intl", "Norbord Inc", "NOVA Chems Corp", "New Albertsons Inc", "Neiman Marcus Gp Inc", "New York Times Co", "Owens IL Inc",
    "Olin Corp", "Pactiv LLC", "PHH Corp", "PulteGroup Inc", "Parker Drilling Co", "Polyone Corp", "Rite Aid Corp", "Royal Caribbean Cruises Ltd", "Radian Gp Inc", "RadioShack Corp", "Ryland Gp Inc",
    "Sanmina Corp", "Seagate Tech HDD Hldgs", "Sealed Air Corp US", "Smithfield Foods Inc", "Istar Finl Inc", "SUNGARD DATA Sys INC", "SEARS ROEBUCK Accep CORP", "Std Pac Corp",
    "Springleaf Fin Corp", "Sprint Comms Inc", "SUPERVALU INC", "Tenet Healthcare Corp", "TOYS R US INC", "Sabre Hldgs Corp", "Tesoro Corp", "TX Competitive Elec Hldgs Co LLC",
    "Unvl Health Svcs Inc", "Unisys Corp", "UTD RENTS NORTH AMERICA INC", "Vulcan Matls Co", "Windstream Corp", "Utd Sts Stl Corp", "NRG Energy Inc" };
  //Market data on 13-Feb-2014  
  public final static double[] CDX_NA_HY_20140213_PRICES = new double[] {1.0756, 1.0762, 1.0571, 1.0652 };
  public final static double[] CDX_NA_HY_20140213_RECOVERY_RATES = new double[] {0.4, 0.4942, 0.4, 0.3929, 0.4922, 0.3976, 0.395, 0.4, 0.3525, 0.37, 0.3929, 0.4, 0.3283, 0.1521, 0.179, 0.4268, 0.4,
    0.425, 0.4, 0.3513, 0.4, 0.4, 0.3625, 0.4, 0.4, 0.4, 0.4, 0.4, 0.4, 0.3973, 0.3, 0.4, 0.314, 0.4, 0.4004, 0.4, 0.4, 0.4, 0.4, 0.3983, 0.2075, 0.2, 0.3733, 0.3944, 0.3983, 0.4, 0.4, 0.4, 0.3929,
    0.3975, 0.4055, 0.2971, 0.4464, 0.2483, 0.3725, 0.426, 0.374, 0.4, 0.3983, 0.3271, 0.38, 0.4, 0.4, 0.4, 0.4, 0.33, 0.4, 0.4, 0.4, 0.3336, 0.3843, 0.3881, 0.2767, 0.4, 0.2157, 0.4, 0.4, 0.4,
    0.5162, 0.4, 0.4054, 0.3857, 0.5063, 0.4, 0.3857, 0.4, 0.3586, 0.3257, 0.399, 0.06, 0.4, 0.2717, 0.3964, 0.4, 0.4, 0.3914, 0.4 };
  public final static double[][] CDX_NA_HY_20140213_PAR_SPREADS = new double[][] { {0.0019, 0.0028, 0.0054, 0.0089, 0.0138, 0.0187, 0.0238, 0.0262 },
    {0.0038, 0.0051, 0.0094, 0.0141, 0.0185, 0.0226, 0.027, 0.0287 }, {0.0089, 0.0147, 0.0354, 0.0514, 0.0586, 0.065, 0.0692, 0.0709 },
    {0.0038, 0.0062, 0.0108, 0.0187, 0.027, 0.0344, 0.0421, 0.0449 }, {0.0041, 0.0058, 0.0083, 0.0103, 0.0124, 0.0149, 0.0186, 0.0211 },
    {0.0075, 0.009, 0.0205, 0.0307, 0.0403, 0.049, 0.0569, 0.0587 }, {0.0039, 0.0056, 0.0096, 0.0172, 0.0266, 0.0345, 0.0419, 0.0438 },
    {0.0021, 0.0025, 0.0048, 0.0081, 0.012, 0.0163, 0.0228, 0.0245 }, {0.0024, 0.0032, 0.0062, 0.0114, 0.0174, 0.0235, 0.031, 0.0331 },
    {0.0028, 0.0038, 0.0077, 0.0135, 0.0214, 0.0287, 0.0387, 0.041 }, {0.0011, 0.0014, 0.0027, 0.0046, 0.0079, 0.0108, 0.0163, 0.0184 },
    {0.0029, 0.004, 0.0081, 0.0158, 0.0234, 0.0319, 0.0382, 0.0412 }, {0.0029, 0.0037, 0.0118, 0.0241, 0.0386, 0.0497, 0.0588, 0.061 },
    {0.0453, 0.0506, 0.065, 0.0789, 0.0898, 0.0979, 0.1069, 0.1023 }, {0.1687, 0.1836, 0.2099, 0.227, 0.2349, 0.2445, 0.2397, 0.2314 }, {0.0036, 0.006, 0.009, 0.012, 0.0153, 0.0194, 0.0245, 0.026 },
    {0.0029, 0.0043, 0.0083, 0.0152, 0.0228, 0.0264, 0.0315, 0.0335 }, {0.004, 0.0065, 0.0096, 0.0127, 0.0149, 0.017, 0.0202, 0.0226 },
    {0.0027, 0.0036, 0.0069, 0.0113, 0.0171, 0.0229, 0.0305, 0.0328 }, {0.0027, 0.0046, 0.0097, 0.0152, 0.023, 0.0301, 0.039, 0.0412 },
    {0.0017, 0.0025, 0.0064, 0.0128, 0.0209, 0.0279, 0.0318, 0.0334 }, {0.0034, 0.0044, 0.0086, 0.0146, 0.0216, 0.0291, 0.0365, 0.0383 },
    {0.0018, 0.0022, 0.004, 0.0068, 0.0106, 0.0147, 0.0193, 0.021 }, {0.0033, 0.0046, 0.0099, 0.0187, 0.0275, 0.0358, 0.0457, 0.0505 },
    {0.0016, 0.0022, 0.0044, 0.0073, 0.0113, 0.0153, 0.0215, 0.0241 }, {0.002, 0.0039, 0.0072, 0.0107, 0.0154, 0.0202, 0.028, 0.0306 },
    {Double.NaN, 0.0021, 0.006, 0.0121, 0.0183, 0.0232, 0.03, 0.0319 }, {0.0029, 0.0039, 0.0084, 0.0142, 0.0213, 0.029, 0.0369, 0.0395 },
    {0.0019, 0.0024, 0.0057, 0.0115, 0.0185, 0.0264, 0.0371, 0.0394 }, {0.0024, 0.0032, 0.0068, 0.0112, 0.0172, 0.0233, 0.0301, 0.0319 },
    {0.0046, 0.0069, 0.0166, 0.0277, 0.0391, 0.0495, 0.0576, 0.06 }, {0.0032, 0.0042, 0.0084, 0.0149, 0.0226, 0.0309, 0.0407, 0.0429 },
    {0.0025, 0.0034, 0.0071, 0.0146, 0.0226, 0.0297, 0.0365, 0.0382 }, {0.0094, 0.0127, 0.029, 0.0423, 0.0525, 0.0614, 0.0654, 0.0668 },
    {0.0018, 0.0024, 0.0048, 0.0079, 0.0122, 0.0177, 0.025, 0.0272 }, {0.002, 0.0031, 0.0059, 0.0107, 0.0175, 0.0233, 0.0335, 0.0364 },
    {0.0026, 0.0037, 0.0067, 0.0101, 0.0142, 0.0191, 0.0265, 0.0282 }, {0.0005, 0.0007, 0.0011, 0.0018, 0.0027, 0.0037, 0.0059, 0.0073 },
    {0.0007, 0.0015, 0.0033, 0.0059, 0.009, 0.0135, 0.0198, 0.0223 }, {0.0008, 0.0011, 0.003, 0.0052, 0.0076, 0.0106, 0.0151, 0.0171 },
    {0.0047, 0.0071, 0.0164, 0.0232, 0.0327, 0.0411, 0.051, 0.0534 }, {0.0049, 0.0078, 0.0111, 0.0162, 0.0234, 0.0294, 0.0358, 0.0361 },
    {0.1594, 0.1763, 0.1821, 0.1765, 0.1674, 0.1633, 0.1579, 0.1519 }, {0.004, 0.0052, 0.01, 0.0186, 0.0295, 0.0382, 0.0466, 0.0486 },
    {0.0028, 0.0043, 0.0095, 0.0155, 0.0227, 0.0294, 0.0386, 0.0402 }, {0.0015, 0.0019, 0.0048, 0.008, 0.0118, 0.0164, 0.0215, 0.0234 },
    {0.002, 0.003, 0.0061, 0.0095, 0.014, 0.0187, 0.0241, 0.0263 }, {0.0026, 0.0034, 0.0069, 0.0105, 0.0155, 0.0201, 0.0286, 0.0294 },
    {0.0019, 0.0029, 0.006, 0.0101, 0.0159, 0.0219, 0.0293, 0.0319 }, {0.0018, 0.0024, 0.0054, 0.0098, 0.015, 0.0213, 0.0266, 0.0289 },
    {0.0012, 0.0015, 0.004, 0.007, 0.0104, 0.0142, 0.0211, 0.0229 }, {0.0028, 0.0034, 0.0068, 0.0119, 0.0185, 0.0251, 0.0338, 0.0364 },
    {0.0553, 0.065, 0.0727, 0.0769, 0.0811, 0.0842, 0.0858, 0.0858 }, {0.0043, 0.0067, 0.0171, 0.0292, 0.042, 0.052, 0.0599, 0.0619 },
    {0.0028, 0.0037, 0.0078, 0.0135, 0.0217, 0.0288, 0.039, 0.0414 }, {0.0044, 0.006, 0.0112, 0.0167, 0.0223, 0.0278, 0.0349, 0.0376 },
    {0.0022, 0.0031, 0.0068, 0.0131, 0.0192, 0.0253, 0.0354, 0.0375 }, {0.0013, 0.0018, 0.0044, 0.0083, 0.0124, 0.0168, 0.0241, 0.0257 },
    {0.0009, 0.0016, 0.003, 0.0054, 0.0086, 0.0125, 0.0162, 0.0174 }, {0.0091, 0.0131, 0.0322, 0.0507, 0.0632, 0.0727, 0.0785, 0.0787 },
    {0.001, 0.0017, 0.0031, 0.0059, 0.009, 0.0127, 0.0179, 0.0203 }, {0.0011, 0.0017, 0.0033, 0.0068, 0.0106, 0.0147, 0.021, 0.0234 },
    {0.0013, 0.0017, 0.0041, 0.0071, 0.0113, 0.0154, 0.0224, 0.0243 }, {0.001, 0.0014, 0.0035, 0.0058, 0.009, 0.0123, 0.0163, 0.0192 },
    {0.0025, 0.0034, 0.0098, 0.0184, 0.0263, 0.0336, 0.0413, 0.0428 }, {0.0036, 0.0058, 0.0118, 0.0183, 0.0241, 0.0297, 0.0345, 0.0372 },
    {0.0018, 0.0022, 0.0046, 0.0079, 0.012, 0.0162, 0.0233, 0.0248 }, {0.0041, 0.0052, 0.0086, 0.016, 0.023, 0.0304, 0.0346, 0.036 },
    {0.0016, 0.0023, 0.0039, 0.0071, 0.0117, 0.0168, 0.0213, 0.0211 }, {0.0023, 0.0033, 0.0075, 0.0131, 0.0201, 0.0265, 0.0336, 0.0356 },
    {0.0014, 0.0022, 0.0044, 0.0075, 0.0119, 0.0164, 0.024, 0.0257 }, {0.0051, 0.007, 0.0124, 0.0181, 0.0236, 0.0293, 0.0362, 0.0386 },
    {0.1214, 0.1466, 0.1708, 0.1862, 0.1825, 0.1808, 0.1724, 0.1645 }, {0.0024, 0.0031, 0.0061, 0.0106, 0.0162, 0.0211, 0.0284, 0.0299 },
    {0.0028, 0.0036, 0.0069, 0.0118, 0.0187, 0.0249, 0.032, 0.0348 }, {0.0016, 0.0021, 0.0041, 0.0062, 0.009, 0.0132, 0.0203, 0.0232 },
    {0.0012, 0.0015, 0.0037, 0.0072, 0.0107, 0.0147, 0.0214, 0.0233 }, {0.002, 0.0034, 0.0068, 0.0118, 0.018, 0.0241, 0.0315, 0.0335 },
    {0.0041, 0.0051, 0.0098, 0.0156, 0.0215, 0.0279, 0.036, 0.0395 }, {0.0029, 0.004, 0.0084, 0.0145, 0.0213, 0.0281, 0.0345, 0.0371 },
    {0.0846, 0.0986, 0.1181, 0.1229, 0.1239, 0.1262, 0.1257, 0.1221 }, {0.0026, 0.0035, 0.0068, 0.0112, 0.0164, 0.0217, 0.0318, 0.0333 },
    {0.0051, 0.0073, 0.0124, 0.0192, 0.0248, 0.0304, 0.0358, 0.0378 }, {0.0046, 0.0059, 0.0109, 0.0164, 0.0235, 0.0297, 0.0384, 0.0418 },
    {0.0053, 0.0068, 0.0138, 0.0236, 0.0335, 0.0427, 0.0507, 0.053 }, {0.0029, 0.0044, 0.0085, 0.0145, 0.0211, 0.0291, 0.0363, 0.0386 },
    {0.0661, 0.0946, 0.1263, 0.1364, 0.1385, 0.1409, 0.1389, 0.1352 }, {0.0026, 0.0038, 0.0083, 0.014, 0.02, 0.0258, 0.0339, 0.0367 },
    {0.0028, 0.0036, 0.0069, 0.0118, 0.0174, 0.0236, 0.0287, 0.0303 }, {4.7657, 4.3612, 3.8436, 3.5247, 3.3097, 3.1568, 2.9315, 2.711 },
    {0.0014, 0.0022, 0.0041, 0.006, 0.0093, 0.0119, 0.0154, 0.0167 }, {0.0031, 0.0042, 0.0078, 0.0122, 0.018, 0.025, 0.0296, 0.0316 },
    {0.0016, 0.0028, 0.0059, 0.0095, 0.0145, 0.0197, 0.0271, 0.028 }, {0.0011, 0.0016, 0.003, 0.0059, 0.0094, 0.013, 0.0191, 0.0212 },
    {0.0032, 0.0048, 0.0093, 0.0166, 0.0258, 0.034, 0.0415, 0.0434 }, {0.0032, 0.0047, 0.0099, 0.0205, 0.0315, 0.0414, 0.0501, 0.0529 },
    {0.0023, 0.0033, 0.0083, 0.0152, 0.0238, 0.0309, 0.0368, 0.0384 } };

  /**
   * Build the intrinsic credit curves for CDX.NA.HY.21 on 13-Feb-2013
   * @return the credit curves 
   */
  public static ISDACompliantCreditCurve[] getCDX_NA_HY_20140213_CreditCurves() {
    final LocalDate tradeDate = LocalDate.of(2014, 2, 13);
    return buildCreditCurves(tradeDate, CDX_NA_HY_20140213_PAR_SPREADS, CDX_NA_HY_20140213_RECOVERY_RATES, CDS_TENORS, ISDA_USD_20140213);
  }

  public static ISDACompliantCreditCurve[] buildCreditCurves(final LocalDate tradeDate, final double[][] parSpreads, final double[] recoveryRates, final Period[] tenors,
      final ISDACompliantYieldCurve yieldCurve) {
    final CDSAnalyticFactory factory = new CDSAnalyticFactory(0.0);
    final CDSAnalytic[] pillarCDS = factory.makeIMMCDS(tradeDate, tenors);
    final int indexSize = parSpreads.length;
    final ISDACompliantCreditCurve[] creditCurves = new ISDACompliantCreditCurve[indexSize];

    //this section of code is hugely wasteful. If we do this for real (i.e. not just in a test), must improve  
    for (int i = 0; i < indexSize; i++) {
      final double[] spreads = parSpreads[i];
      final int nPillars = spreads.length;
      final CDSAnalytic[] tempCDS = new CDSAnalytic[nPillars];
      final double[] tempSpreads = new double[nPillars];
      int count = 0;
      for (int j = 0; j < nPillars; j++) {
        if (!Double.isNaN(parSpreads[i][j])) {
          tempCDS[count] = pillarCDS[j].withRecoveryRate(recoveryRates[i]);
          tempSpreads[count] = spreads[j];
          count++;
        }
      }

      CDSAnalytic[] calCDS = null;
      double[] calSpreads = null;
      if (count == nPillars) {
        calCDS = tempCDS;
        calSpreads = tempSpreads;
      } else {
        calCDS = new CDSAnalytic[count];
        calSpreads = new double[count];
        System.arraycopy(tempCDS, 0, calCDS, 0, count);
        System.arraycopy(tempSpreads, 0, calSpreads, 0, count);
      }

      final CreditCurveCalibrator calibrator = new CreditCurveCalibrator(calCDS, yieldCurve);
      creditCurves[i] = calibrator.calibrate(calSpreads);
    }
    return creditCurves;
  }

}
