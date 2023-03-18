package yokwe.familytree;

import java.util.ArrayList;
import java.util.List;

public class LifeEvent implements Comparable<LifeEvent> {
	enum Type {
		DEATH,
		BIRTH,
		MARRIAGE,
		BRANCH,
		RETIREMENT,
		DISALLOW_INHERIT,
		ALLOW_INHERIT,
		INHERIT,
	}
	
	public static LifeEvent death(String string, JapaneseDate date, String value) {
		return new LifeEvent(string, Type.DEATH, date, value);
	}
	public static LifeEvent birth(String string, JapaneseDate date, String value) {
		return new LifeEvent(string, Type.BIRTH, date, value);
	}
	public static LifeEvent marriage(String string, JapaneseDate date, String value) {
		return new LifeEvent(string, Type.MARRIAGE, date, value);
	}
	public static LifeEvent branch(String string, JapaneseDate date, String value) {
		return new LifeEvent(string, Type.BRANCH, date, value);
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
		final String  keyword;
		final Handler handler;
		Pair(String keyword, Handler handler) {
			this.keyword = keyword;
			this.handler = handler;
		}
	}
	public static class Converter {
		private List<Pair> list = new ArrayList<>();
		
		public Converter() {
			addHandler(DisallowInherit.KEYWORD, new DisallowInherit());
			addHandler(AllowInherit.KEYWORD,    new AllowInherit());
			addHandler(Inherit.KEYWORD,         new Inherit());
			addHandler(Retirement.KEYWORD,      new Retirement());
		}
		private void addHandler(String keyword, Handler handler) {
			list.add(new Pair(keyword, handler));
		}
		
		public LifeEvent toLifeEvent(String string) {
			for(var e: list) {
				if (string.contains(e.keyword)) return e.handler.toLiveEvent(string);
			}
			return null;
		}
	}
}


class DisallowInherit implements LifeEvent.Handler {
	public static final String KEYWORD = "廃嫡";

	@Override
	public LifeEvent toLiveEvent(String string) {
		JapaneseDate date = JapaneseDate.getInstance(string);
		if (date == null) return null;
		return LifeEvent.disallowInherit(string, date);
	}
}
class AllowInherit implements LifeEvent.Handler {
	public static final String KEYWORD = "嗣子願済";

	@Override
	public LifeEvent toLiveEvent(String string) {
		JapaneseDate date = JapaneseDate.getInstance(string);
		if (date == null) return null;
		return LifeEvent.allowInherit(string, date);
	}
}
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
class Retirement implements LifeEvent.Handler {
	public static final String KEYWORD = "隠居";

	@Override
	public LifeEvent toLiveEvent(String string) {
		JapaneseDate date = JapaneseDate.getInstance(string);
		if (date == null) return null;
		return LifeEvent.retirement(string, date);
	}
}


