/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Utility class for building date constraints for the time series fetching functions.
 * <p>
 * Date constraint strings are crude expressions that are evaluated at execution time. This allows the valuation time to be referred to symbolically.
 * <dl>
 * <dt><em>YYYY</em>-</em>MM</em>-<em>DD</em></dt>
 * <dd>The date literal</dd>
 * <dt>Now</dt>
 * <dd>The valuation date</dd>
 * <dt>Null</dt>
 * <dd>Will return <code>null</code> from the {@link #getLocalDate} function</dd>
 * <dt>PreviousWeekDay(<em>expr</em>)</dt>
 * <dd>The previous weekday to the evaluated date constraint expression</dd>
 * <dt>PreviousWeekDay</dt>
 * <dd>The previous weekday to the valuation date. This is equivalent to <code>PreviousWeekDay(NOW)</code></dd>
 * <dt>NextWeekDay(<em>expr</em>)</dt>
 * <dd>The next weekday to the evaluated date constraint expression</dd>
 * <dt><em>expr</em>[+|-]<em>period</em></dt>
 * <dd>The evaluated date constraint expression plus or minus the given period, for example <code>PreviousWeekDay-P7D</code></dd>
 * <dt>-<em>period</em></dt>
 * <dd>The valuation date minus the given period, for example <code>-P1D</code> for the previous day. This is equivalent to <code>NOW-<em>period</em></code></dd>
 * </dl>
 */
public abstract class DateConstraint {

  private static final String NOW_STRING = "Now";

  private static final String NULL_STRING = "Null";

  private static final String PREVIOUS_WEEK_DAY_STRING = "PreviousWeekDay";

  private static final String NEXT_WEEK_DAY_STRING = "NextWeekDay";

  /**
   * Constant for the "null" date constraint. Some of the time series APIs use null to mean the earliest available date.
   */
  public static final DateConstraint NULL = new NullDateConstraint();
  /**
   * Date constraint referring to the current valuation time.
   */
  public static final DateConstraint VALUATION_TIME = new ValuationTime();

  /* package */DateConstraint() {
  }

  public static DateConstraint of(final LocalDate date) {
    ArgumentChecker.notNull(date, "date");
    return new LiteralDateConstraint(date);
  }

  /**
   * Returns a date constraint that corresponds to the weekday before this one.
   * 
   * @return the new date constraint
   */
  public DateConstraint previousWeekDay() {
    return new WeekDayDateConstraint(this, -1);
  }

  /**
   * Returns a date constraint that corresponds to the weekday after this one.
   * 
   * @return the new date constraint
   */
  public DateConstraint nextWeekDay() {
    return new WeekDayDateConstraint(this, 1);
  }

  /**
   * Returns a date constraint that corresponds to this one plus the given period.
   * 
   * @param period the period to add, not null
   * @return the new date constraint
   */
  public DateConstraint plus(Period period) {
    return new PlusMinusPeriodDateConstraint(this, true, period);
  }

  /**
   * Returns a date constraint that corresponds to this one minus the given period.
   * 
   * @param period the period to subtract, not null
   * @return the new date constraint
   */
  public DateConstraint minus(Period period) {
    return new PlusMinusPeriodDateConstraint(this, false, period);
  }

  /**
   * Returns a date constraint that corresponds to this one minus the given period.
   * 
   * @param period the period to subtract, not null
   * @return the new date constraint
   */
  public DateConstraint minus(String period) {
    return minus(Period.parse(period));
  }

  @Override
  public abstract String toString();

  private static final class NullDateConstraint extends DateConstraint {

    @Override
    public DateConstraint previousWeekDay() {
      throw new UnsupportedOperationException("Can't take previous week day from NULL");
    }

    @Override
    public DateConstraint plus(final Period period) {
      throw new UnsupportedOperationException("Can't add a period to NULL");
    }

    @Override
    public DateConstraint minus(final Period period) {
      throw new UnsupportedOperationException("Can't subtract a period from NULL");
    }

    @Override
    public String toString() {
      return NULL_STRING;
    }

  }

  private static final class LiteralDateConstraint extends DateConstraint {

    private final LocalDate _value;

    public LiteralDateConstraint(final LocalDate value) {
      _value = value;
    }

    @Override
    public String toString() {
      return _value.toString();
    }

  }

  private static final class PlusMinusPeriodDateConstraint extends DateConstraint {

    private final DateConstraint _underlying;
    private final boolean _plus;
    private final Period _period;

    public PlusMinusPeriodDateConstraint(final DateConstraint underlying, final boolean plus, final Period period) {
      _underlying = underlying;
      _plus = plus;
      _period = period;
    }

    @Override
    public DateConstraint plus(final Period period) {
      final Period newPeriod;
      if (_plus) {
        newPeriod = _period.plus(period);
      } else {
        newPeriod = _period.minus(period);
      }
      if (newPeriod.isZero()) {
        if (_underlying != null) {
          return _underlying;
        } else {
          return VALUATION_TIME;
        }
      } else {
        return new PlusMinusPeriodDateConstraint(_underlying, _plus, newPeriod);
      }
    }

    @Override
    public DateConstraint minus(final Period period) {
      final Period newPeriod;
      if (_plus) {
        newPeriod = _period.minus(period);
      } else {
        newPeriod = _period.plus(period);
      }
      if (newPeriod.isZero()) {
        if (_underlying != null) {
          return _underlying;
        } else {
          return VALUATION_TIME;
        }
      } else {
        return new PlusMinusPeriodDateConstraint(_underlying, _plus, newPeriod);
      }
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      if (_underlying != null) {
        sb.append(_underlying);
      }
      sb.append(_plus ? '+' : '-');
      sb.append(_period);
      return sb.toString();
    }

  }

  private static final class WeekDayDateConstraint extends DateConstraint {

    private final DateConstraint _underlying;
    private final int _adjust;

    public WeekDayDateConstraint(final DateConstraint underlying, int adjust) {
      _underlying = underlying;
      _adjust = adjust;
    }

    @Override
    public DateConstraint previousWeekDay() {
      if (_adjust == 1) {
        if (_underlying != null) {
          return _underlying;
        } else {
          return VALUATION_TIME;
        }
      } else {
        return new WeekDayDateConstraint(_underlying, _adjust - 1);
      }
    }

    @Override
    public DateConstraint nextWeekDay() {
      if (_adjust == -1) {
        if (_underlying != null) {
          return _underlying;
        } else {
          return VALUATION_TIME;
        }
      } else {
        return new WeekDayDateConstraint(_underlying, _adjust + 1);
      }
    }

    private String expr(final int adjust, final String str) {
      if (_adjust == 1) {
        if (_underlying != null) {
          return str + "(" + _underlying + ")";
        } else {
          return str;
        }
      } else {
        return str + "(" + expr(adjust - 1, str) + ")";
      }
    }

    @Override
    public String toString() {
      if (_adjust < 0) {
        return expr(-_adjust, PREVIOUS_WEEK_DAY_STRING);
      } else {
        return expr(_adjust, NEXT_WEEK_DAY_STRING);
      }
    }

  }

  private static final class ValuationTime extends DateConstraint {

    @Override
    public DateConstraint previousWeekDay() {
      return new WeekDayDateConstraint(null, -1);
    }

    @Override
    public DateConstraint nextWeekDay() {
      return new WeekDayDateConstraint(null, 1);
    }

    @Override
    public DateConstraint plus(final Period period) {
      return new PlusMinusPeriodDateConstraint(null, true, period);
    }

    @Override
    public DateConstraint minus(final Period period) {
      return new PlusMinusPeriodDateConstraint(null, false, period);
    }

    @Override
    public String toString() {
      return NOW_STRING;
    }

  }

  private static Pair<String, String> parseBrackets(final String str) {
    if (str.length() == 0) {
      return Pair.of(null, null);
    } else if (str.charAt(0) == '(') {
      int index = 1;
      int count = 1;
      do {
        switch (str.charAt(index++)) {
          case '(':
            count++;
            break;
          case ')':
            count--;
            break;
        }
      } while (count > 0);
      final String bracketExpr = str.substring(1, index - 1);
      if (index == str.length()) {
        return Pair.of(bracketExpr, null);
      } else {
        return Pair.of(bracketExpr, str.substring(index));
      }
    } else {
      return Pair.of(null, str);
    }
  }

  private static DateConstraint parseRight(final DateConstraint left, final String str) {
    if (str.charAt(0) == '-') {
      return left.minus(Period.parse(str.substring(1)));
    } else if (str.charAt(0) == '+') {
      return left.plus(Period.parse(str.substring(1)));
    } else {
      throw new IllegalArgumentException("Can't parse tail expression " + str + " of " + left);
    }
  }

  private static LocalDate evaluateRight(final LocalDate left, final String str) {
    if (str.charAt(0) == '-') {
      return left.minus(Period.parse(str.substring(1)));
    } else if (str.charAt(0) == '+') {
      return left.plus(Period.parse(str.substring(1)));
    } else {
      throw new IllegalArgumentException("Can't parse tail expression " + str + " of " + left);
    }
  }

  /**
   * Basic parsing of a date constraint string to a {@link DateConstraint} object.
   * <p>
   * This is not a full parser for the syntax described above. For example, expressions such as <code>-P7D-P7D</code> will not be recognized. Such expressions will not however be constructed using the
   * classes above (it would produce <code>-P14D</code>).
   * 
   * @param str the string to parse, not null
   * @return the parsed constraint or null if the empty string is given
   */
  public static DateConstraint parse(final String str) {
    if (str.length() == 0) {
      return null;
    }
    if (str.startsWith(NOW_STRING)) {
      return parseRight(VALUATION_TIME, str.substring(NOW_STRING.length()));
    } else if (str.startsWith(NULL_STRING)) {
      return null;
    } else if (str.charAt(0) == '-') {
      return VALUATION_TIME.minus(Period.parse(str.substring(1)));
    } else if (str.charAt(0) == '+') {
      return VALUATION_TIME.plus(Period.parse(str.substring(1)));
    } else if (str.startsWith(PREVIOUS_WEEK_DAY_STRING)) {
      final Pair<String, String> brackets = parseBrackets(str.substring(PREVIOUS_WEEK_DAY_STRING.length()));
      final DateConstraint left;
      if (brackets.getFirst() != null) {
        left = parse(brackets.getFirst()).previousWeekDay();
      } else {
        left = VALUATION_TIME.previousWeekDay();
      }
      if (brackets.getSecond() != null) {
        return parseRight(left, brackets.getSecond());
      } else {
        return left;
      }
    } else if (str.startsWith(NEXT_WEEK_DAY_STRING)) {
      final Pair<String, String> brackets = parseBrackets(str.substring(NEXT_WEEK_DAY_STRING.length()));
      final DateConstraint left;
      if (brackets.getFirst() != null) {
        left = parse(brackets.getFirst()).nextWeekDay();
      } else {
        left = VALUATION_TIME.nextWeekDay();
      }
      if (brackets.getSecond() != null) {
        return parseRight(left, brackets.getSecond());
      } else {
        return left;
      }
    } else {
      return new LiteralDateConstraint(LocalDate.parse(str));
    }
  }

  /**
   * Evaluates a date constraint expression with respect to the information in the execution context such as the valuation time.
   * <p>
   * This is more efficient than parsing and evaluating the {@link DateConstraint} object structures as two separate steps.
   * 
   * @param context the execution context, not null
   * @param str the string to parse and evaluate
   * @return the evaluated local date, possibly null
   */
  public static LocalDate evaluate(final FunctionExecutionContext context, final String str) {
    if (str.length() == 0) {
      return null;
    }
    if (str.startsWith(NOW_STRING)) {
      if (str.length() == NOW_STRING.length()) {
        return valuationTime(context);
      } else {
        return evaluateRight(valuationTime(context), str.substring(NOW_STRING.length()));
      }
    } else if (str.startsWith(NULL_STRING)) {
      return null;
    } else if (str.charAt(0) == '-') {
      return valuationTime(context).minus(Period.parse(str.substring(1)));
    } else if (str.charAt(0) == '+') {
      return valuationTime(context).plus(Period.parse(str.substring(1)));
    } else if (str.startsWith(PREVIOUS_WEEK_DAY_STRING)) {
      final Pair<String, String> brackets = parseBrackets(str.substring(PREVIOUS_WEEK_DAY_STRING.length()));
      final LocalDate left;
      if (brackets.getFirst() != null) {
        left = DateUtils.previousWeekDay(evaluate(context, brackets.getFirst()));
      } else {
        left = DateUtils.previousWeekDay(valuationTime(context));
      }
      if (brackets.getSecond() != null) {
        return evaluateRight(left, brackets.getSecond());
      } else {
        return left;
      }
    } else if (str.startsWith(NEXT_WEEK_DAY_STRING)) {
      final Pair<String, String> brackets = parseBrackets(str.substring(NEXT_WEEK_DAY_STRING.length()));
      final LocalDate left;
      if (brackets.getFirst() != null) {
        left = DateUtils.nextWeekDay(evaluate(context, brackets.getFirst()));
      } else {
        left = DateUtils.nextWeekDay(valuationTime(context));
      }
      if (brackets.getSecond() != null) {
        return evaluateRight(left, brackets.getSecond());
      } else {
        return left;
      }
    } else {
      return LocalDate.parse(str);
    }
  }

  private static LocalDate valuationTime(final FunctionExecutionContext context) {
    return LocalDate.now(context.getValuationClock());
  }

}
