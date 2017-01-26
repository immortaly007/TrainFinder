package com.basdado.trainfinder.model;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TravelAdvice {
	
	private final List<TravelAdviceOption> travelAdviceOptions;
	
	public TravelAdvice(List<TravelAdviceOption> travelAdviceOptions) {
		this.travelAdviceOptions = Collections.unmodifiableList(travelAdviceOptions);
	}
	
	public List<TravelAdviceOption> getTravelAdviceOptions() {
		return travelAdviceOptions;
	}
	
	@Override
	public String toString() {
		return "travelAdviceOptions: {\r\n" + String.join("\r\n", travelAdviceOptions.stream().map(t -> t.toString()).collect(Collectors.toList()));
	}
	
}
