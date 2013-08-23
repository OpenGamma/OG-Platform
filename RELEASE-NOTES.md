Release notes
-------------
These are the release notes for the v2.1.0-M1px branch.


v2.1.0-M1p01
------------
- Update batch database schema to handle execution sequences with multiple steps
- Fix batch database writer to handle concurrent calculation cycles
- Fix caching of market data selectors in the engine


v2.1.0-M1p02
------------
- Relax batch database constraints


v2.1.0-M1p03
------------
- PLAT-4231 - Add currency conversion for YIELD_CURVE_PNL_SERIES


v2.1.0-M1p04
------------
- PLAT-4165 - Fix PositionGreekContractMultiplier to use unit amount from security
- PLAT-4373 - Fix for FX forward maturity being computed without spot date and holiday adjustments
