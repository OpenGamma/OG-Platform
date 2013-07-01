/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.fudgemsg.FixedIncomeStripFudgeBuilder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

/**
 * A fixed income strip. <b>Note that the futures are assumed to be quarterly.</b>
 */
public class FixedIncomeStrip implements Serializable, Comparable<FixedIncomeStrip> {

  private static final long serialVersionUID = 1L;

  private final StripInstrumentType _instrumentType;
  private final Tenor _curveNodePointTime;
  private final String _conventionName;
  private final int _nthFutureFromTenor;
  private final int _periodsPerYear;
  private final Tenor _payTenor;
  private final Tenor _receiveTenor;
  private final Tenor _resetTenor;
  private final IndexType _payIndexType;
  private final IndexType _receiveIndexType;
  private final IndexType _indexType;
  private final FixedIncomeStrip _strip1;
  private final FixedIncomeStrip _strip2;
  private final OperationType _operation;
  /**
   * Creates a strip for non-future and non-basis swap instruments.
   *
   * @param instrumentType  the instrument type
   * @param curveNodePointTime  the time of the curve node point
   * @param conventionName  the name of the yield curve specification builder configuration
   */
  public FixedIncomeStrip(final StripInstrumentType instrumentType, final Tenor curveNodePointTime, final String conventionName) {
    ArgumentChecker.notNull(instrumentType, "InstrumentType");
    ArgumentChecker.isTrue(instrumentType != StripInstrumentType.FUTURE, "Cannot handle futures in this constructor");
    ArgumentChecker.isTrue(instrumentType != StripInstrumentType.PERIODIC_ZERO_DEPOSIT, "Cannot handle periodic zero deposits in this constructor");
    ArgumentChecker.isTrue(instrumentType != StripInstrumentType.BASIS_SWAP, "Cannot handle basis swaps in this constructor");
    ArgumentChecker.notNull(curveNodePointTime, "Tenor");
    ArgumentChecker.notNull(conventionName, "ConventionName");
    _instrumentType = instrumentType;
    _curveNodePointTime = curveNodePointTime;
    _nthFutureFromTenor = 0;
    _periodsPerYear = 0;
    _conventionName = conventionName;
    _payTenor = null;
    _receiveTenor = null;
    _payIndexType = null;
    _receiveIndexType = null;
    _resetTenor = null;
    _indexType = null;
    _strip1 = null;
    _strip2 = null;
    _operation = null;
  }

  /**
   * Creates a future strip.
   *
   * @param instrumentType  the instrument type
   * @param curveNodePointTime  the time of the curve node point
   * @param conventionName  the name of the convention to use to resolve the strip into a security
   * @param nthFutureFromTenor  how many futures to step through from the curveDate + the tenor. 1-based, must be >0.
   *   e.g. 3 (tenor = 1YR) => 3rd quarterly future after curveDate +  1YR.
   */
  public FixedIncomeStrip(final StripInstrumentType instrumentType, final Tenor curveNodePointTime, final int nthFutureFromTenor, final String conventionName) {
    ArgumentChecker.isTrue(instrumentType == StripInstrumentType.FUTURE, "Strip type for this constructor must be a future");
    ArgumentChecker.notNull(curveNodePointTime, "Tenor");
    ArgumentChecker.isTrue(nthFutureFromTenor > 0, "Number of future must be greater than zero");
    ArgumentChecker.notNull(conventionName, "ConventionName");
    _instrumentType = instrumentType;
    _curveNodePointTime = curveNodePointTime;
    _nthFutureFromTenor = nthFutureFromTenor;
    _conventionName = conventionName;
    _periodsPerYear = 0;
    _payTenor = null;
    _receiveTenor = null;
    _payIndexType = null;
    _receiveIndexType = null;
    _resetTenor = null;
    _indexType = null;
    _strip1 = null;
    _strip2 = null;
    _operation = null;
  }

