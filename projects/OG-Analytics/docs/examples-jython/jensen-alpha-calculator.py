import com.opengamma.util.timeseries.DoubleTimeSeries
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding

from com.opengamma.financial.riskreward import JensenAlphaCalculator
from com.opengamma.util.timeseries.fast import DateTimeNumericEncoding
from com.opengamma.util.timeseries.fast.longint import FastArrayLongDoubleTimeSeries
from com.opengamma.financial.timeseries.analysis import DoubleTimeSeriesStatisticsCalculator

DATE_EPOCH_DAYS = DateTimeNumericEncoding.DATE_EPOCH_DAYS

T = [1]

asset_return = FastArrayLongDoubleTimeSeries(DATE_EPOCH_DAYS, T, [0.12])
risk_free_rate = FastArrayLongDoubleTimeSeries(DATE_EPOCH_DAYS, T, [0.03])
market_return = FastArrayLongDoubleTimeSeries(DATE_EPOCH_DAYS, T, [0.11])
beta = 0.7

def mock_calc(x):
    return x[0][0]

mc = DoubleTimeSeriesStatisticsCalculator(mock_calc)

calc = JensenAlphaCalculator(mc, mc, mc)

print calc.evaluate(asset_return, risk_free_rate, beta, market_return)

