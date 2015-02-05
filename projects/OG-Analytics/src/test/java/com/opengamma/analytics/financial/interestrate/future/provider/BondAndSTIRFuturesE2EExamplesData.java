/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;

/**
 * 
 */
public class BondAndSTIRFuturesE2EExamplesData {

  /* volatility surface */
  private static final double[] EXPIRY;
  private static final double[] SIMPLEMONEY;
  private static final double[] VOL = new double[] {
      1.0623, 1.0623, 1.0623, 1.0623, 1.0623, 0.9517, 0.8098, 0.6903, 0.6519, 0.6872, 0.7490, 0.8161, 0.8823,
      1.0623, 1.0623, 1.0623, 1.0623, 1.0623, 0.9517, 0.8098, 0.6903, 0.6519, 0.6872, 0.7490, 0.8161, 0.8823,
      1.0623, 1.0623, 1.0623, 1.0623, 1.0623, 0.9517, 0.8098, 0.6903, 0.6519, 0.6872, 0.7490, 0.8161, 0.8823,
      1.1414, 1.0815, 1.0316, 0.9926, 0.9638, 0.8791, 0.7843, 0.7094, 0.6817, 0.6948, 0.7252, 0.7617, 0.8002,
      1.1278, 1.0412, 0.9654, 0.9021, 0.8511, 0.8108, 0.7794, 0.7551, 0.7369, 0.7240, 0.7160, 0.7128, 0.7144,
      0.9697, 0.9412, 0.9130, 0.8854, 0.8585, 0.8327, 0.8084, 0.7861, 0.7664, 0.7502, 0.7383, 0.7318, 0.7317,
      0.9611, 0.9265, 0.8938, 0.8630, 0.8347, 0.8089, 0.7859, 0.7659, 0.7489, 0.7351, 0.7242, 0.7161, 0.7105,
      0.9523, 0.9116, 0.8741, 0.8401, 0.8101, 0.7843, 0.7626, 0.7451, 0.7310, 0.7197, 0.7098, 0.7000, 0.6886
  };
  private static final double[] EXPIRY_SET = new double[] {7.0 / 365.0, 14.0 / 365.0, 21.0 / 365.0, 30.0 / 365.0,
      60.0 / 365.0, 90.0 / 365.0, 120.0 / 365.0, 180.0 / 365.0 };
  private static final double[] MONEYNESS_SET = new double[] {-8.0E-3, -7.0E-3, -6.0E-3, -5.0E-3, -4.0E-3, -3.0E-3,
      -2.0E-3, -1.0E-3, 0.0, 1.0E-3, 2.0E-3, 3.0E-3, 4.0E-3 };
  private static final int NUM_EXPIRY = EXPIRY_SET.length;
  private static final int NUM_MONEY = MONEYNESS_SET.length;
  static {
    int nTotal = NUM_EXPIRY * NUM_MONEY;
    EXPIRY = new double[nTotal];
    SIMPLEMONEY = new double[nTotal];
    for (int i = 0; i < NUM_EXPIRY; ++i) {
      for (int j = 0; j < NUM_MONEY; ++j) {
        EXPIRY[i * NUM_MONEY + j] = EXPIRY_SET[i];
        SIMPLEMONEY[i * NUM_MONEY + j] = MONEYNESS_SET[j];
      }
    }
  }

  /* curves for STIR futures */
  private static final double[] TIME_GBP = new double[] {0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 1.75, 2.0, 2.25, 2.5, 2.75,
      3.0, 3.25, 3.5, 3.75, 4.0, 4.25, 4.5, 4.75, 5.0, 5.25, 5.5, 5.75, 6.0 };
  private static final double[] RATE_GBP = new double[] {0.00521959, 0.00532545, 0.00563185, 0.00612656, 0.00673759,
      0.00749054, 0.0083361, 0.00925553, 0.01019582, 0.01117409, 0.01215662, 0.01313694, 0.01406237, 0.01499585,
      0.01589558, 0.01676373, 0.01757357, 0.01838094, 0.01915775, 0.01990575, 0.02060564, 0.02130964, 0.02197014,
      0.02257627 };
  private static final double[] TIME_EUR = new double[] {0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 1.75, 2.0, 2.25, 2.5, 2.75,
      3.0, 3.25, 3.5, 3.75, 4.0, 4.25, 4.5, 4.75, 5.0, 5.25, 5.5, 5.75, 6.0 };
  private static final double[] RATE_EUR = new double[] {0.00291306, 0.00262997, 0.00257961, 0.00260046, 0.00265988,
      0.00276551, 0.00292218, 0.00313445, 0.00340066, 0.00372969, 0.00411252, 0.00455144, 0.00502192, 0.00553942,
      0.00608475, 0.00665035, 0.00720504, 0.00778156, 0.00835624, 0.00893305, 0.00949244, 0.01007358, 0.01065724,
      0.01124195 };

  /* curves for bond futures */
  private static final double[] TIME_ISSUER_LGT = new double[] {0.0027397260273972603, 0.019178082191780823,
      0.0821917808219178, 0.2465753424657534, 0.4931506849315068, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0,
      15.0, 20.0, 30.0 };
  private static final double[] RATE_ISSUER_LGT = new double[] {0.0039, 0.0039, 0.0039, 0.0039, 0.00368, 0.00432,
      0.00659, 0.01068, 0.01554, 0.019, 0.02166, 0.02388, 0.02611, 0.02824, 0.02956, 0.03376, 0.03603, 0.03824 };
  private static final double REPO_RATE_LGT = 0.0017;
  private static final double[] TIME_ISSUER_GER = new double[] {0.0027397260273972603, 0.019178082191780823,
      0.0821917808219178, 0.2465753424657534, 0.4931506849315068, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0,
      15.0, 20.0, 30.0 };
  private static final double[] RATE_ISSUER_GER = new double[] {6.8E-4, 6.8E-4, 6.8E-4, 6.8E-4, 8.8E-4, 8.3E-4,
      0.00109, 0.00212, 0.00414, 0.00674, 0.00838, 0.01075, 0.01311, 0.01547, 0.0174, 0.02289, 0.02577, 0.02693 };
  private static final double REPO_RATE_SCH = -2.0E-4;
  private static final double REPO_RATE_BUN = 9.0E-4;
  private static final double REPO_RATE_BOB = -0.0013;

