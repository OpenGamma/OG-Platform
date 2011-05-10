package com.opengamma.master.marketdatasnapshot;

import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.core.marketdatasnapshot.MarketDataValueSpecification;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;



/**
 * Fudge message builder for {@link ManageableUnstructuredMarketDataSnapshot}
 */
@FudgeBuilderFor(ManageableUnstructuredMarketDataSnapshot.class)
public class ManageableUnstrucutredMarketDataSnapshotBuilder implements FudgeBuilder<ManageableUnstructuredMarketDataSnapshot>  {

  private static final String VALUE_SPEC_FIELD = "valueSpec";
  private static final String VALUE_NAME_FIELD = "valueName";
  private static final String VALUE_FIELD = "value";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, ManageableUnstructuredMarketDataSnapshot object) {
    MutableFudgeMsg ret = context.newMessage();
    
    for (Map.Entry<MarketDataValueSpecification, Map<String, ValueSnapshot>> subMap : object.getValues().entrySet()) {
      
      for (Map.Entry<String, ValueSnapshot> subMapEntry : subMap.getValue().entrySet()) {
        MutableFudgeMsg msg = context.newMessage();

        context.addToMessage(msg, VALUE_SPEC_FIELD, null, subMap.getKey());
        context.addToMessage(msg, VALUE_NAME_FIELD, null, subMapEntry.getKey());
        context.addToMessage(msg, VALUE_FIELD, null, subMapEntry.getValue());
        
        ret.add(1, msg);
      }
    }
    return ret;
  }

  @Override
  public ManageableUnstructuredMarketDataSnapshot buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    Map<MarketDataValueSpecification, Map<String, ValueSnapshot>> values = new HashMap<MarketDataValueSpecification, Map<String, ValueSnapshot>>();

    for (FudgeField fudgeField : message.getAllByOrdinal(1)) {
      FudgeMsg innerValue = (FudgeMsg) fudgeField.getValue();
      MarketDataValueSpecification spec = context.fieldValueToObject(MarketDataValueSpecification.class,
          innerValue.getByName(VALUE_SPEC_FIELD));
      String valueName = context.fieldValueToObject(String.class, innerValue.getByName(VALUE_NAME_FIELD));
      ValueSnapshot value = context.fieldValueToObject(ValueSnapshot.class, innerValue.getByName(VALUE_FIELD));
      if (!values.containsKey(spec)) {
        values.put(spec, new HashMap<String, ValueSnapshot>());
      }
      values.get(spec).put(valueName, value);
    }
    
    ManageableUnstructuredMarketDataSnapshot ret = new ManageableUnstructuredMarketDataSnapshot();
    ret.setValues(values);
    return ret;
  }

}
