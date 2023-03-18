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
		public static final String FORMAT = "明治19年式";

		public static class Member {
			public final String name;
			public final String relation;
			// 戸主　母　妻　長男　二男　三男　長女　二女　三女　婦　孫
			public final String relationToFamly;

			public final String birthdate;
			
			public final List<Detail> details = new ArrayList<>();
			
			public Member(String name, String relation, String relationToFamily, String birthdate) {
				this.name            = name;
				this.relation        = relation;
				this.relationToFamly = relationToFamily;
				this.birthdate       = birthdate;
			}
			void addDetail(Detail detail) {
				details.add(detail);
			}
		}

		public final String        previousHead;
		public final Member        head;
		public final List<Member>  members = new ArrayList<>();
		
		public M19(String id, String domicile,
			String previousHead, Member head, List<Member> members) {
			super(FORMAT, id, domicile);
			this.previousHead = previousHead;
			this.head         = head;
		}
		void addMember(Member member) {
			members.add(member);
		}
	}
	
	public static class M31 extends FamilyRecord {
		public static final String FORMAT = "明治31年式";

		// FIXME Member
		public static class Member {
			
		}
		
		public final String        previousHead;
		// FIXME heead
		public final Member        head;
		public final List<Member>  members = new ArrayList<>();

		M31(String format, String id, String domicile,
			String previousHead, Member head, List<Member> members) {
			super(FORMAT, id, domicile);
			this.previousHead = previousHead;
			this.head         = head;
		}
		void addMember(Member member) {
			members.add(member);
		}
	}
	public static class T04  {
		public static final String FORMAT = "大正4年式";
	}
	public static class S23  {
		public static final String FORMAT = "昭和23年式";
	}
}
