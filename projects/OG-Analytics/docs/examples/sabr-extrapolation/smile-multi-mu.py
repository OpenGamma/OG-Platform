### @export "imports"
from com.opengamma.financial.model.option.pricing.analytic.formula import BlackFunctionData
from com.opengamma.financial.model.option.pricing.analytic.formula import EuropeanVanillaOption
from com.opengamma.financial.model.option.pricing.analytic.formula import SABRExtrapolationRightFunction
from com.opengamma.financial.model.volatility import BlackImpliedVolatilityFormula
from com.opengamma.financial.model.volatility.smile.function import SABRFormulaData

### @export "sabr-data"
FORWARD = 0.05
ALPHA = 0.05
BETA = 0.50
NU = 0.50
RHO = -0.25

sabr_data =  SABRFormulaData(FORWARD, ALPHA, BETA, NU, RHO)
print sabr_data

### @export "sabr-function"
CUT_OFF_STRIKE = 0.10
TIME_TO_EXPIRY = 2.0
mu = 5.0
sabr_fn = SABRExtrapolationRightFunction(sabr_data, CUT_OFF_STRIKE, TIME_TO_EXPIRY, mu)
print sabr_fn

### @export "create-option"
strike = 0.12
option = EuropeanVanillaOption(strike, TIME_TO_EXPIRY, True)
print option

### @export "calculate-price"
price = sabr_fn.price(option)
print price

### @export "calculate-implied-vol"
implied = BlackImpliedVolatilityFormula()
black_data = BlackFunctionData(FORWARD, 1.0, 0.0)
implied.getImpliedVolatility(black_data, option, price)

### @export "create-table"
f = open("dexy--sabr-extrapolation-data.txt", "w")

# Write header row
f.write("Mu\tPrice\tStrike\tImpliedVolPct\n")

RANGE_STRIKE = 0.02
N = 100

for mu in [5.0, 40.0, 90.0, 150.0]:
    sabr_fn = SABRExtrapolationRightFunction(sabr_data, CUT_OFF_STRIKE, TIME_TO_EXPIRY, mu)
    for p in range(0, N):
        strike = CUT_OFF_STRIKE - RANGE_STRIKE + p * 4.0 * RANGE_STRIKE / N
        option = EuropeanVanillaOption(strike, TIME_TO_EXPIRY, True)
        price = sabr_fn.price(option)
        implied_vol = implied.getImpliedVolatility(black_data, option, price) * 100
        f.write("%s\t%s\t%s\t%s\n" % (mu, sabr_fn.price(option), strike, implied_vol))

f.close()

