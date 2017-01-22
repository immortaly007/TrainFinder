package com.basdado.trainfinder.ns.xml;

import java.time.Duration;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class TravelAdviceDurationXmlAdapter extends XmlAdapter<String, Duration> {

	@Override
	public Duration unmarshal(String v) throws Exception {
		String[] res = v.split(":");
		int[] hoursMinutes = { Integer.valueOf(res[0]), Integer.valueOf(res[1]) };
		return Duration.ofMinutes(hoursMinutes[0] * 60 + hoursMinutes[1]);
	}

	@Override
	public String marshal(Duration v) throws Exception {
		return v.toHours() + ":" + (v.toMinutes() % 60);
	}

}
