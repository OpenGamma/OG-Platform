/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingFlatSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.IborInterpolatedStubCompoundingCoupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.IborInterpolatedStubCoupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Definition for interpolated stub coupon.
 * 
 */
public final class InterpolatedStubCouponDefinition extends CouponDefinition implements InstrumentDefinitionWithData<Payment, DoubleTimeSeries<ZonedDateTime>> {

  private final CouponDefinition _fullCoupon;
  
  private final ZonedDateTime _firstInterpolatedDate;
  
  private final double _firstInterpolatedYearFraction;
  
  private final IborIndex _firstStubIndex; 
  
  private final ZonedDateTime _secondInterpolatedDate;
  
  private final double _secondInterpolatedYearFraction;
  
  private final IborIndex _secondStubIndex;
  
  private InterpolatedStubCouponDefinition(
      final CouponDefinition fullCoupon,
      final ZonedDateTime firstInterpolatedDate,
      final double firstInterpolatedYearFraction,
      final IborIndex firstStubIndex,
      final ZonedDateTime secondInterpolatedDate,
      final double secondInterpolatedYearFraction,
      final IborIndex secondStubIndex) {
    super(fullCoupon.getCurrency(), fullCoupon.getPaymentDate(), fullCoupon.getAccrualStartDate(), fullCoupon.getAccrualEndDate(), fullCoupon.getPaymentYearFraction(), fullCoupon.getNotional());
    _fullCoupon = fullCoupon;
    _firstInterpolatedDate = firstInterpolatedDate;
    _firstInterpolatedYearFraction = firstInterpolatedYearFraction;
    _firstStubIndex = firstStubIndex;
    _secondInterpolatedDate = secondInterpolatedDate;
    _secondInterpolatedYearFraction = secondInterpolatedYearFraction;
    _secondStubIndex = secondStubIndex;
  }
  
  public static InterpolatedStubCouponDefinition from(
      final CouponDefinition fullCoupon,
      final ZonedDateTime firstInterpolatedDate,
      final double firstInterpolatedYearFraction,
      final IborIndex firstStubIndex,
      final ZonedDateTime secondInterpolatedDate,
      final double secondInterpolatedYearFraction,
      final IborIndex secondStubIndex) {
    return new InterpolatedStubCouponDefinition(fullCoupon, firstInterpolatedDate, firstInterpolatedYearFraction, firstStubIndex, secondInterpolatedDate, secondInterpolatedYearFraction, secondStubIndex);
  }
  
  public ZonedDateTime getFirstInterpolatedDate() {
    return _firstInterpolatedDate;
  }
  
  public double getFirstInterpolatedYearFraction() {
    return _firstInterpolatedYearFraction;
  }
  
  public IborIndex getFirstStubIndex() {
    return _firstStubIndex;
  }
  
  public ZonedDateTime getSecondInterpolatedDate() {
    return _secondInterpolatedDate;
  }
  
  public double getSecondInterpolatedYearFraction() {
    return _secondInterpolatedYearFraction;
  }
  
  public IborIndex getSecondStubIndex() {
    return _secondStubIndex;
  }
  
  @Override
  public Payment toDerivative(ZonedDateTime date, String... yieldCurveNames) {
    throw new UnsupportedOperationException("InterpolatedStubCouponDefinition does not support toDerivative with yield curve name - deprecated method");
  }

  @Override
  public Payment toDerivative(ZonedDateTime date, DoubleTimeSeries<ZonedDateTime> data, String... yieldCurveNames) {
    throw new UnsupportedOperationException("InterpolatedStubCouponDefinition does not support toDerivative with yield curve name - deprecated method");
  }

  @Override
  public Payment toDerivative(ZonedDateTime date) {
    Payment fullCoupon = _fullCoupon.toDerivative(date);
    
    double firstInterpolatedTime = TimeCalculator.getTimeBetween(date, _firstInterpolatedDate);
    double secondInterpolatedTime = TimeCalculator.getTimeBetween(date, _secondInterpolatedDate);
    InterpolatedStubParameterObject of = InterpolatedStubParameterObject.of(firstInterpolatedTime, _firstInterpolatedYearFraction, _firstStubIndex, secondInterpolatedTime, _secondInterpolatedYearFraction, _secondStubIndex);
    return fullCoupon.accept(InterpolatedStubCouponVisitor.getInstance(), of);
  }

  @Override
  public Payment toDerivative(ZonedDateTime date, DoubleTimeSeries<ZonedDateTime> data) {
    Payment fullCoupon = ((InstrumentDefinitionWithData<Payment, DoubleTimeSeries<ZonedDateTime>>) _fullCoupon).toDerivative(date, data);
    
    double firstInterpolatedTime = TimeCalculator.getTimeBetween(date, _firstInterpolatedDate);
    double secondInterpolatedTime = TimeCalculator.getTimeBetween(date, _secondInterpolatedDate);
    InterpolatedStubParameterObject of = InterpolatedStubParameterObject.of(firstInterpolatedTime, _firstInterpolatedYearFraction, _firstStubIndex, secondInterpolatedTime, _secondInterpolatedYearFraction, _secondStubIndex);
    return fullCoupon.accept(InterpolatedStubCouponVisitor.getInstance(), of);
  }

  @Override
  public <U, V> V accept(InstrumentDefinitionVisitor<U, V> visitor, U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return _fullCoupon.accept(visitor, data);
  }