  /**
   * Access TIME_ISSUER_LGT
   * @return TIME_ISSUER_LGT
   */
  public double[] getTimeLGT() {
    return TIME_ISSUER_LGT.clone();
  }

  /**
   * Access RATE_ISSUER_LGT
   * @return RATE_ISSUER_LGT
   */
  public double[] getRateLGT() {
    return RATE_ISSUER_LGT.clone();
  }

  /**
   * Access REPO_RATE_LGT
   * @return REPO_RATE_LGT
   */
  public double getRepoLGT() {
    return REPO_RATE_LGT;
  }

  /**
   * Access TIME_ISSUER_GER
   * @return TIME_ISSUER_GER
   */
  public double[] getTimeGER() {
    return TIME_ISSUER_GER.clone();
  }

  /**
   * Access RATE_ISSUER_GER
   * @return RATE_ISSUER_GER
   */
  public double[] getRateGER() {
    return RATE_ISSUER_GER.clone();
  }

  /**
   * Access REPO_RATE_SCH
   * @return REPO_RATE_SCH
   */
  public double getRepoSCH() {
    return REPO_RATE_SCH;
  }

  /**
   * Access REPO_RATE_BUN
   * @return REPO_RATE_BUN
   */
  public double getRepoBUN() {
    return REPO_RATE_BUN;
  }

  /**
   * Access REPO_RATE_BOB
   * @return REPO_RATE_BOB
   */
  public double getRepoBOB() {
    return REPO_RATE_BOB;
  }

  /**
   * access EXPIRY
   * @return EXPIRY
   */
  public double[] getExpiry() {
    return EXPIRY.clone();
  }

  /**
   * access SIMPLEMONEY
   * @return SIMPLEMONEY
   */
  public double[] getSimpleMoneyness() {
    return SIMPLEMONEY.clone();
  }

  /**
   * access VOL
   * @return VOL
   */
  public double[] getVolatility() {
    return VOL.clone();
  }

  /**
   * access TIME_GBP
   * @return TIME_GBP
   */
  public double[] getTimeGBP() {
    return TIME_GBP.clone();
  }

  /**
   * access RATE_GBP
   * @return RATE_GBP
   */
  public double[] getRateGBP() {
    return RATE_GBP.clone();
  }

  /**
   * access TIME_EUR
   * @return TIME_EUR
   */
  public double[] getTimeEUR() {
    return TIME_EUR.clone();
  }

  /**
   * access RATE_EUR
   * @return RATE_EUR
   */
  public double[] getRateEUR() {
    return RATE_EUR.clone();
  }

  /**
   * access GBPTarget
   * @return GBPTarget
   */
  public Calendar getGBPCalendar() {
    return new GBPTarget();
  }

  /**
   * access EURTarget
   * @return EURTarget
   */
  public Calendar getEURCalendar() {
    return new EURTarget();
  }

