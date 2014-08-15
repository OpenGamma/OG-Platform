================================
How to Load Securities using XML
================================

It is possible to add securities via xml through configuration screens. To illustrate the process, take the creation of a Bond Security for example :

Open web a browser and connect to the OG server URL
Navigate to "/jax/securities/", In the Add security by XML section select **DbSec** from the dropdown.

Add the sample XML below

.. code :: xml

      <?xmlversion="1.0"encoding="UTF-8"?>
      <beantype="com.opengamma.financial.security.bond.GovernmentBondSecurity">
          <externalIdBundle>
            <externalIds>
              <item>TICKER~ABC</item>
            </externalIds>
          </externalIdBundle>
          <name>SampleBondName</name>
          <securityType>BOND</securityType>
          <issuerName>USTREASURYN/B</issuerName>
          <issuerType>Sovereign</issuerType>
          <issuerDomicile>US</issuerDomicile>
          <market>USGOVERNMENT</market>
          <currency>USD</currency>
          <yieldConvention>USstreet</yieldConvention>
          <lastTradeDate>
            <expiry>2018-04-30T00:00Z</expiry>
            <accuracy>DAY_MONTH_YEAR</accuracy>
          </lastTradeDate>
          <couponType>FIXED</couponType>
          <couponRate>2.625</couponRate>
          <couponFrequency>Semi-annual</couponFrequency>
          <dayCount>Actual/ActualICMA</dayCount>
          <interestAccrualDate>2011-04-30T00:00Z</interestAccrualDate>
          <settlementDate>2011-04-30T00:00Z</settlementDate>
          <firstCouponDate>2011-10-31T00:00Z</firstCouponDate>
          <issuancePrice>99.4</issuancePrice>
          <totalAmountIssued>6.6E10</totalAmountIssued>
          <minimumAmount>100.0</minimumAmount>
          <minimumIncrement>100.0</minimumIncrement>
          <parAmount>100.0</parAmount>
          <redemptionValue>100.0</redemptionValue>
      </bean>

Attributes are added as key-value pairs, for example:

.. code :: xml

      <attributes>
        <entrykey="IndustrySector">Financial</entry>
        <entrykey="RatingSP">A</entry>
      </attributes>

The XML maps directly onto the Model, in this case the Joda­Bean BondSecurity. The properties definitions below match the XML bean nodes:

.. code :: java

      /*Theissuername.*/
      @PropertyDefinition(validate="notNull")
      privateString_issuerName;
      /*Theissuertype.*/
      @PropertyDefinition(validate="notNull")
      privateString_issuerType;
      /*Theissuerdomicile.*/
      @PropertyDefinition(validate="notNull")
      privateString_issuerDomicile;
      /*Themarket.*/
      @PropertyDefinition(validate="notNull")
      privateString_market;
      /*Thecurrency.*/
      @PropertyDefinition(validate="notNull")
      privateCurrency_currency;
      /* Theyieldconvention.*/
      ...
      ...
      /*Theparamount.*/
      @PropertyDefinition
      privatedouble_parAmount;
      /*Theredemptionvalue.*/
      @PropertyDefinition
      privatedouble_redemptionValue;


And of type:

.. code :: java

    /*Thesecuritytype.*/
    publicstaticfinalStringSECURITY_TYPE="BOND";


Further security definitions can be found by inspecting the subclasses of ``com.opengamma.master.security.ManageableSecurity``