  /**
   * Creates a zero deposit strip
   * @param instrumentType The instrument type
   * @param curveNodePointTime The time of the curve node point
   * @param periodsPerYear The number of periods per year
   * @param isPeriodicZeroDepositStrip Is this instrument a periodic zero deposit strip
   * @param conventionName The name of the convention to use to resolve the strip into a security
   */
  public FixedIncomeStrip(final StripInstrumentType instrumentType, final Tenor curveNodePointTime, final int periodsPerYear, final boolean isPeriodicZeroDepositStrip,
      final String conventionName) {
    ArgumentChecker.isTrue(instrumentType == StripInstrumentType.PERIODIC_ZERO_DEPOSIT, "Strip type for this constructor must be a periodic zero deposit");
    ArgumentChecker.isTrue(isPeriodicZeroDepositStrip, "Must have flag indicating periodic zero deposit set to true");
    ArgumentChecker.notNull(curveNodePointTime, "Tenor");
    ArgumentChecker.isTrue(periodsPerYear > 0, "Number of periods per year must be greater than zero");
    ArgumentChecker.notNull(conventionName, "ConventionName");
    _instrumentType = instrumentType;
    _curveNodePointTime = curveNodePointTime;
    _periodsPerYear = periodsPerYear;
    _nthFutureFromTenor = 0;
    _conventionName = conventionName;
    _payTenor = null;
    _receiveTenor = null;
    _payIndexType = null;
    _receiveIndexType = null;
    _resetTenor = null;
    _indexType = null;
    _strip1 = null;
    _strip2 = null;
    _operation = null;
  }

  /**
   * Creates a basis swap strip where the two legs are on the same type of index (e.g. a USD 3M Fed Funds / 6M Libor swap)
   * @param instrumentType The instrument type
   * @param curveNodePointTime The time of the curve node point
   * @param payTenor The pay tenor
   * @param receiveTenor The receive tenor
   * @param payIndexType The pay index type
   * @param receiveIndexType The receive index type
   * @param conventionName The name of the convention to use to resolve the strip into a security
   */
  public FixedIncomeStrip(final StripInstrumentType instrumentType, final Tenor curveNodePointTime, final Tenor payTenor, final Tenor receiveTenor,
      final IndexType payIndexType, final IndexType receiveIndexType, final String conventionName) {
    ArgumentChecker.isTrue(instrumentType == StripInstrumentType.BASIS_SWAP, "Strip type for this constructor must be a basis swap");
    ArgumentChecker.notNull(curveNodePointTime, "curve node tenor");
    ArgumentChecker.notNull(payTenor, "pay tenor");
    ArgumentChecker.notNull(receiveTenor, "receive tenor");
    ArgumentChecker.notNull(conventionName, "convention name");
    _instrumentType = instrumentType;
    _curveNodePointTime = curveNodePointTime;
    _periodsPerYear = 0;
    _nthFutureFromTenor = 0;
    _conventionName = conventionName;
    _payTenor = payTenor;
    _receiveTenor = receiveTenor;
    _payIndexType = payIndexType;
    _receiveIndexType = receiveIndexType;
    _resetTenor = null;
    _indexType = null;
    _strip1 = null;
    _strip2 = null;
    _operation = null;
  }

  /**
   * Creates a basis swap strip where the two legs are on the same type of index (e.g. a USD 3M Fed Funds / 6M Libor swap)
   * @param instrumentType The instrument type
   * @param curveNodePointTime The time of the curve node point
   * @param resetTenor The reset tenor
   * @param indexType The index type
   * @param conventionName The name of the convention to use to resolve the strip into a security
   */
  public FixedIncomeStrip(final StripInstrumentType instrumentType, final Tenor curveNodePointTime, final Tenor resetTenor,
      final IndexType indexType, final String conventionName) {
    ArgumentChecker.notNull(curveNodePointTime, "curve node tenor");
    ArgumentChecker.notNull(resetTenor, "reset tenor");
    ArgumentChecker.notNull(conventionName, "convention name");
    _instrumentType = instrumentType;
    _curveNodePointTime = curveNodePointTime;
    _periodsPerYear = 0;
    _nthFutureFromTenor = 0;
    _conventionName = conventionName;
    _resetTenor = resetTenor;
    _indexType = indexType;
    _payTenor = null;
    _receiveTenor = null;
    _payIndexType = null;
    _receiveIndexType = null;
    _strip1 = null;
    _strip2 = null;
    _operation = null;
  }

