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
	public static LifeEvent death(String string, JapaneseDate date) {
		return new LifeEvent(string, Type.DEATH, date);
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
			addHandler(Death.KEYWORD,           new Death());
			addHandler(Birth.KEYWORD,           new Birth());
			addHandler(Marriage.KEYWORD,        new Marriage());
			addHandler(Adoption.KEYWORD,        new Adoption());
			addHandler(Divorce.KEYWORD,         new Divorce());
			addHandler(Branch.KEYWORD,          new Branch());
			addHandler(Retirement.KEYWORD,      new Retirement());
			addHandler(DisallowInherit.KEYWORD, new DisallowInherit());
			addHandler(AllowInherit.KEYWORD,    new AllowInherit());
			addHandler(Inherit.KEYWORD,         new Inherit());
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

	@Override
	public LifeEvent toLiveEvent(String string) {
		JapaneseDate date = JapaneseDate.getInstance(string);
		if (date == null) return null;
		return LifeEvent.birth(string, date);
	}
}
// BIRTH           ("死亡"),
class Death implements LifeEvent.Handler {
	public static final String KEYWORD = "死亡";

	@Override
	public LifeEvent toLiveEvent(String string) {
		JapaneseDate date = JapaneseDate.getInstance(string);
		if (date == null) return null;
		return LifeEvent.death(string, date);
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
