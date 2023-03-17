package yokwe.familytree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import yokwe.util.UnexpectedException;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

public class FamilyRegister {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	@Sheet.SheetName("戸籍一覧")
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class Register extends Sheet {
		@ColumnName("戸籍ID")
		public String registerID;
		@ColumnName("シート")
		public String sheetName;
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
		String getID() {
			return personID;
		}
		@ColumnName("人物ID")
		public String personID;
		@ColumnName("姓")
		public String familyName;
		@ColumnName("名")
		public String givenName;
		@ColumnName("性別")
		public String gender;
		@ColumnName("続柄")
		public String relationship;
		@ColumnName("父")
		public String father;
		@ColumnName("母")
		public String mother;
		@ColumnName("配偶者")
		public String spouse;
		//
		@ColumnName("誕生日")
		public String birthDate;
		@ColumnName("結婚日")
		public String marriageDate;
		@ColumnName("死亡日")
		public String deathDate;
		//
		@ColumnName("誕生年")
		public String birthYear;
		@ColumnName("結婚年")
		public String marriageYear;
		@ColumnName("死亡年")
		public String deathYear;
		//
		@ColumnName("結婚年齢")
		public String marriageAge;
		@ColumnName("死亡年齢")
		public String deathAge;
	}
	
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class Record extends Sheet {
		@ColumnName("戸籍")
		public String registerID;
		@ColumnName("人物")
		public String personID;
		@ColumnName("項目")
		public String type;
		@ColumnName("内容")
		public String detail;
	}
	
	public static void readSheet(String url, List<Register> registerList, List<Person> personList, List<Record> familyList) {
		SpreadSheet spreadSheet = new SpreadSheet(url, true);
		List<String> sheetNameList = spreadSheet.getSheetNameList();
		for(var sheetName: sheetNameList) {
			if (sheetName.equals("戸籍一覧")) {
				var list = Sheet.extractSheet(spreadSheet, Register.class, sheetName);
				registerList.addAll(list);
			} else if (sheetName.equals("人物一覧")) {
				var list = Sheet.extractSheet(spreadSheet, Person.class, sheetName);
				personList.addAll(list);
			} else if (sheetName.startsWith("戸籍-")) {
				var list = Sheet.extractSheet(spreadSheet, Record.class, sheetName);
				familyList.addAll(list);
			} else {
				logger.info("Unknown sheetName {}", sheetName);
				throw new UnexpectedException("Unexpected");
			}
		}
		// sanity check
		logger.info("readSheet registerList {}", registerList.size());
		logger.info("readSheet personList   {}", personList.size());
		logger.info("readSheet familyList   {}", familyList.size());
		check(registerList, personList, familyList);
	}
	
	public static List<Record> filter(List<Record> familyList, String registerID) {
		return familyList.stream().filter(o -> o.registerID.equals(registerID)).collect(Collectors.toList());
	}
	
	private static Set<String> validFormat = new HashSet<>();
	static {
		validFormat.add(FamilyRecord.M19.FORMAT);
		validFormat.add(FamilyRecord.M31.FORMAT);
		validFormat.add(FamilyRecord.T04.FORMAT);
		validFormat.add(FamilyRecord.S23.FORMAT);
	}
	
	private static void check(List<Register> registerList, List<Person> personList, List<Record> familyList) {		
		int countError = 0;
		// check format
		{
			for(var e: registerList) {
				if (!validFormat.contains(e.format)) {
					logger.error("Unknown format  {}  {}", e.registerID, e.format);
					countError++;
				}
			}
		}
		// check registerID with registerID in familyList
		{
			Set<String> registerSet       = registerList.stream().map(o -> o.registerID).filter(o -> o != null).collect(Collectors.toSet());
			Set<String> registerSetFamily = familyList.stream().map(o -> o.registerID).filter(o -> o != null).collect(Collectors.toSet());

			Map<String, Integer> countMap = new TreeMap<>();
			registerSet.stream().forEach(o -> countMap.put(o, 0));
			Set<String> unknownSet = new TreeSet<>();
			
			//  personID
			for(var id: registerSetFamily) {
				if (!registerSet.contains(id)) unknownSet.add(id);
				countMap.put(id, 1);
			}
			for(var e: unknownSet) {
				logger.info("Unknown register id is used in familyList {}", e);
				countError++;
			}
			for(var e: countMap.keySet()) {
				if (countMap.get(e) == 0) {
					logger.error("Unused register id {}", e);
					countError++;
				}
			}
		}
		// check personID with personID in familyList
		{
			Set<String> personSet       = personList.stream().map(o -> o.personID).filter(o -> o != null).collect(Collectors.toSet());
			Set<String> personSetFamily = familyList.stream().map(o -> o.personID).filter(o -> o != null).collect(Collectors.toSet());
			
			Map<String, Integer> countMap = new TreeMap<>();
			personSet.stream().forEach(o -> countMap.put(o, 0));
			Set<String> unknownSet = new TreeSet<>();
			
			//  personID
			for(var id: personSetFamily) {
				if (!personSet.contains(id)) unknownSet.add(id);
				countMap.put(id, 1);
			}
			for(var e: unknownSet) {
				logger.info("Unknown person id is used in familyList {}", e);
				countError++;
			}
			for(var e: countMap.keySet()) {
				if (countMap.get(e) == 0) {
					logger.error("Unused person id {}", e);
					countError++;
				}
			}
		}
		if (countError != 0) {
			logger.error("Found Error  {}", countError);
			throw new UnexpectedException("");
		}
	}
}
