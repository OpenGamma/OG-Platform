/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import com.opengamma.core.security.Security;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.PointsCurveNodeWithIdentifier;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.currency.CurrencyMatrixValue;
import com.opengamma.financial.currency.CurrencyMatrixValueVisitor;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.sesame.Environment;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Function2;
import com.opengamma.util.result.Result;

/**
 *
 */
@SuppressWarnings("unchecked")
public class DefaultMarketDataFn implements MarketDataFn {

  private final CurrencyMatrix _currencyMatrix;

  public DefaultMarketDataFn(CurrencyMatrix currencyMatrix) {
    _currencyMatrix = ArgumentChecker.notNull(currencyMatrix, "currencyMatrix");
  }

  @Override
  public Result<Double> getCurveNodeValue(Environment env, CurveNodeWithIdentifier node) {
    FieldName fieldName = FieldName.of(node.getDataField());
    ExternalIdBundle id = node.getIdentifier().toBundle();
    // this should use a link but that requires changes to the curve configuration classes.
    // CurveNodeWithIdentifier should refer to its data using a link (probably a SecurityLink) instead of an ID
    return env.getMarketDataBundle().get(RawId.of(id, Double.class, fieldName), Double.class);
  }

  @Override
  public Result<Double> getCurveNodeUnderlyingValue(Environment env, PointsCurveNodeWithIdentifier node) {
    ExternalIdBundle id = node.getUnderlyingIdentifier().toBundle();
    FieldName fieldName = FieldName.of(node.getUnderlyingDataField());
    return env.getMarketDataBundle().get(RawId.of(id, fieldName), Double.class);
  }

  @Override
  public Result<Double> getMarketValue(Environment env, Security security) {
    MarketDataId key = SecurityId.of(security);
    return env.getMarketDataBundle().get(key, Double.class);
  }

  @Override
  public <T> Result<T> getValue(Environment env, Security security, FieldName fieldName, Class<T> valueType) {
    MarketDataId key = SecurityId.of(security, valueType, fieldName);
    return env.getMarketDataBundle().get(key, valueType);
  }

  @Override
  public Result<Double> getFxRate(final Environment env, CurrencyPair currencyPair) {
    return getFxRate(env, currencyPair.getBase(), currencyPair.getCounter());
  }

  private Result<Double> getFxRate(final Environment env, final Currency base, final Currency counter) {
    CurrencyMatrixValue value = _currencyMatrix.getConversion(base, counter);
    if (value == null) {
      return Result.failure(FailureStatus.MISSING_DATA, "No conversion available for {}", CurrencyPair.of(base, counter));
    }
    CurrencyMatrixValueVisitor<Result<Double>> visitor = new CurrencyMatrixValueVisitor<Result<Double>>() {
      @Override
      public Result<Double> visitFixed(CurrencyMatrixValue.CurrencyMatrixFixed fixedValue) {
        return Result.success(fixedValue.getFixedValue());
      }

      @Override
      public Result<Double> visitValueRequirement(CurrencyMatrixValue.CurrencyMatrixValueRequirement req) {
        ValueRequirement valueRequirement = req.getValueRequirement();
        ExternalIdBundle id = valueRequirement.getTargetReference().getRequirement().getIdentifiers();
        String dataField = valueRequirement.getValueName();
        Result<Double> result =
            env.getMarketDataBundle().get(RawId.of(id, FieldName.of(dataField)), Double.class);

        if (result.isSuccess()) {
          Double spotRate = result.getValue();
          return Result.success(req.isReciprocal() ? 1 / spotRate : spotRate);
        } else {
          return Result.failure(result);
        }
      }

      @Override
      public Result<Double> visitCross(CurrencyMatrixValue.CurrencyMatrixCross cross) {
        Result<Double> baseCrossRate = getFxRate(env, base, cross.getCrossCurrency());
        Result<Double> crossCounterRate = getFxRate(env, cross.getCrossCurrency(), counter);

        return baseCrossRate.combineWith(crossCounterRate, new Function2<Double, Double, Result<Double>>() {
          @Override
          public Result<Double> apply(Double rate1, Double rate2) {
            return Result.success(rate1 * rate2);
          }
        });
      }
    };
    return value.accept(visitor);
  }
}
