package yokwe.familytree;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yokwe.util.UnexpectedException;

public class Detail implements Comparable<Detail> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	enum Type {
		DEATH,
		BIRTH,
		MARRIAGE,
		BRANCH,
		RETIREMENT,
		DISINHERIT,
		HEIR,
	}
	
	public static Detail Death(JapaneseDate date, String value) {
		return new Detail(date, Type.DEATH, value);
	}
	public static Detail Death(JapaneseDate date) {
		return new Detail(date, Type.DEATH);
	}
	public static Detail Birth(JapaneseDate date, String value) {
		return new Detail(date, Type.BIRTH, value);
	}
	public static Detail Birth(JapaneseDate date) {
		return new Detail(date, Type.BIRTH);
	}
	public static Detail Marriage(JapaneseDate date, String value) {
		return new Detail(date, Type.MARRIAGE, value);
	}
	public static Detail Marriage(JapaneseDate date) {
		return new Detail(date, Type.MARRIAGE);
	}
	
	
	public final JapaneseDate date;
	public final Type         type;
	public final String       value;
	
	private Detail(JapaneseDate date, Type type, String value) {
		this.date   = date;
		this.type   = type;
		this.value  = value;
	}
	private Detail(JapaneseDate date, Type type) {
		this(date, type, null);
	}
	
	@Override
	public String toString() {
		if (value != null) {
			return String.format("{%s %s %s}", date, type, value);
		} else {
			return String.format("{%s %s}", date, type);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Detail) {
			return compareTo((Detail)o) == 0;
		} else {
			return false;
		}
	}
	@Override
	public int compareTo(Detail that) {
		int ret = this.date.compareTo(that.date);
		if (ret == 0) ret = this.type.compareTo(that.type);
		return ret;
	}
	
	
	public static class M19 {
		private static final Pattern PAT_DEATH      = Pattern.compile("時(.+?)ニ於テ死亡");
		private static final Pattern PAT_BIRTH      = Pattern.compile("^(.+?)ニ於テ出生");
		private static final Pattern PAT_MARRIAGE_A = Pattern.compile("番[戸地](.+?)ト婚姻届出");
		private static final Pattern PAT_MARRIAGE_B = Pattern.compile("^(.+?)ト婚姻届出");
		
		public static Detail getInstance(String string) {
			JapaneseDate date = JapaneseDate.getInstance(string);
			if (date == null) return null;
			
			if (string.contains("死亡")) {
				Matcher m = PAT_DEATH.matcher(string);
				if (m.find()) {
					String place = m.group(1);
//					logger.info("DEATH  {}", place);
					return Detail.Death(date, place);
				}
//				logger.info("## {}", string);
				return Detail.Death(date);
			}
			if (string.contains("出生")) {
				Matcher m = PAT_BIRTH.matcher(string);
				if (m.find()) {
					String place = m.group(1);
//					logger.info("BIRTH  {}", place);
					return Detail.Birth(date, place);
				}
				return Detail.Birth(date);
			}
			if (string.contains("入籍")) {
				if (string.contains("携帯入籍")) return null;
//				logger.info("MARRIAGE {}", string);
				return Detail.Marriage(date);
			}
			if (string.contains("婚姻")) {
				{
					Matcher m = PAT_MARRIAGE_A.matcher(string);
					if (m.find()) {
						String spouse = m.group(1);
//						logger.info("MARRIAGE  {}", spouse);
						return Detail.Marriage(date, spouse);
					}
				}
				{
					Matcher m = PAT_MARRIAGE_B.matcher(string);
					if (m.find()) {
						String spouse = m.group(1);
//						logger.info("MARRIAGE  {}", spouse);
						return Detail.Marriage(date, spouse);
					}
				}
				logger.error("婚姻 {}!", string);
				throw new UnexpectedException("Unpexpeced");
			}
			logger.info("## {}", string);
			return null;
		}

		public static List<Detail> toDetailList(List<String> list) {
			List<Detail> result = new ArrayList<>();
			for(var e: list) {
				Detail detail = getInstance(e);
				if (detail != null) result.add(detail);
			}
			return result;
		}
	}
}