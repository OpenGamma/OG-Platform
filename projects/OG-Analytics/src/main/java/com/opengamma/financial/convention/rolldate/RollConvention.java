/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.convention.rolldate;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.Temporal;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

/**
 * Convention that controls the calculation period end date adjustments.
 */
public enum RollConvention {

  /**
   * End Of Month roll date adjuster
   */
  EOM {
    @Override
    public TemporalAdjuster getAdjuster() {
      return EndOfMonthTemporalAdjuster.getAdjuster();
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, TemporalAdjusters.lastDayOfMonth());
    }
  },

  /**
   * SFE roll date adjuster
   */
  SFE {
    @Override
    public TemporalAdjuster getAdjuster() {
      return TemporalAdjusters.dayOfWeekInMonth(2, DayOfWeek.FRIDAY);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, TemporalAdjusters.dayOfWeekInMonth(2, DayOfWeek.FRIDAY));
    }
  },

  /**
   * No adjustment roll date adjuster
   */
  NONE {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new TemporalAdjuster() {
        @Override
        public Temporal adjustInto(final Temporal temporal) {
          return temporal;
        }
      };
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new TemporalAdjuster() {
        @Override
        public Temporal adjustInto(final Temporal temporal) {
          return temporal;
        }
      });
    }
  },

  /**
   * TBILL roll date adjuster
   */
  TBILL {
    @Override
    public TemporalAdjuster getAdjuster() {
      return null;
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return MON.getRollDateAdjuster(numMonthsToAdjust);
    }
  },

  /**
   *  day of the month number 1 roll date adjuster
   */
  ONE {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(1);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(1));
    }
  },

  /**
   *  day of the month number 2 roll date adjuster
   */
  TWO {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(2);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(2));
    }
  },

  /**
   *  day of the month number 3 roll date adjuster
   */
  THREE {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(3);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(3));
    }
  },
  /**
   *  day of the month number 4 roll date adjuster
   */
  FOUR {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(4);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(4));
    }
  },
  /**
   *  day of the month number 5 roll date adjuster
   */
  FIVE {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(5);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(5));
    }
  },
  /**
   *  day of the month number 6 roll date adjuster
   */
  SIX {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(6);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(6));
    }
  },
  /**
   *  day of the month number 7 roll date adjuster
   */
  SEVEN {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(7);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(7));
    }
  },
  /**
   *  day of the month number 8 roll date adjuster
   */
  EIGHT {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(8);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(8));
    }
  },
  /**
   *  day of the month number 9 roll date adjuster
   */
  NINE {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(9);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(9));
    }
  },
  /**
   *  day of the month number 10 roll date adjuster
   */
  TEN {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(10);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(10));
    }
  },
  /**
   *  day of the month number 11 roll date adjuster
   */
  ELEVEN {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(11);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(11));
    }
  },
  /**
   *  day of the month number 12 roll date adjuster
   */
  TWELVE {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(12);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(12));
    }
  },
  /**
   *  day of the month number 13 roll date adjuster
   */
  THIRTEEN {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(13);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(13));
    }
  },
  /**
   *  day of the month number 14 roll date adjuster
   */
  FOURTEEN {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(14);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(14));
    }
  },
  /**
   *  day of the month number 15 roll date adjuster
   */
  FIFTEEN {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(15);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(15));
    }
  },
  /**
   *  day of the month number 16 roll date adjuster
   */
  SIXTEEN {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(16);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(16));
    }
  },
  /**
   *  day of the month number 17 roll date adjuster
   */
  SEVENTEEN {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(17);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(17));
    }
  },
  /**
   *  day of the month number 18 roll date adjuster
   */
  EIGHTEEN {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(18);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(18));
    }
  },
  /**
   *  day of the month number 19 roll date adjuster
   */
  NINETEEN {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(19);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(19));
    }
  },
  /**
   *  day of the month number 20 roll date adjuster
   */
  TWENTY {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(20);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(20));
    }
  },
  /**
   *  day of the month number 21 roll date adjuster
   */
  TWENTY_ONE {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(21);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(21));
    }
  },
  /**
   *  day of the month number 22 roll date adjuster
   */
  TWENTY_TWO {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(22);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(22));
    }
  },
  /**
   *  day of the month number 23 roll date adjuster
   */
  TWENTY_THREE {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(23);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(23));
    }
  },
  /**
   *  day of the month number 24 roll date adjuster
   */
  TWENTY_FOUR {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(24);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(24));
    }
  },
  /**
   *  day of the month number 25 roll date adjuster
   */
  TWENTY_FIVE {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(25);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(25));
    }
  },
  /**
   *  day of the month number 26 roll date adjuster
   */
  TWENTY_SIX {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(26);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(26));
    }
  },
  /**
   *  day of the month number 27 roll date adjuster
   */
  TWENTY_SEVEN {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(27);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(27));
    }
  },
  /**
   *  day of the month number 28 roll date adjuster
   */
  TWENTY_EIGHT {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(28);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(28));
    }
  },
  /**
   *  day of the month number 29 roll date adjuster
   */
  TWENTY_NINE {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(29);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(29));
    }
  },
  /**
   *  day of the month number 30 roll date adjuster
   */
  THIRTY {
    @Override
    public TemporalAdjuster getAdjuster() {
      return new DayOfMonthTemporalAdjuster(30);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, new DayOfMonthTemporalAdjuster(30));
    }
  },
  /**
   *  monday roll date adjuster
   */
  MON {
    @Override
    public TemporalAdjuster getAdjuster() {
      return TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
    }
  },
  /**
   *  tuesday roll date adjuster
   */
  TUE {
    @Override
    public TemporalAdjuster getAdjuster() {
      return TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY));
    }
  },
  /**
   *  wednesday roll date adjuster
   */
  WED {
    @Override
    public TemporalAdjuster getAdjuster() {
      return TemporalAdjusters.nextOrSame(DayOfWeek.WEDNESDAY);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, TemporalAdjusters.nextOrSame(DayOfWeek.WEDNESDAY));
    }
  },
  /**
   *  thursday roll date adjuster
   */
  THU {
    @Override
    public TemporalAdjuster getAdjuster() {
      return TemporalAdjusters.nextOrSame(DayOfWeek.THURSDAY);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, TemporalAdjusters.nextOrSame(DayOfWeek.THURSDAY));
    }
  },
  /**
   *  friday roll date adjuster
   */
  FRI {
    @Override
    public TemporalAdjuster getAdjuster() {
      return TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
    }
  },
  /**
   *  saturday roll date adjuster
   */
  SAT {
    @Override
    public TemporalAdjuster getAdjuster() {
      return TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
    }
  },
  /**
   *  sunday roll date adjuster
   */
  SUN {
    @Override
    public TemporalAdjuster getAdjuster() {
      return TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY);
    }

    @Override
    public RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust) {
      return new GeneralRollDateAdjuster(numMonthsToAdjust, TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }
  };

  public abstract TemporalAdjuster getAdjuster();

  public abstract RollDateAdjuster getRollDateAdjuster(final int numMonthsToAdjust);

  public LocalDate adjust(final LocalDate date) {
    return date.with(getAdjuster());
  }

  /**
   * Get convention for this day of month.
   * @param dayOfMonth the day of the month
   * @return roll date adjuster
   */
  public static RollConvention dayOfMonth(final int dayOfMonth) {
    switch (dayOfMonth) {
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
}
