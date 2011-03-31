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
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
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
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Fudge builder for a {@link CurrencyMatrix}. This handles the general case - matrices may typically be sparse so
 * there may be more efficient encodings possible. In those cases, serialize and add class headers directly.
 */
@GenericFudgeBuilderFor(CurrencyMatrix.class)
public class CurrencyMatrixBuilder implements FudgeBuilder<CurrencyMatrix> {

  private static final String UNIQUE_ID_FIELD_NAME = "uniqueId";
  private static final String FIXED_RATE_FIELD_NAME = "fixedRate";
  private static final String VALUE_REQUIREMENTS_FIELD_NAME = "valueReq";
  private static final String CROSS_CONVERT_FIELD_NAME = "crossConvert";

  private static MutableFudgeFieldContainer getOrCreateMessage(final FudgeMessageFactory factory, final String name, final Map<String, MutableFudgeFieldContainer> map) {
    MutableFudgeFieldContainer msg = map.get(name);
    if (msg == null) {
      msg = factory.newMessage();
      map.put(name, msg);
    }
    return msg;
  }

  private static FudgeFieldContainer mapToMessage(final FudgeMessageFactory factory, final Map<String, MutableFudgeFieldContainer> map) {
    final MutableFudgeFieldContainer msg = factory.newMessage();
    for (Map.Entry<String, MutableFudgeFieldContainer> entry : map.entrySet()) {
      msg.add(entry.getKey(), null, FudgeWireType.SUB_MESSAGE, entry.getValue());
    }
    return msg;
  }

