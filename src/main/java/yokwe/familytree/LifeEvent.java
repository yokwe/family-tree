package yokwe.familytree;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yokwe.util.JapaneseDate;
import yokwe.util.UnexpectedException;

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
	
	public LifeEvent(LifeEvent that, String value) {
		this(that.string, that.type, that.date, value);
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
			// order of addHandler is very important
			addHandler(Birth.KEYWORD,           new Birth());
			addHandler(Divorce.KEYWORD,         new Divorce());
			addHandler(Adoption.KEYWORD,        new Adoption());
			addHandler(Marriage.KEYWORD,        new Marriage());
			addHandler(Branch.KEYWORD,          new Branch());
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
			logger.debug("## REJECT {}", string);
			return null;
		}
	}
}


abstract class BaseHandler implements LifeEvent.Handler {
	protected final LifeEvent.Type type;
	BaseHandler(LifeEvent.Type type) {
		this.type = type;
	}
	
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
	
	protected LifeEvent getInstance(String string, JapaneseDate date, String value) {
		return new LifeEvent(string, type, date, value);
	}
	
	@Override
	public LifeEvent toLiveEvent(String detail) {
		// use concrete class name for logger
		final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
		
		String string = detail.replace("？", "");
		for(var m: excludes) {
			if (m.reset(string).find()) {
				logger.debug("## {} REJECT {}", type, string);
				return null;
			}
		}

		JapaneseDate date = JapaneseDate.getInstance(string);
		if (date == null) {
			logger.debug("## {} REJECT {}", type, string);
			return null;
		}

		if (includes.isEmpty()) {
			return getInstance(string, date, null);
		} else {
			for(var m: includes) {
				if (m.reset(string).find()) {
					int count = m.groupCount();
					if (count == 0) {
						return getInstance(string, date, null);
					} else if (count == 1){
						String value = m.group(1);
						return getInstance(string, date, value);
					} else {
						logger.error("count {}", count);
						throw new UnexpectedException("Unexpeted");
					}
				}
			}
		}
		logger.debug("## {} REJECT {}", type, string);
		return null;
	}
}

// BIRTH           ("出生"),
class Birth extends BaseHandler {
	public static final String KEYWORD = "出生";
	
	Birth() {
		super(LifeEvent.Type.BIRTH);
		include(
			"^(.+?)戸主.+?ニ於テ",
			"^(.+?)ニ於テ出生",
			"日(.+?)で?出生");
	}
	
	@Override
	public LifeEvent getInstance(String string, JapaneseDate date, String value) {
		return super.getInstance(string, null, value);
	}
}
// DEATH          ("死亡"),
class Death extends BaseHandler {
	public static final String KEYWORD = "死亡";

	Death() {
		super(LifeEvent.Type.DEATH);
		exclude(
			"日夫"
		);
		include(
			"分(.+?)ニ於テ死亡",
			"時(.+?)ニ於テ死亡",
			"日(.+?)ニ於テ死亡",
			"分(.+?)で死亡",
			"時(.+?)で死亡",
			"日(.+?)で死亡",
			"死亡$",
			".+?日.+?死亡.+?受付$"
		);
	}
}
// MARRIAGE        ("結婚"),
class Marriage extends BaseHandler {
	public static final String KEYWORD = "入籍|婚姻";

	Marriage() {
		super(LifeEvent.Type.MARRIAGE);
		exclude(
			"携帯入籍",
			"共ニ入籍",
			"死亡",
			"長男好弥");
		include(
			"日(.{1,7})ト婚姻",
			"番[地戸](.{1,7})ト婚姻",
			"入籍ス$",
			"^(.+)ト婚姻届出.+受付$",
			"ト婚姻届出",
			"^(.+)と婚姻夫の氏を称する旨",
			"日(.+)と婚姻届出",
			"^(.+)と婚姻届出");
	}
}
// ADOPTION        ("養子"),
class Adoption extends BaseHandler {
	public static final String KEYWORD = "養子|養嗣子|貰受ル";

	Adoption() {
		super(LifeEvent.Type.ADOPTION);
		exclude("を養子とする縁組届出");
	}
}
// DIVORCE         ("離婚"),
class Divorce extends BaseHandler {
	public static final String KEYWORD = "離婚";

	Divorce() {
		super(LifeEvent.Type.DIVORCE);
		exclude("父母離婚");
	}
}
// BRANCH          ("分家"),
class Branch extends BaseHandler {
	public static final String KEYWORD = "分家";
	
	Branch() {
		super(LifeEvent.Type.BRANCH);
		exclude(
			"共ニ除籍",
			"ニ従ヒ分家ス",
			"長谷川好弥父分家");
		include(
			"^(.+?)[ニに]?分家届出",
			"ニ分家届",
			"分家ス$");
	}
}
// RETIREMENT      ("隠居"),
class Retirement extends BaseHandler {
	public static final String KEYWORD = "隠居";

	Retirement() {
		super(LifeEvent.Type.RETIREMENT);
	}
}
// DISALLOW_INHERIT("廃嫡"),
class DisallowInherit extends BaseHandler {
	public static final String KEYWORD = "廃嫡";

	DisallowInherit() {
		super(LifeEvent.Type.DISALLOW_INHERIT);
	}
}
// ALLOW_INHERIT   ("嗣子"),
class AllowInherit extends BaseHandler {
	public static final String KEYWORD = "嗣子";

	AllowInherit() {
		super(LifeEvent.Type.ALLOW_INHERIT);
	}
}
// INHERIT         ("相続");
class Inherit extends BaseHandler {
	public static final String KEYWORD = "相続";

	Inherit() {
		super(LifeEvent.Type.INHERIT);
		exclude("抹消");
	}
}
