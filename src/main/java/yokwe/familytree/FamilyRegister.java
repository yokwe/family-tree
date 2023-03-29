package yokwe.familytree;

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
		@ColumnName("本籍")
		public String domicile;
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
		// list of type for sheet
		//   前戸主, 形式, 本籍, 氏名, 番号, 説明
		// list of type for member
		//   出生, 前戸主トノ続柄, 名前, 家族トノ続柄, 戸主ト成リタル原因及ヒ年月日, 母, 父, 父母トノ続柄, 続柄, 記載事項, 養母, 養父, 養父母トノ続柄
		// list of value for 続柄　※戸主との続柄
		//   三女, 三男, 二女, 二男, 五女, 五男, 四女, 四男, 夫, 妹, 妻, 姉, 姪, 婦, 孫, 弟, 弟妻, 戸主, 母, 父, 甥, 長女, 長男, 養子, 養母, 養父, 養父の養女
		// list of value for 前戸主トノ続柄
		//   -, 二男, 加藤角蔵長男, 神谷安左エ門養嗣子, 神谷齢蔵長男, 長男, 長谷川鎌吉三男
		// list of value for 家族トノ続柄
		//   -, 三男上一妻, 三男嘉太郎妻, 二男嘉市妻, 二男嘉市長女, 二男嘉市長男, 二男清一妻, 二男隆一妻, 五男藤内妻, 四男四郎妻, 弟助治郎二男, 弟助治郎妻, 弟助治郎長男, 弟嘉太郎妻, 弟清一妻, 弟藤内妻, 父嘉兵衛二男, 父嘉兵衛妻, 父龍蔵妻, 父龍蔵長男, 長男坂市妻, 長男好弥妻, 長男孝次妻, 長男藤内妻, 養父安左エ門妻, 養父安左衛門養女
		// list of value for 父母トノ続柄
		//   三女, 三男, 二女, 二男, 五女, 五男, 四女, 四男, 長女, 長男, ？女, ？男
		// list of value for 養父母トノ続柄
		//   養子
		@ColumnName("戸籍")
		public String registerID;
		@ColumnName("人物")
		public String personID;
		@ColumnName("項目")
		public String type;
		@ColumnName("内容")
		public String detail;
	}
	
	// constant for sheet
	public static final String HEAD         = "戸主";
	public static final String EX_HEAD      = "前戸主";
	public static final String FORMAT       = "形式";
	public static final String DOMICILE     = "本籍";
	public static final String FULL_NAME    = "氏名";
	public static final String DESCRIPTION  = "説明";
	
	// constants for member
	public static final String BIRTH              = "出生";
	public static final String NAME               = "名前";
	public static final String MOTHER             = "母";
	public static final String FATHER             = "父";
	public static final String RELATION           = "続柄";
	public static final String RELATION_TO_PARENT = "父母トノ続柄";
	public static final String RELATION_TO_FAMILY = "家族トノ続柄";
	public static final String DESCRIBED_ITEM     = "記載事項";
	
	// constant for relation
	public static final String WIFE               = "妻";
	
	
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
	
	public static final String FORMAT_M19 = "明治19年式";
	public static final String FORMAT_M31 = "明治31年式";
	public static final String FORMAT_T04 = "大正4年式";
	public static final String FORMAT_S23 = "昭和23年式";
	
	private static Set<String> validFormat = new HashSet<>();
	static {
		validFormat.add(FORMAT_M19);
		validFormat.add(FORMAT_M31);
		validFormat.add(FORMAT_T04);
		validFormat.add(FORMAT_S23);
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
