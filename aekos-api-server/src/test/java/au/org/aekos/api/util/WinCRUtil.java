package au.org.aekos.api.util;

import org.apache.commons.lang3.SystemUtils;

public class WinCRUtil {
	
	public static String cleanse(String in){
		if(SystemUtils.IS_OS_WINDOWS){
			return in.replaceAll("\r", "");
		}
		return in;
	}
}
