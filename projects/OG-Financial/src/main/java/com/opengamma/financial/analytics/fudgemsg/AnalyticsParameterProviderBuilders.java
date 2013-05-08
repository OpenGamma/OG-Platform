/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;

/**
 * Contains builders for the objects that analytics needs to perform pricing.
 */
public final class AnalyticsParameterProviderBuilders {

  private AnalyticsParameterProviderBuilders() {
  }

  /**
   * Fudge builder for {@link IborIndex}
   */
  @FudgeBuilderFor(IborIndex.class)
  public static class IborIndexBuilder extends AbstractFudgeBuilder<IborIndex> {
    private static final String CURRENCY_FIELD = "currency";
    private static final String SPOT_LAG_FIELD = "spotLag";
    private static final String DAY_COUNT_FIELD = "dayCount";
    private static final String BUSINESS_DAY_CONVENTION_FIELD = "businessDayConvention";
    private static final String EOM_FIELD = "isEOM";
    private static final String TENOR_FIELD = "tenor";
    private static final String NAME_FIELD = "name";

    @Override
    public IborIndex buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Currency currency = Currency.of(message.getString(CURRENCY_FIELD));
      final int spotLag = message.getInt(SPOT_LAG_FIELD);
      final DayCount dayCount = DayCountFactory.INSTANCE.getDayCount(message.getString(DAY_COUNT_FIELD));
      final BusinessDayConvention businessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention(message.getString(BUSINESS_DAY_CONVENTION_FIELD));
      final boolean isEOM = message.getBoolean(EOM_FIELD);
      final Period tenor = Period.parse(message.getString(TENOR_FIELD));
      final String name = message.getString(NAME_FIELD);
      return new IborIndex(currency, tenor, spotLag, dayCount, businessDayConvention, isEOM, name);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final IborIndex object) {
      message.add(SPOT_LAG_FIELD, object.getSpotLag());
      message.add(DAY_COUNT_FIELD, object.getDayCount().getConventionName());
      message.add(BUSINESS_DAY_CONVENTION_FIELD, object.getBusinessDayConvention().getConventionName());
      message.add(EOM_FIELD, object.isEndOfMonth());
      message.add(TENOR_FIELD, object.getTenor().toString());
      message.add(NAME_FIELD, object.getName());
    }
  }

  /**
   * Fudge builder for {@link FXMatrix}
   */
  @FudgeBuilderFor(FXMatrix.class)
  public static class FXMatrixBuilder extends AbstractFudgeBuilder<FXMatrix> {
    private static final String CURRENCY_FIELD = "currency";
    private static final String ORDER_FIELD = "order";
    private static final String ENTRIES_FIELD = "entries";
    private static final String FX_RATES_FIELD = "fxRates";
    private static final String ROW_FIELD = "row";

    @Override
    public FXMatrix buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final List<FudgeField> currencies = message.getAllByName(CURRENCY_FIELD);
      final List<FudgeField> orders = message.getAllByName(ORDER_FIELD);
      final Map<Currency, Integer> map = new HashMap<>();
      for (int i = 0; i < currencies.size(); i++) {
        final Currency currency = Currency.of((String) currencies.get(i).getValue());
        final Integer order = ((Number) orders.get(i).getValue()).intValue();
        map.put(currency, order);
      }
      final List<FudgeField> entries = message.getAllByName(ENTRIES_FIELD);
      final List<FudgeField> arrays = message.getAllByName(FX_RATES_FIELD);
      final double[][] fxRates = new double[entries.size()][];
      for (int i = 0; i < entries.size(); i++) {
        final FudgeMsg msg = (FudgeMsg) arrays.get(i).getValue();
        final double[] row = deserializer.fieldValueToObject(double[].class, msg.getByName(ROW_FIELD));
        fxRates[i] = row;
      }
      return new FXMatrix(map, fxRates);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final FXMatrix object) {
      final Map<Currency, Integer> currencies = object.getCurrencies();
      for (final Map.Entry<Currency, Integer> entry : currencies.entrySet()) {
        message.add(CURRENCY_FIELD, entry.getKey().getCode());
        message.add(ORDER_FIELD, entry.getValue());
      }
      final double[][] rates = object.getRates();
      for (final double[] array : rates) {
        message.add(ENTRIES_FIELD, array.length);
        final MutableFudgeMsg msg = serializer.newMessage();
        serializer.addToMessageWithClassHeaders(msg, ROW_FIELD, null, array);
        message.add(FX_RATES_FIELD, msg);
      }
    }

  }

  /**
   * Fudge builder for {@link MulticurveProviderDiscount}
   */
  @FudgeBuilderFor(MulticurveProviderDiscount.class)
  public static class MulticurveProviderDiscountBuilder extends AbstractFudgeBuilder<MulticurveProviderDiscount> {
    private static final String CURRENCY_FIELD = "currency";
    private static final String DISCOUNTING_CURVE_FIELD = "discountingCurve";
    private static final String INDEX_ON_FIELD = "indexON";
    private static final String OVERNIGHT_CURVE_FIELD = "overnightCurve";
    private static final String INDEX_IBOR_FIELD = "iborIndex";
    private static final String INDEX_IBOR_CURVE = "iborCurve";
    private static final String FX_MATRIX_FIELD = "fxMatrix";

    @Override
    public MulticurveProviderDiscount buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Map<Currency, YieldAndDiscountCurve> discountingCurves = new LinkedHashMap<>();
      final List<FudgeField> currencyFields = message.getAllByName(CURRENCY_FIELD);
      final List<FudgeField> discountingCurveFields = message.getAllByName(DISCOUNTING_CURVE_FIELD);
      for (int i = 0; i < currencyFields.size(); i++) {
        final Currency currency = Currency.of((String) currencyFields.get(i).getValue());
        final YieldAndDiscountCurve curve = deserializer.fudgeMsgToObject(YieldAndDiscountCurve.class, (FudgeMsg) discountingCurveFields.get(i).getValue());
        discountingCurves.put(currency, curve);
      }
      final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves = new LinkedHashMap<>();
      final List<FudgeField> indexIborFields = message.getAllByName(INDEX_IBOR_FIELD);
      final List<FudgeField> forwardIborCurveFields = message.getAllByName(INDEX_IBOR_CURVE);
      for (int i = 0; i < currencyFields.size(); i++) {
        final IborIndex index = (IborIndex) indexIborFields.get(i).getValue();
        final YieldAndDiscountCurve curve = deserializer.fudgeMsgToObject(YieldAndDiscountCurve.class, (FudgeMsg) forwardIborCurveFields.get(i).getValue());
        forwardIborCurves.put(index, curve);
      }
      final Map<IndexON, YieldAndDiscountCurve> forwardONCurves = new LinkedHashMap<>();
      final List<FudgeField> indexONFields = message.getAllByName(INDEX_ON_FIELD);
      final List<FudgeField> forwardONCurveFields = message.getAllByName(OVERNIGHT_CURVE_FIELD);
      for (int i = 0; i < currencyFields.size(); i++) {
        final IborIndex index = (IborIndex) indexONFields.get(i).getValue();
        final YieldAndDiscountCurve curve = deserializer.fudgeMsgToObject(YieldAndDiscountCurve.class, (FudgeMsg) forwardONCurveFields.get(i).getValue());
        forwardIborCurves.put(index, curve);
      }
      final FXMatrix fxMatrix = deserializer.fieldValueToObject(FXMatrix.class, message.getByName(FX_MATRIX_FIELD));
      return new MulticurveProviderDiscount(discountingCurves, forwardIborCurves, forwardONCurves, fxMatrix);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final MulticurveProviderDiscount object) {
      final Map<Currency, YieldAndDiscountCurve> discountingCurves = object.getDiscountingCurves();
      for (final Map.Entry<Currency, YieldAndDiscountCurve> entry : discountingCurves.entrySet()) {
        message.add(CURRENCY_FIELD, entry.getKey().getCode());
        serializer.addToMessageWithClassHeaders(message, DISCOUNTING_CURVE_FIELD, null, entry.getValue());
      }
      final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves = object.getForwardIborCurves();
      for (final Map.Entry<IborIndex, YieldAndDiscountCurve> entry : forwardIborCurves.entrySet()) {
        serializer.addToMessageWithClassHeaders(message, INDEX_IBOR_FIELD, null, entry.getKey());
        serializer.addToMessageWithClassHeaders(message, INDEX_IBOR_CURVE, null, entry.getValue());
      }
      final Map<IndexON, YieldAndDiscountCurve> forwardONCurves = object.getForwardONCurves();
      for (final Map.Entry<IndexON, YieldAndDiscountCurve> entry : forwardONCurves.entrySet()) {
        serializer.addToMessageWithClassHeaders(message, INDEX_ON_FIELD, null, entry.getKey());
        serializer.addToMessageWithClassHeaders(message, OVERNIGHT_CURVE_FIELD, null, entry.getValue());
      }
      serializer.addToMessageWithClassHeaders(message, FX_MATRIX_FIELD, null, object.getFxRates());
    }

  }
}
