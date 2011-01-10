/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast;

import java.util.Calendar;

import com.opengamma.OpenGammaRuntimeException;

/**
 * @author jim
 * 
 */
public enum DateTimeNumericEncoding {

  TIME_EPOCH_NANOS {
    private static final long SECONDS_PER_DAY = 3600 * 24;
    private static final long MILLIS_PER_DAY = SECONDS_PER_DAY * 1000;
    private static final long NANOS_PER_DAY = MILLIS_PER_DAY * 1000;
    private static final long NANOS_PER_SECOND = 1000000;
    private static final long NANOS_PER_MILLI = 1000;
    private final ThreadLocal<Calendar> _calendar = new ThreadLocal<Calendar>() {
      @Override
      protected Calendar initialValue() {
        return Calendar.getInstance();
      }
    };

    @Override
    public DateTimeResolution getResolution() {
      return DateTimeResolution.DATETIME;
    }

    @Override
    public boolean isIntegerBigEnough() {
      return false;
    }

    @Override
    public long convertToLong(final long sourceValue, final DateTimeNumericEncoding targetEncoding) {
      switch (targetEncoding) {
        case DATE_DDMMYYYY:
          final Calendar calendar = _calendar.get();
          calendar.setTimeInMillis(sourceValue / 1000);
          long value = calendar.get(Calendar.YEAR) * 10000;
          value += (calendar.get(Calendar.MONTH) + 1) * 100;
          value += calendar.get(Calendar.DAY_OF_MONTH);
          return value;
        case DATE_EPOCH_DAYS:
          return sourceValue / NANOS_PER_DAY;
        case TIME_EPOCH_SECONDS:
          return sourceValue / NANOS_PER_SECOND;
        case TIME_EPOCH_MILLIS:
          return sourceValue / NANOS_PER_MILLI;
        case TIME_EPOCH_NANOS:
          return sourceValue;
      }
      throw new OpenGammaRuntimeException("Impossible");
    }

  },
  TIME_EPOCH_MILLIS {
    private static final long MILLIS_PER_DAY = 3600 * 24 * 1000;
    private static final long MILLIS_PER_SECOND = 1000;
    private static final long NANOS_PER_MILLI = 1000;
    private final ThreadLocal<Calendar> _calendar = new ThreadLocal<Calendar>() {
      @Override
      protected Calendar initialValue() {
        return Calendar.getInstance();
      }
    };

    @Override
    public DateTimeResolution getResolution() {
      return DateTimeResolution.DATETIME;
    }

    @Override
    public boolean isIntegerBigEnough() {
      return false;
    }

    @Override
    public long convertToLong(final long sourceValue, final DateTimeNumericEncoding targetEncoding) {
      switch (targetEncoding) {
        case DATE_DDMMYYYY:
          final Calendar calendar = _calendar.get();
          calendar.setTimeInMillis(sourceValue);
          long value = calendar.get(Calendar.YEAR) * 10000;
          value += (calendar.get(Calendar.MONTH) + 1) * 100;
          value += calendar.get(Calendar.DAY_OF_MONTH);
          return value;
        case DATE_EPOCH_DAYS:
          return sourceValue / MILLIS_PER_DAY;
        case TIME_EPOCH_SECONDS:
          return sourceValue / MILLIS_PER_SECOND;
        case TIME_EPOCH_MILLIS:
          return sourceValue;
        case TIME_EPOCH_NANOS:
          return sourceValue * NANOS_PER_MILLI;
      }
      throw new OpenGammaRuntimeException("Impossible");
    }
  },
  TIME_EPOCH_SECONDS {
    private static final long SECONDS_PER_DAY = 3600 * 24;
    private static final long MILLIS_PER_SECOND = 1000;
    private static final long NANOS_PER_SECOND = 1000000;
    private final ThreadLocal<Calendar> _calendar = new ThreadLocal<Calendar>() {
      @Override
      protected Calendar initialValue() {
        return Calendar.getInstance();
      }
    };

    @Override
    public DateTimeResolution getResolution() {
      return DateTimeResolution.DATE;
    }

    @Override
    public boolean isIntegerBigEnough() {
      return true; // although lets face it, it's not a great idea
    }

    @Override
    public long convertToLong(final long sourceValue, final DateTimeNumericEncoding targetEncoding) {
      switch (targetEncoding) {
        case DATE_DDMMYYYY:
          final Calendar calendar = _calendar.get();
          calendar.setTimeInMillis(sourceValue * 1000);
          long value = calendar.get(Calendar.YEAR) * 10000;
          value += (calendar.get(Calendar.MONTH) + 1) * 100;
          value += calendar.get(Calendar.DAY_OF_MONTH);
          return value;
        case DATE_EPOCH_DAYS:
          return sourceValue / SECONDS_PER_DAY;
        case TIME_EPOCH_SECONDS:
          return sourceValue;
        case TIME_EPOCH_MILLIS:
          return sourceValue * MILLIS_PER_SECOND;
        case TIME_EPOCH_NANOS:
          return sourceValue * NANOS_PER_SECOND;
      }
      throw new OpenGammaRuntimeException("Impossible");
    }
  },
  DATE_EPOCH_DAYS {
    private static final long SECONDS_PER_DAY = 3600 * 24;
    private static final long MILLIS_PER_DAY = SECONDS_PER_DAY * 1000;
    private static final long NANOS_PER_DAY = MILLIS_PER_DAY * 1000;
    private final ThreadLocal<Calendar> _calendar = new ThreadLocal<Calendar>() {
      @Override
      protected Calendar initialValue() {
        return Calendar.getInstance();
      }
    };

    @Override
    public DateTimeResolution getResolution() {
      return DateTimeResolution.DATE;
    }

    @Override
    public boolean isIntegerBigEnough() {
      return true;
    }

    @Override
    public long convertToLong(final long sourceValue, final DateTimeNumericEncoding targetEncoding) {
      switch (targetEncoding) {
        case DATE_DDMMYYYY:
          final Calendar calendar = _calendar.get();
          calendar.setTimeInMillis(sourceValue * MILLIS_PER_DAY);
          long value = calendar.get(Calendar.YEAR) * 10000;
          value += (calendar.get(Calendar.MONTH) + 1) * 100;
          value += calendar.get(Calendar.DAY_OF_MONTH);
          return value;
        case DATE_EPOCH_DAYS:
          return sourceValue;
        case TIME_EPOCH_SECONDS:
          return sourceValue * SECONDS_PER_DAY;
        case TIME_EPOCH_MILLIS:
          return sourceValue * MILLIS_PER_DAY;
        case TIME_EPOCH_NANOS:
          return sourceValue * NANOS_PER_DAY;
      }
      throw new OpenGammaRuntimeException("Impossible");
    }
  },
  DATE_DDMMYYYY {
    private static final long MILLIS_PER_DAY = 3600 * 24 * 1000;
    private static final long MILLIS_PER_SECOND = 1000;
    private static final long NANOS_PER_MILLI = 1000;
    private final ThreadLocal<Calendar> _calendar = new ThreadLocal<Calendar>() {
      @Override
      protected Calendar initialValue() {
        return Calendar.getInstance();
      }
    };

    @Override
    public DateTimeResolution getResolution() {
      return DateTimeResolution.DATE;
    }

    @Override
    public boolean isIntegerBigEnough() {
      return true;
    }

    @Override
    public long convertToLong(final long sourceValue, final DateTimeNumericEncoding targetEncoding) {
      if (targetEncoding == DATE_DDMMYYYY) {
        return sourceValue;
      } else {
        final int year = (int) (sourceValue / 100000);
        final int month = (int) ((sourceValue - (year * 100000)) / 100);
        final int day = (int) (sourceValue % 100);
        final Calendar cal = _calendar.get();
        cal.clear();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        final long timeInMillis = cal.getTimeInMillis();
        switch (targetEncoding) {
          case DATE_EPOCH_DAYS:
            return timeInMillis / MILLIS_PER_DAY;
          case TIME_EPOCH_SECONDS:
            return timeInMillis / MILLIS_PER_SECOND;
          case TIME_EPOCH_MILLIS:
            return timeInMillis;
          case TIME_EPOCH_NANOS:
            return timeInMillis * NANOS_PER_MILLI;
        }
        throw new OpenGammaRuntimeException("Impossible");
      }

    }
  };
  public abstract DateTimeResolution getResolution();

  public abstract boolean isIntegerBigEnough();

  public int convertToInt(final int sourceValue, final DateTimeNumericEncoding targetEncoding) {
    if (targetEncoding.isIntegerBigEnough() && this.isIntegerBigEnough()) {
      return (int) convertToLong((long) sourceValue, targetEncoding);
    } else {
      throw new OpenGammaRuntimeException("Source and/or Target encoding doesn't support integers");
    }
  }

  public int convertToInt(final long sourceValue, final DateTimeNumericEncoding targetEncoding) {
    if (targetEncoding.isIntegerBigEnough()) {
      return (int) convertToLong(sourceValue, targetEncoding);
    } else {
      throw new OpenGammaRuntimeException("Target encoding doesn't support integers");
    }
  }

  public long convertToLong(final int sourceValue, final DateTimeNumericEncoding targetEncoding) {
    if (this.isIntegerBigEnough()) {
      return convertToLong((long) sourceValue, targetEncoding);
    } else {
      throw new OpenGammaRuntimeException("Source encoding doesn't support integers");
    }
  }

  public abstract long convertToLong(long sourceValue, DateTimeNumericEncoding targetEncoding);
}