  @Override
  public <V> V accept(InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return _fullCoupon.accept(visitor);
  }
  
  private static final class InterpolatedStubParameterObject {
    private final double _firstInterpolatedTime;
    
    private final double _firstInterpolatedYearFraction;
    
    private final IborIndex _firstStubIndex;
    
    private final double _secondInterpolatedTime;
    
    private final double _secondInterpolatedYearFraction;
    
    private final IborIndex _secondStubIndex;
    
    private InterpolatedStubParameterObject(
        final double firstInterpolatedTime,
        final double firstInterpolatedYearFraction,
        final IborIndex firstStubIndex,
        final double secondInterpolatedTime,
        final double secondInterpolatedYearFraction,
        final IborIndex secondStubIndex) {
      _firstInterpolatedTime = firstInterpolatedTime;
      _firstInterpolatedYearFraction = firstInterpolatedYearFraction;
      _firstStubIndex = firstStubIndex;
      _secondInterpolatedTime = secondInterpolatedTime;
      _secondInterpolatedYearFraction = secondInterpolatedYearFraction;
      _secondStubIndex = secondStubIndex;
    }
    
    public double getFirstInterpolatedTime() {
      return _firstInterpolatedTime;
    }
    
    public double getFirstInterpolatedYearFraction() {
      return _firstInterpolatedYearFraction;
    }
    
    public double getSecondInterpolatedTime() {
      return _secondInterpolatedTime;
    }
    
    public double getSecondInterpolatedYearFraction() {
      return _secondInterpolatedYearFraction;
    }
    
    public IborIndex getFirstStubIndex() {
      return _firstStubIndex;
    }
    
    public IborIndex getSecondStubIndex() {
      return _secondStubIndex;
    }
    
    public static InterpolatedStubParameterObject of(
        final double firstInterpolatedTime,
        final double firstInterpolatedYearFraction,
        final IborIndex firstStubIndex,
        final double secondInterpolatedTime,
        final double secondInterpolatedYearFraction,
        final IborIndex secondStubIndex) {
      return new InterpolatedStubParameterObject(firstInterpolatedTime, firstInterpolatedYearFraction, firstStubIndex, secondInterpolatedTime, secondInterpolatedYearFraction, secondStubIndex);
    }
  }

  private static final class InterpolatedStubCouponVisitor extends InstrumentDerivativeVisitorAdapter<InterpolatedStubParameterObject, Payment> {
    
    private static final InterpolatedStubCouponVisitor INSTANCE = new InterpolatedStubCouponVisitor();
    
    private InterpolatedStubCouponVisitor() {
    }
    
    public static InterpolatedStubCouponVisitor getInstance() {
      return INSTANCE;
    }
    
    @Override
    public Payment visitCouponFixed(CouponFixed payment, InterpolatedStubParameterObject data) {
      return payment;
    }
    
    @Override
    public Payment visitCouponIbor(CouponIbor payment, InterpolatedStubParameterObject data) {
      return IborInterpolatedStubCoupon.from(
          payment,
          data.getFirstInterpolatedTime(),
          data.getFirstInterpolatedYearFraction(),
          data.getFirstStubIndex(),
          data.getSecondInterpolatedTime(),
          data.getSecondInterpolatedYearFraction(),
          data.getSecondStubIndex());
    }
    
    @Override
    public Payment visitCouponIborSpread(CouponIborSpread payment, InterpolatedStubParameterObject data) {
      return IborInterpolatedStubCoupon.from(
          payment,
          data.getFirstInterpolatedTime(),
          data.getFirstInterpolatedYearFraction(),
          data.getFirstStubIndex(),
          data.getSecondInterpolatedTime(),
          data.getSecondInterpolatedYearFraction(),
          data.getSecondStubIndex());
    }
    
    @Override
    public Payment visitCouponIborCompounding(CouponIborCompounding payment, InterpolatedStubParameterObject data) {
      return IborInterpolatedStubCompoundingCoupon.from(
          payment,
          data.getFirstInterpolatedTime(),
          data.getFirstInterpolatedYearFraction(),
          data.getFirstStubIndex(),
          data.getSecondInterpolatedTime(),
          data.getSecondInterpolatedYearFraction(),
          data.getSecondStubIndex());
    }
    
    @Override
    public Payment visitCouponIborCompoundingFlatSpread(CouponIborCompoundingFlatSpread payment, InterpolatedStubParameterObject data) {
      return IborInterpolatedStubCompoundingCoupon.from(
          payment,
          data.getFirstInterpolatedTime(),
          data.getFirstInterpolatedYearFraction(),
          data.getFirstStubIndex(),
          data.getSecondInterpolatedTime(),
          data.getSecondInterpolatedYearFraction(),
          data.getSecondStubIndex());
    }
    
    @Override
    public Payment visitCouponIborCompoundingSpread(CouponIborCompoundingSpread payment, InterpolatedStubParameterObject data) {
      return IborInterpolatedStubCompoundingCoupon.from(
          payment,
          data.getFirstInterpolatedTime(),
          data.getFirstInterpolatedYearFraction(),
          data.getFirstStubIndex(),
          data.getSecondInterpolatedTime(),
          data.getSecondInterpolatedYearFraction(),
          data.getSecondStubIndex());
    }
  }
}
