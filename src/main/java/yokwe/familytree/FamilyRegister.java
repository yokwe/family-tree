package yokwe.familytree;

import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.Sheet.ColumnName;

public class FamilyRegister {
	@Sheet.SheetName("戸籍一覧")
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class Register {
		@ColumnName("戸籍ID")
		String registerID;
		@ColumnName("形式")
		String format;
		@ColumnName("番号右上")
		String number;
		@ColumnName("姓")
		String familyName;
		@ColumnName("前戸主")
		String previousHeadOfFamily;
		@ColumnName("戸主")
		String headOfFamily;
	}
	
	@Sheet.SheetName("人物一覧")
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class Person {
		@ColumnName("人物ID")
		String perrsonID;
		@ColumnName("姓")
		String familyName;
		@ColumnName("名")
		String givenName;
		@ColumnName("性")
		String gender;
		@ColumnName("続柄")
		String relationShip;
		//
		@ColumnName("誕生日")
		String birthDate;
		@ColumnName("結婚日")
		String marriageDate;
		@ColumnName("死亡日")
		String deathDate;
		//
		@ColumnName("誕生年西暦")
		String birthDateAD;
		@ColumnName("結婚年西暦")
		String marriageDateAD;
		@ColumnName("死亡年西暦")
		String deathDateAD;
	}
	
	@Sheet.SheetName("戸籍")
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class Family {
		@ColumnName("戸籍")
		String registerID;
		@ColumnName("人物")
		String personID;
		@ColumnName("項目")
		String type;
		@ColumnName("内容")
		String detail;
	}
	
}
