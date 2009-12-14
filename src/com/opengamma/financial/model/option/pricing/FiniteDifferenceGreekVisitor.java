/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.greeks.GreekResult;
import com.opengamma.financial.greeks.GreekVisitor;
import com.opengamma.financial.greeks.SingleGreekResult;
import com.opengamma.financial.model.interestrate.curve.ConstantInterestRateDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.time.DateUtil;

/**
 * 
 * @author emcleod
 */
@SuppressWarnings("unchecked")
public class FiniteDifferenceGreekVisitor<S extends StandardOptionDataBundle, T extends OptionDefinition> implements GreekVisitor<GreekResult<?>> {
  private static final double EPS = 1e-6;
  private final Function1D<S, Double> _pricingFunction;
  private final S _data;
  private final T _definition;

  public FiniteDifferenceGreekVisitor(final Function1D<S, Double> pricingFunction, final S data, final T definition) {
    _pricingFunction = pricingFunction;
    _data = data;
    _definition = definition;
  }

  @Override
  public GreekResult<?> visitDelta() {
    final Double s = _data.getSpot();
    final S dataUp = (S) _data.withSpot(s + EPS);
    final S dataDown = (S) _data.withSpot(s - EPS);
    return new SingleGreekResult(getFirstDerivative(dataUp, dataDown));
  }

  @Override
  public GreekResult<?> visitGamma() {
    final Double s = _data.getSpot();
    final S dataUp = (S) _data.withSpot(s + EPS);
    final S dataDown = (S) _data.withSpot(s - EPS);
    return new SingleGreekResult(getSecondDerivative(dataUp, dataDown, _data));
  }

  @Override
  public GreekResult<?> visitVega() {
    final ZonedDateTime date = _data.getDate();
    final double t = _definition.getTimeToExpiry(date);
    final Double sigma = _data.getVolatility(t, _definition.getStrike());
    final VolatilitySurface upSurface = new ConstantVolatilitySurface(sigma + EPS);
    final VolatilitySurface downSurface = new ConstantVolatilitySurface(sigma - EPS);
    final S dataUp = (S) _data.withVolatilitySurface(upSurface);
    final S dataDown = (S) _data.withVolatilitySurface(downSurface);
    return new SingleGreekResult(getFirstDerivative(dataUp, dataDown));
  }

  @Override
  public GreekResult<?> visitPrice() {
    return new SingleGreekResult(_pricingFunction.evaluate(_data));
  }

  @Override
  public GreekResult<?> visitRho() {
    final ZonedDateTime date = _data.getDate();
    final double t = _definition.getTimeToExpiry(date);
    final double r = _data.getInterestRate(t);
    final double b = _data.getCostOfCarry();
    final DiscountCurve upCurve = new ConstantInterestRateDiscountCurve(r + EPS);
    final DiscountCurve downCurve = new ConstantInterestRateDiscountCurve(r - EPS);
    final S dataUp = (S) _data.withCostOfCarry(b + EPS).withDiscountCurve(upCurve);
    final S dataDown = (S) _data.withCostOfCarry(b - EPS).withDiscountCurve(downCurve);
    return new SingleGreekResult(getFirstDerivative(dataUp, dataDown));
  }

  @Override
  public GreekResult<?> visitTheta() {
    final ZonedDateTime date = _data.getDate();
    final ZonedDateTime offset = DateUtil.getDateOffsetWithYearFraction(date, EPS);
    final S dataUp = (S) _data.withDate(offset);
    return new SingleGreekResult(getForwardFirstDerivative(dataUp, _data));
  }

  @Override
  public GreekResult<?> visitTimeBucketedRho() {
    return null;
  }

  @Override
  public GreekResult<?> visitCarryRho() {
    final double b = _data.getCostOfCarry();
    final S dataUp = (S) _data.withCostOfCarry(b + EPS);
    final S dataDown = (S) _data.withCostOfCarry(b - EPS);
    return new SingleGreekResult(getFirstDerivative(dataUp, dataDown));
  }

  @Override
  public GreekResult<?> visitDZetaDVol() {
    return null;
  }

