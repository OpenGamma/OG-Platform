Conventions: Examples - Main
============================

Forex
-----

**Spot**

The names in the examples are **XXXYYYFXSpot** where **XXX** is the first currency (in the standard order) and **YYY** the second currency. 

* EURGBPFXSpot
* EURUSDFXSpot
* EURCHFFXSpot
* EURJPYFXSpot

* GBPUSDFXSpot
* GBPCHFFXSpot 
* GBPJPYFXSpot

* USDCHFFXSpot
* USDJPYFXSpot

* CHFJPYFXSpot


**Forward**

The names in the examples are **XXXYYYFXSwap** where **XXX** is the first currency (in the standard order) and **YYY** the second currency. 

* EURGBPFXSwap
* EURUSDFXSwap
* EURCHFFXSwap
* EURJPYFXSwap

* GBPUSDFXSwap
* GBPCHFFXSwap
* GBPJPYFXSwap

* USDCHFFXSwap
* USDJPYFXSwap

* CHFJPYFXSwap

Overnight Index
-------------

EUREONIA
GBPSONIA
USDFEDFUND
JPYMUTAN

Ibor Index
--------

* EUREURIBOR1M 
* EUREURIBOR3M 
* EUREURIBOR6M 
* EUREURIBOR12M

* GBPLIBOR1M 
* GBPLIBOR3M 
* GBPLIBOR6M

* USDLIBOR1M 
* USDLIBOR3M 
* USDLIBOR6M

Swap Legs
--------

The usual comments are 

* *_PayLag*: the payment is not done at the end of the accrual period but at a payment offset, like for OIS.
* *_SpotLag*:  the spot lag is not the standard one, like for cross-currency swaps
* *_ZZZ*: where ZZZ is a calendar, the payment calendar is not the standard one, like for cross-currency swaps.

**Fixed Leg**

The names in the examples are **XXXFixedYY[_comment]**. The **XXX** is the currency of the leg. The **YY** is the payment tenor (like *3M* or *1Y*). The last part is optional and indicate comments on the convention. 

* EURFixed1Y
* EURFixed1Y_PayLag: Payment lag of 1d for OIS.

* GBPFixed3M 
* GBPFixed6M 
* GBPFixed1Y

* USDFixed6M: Convention 30U/360
* USDFixed1Y_PayLag: Payment lag of 2d for OIS, convention ACT/360
* USDFixed1Y: Convention ACT/360

**Ibor Leg**

The names in the examples are **XXXYYYZZ[_comment]**. The **XXX** is the currency of the leg. The **YYY** is the index name (like *EURIBOR* or *LIBOR*). The **ZZ** is the index and payment tenor (like *3M* or *1Y*). The last part is optional and indicate comments on the convention.

* EUREURIBOR1M
* EUREURIBOR3M
* EUREURIBOR3M_NYC: Payment on double calendar (TARGET and NYC) for cross-currency swaps.
* EUREURIBOR6M
* EUREURIBOR12M

* GBPLIBOR1M
* GBPLIBOR3M
* GBPLIBOR3M_NYC: Payment on double calendar (LON and NYC) for cross-currency swaps.
* GBPLIBOR6M

* USDLIBOR1M
* USDLIBOR3M
* USDLIBOR3M_TAR: Payment on double calendar (NYC and TARGET) for cross-currency swaps.
* USDLIBOR3M_LON: Payment on double calendar (NYC and LON) for cross-currency swaps.
* USDLIBOR6M

**Ibor Compounded Leg**

The names in the examples are **XXXYYYZZCmpVV**. The **XXX** is the currency of the leg. The **YYY** is the index name (like *EURIBOR* or *LIBOR*). The **ZZ** is the index tenor (like *1M* or *3M*). The **Cmp** is a literal indicating composition. The **VV** is the payment tenor (like *6M* or *1Y*). The last part indicates the compounding type (in case of spread).

* USDLIBOR1MCmp3M_Flat
* USDLIBOR3MCmp6M_Flat

**Overnight Compounded Leg**

The names in the examples are **XXXYYYCmpZZ**. The **XXX** is the currency of the leg. The **YYY** is the index name (like *FEDFUNDS* or *EONIA*). The **Cmp** is a literal indicating composition. The **ZZ** is the payment tenor (like *3M* or *1Y*).

* EUREONIACmp1Y
* GBPSONIACmp1Y
* USDFEDFUNDSCmp1Y

**Overnight Arithmetic Average Leg**

The names in the examples are **XXXYYYAAZZ**. The **XXX** is the currency of the leg. The **YYY** is the index name (like *FEDFUNDS*). The **AA** is a literal indicating arithmetic average. The **ZZ** is the payment tenor (like *3M* or *1Y*).

* USDFEDFUNDSAA3M

**STIR Futures**

The names in the examples are **XXXSTIRFuturesFZZ**. The **XXX** is the currency of the futures. The **STIRFutures** is a literal indicating STIR Futures. The **F** is the expiry frequency, with **Q** for the quarterly futures and **S** for serial (or monthly) futures. The **ZZ** is the underlying index tenor.

* USDSTIRFuturesQ3M
* USDSTIRFuturesS3M
* USDSTIRFuturesS1M
* USDSTIRFuturesQ3M

