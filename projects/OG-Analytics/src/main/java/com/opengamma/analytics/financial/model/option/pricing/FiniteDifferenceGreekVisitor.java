/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing;

import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.greeks.AbstractGreekVisitor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.time.DateUtils;

/**
 * @param <S> Type of the option data bundle
 * @param <T> Type of the option definition
 */
public class FiniteDifferenceGreekVisitor<S extends StandardOptionDataBundle, T extends OptionDefinition> extends AbstractGreekVisitor<Double> {
  private static final double EPS = 1e-4; // TODO make this so it can be set
  private final Function1D<S, Double> _pricingFunction;
  private final S _data;
  private final T _definition;

  public FiniteDifferenceGreekVisitor(final Function1D<S, Double> pricingFunction, final S data, final T definition) {
    Validate.notNull(pricingFunction, "pricing function");
    Validate.notNull(data, "data");
    Validate.notNull(definition, "definition");
    _pricingFunction = pricingFunction;
    _data = data;
    _definition = definition;
  }

  @Override
  public Double visitDelta() {
    final Double s = _data.getSpot();
    final S dataUp = (S) _data.withSpot(s + EPS);
    final S dataDown = (S) _data.withSpot(s - EPS);
    return getFirstDerivative(dataUp, dataDown);
  }

  @Override
  public Double visitGamma() {
    final Double s = _data.getSpot();
    final S dataUp = (S) _data.withSpot(s + EPS);
    final S dataDown = (S) _data.withSpot(s - EPS);
    return getSecondDerivative(dataUp, dataDown, _data);
  }

  @Override
  public Double visitVega() {
    final VolatilitySurface upSurface = _data.getVolatilitySurface().withParallelShift(EPS);
    final VolatilitySurface downSurface = _data.getVolatilitySurface().withParallelShift(-EPS);
    final S dataUp = (S) _data.withVolatilitySurface(upSurface);
    final S dataDown = (S) _data.withVolatilitySurface(downSurface);
    return getFirstDerivative(dataUp, dataDown);
  }

  @Override
  public Double visitPrice() {
    return _pricingFunction.evaluate(_data);
  }

  @Override
  public Double visitRho() {
    final ZonedDateTime date = _data.getDate();
    final double t = _definition.getTimeToExpiry(date);
    final double r = _data.getInterestRate(t);
    final double b = _data.getCostOfCarry();
    final YieldAndDiscountCurve upCurve = YieldCurve.from(ConstantDoublesCurve.from(r + EPS));
    final YieldAndDiscountCurve downCurve = YieldCurve.from(ConstantDoublesCurve.from(r - EPS));
    final S dataUp = (S) _data.withCostOfCarry(b + EPS).withInterestRateCurve(upCurve);
    final S dataDown = (S) _data.withCostOfCarry(b - EPS).withInterestRateCurve(downCurve);
    return getFirstDerivative(dataUp, dataDown);
  }

  @Override
  public Double visitTheta() {
    final ZonedDateTime date = _data.getDate();
    final ZonedDateTime offset = DateUtils.getDateOffsetWithYearFraction(date, EPS);
    final S dataUp = (S) _data.withDate(offset);
    return getForwardFirstDerivative(dataUp, _data);
  }

  @Override
  public Double visitCarryRho() {
    final double b = _data.getCostOfCarry();
    final S dataUp = (S) _data.withCostOfCarry(b + EPS);
    final S dataDown = (S) _data.withCostOfCarry(b - EPS);
    return getFirstDerivative(dataUp, dataDown);
  }

  @Override
  public Double visitDZetaDVol() {
    return null;
  }

