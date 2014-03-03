package com.opengamma.financial.security.index;

import static org.testng.AssertJUnit.assertEquals;

import java.util.SortedMap;
import java.util.TreeMap;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class IndexFamilyFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  private static final IndexFamily s_ref = new IndexFamily();
  
  static {
    s_ref.setExternalIdBundle(ExternalId.of(ExternalSchemes.BLOOMBERG_INDEX_FAMILY, "ICE LIBOR USD").toBundle());
    SortedMap<Tenor, ExternalId> entries = new TreeMap<>();
    entries.put(Tenor.ON, ExternalSchemes.bloombergTickerSecurityId("US00O/N Index"));
    entries.put(Tenor.THREE_MONTHS, ExternalSchemes.bloombergTickerSecurityId("US0003M Index"));
    entries.put(Tenor.SIX_MONTHS, ExternalSchemes.bloombergTickerSecurityId("US0006M Index"));
    entries.put(Tenor.NINE_MONTHS, ExternalSchemes.bloombergTickerSecurityId("US0009M Index"));
    entries.put(Tenor.TWELVE_MONTHS, ExternalSchemes.bloombergTickerSecurityId("US0012M Index"));
    s_ref.setMembers(entries);
    s_ref.setName("ICE LIBOR USD");
    s_ref.addAttribute("Test", "Value");
  }

  @Test
  public void testCycle() {
    assertEquals(s_ref, cycleObject(IndexFamily.class, s_ref));
  }

}