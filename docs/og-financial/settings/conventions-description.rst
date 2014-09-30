Conventions: Description
====================

Forex
-----

**Spot**

The fields are the 
* *settlementDays*: the number of business days between today and the
   spot date.
* *settlementRegion*: The calendar to be used to compute the business days.

Example::
    <bean type="com.opengamma.financial.convention.FXSpotConvention">
     <uniqueId>DbCnv~1284~1</uniqueId>
    <externalIdBundle>
      <externalIds>
       <item>CONVENTION~GBPUSDFXSpot</item>
      </externalIds>
     </externalIdBundle>
     <attributes/>
     <name>GBPUSDFXSpot</name>
     <settlementDays>2</settlementDays>
     <settlementRegion>FINANCIAL_REGION~GB+US</settlementRegion>
    </bean>

**Swap/Forward**

The fields are the 
* *spotConvention*: the FX spot convention (as described in the
   previous section).
* *businessDayConvention*: The business day convention to compute the
   end date of the swap/forward.
* *isEOM*: The flag indicating if the end-of-month rule apply in
   computing the end date of the swap/forward. 
* *settlementRegion*: The calendar to be used to compute the business
   day convention adjustment.


Example::
    <bean type="com.opengamma.financial.convention.FXForwardAndSwapConvention">
     <uniqueId>DbCnv~1286~0</uniqueId>
     <externalIdBundle>
      <externalIds>
       <item>CONVENTION~GBPUSDFXSwap</item>
      </externalIds>
     </externalIdBundle>
     <attributes/>
     <name>GBPUSDFXSwap</name>
     <spotConvention>CONVENTION~GBPUSDFXSpot</spotConvention>
     <businessDayConvention>Following</businessDayConvention>
     <isEOM>false</isEOM>
     <settlementRegion>FINANCIAL_REGION~GB+US</settlementRegion>
    </bean>

Swap Legs
--------

**Fixed Leg**

The fields are the 
* paymentTenor
* dayCount
