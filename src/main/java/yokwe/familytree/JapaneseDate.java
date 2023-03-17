package yokwe.familytree;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yokwe.util.UnexpectedException;

public class JapaneseDate {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static Pattern pat_DATE  = Pattern.compile("(..)(元|[0-9]{1,2})年([1,2]?[0-9])月([1-3]?[0-9])日");

	public static String findDateString(String string) {
		Matcher m = pat_DATE.matcher(string);
		if (m.find()) {
			return m.group(0);
		} else {
			return null;
		}
	}
	
	public static JapaneseDate getInstance(String string) {
		String dateString = findDateString(string);
		return dateString == null ? null : new JapaneseDate(dateString);
	}
	
	
	private static Map<String, Integer> offsetMap = new TreeMap<>();
	static {
		offsetMap.put("平成", 1988);
		offsetMap.put("昭和", 1925);
		offsetMap.put("大正", 1911);
		offsetMap.put("明治", 1867);
		
		offsetMap.put("万延", 1859);
		offsetMap.put("安政", 1853);
		offsetMap.put("嘉永", 1847);
		offsetMap.put("天保", 1829);
		offsetMap.put("文政", 1817);
		offsetMap.put("文化", 1803);
	}
	
	public final String string;
	public final int    year;
	
	private JapaneseDate(String newValue) {
		string = newValue;
		Matcher m = pat_DATE.matcher(string);
		if (m.matches()) {
			String era = m.group(1);
			String yearString = m.group(2);
			int yearNumber = yearString.equals("元") ? 1 : Integer.valueOf(yearString);
			
			if (offsetMap.containsKey(era)) {
				year = offsetMap.get(era) + yearNumber;
			} else {
				logger.error("Unpexpeced  era");
				logger.error("string {}!", string);
				logger.error("era {}!", era);
				throw new UnexpectedException("Unpexpeced  era");
			}
		} else {
			logger.error("Unpexpeced string");
			logger.error("string {}!", string);
			throw new UnexpectedException("Unpexpeced string");
		}
	}

	@Override
	public String toString() {
		return String.format("{%s %d}", string, year);
	}

}
