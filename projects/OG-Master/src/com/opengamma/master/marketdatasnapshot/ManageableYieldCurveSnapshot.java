package com.opengamma.master.marketdatasnapshot;

import javax.time.Instant;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;

public class ManageableYieldCurveSnapshot implements YieldCurveSnapshot {


  private Instant _valuationTime;
  private UnstructuredMarketDataSnapshot _values;

  /**
   * Gets the values field.
   * @return the values
   */
  public UnstructuredMarketDataSnapshot getValues() {
    return _values;
  }

  /**
   * Sets the values field.
   * @param values  the values
   */
  public void setValues(UnstructuredMarketDataSnapshot values) {
    _values = values;
  }

  /**
   * Gets the valuationTime field.
   * @return the valuationTime
   */
  public Instant getValuationTime() {
    return _valuationTime;
  }

  /**
   * Sets the valuationTime field.
   * @param valuationTime  the valuationTime
   */
  public void setValuationTime(Instant valuationTime) {
    _valuationTime = valuationTime;
  }
  
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg(FudgeSerializationContext context) {
    MutableFudgeFieldContainer ret = context.newMessage();
    FudgeSerializationContext.addClassHeader(ret, ManageableYieldCurveSnapshot.class);
    context.objectToFudgeMsg(ret, "values", null, _values);
    context.objectToFudgeMsg(ret, "valuationTime", null, _valuationTime);
    return ret;
  }

  public static ManageableYieldCurveSnapshot fromFudgeMsg(FudgeDeserializationContext context, FudgeFieldContainer msg) {
    UnstructuredMarketDataSnapshot values = context.fieldValueToObject(ManageableUnstructuredMarketDataSnapshot.class,
        msg.getByName("values"));
    Instant valuationTime = context.fieldValueToObject(Instant.class, msg.getByName("valuationTime"));
    ManageableYieldCurveSnapshot ret = new ManageableYieldCurveSnapshot();

    ret.setValuationTime(valuationTime);
    ret.setValues(values);

    return ret;
  }
  
  
}
