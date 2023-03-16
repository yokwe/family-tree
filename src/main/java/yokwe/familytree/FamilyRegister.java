package yokwe.familytree;

import yokwe.util.libreoffice.Sheet;

public class FamilyRegister {
	@Sheet.SheetName("戸籍一覧")
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class Register extends Sheet {
		@ColumnName("戸籍ID")
		public String registerID;
		@ColumnName("形式")
		public String format;
		@ColumnName("番号右上")
		public String number;
		@ColumnName("姓")
		public String familyName;
		@ColumnName("前戸主")
		public String previousHeadOfFamily;
		@ColumnName("戸主")
		public String headOfFamily;
	}
	
	@Sheet.SheetName("人物一覧")
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class Person extends Sheet {
		@ColumnName("人物ID")
		public String personID;
		@ColumnName("姓")
		public String familyName;
		@ColumnName("名")
		public String givenName;
		@ColumnName("性別")
		public String gender;
		@ColumnName("続柄")
		public String relationShip;
		//
		@ColumnName("誕生日")
		public String birthDate;
		@ColumnName("結婚日")
		public String marriageDate;
		@ColumnName("死亡日")
		public String deathDate;
		//
		@ColumnName("誕生年西暦")
		public String birthDateAD;
		@ColumnName("結婚年西暦")
		public String marriageDateAD;
		@ColumnName("死亡年西暦")
		public String deathDateAD;
		//
		@ColumnName("結婚年齢概算")
		public String marriageAge;
		@ColumnName("死亡年齢概算")
		public String deathAge;
	}
	
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class Family extends Sheet {
		@ColumnName("戸籍")
		public String registerID;
		@ColumnName("人物")
		public String personID;
		@ColumnName("項目")
		public String type;
		@ColumnName("内容")
		public String detail;
	}
	
}
