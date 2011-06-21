package com.opengamma.engine.fudgemsg;

import org.testng.annotations.Test;

import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeDataTest;

public class VolatilityCubeDataBuilderTest extends AbstractBuilderTestCase {

  @Test
  public void testCycleSimpleGraph() {
    VolatilityCubeData simpleData = VolatilityCubeDataTest.getSimpleData();
    VolatilityCubeDataTest.checkSimpleData(simpleData);
    
    VolatilityCubeData cycledObject = cycleObject(VolatilityCubeData.class, simpleData);
    VolatilityCubeDataTest.checkSimpleData(cycledObject);
  }

  @Test
  public void testCycleNullGraph() {
    VolatilityCubeData simpleData = VolatilityCubeDataTest.getNullData();
    VolatilityCubeDataTest.checkNulldata(simpleData);
    
    VolatilityCubeData cycledObject = cycleObject(VolatilityCubeData.class, simpleData);
    VolatilityCubeDataTest.checkNulldata(cycledObject);
  }
}
