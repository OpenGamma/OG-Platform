package com.opengamma.language.object;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class CreateBeanFunctionTest {

  private static final double MAX_DELTA = 1E-15;

  /**
   * This is needed to register the meta-bean for the security type so it can be looked up when the bean is created.
   */
  @BeforeClass
  public void registerMetaBean() throws ClassNotFoundException {
    Class.forName("com.opengamma.financial.security.fx.FXForwardSecurity");
  }

  @Test
  public void invoke() {
    String testFunctionName = "testFunctionName";
    CreateBeanFunction function = new CreateBeanFunction(testFunctionName, FXForwardSecurity.class);

    UniqueId uniqueId = UniqueId.of("Tst", "uid");
    ExternalId regionId = ExternalId.of("Tst", "region");
    Currency payCurrency = Currency.AUD;
    double payAmount = 1000000d;
    Currency receiveCurrency = Currency.DKK;
    double receiveAmount = 8000000d;
    ZonedDateTime forwardDate = ZonedDateTime.now();
    ExternalIdBundle idBundle = ExternalIdBundle.of(ExternalId.of("Tst", "id"));
    String name = "securityName";
    Map<String, String> attributes = Collections.singletonMap("Foo", "Bar");
    Object[] parameters = {uniqueId, idBundle, name, attributes, payCurrency, payAmount, receiveCurrency, receiveAmount, forwardDate, regionId };
    MetaFunction metaFunction = function.getMetaFunction();
    assertNotNull(metaFunction);
    assertEquals(testFunctionName, metaFunction.getName());
    CreateBeanFunction.Invoker invoker = (CreateBeanFunction.Invoker) metaFunction.getInvoker();
    Object bean = invoker.invokeImpl(null, parameters);
    assertNotNull(bean);
    assertTrue(bean instanceof FXForwardSecurity);
    FXForwardSecurity security = (FXForwardSecurity) bean;
    assertEquals(idBundle, security.getExternalIdBundle());
    assertEquals(name, security.getName());
    assertEquals(FXForwardSecurity.SECURITY_TYPE, security.getSecurityType());
    assertEquals(attributes, security.getAttributes());
    assertEquals(payCurrency, security.getPayCurrency());
    assertEquals(payAmount, security.getPayAmount(), MAX_DELTA);
    assertEquals(receiveCurrency, security.getReceiveCurrency());
    assertEquals(receiveAmount, security.getReceiveAmount(), MAX_DELTA);
    assertEquals(forwardDate, security.getForwardDate());
    assertEquals(regionId, security.getRegionId());
  }
}
