/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.types.PrimitiveFieldTypes;
import org.springframework.util.ObjectUtils;

import com.opengamma.core.common.Currency;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * Represents a entry within a {@link CurrencyMatrix}.
 */
public abstract class CurrencyMatrixValue {

  /**
   * A fixed conversion rate from one currency to another.
   */
  public static final class CurrencyMatrixFixedValue extends CurrencyMatrixValue {

    private final double _fixedValue;

    private CurrencyMatrixFixedValue(final double fixedValue) {
      _fixedValue = fixedValue;
    }

    @Override
    public <T> T accept(final CurrencyMatrixValueVisitor<T> visitor) {
      return visitor.visitFixedValue(this);
    }

    public double getFixedValue() {
      return _fixedValue;
    }

    @Override
    public CurrencyMatrixFixedValue getReciprocal() {
      if (getFixedValue() == 1.0) {
        return this;
      } else {
        return new CurrencyMatrixFixedValue(1 / getFixedValue());
      }
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof CurrencyMatrixFixedValue)) {
        return false;
      }
      return ((CurrencyMatrixFixedValue) o).getFixedValue() == getFixedValue();
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
   * graph. This might be a live data sourcing function to use current market data.
   */
  public static final class CurrencyMatrixUniqueIdentifier extends CurrencyMatrixValue {

    private final UniqueIdentifier _uniqueIdentifier;
    private final boolean _reciprocal;

    private CurrencyMatrixUniqueIdentifier(final UniqueIdentifier uniqueIdentifier, boolean reciprocal) {
      _uniqueIdentifier = uniqueIdentifier;
      _reciprocal = reciprocal;
    }

    @Override
    public <T> T accept(final CurrencyMatrixValueVisitor<T> visitor) {
      return visitor.visitUniqueIdentifier(this);
    }

    public UniqueIdentifier getUniqueIdentifier() {
      return _uniqueIdentifier;
    }

    public boolean isReciprocal() {
      return _reciprocal;
    }

    @Override
    public CurrencyMatrixUniqueIdentifier getReciprocal() {
      return new CurrencyMatrixUniqueIdentifier(getUniqueIdentifier(), !isReciprocal());
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof CurrencyMatrixUniqueIdentifier)) {
        return false;
      }
      final CurrencyMatrixUniqueIdentifier oc = (CurrencyMatrixUniqueIdentifier) o;
      return getUniqueIdentifier().equals(oc.getUniqueIdentifier()) && (isReciprocal() == oc.isReciprocal());
    }

    @Override
    public int hashCode() {
      return getUniqueIdentifier().hashCode() * 17 + ObjectUtils.hashCode(isReciprocal());
    }

    public void toFudgeMsg(final FudgeMessageFactory factory, final MutableFudgeFieldContainer msg) {
      getUniqueIdentifier().toFudgeMsg(factory, msg);
      msg.add("reciprocal", null, PrimitiveFieldTypes.BOOLEAN_TYPE, isReciprocal());
    }

    public static CurrencyMatrixUniqueIdentifier fromFudgeMsg(final FudgeFieldContainer msg) {
      return new CurrencyMatrixUniqueIdentifier(UniqueIdentifier.fromFudgeMsg(msg), msg.getBoolean("reciprocal"));
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("CurrencyMatrixValue[");
      sb.append(getUniqueIdentifier().toString());
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

    private final Currency _crossCurrency;

    private CurrencyMatrixCross(final Currency crossCurrency) {
      _crossCurrency = crossCurrency;
    }

    @Override
    public <T> T accept(final CurrencyMatrixValueVisitor<T> visitor) {
      return visitor.visitCross(this);
    }

    public Currency getCrossCurrency() {
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

  public static CurrencyMatrixFixedValue of(final double fixedValue) {
    ArgumentChecker.notZero(fixedValue, 0, "fixedValue");
    return new CurrencyMatrixFixedValue(fixedValue);
  }

  public static CurrencyMatrixUniqueIdentifier of(final UniqueIdentifier uniqueIdentifier) {
    return new CurrencyMatrixUniqueIdentifier(uniqueIdentifier, false);
  }

  public static CurrencyMatrixCross of(final Currency currency) {
    return new CurrencyMatrixCross(currency);
  }

  public abstract <T> T accept(final CurrencyMatrixValueVisitor<T> visitor);

  public abstract CurrencyMatrixValue getReciprocal();

  @Override
  public abstract boolean equals(final Object o);

  @Override
  public abstract int hashCode();

}