  // TODO need to use forward differencing for dt?
  @Override
  public GreekResult<?> visitDeltaBleed() {
    final double s = _data.getSpot();
    final double sUp = s + EPS;
    final double sDown = s - EPS;
    final ZonedDateTime dateUp = DateUtil.getDateOffsetWithYearFraction(_data.getDate(), EPS);
    final ZonedDateTime dateDown = DateUtil.getDateOffsetWithYearFraction(_data.getDate(), -EPS);
    final S dataUp1Up2 = (S) _data.withSpot(sUp).withDate(dateUp);
    final S dataUp1Down2 = (S) _data.withSpot(sUp).withDate(dateDown);
    final S dataDown1Up2 = (S) _data.withSpot(sDown).withDate(dateUp);
    final S dataDown1Down2 = (S) _data.withSpot(sDown).withDate(dateDown);
    return new SingleGreekResult(getMixedSecondDerivative(dataUp1Up2, dataUp1Down2, dataDown1Up2, dataDown1Down2));
  }

  @Override
  public GreekResult<?> visitDriftlessTheta() {
    return null;
  }

  @Override
  public GreekResult<?> visitElasticity() {
    final double delta = ((SingleGreekResult) visitDelta()).getResult();
    final double price = ((SingleGreekResult) visitPrice()).getResult();
    return new SingleGreekResult(_data.getSpot() * delta / price);
  }

  @Override
  public GreekResult<?> visitGammaBleed() {
    final double s = _data.getSpot();
    final double sUp = s + EPS;
    final double sDown = s - EPS;
    final ZonedDateTime dateUp = DateUtil.getDateOffsetWithYearFraction(_data.getDate(), EPS);
    final ZonedDateTime dateDown = DateUtil.getDateOffsetWithYearFraction(_data.getDate(), -EPS);
    final S dataUp1Up1 = (S) _data.withSpot(sUp).withDate(dateUp);
    final S dataUp2 = (S) _data.withDate(dateUp);
    final S dataDown1Up2 = (S) _data.withSpot(sDown).withDate(dateUp);
    final S dataUp1Down2 = (S) _data.withSpot(sUp).withDate(dateDown);
    final S dataDown2 = (S) _data.withDate(dateDown);
    final S dataDown1Down2 = (S) _data.withSpot(sDown).withDate(dateDown);
    return new SingleGreekResult(getMixedThirdDerivative(dataUp1Up1, dataUp2, dataDown1Up2, dataUp1Down2, dataDown2, dataDown1Down2));
  }

  @Override
  public GreekResult<?> visitGammaP() {
    return new SingleGreekResult(getGammaP(0, 0));
  }

  @Override
  public GreekResult<?> visitGammaPBleed() {
    final double gammaPUp = getGammaP(0, EPS);
    final double gammaPDown = getGammaP(0, -EPS);
    return new SingleGreekResult((gammaPUp - gammaPDown) / (2 * EPS));
  }

  @Override
  public GreekResult<?> visitPhi() {
    return new SingleGreekResult(-((SingleGreekResult) visitCarryRho()).getResult());
  }

  @Override
  public GreekResult<?> visitSpeed() {
    final double s = _data.getSpot();
    final S dataUpUp = (S) _data.withSpot(s + 2 * EPS);
    final S dataUp = (S) _data.withSpot(s + EPS);
    final S dataDown = (S) _data.withSpot(s - EPS);
    return new SingleGreekResult(getThirdDerivative(dataUpUp, dataUp, _data, dataDown));
  }

  @Override
  public GreekResult<?> visitSpeedP() {
    final double gammaPUp = getGammaP(EPS, 0);
    final double gammaPDown = getGammaP(-EPS, 0);
    return new SingleGreekResult((gammaPUp - gammaPDown) / (2 * EPS));
  }

  @Override
  public GreekResult<?> visitStrikeDelta() {
    return null;
  }

  @Override
  public GreekResult<?> visitStrikeGamma() {
    return null;
  }

