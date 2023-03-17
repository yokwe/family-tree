package yokwe.familytree;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class FamilyRecord {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public final String format;
	public final String id;       // registerID
	public final String domicile;
	
	FamilyRecord(String format, String id, String domicile) {
		this.format       = format;
		this.id           = id;
		this.domicile     = domicile;
	}
	
	public static class M19 extends FamilyRecord {
		private static final Pattern PAT_DEATH = Pattern.compile("時(.+?)ニ於テ死亡");
		private static final Pattern PAT_BIRTH = Pattern.compile("^(.+?)ニ於テ出生");
		private static final Pattern PAT_MARRIAGE_A = Pattern.compile("番[戸地](.+?)ト婚姻届出");
		private static final Pattern PAT_MARRIAGE_B = Pattern.compile("^(.+?)ト婚姻届出");
		
		public static Detail toDetail(String string) {
			JapaneseDate date = JapaneseDate.getInstance(string);
			if (date == null) return null;
			// FIXME
			
			if (string.contains("死亡")) {
				Matcher m = PAT_DEATH.matcher(string);
				if (m.find()) {
					String place = m.group(1);
//					logger.info("DEATH  {}", place);
					return new Detail.Death(string, date, place);
				}
//				logger.info("## {}", string);
				return new Detail.Death(string, date);
			}
			if (string.contains("出生")) {
				Matcher m = PAT_BIRTH.matcher(string);
				if (m.find()) {
					String place = m.group(1);
//					logger.info("BIRTH  {}", place);
					return new Detail.Birth(string, date, place);
				}
				return new Detail. Birth(string, date);
			}
			if (string.contains("入籍")) {
				if (string.contains("携帯入籍")) return null;
				return new Detail. Marriage(string, date);
			}
			if (string.contains("婚姻")) {
				{
					Matcher m = PAT_MARRIAGE_A.matcher(string);
					if (m.find()) {
						String spouse = m.group(1);
//						logger.info("MARRIAGE  {}", spouse);
						return new Detail.Marriage(string, date, spouse);
					}
				}
				{
					Matcher m = PAT_MARRIAGE_B.matcher(string);
					if (m.find()) {
						String spouse = m.group(1);
//						logger.info("MARRIAGE  {}", spouse);
						return new Detail.Marriage(string, date, spouse);
					}
				}
//				logger.info("## {}", string);
			}
			logger.info("## {}", string);
			return null;
		}
		public static List<Detail> toDetailList(List<String> list) {
			List<Detail> result = new ArrayList<>();
			for(var e: list) {
				Detail detail = toDetail(e);
				if (detail == null) continue;
				result.add(detail);
			}
			return result;
		}
		
		public static class Member {
			public final String name;
			public final String relationship;
			// 戸主　母　妻　長男　二男　三男　長女　二女　三女　婦　孫
			public final String relationshipToFamily;

			public final String birthdate;
			
			public final List<Detail> details = new ArrayList<>();
			
			public Member(String name, String relationship, String relationshipToFamily, String birthdate) {
				this.name = name;
				this.relationship = relationship;
				this.relationshipToFamily = relationshipToFamily;
				this.birthdate = birthdate;
			}
			void addDetail(Detail detail) {
				details.add(detail);
			}
		}

		public static final String FORMAT = "明治19年式";
		
		public final String        previousHeadOfFamily;
		public final Member        headOfFamily;
		public final List<Member>  members = new ArrayList<>();
		
		public M19(String id, String domicile,
			String previousHeadOfFamily, Member headOfFamily, List<Member> members) {
			super(FORMAT, id, domicile);
			this.previousHeadOfFamily = previousHeadOfFamily;
			this.headOfFamily         = headOfFamily;
		}
		void addMember(Member member) {
			members.add(member);
		}
	}
	
	public static class M31  {
		public static final String FORMAT = "明治31年式";
	}
	public static class T04  {
		public static final String FORMAT = "大正4年式";
	}
	public static class S23  {
		public static final String FORMAT = "昭和23年式";
	}
}
