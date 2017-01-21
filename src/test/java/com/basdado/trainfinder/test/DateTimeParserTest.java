package com.basdado.trainfinder.test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.junit.Assert;
import org.junit.Test;


public class DateTimeParserTest {

	@Test
	public void testDateTimeParser() {
		OffsetDateTime res = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXX").parse("2017-01-20T23:42:00+0100", OffsetDateTime::from); 
		
		
		Assert.assertEquals(res, OffsetDateTime.of(2017, 1, 20, 23, 42, 0, 0, ZoneOffset.ofHours(1)));
	}
	
}