  // TODO need to use forward differencing for dt?
  @Override
  public Double visitDeltaBleed() {
    final double s = _data.getSpot();
    final double sUp = s + EPS;
    final double sDown = s - EPS;
    final ZonedDateTime dateUp = DateUtils.getDateOffsetWithYearFraction(_data.getDate(), EPS);
    final ZonedDateTime dateDown = DateUtils.getDateOffsetWithYearFraction(_data.getDate(), -EPS);
    final S dataUp1Up2 = (S) _data.withSpot(sUp).withDate(dateUp);
    final S dataUp1Down2 = (S) _data.withSpot(sUp).withDate(dateDown);
    final S dataDown1Up2 = (S) _data.withSpot(sDown).withDate(dateUp);
    final S dataDown1Down2 = (S) _data.withSpot(sDown).withDate(dateDown);
    return getMixedSecondDerivative(dataUp1Up2, dataUp1Down2, dataDown1Up2, dataDown1Down2);
  }

  @Override
  public Double visitDriftlessTheta() {
    return null;
  }

  @Override
  public Double visitElasticity() {
    final double delta = visitDelta();
    final double price = visitPrice();
    return _data.getSpot() * delta / price;
  }

  @Override
  public Double visitGammaBleed() {
    final double s = _data.getSpot();
    final double sUp = s + EPS;
    final double sDown = s - EPS;
    final ZonedDateTime dateUp = DateUtils.getDateOffsetWithYearFraction(_data.getDate(), EPS);
    final ZonedDateTime dateDown = DateUtils.getDateOffsetWithYearFraction(_data.getDate(), -EPS);
    final S dataUp1Up1 = (S) _data.withSpot(sUp).withDate(dateUp);
    final S dataUp2 = (S) _data.withDate(dateUp);
    final S dataDown1Up2 = (S) _data.withSpot(sDown).withDate(dateUp);
    final S dataUp1Down2 = (S) _data.withSpot(sUp).withDate(dateDown);
    final S dataDown2 = (S) _data.withDate(dateDown);
    final S dataDown1Down2 = (S) _data.withSpot(sDown).withDate(dateDown);
    return getMixedThirdDerivative(dataUp1Up1, dataUp2, dataDown1Up2, dataUp1Down2, dataDown2, dataDown1Down2);
  }

  @Override
  public Double visitGammaP() {
    return getGammaP(0, 0);
  }

  @Override
  public Double visitGammaPBleed() {
    final double gammaPUp = getGammaP(0, EPS);
    final double gammaPDown = getGammaP(0, -EPS);
    return (gammaPUp - gammaPDown) / (2 * EPS);
  }

  @Override
  public Double visitPhi() {
    return -visitCarryRho();
  }

  @Override
  public Double visitSpeed() {
    final double s = _data.getSpot();
    final S dataUpUp = (S) _data.withSpot(s + 2 * EPS);
    final S dataUp = (S) _data.withSpot(s + EPS);
    final S dataDown = (S) _data.withSpot(s - EPS);
    return getThirdDerivative(dataUpUp, dataUp, _data, dataDown);
  }

  @Override
  public Double visitSpeedP() {
    final double gammaPUp = getGammaP(EPS, 0);
    final double gammaPDown = getGammaP(-EPS, 0);
    return (gammaPUp - gammaPDown) / (2 * EPS);
  }

  @Override
  public Double visitStrikeDelta() {
    return null;
  }

  @Override
  public Double visitStrikeGamma() {
    return null;
  }

  @Override
  public Double visitUltima() {
    final VolatilitySurface upUpSurface = _data.getVolatilitySurface().withParallelShift(2 * EPS);
    final VolatilitySurface upSurface = _data.getVolatilitySurface().withParallelShift(EPS);
    final VolatilitySurface downSurface = _data.getVolatilitySurface().withParallelShift(-EPS);
    final S dataUpUp = (S) _data.withVolatilitySurface(upUpSurface);
    final S dataUp = (S) _data.withVolatilitySurface(upSurface);
    final S dataDown = (S) _data.withVolatilitySurface(downSurface);
    return getThirdDerivative(dataUpUp, dataUp, _data, dataDown);
  }

