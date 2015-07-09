package com.opengamma.sesame.marketdata.scenarios;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.sesame.marketdata.VolatilitySurfaceId;

@Test
public class VolatilitySurfaceNameFilterTestTest {

  private static final String NAME = "a vol surface";

  public void match() {
    VolatilitySurfaceNameFilter filter = VolatilitySurfaceNameFilter.of(NAME);
    assertFalse(filter.apply(VolatilitySurfaceId.of(NAME)).isEmpty());
  }

  public void noMatch() {
    VolatilitySurfaceNameFilter filter = VolatilitySurfaceNameFilter.of(NAME);
    assertTrue(filter.apply(VolatilitySurfaceId.of("Foo")).isEmpty());
  }
}
