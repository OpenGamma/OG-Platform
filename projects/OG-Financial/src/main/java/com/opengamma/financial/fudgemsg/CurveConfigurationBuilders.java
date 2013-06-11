/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IndexCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IssuerCurveTypeConfiguration;
import com.opengamma.id.UniqueId;

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
      if (object.getUniqueId() != null) {
        serializer.addToMessageWithClassHeaders(message, UNIQUE_ID_FIELD, null, object.getUniqueId(), UniqueId.class);
      }
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
      if (object.getUniqueId() != null) {
        serializer.addToMessageWithClassHeaders(message, UNIQUE_ID_FIELD, null, object.getUniqueId(), UniqueId.class);
      }
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
      if (object.getUniqueId() != null) {
        serializer.addToMessageWithClassHeaders(message, UNIQUE_ID_FIELD, null, object.getUniqueId(), UniqueId.class);
      }
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

  static void setUniqueId(final FudgeDeserializer deserializer, final FudgeMsg message, final CurveTypeConfiguration configuration) {
    final FudgeField uniqueId = message.getByName(UNIQUE_ID_FIELD);
    if (uniqueId != null) {
      configuration.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueId));
    }
  }

}