  @Override
  public Double visitVanna() {
    final double s = _data.getSpot();
    final double sUp = s + EPS;
    final double sDown = s - EPS;
    final VolatilitySurface upSurface = _data.getVolatilitySurface().withParallelShift(EPS);
    final VolatilitySurface downSurface = _data.getVolatilitySurface().withParallelShift(-EPS);
    final S dataUp1Up2 = (S) _data.withSpot(sUp).withVolatilitySurface(upSurface);
    final S dataUp1Down2 = (S) _data.withSpot(sDown).withVolatilitySurface(upSurface);
    final S dataDown1Up2 = (S) _data.withSpot(sUp).withVolatilitySurface(downSurface);
    final S dataDown1Down2 = (S) _data.withSpot(sDown).withVolatilitySurface(downSurface);
    return getMixedSecondDerivative(dataUp1Up2, dataUp1Down2, dataDown1Up2, dataDown1Down2);
  }

  @Override
  public Double visitVarianceUltima() {
    final double t = _definition.getTimeToExpiry(_data.getDate());
    final double sigma = _data.getVolatility(t, _definition.getStrike());
    final double variance = sigma * sigma;
    final VolatilitySurface upUpSurface = new VolatilitySurface(ConstantDoublesSurface.from(Math.sqrt(variance + 2 * EPS)));
    final VolatilitySurface upSurface = new VolatilitySurface(ConstantDoublesSurface.from(Math.sqrt(variance + EPS)));
    final VolatilitySurface downSurface = new VolatilitySurface(ConstantDoublesSurface.from(Math.sqrt(variance - EPS)));
    final S dataUpUp = (S) _data.withVolatilitySurface(upUpSurface);
    final S dataUp = (S) _data.withVolatilitySurface(upSurface);
    final S dataDown = (S) _data.withVolatilitySurface(downSurface);
    return getThirdDerivative(dataUpUp, dataUp, _data, dataDown);
  }

  @Override
  public Double visitVarianceVanna() {
    final double t = _definition.getTimeToExpiry(_data.getDate());
    final double sigma = _data.getVolatility(t, _definition.getStrike());
    final double variance = sigma * sigma;
    final double s = _data.getSpot();
    final double sUp = s + EPS;
    final double sDown = s - EPS;
    final VolatilitySurface upSurface = new VolatilitySurface(ConstantDoublesSurface.from(Math.sqrt(variance + EPS)));
    final VolatilitySurface downSurface = new VolatilitySurface(ConstantDoublesSurface.from(Math.sqrt(variance - EPS)));
    final S dataUp1Up2 = (S) _data.withVolatilitySurface(upSurface).withSpot(sUp);
    final S dataUp1Down2 = (S) _data.withVolatilitySurface(upSurface).withSpot(sDown);
    final S dataDown1Up2 = (S) _data.withVolatilitySurface(downSurface).withSpot(sUp);
    final S dataDown1Down2 = (S) _data.withVolatilitySurface(downSurface).withSpot(sDown);
    return _data.getSpot() * getMixedSecondDerivative(dataUp1Up2, dataUp1Down2, dataDown1Up2, dataDown1Down2);
  }

  @Override
  public Double visitVarianceVega() {
    final double t = _definition.getTimeToExpiry(_data.getDate());
    final double sigma = _data.getVolatility(t, _definition.getStrike());
    final double variance = sigma * sigma;
    final VolatilitySurface upSurface = new VolatilitySurface(ConstantDoublesSurface.from(Math.sqrt(variance + EPS)));
    final VolatilitySurface downSurface = new VolatilitySurface(ConstantDoublesSurface.from(Math.sqrt(variance - EPS)));
    final S dataUp = (S) _data.withVolatilitySurface(upSurface);
    final S dataDown = (S) _data.withVolatilitySurface(downSurface);
    return getFirstDerivative(dataUp, dataDown);
  }

