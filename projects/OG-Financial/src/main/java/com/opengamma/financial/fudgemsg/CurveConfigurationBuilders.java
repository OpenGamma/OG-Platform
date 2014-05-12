/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.Period;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InflationCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InflationIssuerCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IssuerCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.OvernightCurveTypeConfiguration;
import com.opengamma.id.ExternalId;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdFudgeBuilder;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Builders for curve construction configurations.
 */
/* package */final class CurveConfigurationBuilders {
  /** The name field */
  private static final String NAME_FIELD = "name";
  /** The unique id field */
  private static final String UNIQUE_ID_FIELD = "uniqueId";

  /**
   * Private constructor.
   */
  private CurveConfigurationBuilders() {
  }

  /**
   * Fudge builder for {@link DiscountingCurveTypeConfiguration}
   */
  @FudgeBuilderFor(DiscountingCurveTypeConfiguration.class)
  public static class DiscountingCurveTypeConfigurationBuilder implements FudgeBuilder<DiscountingCurveTypeConfiguration> {
    /** The reference field */
    private static final String REFERENCE_FIELD = "reference";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final DiscountingCurveTypeConfiguration object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      serializer.addToMessage(message, REFERENCE_FIELD, null, object.getReference());
      return message;
    }

    @Override
    public DiscountingCurveTypeConfiguration buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String reference = message.getString(REFERENCE_FIELD);
      final DiscountingCurveTypeConfiguration configuration = new DiscountingCurveTypeConfiguration(reference);
      return configuration;
    }

  }

  /**
   * Fudge builder for {@link IborCurveTypeConfiguration}
   */
  @FudgeBuilderFor(IborCurveTypeConfiguration.class)
  public static class IborCurveTypeConfigurationBuilder implements FudgeBuilder<IborCurveTypeConfiguration> {
    /** The convention name field */
    private static final String CONVENTION_FIELD = "convention";
    /** The index type field */
    private static final String TENOR_FIELD = "indexTenor";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final IborCurveTypeConfiguration object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      serializer.addToMessage(message, CONVENTION_FIELD, null, object.getConvention());
      serializer.addToMessage(message, TENOR_FIELD, null, object.getTenor().getPeriod().toString());
      return message;
    }

    @Override
    public IborCurveTypeConfiguration buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final ExternalId convention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(CONVENTION_FIELD));
      final Tenor tenor = Tenor.of(Period.parse(message.getString(TENOR_FIELD)));
      return new IborCurveTypeConfiguration(convention, tenor);
    }

  }

  /**
   * Fudge builder for {@link OvernightCurveTypeConfiguration}
   */
  @FudgeBuilderFor(OvernightCurveTypeConfiguration.class)
  public static class OvernightCurveTypeConfigurationBuilder implements FudgeBuilder<OvernightCurveTypeConfiguration> {
    /** The convention name field */
    private static final String CONVENTION_FIELD = "convention";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final OvernightCurveTypeConfiguration object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      serializer.addToMessage(message, CONVENTION_FIELD, null, object.getConvention());
      return message;
    }

    @Override
    public OvernightCurveTypeConfiguration buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final ExternalId convention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(CONVENTION_FIELD));
      final OvernightCurveTypeConfiguration configuration = new OvernightCurveTypeConfiguration(convention);
      return configuration;
    }

  }

  /**
   * Fudge builder for {@link IssuerCurveTypeConfiguration}
   */
  @FudgeBuilderFor(IssuerCurveTypeConfiguration.class)
  public static class IssuerCurveTypeConfigurationBuilder implements FudgeBuilder<IssuerCurveTypeConfiguration> {
    /**
     * The issuer type field
     * @deprecated This field is kept for backwards compatibility
     */
    @Deprecated
    private static final String ISSUER_NAME_FIELD = "issuerName";
    /**
     * The underlying reference field
     * @deprecated This field is kept for backwards compatibility
     */
    @Deprecated
    private static final String UNDERLYING_REFERENCE_FIELD = "underlyingReference";
    /** The key field */
    private static final String KEY_FIELD = "key";
    /** The key class field */
    private static final String KEY_CLASS_FIELD = "keyClass";
    /** The legal entity filter field */
    private static final String LEGAL_ENTITY_FILTER_FIELD = "filter";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final IssuerCurveTypeConfiguration object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      for (final Object key : object.getKeys()) {
        serializer.addToMessageWithClassHeaders(message, KEY_FIELD, null, key);
        serializer.addToMessage(message, KEY_CLASS_FIELD, null, key.getClass());
      }
      for (final LegalEntityFilter<LegalEntity> filter : object.getFilters().getFiltersToUse()) {
        serializer.addToMessageWithClassHeaders(message, LEGAL_ENTITY_FILTER_FIELD, null, filter);
      }
      return message;
    }

    @SuppressWarnings("deprecation")
    @Override
    public IssuerCurveTypeConfiguration buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      if (message.hasField(ISSUER_NAME_FIELD)) {
        if (message.hasField(UNDERLYING_REFERENCE_FIELD)) {
          final String issuerName = message.getString(ISSUER_NAME_FIELD);
          final String underlyingReference = message.getString(UNDERLYING_REFERENCE_FIELD);
          return new IssuerCurveTypeConfiguration(issuerName, underlyingReference);
        }
        throw new IllegalStateException("Configuration has " + ISSUER_NAME_FIELD + " but no " + UNDERLYING_REFERENCE_FIELD);
      }
      final List<FudgeField> keyFields = message.getAllByName(KEY_FIELD);
      final List<FudgeField> keyClassFields = message.getAllByName(KEY_CLASS_FIELD);
      final Set<Object> keys = new HashSet<>();
      for (int i = 0; i < keyFields.size(); i++) {
        final Class<?> clazz = deserializer.fieldValueToObject(Class.class, keyClassFields.get(i));
        final Object value = deserializer.fieldValueToObject(clazz, keyFields.get(i));
        if (clazz.equals(String.class)) {
          try {
            keys.add(Currency.of((String) value));
          } catch (final IllegalArgumentException e) {
            keys.add(value);
          }
        } else {
          keys.add(value);
        }
      }
      final List<FudgeField> filterFields = message.getAllByName(LEGAL_ENTITY_FILTER_FIELD);
      final Set<LegalEntityFilter<LegalEntity>> filters = new HashSet<>();
      for (final FudgeField field : filterFields) {
        filters.add((LegalEntityFilter<LegalEntity>) deserializer.fieldValueToObject(field));
      }
      return new IssuerCurveTypeConfiguration(keys, filters);
    }

  }

  /**
   * Fudge builder for {@link InflationCurveTypeConfiguration}
   */
  @FudgeBuilderFor(InflationCurveTypeConfiguration.class)
  public static class InflationCurveTypeConfigurationBuilder implements FudgeBuilder<InflationCurveTypeConfiguration> {
    /** The reference field */
    private static final String REFERENCE_FIELD = "reference";
    /** The price index field */
    private static final String PRICE_INDEX_FIELD = "priceIndex";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final InflationCurveTypeConfiguration object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(REFERENCE_FIELD, object.getReference());
      serializer.addToMessage(message, PRICE_INDEX_FIELD, null, object.getPriceIndex());
      return message;
    }

    @Override
    public InflationCurveTypeConfiguration buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String reference = message.getString(REFERENCE_FIELD);
      final ExternalId priceIndex = deserializer.fieldValueToObject(ExternalId.class, message.getByName(PRICE_INDEX_FIELD));
      final InflationCurveTypeConfiguration configuration = new InflationCurveTypeConfiguration(reference, priceIndex);
      return configuration;
    }

  }

  /**
   * Fudge builder for {@link InflationIssuerCurveTypeConfiguration}
   */
  @FudgeBuilderFor(InflationIssuerCurveTypeConfiguration.class)
  public static class InflationIssuerCurveTypeConfigurationBuilder implements FudgeBuilder<InflationIssuerCurveTypeConfiguration> {
    /** The reference field */
    private static final String REFERENCE_FIELD = "reference";
    /** The price index field */
    private static final String PRICE_INDEX_FIELD = "priceIndex";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final InflationIssuerCurveTypeConfiguration object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(REFERENCE_FIELD, object.getReference());
      serializer.addToMessage(message, PRICE_INDEX_FIELD, null, object.getPriceIndex());
      return message;
    }

    @Override
    public InflationIssuerCurveTypeConfiguration buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String reference = message.getString(REFERENCE_FIELD);
      final ExternalId priceIndex = deserializer.fieldValueToObject(ExternalId.class, message.getByName(PRICE_INDEX_FIELD));
      final InflationIssuerCurveTypeConfiguration configuration = new InflationIssuerCurveTypeConfiguration(reference, priceIndex);
      return configuration;
    }

  }

  /**
   * Fudge builder for {@link CurveGroupConfiguration}
   */
  @FudgeBuilderFor(CurveGroupConfiguration.class)
  public static class CurveGroupConfigurationBuilder implements FudgeBuilder<CurveGroupConfiguration> {
    /** The order field */
    private static final String ORDER_FIELD = "order";
    /** The curve field */
    private static final String CURVE_FIELD = "curveName";
    /** The curve types field */
    private static final String CURVE_TYPES_FIELD = "typesForCurve";
    /** The curve type field */
    private static final String CURVE_TYPE_FIELD = "type";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CurveGroupConfiguration object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      serializer.addToMessage(message, ORDER_FIELD, null, object.getOrder());
      for (final Map.Entry<String, List<? extends CurveTypeConfiguration>> entry : object.getTypesForCurves().entrySet()) {
        final MutableFudgeMsg subMessage = serializer.newMessage();
        message.add(CURVE_FIELD, entry.getKey());
        for (final CurveTypeConfiguration type : entry.getValue()) {
          serializer.addToMessageWithClassHeaders(subMessage, CURVE_TYPE_FIELD, null, type);
        }
        message.add(CURVE_TYPES_FIELD, subMessage);
      }
      return message;
    }

    @Override
    public CurveGroupConfiguration buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final int order = message.getInt(ORDER_FIELD);
      final List<FudgeField> curveFields = message.getAllByName(CURVE_FIELD);
      final List<FudgeField> typesForCurveFields = message.getAllByName(CURVE_TYPES_FIELD);
      final int n = curveFields.size();
      if (typesForCurveFields.size() != n) {
        throw new OpenGammaRuntimeException("Did not have types for each curve name");
      }
      final Map<String, List<? extends CurveTypeConfiguration>> curveTypes = new HashMap<>();
      for (int i = 0; i < n; i++) {
        final FudgeField nameField = curveFields.get(i);
        final String name = deserializer.fieldValueToObject(String.class, nameField);
        final List<FudgeField> msgForCurve = ((FudgeMsg) typesForCurveFields.get(i).getValue()).getAllByName(CURVE_TYPE_FIELD);
        final List<CurveTypeConfiguration> list = new ArrayList<>();
        for (final FudgeField field : msgForCurve) {
          list.add(deserializer.fieldValueToObject(CurveTypeConfiguration.class, field));
        }
        curveTypes.put(name, list);
      }
      final CurveGroupConfiguration configuration = new CurveGroupConfiguration(order, curveTypes);
      return configuration;
    }

  }

  /**
   * Fudge builder for {@link CurveConstructionConfiguration}
   */
  @FudgeBuilderFor(CurveConstructionConfiguration.class)
  public static class CurveConstructionConfigurationBuilder implements FudgeBuilder<CurveConstructionConfiguration> {
    /** The group field */
    private static final String GROUP_FIELD = "group";
    /** The exogenous configuration field */
    private static final String EXOGENOUS_CONFIGURATION_FIELD = "exogenousConfiguration";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CurveConstructionConfiguration object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(NAME_FIELD, object.getName());
      for (final CurveGroupConfiguration curveType : object.getCurveGroups()) {
        serializer.addToMessageWithClassHeaders(message, GROUP_FIELD, null, curveType);
      }
      if (object.getExogenousConfigurations() != null) {
        for (final String exogenousConfig : object.getExogenousConfigurations()) {
          serializer.addToMessage(message, EXOGENOUS_CONFIGURATION_FIELD, null, exogenousConfig);
        }
      }
      addUniqueId(serializer, object, message);
      return message;
    }

    @Override
    public CurveConstructionConfiguration buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final List<FudgeField> curveTypeFields = message.getAllByName(GROUP_FIELD);
      final List<CurveGroupConfiguration> curveTypes = new ArrayList<>();
      for (final FudgeField field : curveTypeFields) {
        curveTypes.add(deserializer.fieldValueToObject(CurveGroupConfiguration.class, field));
      }
      List<String> exogenousConfigurations = new ArrayList<>();
      for (final FudgeField field : message.getAllByName(EXOGENOUS_CONFIGURATION_FIELD)) {
        exogenousConfigurations.add((String) field.getValue());
      }
      final CurveConstructionConfiguration configuration = new CurveConstructionConfiguration(name, curveTypes, exogenousConfigurations);
      setUniqueId(deserializer, message, configuration);
      return configuration;
    }

  }

  static void addUniqueId(final FudgeSerializer serializer, final UniqueIdentifiable object, final MutableFudgeMsg message) {
    if (object.getUniqueId() != null) {
      message.add(UNIQUE_ID_FIELD, null, UniqueIdFudgeBuilder.toFudgeMsg(serializer, object.getUniqueId()));
    }
  }

  static void setUniqueId(final FudgeDeserializer deserializer, final FudgeMsg message, final MutableUniqueIdentifiable configuration) {
    final FudgeField uniqueId = message.getByName(UNIQUE_ID_FIELD);
    if (uniqueId != null) {
      configuration.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueId));
    }
  }

}
