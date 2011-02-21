/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.types.PrimitiveFieldTypes;
import org.springframework.util.ObjectUtils;

import com.opengamma.core.common.CurrencyUnit;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.ArgumentChecker;

/**
 * Represents a entry within a {@link CurrencyMatrix}.
 */
public abstract class CurrencyMatrixValue {

  /**
   * A fixed conversion rate from one currency to another.
   */
  public static final class CurrencyMatrixFixed extends CurrencyMatrixValue {

    private final double _fixedValue;

    private CurrencyMatrixFixed(final double fixedValue) {
      _fixedValue = fixedValue;
    }

    @Override
    public <T> T accept(final CurrencyMatrixValueVisitor<T> visitor) {
      return visitor.visitFixed(this);
    }

    public double getFixedValue() {
      return _fixedValue;
    }

    @Override
    public CurrencyMatrixFixed getReciprocal() {
      if (getFixedValue() == 1.0) {
        return this;
      } else {
        return new CurrencyMatrixFixed(1 / getFixedValue());
      }
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof CurrencyMatrixFixed)) {
        return false;
      }
      return ((CurrencyMatrixFixed) o).getFixedValue() == getFixedValue();
    }

    @Override
    public int hashCode() {
      return ObjectUtils.hashCode(getFixedValue());
    }

    @Override
    public String toString() {
      return "CurrencyMatrixValue[" + getFixedValue() + "]";
    }

  }

  /**
   * A conversion rate from one currency to another supplied by another node in the dependency
   * graph. This might be a live data sourcing function to use current market data, or a
   * calculated value based on something else.
   */
  public static final class CurrencyMatrixValueRequirement extends CurrencyMatrixValue {

    private final ValueRequirement _valueRequirement;
    private final boolean _reciprocal;

    private CurrencyMatrixValueRequirement(final ValueRequirement valueRequirement, boolean reciprocal) {
      _valueRequirement = valueRequirement;
      _reciprocal = reciprocal;
    }

    @Override
    public <T> T accept(final CurrencyMatrixValueVisitor<T> visitor) {
      return visitor.visitValueRequirement(this);
    }

    public ValueRequirement getValueRequirement() {
      return _valueRequirement;
    }

    public boolean isReciprocal() {
      return _reciprocal;
    }

    @Override
    public CurrencyMatrixValueRequirement getReciprocal() {
      return new CurrencyMatrixValueRequirement(getValueRequirement(), !isReciprocal());
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof CurrencyMatrixValueRequirement)) {
        return false;
      }
      final CurrencyMatrixValueRequirement oc = (CurrencyMatrixValueRequirement) o;
      return getValueRequirement().equals(oc.getValueRequirement()) && (isReciprocal() == oc.isReciprocal());
    }

    @Override
    public int hashCode() {
      return getValueRequirement().hashCode() * 17 + ObjectUtils.hashCode(isReciprocal());
    }

    public MutableFudgeFieldContainer toFudgeMsg(final FudgeSerializationContext context) {
      final MutableFudgeFieldContainer msg = context.objectToFudgeMsg(getValueRequirement());
      msg.add("reciprocal", null, PrimitiveFieldTypes.BOOLEAN_TYPE, isReciprocal());
      return msg;
    }

    public static CurrencyMatrixValueRequirement fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer msg) {
      return new CurrencyMatrixValueRequirement(context.fudgeMsgToObject(ValueRequirement.class, msg), msg.getBoolean("reciprocal"));
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("CurrencyMatrixValue[");
      sb.append(getValueRequirement().toString());
      if (isReciprocal()) {
        sb.append(" ^-1");
      }
      return sb.append("]").toString();
    }

  }

  /**
   * A conversion rate from one currency to another via an intermediate currency.
   */
  public static final class CurrencyMatrixCross extends CurrencyMatrixValue {

    private final CurrencyUnit _crossCurrency;

    private CurrencyMatrixCross(final CurrencyUnit crossCurrency) {
      _crossCurrency = crossCurrency;
    }

    @Override
    public <T> T accept(final CurrencyMatrixValueVisitor<T> visitor) {
      return visitor.visitCross(this);
    }

    public CurrencyUnit getCrossCurrency() {
      return _crossCurrency;
    }

    @Override
    public CurrencyMatrixCross getReciprocal() {
      return this;
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof CurrencyMatrixCross)) {
        return false;
      }
      return getCrossCurrency().equals(((CurrencyMatrixCross) o).getCrossCurrency());
    }

    @Override
    public int hashCode() {
      return getCrossCurrency().hashCode();
    }

    @Override
    public String toString() {
      return "CurrencyMatrixValue[" + getCrossCurrency() + "]";
    }

  }

  /**
   * Creates a matrix value that is a given constant value.
   * 
   * @param fixedValue the value
   * @return the matrix value
   */
  public static CurrencyMatrixFixed of(final double fixedValue) {
    ArgumentChecker.notZero(fixedValue, 0, "fixedValue");
    return new CurrencyMatrixFixed(fixedValue);
  }

  /**
   * Creates a matrix value that is obtained from market data on the given unique identifier.
   * 
   * @param uniqueId the unique identifier to retrieve market data for
   * @return the matrix value
   */
  public static CurrencyMatrixValueRequirement of(final UniqueIdentifier uniqueId) {
    return new CurrencyMatrixValueRequirement(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, uniqueId), false);
  }

  /**
   * Creates a matrix value that is obtained from an arbitrary value produced by the dependency graph. This may
   * be a requirement satisfied by a live data provider or a value calculated by other functions.
   * 
   * @param valueRequirement identifies the value to use
   * @return the matrix value
   */
  public static CurrencyMatrixValueRequirement of(final ValueRequirement valueRequirement) {
    return new CurrencyMatrixValueRequirement(valueRequirement, false);
  }

  /**
   * Creates a matrix value that indicates a conversion between currencies should be performed using
   * the rates of each to/from an intermediate currency.
   * 
   * @param currency the intermediate currency
   * @return the matrix value
   */
  public static CurrencyMatrixCross of(final CurrencyUnit currency) {
    return new CurrencyMatrixCross(currency);
  }

  public abstract <T> T accept(final CurrencyMatrixValueVisitor<T> visitor);

  public abstract CurrencyMatrixValue getReciprocal();

  @Override
  public abstract boolean equals(final Object o);

  @Override
  public abstract int hashCode();

}
