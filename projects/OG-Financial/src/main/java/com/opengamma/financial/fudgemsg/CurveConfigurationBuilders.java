/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IndexCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IssuerCurveTypeConfiguration;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdFudgeBuilder;
import com.opengamma.id.UniqueIdentifiable;

/**
 * 
 */
/* package */ final class CurveConfigurationBuilders {
  private static final String NAME_FIELD = "name";
  private static final String UNIQUE_ID_FIELD = "uniqueId";

  private CurveConfigurationBuilders() {
  }

  @FudgeBuilderFor(DiscountingCurveTypeConfiguration.class)
  public static class DiscountingCurveTypeConfigurationBuilder implements FudgeBuilder<DiscountingCurveTypeConfiguration> {
    private static final String CODE_FIELD = "code";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final DiscountingCurveTypeConfiguration object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      serializer.addToMessage(message, NAME_FIELD, null, object.getName());
      serializer.addToMessage(message, CODE_FIELD, null, object.getCode());
      addUniqueId(serializer, object, message);
      return message;
    }

    @Override
    public DiscountingCurveTypeConfiguration buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final String code = message.getString(CODE_FIELD);
      final DiscountingCurveTypeConfiguration configuration = new DiscountingCurveTypeConfiguration(name, code);
      setUniqueId(deserializer, message, configuration);
      return configuration;
    }

  }

  @FudgeBuilderFor(IndexCurveTypeConfiguration.class)
  public static class IndexCurveTypeConfigurationBuilder implements FudgeBuilder<IndexCurveTypeConfiguration> {
    private static final String CONVENTION_NAME_FIELD = "conventionName";
    private static final String INDEX_TYPE_FIELD = "indexType";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final IndexCurveTypeConfiguration object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      serializer.addToMessage(message, NAME_FIELD, null, object.getName());
      serializer.addToMessage(message, CONVENTION_NAME_FIELD, null, object.getConventionName());
      serializer.addToMessage(message, INDEX_TYPE_FIELD, null, object.getIndexType());
      addUniqueId(serializer, object, message);
      return message;
    }

    @Override
    public IndexCurveTypeConfiguration buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final String conventionName = message.getString(CONVENTION_NAME_FIELD);
      final String indexType = message.getString(INDEX_TYPE_FIELD);
      final IndexCurveTypeConfiguration configuration = new IndexCurveTypeConfiguration(name, conventionName, indexType);
      setUniqueId(deserializer, message, configuration);
      return configuration;
    }

  }

  @FudgeBuilderFor(IssuerCurveTypeConfiguration.class)
  public static class IssuerCurveTypeConfigurationBuilder implements FudgeBuilder<IssuerCurveTypeConfiguration> {
    private static final String ISSUER_NAME_FIELD = "issuerName";
    private static final String UNDERLYING_CODE_FIELD = "underlyingCode";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final IssuerCurveTypeConfiguration object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      serializer.addToMessage(message, NAME_FIELD, null, object.getName());
      serializer.addToMessage(message, ISSUER_NAME_FIELD, null, object.getIssuerName());
      serializer.addToMessage(message, UNDERLYING_CODE_FIELD, null, object.getUnderlyingCode());
      addUniqueId(serializer, object, message);
      return message;
    }

    @Override
    public IssuerCurveTypeConfiguration buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final String issuerName = message.getString(ISSUER_NAME_FIELD);
      final String underlyingCode = message.getString(UNDERLYING_CODE_FIELD);
      final IssuerCurveTypeConfiguration configuration = new IssuerCurveTypeConfiguration(name, issuerName, underlyingCode);
      setUniqueId(deserializer, message, configuration);
      return configuration;
    }

  }

  @FudgeBuilderFor(CurveGroupConfiguration.class)
  public static class CurveGroupConfigurationBuilder implements FudgeBuilder<CurveGroupConfiguration> {
    private static final String ORDER_FIELD = "order";
    private static final String CURVE_FIELD = "curve";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CurveGroupConfiguration object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      serializer.addToMessage(message, ORDER_FIELD, null, object.getOrder());
      for (final CurveTypeConfiguration curveType : object.getCurveTypes()) {
        serializer.addToMessageWithClassHeaders(message, CURVE_FIELD, null, curveType);
      }
      addUniqueId(serializer, object, message);
      return message;
    }

    @Override
    public CurveGroupConfiguration buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final int order = message.getInt(ORDER_FIELD);
      final List<FudgeField> curveTypeFields = message.getAllByName(CURVE_FIELD);
      final List<CurveTypeConfiguration> curveTypes = new ArrayList<>();
      for (final FudgeField field : curveTypeFields) {
        curveTypes.add(deserializer.fieldValueToObject(CurveTypeConfiguration.class, field));
      }
      final CurveGroupConfiguration configuration = new CurveGroupConfiguration(order, curveTypes);
      setUniqueId(deserializer, message, configuration);
      return configuration;
    }

  }

  @FudgeBuilderFor(CurveConstructionConfiguration.class)
  public static class CurveConstructionConfigurationBuilder implements FudgeBuilder<CurveConstructionConfiguration> {
    private static final String GROUP_FIELD = "group";
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
      List<String> exogenousConfigurations = null;
      final List<FudgeField> exogenousConfigFields = message.getAllByName(EXOGENOUS_CONFIGURATION_FIELD);
      if (!exogenousConfigFields.isEmpty()) {
        exogenousConfigurations = new ArrayList<>();
        for (final FudgeField field : exogenousConfigFields) {
          exogenousConfigurations.add((String) field.getValue());
        }
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
