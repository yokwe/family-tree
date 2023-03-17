package yokwe.familytree;

public class Detail {
	enum Type {
		DEATH,
		BIRTH,
		MARRIAGE,
		BRANCH_FAMILY,
		RETIREMENT,
		DISINHERIT,
		HEIR,
	}
	
	public final String       string;
	//
	public final JapaneseDate date;
	public final Type         type;
	
	public Detail(String string, JapaneseDate date, Type type) {
		this.string = string;
		this.date   = date;
		this.type   = type;
	}
	
	public static class Death extends Detail {
		public final String place;
		
		public Death(String string, JapaneseDate date, String place) {
			super(string, date, Type.BIRTH);
			this.place = place;
		}
		public Death(String string, JapaneseDate date) {
			this(string, date, null);
		}
		
		@Override
		public String toString() {
			if (place == null) {
				return String.format("{%s %s}", date, type);
			} else {
				return String.format("{%s %s %s}", date, type, place);
			}
		}
	}
	
	public static class Birth extends Detail {
		public final String place;
		
		public Birth(String string, JapaneseDate date, String place) {
			super(string, date, Type.BIRTH);
			this.place = place;
		}
		public Birth(String string, JapaneseDate date) {
			this(string, date, null);
		}
		
		@Override
		public String toString() {
			if (place == null) {
				return String.format("{%s %s}", date, type);
			} else {
				return String.format("{%s %s %s}", date, type, place);
			}
		}
	}
	
	public static class Marriage extends Detail {
		public final String spouse;
		
		public Marriage(String string, JapaneseDate date, String spouse) {
			super(string, date, Type.MARRIAGE);
			this.spouse = spouse;
		}
		public Marriage(String string, JapaneseDate date) {
			this(string, date, null);
		}
		
		@Override
		public String toString() {
			if (spouse == null) {
				return String.format("{%s %s}", date, type);
			} else {
				return String.format("{%s %s %s}", date, type, spouse);
			}
		}
	}
	
	
}