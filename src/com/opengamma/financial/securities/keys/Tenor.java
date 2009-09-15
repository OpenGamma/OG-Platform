package com.opengamma.financial.securities.keys;

import javax.time.period.Period;

public class Tenor {
	private Period _period;
	public Tenor(Period period) {
		_period = period;
	}
	
	public Period getPeriod() {
		return _period;
	}
}