  private class GBPTarget extends MondayToFridayCalendar {
    private static final long serialVersionUID = 1L;
    public GBPTarget() {
      super("GBP");
      LocalDate[] holiday = new LocalDate[] {LocalDate.of(2005, 1, 3),
          LocalDate.of(2005, 3, 25), LocalDate.of(2005, 3, 28), LocalDate.of(2005, 5, 2), LocalDate.of(2005, 5, 30),
          LocalDate.of(2005, 8, 29), LocalDate.of(2005, 12, 26), LocalDate.of(2005, 12, 27), LocalDate.of(2006, 1, 2),
          LocalDate.of(2006, 4, 14), LocalDate.of(2006, 4, 17), LocalDate.of(2006, 5, 1), LocalDate.of(2006, 5, 29),
          LocalDate.of(2006, 8, 28), LocalDate.of(2006, 12, 25), LocalDate.of(2006, 12, 26), LocalDate.of(2007, 1, 1),
          LocalDate.of(2007, 4, 6), LocalDate.of(2007, 4, 9), LocalDate.of(2007, 5, 7), LocalDate.of(2007, 5, 28),
          LocalDate.of(2007, 8, 27), LocalDate.of(2007, 12, 25), LocalDate.of(2007, 12, 26), LocalDate.of(2008, 1, 1),
          LocalDate.of(2008, 3, 21), LocalDate.of(2008, 3, 24), LocalDate.of(2008, 5, 5), LocalDate.of(2008, 5, 26),
          LocalDate.of(2008, 8, 25), LocalDate.of(2008, 12, 25), LocalDate.of(2008, 12, 26), LocalDate.of(2009, 1, 1),
          LocalDate.of(2009, 4, 10), LocalDate.of(2009, 4, 13), LocalDate.of(2009, 5, 4), LocalDate.of(2009, 5, 25),
          LocalDate.of(2009, 8, 31), LocalDate.of(2009, 12, 25), LocalDate.of(2009, 12, 28), LocalDate.of(2010, 1, 1),
          LocalDate.of(2010, 4, 2), LocalDate.of(2010, 4, 5), LocalDate.of(2010, 5, 3), LocalDate.of(2010, 5, 31),
          LocalDate.of(2010, 8, 30), LocalDate.of(2010, 12, 27), LocalDate.of(2010, 12, 28), LocalDate.of(2011, 1, 3),
          LocalDate.of(2011, 4, 22), LocalDate.of(2011, 4, 25), LocalDate.of(2011, 4, 29), LocalDate.of(2011, 5, 2),
          LocalDate.of(2011, 5, 30), LocalDate.of(2011, 8, 29), LocalDate.of(2011, 12, 26), LocalDate.of(2011, 12, 27),
          LocalDate.of(2012, 1, 2), LocalDate.of(2012, 4, 6), LocalDate.of(2012, 4, 9), LocalDate.of(2012, 5, 7),
          LocalDate.of(2012, 6, 4), LocalDate.of(2012, 6, 5), LocalDate.of(2012, 8, 27), LocalDate.of(2012, 12, 25),
          LocalDate.of(2012, 12, 26), LocalDate.of(2013, 1, 1), LocalDate.of(2013, 3, 29), LocalDate.of(2013, 4, 1),
          LocalDate.of(2013, 5, 6), LocalDate.of(2013, 5, 27), LocalDate.of(2013, 8, 26), LocalDate.of(2013, 12, 25),
          LocalDate.of(2013, 12, 26), LocalDate.of(2014, 1, 1), LocalDate.of(2014, 4, 18), LocalDate.of(2014, 4, 21),
          LocalDate.of(2014, 5, 5), LocalDate.of(2014, 5, 26), LocalDate.of(2014, 8, 25), LocalDate.of(2014, 12, 25),
          LocalDate.of(2014, 12, 26), LocalDate.of(2015, 1, 1), LocalDate.of(2015, 4, 3), LocalDate.of(2015, 4, 6),
          LocalDate.of(2015, 5, 4), LocalDate.of(2015, 5, 25), LocalDate.of(2015, 8, 31), LocalDate.of(2015, 12, 25),
          LocalDate.of(2015, 12, 28), LocalDate.of(2016, 1, 1), LocalDate.of(2016, 3, 25), LocalDate.of(2016, 3, 28),
          LocalDate.of(2016, 5, 2), LocalDate.of(2016, 5, 30), LocalDate.of(2016, 8, 29), LocalDate.of(2016, 12, 26),
          LocalDate.of(2016, 12, 27), LocalDate.of(2017, 1, 2), LocalDate.of(2017, 4, 14), LocalDate.of(2017, 4, 17),
          LocalDate.of(2017, 5, 1), LocalDate.of(2017, 5, 29), LocalDate.of(2017, 8, 28), LocalDate.of(2017, 12, 25),
          LocalDate.of(2017, 12, 26), LocalDate.of(2018, 1, 1), LocalDate.of(2018, 3, 30), LocalDate.of(2018, 4, 2),
          LocalDate.of(2018, 5, 7), LocalDate.of(2018, 5, 28), LocalDate.of(2018, 8, 27), LocalDate.of(2018, 12, 25),
          LocalDate.of(2018, 12, 26), LocalDate.of(2019, 1, 1), LocalDate.of(2019, 4, 19), LocalDate.of(2019, 4, 22),
          LocalDate.of(2019, 5, 6), LocalDate.of(2019, 5, 27), LocalDate.of(2019, 8, 26), LocalDate.of(2019, 12, 25),
          LocalDate.of(2019, 12, 26), LocalDate.of(2020, 1, 1), LocalDate.of(2020, 4, 10), LocalDate.of(2020, 4, 13),
          LocalDate.of(2020, 5, 4), LocalDate.of(2020, 5, 25), LocalDate.of(2020, 8, 31), LocalDate.of(2020, 12, 25),
          LocalDate.of(2020, 12, 28), LocalDate.of(2021, 1, 1), LocalDate.of(2021, 4, 2), LocalDate.of(2021, 4, 5),
          LocalDate.of(2021, 5, 3), LocalDate.of(2021, 5, 31), LocalDate.of(2021, 8, 30), LocalDate.of(2021, 12, 27),
          LocalDate.of(2021, 12, 28), LocalDate.of(2022, 1, 3), LocalDate.of(2022, 4, 15), LocalDate.of(2022, 4, 18),
          LocalDate.of(2022, 5, 2), LocalDate.of(2022, 5, 30), LocalDate.of(2022, 8, 29), LocalDate.of(2022, 12, 26),
          LocalDate.of(2022, 12, 27), LocalDate.of(2023, 1, 2), LocalDate.of(2023, 4, 7), LocalDate.of(2023, 4, 10),
          LocalDate.of(2023, 5, 1), LocalDate.of(2023, 5, 29), LocalDate.of(2023, 8, 28), LocalDate.of(2023, 12, 25),
          LocalDate.of(2023, 12, 26), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 29), LocalDate.of(2024, 4, 1),
          LocalDate.of(2024, 5, 6), LocalDate.of(2024, 5, 27), LocalDate.of(2024, 8, 26), LocalDate.of(2024, 12, 25),
          LocalDate.of(2024, 12, 26), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 4, 18), LocalDate.of(2025, 4, 21),
          LocalDate.of(2025, 5, 5), LocalDate.of(2025, 5, 26), LocalDate.of(2025, 8, 25), LocalDate.of(2025, 12, 25),
          LocalDate.of(2025, 12, 26), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 3), LocalDate.of(2026, 4, 6),
          LocalDate.of(2026, 5, 4), LocalDate.of(2026, 5, 25), LocalDate.of(2026, 8, 31), LocalDate.of(2026, 12, 25),
          LocalDate.of(2026, 12, 28), LocalDate.of(2027, 1, 1), LocalDate.of(2027, 3, 26), LocalDate.of(2027, 3, 29),
          LocalDate.of(2027, 5, 3), LocalDate.of(2027, 5, 31), LocalDate.of(2027, 8, 30), LocalDate.of(2027, 12, 27),
          LocalDate.of(2027, 12, 28), LocalDate.of(2028, 1, 3), LocalDate.of(2028, 4, 14), LocalDate.of(2028, 4, 17),
          LocalDate.of(2028, 5, 1), LocalDate.of(2028, 5, 29), LocalDate.of(2028, 8, 28), LocalDate.of(2028, 12, 25),
          LocalDate.of(2028, 12, 26), LocalDate.of(2029, 1, 1), LocalDate.of(2029, 3, 30), LocalDate.of(2029, 4, 2),
          LocalDate.of(2029, 5, 7), LocalDate.of(2029, 5, 28), LocalDate.of(2029, 8, 27), LocalDate.of(2029, 12, 25),
          LocalDate.of(2029, 12, 26), LocalDate.of(2030, 1, 1), LocalDate.of(2030, 4, 19), LocalDate.of(2030, 4, 22),
          LocalDate.of(2030, 5, 6), LocalDate.of(2030, 5, 27), LocalDate.of(2030, 8, 26), LocalDate.of(2030, 12, 25),
          LocalDate.of(2030, 12, 26), LocalDate.of(2031, 1, 1), LocalDate.of(2031, 4, 11), LocalDate.of(2031, 4, 14),
          LocalDate.of(2031, 5, 5), LocalDate.of(2031, 5, 26), LocalDate.of(2031, 8, 25), LocalDate.of(2031, 12, 25),
          LocalDate.of(2031, 12, 26), LocalDate.of(2032, 1, 1), LocalDate.of(2032, 3, 26), LocalDate.of(2032, 3, 29),
          LocalDate.of(2032, 5, 3), LocalDate.of(2032, 5, 31), LocalDate.of(2032, 8, 30), LocalDate.of(2032, 12, 27),
          LocalDate.of(2032, 12, 28), LocalDate.of(2033, 1, 3), LocalDate.of(2033, 4, 15), LocalDate.of(2033, 4, 18),
          LocalDate.of(2033, 5, 2), LocalDate.of(2033, 5, 30), LocalDate.of(2033, 8, 29), LocalDate.of(2033, 12, 26),
          LocalDate.of(2033, 12, 27), LocalDate.of(2034, 1, 2), LocalDate.of(2034, 4, 7), LocalDate.of(2034, 4, 10),
          LocalDate.of(2034, 5, 1), LocalDate.of(2034, 5, 29), LocalDate.of(2034, 8, 28), LocalDate.of(2034, 12, 25),
          LocalDate.of(2034, 12, 26), LocalDate.of(2035, 1, 1), LocalDate.of(2035, 3, 23), LocalDate.of(2035, 3, 26),
          LocalDate.of(2035, 5, 7), LocalDate.of(2035, 5, 28), LocalDate.of(2035, 8, 27), LocalDate.of(2035, 12, 25),
          LocalDate.of(2035, 12, 26), LocalDate.of(2036, 1, 1), LocalDate.of(2036, 4, 11), LocalDate.of(2036, 4, 14),
          LocalDate.of(2036, 5, 5), LocalDate.of(2036, 5, 26), LocalDate.of(2036, 8, 25), LocalDate.of(2036, 12, 25),
          LocalDate.of(2036, 12, 26), LocalDate.of(2037, 1, 1), LocalDate.of(2037, 4, 3), LocalDate.of(2037, 4, 6),
          LocalDate.of(2037, 5, 4), LocalDate.of(2037, 5, 25), LocalDate.of(2037, 8, 31), LocalDate.of(2037, 12, 25),
          LocalDate.of(2037, 12, 28), LocalDate.of(2038, 1, 1), LocalDate.of(2038, 4, 23), LocalDate.of(2038, 4, 26),
          LocalDate.of(2038, 5, 3), LocalDate.of(2038, 5, 31), LocalDate.of(2038, 8, 30), LocalDate.of(2038, 12, 27),
          LocalDate.of(2038, 12, 28), LocalDate.of(2039, 1, 3), LocalDate.of(2039, 4, 8), LocalDate.of(2039, 4, 11),
          LocalDate.of(2039, 5, 2), LocalDate.of(2039, 5, 30), LocalDate.of(2039, 8, 29), LocalDate.of(2039, 12, 26),
          LocalDate.of(2039, 12, 27), LocalDate.of(2040, 1, 2), LocalDate.of(2040, 3, 30), LocalDate.of(2040, 4, 2),
          LocalDate.of(2040, 5, 7), LocalDate.of(2040, 5, 28), LocalDate.of(2040, 8, 27), LocalDate.of(2040, 12, 25),
          LocalDate.of(2040, 12, 26), LocalDate.of(2041, 1, 1), LocalDate.of(2041, 4, 19), LocalDate.of(2041, 4, 22),
          LocalDate.of(2041, 5, 6), LocalDate.of(2041, 5, 27), LocalDate.of(2041, 8, 26), LocalDate.of(2041, 12, 25),
          LocalDate.of(2041, 12, 26), LocalDate.of(2042, 1, 1), LocalDate.of(2042, 4, 4), LocalDate.of(2042, 4, 7),
          LocalDate.of(2042, 5, 5), LocalDate.of(2042, 5, 26), LocalDate.of(2042, 8, 25), LocalDate.of(2042, 12, 25),
          LocalDate.of(2042, 12, 26), LocalDate.of(2043, 1, 1), LocalDate.of(2043, 3, 27), LocalDate.of(2043, 3, 30),
          LocalDate.of(2043, 5, 4), LocalDate.of(2043, 5, 25), LocalDate.of(2043, 8, 31), LocalDate.of(2043, 12, 25),
          LocalDate.of(2043, 12, 28), LocalDate.of(2044, 1, 1), LocalDate.of(2044, 4, 15), LocalDate.of(2044, 4, 18),
          LocalDate.of(2044, 5, 2), LocalDate.of(2044, 5, 30), LocalDate.of(2044, 8, 29), LocalDate.of(2044, 12, 26),
          LocalDate.of(2044, 12, 27), LocalDate.of(2045, 1, 2), LocalDate.of(2045, 4, 7), LocalDate.of(2045, 4, 10),
          LocalDate.of(2045, 5, 1), LocalDate.of(2045, 5, 29), LocalDate.of(2045, 8, 28), LocalDate.of(2045, 12, 25),
          LocalDate.of(2045, 12, 26), LocalDate.of(2046, 1, 1), LocalDate.of(2046, 3, 23), LocalDate.of(2046, 3, 26),
          LocalDate.of(2046, 5, 7), LocalDate.of(2046, 5, 28), LocalDate.of(2046, 8, 27), LocalDate.of(2046, 12, 25),
          LocalDate.of(2046, 12, 26), LocalDate.of(2047, 1, 1), LocalDate.of(2047, 4, 12), LocalDate.of(2047, 4, 15),
          LocalDate.of(2047, 5, 6), LocalDate.of(2047, 5, 27), LocalDate.of(2047, 8, 26), LocalDate.of(2047, 12, 25),
          LocalDate.of(2047, 12, 26), LocalDate.of(2048, 1, 1), LocalDate.of(2048, 4, 3), LocalDate.of(2048, 4, 6),
          LocalDate.of(2048, 5, 4), LocalDate.of(2048, 5, 25), LocalDate.of(2048, 8, 31), LocalDate.of(2048, 12, 25),
          LocalDate.of(2048, 12, 28), LocalDate.of(2049, 1, 1), LocalDate.of(2049, 4, 16), LocalDate.of(2049, 4, 19),
          LocalDate.of(2049, 5, 3), LocalDate.of(2049, 5, 31), LocalDate.of(2049, 8, 30), LocalDate.of(2049, 12, 27),
          LocalDate.of(2049, 12, 28), LocalDate.of(2050, 1, 3), LocalDate.of(2050, 4, 8), LocalDate.of(2050, 4, 11),
          LocalDate.of(2050, 5, 2), LocalDate.of(2050, 5, 30), LocalDate.of(2050, 8, 29), LocalDate.of(2050, 12, 26),
          LocalDate.of(2050, 12, 27), LocalDate.of(2051, 1, 2), LocalDate.of(2051, 3, 31), LocalDate.of(2051, 4, 3),
          LocalDate.of(2051, 5, 1), LocalDate.of(2051, 5, 29), LocalDate.of(2051, 8, 28), LocalDate.of(2051, 12, 25),
          LocalDate.of(2051, 12, 26), LocalDate.of(2052, 1, 1), LocalDate.of(2052, 4, 19), LocalDate.of(2052, 4, 22),
          LocalDate.of(2052, 5, 6), LocalDate.of(2052, 5, 27), LocalDate.of(2052, 8, 26), LocalDate.of(2052, 12, 25),
          LocalDate.of(2052, 12, 26), LocalDate.of(2053, 1, 1), LocalDate.of(2053, 4, 4), LocalDate.of(2053, 4, 7),
          LocalDate.of(2053, 5, 5), LocalDate.of(2053, 5, 26), LocalDate.of(2053, 8, 25), LocalDate.of(2053, 12, 25),
          LocalDate.of(2053, 12, 26), LocalDate.of(2054, 1, 1), LocalDate.of(2054, 3, 27), LocalDate.of(2054, 3, 30),
          LocalDate.of(2054, 5, 4), LocalDate.of(2054, 5, 25), LocalDate.of(2054, 8, 31), LocalDate.of(2054, 12, 25),
          LocalDate.of(2054, 12, 28), LocalDate.of(2055, 1, 1), LocalDate.of(2055, 4, 16), LocalDate.of(2055, 4, 19),
          LocalDate.of(2055, 5, 3), LocalDate.of(2055, 5, 31), LocalDate.of(2055, 8, 30), LocalDate.of(2055, 12, 27),
          LocalDate.of(2055, 12, 28), LocalDate.of(2056, 1, 3), LocalDate.of(2056, 3, 31), LocalDate.of(2056, 4, 3),
          LocalDate.of(2056, 5, 1), LocalDate.of(2056, 5, 29), LocalDate.of(2056, 8, 28), LocalDate.of(2056, 12, 25),
          LocalDate.of(2056, 12, 26), LocalDate.of(2057, 1, 1), LocalDate.of(2057, 4, 20), LocalDate.of(2057, 4, 23),
          LocalDate.of(2057, 5, 7), LocalDate.of(2057, 5, 28), LocalDate.of(2057, 8, 27), LocalDate.of(2057, 12, 25),
          LocalDate.of(2057, 12, 26), LocalDate.of(2058, 1, 1), LocalDate.of(2058, 4, 12), LocalDate.of(2058, 4, 15),
          LocalDate.of(2058, 5, 6), LocalDate.of(2058, 5, 27), LocalDate.of(2058, 8, 26), LocalDate.of(2058, 12, 25),
          LocalDate.of(2058, 12, 26), LocalDate.of(2059, 1, 1), LocalDate.of(2059, 3, 28), LocalDate.of(2059, 3, 31),
          LocalDate.of(2059, 5, 5), LocalDate.of(2059, 5, 26), LocalDate.of(2059, 8, 25), LocalDate.of(2059, 12, 25),
          LocalDate.of(2059, 12, 26), LocalDate.of(2060, 1, 1), LocalDate.of(2060, 4, 16), LocalDate.of(2060, 4, 19),
          LocalDate.of(2060, 5, 3), LocalDate.of(2060, 5, 31), LocalDate.of(2060, 8, 30), LocalDate.of(2060, 12, 27),
          LocalDate.of(2060, 12, 28), LocalDate.of(2061, 1, 3), LocalDate.of(2061, 4, 8), LocalDate.of(2061, 4, 11),
          LocalDate.of(2061, 5, 2), LocalDate.of(2061, 5, 30), LocalDate.of(2061, 8, 29), LocalDate.of(2061, 12, 26),
          LocalDate.of(2061, 12, 27), LocalDate.of(2062, 1, 2), LocalDate.of(2062, 3, 24), LocalDate.of(2062, 3, 27),
          LocalDate.of(2062, 5, 1), LocalDate.of(2062, 5, 29), LocalDate.of(2062, 8, 28), LocalDate.of(2062, 12, 25),
          LocalDate.of(2062, 12, 26), LocalDate.of(2063, 1, 1), LocalDate.of(2063, 4, 13), LocalDate.of(2063, 4, 16),
          LocalDate.of(2063, 5, 7), LocalDate.of(2063, 5, 28), LocalDate.of(2063, 8, 27), LocalDate.of(2063, 12, 25),
          LocalDate.of(2063, 12, 26), LocalDate.of(2064, 1, 1), LocalDate.of(2064, 4, 4), LocalDate.of(2064, 4, 7),
          LocalDate.of(2064, 5, 5), LocalDate.of(2064, 5, 26), LocalDate.of(2064, 8, 25), LocalDate.of(2064, 12, 25),
          LocalDate.of(2064, 12, 26), LocalDate.of(2065, 1, 1), LocalDate.of(2065, 3, 27), LocalDate.of(2065, 3, 30),
          LocalDate.of(2065, 5, 4), LocalDate.of(2065, 5, 25), LocalDate.of(2065, 8, 31), LocalDate.of(2065, 12, 25),
          LocalDate.of(2065, 12, 28), LocalDate.of(2066, 1, 1), LocalDate.of(2066, 4, 9), LocalDate.of(2066, 4, 12),
          LocalDate.of(2066, 5, 3), LocalDate.of(2066, 5, 31), LocalDate.of(2066, 8, 30), LocalDate.of(2066, 12, 27),
          LocalDate.of(2066, 12, 28), LocalDate.of(2067, 1, 3), LocalDate.of(2067, 4, 1), LocalDate.of(2067, 4, 4),
          LocalDate.of(2067, 5, 2), LocalDate.of(2067, 5, 30), LocalDate.of(2067, 8, 29), LocalDate.of(2067, 12, 26),
          LocalDate.of(2067, 12, 27), LocalDate.of(2068, 1, 2), LocalDate.of(2068, 4, 20), LocalDate.of(2068, 4, 23),
          LocalDate.of(2068, 5, 7), LocalDate.of(2068, 5, 28), LocalDate.of(2068, 8, 27), LocalDate.of(2068, 12, 25),
          LocalDate.of(2068, 12, 26), LocalDate.of(2069, 1, 1), LocalDate.of(2069, 4, 12), LocalDate.of(2069, 4, 15),
          LocalDate.of(2069, 5, 6), LocalDate.of(2069, 5, 27), LocalDate.of(2069, 8, 26), LocalDate.of(2069, 12, 25),
          LocalDate.of(2069, 12, 26), LocalDate.of(2070, 1, 1), LocalDate.of(2070, 3, 28), LocalDate.of(2070, 3, 31),
          LocalDate.of(2070, 5, 5), LocalDate.of(2070, 5, 26), LocalDate.of(2070, 8, 25), LocalDate.of(2070, 12, 25),
          LocalDate.of(2070, 12, 26), LocalDate.of(2071, 1, 1), LocalDate.of(2071, 4, 17), LocalDate.of(2071, 4, 20),
          LocalDate.of(2071, 5, 4), LocalDate.of(2071, 5, 25), LocalDate.of(2071, 8, 31), LocalDate.of(2071, 12, 25),
          LocalDate.of(2071, 12, 28), LocalDate.of(2072, 1, 1), LocalDate.of(2072, 4, 8), LocalDate.of(2072, 4, 11),
          LocalDate.of(2072, 5, 2), LocalDate.of(2072, 5, 30), LocalDate.of(2072, 8, 29), LocalDate.of(2072, 12, 26),
          LocalDate.of(2072, 12, 27), LocalDate.of(2073, 1, 2), LocalDate.of(2073, 3, 24), LocalDate.of(2073, 3, 27),
          LocalDate.of(2073, 5, 1), LocalDate.of(2073, 5, 29), LocalDate.of(2073, 8, 28), LocalDate.of(2073, 12, 25),
          LocalDate.of(2073, 12, 26), LocalDate.of(2074, 1, 1), LocalDate.of(2074, 4, 13), LocalDate.of(2074, 4, 16),
          LocalDate.of(2074, 5, 7), LocalDate.of(2074, 5, 28), LocalDate.of(2074, 8, 27), LocalDate.of(2074, 12, 25),
          LocalDate.of(2074, 12, 26) };
      for (int loopy = 0; loopy < holiday.length; loopy++) {
        addNonWorkingDay(holiday[loopy]);
      }
    }
  }

  private class EURTarget extends MondayToFridayCalendar {
    private static final long serialVersionUID = 1L;
    public EURTarget() {
      super("EUR");
      LocalDate[] holiday = new LocalDate[] {LocalDate.of(2005, 3, 25),
          LocalDate.of(2005, 3, 28), LocalDate.of(2005, 12, 26), LocalDate.of(2006, 4, 14), LocalDate.of(2006, 4, 17),
          LocalDate.of(2006, 5, 1), LocalDate.of(2006, 12, 25), LocalDate.of(2006, 12, 26), LocalDate.of(2007, 1, 1),
          LocalDate.of(2007, 4, 6), LocalDate.of(2007, 4, 9), LocalDate.of(2007, 5, 1), LocalDate.of(2007, 12, 25),
          LocalDate.of(2007, 12, 26), LocalDate.of(2008, 1, 1), LocalDate.of(2008, 3, 21), LocalDate.of(2008, 3, 24),
          LocalDate.of(2008, 5, 1), LocalDate.of(2008, 12, 25), LocalDate.of(2008, 12, 26), LocalDate.of(2009, 1, 1),
          LocalDate.of(2009, 4, 10), LocalDate.of(2009, 4, 13), LocalDate.of(2009, 5, 1), LocalDate.of(2009, 12, 25),
          LocalDate.of(2010, 1, 1), LocalDate.of(2010, 4, 2), LocalDate.of(2010, 4, 5), LocalDate.of(2011, 4, 22),
          LocalDate.of(2011, 4, 25), LocalDate.of(2011, 12, 26), LocalDate.of(2012, 4, 6), LocalDate.of(2012, 4, 9),
          LocalDate.of(2012, 5, 1), LocalDate.of(2012, 12, 25), LocalDate.of(2012, 12, 26), LocalDate.of(2013, 1, 1),
          LocalDate.of(2013, 3, 29), LocalDate.of(2013, 4, 1), LocalDate.of(2013, 5, 1), LocalDate.of(2013, 12, 25),
          LocalDate.of(2013, 12, 26), LocalDate.of(2014, 1, 1), LocalDate.of(2014, 4, 18), LocalDate.of(2014, 4, 21),
          LocalDate.of(2014, 5, 1), LocalDate.of(2014, 12, 25), LocalDate.of(2014, 12, 26), LocalDate.of(2015, 1, 1),
          LocalDate.of(2015, 4, 3), LocalDate.of(2015, 4, 6), LocalDate.of(2015, 5, 1), LocalDate.of(2015, 12, 25),
          LocalDate.of(2016, 1, 1), LocalDate.of(2016, 3, 25), LocalDate.of(2016, 3, 28), LocalDate.of(2016, 12, 26),
          LocalDate.of(2017, 4, 14), LocalDate.of(2017, 4, 17), LocalDate.of(2017, 5, 1), LocalDate.of(2017, 12, 25),
          LocalDate.of(2017, 12, 26), LocalDate.of(2018, 1, 1), LocalDate.of(2018, 3, 30), LocalDate.of(2018, 4, 2),
          LocalDate.of(2018, 5, 1), LocalDate.of(2018, 12, 25), LocalDate.of(2018, 12, 26), LocalDate.of(2019, 1, 1),
          LocalDate.of(2019, 4, 19), LocalDate.of(2019, 4, 22), LocalDate.of(2019, 5, 1), LocalDate.of(2019, 12, 25),
          LocalDate.of(2019, 12, 26), LocalDate.of(2020, 1, 1), LocalDate.of(2020, 4, 10), LocalDate.of(2020, 4, 13),
          LocalDate.of(2020, 5, 1), LocalDate.of(2020, 12, 25), LocalDate.of(2021, 1, 1), LocalDate.of(2021, 4, 2),
          LocalDate.of(2021, 4, 5), LocalDate.of(2022, 4, 15), LocalDate.of(2022, 4, 18), LocalDate.of(2022, 12, 26),
          LocalDate.of(2023, 4, 7), LocalDate.of(2023, 4, 10), LocalDate.of(2023, 5, 1), LocalDate.of(2023, 12, 25),
          LocalDate.of(2023, 12, 26), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 29), LocalDate.of(2024, 4, 1),
          LocalDate.of(2024, 5, 1), LocalDate.of(2024, 12, 25), LocalDate.of(2024, 12, 26), LocalDate.of(2025, 1, 1),
          LocalDate.of(2025, 4, 18), LocalDate.of(2025, 4, 21), LocalDate.of(2025, 5, 1), LocalDate.of(2025, 12, 25),
          LocalDate.of(2025, 12, 26), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 3), LocalDate.of(2026, 4, 6),
          LocalDate.of(2026, 5, 1), LocalDate.of(2026, 12, 25), LocalDate.of(2027, 1, 1), LocalDate.of(2027, 3, 26),
          LocalDate.of(2027, 3, 29), LocalDate.of(2028, 4, 14), LocalDate.of(2028, 4, 17), LocalDate.of(2028, 5, 1),
          LocalDate.of(2028, 12, 25), LocalDate.of(2028, 12, 26), LocalDate.of(2029, 1, 1), LocalDate.of(2029, 3, 30),
          LocalDate.of(2029, 4, 2), LocalDate.of(2029, 5, 1), LocalDate.of(2029, 12, 25), LocalDate.of(2029, 12, 26),
          LocalDate.of(2030, 1, 1), LocalDate.of(2030, 4, 19), LocalDate.of(2030, 4, 22), LocalDate.of(2030, 5, 1),
          LocalDate.of(2030, 12, 25), LocalDate.of(2030, 12, 26), LocalDate.of(2031, 1, 1), LocalDate.of(2031, 4, 11),
          LocalDate.of(2031, 4, 14), LocalDate.of(2031, 5, 1), LocalDate.of(2031, 12, 25), LocalDate.of(2031, 12, 26),
          LocalDate.of(2032, 1, 1), LocalDate.of(2032, 3, 26), LocalDate.of(2032, 3, 29), LocalDate.of(2033, 4, 15),
          LocalDate.of(2033, 4, 18), LocalDate.of(2033, 12, 26), LocalDate.of(2034, 4, 7), LocalDate.of(2034, 4, 10),
          LocalDate.of(2034, 5, 1), LocalDate.of(2034, 12, 25), LocalDate.of(2034, 12, 26), LocalDate.of(2035, 1, 1),
          LocalDate.of(2035, 3, 23), LocalDate.of(2035, 3, 26), LocalDate.of(2035, 5, 1), LocalDate.of(2035, 12, 25),
          LocalDate.of(2035, 12, 26), LocalDate.of(2036, 1, 1), LocalDate.of(2036, 4, 11), LocalDate.of(2036, 4, 14),
          LocalDate.of(2036, 5, 1), LocalDate.of(2036, 12, 25), LocalDate.of(2036, 12, 26), LocalDate.of(2037, 1, 1),
          LocalDate.of(2037, 4, 3), LocalDate.of(2037, 4, 6), LocalDate.of(2037, 5, 1), LocalDate.of(2037, 12, 25),
          LocalDate.of(2038, 1, 1), LocalDate.of(2038, 4, 23), LocalDate.of(2038, 4, 26), LocalDate.of(2039, 4, 8),
          LocalDate.of(2039, 4, 11), LocalDate.of(2039, 12, 26), LocalDate.of(2040, 3, 30), LocalDate.of(2040, 4, 2),
          LocalDate.of(2040, 5, 1), LocalDate.of(2040, 12, 25), LocalDate.of(2040, 12, 26), LocalDate.of(2041, 1, 1),
          LocalDate.of(2041, 4, 19), LocalDate.of(2041, 4, 22), LocalDate.of(2041, 5, 1), LocalDate.of(2041, 12, 25),
          LocalDate.of(2041, 12, 26), LocalDate.of(2042, 1, 1), LocalDate.of(2042, 4, 4), LocalDate.of(2042, 4, 7),
          LocalDate.of(2042, 5, 1), LocalDate.of(2042, 12, 25), LocalDate.of(2042, 12, 26), LocalDate.of(2043, 1, 1),
          LocalDate.of(2043, 3, 27), LocalDate.of(2043, 3, 30), LocalDate.of(2043, 5, 1), LocalDate.of(2043, 12, 25),
          LocalDate.of(2044, 1, 1), LocalDate.of(2044, 4, 15), LocalDate.of(2044, 4, 18), LocalDate.of(2044, 12, 26),
          LocalDate.of(2045, 4, 7), LocalDate.of(2045, 4, 10), LocalDate.of(2045, 5, 1), LocalDate.of(2045, 12, 25),
          LocalDate.of(2045, 12, 26), LocalDate.of(2046, 1, 1), LocalDate.of(2046, 3, 23), LocalDate.of(2046, 3, 26),
          LocalDate.of(2046, 5, 1), LocalDate.of(2046, 12, 25), LocalDate.of(2046, 12, 26), LocalDate.of(2047, 1, 1),
          LocalDate.of(2047, 4, 12), LocalDate.of(2047, 4, 15), LocalDate.of(2047, 5, 1), LocalDate.of(2047, 12, 25),
          LocalDate.of(2047, 12, 26), LocalDate.of(2048, 1, 1), LocalDate.of(2048, 4, 3), LocalDate.of(2048, 4, 6),
          LocalDate.of(2048, 5, 1), LocalDate.of(2048, 12, 25), LocalDate.of(2049, 1, 1), LocalDate.of(2049, 4, 16),
          LocalDate.of(2049, 4, 19), LocalDate.of(2050, 4, 8), LocalDate.of(2050, 4, 11), LocalDate.of(2050, 12, 26),
          LocalDate.of(2051, 3, 31), LocalDate.of(2051, 4, 3), LocalDate.of(2051, 5, 1), LocalDate.of(2051, 12, 25),
          LocalDate.of(2051, 12, 26), LocalDate.of(2052, 1, 1), LocalDate.of(2052, 4, 19), LocalDate.of(2052, 4, 22),
          LocalDate.of(2052, 5, 1), LocalDate.of(2052, 12, 25), LocalDate.of(2052, 12, 26), LocalDate.of(2053, 1, 1),
          LocalDate.of(2053, 4, 4), LocalDate.of(2053, 4, 7), LocalDate.of(2053, 5, 1), LocalDate.of(2053, 12, 25),
          LocalDate.of(2053, 12, 26), LocalDate.of(2054, 1, 1), LocalDate.of(2054, 3, 27), LocalDate.of(2054, 3, 30),
          LocalDate.of(2054, 5, 1), LocalDate.of(2054, 12, 25), LocalDate.of(2055, 1, 1), LocalDate.of(2055, 4, 16),
          LocalDate.of(2055, 4, 19), LocalDate.of(2056, 3, 31), LocalDate.of(2056, 4, 3), LocalDate.of(2056, 5, 1),
          LocalDate.of(2056, 12, 25), LocalDate.of(2056, 12, 26), LocalDate.of(2057, 1, 1), LocalDate.of(2057, 4, 20),
          LocalDate.of(2057, 4, 23), LocalDate.of(2057, 5, 1), LocalDate.of(2057, 12, 25), LocalDate.of(2057, 12, 26),
          LocalDate.of(2058, 1, 1), LocalDate.of(2058, 4, 12), LocalDate.of(2058, 4, 15), LocalDate.of(2058, 5, 1),
          LocalDate.of(2058, 12, 25), LocalDate.of(2058, 12, 26), LocalDate.of(2059, 1, 1), LocalDate.of(2059, 3, 28),
          LocalDate.of(2059, 3, 31), LocalDate.of(2059, 5, 1), LocalDate.of(2059, 12, 25), LocalDate.of(2059, 12, 26),
          LocalDate.of(2060, 1, 1), LocalDate.of(2060, 4, 16), LocalDate.of(2060, 4, 19), LocalDate.of(2061, 4, 8),
          LocalDate.of(2061, 4, 11), LocalDate.of(2061, 12, 26), LocalDate.of(2062, 3, 24), LocalDate.of(2062, 3, 27),
          LocalDate.of(2062, 5, 1), LocalDate.of(2062, 12, 25), LocalDate.of(2062, 12, 26), LocalDate.of(2063, 1, 1),
          LocalDate.of(2063, 4, 13), LocalDate.of(2063, 4, 16), LocalDate.of(2063, 5, 1), LocalDate.of(2063, 12, 25),
          LocalDate.of(2063, 12, 26), LocalDate.of(2064, 1, 1), LocalDate.of(2064, 4, 4), LocalDate.of(2064, 4, 7),
          LocalDate.of(2064, 5, 1), LocalDate.of(2064, 12, 25), LocalDate.of(2064, 12, 26), LocalDate.of(2065, 1, 1),
          LocalDate.of(2065, 3, 27), LocalDate.of(2065, 3, 30), LocalDate.of(2065, 5, 1), LocalDate.of(2065, 12, 25),
          LocalDate.of(2066, 1, 1), LocalDate.of(2066, 4, 9), LocalDate.of(2066, 4, 12), LocalDate.of(2067, 4, 1),
          LocalDate.of(2067, 4, 4), LocalDate.of(2067, 12, 26), LocalDate.of(2068, 4, 20), LocalDate.of(2068, 4, 23),
          LocalDate.of(2068, 5, 1), LocalDate.of(2068, 12, 25), LocalDate.of(2068, 12, 26), LocalDate.of(2069, 1, 1),
          LocalDate.of(2069, 4, 12), LocalDate.of(2069, 4, 15), LocalDate.of(2069, 5, 1), LocalDate.of(2069, 12, 25),
          LocalDate.of(2069, 12, 26), LocalDate.of(2070, 1, 1), LocalDate.of(2070, 3, 28), LocalDate.of(2070, 3, 31),
          LocalDate.of(2070, 5, 1), LocalDate.of(2070, 12, 25), LocalDate.of(2070, 12, 26), LocalDate.of(2071, 1, 1),
          LocalDate.of(2071, 4, 17), LocalDate.of(2071, 4, 20), LocalDate.of(2071, 5, 1), LocalDate.of(2071, 12, 25),
          LocalDate.of(2072, 1, 1), LocalDate.of(2072, 4, 8), LocalDate.of(2072, 4, 11), LocalDate.of(2072, 12, 26),
          LocalDate.of(2073, 3, 24), LocalDate.of(2073, 3, 27), LocalDate.of(2073, 5, 1), LocalDate.of(2073, 12, 25),
          LocalDate.of(2073, 12, 26), LocalDate.of(2074, 1, 1), LocalDate.of(2074, 4, 13), LocalDate.of(2074, 4, 16),
          LocalDate.of(2074, 5, 1), LocalDate.of(2074, 12, 25), LocalDate.of(2074, 12, 26) };
      for (int loopy = 0; loopy < holiday.length; loopy++) {
        addNonWorkingDay(holiday[loopy]);
      }
    }

  }
}
