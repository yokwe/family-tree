package yokwe.familytree;

import java.util.ArrayList;
import java.util.List;

public abstract class FamilyRecord {
	public final String format;
	public final String id;       // registerID
	public final String domicile;
	
	FamilyRecord(String format, String id, String domicile) {
		this.format       = format;
		this.id           = id;
		this.domicile     = domicile;
	}
	
	public static class M19 extends FamilyRecord {
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
