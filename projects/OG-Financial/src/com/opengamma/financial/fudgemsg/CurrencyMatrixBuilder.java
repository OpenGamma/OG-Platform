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
import org.fudgemsg.types.FudgeMsgFieldType;
import org.fudgemsg.types.IndicatorFieldType;
import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.types.PrimitiveFieldTypes;
import org.fudgemsg.types.StringFieldType;

import com.opengamma.core.common.Currency;
import com.opengamma.financial.currency.AbstractCurrencyMatrix;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.currency.CurrencyMatrixValue;
import com.opengamma.financial.currency.CurrencyMatrixValueVisitor;
import com.opengamma.financial.currency.CurrencyMatrixValue.CurrencyMatrixCross;
import com.opengamma.financial.currency.CurrencyMatrixValue.CurrencyMatrixFixedValue;
import com.opengamma.financial.currency.CurrencyMatrixValue.CurrencyMatrixUniqueIdentifier;
import com.opengamma.util.tuple.Pair;

/**
 * Fudge builder for a {@link CurrencyMatrix}. This handles the general case - matrices may typically be sparse so
 * there may be more efficient encodings possible. In those cases, serialize and add class headers directly.
 */
@GenericFudgeBuilderFor(CurrencyMatrix.class)
public class CurrencyMatrixBuilder implements FudgeBuilder<CurrencyMatrix> {

  private static final String FIXED_FIELD_NAME = "fixed";
  private static final String UNIQUE_IDENTIFIER_FIELD_NAME = "liveData";
  private static final String CROSS_FIELD_NAME = "cross";

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
      msg.add(entry.getKey(), null, FudgeMsgFieldType.INSTANCE, entry.getValue());
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
    final Map<String, MutableFudgeFieldContainer> uidValues = new HashMap<String, MutableFudgeFieldContainer>();
    for (Currency sourceCurrency : sourceCurrencies) {
      final String sourceISO = sourceCurrency.getISOCode();
      for (Currency targetCurrency : targetCurrencies) {
        final String targetISO = targetCurrency.getISOCode();
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
            final MutableFudgeFieldContainer entries = getOrCreateMessage(context, cross.getCrossCurrency().getISOCode(), crossValues);
            if (suppressInverse) {
              final MutableFudgeFieldContainer subMsg = context.newMessage();
              subMsg.add(targetISO, null, IndicatorFieldType.INSTANCE, IndicatorType.INSTANCE);
              entries.add(sourceISO, null, FudgeMsgFieldType.INSTANCE, subMsg);
            } else {
              entries.add(sourceISO, null, StringFieldType.INSTANCE, targetISO);
            }
            return null;
          }

          @Override
          public Void visitFixedValue(final CurrencyMatrixFixedValue fixedValue) {
            final MutableFudgeFieldContainer entries = getOrCreateMessage(context, sourceISO, fixedValues);
            entries.add(targetISO, null, PrimitiveFieldTypes.DOUBLE_TYPE, fixedValue.getFixedValue());
            if (suppressInverse) {
              entries.add(targetISO, null, IndicatorFieldType.INSTANCE, IndicatorType.INSTANCE);
            }
            return null;
          }

          @Override
          public Void visitUniqueIdentifier(final CurrencyMatrixUniqueIdentifier uniqueIdentifier) {
            final MutableFudgeFieldContainer entries = getOrCreateMessage(context, sourceISO, uidValues);
            context.objectToFudgeMsg(entries, targetISO, null, uniqueIdentifier);
            if (suppressInverse) {
              entries.add(targetISO, null, IndicatorFieldType.INSTANCE, IndicatorType.INSTANCE);
            }
            return null;
          }

        });
      }
    }
    if (!fixedValues.isEmpty()) {
      msg.add(FIXED_FIELD_NAME, null, FudgeMsgFieldType.INSTANCE, mapToMessage(context, fixedValues));
    }
    if (!uidValues.isEmpty()) {
      msg.add(UNIQUE_IDENTIFIER_FIELD_NAME, null, FudgeMsgFieldType.INSTANCE, mapToMessage(context, uidValues));
    }
    if (!crossValues.isEmpty()) {
      msg.add(CROSS_FIELD_NAME, null, FudgeMsgFieldType.INSTANCE, mapToMessage(context, crossValues));
    }
    return msg;
  }

  private static class MatrixImpl extends AbstractCurrencyMatrix {

    private void loadFixed(final FudgeFieldContainer message) {
      final Map<Pair<Currency, Currency>, CurrencyMatrixValue> values = new HashMap<Pair<Currency, Currency>, CurrencyMatrixValue>();
      for (FudgeField field : message) {
        final Currency source = Currency.getInstance(field.getName());
        final FudgeFieldContainer message2 = message.getFieldValue(FudgeFieldContainer.class, field);
        for (FudgeField field2 : message2) {
          final Currency target = Currency.getInstance(field2.getName());
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

    private void loadUID(final FudgeDeserializationContext dctx, final FudgeFieldContainer message) {
      final Map<Pair<Currency, Currency>, CurrencyMatrixValue> values = new HashMap<Pair<Currency, Currency>, CurrencyMatrixValue>();
      for (FudgeField field : message) {
        final Currency source = Currency.getInstance(field.getName());
        for (FudgeField field2 : message.getFieldValue(FudgeFieldContainer.class, field)) {
          final Currency target = Currency.getInstance(field2.getName());
          if (field2.getValue() instanceof FudgeFieldContainer) {
            final CurrencyMatrixValue value = dctx.fieldValueToObject(CurrencyMatrixUniqueIdentifier.class, field2);
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
        final CurrencyMatrixValue cross = CurrencyMatrixValue.of(Currency.getInstance(field.getName()));
        for (FudgeField field2 : (FudgeFieldContainer) field.getValue()) {
          final Currency source = Currency.getInstance(field2.getName());
          if (field2.getValue() instanceof FudgeFieldContainer) {
            final Currency target = Currency.getInstance(((FudgeFieldContainer) field2.getValue()).iterator().next().getName());
            values.put(Pair.of(source, target), cross);
          } else {
            final Currency target = Currency.getInstance((String) field2.getValue());
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
    FudgeField field = message.getByName(CROSS_FIELD_NAME);
    if (field != null) {
      matrix.loadCross(message.getFieldValue(FudgeFieldContainer.class, field));
    }
    field = message.getByName(FIXED_FIELD_NAME);
    if (field != null) {
      matrix.loadFixed(message.getFieldValue(FudgeFieldContainer.class, field));
    }
    field = message.getByName(UNIQUE_IDENTIFIER_FIELD_NAME);
    if (field != null) {
      matrix.loadUID(context, message.getFieldValue(FudgeFieldContainer.class, field));
    }
    return matrix;
  }

}
