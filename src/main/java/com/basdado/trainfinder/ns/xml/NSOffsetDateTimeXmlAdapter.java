package com.basdado.trainfinder.ns.xml;

import java.time.OffsetDateTime;

import com.basdado.trainfinder.ns.Constants;
import com.migesok.jaxb.adapter.javatime.TemporalAccessorXmlAdapter;

public class NSOffsetDateTimeXmlAdapter extends TemporalAccessorXmlAdapter<OffsetDateTime> {

	public NSOffsetDateTimeXmlAdapter() {
		super(Constants.NS_DATETIME_FORMATTER, OffsetDateTime::from);
	}

}