  @Override
  public Double visitVarianceVomma() {
    final double t = _definition.getTimeToExpiry(_data.getDate());
    final double sigma = _data.getVolatility(t, _definition.getStrike());
    final double variance = sigma * sigma;
    final VolatilitySurface upSurface = new VolatilitySurface(ConstantDoublesSurface.from(Math.sqrt(variance + EPS)));
    final VolatilitySurface downSurface = new VolatilitySurface(ConstantDoublesSurface.from(Math.sqrt(variance - EPS)));
    final S dataUp = (S) _data.withVolatilitySurface(upSurface);
    final S dataDown = (S) _data.withVolatilitySurface(downSurface);
    return getSecondDerivative(dataUp, dataDown, _data);
  }

  @Override
  public Double visitVegaBleed() {
    final ZonedDateTime upDate = DateUtils.getDateOffsetWithYearFraction(_data.getDate(), EPS);
    final ZonedDateTime downDate = DateUtils.getDateOffsetWithYearFraction(_data.getDate(), -EPS);
    final VolatilitySurface upSurface = _data.getVolatilitySurface().withParallelShift(EPS);
    final VolatilitySurface downSurface = _data.getVolatilitySurface().withParallelShift(-EPS);
    final S dataUp1Up2 = (S) _data.withVolatilitySurface(upSurface).withDate(upDate);
    final S dataUp1Down2 = (S) _data.withVolatilitySurface(upSurface).withDate(downDate);
    final S dataDown1Up2 = (S) _data.withVolatilitySurface(downSurface).withDate(upDate);
    final S dataDown1Down2 = (S) _data.withVolatilitySurface(downSurface).withDate(downDate);
    return getMixedSecondDerivative(dataUp1Up2, dataUp1Down2, dataDown1Up2, dataDown1Down2);
  }

  @Override
  public Double visitVegaP() {
    final double t = _definition.getTimeToExpiry(_data.getDate());
    final double sigma = _data.getVolatility(t, _definition.getStrike());
    return visitVega() * sigma / 10;
  }

  @Override
  public Double visitVomma() {
    final VolatilitySurface upSurface = _data.getVolatilitySurface().withParallelShift(EPS);
    final VolatilitySurface downSurface = _data.getVolatilitySurface().withParallelShift(-EPS);
    final S dataUp = (S) _data.withVolatilitySurface(upSurface);
    final S dataDown = (S) _data.withVolatilitySurface(downSurface);
    return getSecondDerivative(dataUp, dataDown, _data);
  }

  @Override
  public Double visitVommaP() {
    final double t = _definition.getTimeToExpiry(_data.getDate());
    final double sigma = _data.getVolatility(t, _definition.getStrike());
    return visitVomma() * sigma / 10;
  }

  @Override
  public Double visitZeta() {
    return null;
  }

  @Override
  public Double visitZetaBleed() {
    return null;
  }

  @Override
  public Double visitZomma() {
    final double s = _data.getSpot();
    final double sUp = s + EPS;
    final double sDown = s - EPS;
    final VolatilitySurface upSurface = _data.getVolatilitySurface().withParallelShift(EPS);
    final VolatilitySurface downSurface = _data.getVolatilitySurface().withParallelShift(-EPS);
    final S dataUp1Up1 = (S) _data.withSpot(sUp).withVolatilitySurface(upSurface);
    final S dataUp2 = (S) _data.withVolatilitySurface(upSurface);
    final S dataDown1Up2 = (S) _data.withSpot(sDown).withVolatilitySurface(upSurface);
    final S dataUp1Down2 = (S) _data.withSpot(sUp).withVolatilitySurface(downSurface);
    final S dataDown2 = (S) _data.withVolatilitySurface(downSurface);
    final S dataDown1Down2 = (S) _data.withSpot(sDown).withVolatilitySurface(downSurface);
    return getMixedThirdDerivative(dataUp1Up1, dataUp2, dataDown1Up2, dataUp1Down2, dataDown2, dataDown1Down2);
  }