  @Override
  public GreekResult<?> visitUltima() {
    final double t = _definition.getTimeToExpiry(_data.getDate());
    final double sigma = _data.getVolatility(t, _definition.getStrike());
    final VolatilitySurface upUpSurface = new ConstantVolatilitySurface(sigma + 2 * EPS);
    final VolatilitySurface upSurface = new ConstantVolatilitySurface(sigma + EPS);
    final VolatilitySurface downSurface = new ConstantVolatilitySurface(sigma - EPS);
    final S dataUpUp = (S) _data.withVolatilitySurface(upUpSurface);
    final S dataUp = (S) _data.withVolatilitySurface(upSurface);
    final S dataDown = (S) _data.withVolatilitySurface(downSurface);
    return new SingleGreekResult(getThirdDerivative(dataUpUp, dataUp, _data, dataDown));
  }

  @Override
  public GreekResult<?> visitVanna() {
    final double t = _definition.getTimeToExpiry(_data.getDate());
    final double sigma = _data.getVolatility(t, _definition.getStrike());
    final double s = _data.getSpot();
    final double sUp = s + EPS;
    final double sDown = s - EPS;
    final VolatilitySurface upSurface = new ConstantVolatilitySurface(sigma + EPS);
    final VolatilitySurface downSurface = new ConstantVolatilitySurface(sigma - EPS);
    final S dataUp1Up2 = (S) _data.withSpot(sUp).withVolatilitySurface(upSurface);
    final S dataUp1Down2 = (S) _data.withSpot(sDown).withVolatilitySurface(upSurface);
    final S dataDown1Up2 = (S) _data.withSpot(sUp).withVolatilitySurface(downSurface);
    final S dataDown1Down2 = (S) _data.withSpot(sDown).withVolatilitySurface(downSurface);
    return new SingleGreekResult(getMixedSecondDerivative(dataUp1Up2, dataUp1Down2, dataDown1Up2, dataDown1Down2));
  }

  @Override
  public GreekResult<?> visitVarianceUltima() {
    final double t = _definition.getTimeToExpiry(_data.getDate());
    final double sigma = _data.getVolatility(t, _definition.getStrike());
    final double variance = sigma * sigma;
    final VolatilitySurface upUpSurface = new ConstantVolatilitySurface(Math.sqrt(variance + 2 * EPS));
    final VolatilitySurface upSurface = new ConstantVolatilitySurface(Math.sqrt(variance + EPS));
    final VolatilitySurface downSurface = new ConstantVolatilitySurface(Math.sqrt(variance - EPS));
    final S dataUpUp = (S) _data.withVolatilitySurface(upUpSurface);
    final S dataUp = (S) _data.withVolatilitySurface(upSurface);
    final S dataDown = (S) _data.withVolatilitySurface(downSurface);
    return new SingleGreekResult(getThirdDerivative(dataUpUp, dataUp, _data, dataDown));
  }

  @Override
  public GreekResult<?> visitVarianceVanna() {
    final double t = _definition.getTimeToExpiry(_data.getDate());
    final double sigma = _data.getVolatility(t, _definition.getStrike());
    final double variance = sigma * sigma;
    final double s = _data.getSpot();
    final double sUp = s + EPS;
    final double sDown = s - EPS;
    final VolatilitySurface upSurface = new ConstantVolatilitySurface(Math.sqrt(variance + EPS));
    final VolatilitySurface downSurface = new ConstantVolatilitySurface(Math.sqrt(variance - EPS));
    final S dataUp1Up2 = (S) _data.withVolatilitySurface(upSurface).withSpot(sUp);
    final S dataUp1Down2 = (S) _data.withVolatilitySurface(upSurface).withSpot(sDown);
    final S dataDown1Up2 = (S) _data.withVolatilitySurface(downSurface).withSpot(sUp);
    final S dataDown1Down2 = (S) _data.withVolatilitySurface(downSurface).withSpot(sDown);
    return new SingleGreekResult(_data.getSpot() * getMixedSecondDerivative(dataUp1Up2, dataUp1Down2, dataDown1Up2, dataDown1Down2));
  }