  @Override
  public MutableFudgeFieldContainer buildMessage(final FudgeSerializationContext context, final CurrencyMatrix object) {
    // Inverses are only written if they are not the expected calculated value. This happens (17% empirically) due to
    // rounding errors on fixed rates and we don't want the matrix to degrade after repeated serialization/deserialization.
    final MutableFudgeFieldContainer msg = context.newMessage();
    msg.add(0, CurrencyMatrix.class.getName());
    final Collection<Currency> sourceCurrencies = object.getSourceCurrencies();
    final Collection<Currency> targetCurrencies = object.getTargetCurrencies();
    final Map<String, MutableFudgeFieldContainer> fixedValues = new HashMap<String, MutableFudgeFieldContainer>();
    final Map<String, MutableFudgeFieldContainer> crossValues = new HashMap<String, MutableFudgeFieldContainer>();
    final Map<String, MutableFudgeFieldContainer> reqValues = new HashMap<String, MutableFudgeFieldContainer>();
    for (Currency sourceCurrency : sourceCurrencies) {
      final String sourceISO = sourceCurrency.getCode();
      for (Currency targetCurrency : targetCurrencies) {
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
              } else {
                suppressInverse = true;
              }
            }
          }
        } else {
          suppressInverse = true;
        }
        value.accept(new CurrencyMatrixValueVisitor<Void>() {

          @Override
          public Void visitCross(final CurrencyMatrixCross cross) {
            final MutableFudgeFieldContainer entries = getOrCreateMessage(context, cross.getCrossCurrency().getCode(), crossValues);
            if (suppressInverse) {
              final MutableFudgeFieldContainer subMsg = context.newMessage();
              subMsg.add(targetISO, null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
              entries.add(sourceISO, null, FudgeWireType.SUB_MESSAGE, subMsg);
            } else {
              entries.add(sourceISO, null, FudgeWireType.STRING, targetISO);
            }
            return null;
          }

          @Override
          public Void visitFixed(final CurrencyMatrixFixed fixedValue) {
            final MutableFudgeFieldContainer entries = getOrCreateMessage(context, sourceISO, fixedValues);
            entries.add(targetISO, null, FudgeWireType.DOUBLE, fixedValue.getFixedValue());
            if (suppressInverse) {
              entries.add(targetISO, null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
            }
            return null;
          }

          @Override
          public Void visitValueRequirement(final CurrencyMatrixValueRequirement valueRequirement) {
            final MutableFudgeFieldContainer entries = getOrCreateMessage(context, sourceISO, reqValues);
            context.objectToFudgeMsg(entries, targetISO, null, valueRequirement);
            if (suppressInverse) {
              entries.add(targetISO, null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
            }
            return null;
          }

        });
      }
    }
    if (!fixedValues.isEmpty()) {
      msg.add(FIXED_RATE_FIELD_NAME, null, FudgeWireType.SUB_MESSAGE, mapToMessage(context, fixedValues));
    }
    if (!reqValues.isEmpty()) {
      msg.add(VALUE_REQUIREMENTS_FIELD_NAME, null, FudgeWireType.SUB_MESSAGE, mapToMessage(context, reqValues));
    }
    if (!crossValues.isEmpty()) {
      msg.add(CROSS_CONVERT_FIELD_NAME, null, FudgeWireType.SUB_MESSAGE, mapToMessage(context, crossValues));
    }
    context.objectToFudgeMsg(msg, UNIQUE_ID_FIELD_NAME, null, object.getUniqueId());
    return msg;
  }

  private static class MatrixImpl extends AbstractCurrencyMatrix {

    private void loadFixed(final FudgeFieldContainer message) {
      final Map<Pair<Currency, Currency>, CurrencyMatrixValue> values = new HashMap<Pair<Currency, Currency>, CurrencyMatrixValue>();
      for (FudgeField field : message) {
        final Currency source = Currency.of(field.getName());
        final FudgeFieldContainer message2 = message.getFieldValue(FudgeFieldContainer.class, field);
        for (FudgeField field2 : message2) {
          final Currency target = Currency.of(field2.getName());
          if (field2.getValue() instanceof Double) {
            final CurrencyMatrixValue value = CurrencyMatrixValue.of((Double) field2.getValue());
            values.put(Pair.of(source, target), value);
            values.put(Pair.of(target, source), value.getReciprocal());
          } else {
            values.remove(Pair.of(target, source));
          }
        }
        for (Map.Entry<Pair<Currency, Currency>, CurrencyMatrixValue> valueEntry : values.entrySet()) {
          addConversion(valueEntry.getKey().getFirst(), valueEntry.getKey().getSecond(), valueEntry.getValue());
        }
        values.clear();
      }
    }

    private void loadReq(final FudgeDeserializationContext dctx, final FudgeFieldContainer message) {
      final Map<Pair<Currency, Currency>, CurrencyMatrixValue> values = new HashMap<Pair<Currency, Currency>, CurrencyMatrixValue>();
      for (FudgeField field : message) {
        final Currency source = Currency.of(field.getName());
        for (FudgeField field2 : message.getFieldValue(FudgeFieldContainer.class, field)) {
          final Currency target = Currency.of(field2.getName());
          if (field2.getValue() instanceof FudgeFieldContainer) {
            final CurrencyMatrixValue value = dctx.fieldValueToObject(CurrencyMatrixValueRequirement.class, field2);
            values.put(Pair.of(source, target), value);
            values.put(Pair.of(target, source), value.getReciprocal());
          } else {
            values.remove(Pair.of(target, source));
          }
        }
        for (Map.Entry<Pair<Currency, Currency>, CurrencyMatrixValue> valueEntry : values.entrySet()) {
          addConversion(valueEntry.getKey().getFirst(), valueEntry.getKey().getSecond(), valueEntry.getValue());
        }
        values.clear();
      }
    }

    private void loadCross(final FudgeFieldContainer message) {
      final Map<Pair<Currency, Currency>, CurrencyMatrixValue> values = new HashMap<Pair<Currency, Currency>, CurrencyMatrixValue>();
      for (FudgeField field : message) {
        final CurrencyMatrixValue cross = CurrencyMatrixValue.of(Currency.of(field.getName()));
        for (FudgeField field2 : (FudgeFieldContainer) field.getValue()) {
          final Currency source = Currency.of(field2.getName());
          if (field2.getValue() instanceof FudgeFieldContainer) {
            final Currency target = Currency.of(((FudgeFieldContainer) field2.getValue()).iterator().next().getName());
            values.put(Pair.of(source, target), cross);
          } else {
            final Currency target = Currency.of((String) field2.getValue());
            values.put(Pair.of(source, target), cross);
            values.put(Pair.of(target, source), cross);
          }
        }
        for (Map.Entry<Pair<Currency, Currency>, CurrencyMatrixValue> valueEntry : values.entrySet()) {
          addConversion(valueEntry.getKey().getFirst(), valueEntry.getKey().getSecond(), valueEntry.getValue());
        }
        values.clear();
      }
    }

  }

  @Override
  public CurrencyMatrix buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    final MatrixImpl matrix = new MatrixImpl();
    FudgeField field = message.getByName(UNIQUE_ID_FIELD_NAME);
    if (field != null) {
      matrix.setUniqueId(context.fieldValueToObject(UniqueIdentifier.class, field));
    }
    field = message.getByName(CROSS_CONVERT_FIELD_NAME);
    if (field != null) {
      matrix.loadCross(message.getFieldValue(FudgeFieldContainer.class, field));
    }
    field = message.getByName(FIXED_RATE_FIELD_NAME);
    if (field != null) {
      matrix.loadFixed(message.getFieldValue(FudgeFieldContainer.class, field));
    }
    field = message.getByName(VALUE_REQUIREMENTS_FIELD_NAME);
    if (field != null) {
      matrix.loadReq(context, message.getFieldValue(FudgeFieldContainer.class, field));
    }
    return matrix;
  }

}