  @Override
  public Double visitZommaP() {
    return visitZomma() * _data.getSpot() / 100;
  }

  @Override
  public Double visitDVannaDVol() {
    final double s = _data.getSpot();
    final double sUp = s + EPS;
    final double sDown = s - EPS;
    final VolatilitySurface upSurface = _data.getVolatilitySurface().withParallelShift(EPS);
    final VolatilitySurface downSurface = _data.getVolatilitySurface().withParallelShift(-EPS);
    final S dataUp1Up1 = (S) _data.withVolatilitySurface(upSurface).withSpot(sUp);
    final S dataUp2 = (S) _data.withSpot(sUp);
    final S dataDown1Up2 = (S) _data.withVolatilitySurface(downSurface).withSpot(sUp);
    final S dataUp1Down2 = (S) _data.withVolatilitySurface(upSurface).withSpot(sDown);
    final S dataDown2 = (S) _data.withSpot(sDown);
    final S dataDown1Down2 = (S) _data.withVolatilitySurface(downSurface).withSpot(sDown);
    return getMixedThirdDerivative(dataUp1Up1, dataUp2, dataDown1Up2, dataUp1Down2, dataDown2, dataDown1Down2);
  }

  private double getFirstDerivative(final S dataUp, final S dataDown) {
    return (_pricingFunction.evaluate(dataUp) - _pricingFunction.evaluate(dataDown)) / (2 * EPS);
  }

  private double getForwardFirstDerivative(final S dataUp, final S data) {
    return (_pricingFunction.evaluate(dataUp) - _pricingFunction.evaluate(data)) / EPS;
  }

  private double getSecondDerivative(final S dataUp, final S dataDown, final S data) {
    return (_pricingFunction.evaluate(dataUp) + _pricingFunction.evaluate(dataDown) - 2 * _pricingFunction.evaluate(data)) / (EPS * EPS);
  }

  private double getMixedSecondDerivative(final S dataUp1Up2, final S dataUp1Down2, final S dataDown1Up2, final S dataDown1Down2) {
    return (_pricingFunction.evaluate(dataUp1Up2) - _pricingFunction.evaluate(dataUp1Down2) - _pricingFunction.evaluate(dataDown1Up2) + _pricingFunction.evaluate(dataDown1Down2)) / (4 * EPS * EPS);
  }

  private double getThirdDerivative(final S dataUpUp, final S dataUp, final S data, final S dataDown) {
    return (_pricingFunction.evaluate(dataUpUp) + 3 * _pricingFunction.evaluate(data) - 3 * _pricingFunction.evaluate(dataUp) - _pricingFunction.evaluate(dataDown)) / (EPS * EPS * EPS);
  }

  private double getMixedThirdDerivative(final S dataUp1Up1, final S dataUp2, final S dataDown1Up2, final S dataUp1Down2, final S dataDown2, final S dataDown1Down2) {
    return (_pricingFunction.evaluate(dataUp1Up1) - 2 * _pricingFunction.evaluate(dataUp2) + _pricingFunction.evaluate(dataDown1Up2) - _pricingFunction.evaluate(dataUp1Down2) + 2
        * _pricingFunction.evaluate(dataDown2) - _pricingFunction.evaluate(dataDown1Down2))
        / (2 * EPS * EPS * EPS);
  }

  private double getGammaP(final double spotOffset, final double tOffset) {
    final double spot = _data.getSpot() + spotOffset;
    final ZonedDateTime date = DateUtils.getDateOffsetWithYearFraction(_data.getDate(), tOffset);
    final S dataUp = (S) _data.withSpot(spot + EPS).withDate(date);
    final S dataDown = (S) _data.withSpot(spot - EPS).withDate(date);
    final S data = (S) _data.withSpot(spot).withDate(date);
    final double gamma = getSecondDerivative(dataUp, dataDown, data);
    return gamma * spot / 100;
  }
}
