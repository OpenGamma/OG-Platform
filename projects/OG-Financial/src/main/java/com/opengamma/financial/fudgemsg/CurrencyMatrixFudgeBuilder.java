/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgFactory;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;
import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.financial.currency.AbstractCurrencyMatrix;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.currency.CurrencyMatrixValue;
import com.opengamma.financial.currency.CurrencyMatrixValue.CurrencyMatrixCross;
import com.opengamma.financial.currency.CurrencyMatrixValue.CurrencyMatrixFixed;
import com.opengamma.financial.currency.CurrencyMatrixValue.CurrencyMatrixValueRequirement;
import com.opengamma.financial.currency.CurrencyMatrixValueVisitor;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Fudge builder for a {@link CurrencyMatrix}. This handles the general case - matrices may typically be sparse so
 * there may be more efficient encodings possible. In those cases, serialize and add class headers directly.
 */
@GenericFudgeBuilderFor(CurrencyMatrix.class)
public class CurrencyMatrixFudgeBuilder implements FudgeBuilder<CurrencyMatrix> {

  /** Field name. */
  public static final String UNIQUE_ID_FIELD_NAME = "uniqueId";
  /** Field name. */
  public static final String FIXED_RATE_FIELD_NAME = "fixedRate";
  /** Field name. */
  public static final String VALUE_REQUIREMENTS_FIELD_NAME = "valueReq";
  /** Field name. */
  public static final String CROSS_CONVERT_FIELD_NAME = "crossConvert";

  private static MutableFudgeMsg getOrCreateMessage(final FudgeMsgFactory factory, final String name, final Map<String, MutableFudgeMsg> map) {
    MutableFudgeMsg msg = map.get(name);
    if (msg == null) {
      msg = factory.newMessage();
      map.put(name, msg);
    }
    return msg;
  }

