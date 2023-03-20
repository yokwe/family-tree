package yokwe.familytree;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LifeEvent implements Comparable<LifeEvent> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	enum Type {
		BIRTH           ("出生"),
		DEATH           ("死亡"),
		MARRIAGE        ("結婚"),
		ADOPTION        ("養子"),
		DIVORCE         ("離婚"),
		BRANCH          ("分家"),
		RETIREMENT      ("隠居"),
		DISALLOW_INHERIT("廃嫡"),
		ALLOW_INHERIT   ("嗣子"),
		INHERIT         ("相続");
		
		final String name;
		
		private Type(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	public static LifeEvent birth(String string, JapaneseDate date) {
		return new LifeEvent(string, Type.BIRTH, date);
	}
	public static LifeEvent birth(String string, String place) {
		return new LifeEvent(string, Type.BIRTH, null, place);
	}
	public static LifeEvent death(String string, JapaneseDate date) {
		return new LifeEvent(string, Type.DEATH, date);
	}
	public static LifeEvent death(String string, JapaneseDate date, String place) {
		return new LifeEvent(string, Type.DEATH, date, place);
	}
	public static LifeEvent marriage(String string, JapaneseDate date) {
		return new LifeEvent(string, Type.MARRIAGE, date);
	}
	public static LifeEvent adoption(String string, JapaneseDate date) {
		return new LifeEvent(string, Type.ADOPTION, date);
	}
	public static LifeEvent divorce(String string, JapaneseDate date) {
		return new LifeEvent(string, Type.DIVORCE, date);
	}
	public static LifeEvent branch(String string, JapaneseDate date) {
		return new LifeEvent(string, Type.BRANCH, date);
	}
	public static LifeEvent retirement(String string, JapaneseDate date) {
		return new LifeEvent(string, Type.RETIREMENT, date);
	}
	public static LifeEvent disallowInherit(String string, JapaneseDate date) {
		return new LifeEvent(string, Type.DISALLOW_INHERIT, date);
	}
	public static LifeEvent allowInherit(String string, JapaneseDate date) {
		return new LifeEvent(string, Type.ALLOW_INHERIT, date);
	}
	public static LifeEvent inherit(String string, JapaneseDate date) {
		return new LifeEvent(string, Type.INHERIT, date);
	}
	
	public final String       string;
	public final Type         type;
	public final JapaneseDate date;
	public final String       value;
	
	private LifeEvent(String string, Type type, JapaneseDate date, String value) {
		this.string = string;
		this.type   = type;
		this.date   = date;
		this.value  = value;
	}
	private LifeEvent(String string, Type type, JapaneseDate date) {
		this(string, type, date, null);
	}
	private LifeEvent(String string, Type type, String value) {
		this(string, type, null, value);
	}
	
	private static boolean verboseString = false;
	public static void setVerboseString(boolean newValue) {
		verboseString = newValue;
	}
	@Override
	public String toString() {
		if (verboseString) return toStringVerbose();
		if (date == null) {
			return String.format("{#### %s %s}", type, value);
		} else if (value == null) {
			return String.format("{%s %s}", date, type);
		} else {
			return String.format("{%s %s %s}", date, type, value);
		}
	}
	private String toStringVerbose() {
		if (date == null) {
			return String.format("{#### %s %s  %s}", type, value, string);
		} else if (value == null) {
			return String.format("{%s %s  %s}", date, type, string);
		} else {
			return String.format("{%s %s %s  %s}", date, type, value, string);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof LifeEvent) {
			return compareTo((LifeEvent)o) == 0;
		} else {
			return false;
		}
	}
	@Override
	public int compareTo(LifeEvent that) {
		int ret = 0;
		if (ret == 0 && this.date != null && that.date != null) ret = this.date.compareTo(that.date);
		if (ret == 0) ret = this.type.compareTo(that.type);
		if (ret == 0 && this.value != null && that.value != null) ret = this.value.compareTo(that.value);
		return ret;
	}
	
	
	public interface Handler {
		LifeEvent toLiveEvent(String string);
	}
	private static class Pair {
		final Matcher  matcher;
		final Handler  handler;
		Pair(String pattern, Handler handler) {
			this.matcher = Pattern.compile(pattern).matcher("");
			this.handler = handler;
		}
	}
	public static class Converter {
		private List<Pair> list = new ArrayList<>();
		
		public Converter() {
			addHandler(Birth.KEYWORD,           new Birth());
			// FIXME FROM HERE
			addHandler(Marriage.KEYWORD,        new Marriage());
			addHandler(Adoption.KEYWORD,        new Adoption());
			addHandler(Divorce.KEYWORD,         new Divorce());
			addHandler(Branch.KEYWORD,          new Branch());
			// FIXME UNTIL HERE
			addHandler(DisallowInherit.KEYWORD, new DisallowInherit());
			addHandler(AllowInherit.KEYWORD,    new AllowInherit());
			addHandler(Inherit.KEYWORD,         new Inherit());
			addHandler(Retirement.KEYWORD,      new Retirement());
			addHandler(Death.KEYWORD,           new Death());
		}
		private void addHandler(String keyword, Handler handler) {
			list.add(new Pair(keyword, handler));
		}
		
		public LifeEvent toLifeEvent(String string) {
			for(var pair: list) {
				if (pair.matcher.reset(string).find()) return pair.handler.toLiveEvent(string);
			}
			logger.info("##  {}", string);
			return null;
		}
	}
}


// DEATH           ("出生"),
class Birth implements LifeEvent.Handler {
	public static final String KEYWORD = "出生";
	
	private static Matcher M_A = Pattern.compile("^(.+?)ニ於テ出生").matcher("");
	private static Matcher M_B = Pattern.compile("日(.+?)で出生").matcher("");
	
	@Override
	public LifeEvent toLiveEvent(String string) {
		M_A.reset(string);
		if (M_A.find()) {
			String place = M_A.group(1);
			return LifeEvent.birth(string, place);
		}
		M_B.reset(string);
		if (M_B.find()) {
			String place = M_B.group(1);
			return LifeEvent.birth(string, place);
		}
		if (string.contains("出生届出")) return null;
		// return LifeEvent.birth("## " + string, date);
		return null;
	}
}
// BIRTH           ("死亡"),
class Death implements LifeEvent.Handler {
	public static final String KEYWORD = "死亡";

	private static Matcher M_A = Pattern.compile("日夫.*死亡$").matcher("");
	private static Matcher M_B = Pattern.compile("日死亡$").matcher("");
	private static Matcher M_C = Pattern.compile("分(.+?)ニ於テ死亡").matcher("");
	private static Matcher M_D = Pattern.compile("時(.+?)ニ於テ死亡").matcher("");
	private static Matcher M_E = Pattern.compile("日(.+?)ニ於テ死亡").matcher("");
	private static Matcher M_F = Pattern.compile("分(.+?)で死亡").matcher("");
	private static Matcher M_G = Pattern.compile("時(.+?)で死亡").matcher("");
	private static Matcher M_H = Pattern.compile("日(.+?)で死亡").matcher("");
	
	@Override
	public LifeEvent toLiveEvent(String string) {
		JapaneseDate date = JapaneseDate.getInstance(string);
		if (date == null) return null;
		if (M_A.reset(string).find()) return null;
		if (M_B.reset(string).find()) {
			return LifeEvent.death(string, date);
		}
		if (M_C.reset(string).find()) {
			String place = M_C.group(1);
			return LifeEvent.death(string, date, place);
		}
		if (M_D.reset(string).find()) {
			String place = M_D.group(1);
			return LifeEvent.death(string, date, place);
		}
		if (M_E.reset(string).find()) {
			String place = M_E.group(1);
			return LifeEvent.death(string, date, place);
		}
		if (M_F.reset(string).find()) {
			String place = M_F.group(1);
			return LifeEvent.death(string, date, place);
		}
		if (M_G.reset(string).find()) {
			String place = M_G.group(1);
			return LifeEvent.death(string, date, place);
		}
		if (M_H.reset(string).find()) {
			String place = M_H.group(1);
			return LifeEvent.death(string, date, place);
		}
		// return LifeEvent.birth("## " + string, date);
		return null;
	}
}
// MARRIAGE        ("結婚"),
class Marriage implements LifeEvent.Handler {
	public static final String KEYWORD = "入籍|婚姻";

	@Override
	public LifeEvent toLiveEvent(String string) {
		JapaneseDate date = JapaneseDate.getInstance(string);
		if (date == null) return null;
		return LifeEvent.marriage(string, date);
	}
}
// ADOPTION        ("養子"),
class Adoption implements LifeEvent.Handler {
	public static final String KEYWORD = "養子";

	@Override
	public LifeEvent toLiveEvent(String string) {
		JapaneseDate date = JapaneseDate.getInstance(string);
		if (date == null) return null;
		return LifeEvent.adoption(string, date);
	}
}
// DIVORCE         ("離婚"),
class Divorce implements LifeEvent.Handler {
	public static final String KEYWORD = "離婚";

	@Override
	public LifeEvent toLiveEvent(String string) {
		JapaneseDate date = JapaneseDate.getInstance(string);
		if (date == null) return null;
		return LifeEvent.divorce(string, date);
	}
}
// BRANCH          ("分家"),
class Branch implements LifeEvent.Handler {
	public static final String KEYWORD = "分家";

	@Override
	public LifeEvent toLiveEvent(String string) {
		JapaneseDate date = JapaneseDate.getInstance(string);
		if (date == null) return null;
		return LifeEvent.branch(string, date);
	}
}
// RETIREMENT      ("隠居"),
class Retirement implements LifeEvent.Handler {
	public static final String KEYWORD = "隠居";

	@Override
	public LifeEvent toLiveEvent(String string) {
		JapaneseDate date = JapaneseDate.getInstance(string);
		if (date == null) return null;
		return LifeEvent.retirement(string, date);
	}
}
// DISALLOW_INHERIT("廃嫡"),
class DisallowInherit implements LifeEvent.Handler {
	public static final String KEYWORD = "廃嫡";

	@Override
	public LifeEvent toLiveEvent(String string) {
		JapaneseDate date = JapaneseDate.getInstance(string);
		if (date == null) return null;
		return LifeEvent.disallowInherit(string, date);
	}
}
// ALLOW_INHERIT   ("嗣子"),
class AllowInherit implements LifeEvent.Handler {
	public static final String KEYWORD = "嗣子";

	@Override
	public LifeEvent toLiveEvent(String string) {
		JapaneseDate date = JapaneseDate.getInstance(string);
		if (date == null) return null;
		return LifeEvent.allowInherit(string, date);
	}
}
// INHERIT         ("相続");
class Inherit implements LifeEvent.Handler {
	public static final String KEYWORD = "相続";

	@Override
	public LifeEvent toLiveEvent(String string) {
		if (string.contains("抹消")) return null;
		JapaneseDate date = JapaneseDate.getInstance(string);
		if (date == null) return null;
		return LifeEvent.inherit(string, date);
	}
}
