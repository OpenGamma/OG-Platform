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
    ArgumentChecker.isTrue(instrumentType == StripInstrumentType.SWAP || instrumentType == StripInstrumentType.OIS_SWAP, "Strip type for this constructor must be a swap or OIS");
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
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the instrument type used to construct this strip.
   * 
   * @return the instrument type, not null
   */
  public StripInstrumentType getInstrumentType() {
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
    return new Tenor(getInstrumentType() == StripInstrumentType.FUTURE ?
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
   * Gets the reset tenor for a basis swap
   * @return The receive tenor
   */
  public Tenor getResetTenor() {
    if (_instrumentType != StripInstrumentType.SWAP && _instrumentType != StripInstrumentType.OIS_SWAP) {
      throw new IllegalStateException("Cannot get the receive tenor for an instrument that is not a swap or OIS; have " + toString());
    }
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
   * Gets the receive tenor for a basis swap
   * @return The receive tenor
   */
  public IndexType getIndexType() {
    if (_instrumentType != StripInstrumentType.SWAP && _instrumentType != StripInstrumentType.OIS_SWAP) {
      throw new IllegalStateException("Cannot get the index type for an instrument that is not a swap or OIS; have " + toString());
    }
    return _indexType;
  }

  //-------------------------------------------------------------------------
  @Override
  public int compareTo(final FixedIncomeStrip other) {
    int result = getEffectiveTenor().getPeriod().toPeriodFields().toEstimatedDuration().compareTo(other.getEffectiveTenor().getPeriod().toPeriodFields().toEstimatedDuration());
    if (result != 0) {
      return result;
    }
    result = getInstrumentType().ordinal() - other.getInstrumentType().ordinal();
    if (result != 0) {
      return result;
    } else if (getInstrumentType() == StripInstrumentType.FUTURE) {
      result = getNumberOfFuturesAfterTenor() - other.getNumberOfFuturesAfterTenor();
    } else if (getInstrumentType() == StripInstrumentType.PERIODIC_ZERO_DEPOSIT) {
      result = getPeriodsPerYear() - other.getPeriodsPerYear();
    } else if (getInstrumentType() == StripInstrumentType.SWAP || getInstrumentType() == StripInstrumentType.OIS_SWAP && getIndexType() != null) {
      result = getResetTenor().compareTo(other.getResetTenor());
      if (result != 0) {
        return result;
      }
      return getIndexType().compareTo(other.getIndexType());
    } else if (getInstrumentType() == StripInstrumentType.BASIS_SWAP) {
      result = getPayTenor().compareTo(other.getPayTenor());
      if (result != 0) {
        return result;
      }
      result = getReceiveTenor().compareTo(other.getReceiveTenor());
      if (result != 0) {
        return result;
      }
      result = getPayIndexType().ordinal() - other.getPayIndexType().ordinal();
      if (result != 0) {
        return result;
      }
      result = getReceiveIndexType().ordinal() - other.getReceiveIndexType().ordinal();
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
