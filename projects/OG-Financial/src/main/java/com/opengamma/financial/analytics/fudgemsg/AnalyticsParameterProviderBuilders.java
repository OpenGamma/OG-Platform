/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

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
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderForward;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Contains builders for the objects that analytics needs to perform pricing.
 */
public final class AnalyticsParameterProviderBuilders {

  /**
   * Private constructor.
   */
  private AnalyticsParameterProviderBuilders() {
  }

  /**
   * Fudge builder for {@link IborIndex}
   */
  @FudgeBuilderFor(IborIndex.class)
  public static class IborIndexBuilder extends AbstractFudgeBuilder<IborIndex> {
    /** Currencies field */
    private static final String CURRENCY_FIELD = "currency";
    /** Spot lag field */
    private static final String SPOT_LAG_FIELD = "spotLag";
    /** Daycount field */
    private static final String DAY_COUNT_FIELD = "dayCount";
    /** Business day convention field */
    private static final String BUSINESS_DAY_CONVENTION_FIELD = "businessDayConvention";
    /** EOM convention field */
    private static final String EOM_FIELD = "isEOM";
    /** The tenor field */
    private static final String TENOR_FIELD = "tenor";
    /** The name field */
    private static final String NAME_FIELD = "name";

    @Override
    public IborIndex buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Currency currency = Currency.of(message.getString(CURRENCY_FIELD));
      final int spotLag = message.getInt(SPOT_LAG_FIELD);
      final DayCount dayCount = DayCountFactory.of(message.getString(DAY_COUNT_FIELD));
      final BusinessDayConvention businessDayConvention = BusinessDayConventionFactory.of(message.getString(BUSINESS_DAY_CONVENTION_FIELD));
      final boolean isEOM = message.getBoolean(EOM_FIELD);
      final Period tenor = Period.parse(message.getString(TENOR_FIELD));
      final String name = message.getString(NAME_FIELD);
      return new IborIndex(currency, tenor, spotLag, dayCount, businessDayConvention, isEOM, name);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final IborIndex object) {
      message.add(SPOT_LAG_FIELD, object.getSpotLag());
      message.add(DAY_COUNT_FIELD, object.getDayCount().getName());
      message.add(BUSINESS_DAY_CONVENTION_FIELD, object.getBusinessDayConvention().getName());
      message.add(EOM_FIELD, object.isEndOfMonth());
      message.add(TENOR_FIELD, object.getTenor().toString());
      message.add(NAME_FIELD, object.getName());
      message.add(CURRENCY_FIELD, object.getCurrency().getCode());
    }
  }

  /**
   * Fudge builder for {@link IndexON}
   */
  @FudgeBuilderFor(IndexON.class)
  public static class IndexONBuilder extends AbstractFudgeBuilder<IndexON> {
    /** Currencies field */
    private static final String CURRENCY_FIELD = "currency";
    /** Index name field */
    private static final String NAME_FIELD = "name";
    /** Daycount field */
    private static final String DAY_COUNT_FIELD = "dayCount";
    /** Publication lag field */
    private static final String PUBLICATION_LAG_FIELD = "publicationLag";

    @Override
    public IndexON buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final Currency currency = Currency.of(message.getString(CURRENCY_FIELD));
      final DayCount dayCount = DayCountFactory.of(message.getString(DAY_COUNT_FIELD));
      final int publicationLag = message.getInt(PUBLICATION_LAG_FIELD);
      return new IndexON(name, currency, dayCount, publicationLag);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final IndexON object) {
      message.add(CURRENCY_FIELD, object.getCurrency().getCode());
      message.add(NAME_FIELD, object.getName());
      message.add(DAY_COUNT_FIELD, object.getDayCount().getName());
      message.add(PUBLICATION_LAG_FIELD, object.getPublicationLag());
    }

  }

  /**
   * Fudge builder for {@link IndexPrice}
   */
  @FudgeBuilderFor(IndexPrice.class)
  public static class IndexPriceBuilder extends AbstractFudgeBuilder<IndexPrice> {
    /** Name field */
    private static final String NAME_FIELD = "name";
    /** Currencies field */
    private static final String CURRENCY_FIELD = "currency";

    @Override
    public IndexPrice buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final Currency currency = Currency.of(message.getString(CURRENCY_FIELD));
      return new IndexPrice(name, currency);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final IndexPrice object) {
      message.add(NAME_FIELD, object.getName());
      message.add(CURRENCY_FIELD, object.getCurrency().getCode());
    }

  }

  /**
   * Fudge builder for {@link FXMatrix}
   */
  @FudgeBuilderFor(FXMatrix.class)
  public static class FXMatrixBuilder extends AbstractFudgeBuilder<FXMatrix> {
    /** Currencies field */
    private static final String CURRENCY_FIELD = "currency";
    /** Order (of the entries) field */
    private static final String ORDER_FIELD = "order";
    /** Entries field */
    private static final String ENTRIES_FIELD = "entries";
    /** FX rates field */
    private static final String FX_RATES_FIELD = "fxRates";
    /** Row field */
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
    /** Currencies field */
    private static final String CURRENCY_FIELD = "currency";
    /** Discounting curves field */
    private static final String DISCOUNTING_CURVE_FIELD = "discountingCurve";
    /** Overnight indices field */
    private static final String INDEX_ON_FIELD = "indexON";
    /** Overnight curves field */
    private static final String OVERNIGHT_CURVE_FIELD = "overnightCurve";
    /** Index indices field */
    private static final String INDEX_IBOR_FIELD = "iborIndex";
    /** Ibor curves field */
    private static final String INDEX_IBOR_CURVE = "iborCurve";
    /** FX matrix field */
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
      for (int i = 0; i < indexIborFields.size(); i++) {
        final IborIndex index = deserializer.fudgeMsgToObject(IborIndex.class, (FudgeMsg) indexIborFields.get(i).getValue());
        final YieldAndDiscountCurve curve = deserializer.fudgeMsgToObject(YieldAndDiscountCurve.class, (FudgeMsg) forwardIborCurveFields.get(i).getValue());
        forwardIborCurves.put(index, curve);
      }
      final Map<IndexON, YieldAndDiscountCurve> forwardONCurves = new LinkedHashMap<>();
      final List<FudgeField> indexONFields = message.getAllByName(INDEX_ON_FIELD);
      final List<FudgeField> forwardONCurveFields = message.getAllByName(OVERNIGHT_CURVE_FIELD);
      for (int i = 0; i < indexONFields.size(); i++) {
        final IndexON index = deserializer.fudgeMsgToObject(IndexON.class, (FudgeMsg) indexONFields.get(i).getValue());
        final YieldAndDiscountCurve curve = deserializer.fudgeMsgToObject(YieldAndDiscountCurve.class, (FudgeMsg) forwardONCurveFields.get(i).getValue());
        forwardONCurves.put(index, curve);
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

  /**
   * Fudge builder for {@link MulticurveProviderForward}
   */
  @FudgeBuilderFor(MulticurveProviderForward.class)
  public static class MulticurveProviderForwardBuilder extends AbstractFudgeBuilder<MulticurveProviderForward> {
    /** Currencies field */
    private static final String CURRENCY_FIELD = "currency";
    /** Discounting curves field */
    private static final String DISCOUNTING_CURVE_FIELD = "discountingCurve";
    /** Overnight indices field */
    private static final String INDEX_ON_FIELD = "indexON";
    /** Overnight curves field */
    private static final String FORWARD_OVERNIGHT_CURVE_FIELD = "forwardOvernightCurve";
    /** Index indices field */
    private static final String INDEX_IBOR_FIELD = "iborIndex";
    /** Ibor curves field */
    private static final String FORWARD_IBOR_CURVE_FIELD = "forwardIborCurve";
    /** FX matrix field */
    private static final String FX_MATRIX_FIELD = "fxMatrix";

    @Override
    public MulticurveProviderForward buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Map<Currency, YieldAndDiscountCurve> discountingCurves = new LinkedHashMap<>();
      final List<FudgeField> currencyFields = message.getAllByName(CURRENCY_FIELD);
      final List<FudgeField> discountingCurveFields = message.getAllByName(DISCOUNTING_CURVE_FIELD);
      for (int i = 0; i < currencyFields.size(); i++) {
        final Currency currency = Currency.of((String) currencyFields.get(i).getValue());
        final YieldAndDiscountCurve curve = deserializer.fudgeMsgToObject(YieldAndDiscountCurve.class, (FudgeMsg) discountingCurveFields.get(i).getValue());
        discountingCurves.put(currency, curve);
      }
      final Map<IborIndex, DoublesCurve> forwardIborCurves = new LinkedHashMap<>();
      final List<FudgeField> indexIborFields = message.getAllByName(INDEX_IBOR_FIELD);
      final List<FudgeField> forwardIborCurveFields = message.getAllByName(FORWARD_IBOR_CURVE_FIELD);
      for (int i = 0; i < currencyFields.size(); i++) {
        final IborIndex index = deserializer.fudgeMsgToObject(IborIndex.class, (FudgeMsg) indexIborFields.get(i).getValue());
        final DoublesCurve curve = deserializer.fudgeMsgToObject(DoublesCurve.class, (FudgeMsg) forwardIborCurveFields.get(i).getValue());
        forwardIborCurves.put(index, curve);
      }
      final Map<IndexON, YieldAndDiscountCurve> forwardONCurves = new LinkedHashMap<>();
      final List<FudgeField> indexONFields = message.getAllByName(INDEX_ON_FIELD);
      final List<FudgeField> forwardONCurveFields = message.getAllByName(FORWARD_OVERNIGHT_CURVE_FIELD);
      for (int i = 0; i < currencyFields.size(); i++) {
        final IndexON index = deserializer.fudgeMsgToObject(IndexON.class, (FudgeMsg) indexONFields.get(i).getValue());
        final YieldAndDiscountCurve curve = deserializer.fudgeMsgToObject(YieldAndDiscountCurve.class, (FudgeMsg) forwardONCurveFields.get(i).getValue());
        forwardONCurves.put(index, curve);
      }
      final FXMatrix fxMatrix = deserializer.fieldValueToObject(FXMatrix.class, message.getByName(FX_MATRIX_FIELD));
      return new MulticurveProviderForward(discountingCurves, forwardIborCurves, forwardONCurves, fxMatrix);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final MulticurveProviderForward object) {
      final Map<Currency, YieldAndDiscountCurve> discountingCurves = object.getDiscountingCurves();
      for (final Map.Entry<Currency, YieldAndDiscountCurve> entry : discountingCurves.entrySet()) {
        message.add(CURRENCY_FIELD, entry.getKey().getCode());
        serializer.addToMessageWithClassHeaders(message, DISCOUNTING_CURVE_FIELD, null, entry.getValue());
      }
      final Map<IborIndex, DoublesCurve> forwardIborCurves = object.getForwardIborCurves();
      for (final Map.Entry<IborIndex, DoublesCurve> entry : forwardIborCurves.entrySet()) {
        serializer.addToMessageWithClassHeaders(message, INDEX_IBOR_FIELD, null, entry.getKey());
        serializer.addToMessageWithClassHeaders(message, FORWARD_IBOR_CURVE_FIELD, null, entry.getValue());
      }
      final Map<IndexON, YieldAndDiscountCurve> forwardONCurves = object.getForwardONCurves();
      for (final Map.Entry<IndexON, YieldAndDiscountCurve> entry : forwardONCurves.entrySet()) {
        serializer.addToMessageWithClassHeaders(message, INDEX_ON_FIELD, null, entry.getKey());
        serializer.addToMessageWithClassHeaders(message, FORWARD_OVERNIGHT_CURVE_FIELD, null, entry.getValue());
      }
      serializer.addToMessageWithClassHeaders(message, FX_MATRIX_FIELD, null, object.getFxRates());
    }

  }

  /**
   * Fudge builder for {@link InflationProviderDiscount}
   */
  @FudgeBuilderFor(InflationProviderDiscount.class)
  public static class InflationProviderDiscountBuilder extends AbstractFudgeBuilder<InflationProviderDiscount> {
    /** The multi-curve provider */
    private static final String YIELD_CURVES_FIELD = "yieldCurves";
    /** Price index field */
    private static final String PRICE_INDEX_FIELD = "priceIndex";
    /** Price index curve field */
    private static final String PRICE_INDEX_CURVE_FIELD = "priceIndexCurve";

    @Override
    public InflationProviderDiscount buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final MulticurveProviderDiscount yieldCurves = deserializer.fieldValueToObject(MulticurveProviderDiscount.class, message.getByName(YIELD_CURVES_FIELD));
      final List<FudgeField> indexFields = message.getAllByName(PRICE_INDEX_FIELD);
      final List<FudgeField> indexCurveFields = message.getAllByName(PRICE_INDEX_CURVE_FIELD);
      final Map<IndexPrice, PriceIndexCurve> priceIndexCurves = new LinkedHashMap<>();
      final int n = indexFields.size();
      for (int i = 0; i < n; i++) {
        final IndexPrice index = deserializer.fudgeMsgToObject(IndexPrice.class, (FudgeMsg) indexFields.get(i).getValue());
        final PriceIndexCurve curve = deserializer.fudgeMsgToObject(PriceIndexCurve.class, (FudgeMsg) indexCurveFields.get(i).getValue());
        priceIndexCurves.put(index, curve);
      }
      return new InflationProviderDiscount(yieldCurves, priceIndexCurves);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final InflationProviderDiscount object) {
      serializer.addToMessageWithClassHeaders(message, YIELD_CURVES_FIELD, null, object.getMulticurveProvider());
      final Map<IndexPrice, PriceIndexCurve> priceIndexCurves = object.getPriceIndexCurves();
      for (final Map.Entry<IndexPrice, PriceIndexCurve> entry : priceIndexCurves.entrySet()) {
        serializer.addToMessageWithClassHeaders(message, PRICE_INDEX_FIELD, null, entry.getKey());
        serializer.addToMessageWithClassHeaders(message, PRICE_INDEX_CURVE_FIELD, null, entry.getValue());
      }
    }

  }

  /**
   * Fudge builder for {@link IssuerProviderDiscount}
   */
  @FudgeBuilderFor(IssuerProviderDiscount.class)
  public static class IssuerProviderDiscountBuilder extends AbstractFudgeBuilder<IssuerProviderDiscount> {
    /** The curve provider field */
    private static final String CURVE_PROVIDER_FIELD = "curveProvider";
    /** The issuer reference class field */
    private static final String ISSUER_REFERENCE_CLASS_FIELD = "issuerReferenceClass";
    /** The issuer reference field */
    private static final String ISSUER_REFERENCE_FIELD = "issuerReference";
    /** The issuer legal entity filter */
    private static final String ISSUER_FILTER_FIELD = "issuerFilter";
    /** The issuer curve field */
    private static final String ISSUER_CURVE_FIELD = "issuerCurve";

    @Override
    public IssuerProviderDiscount buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final MulticurveProviderDiscount multicurves = deserializer.fieldValueToObject(MulticurveProviderDiscount.class, message.getByName(CURVE_PROVIDER_FIELD));
      final List<FudgeField> issuerClassFields = message.getAllByName(ISSUER_REFERENCE_CLASS_FIELD);
      final List<FudgeField> issuerReferenceFields = message.getAllByName(ISSUER_REFERENCE_FIELD);
      final List<FudgeField> issuerFilterFields = message.getAllByName(ISSUER_FILTER_FIELD);
      final List<FudgeField> issuerCurveFields = message.getAllByName(ISSUER_CURVE_FIELD);
      final Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> issuerCurves = new HashMap<>();
      for (int i = 0; i < issuerReferenceFields.size(); i++) {
        final Class<?> clazz = deserializer.fieldValueToObject(Class.class, issuerClassFields.get(i));
        final Object issuerReference = deserializer.fieldValueToObject(clazz, issuerReferenceFields.get(i));
        final LegalEntityFilter<LegalEntity> issuerFilter = deserializer.fieldValueToObject(LegalEntityFilter.class, issuerFilterFields.get(i));
        final YieldAndDiscountCurve curve = deserializer.fieldValueToObject(YieldAndDiscountCurve.class, issuerCurveFields.get(i));
        issuerCurves.put(Pairs.<Object, LegalEntityFilter<LegalEntity>>of(issuerReference, issuerFilter), curve);
      }
      return new IssuerProviderDiscount(multicurves, issuerCurves);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final IssuerProviderDiscount object) {
      serializer.addToMessageWithClassHeaders(message, CURVE_PROVIDER_FIELD, null, object.getMulticurveProvider());
      for (final Map.Entry<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> entry : object.getIssuerCurves().entrySet()) {
        serializer.addToMessage(message, ISSUER_REFERENCE_CLASS_FIELD, null, entry.getKey().getFirst().getClass());
        serializer.addToMessageWithClassHeaders(message, ISSUER_REFERENCE_FIELD, null, entry.getKey().getFirst());
        serializer.addToMessageWithClassHeaders(message, ISSUER_FILTER_FIELD, null, entry.getKey().getSecond());
        serializer.addToMessageWithClassHeaders(message, ISSUER_CURVE_FIELD, null, entry.getValue());
      }
    }

  }

  /**
   * Fudge builder for {@link HullWhiteOneFactorProviderDiscount}
   */
  @FudgeBuilderFor(HullWhiteOneFactorProviderDiscount.class)
  public static class HullWhiteOneFactorProviderDiscountBuilder extends AbstractFudgeBuilder<HullWhiteOneFactorProviderDiscount> {
    /** The curve provider field */
    private static final String CURVE_PROVIDER_FIELD = "curveProvider";
    /** The Hull-White parameters field */
    private static final String HULL_WHITE_PARAMETERS_FIELD = "hullWhiteParameters";
    /** The currency field */
    private static final String CURRENCY_FIELD = "currency";

    @Override
    public HullWhiteOneFactorProviderDiscount buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final MulticurveProviderDiscount multicurves = deserializer.fieldValueToObject(MulticurveProviderDiscount.class, message.getByName(CURVE_PROVIDER_FIELD));
      final HullWhiteOneFactorPiecewiseConstantParameters parameters = deserializer.fieldValueToObject(HullWhiteOneFactorPiecewiseConstantParameters.class,
          message.getByName(HULL_WHITE_PARAMETERS_FIELD));
      final Currency currency = Currency.of(message.getString(CURRENCY_FIELD));
      return new HullWhiteOneFactorProviderDiscount(multicurves, parameters, currency);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final HullWhiteOneFactorProviderDiscount object) {
      serializer.addToMessageWithClassHeaders(message, CURVE_PROVIDER_FIELD, null, object.getMulticurveProvider());
      serializer.addToMessageWithClassHeaders(message, HULL_WHITE_PARAMETERS_FIELD, null, object.getHullWhiteParameters());
      message.add(CURRENCY_FIELD, object.getHullWhiteCurrency().getCode());
    }

  }

  /**
   * Fudge builder for {@link HullWhiteOneFactorPiecewiseConstantParameters}
   */
  @FudgeBuilderFor(HullWhiteOneFactorPiecewiseConstantParameters.class)
  public static class HullWhiteOneFactorPiecewiseConstantParametersBuilder extends AbstractFudgeBuilder<HullWhiteOneFactorPiecewiseConstantParameters> {
    /** The mean reversion field */
    private static final String MEAN_REVERSION_FIELD = "meanReversion";
    /** The volatility field */
    private static final String VOLATILITY_FIELD = "volatility";
    /** The time field */
    private static final String TIME_FIELD = "time";

    @Override
    public HullWhiteOneFactorPiecewiseConstantParameters buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final double meanReversion = message.getDouble(MEAN_REVERSION_FIELD);
      final List<FudgeField> volatilityFields = message.getAllByName(VOLATILITY_FIELD);
      int n = volatilityFields.size();
      final double[] volatilities = new double[n];
      for (int i = 0; i < n; i++) {
        volatilities[i] = (Double) volatilityFields.get(i).getValue();
      }
      final List<FudgeField> timeFields = message.getAllByName(TIME_FIELD);
      n = timeFields.size();
      final double[] times = new double[n];
      for (int i = 0; i < n; i++) {
        times[i] = (Double) timeFields.get(i).getValue();
      }
      return new HullWhiteOneFactorPiecewiseConstantParameters(meanReversion, volatilities, times);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final HullWhiteOneFactorPiecewiseConstantParameters object) {
      message.add(MEAN_REVERSION_FIELD, object.getMeanReversion());
      final double[] volatility = object.getVolatility();
      final double[] volatilityTime = object.getVolatilityTime();
      for (int i = 0; i < volatility.length; i++) {
        message.add(VOLATILITY_FIELD, volatility[i]);
        message.add(TIME_FIELD, volatilityTime[i + 1]); //values are added to the time array in the constructor
      }
    }

  }

  /**
   * Fudge builder for {@link G2ppPiecewiseConstantParameters}
   */
  @FudgeBuilderFor(G2ppPiecewiseConstantParameters.class)
  public static class G2ppPiecewiseConstantParametersBuilder extends AbstractFudgeBuilder<G2ppPiecewiseConstantParameters> {
    /** The mean reversion fields */
    private static final String MEAN_REVERSION_FIELD = "meanReversion";
    /** The volatility fields */
    private static final String VOLATILITIES_FIELD = "volatilities";
    /** The time fields */
    private static final String TIME_FIELD = "times";
    /** The correlation field */
    private static final String CORRELATION_FIELD = "correlation";

    @Override
    public G2ppPiecewiseConstantParameters buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final List<FudgeField> meanReversionFields = message.getAllByName(MEAN_REVERSION_FIELD);
      int n = meanReversionFields.size();
      final double[] meanReversion = new double[n];
      for (int i = 0; i < n; i++) {
        meanReversion[i] = (Double) meanReversionFields.get(i).getValue();
      }
      final List<FudgeField> volatilityFields = message.getAllByName(VOLATILITIES_FIELD);
      n = volatilityFields.size();
      final double[][] volatilities = new double[n][];
      for (int i = 0; i < n; i++) {
        volatilities[i] = deserializer.fieldValueToObject(double[].class, volatilityFields.get(i));
      }
      final List<FudgeField> timeFields = message.getAllByName(TIME_FIELD);
      n = timeFields.size();
      final double[] times = new double[n];
      for (int i = 0; i < n; i++) {
        times[i] = (Double) timeFields.get(i).getValue();
      }
      final double correlation = message.getDouble(CORRELATION_FIELD);
      return new G2ppPiecewiseConstantParameters(meanReversion, volatilities, times, correlation);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final G2ppPiecewiseConstantParameters object) {
      final double[] meanReversion = object.getMeanReversion();
      for (final double element : meanReversion) {
        message.add(MEAN_REVERSION_FIELD, element);
      }
      final DoubleArrayList[] volatility = object.getVolatility();
      for (final DoubleArrayList element : volatility) {
        serializer.addToMessage(message, VOLATILITIES_FIELD, null, element.toDoubleArray());
      }
      final double[] volatilityTime = object.getVolatilityTime();
      for (int i = 0; i < volatilityTime.length - 2; i++) {
        message.add(TIME_FIELD, volatilityTime[i + 1]); //values added to front and back of times array on construction
      }
      message.add(CORRELATION_FIELD, object.getCorrelation());
    }

  }
}