  private static FudgeMsg mapToMessage(final FudgeMsgFactory factory, final Map<String, MutableFudgeMsg> map) {
    final MutableFudgeMsg msg = factory.newMessage();
    for (final Map.Entry<String, MutableFudgeMsg> entry : map.entrySet()) {
      msg.add(entry.getKey(), null, FudgeWireType.SUB_MESSAGE, entry.getValue());
    }
    return msg;
  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CurrencyMatrix object) {
    // Inverses are only written if they are not the expected calculated value. This happens (17% empirically) due to
    // rounding errors on fixed rates and we don't want the matrix to degrade after repeated serialization/deserialization.
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add(0, CurrencyMatrix.class.getName());
    final Collection<Currency> sourceCurrencies = object.getSourceCurrencies();
    final Collection<Currency> targetCurrencies = object.getTargetCurrencies();
    final Map<String, MutableFudgeMsg> fixedValues = new HashMap<String, MutableFudgeMsg>();
    final Map<String, MutableFudgeMsg> crossValues = new HashMap<String, MutableFudgeMsg>();
    final Map<String, MutableFudgeMsg> reqValues = new HashMap<String, MutableFudgeMsg>();
    for (final Currency sourceCurrency : sourceCurrencies) {
      final String sourceISO = sourceCurrency.getCode();
      for (final Currency targetCurrency : targetCurrencies) {
        final String targetISO = targetCurrency.getCode();
        final int cmp = sourceISO.compareTo(targetISO);
        if (cmp == 0) {
          continue;
        }
        final CurrencyMatrixValue value = object.getConversion(sourceCurrency, targetCurrency);
        if (value == null) {
          continue;
        }
        final boolean suppressInverse;
        if (targetCurrencies.contains(sourceCurrency) && sourceCurrencies.contains(targetCurrency)) {
          final CurrencyMatrixValue inverse = object.getConversion(targetCurrency, sourceCurrency);
          if (inverse == null) {
            suppressInverse = true;
          } else {
            if (cmp < 0) {
              suppressInverse = !value.getReciprocal().equals(inverse);
            } else {
              if (inverse.getReciprocal().equals(value)) {
                continue;
              }
              suppressInverse = true;
            }
          }
        } else {
          suppressInverse = true;
        }
        value.accept(new CurrencyMatrixValueVisitor<Void>() {

          @Override
          public Void visitCross(final CurrencyMatrixCross cross) {
            final MutableFudgeMsg entries = getOrCreateMessage(serializer, cross.getCrossCurrency().getCode(), crossValues);
            if (suppressInverse) {
              final MutableFudgeMsg subMsg = serializer.newMessage();
              subMsg.add(targetISO, null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
              entries.add(sourceISO, null, FudgeWireType.SUB_MESSAGE, subMsg);
            } else {
              entries.add(sourceISO, null, FudgeWireType.STRING, targetISO);
            }
            return null;
          }

          @Override
          public Void visitFixed(final CurrencyMatrixFixed fixedValue) {
            final MutableFudgeMsg entries = getOrCreateMessage(serializer, sourceISO, fixedValues);
            entries.add(targetISO, null, FudgeWireType.DOUBLE, fixedValue.getFixedValue());
            if (suppressInverse) {
              entries.add(targetISO, null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
            }
            return null;
          }

          @Override
          public Void visitValueRequirement(final CurrencyMatrixValueRequirement valueRequirement) {
            final MutableFudgeMsg entries = getOrCreateMessage(serializer, sourceISO, reqValues);
            serializer.addToMessage(entries, targetISO, null, valueRequirement);
            if (suppressInverse) {
              entries.add(targetISO, null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
            }
            return null;
          }

        });
      }
    }
    if (!fixedValues.isEmpty()) {
      msg.add(FIXED_RATE_FIELD_NAME, null, FudgeWireType.SUB_MESSAGE, mapToMessage(serializer, fixedValues));
    }
    if (!reqValues.isEmpty()) {
      msg.add(VALUE_REQUIREMENTS_FIELD_NAME, null, FudgeWireType.SUB_MESSAGE, mapToMessage(serializer, reqValues));
    }
    if (!crossValues.isEmpty()) {
      msg.add(CROSS_CONVERT_FIELD_NAME, null, FudgeWireType.SUB_MESSAGE, mapToMessage(serializer, crossValues));
    }
    serializer.addToMessage(msg, UNIQUE_ID_FIELD_NAME, null, object.getUniqueId());
    return msg;
  }

  private static class MatrixImpl extends AbstractCurrencyMatrix {

    private void loadFixed(final FudgeMsg message) {
      final Map<Pair<Currency, Currency>, CurrencyMatrixValue> values = new HashMap<Pair<Currency, Currency>, CurrencyMatrixValue>();
      for (final FudgeField field : message) {
        final Currency source = Currency.of(field.getName());
        final FudgeMsg message2 = message.getFieldValue(FudgeMsg.class, field);
        for (final FudgeField field2 : message2) {
          final Currency target = Currency.of(field2.getName());
          if (field2.getValue() instanceof Double) {
            final CurrencyMatrixValue value = CurrencyMatrixValue.of((Double) field2.getValue());
            values.put(Pairs.of(source, target), value);
            values.put(Pairs.of(target, source), value.getReciprocal());
          } else {
            values.remove(Pairs.of(target, source));
          }
        }
        for (final Map.Entry<Pair<Currency, Currency>, CurrencyMatrixValue> valueEntry : values.entrySet()) {
          addConversion(valueEntry.getKey().getFirst(), valueEntry.getKey().getSecond(), valueEntry.getValue());
        }
        values.clear();
      }
    }

    private void loadReq(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Map<Pair<Currency, Currency>, CurrencyMatrixValue> values = new HashMap<Pair<Currency, Currency>, CurrencyMatrixValue>();
      for (final FudgeField field : message) {
        final Currency source = Currency.of(field.getName());
        for (final FudgeField field2 : message.getFieldValue(FudgeMsg.class, field)) {
          final Currency target = Currency.of(field2.getName());
          if (field2.getValue() instanceof FudgeMsg) {
            final CurrencyMatrixValue value = deserializer.fieldValueToObject(CurrencyMatrixValueRequirement.class, field2);
            values.put(Pairs.of(source, target), value);
            values.put(Pairs.of(target, source), value.getReciprocal());
          } else {
            values.remove(Pairs.of(target, source));
          }
        }
        for (final Map.Entry<Pair<Currency, Currency>, CurrencyMatrixValue> valueEntry : values.entrySet()) {
          addConversion(valueEntry.getKey().getFirst(), valueEntry.getKey().getSecond(), valueEntry.getValue());
        }
        values.clear();
      }
    }

    private void loadCross(final FudgeMsg message) {
      final Map<Pair<Currency, Currency>, CurrencyMatrixValue> values = new HashMap<Pair<Currency, Currency>, CurrencyMatrixValue>();
      for (final FudgeField field : message) {
        final CurrencyMatrixValue cross = CurrencyMatrixValue.of(Currency.of(field.getName()));
        for (final FudgeField field2 : (FudgeMsg) field.getValue()) {
          final Currency source = Currency.of(field2.getName());
          if (field2.getValue() instanceof FudgeMsg) {
            final Currency target = Currency.of(((FudgeMsg) field2.getValue()).iterator().next().getName());
            values.put(Pairs.of(source, target), cross);
          } else {
            final Currency target = Currency.of((String) field2.getValue());
            values.put(Pairs.of(source, target), cross);
            values.put(Pairs.of(target, source), cross);
          }
        }
        for (final Map.Entry<Pair<Currency, Currency>, CurrencyMatrixValue> valueEntry : values.entrySet()) {
          addConversion(valueEntry.getKey().getFirst(), valueEntry.getKey().getSecond(), valueEntry.getValue());
        }
        values.clear();
      }
    }

  }

  @Override
  public CurrencyMatrix buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final MatrixImpl matrix = new MatrixImpl();
    FudgeField field = message.getByName(UNIQUE_ID_FIELD_NAME);
    if (field != null) {
      matrix.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, field));
    }
    field = message.getByName(CROSS_CONVERT_FIELD_NAME);
    if (field != null) {
      matrix.loadCross(message.getFieldValue(FudgeMsg.class, field));
    }
    field = message.getByName(FIXED_RATE_FIELD_NAME);
    if (field != null) {
      matrix.loadFixed(message.getFieldValue(FudgeMsg.class, field));
    }
    field = message.getByName(VALUE_REQUIREMENTS_FIELD_NAME);
    if (field != null) {
      matrix.loadReq(deserializer, message.getFieldValue(FudgeMsg.class, field));
    }
    return matrix;
  }

}
