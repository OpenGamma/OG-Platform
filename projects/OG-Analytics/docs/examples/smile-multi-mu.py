### @export "imports"
from com.opengamma.financial.model.volatility.smile.function import SABRFormulaData
from com.opengamma.financial.model.option.pricing.analytic.formula import SABRExtrapolationRightFunction
from com.opengamma.financial.model.option.pricing.analytic.formula import EuropeanVanillaOption

### @export "sabr-data"
forward = 0.05
alpha = 0.05
beta = 0.50
nu = 0.50
rho = -0.25

sabr_data =  SABRFormulaData(forward, alpha, beta, nu, rho)
print sabr_data

### @export "sabr-function"
cut_off_strike = 0.10
time_to_expiry = 2.0
mu = 5.0
sabr_fn = SABRExtrapolationRightFunction(sabr_data, cut_off_strike, time_to_expiry, mu)
print sabr_fn

### @export "create-option"
option = EuropeanVanillaOption(cut_off_strike, time_to_expiry, True)

### @export "calculate-price"
sabr_fn.price(option)

### @export "create-table"
f = open("dexy--smile-data.txt", "w")

# Write header row
f.write("Mu\tPrice\tStrikePrice\n")

range_strike = 0.02
n = 100

for mu in [5.0, 40.0, 90.0, 150.0]:
    sabr_fn = SABRExtrapolationRightFunction(sabr_data, cut_off_strike, time_to_expiry, mu)
    for p in range(0, n):
        strike_price = cut_off_strike - range_strike + p * 4.0 * range_strike / n
        option = EuropeanVanillaOption(strike_price, time_to_expiry, True)
        f.write("%s\t%s\t%s\n" % (mu, sabr_fn.price(option), strike_price))

f.close()