  public FixedIncomeStrip(final FixedIncomeStrip strip1, final FixedIncomeStrip strip2, final OperationType operation, final Tenor curveNodePointTime,
      final String conventionName) {
    ArgumentChecker.notNull(strip1, "strip 1");
    ArgumentChecker.notNull(strip2, "strip 2");
    ArgumentChecker.notNull(operation, "operation");
    ArgumentChecker.notNull(curveNodePointTime, "curve node point time");
    ArgumentChecker.notNull(conventionName, "convention name");
    _strip1 = strip1;
    _strip2 = strip2;
    _operation = operation;
    _curveNodePointTime = curveNodePointTime;
    _conventionName = conventionName;
    _instrumentType = StripInstrumentType.SPREAD;
    _periodsPerYear = 0;
    _nthFutureFromTenor = 0;
    _payTenor = null;
    _receiveTenor = null;
    _payIndexType = null;
    _receiveIndexType = null;
    _resetTenor = null;
    _indexType = null;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the instrument type used to construct this strip.
   *
   * @return the instrument type, not null
   */
  public StripInstrumentType getInstrumentType() {
    if (_strip1 != null) {
      throw new IllegalStateException("Cannot get strip instrument type for a spread strip " + toString());
    }
    return _instrumentType;
  }

  /**
   * Gets the curve node point in time.
   *
   * @return a tenor representing the time of the curve node point, not null
   */
  public Tenor getCurveNodePointTime() {
    return _curveNodePointTime;
  }

  /**
   * Get the number of the quarterly IR futures after the tenor to choose.
   * NOTE: THIS DOESN'T REFER TO A GENERIC FUTURE
   *
   * @return the number of futures after the tenor
   * @throws IllegalStateException if called on a non-future strip
   */
  public int getNumberOfFuturesAfterTenor() {
    if (_instrumentType != StripInstrumentType.FUTURE) {
      throw new IllegalStateException("Cannot get number of futures after tenor for a non-future strip " + toString());
    }
    return _nthFutureFromTenor;
  }

  /**
   * Get the periods per year of a periodic zero deposit security
   *
   * @return the number of periods per year
   * @throws IllegalStateException if called on a non-periodic zero deposit strip
   */
  public int getPeriodsPerYear() {
    if (_instrumentType != StripInstrumentType.PERIODIC_ZERO_DEPOSIT) {
      throw new IllegalStateException("Cannot get number of periods per year for a non-periodic zero deposit strip " + toString());
    }
    return _periodsPerYear;
  }

  /**
   * Gets the name of the convention used to resolve this strip definition into a security.
   *
   * @return the name, not null
   */
  public String getConventionName() {
    return _conventionName;
  }

  /**
   * Calculates the tenor of a strip. For all instruments except futures, this is the same as that entered on construction.
   * For futures, this is the start tenor + (3 * future number)
   * @return The effective tenor of the strip
   */
  public Tenor getEffectiveTenor() {
    return Tenor.of(getInstrumentType() == StripInstrumentType.FUTURE ?
        getCurveNodePointTime().getPeriod().plusMonths(3 * getNumberOfFuturesAfterTenor()) : getCurveNodePointTime().getPeriod());
  }

  /**
   * Gets the pay tenor for a basis swap
   * @return The pay tenor
   */
  public Tenor getPayTenor() {
    if (_instrumentType != StripInstrumentType.BASIS_SWAP) {
      throw new IllegalStateException("Cannot get the pay tenor for an instrument that is not a basis swap; have " + toString());
    }
    return _payTenor;
  }

  /**
   * Gets the receive tenor for a basis swap
   * @return The receive tenor
   */
  public Tenor getReceiveTenor() {
    if (_instrumentType != StripInstrumentType.BASIS_SWAP) {
      throw new IllegalStateException("Cannot get the receive tenor for an instrument that is not a basis swap; have " + toString());
    }
    return _receiveTenor;
  }

  /**
   * Gets the reset tenor.
   * @return The reset tenor
   */
  public Tenor getResetTenor() {
    return _resetTenor;
  }

  /**
   * Gets the pay index type for a basis swap
   * @return The pay index type
   */
  public IndexType getPayIndexType() {
    if (_instrumentType != StripInstrumentType.BASIS_SWAP) {
      throw new IllegalStateException("Cannot get the pay index type for an instrument that is not a basis swap; have " + toString());
    }
    return _payIndexType;
  }

  /**
   * Gets the receive tenor for a basis swap
   * @return The receive tenor
   */
  public IndexType getReceiveIndexType() {
    if (_instrumentType != StripInstrumentType.BASIS_SWAP) {
      throw new IllegalStateException("Cannot get the receive index type for an instrument that is not a basis swap; have " + toString());
    }
    return _receiveIndexType;
  }

  /**
   * Gets the index type.
   * @return The receive tenor
   */
  public IndexType getIndexType() {
    return _indexType;
  }

  public FixedIncomeStrip getStrip1() {
    if (_instrumentType != StripInstrumentType.SPREAD) {
      throw new IllegalStateException("Cannot get the first strip for an instrument that is not a spread strip " + toString());
    }
    return _strip1;
  }

  public FixedIncomeStrip getStrip2() {
    if (_instrumentType != StripInstrumentType.SPREAD) {
      throw new IllegalStateException("Cannot get the second strip for an instrument that is not a spread strip " + toString());
    }
    return _strip2;
  }

  public OperationType getOperation() {
    if (_instrumentType != StripInstrumentType.SPREAD) {
      throw new IllegalStateException("Cannot get the operation for an instrument that is not a spread strip " + toString());
    }
    return _operation;
  }

  //-------------------------------------------------------------------------
  @Override
  public int compareTo(final FixedIncomeStrip other) {
    int result = DateUtils.estimatedDuration(getEffectiveTenor().getPeriod()).compareTo(DateUtils.estimatedDuration(other.getEffectiveTenor().getPeriod()));
    if (result != 0) {
      return result;
    }
    if (_instrumentType == StripInstrumentType.SPREAD) {
      result = getStrip1().compareTo(other.getStrip1());
      if (result != 0) {
        return result;
      }
      result = getStrip2().compareTo(other.getStrip2());
      if (result != 0) {
        return result;
      }
      return getOperation().ordinal() - other.getOperation().ordinal();
    }
    result = getInstrumentType().ordinal() - other.getInstrumentType().ordinal();
    if (result != 0) {
      return result;
    } else if (getInstrumentType() == StripInstrumentType.FUTURE) {
      result = getNumberOfFuturesAfterTenor() - other.getNumberOfFuturesAfterTenor();
    } else if (getInstrumentType() == StripInstrumentType.PERIODIC_ZERO_DEPOSIT) {
      result = getPeriodsPerYear() - other.getPeriodsPerYear();
    } else if (getInstrumentType() == StripInstrumentType.SWAP || getInstrumentType() == StripInstrumentType.OIS_SWAP && getIndexType() != null) {
      result = ObjectUtils.compare(getResetTenor(), other.getResetTenor());
      if (result != 0) {
        return result;
      }
      return ObjectUtils.compare(getIndexType(), other.getIndexType());
    } else if (getInstrumentType() == StripInstrumentType.BASIS_SWAP) {
      result = getPayTenor().compareTo(other.getPayTenor());
      if (result != 0) {
        return result;
      }
      result = ObjectUtils.compare(getReceiveTenor(), other.getReceiveTenor());
      if (result != 0) {
        return result;
      }
      result = ObjectUtils.compare(getPayIndexType(), other.getPayIndexType());
      if (result != 0) {
        return result;
      }
      result = ObjectUtils.compare(getReceiveIndexType(), other.getReceiveIndexType());
    }
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof FixedIncomeStrip) {
      final FixedIncomeStrip other = (FixedIncomeStrip) obj;
      if (_instrumentType == StripInstrumentType.SPREAD) {
        return ObjectUtils.equals(_curveNodePointTime, other._curveNodePointTime) &&
            ObjectUtils.equals(_conventionName, other._conventionName) &&
            ObjectUtils.equals(_strip1, other._strip1) &&
            ObjectUtils.equals(_strip2, other._strip2) &&
            _operation == other._operation;
      }
      final boolean result = ObjectUtils.equals(_curveNodePointTime, other._curveNodePointTime) &&
          ObjectUtils.equals(_conventionName, other._conventionName) &&
          _instrumentType == other._instrumentType;
      if (getInstrumentType() == StripInstrumentType.FUTURE) {
        return result && _nthFutureFromTenor == other._nthFutureFromTenor;
      }
      if (getInstrumentType() == StripInstrumentType.PERIODIC_ZERO_DEPOSIT) {
        return result && _periodsPerYear == other._periodsPerYear;
      }
      if (getInstrumentType() == StripInstrumentType.SWAP || getInstrumentType() == StripInstrumentType.OIS_SWAP && getIndexType() != null) {
        return result &&
            ObjectUtils.equals(getResetTenor(), other.getResetTenor()) &&
            ObjectUtils.equals(getIndexType(), other.getIndexType());
      }
      if (getInstrumentType() == StripInstrumentType.BASIS_SWAP) {
        return result &&
            ObjectUtils.equals(getPayTenor(), other.getPayTenor()) &&
            ObjectUtils.equals(getReceiveTenor(), other.getReceiveTenor()) &&
            getPayIndexType() == other.getPayIndexType() &&
            getReceiveIndexType() == other.getReceiveIndexType();
      }
      return result;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _curveNodePointTime.hashCode();
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("FixedIncomeStrip[");
    if (_instrumentType == StripInstrumentType.SPREAD) {
      sb.append(_strip1.toString());
      sb.append(_operation.getSymbol());
      sb.append(_strip2.toString());
      sb.append("]");
      return sb.toString();
    }
    sb.append("instrument type=");
    sb.append(getInstrumentType());
    sb.append(", ");
    sb.append("tenor=");
    sb.append(getCurveNodePointTime());
    sb.append(", ");
    if (getInstrumentType() == StripInstrumentType.FUTURE) {
      sb.append("future number after tenor=");
      sb.append(getNumberOfFuturesAfterTenor());
      sb.append(", ");
    } else if (getInstrumentType() == StripInstrumentType.PERIODIC_ZERO_DEPOSIT) {
      sb.append("periods per year=");
      sb.append(getPeriodsPerYear());
      sb.append(", ");
    } else if (getInstrumentType() == StripInstrumentType.SWAP || getInstrumentType() == StripInstrumentType.OIS_SWAP && getIndexType() != null) {
      sb.append("reset tenor=");
      sb.append(getResetTenor());
      sb.append(" on ");
      sb.append(getIndexType());
      sb.append(", ");
    } else if (getInstrumentType() == StripInstrumentType.BASIS_SWAP) {
      sb.append("pay tenor=");
      sb.append(getPayTenor());
      sb.append(" on ");
      sb.append(getPayIndexType());
      sb.append(", receive tenor=");
      sb.append(getReceiveTenor());
      sb.append(" on ");
      sb.append(getReceiveIndexType());
      sb.append(", ");
    }
    sb.append("convention name=");
    sb.append(getConventionName());
    sb.append("]");
    return sb.toString();
  }

  //-------------------------------------------------------------------------
  // REVIEW: jim 22-Aug-2010 -- get rid of these and use the builder directly
  public void toFudgeMsg(final FudgeSerializer serializer, final MutableFudgeMsg message) {
    final FixedIncomeStripFudgeBuilder builder = new FixedIncomeStripFudgeBuilder();
    final MutableFudgeMsg container = builder.buildMessage(serializer, this);
    for (final FudgeField field : container.getAllFields()) {
      message.add(field);
    }
  }

  // REVIEW: jim 22-Aug-2010 -- get rid of these and use the builder directly
  public static FixedIncomeStrip fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final FixedIncomeStripFudgeBuilder builder = new FixedIncomeStripFudgeBuilder();
    return builder.buildObject(deserializer, message);
  }

}
