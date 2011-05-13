/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.future;

import java.util.Arrays;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.FixedIncomeFutureInstrumentDefinition;
import com.opengamma.financial.instrument.FixedIncomeFutureInstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.bond.BondConvention;
import com.opengamma.financial.instrument.bond.BondDefinition;
import com.opengamma.financial.instrument.bond.BondForwardDefinition;
import com.opengamma.financial.interestrate.bond.definition.BondForward;
import com.opengamma.financial.interestrate.future.definition.BondFuture;

/**
 * 
 */
public class BondFutureDefinition implements FixedIncomeFutureInstrumentDefinition<BondFuture> {
  private final BondDefinition[] _deliverableBonds;
  private final double[] _conversionFactors;
  private final BondConvention _convention;
  private final LocalDate _deliveryDate;

  public BondFutureDefinition(final BondDefinition[] deliverableBonds, final double[] conversionFactors, final BondConvention convention, final LocalDate deliveryDate) {
    Validate.noNullElements(deliverableBonds, "deliverable bonds");
    Validate.notNull(conversionFactors, "conversion factor");
    Validate.isTrue(deliverableBonds.length == conversionFactors.length, "each deliverable bond must have a conversion factor");
    Validate.notNull(convention, "convention");
    Validate.notNull(deliveryDate, "delivery date");
    _deliverableBonds = deliverableBonds;
    _conversionFactors = conversionFactors;
    _convention = convention;
    _deliveryDate = deliveryDate;
  }

  public BondDefinition[] getDeliverableBonds() {
    return _deliverableBonds;
  }

  public double[] getConversionFactors() {
    return _conversionFactors;
  }

  public BondConvention getConvention() {
    return _convention;
  }

  public LocalDate getDeliveryDate() {
    return _deliveryDate;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _convention.hashCode();
    result = prime * result + Arrays.hashCode(_conversionFactors);
    result = prime * result + Arrays.hashCode(_deliverableBonds);
    result = prime * result + _deliveryDate.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final BondFutureDefinition other = (BondFutureDefinition) obj;
    if (!Arrays.equals(_conversionFactors, other._conversionFactors)) {
      return false;
    }
    if (!ObjectUtils.equals(_deliveryDate, other._deliveryDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_convention, other._convention)) {
      return false;
    }
    return Arrays.equals(_deliverableBonds, other._deliverableBonds);
  }

  @Override
  public BondFuture toDerivative(final ZonedDateTime date, final double price, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.isTrue(!date.toLocalDate().isAfter(_deliveryDate), date + " is after delivery date (" + _deliveryDate + ")");
    Validate.notNull(yieldCurveNames, "yield curve name(s)");
    final int n = _deliverableBonds.length;
    final BondForward[] bondForwards = new BondForward[n];
    for (int i = 0; i < n; i++) {
      bondForwards[i] = new BondForwardDefinition(_deliverableBonds[i], _deliveryDate, _convention).toDerivative(date, yieldCurveNames);
    }
    return new BondFuture(bondForwards, _conversionFactors, price);
  }

  @Override
  public <U, V> V accept(final FixedIncomeFutureInstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitBondFutureDefinition(this, data);
  }

  @Override
  public <V> V accept(final FixedIncomeFutureInstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitBondFutureDefinition(this);
  }

}