  @Override
  public GreekResult<?> visitVarianceVega() {
    final double t = _definition.getTimeToExpiry(_data.getDate());
    final double sigma = _data.getVolatility(t, _definition.getStrike());
    final double variance = sigma * sigma;
    final VolatilitySurface upSurface = new ConstantVolatilitySurface(Math.sqrt(variance + EPS));
    final VolatilitySurface downSurface = new ConstantVolatilitySurface(Math.sqrt(variance - EPS));
    final S dataUp = (S) _data.withVolatilitySurface(upSurface);
    final S dataDown = (S) _data.withVolatilitySurface(downSurface);
    return new SingleGreekResult(getFirstDerivative(dataUp, dataDown));
  }

  @Override
  public GreekResult<?> visitVarianceVomma() {
    final double t = _definition.getTimeToExpiry(_data.getDate());
    final double sigma = _data.getVolatility(t, _definition.getStrike());
    final double variance = sigma * sigma;
    final VolatilitySurface upSurface = new ConstantVolatilitySurface(Math.sqrt(variance + EPS));
    final VolatilitySurface downSurface = new ConstantVolatilitySurface(Math.sqrt(variance - EPS));
    final S dataUp = (S) _data.withVolatilitySurface(upSurface);
    final S dataDown = (S) _data.withVolatilitySurface(downSurface);
    return new SingleGreekResult(getSecondDerivative(dataUp, dataDown, _data));
  }

  @Override
  public GreekResult<?> visitVegaBleed() {
    final ZonedDateTime upDate = DateUtil.getDateOffsetWithYearFraction(_data.getDate(), EPS);
    final ZonedDateTime downDate = DateUtil.getDateOffsetWithYearFraction(_data.getDate(), -EPS);
    final double sigma = _data.getVolatility(_definition.getTimeToExpiry(_data.getDate()), _definition.getStrike());
    final VolatilitySurface upSurface = new ConstantVolatilitySurface(sigma + EPS);
    final VolatilitySurface downSurface = new ConstantVolatilitySurface(sigma - EPS);
    final S dataUp1Up2 = (S) _data.withVolatilitySurface(upSurface).withDate(upDate);
    final S dataUp1Down2 = (S) _data.withVolatilitySurface(upSurface).withDate(downDate);
    final S dataDown1Up2 = (S) _data.withVolatilitySurface(downSurface).withDate(upDate);
    final S dataDown1Down2 = (S) _data.withVolatilitySurface(downSurface).withDate(downDate);
    return new SingleGreekResult(getMixedSecondDerivative(dataUp1Up2, dataUp1Down2, dataDown1Up2, dataDown1Down2));
  }

  @Override
  public GreekResult<?> visitVegaP() {
    final double t = _definition.getTimeToExpiry(_data.getDate());
    final double sigma = _data.getVolatility(t, _definition.getStrike());
    return new SingleGreekResult(((SingleGreekResult) visitVega()).getResult() * sigma / 10);
  }

  @Override
  public GreekResult<?> visitVomma() {
    final double t = _definition.getTimeToExpiry(_data.getDate());
    final double sigma = _data.getVolatility(t, _definition.getStrike());
    final VolatilitySurface upSurface = new ConstantVolatilitySurface(sigma + EPS);
    final VolatilitySurface downSurface = new ConstantVolatilitySurface(sigma - EPS);
    final S dataUp = (S) _data.withVolatilitySurface(upSurface);
    final S dataDown = (S) _data.withVolatilitySurface(downSurface);
    return new SingleGreekResult(getSecondDerivative(dataUp, dataDown, _data));
  }

  @Override
  public GreekResult<?> visitVommaP() {
    final double t = _definition.getTimeToExpiry(_data.getDate());
    final double sigma = _data.getVolatility(t, _definition.getStrike());
    return new SingleGreekResult(((SingleGreekResult) visitVomma()).getResult() * sigma / 10);
  }

  @Override
  public GreekResult<?> visitZeta() {
    return null;
  }

  @Override
  public GreekResult<?> visitZetaBleed() {
    return null;
  }

