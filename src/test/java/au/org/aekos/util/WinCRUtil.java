package au.org.aekos.util;

import org.apache.commons.lang.SystemUtils;

public class WinCRUtil {
	
	public static String cleanse(String in){
		if(SystemUtils.IS_OS_WINDOWS){
			return in.replaceAll("\r", "");
		}
		return in;
	}
	

}
