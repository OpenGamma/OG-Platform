/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.rolldate;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.temporal.Temporal;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

/**
 * Convention that controls rolling date adjustments.
 */
public enum RollConvention {

  /**
   * EOM, end of month roll date adjuster.
   */
  EOM {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return EndOfMonthRollDateAdjuster.getAdjuster();
    }
  },

  /**
   * FRN Convention or Eurodollar convention roll date adjuster.
   */
  FRN {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numDaysToAdjust) {
      throw new UnsupportedOperationException("FRN not implemented yet.");
    }
  },

  /**
   * IMM settlement dates roll date adjuster.
   * 3rd Wednesday of March, June, September or December
   */
  IMM {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      switch (numMonthsToAdjust) {
        case 1:
          return MonthlyIMMRollDateAdjuster.getAdjuster();
        case 3:
          return QuarterlyIMMRollDateAdjuster.getAdjuster();
        default:
          return new GeneralRollDateAdjuster(numMonthsToAdjust, TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.WEDNESDAY));
      }
    }
  },

  /**
   * One Sydney business day preceeding the 2nd Friday of the month.
   */
  IMM_AUD {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      throw new UnsupportedOperationException("IMM_AUD not implemented yet.");
    }
  },

  /**
   * IMM_CAD.
   */
  IMM_CAD {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      throw new UnsupportedOperationException("IMM_CAD not implemented yet.");
    }
  },

  /**
   * The Wednesday after the 9th day of the month.
   */
  IMM_NZD {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new TemporalAdjuster() {
        @Override
        public Temporal adjustInto(Temporal temporal) {
          return temporal.with(new DayOfMonthTemporalAdjuster(9)).with(TemporalAdjusters.nextOrSame(DayOfWeek.WEDNESDAY));
        }
      });
    }
  },

  /**
   * Sydney Futures Exchange convention. 2nd Friday of the month.
   */
  SFE {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, TemporalAdjusters.dayOfWeekInMonth(2, DayOfWeek.FRIDAY));
    }
  },

  /**
   * No adjustment i.e. daily or can be used if date should be taken with no adjustment.
   */
  NONE {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new TemporalAdjuster() {
        @Override
        public Temporal adjustInto(Temporal temporal) {
          return temporal;
        }
      });
    }
  },

  /**
   * Treasury bill convention. Each Monday
   */
  TBILL {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return MON.getRollDateAdjuster(numMonthsToAdjust);
    }
  },

  /**
   * 1st day of the month roll date adjuster.
   */
  ONE {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(1));
    }
  },

  /**
   * 2nd day of the month roll date adjuster.
   */
  TWO {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(2));
    }
  },

  /**
   * 3rd day of the month roll date adjuster.
   */
  THREE {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(3));
    }
  },

  /**
   * 4th day of the month roll date adjuster.
   */
  FOUR {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(4));
    }
  },

  /**
   * 5th day of the month roll date adjuster.
   */
  FIVE {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(5));
    }
  },

  /**
   * 6th day of the month roll date adjuster.
   */
  SIX {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(6));
    }
  },

  /**
   * 7th day of the month roll date adjuster.
   */
  SEVEN {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(7));
    }
  },

  /**
   * 8th day of the month roll date adjuster.
   */
  EIGHT {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(8));
    }
  },

  /**
   * 9th day of the month roll date adjuster.
   */
  NINE {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(9));
    }
  },

  /**
   * 10th day of the month roll date adjuster.
   */
  TEN {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(10));
    }
  },

  /**
   * 11th day of the month roll date adjuster.
   */
  ELEVEN {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(11));
    }
  },

  /**
   * 12th day of the month roll date adjuster.
   */
  TWELVE {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(12));
    }
  },

  /**
   * 13th day of the month roll date adjuster.
   */
  THIRTEEN {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(13));
    }
  },

  /**
   * 14th day of the month roll date adjuster.
   */
  FOURTEEN {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(14));
    }
  },

  /**
   * 15th day of the month roll date adjuster.
   */
  FIFTEEN {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(15));
    }
  },

  /**
   * 16th day of the month roll date adjuster.
   */
  SIXTEEN {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(16));
    }
  },

  /**
   * 17th day of the month roll date adjuster.
   */
  SEVENTEEN {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(17));
    }
  },

  /**
   * 18th day of the month roll date adjuster.
   */
  EIGHTEEN {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(18));
    }
  },

  /**
   * 19th day of the month roll date adjuster.
   */
  NINETEEN {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(19));
    }
  },

  /**
   * 20th day of the month roll date adjuster.
   */
  TWENTY {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(20));
    }
  },

  /**
   * 21st day of the month roll date adjuster.
   */
  TWENTY_ONE {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(21));
    }
  },

  /**
   * 22nd day of the month roll date adjuster.
   */
  TWENTY_TWO {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(22));
    }
  },

  /**
   * 23rd day of the month roll date adjuster.
   */
  TWENTY_THREE {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(23));
    }
  },

  /**
   * 24th day of the month roll date adjuster.
   */
  TWENTY_FOUR {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(24));
    }
  },

  /**
   * 25th day of the month roll date adjuster.
   */
  TWENTY_FIVE {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(25));
    }
  },

  /**
   * 26th day of the month roll date adjuster.
   */
  TWENTY_SIX {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(26));
    }
  },

  /**
   * 27th day of the month roll date adjuster.
   */
  TWENTY_SEVEN {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(27));
    }
  },

  /**
   * 28th day of the month roll date adjuster.
   */
  TWENTY_EIGHT {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(28));
    }
  },

  /**
   * 29th day of the month roll date adjuster.
   */
  TWENTY_NINE {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(29));
    }
  },

  /**
   * 30th day of the month roll date adjuster.
   */
  THIRTY {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(30));
    }
  },

  /**
   * Monday roll date adjuster.
   */
  MON {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
    }
  },

  /**
   * Tuesday roll date adjuster.
   */
  TUE {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY));
    }
  },

  /**
   * Wednesday roll date adjuster.
   */
  WED {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, TemporalAdjusters.nextOrSame(DayOfWeek.WEDNESDAY));
    }
  },

  /**
   * Thursday roll date adjuster.
   */
  THU {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, TemporalAdjusters.nextOrSame(DayOfWeek.THURSDAY));
    }
  },

  /**
   * Friday roll date adjuster.
   */
  FRI {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
    }
  },

  /**
   * Saturday roll date adjuster.
   */
  SAT {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
    }
  },

  /**
   * Sunday roll date adjuster.
   */
  SUN {
    @Override
    public RollDateAdjuster getRollDateAdjuster(int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }
  };

  /**
   * Get convention for this day of month.
   * 
   * @param dayOfMonth  the day of the month
   * @return the convention, not null
   * @throws IllegalArgumentException if the day of month is invalid
   */
  public static RollConvention dayOfMonth(final int dayOfMonth) {
    switch(dayOfMonth) {
      case 1:
        return ONE;
      case 2:
        return TWO;
      case 3:
        return THREE;
      case 4:
        return FOUR;
      case 5:
        return FIVE;
      case 6:
        return SIX;
      case 7:
        return SEVEN;
      case 8:
        return EIGHT;
      case 9:
        return NINE;
      case 10:
        return TEN;
      case 11:
        return ELEVEN;
      case 12:
        return TWELVE;
      case 13:
        return THIRTEEN;
      case 14:
        return FOURTEEN;
      case 15:
        return FIFTEEN;
      case 16:
        return SIXTEEN;
      case 17:
        return SEVENTEEN;
      case 18:
        return EIGHTEEN;
      case 19:
        return NINETEEN;
      case 20:
        return TWENTY;
      case 21:
        return TWENTY_ONE;
      case 22:
        return TWENTY_TWO;
      case 23:
        return TWENTY_THREE;
      case 24:
        return TWENTY_FOUR;
      case 25:
        return TWENTY_FIVE;
      case 26:
        return TWENTY_SIX;
      case 27:
        return TWENTY_SEVEN;
      case 28:
        return TWENTY_EIGHT;
      case 29:
        return TWENTY_NINE;
      case 30:
        return THIRTY;
      default:
        throw new IllegalArgumentException("RollConvention only valid for days 1-30: " + dayOfMonth);
    }
  }

  /**
   * Gets the roll date adjuster.
   * 
   * @param numMonthsToAdjust  the number of months to adjust, not null
   * @return the adjuster, not null
   */
  public abstract RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust);

}
