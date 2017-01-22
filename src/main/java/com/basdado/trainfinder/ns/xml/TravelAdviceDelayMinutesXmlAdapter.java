package com.basdado.trainfinder.ns.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class TravelAdviceDelayMinutesXmlAdapter extends XmlAdapter<String, Integer> {

	@Override
	public Integer unmarshal(String v) throws Exception {
		String value = v.substring(1, v.length() - 4);
		return Integer.valueOf(value);
	}

	@Override
	public String marshal(Integer v) throws Exception {
		return "+" + v + " min";
	}

}
