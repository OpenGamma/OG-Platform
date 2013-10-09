/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.joda.convert.FromString;
import org.joda.convert.ToString;

import com.opengamma.util.ArgumentChecker;

/**
 * An amount of a currency.
 * <p>
 * This class represents a {@code double} amount associated with a currency.
 * It is specifically named "CurrencyAmount" and not "Money" to indicate that
 * it simply holds a currency and an amount. By contrast, naming it "Money"
 * would imply it was a suitable choice for accounting purposes, which it is not.
 * <p>
 * This design approach has been chosen primarily for performance reasons.
 * Using a {@code BigDecimal} is markedly slower.
 * <p>
 * A {@code double} is a 64 bit floating point value suitable for most calculations.
 * Floating point maths is
 * <a href="http://docs.oracle.com/cd/E19957-01/806-3568/ncg_goldberg.html">inexact</a>
 * due to the conflict between binary and decimal arithmetic.
 * As such, there is the potential for data loss at the margins.
 * For example, adding the {@code double} values {@code 0.1d} and {@code 0.2d}
 * results in {@code 0.30000000000000004} rather than {@code 0.3}.
 * As can be seen, the level of error is small, hence providing this class is
 * used appropriately, the use of {@code double} is acceptable.
 * For example, using this class to provide a meaningful result type after
 * calculations have completed would be an appropriate use.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class CurrencyAmount implements Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The currency.
   */
  private final Currency _currency;
  /**
   * The amount.
   */
  private final double _amount;

  /**
   * Obtains an instance of {@code CurrencyAmount} for the specified currency and amount.
   *
   * @param currency  the currency the amount is in, not null
   * @param amount  the amount of the currency to represent
   * @return the currency amount, not null
   */
  public static CurrencyAmount of(final Currency currency, final double amount) {
    return new CurrencyAmount(currency, amount);
  }

  /**
   * Obtains an instance of {@code CurrencyAmount} for the specified ISO-4217
   * three letter currency code and amount.
   * <p>
   * A currency is uniquely identified by ISO-4217 three letter code.
   * This method creates the currency if it is not known.
   *
   * @param currencyCode  the three letter currency code, ASCII and upper case, not null
   * @param amount  the amount of the currency to represent
   * @return the currency amount, not null
   * @throws IllegalArgumentException if the currency code is invalid
   */
  public static CurrencyAmount of(final String currencyCode, final double amount) {
    return of(Currency.of(currencyCode), amount);
  }

  /**
   * Parses the string to produce a {@code CurrencyAmount}.
   * <p>
   * This parses the {@code toString} format of '${currency} ${amount}'.
   * 
   * @param amountStr  the amount string, not null
   * @return the currency amount
   * @throws IllegalArgumentException if the amount cannot be parsed
   */
  @FromString
  public static CurrencyAmount parse(final String amountStr) {
    ArgumentChecker.notNull(amountStr, "amountStr");
    String[] parts = StringUtils.split(amountStr, ' ');
    if (parts.length != 2) {
      throw new IllegalArgumentException("Unable to parse amount, invalid format: " + amountStr);
    }
    try {
      Currency cur = Currency.parse(parts[0]);
      double amount = Double.parseDouble(parts[1]);
      return new CurrencyAmount(cur, amount);
    } catch (RuntimeException ex) {
      throw new IllegalArgumentException("Unable to parse amount: " + amountStr, ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   * 
   * @param currency  the currency, not null
   * @param amount  the amount
   */
  private CurrencyAmount(final Currency currency, final double amount) {
    ArgumentChecker.notNull(currency, "currency");
    _currency = currency;
    _amount = amount;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the currency.
   * 
   * @return the currency, not null
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Gets the amount of the currency.
   * 
   * @return the amount
   */
  public double getAmount() {
    return _amount;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a copy of this {@code CurrencyAmount} with the specified amount added.
   * <p>
   * This adds the specified amount to this monetary amount, returning a new object.
   * The addition simply uses standard {@code double} arithmetic.
   * <p>
   * This instance is immutable and unaffected by this method. 
   * 
   * @param amountToAdd  the amount to add, in the same currency, not null
   * @return an amount based on this with the specified amount added, not null
   * @throws IllegalArgumentException if the currencies are not equal
   */
  public CurrencyAmount plus(final CurrencyAmount amountToAdd) {
    ArgumentChecker.notNull(amountToAdd, "amountToAdd");
    ArgumentChecker.isTrue(amountToAdd.getCurrency().equals(_currency), "Unable to add amounts in different currencies");
    return plus(amountToAdd.getAmount());
  }

  /**
   * Returns a copy of this {@code CurrencyAmount} with the specified amount added.
   * <p>
   * This adds the specified amount to this monetary amount, returning a new object.
   * The addition simply uses standard {@code double} arithmetic.
   * <p>
   * This instance is immutable and unaffected by this method. 
   * 
   * @param amountToAdd  the amount to add, in the same currency
   * @return an amount based on this with the specified amount added, not null
   */
  public CurrencyAmount plus(final double amountToAdd) {
    return new CurrencyAmount(_currency, _amount + amountToAdd);
  }

  /**
   * Returns a copy of this {@code CurrencyAmount} with the amount multiplied.
   * <p>
   * This takes this amount and multiplies it by the specified value.
   * The multiplication simply uses standard {@code double} arithmetic.
   * <p>
   * This instance is immutable and unaffected by this method. 
   * 
   * @param valueToMultiplyBy  the scalar amount to multiply by
   * @return an amount based on this with the amount multiplied, not null
   */
  public CurrencyAmount multipliedBy(final double valueToMultiplyBy) {
    return new CurrencyAmount(_currency, _amount * valueToMultiplyBy);
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if this amount equals another amount.
   * <p>
   * The comparison checks the currency and amount.
   * 
   * @param obj  the other amount, null returns false
   * @return true if equal
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof CurrencyAmount) {
      CurrencyAmount other = (CurrencyAmount) obj;
      return _currency.equals(other._currency) &&
        Double.doubleToLongBits(_amount) == Double.doubleToLongBits(other._amount);
    }
    return false;
  }

  /**
   * Returns a suitable hash code for the amount.
   * 
   * @return the hash code
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long amountBits = Double.doubleToLongBits(_amount);
    result = prime * result + (int) (amountBits ^ (amountBits >>> 32));
    result = prime * result + _currency.hashCode();
    return result;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the amount as a string.
   * <p>
   * The format is the currency code, followed by a space, followed by the
   * amount: '${currency} ${amount}'.
   * 
   * @return the currency amount, not null
   */
  @Override
  @ToString
  public String toString() {
    return _currency + " " + _amount;
  }

}