  @Override
  public GreekResult<?> visitZomma() {
    final double t = _definition.getTimeToExpiry(_data.getDate());
    final double sigma = _data.getVolatility(t, _definition.getStrike());
    final double s = _data.getSpot();
    final double sUp = s + EPS;
    final double sDown = s - EPS;
    final VolatilitySurface upSurface = new ConstantVolatilitySurface(sigma + EPS);
    final VolatilitySurface downSurface = new ConstantVolatilitySurface(sigma - EPS);
    final S dataUp1Up1 = (S) _data.withSpot(sUp).withVolatilitySurface(upSurface);
    final S dataUp2 = (S) _data.withVolatilitySurface(upSurface);
    final S dataDown1Up2 = (S) _data.withSpot(sDown).withVolatilitySurface(upSurface);
    final S dataUp1Down2 = (S) _data.withSpot(sUp).withVolatilitySurface(downSurface);
    final S dataDown2 = (S) _data.withVolatilitySurface(downSurface);
    final S dataDown1Down2 = (S) _data.withSpot(sDown).withVolatilitySurface(downSurface);
    return new SingleGreekResult(getMixedThirdDerivative(dataUp1Up1, dataUp2, dataDown1Up2, dataUp1Down2, dataDown2, dataDown1Down2));
  }

  @Override
  public GreekResult<?> visitZommaP() {
    return new SingleGreekResult(((SingleGreekResult) visitZomma()).getResult() * _data.getSpot() / 100);
  }

  @Override
  public GreekResult<?> visitDVannaDVol() {
    final double t = _definition.getTimeToExpiry(_data.getDate());
    final double sigma = _data.getVolatility(t, _definition.getStrike());
    final double s = _data.getSpot();
    final double sUp = s + EPS;
    final double sDown = s - EPS;
    final VolatilitySurface upSurface = new ConstantVolatilitySurface(sigma + EPS);
    final VolatilitySurface downSurface = new ConstantVolatilitySurface(sigma - EPS);
    final S dataUp1Up1 = (S) _data.withVolatilitySurface(upSurface).withSpot(sUp);
    final S dataUp2 = (S) _data.withSpot(sUp);
    final S dataDown1Up2 = (S) _data.withVolatilitySurface(downSurface).withSpot(sUp);
    final S dataUp1Down2 = (S) _data.withVolatilitySurface(upSurface).withSpot(sDown);
    final S dataDown2 = (S) _data.withSpot(sDown);
    final S dataDown1Down2 = (S) _data.withVolatilitySurface(downSurface).withSpot(sDown);
    return new SingleGreekResult(getMixedThirdDerivative(dataUp1Up1, dataUp2, dataDown1Up2, dataUp1Down2, dataDown2, dataDown1Down2));
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
    return (_pricingFunction.evaluate(dataUp1Up2) - _pricingFunction.evaluate(dataUp1Down2) - _pricingFunction.evaluate(dataDown1Up2) + _pricingFunction.evaluate(dataDown1Down2))
        / (4 * EPS * EPS);
  }

  private double getThirdDerivative(final S dataUpUp, final S dataUp, final S data, final S dataDown) {
    return (_pricingFunction.evaluate(dataUpUp) - 3 * _pricingFunction.evaluate(dataUp) + 3 * _pricingFunction.evaluate(data) - _pricingFunction.evaluate(dataDown))
        / (EPS * EPS * EPS);
  }

  private double getMixedThirdDerivative(final S dataUp1Up1, final S dataUp2, final S dataDown1Up2, final S dataUp1Down2, final S dataDown2, final S dataDown1Down2) {
    return (_pricingFunction.evaluate(dataUp1Up1) - 2 * _pricingFunction.evaluate(dataUp2) + _pricingFunction.evaluate(dataDown1Up2) - _pricingFunction.evaluate(dataUp1Down2) + 2
        * _pricingFunction.evaluate(dataDown2) - _pricingFunction.evaluate(dataDown1Down2))
        / (2 * EPS * EPS * EPS);
  }

  private double getGammaP(final double spotOffset, final double tOffset) {
    final double spot = _data.getSpot() + spotOffset;
    final ZonedDateTime date = DateUtil.getDateOffsetWithYearFraction(_data.getDate(), tOffset);
    final S dataUp = (S) _data.withSpot(spot + EPS).withDate(date);
    final S dataDown = (S) _data.withSpot(spot - EPS).withDate(date);
    final S data = (S) _data.withSpot(spot).withDate(date);
    final double gamma = getSecondDerivative(dataUp, dataDown, data);
    return gamma * spot / 100;
  }
}