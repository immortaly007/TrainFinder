package com.basdado.trainfinder.util;

public class ObjectUtil {
	
	@SafeVarargs
	public static <T> T coalesce(T... objs) {
		
		for (T obj : objs) {
			if (obj != null) {
				return obj;
			}
		}
		return null;
	}
	
}
