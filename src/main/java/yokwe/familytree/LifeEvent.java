package yokwe.familytree;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yokwe.familytree.Detail.Type;

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
	
	
	public final String       string;
	public final Type         type;
	public final JapaneseDate date;
	public final String       value;
	
	public LifeEvent(String string, Type type, JapaneseDate date, String value) {
		this.string = string;
		this.type   = type;
		this.date   = date;
		this.value  = value;
	}
	public LifeEvent(String string, Type type, JapaneseDate date) {
		this(string, type, date, null);
	}
	public LifeEvent(String string, Type type, String value) {
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
			addHandler(Adoption.KEYWORD,        new Adoption());
			addHandler(Divorce.KEYWORD,         new Divorce());
			addHandler(Marriage.KEYWORD,        new Marriage());
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


abstract class BaseHandler implements LifeEvent.Handler {
	protected List<Matcher> includes = new ArrayList<>();
	protected void include(String... args) {
		for(var e: args) {
			includes.add(Pattern.compile(e).matcher(""));
		}
	}
	protected List<Matcher> excludes = new ArrayList<>();
	protected void exclude(String... args) {
		for(var e: args) {
			excludes.add(Pattern.compile(e).matcher(""));
		}
	}
	
	abstract protected LifeEvent getInstance(String string, JapaneseDate date, String value);
	protected LifeEvent getInstance(String string, JapaneseDate date) {
		return getInstance(string, date, null);
	}
	protected LifeEvent getInstance(String string, String value) {
		return getInstance(string, null, value);
	}
	
	@Override
	public LifeEvent toLiveEvent(String string) {
		for(var m: excludes) {
			if (m.reset(string).find()) return null;
		}

		JapaneseDate date = JapaneseDate.getInstance(string);
		if (date == null) return null;

		if (includes.isEmpty()) {
			return getInstance(string, date);
		} else {
			for(var m: includes) {
				if (m.reset(string).find()) {
					String value = m.group(1);
					return getInstance(string, date, value);
				}
			}
		}
		return null;
	}
}

// BIRTH           ("出生"),
class Birth extends BaseHandler {
	public static final String KEYWORD = "出生";
	
	Birth() {
//		exclude("出生届出");
		include(
			"^(.+?)ニ於テ出生",
			"\"日(.+?)で出生\"");
	}
	
	public LifeEvent getInstance(String string, JapaneseDate date, String value) {
		return new LifeEvent(string, LifeEvent.Type.BIRTH, null, value);
	}
}
// DEATH          ("死亡"),
class Death extends BaseHandler {
	public static final String KEYWORD = "死亡";

	Death() {
		include(
			"分(.+?)ニ於テ死亡",
			"時(.+?)ニ於テ死亡",
			"日(.+?)ニ於テ死亡",
			"分(.+?)で死亡",
			"時(.+?)で死亡",
			"日(.+?)で死亡"
		);
	}
	
	@Override
	public LifeEvent getInstance(String string, JapaneseDate date, String value) {
		return new LifeEvent(string, LifeEvent.Type.DEATH, date, value);
	}
}
// MARRIAGE        ("結婚"),
class Marriage extends BaseHandler {
	public static final String KEYWORD = "入籍|婚姻";

	Marriage() {
		exclude(
			"携帯入籍",
			"共ニ入籍",
			"死亡");
		include(
			"日(.+?)ト婚姻",
			"番[地戸](.+?)ト婚姻",
			"日(.+)入籍ス",
			"^(.+)ト婚姻届出.+受付$",
			"^(.+)と婚姻夫の氏を称する旨",
			"日(.+)と婚姻届出",
			"^(.+)と婚姻届出");
	}
	
	@Override
	public LifeEvent getInstance(String string, JapaneseDate date, String value) {
		return new LifeEvent(string, LifeEvent.Type.MARRIAGE, date, value);
	}
}
// ADOPTION        ("養子"),
class Adoption extends BaseHandler {
	public static final String KEYWORD = "養子";

	Adoption() {
		//
	}
	
	@Override
	public LifeEvent getInstance(String string, JapaneseDate date, String value) {
		return new LifeEvent(string, LifeEvent.Type.ADOPTION, date, value);
	}
}
// DIVORCE         ("離婚"),
class Divorce extends BaseHandler {
	public static final String KEYWORD = "離婚";

	Divorce() {
		//
	}
	
	@Override
	public LifeEvent getInstance(String string, JapaneseDate date, String value) {
		return new LifeEvent(string, LifeEvent.Type.DIVORCE, date, value);
	}
}
// BRANCH          ("分家"),
class Branch extends BaseHandler {
	public static final String KEYWORD = "分家";
	
	Branch() {
		//
	}
	
	@Override
	public LifeEvent getInstance(String string, JapaneseDate date, String value) {
		return new LifeEvent(string, LifeEvent.Type.BRANCH, date, value);
	}
}
// RETIREMENT      ("隠居"),
class Retirement extends BaseHandler {
	public static final String KEYWORD = "隠居";

	Retirement() {
		//
	}
	
	@Override
	public LifeEvent getInstance(String string, JapaneseDate date, String value) {
		return new LifeEvent(string, LifeEvent.Type.RETIREMENT, date, value);
	}
}
// DISALLOW_INHERIT("廃嫡"),
class DisallowInherit extends BaseHandler {
	public static final String KEYWORD = "廃嫡";

	DisallowInherit() {
		//
	}
	
	@Override
	public LifeEvent getInstance(String string, JapaneseDate date, String value) {
		return new LifeEvent(string, LifeEvent.Type.DISALLOW_INHERIT, date, value);
	}
}
// ALLOW_INHERIT   ("嗣子"),
class AllowInherit extends BaseHandler {
	public static final String KEYWORD = "嗣子";

	AllowInherit() {
		//
	}
	
	@Override
	public LifeEvent getInstance(String string, JapaneseDate date, String value) {
		return new LifeEvent(string, LifeEvent.Type.ALLOW_INHERIT, date, value);
	}
}
// INHERIT         ("相続");
class Inherit extends BaseHandler {
	public static final String KEYWORD = "相続";

	Inherit() {
		//
	}
	
	@Override
	public LifeEvent getInstance(String string, JapaneseDate date, String value) {
		return new LifeEvent(string, LifeEvent.Type.INHERIT, date, value);
	}
}
