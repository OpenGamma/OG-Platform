package com.opengamma.master.marketdatasnapshot;

import java.util.Map;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.joda.beans.BeanDefinition;

import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilityPoint;

/**
 * 
 */
@BeanDefinition
public class ManageableVolatilityCubeSnapshot implements VolatilityCubeSnapshot {
  
  /**
   * The values in the snapshot.  values are possibly null, representing a point which was requested but unavailable
   */
  private Map<VolatilityPoint, ValueSnapshot> _values;
  
  

  @Override
  public Map<VolatilityPoint, ValueSnapshot> getValues() {
    return _values;
  }

  private void setValues(Map<VolatilityPoint, ValueSnapshot> values) {
    _values = values;
  }


  public org.fudgemsg.FudgeMsg toFudgeMsg(FudgeSerializationContext context) {
    MutableFudgeMsg ret = context.newMessage();
    FudgeSerializationContext.addClassHeader(ret, ManageableVolatilityCubeSnapshot.class);
    context.addToMessage(ret, "values", null, _values);
    return ret;
  }

  public static ManageableVolatilityCubeSnapshot fromFudgeMsg(FudgeDeserializationContext context, FudgeMsg msg) {
    @SuppressWarnings("unchecked")
    Map<VolatilityPoint, ValueSnapshot> values = context.fieldValueToObject(Map.class, msg.getByName("values"));
    ManageableVolatilityCubeSnapshot ret = new ManageableVolatilityCubeSnapshot();

    ret.setValues(values);

    return ret;
  }
}
