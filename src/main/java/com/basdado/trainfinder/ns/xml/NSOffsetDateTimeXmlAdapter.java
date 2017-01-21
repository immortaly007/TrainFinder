package com.basdado.trainfinder.ns.xml;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import com.migesok.jaxb.adapter.javatime.TemporalAccessorXmlAdapter;

public class NSOffsetDateTimeXmlAdapter extends TemporalAccessorXmlAdapter<OffsetDateTime> {

	public NSOffsetDateTimeXmlAdapter() {
		super(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXX"), OffsetDateTime::from);
	}

}
