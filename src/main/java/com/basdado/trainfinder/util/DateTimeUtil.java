package com.basdado.trainfinder.util;

import java.time.OffsetDateTime;

public class DateTimeUtil {
	
	public static final boolean between(OffsetDateTime min, OffsetDateTime max, OffsetDateTime x) {
		return (x.isAfter(min) || x.isEqual(min)) && x.isBefore(max);
	}

}
