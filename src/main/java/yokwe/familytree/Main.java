package yokwe.familytree;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yokwe.familytree.FamilyRegister.Record;
import yokwe.familytree.FamilyRegister.Person;
import yokwe.familytree.FamilyRegister.Register;
import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;;

public class Main {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static String URL_FAMILY_REGISTER = StringUtil.toURLString("tmp/family-register.ods");
	
	public static class PersonMap {
		private Map<String, Person> map = new LinkedHashMap<>();
		
		public PersonMap(List<Person> personList) {
			for(var person: personList) {
				if (map.containsKey(person.personID)) {
					logger.error("Duplicate personID  {}", person.personID);
					throw new UnexpectedException("Unpexpected");
				} else {
					map.put(person.personID, person);
				}
			}
		}
		public Set<String> keySet() {
			return map.keySet();
		}
		public boolean containsKey(String personID) {
			return map.containsKey(personID);
		}
		public Person get(String personID) {
			if (map.containsKey(personID)) {
				return map.get(personID);
			} else {
				logger.error("Unknown personID  {}", personID);
				throw new UnexpectedException("Unpexpected");
			}
		}
	}
	public static class RegisterMap {
		private Map<String, Register> map = new LinkedHashMap<>();
		
		public RegisterMap(List<Register> registerList) {
			for(var register: registerList) {
				if (map.containsKey(register.registerID)) {
					logger.error("Duplicate registerID  {}", register.registerID);
					throw new UnexpectedException("Unpexpected");
				} else {
					map.put(register.registerID, register);
				}
			}
		}
		public Set<String> keySet() {
			return map.keySet();
		}
		public boolean containsKey(String personID) {
			return map.containsKey(personID);
		}
		public Register get(String registerID) {
			if (map.containsKey(registerID)) {
				return map.get(registerID);
			} else {
				logger.error("Unknown registerID  {}", registerID);
				throw new UnexpectedException("Unpexpected");
			}
		}
	}
	
	public static class PersonEntry {
		public final String registerID;
		public final String key;
		public final String value;
		public       String format = null;
		
		public PersonEntry(Record family) {
			this.registerID = family.registerID;
			this.key        = family.type;
			this.value      = family.detail;
		}
	}
	public static class PersonEntryMap {
		private Map<String, List<PersonEntry>> entryListMap = new TreeMap<>();
		//          personID
		private Map<String, String>      formatMap = new TreeMap<>();
		//          personID
		private Set<String> registerIDSet = new TreeSet<>();
		
		public PersonEntryMap(List<Record> familyList) {
			for(var e: familyList) {
				add(e);
			}
			fixFormat();
		}
		
		public void add(Record family) {
			registerIDSet.add(family.registerID);
			
			if (family.type.equals("形式")) formatMap.put(family.registerID, family.detail);
			if (family.personID == null) return;
			String key   = family.personID;
			PersonEntry  entry = new PersonEntry(family);
			
			List<PersonEntry> list;
			if (entryListMap.containsKey(key)) {
				list = entryListMap.get(key);
			} else {
				list = new ArrayList<PersonEntry>();
				entryListMap.put(key, list);
			}
			list.add(entry);
		}
		public Set<String> getPersonIDSet() {
			return entryListMap.keySet();
		}
		public Set<String> getRegisterIDSet() {
			return registerIDSet;
		}
		public Collection<List<PersonEntry>> values() {
			return entryListMap.values();
		}
		public boolean containsKey(String personID) {
			return entryListMap.containsKey(personID);
		}
		public List<PersonEntry>	get(String personID) {
			if (entryListMap.containsKey(personID)) {
				return entryListMap.get(personID);
			} else {
				logger.error("Unknown personID  {}", personID);
				throw new UnexpectedException("Unpexpected");
			}
		}
		public Set<Map.Entry<String, List<PersonEntry>>> entrySet() {
			return entryListMap.entrySet();
		}
		public void fixFormat() {
			for(var list: entryListMap.values()) {
				for(var e: list) {
					e.format = getFormat(e.registerID);
				}
			}
		}
		public String getFormat(String registerID) {
			if (formatMap.containsKey(registerID)) {
				return formatMap.get(registerID);
			} else {
				logger.error("Unknown registerID  {}", registerID);
				throw new UnexpectedException("Unpexpected");
			}
		}
	}

	private static void load(List<Person> personList, List<Register> registerList, List<Record> familyList) {
		SpreadSheet spreadSheet = new SpreadSheet(URL_FAMILY_REGISTER, true);
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
	}
	
	private static void check(PersonMap personMap, RegisterMap registerMap, PersonEntryMap personEntryMap) {
		int countError = 0;
		// check personID with personID in familyList
		{
			Map<String, Integer> countMap = new TreeMap<>();
			//  personID
			for(var e: personMap.keySet()) {
				countMap.put(e, 0);
			}
			Set<String> unknownSet = new TreeSet<>();
			//  personID
			Set<String> set = personEntryMap.getPersonIDSet();
			for(var personID: set) {
				if (!personMap.containsKey(personID)) {
					unknownSet.add(personID);
				}
				countMap.put(personID, 1);
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
		// check registerID with registerID in familyList
		{
			Map<String, Integer> countMap = new TreeMap<>();
			//  registerID
			for(var e: registerMap.keySet()) {
				countMap.put(e, 0);
			}
			Set<String> unknownSet = new TreeSet<>();
			//  registerID
			Set<String> set = personEntryMap.getRegisterIDSet();
			for(var registerID: set) {
				if (!registerMap.containsKey(registerID)) {
					unknownSet.add(registerID);
				}
				countMap.put(registerID, 1);
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
		if (countError != 0) {
			logger.warn("Found Error  {}", countError);
		}
	}
	
	public static class PersonDetail {
		public String personID;
		public String familyName;
		public String givenName;
		//
		public String birthDate;
		public String marriageDate;
		public String spause;
		public String deathDate;
		//
		public String birthYear;
		public String marriageYear;
		public String deathYear;
		
		PersonDetail(Person person) {
			this.personID   = person.personID;
			this.familyName = person.familyName;
			this.givenName  = person.givenName;
			//
			this.birthDate    = person.birthDate;
			this.marriageDate = new String();
			this.spause       = new String();
			this.deathDate    = new String();
			//
			this.birthYear    = new String();
			this.marriageYear = new String();
			this.deathYear    = new String();
		}
	}
	
	
	private static String normalizeFatherMother(String name) {
		if (name.startsWith("亡")) name = name.substring(1);
		return name;
	}
	
	private static Pattern pat_DATE  = Pattern.compile("(..)(元|[0-9]{1,2})年([1,2]?[0-9])月([1-3]?[0-9])日");
	private static String findDate(String string) {
		Matcher m = pat_DATE.matcher(string);
		if (m.find()) {
			return m.group(0);
		} else {
			return null;
		}
	}
	
	private static int toYear(String string) {
		Matcher m = pat_DATE.matcher(string);
		if (m.matches()) {
			String era = m.group(1);
			String yearString = m.group(2);
			int year = yearString.equals("元") ? 1 : Integer.valueOf(yearString);
			switch(era) {
			case "平成":
				return year + 1988;
			case "昭和":
				return year + 1925;
			case "大正":
				return year + 1911;
			case "明治":
				return year + 1867;
			case "天保":
				return year + 1829;
			case "安政":
				return year + 1853;
			case "万延":
				return year + 1859;
			case "文化":
				return year + 1803;
			case "嘉永":
				return year + 1847;
			case "文政":
				return year + 1817;
			default:
				logger.error("Unpexpeced {}", string);
				throw new UnexpectedException("Unpexpected");
			}
		} else {
			logger.error("Unpexpeced  {}", string);
			throw new UnexpectedException("Unpexpected");
		}
	}

	private static void buildPersonMap(PersonMap personMap, RegisterMap registerMap, PersonEntryMap personEntryMap) {
		List<PersonDetail> details = new ArrayList<>();
		
		for(var personID: personEntryMap.getPersonIDSet()) {
			Person person = personMap.get(personID);
			List<PersonEntry> entryList = personEntryMap.get(personID);
			String familyGivenName = person.familyName+person.givenName;
			
			// sanity check
			{
				Map<String, String> map = new TreeMap<>();
				List<String> detailList = new ArrayList<>();
				for(var e: entryList) {
					String format     = e.format;
					String key        = e.key;
					String value      = e.value;
					if (value == null) continue;
					
					switch(key) {
					case "出生":
						toYear(value);
						break;
					case "前戸主トノ続柄":
						continue;
					case "名前":
						if (value.equals(familyGivenName)) value = person.givenName;
						break;
					case "家族トノ続柄":
						continue;
					case "戸主ト成リタル原因及ヒ年月日":
						continue;
					case "母":
					case "父":
						value = normalizeFatherMother(value);
						break;
					case "父母トノ続柄":
						continue;
					case "続柄":
						if (!value.endsWith("男") && !value.endsWith("女")) continue;
						break;
					case "養母":
						break;
					case "養父":
						break;
					case "養父母トノ続柄":
						break;
					case "記載事項":
						detailList.add(value);
						continue;
					default:
						logger.error("Unknonw key {}", key);
						throw new UnexpectedException("Unpexpected");
					}
					
					if (map.containsKey(key)) {
						String oldValue = map.get(key);
						if (!oldValue.equals(value)) {
							logger.warn("differenct value  {}  {}  {}  {}  {}  {}", personID, e.registerID, person.personID, key, oldValue, value);
						}
					} else {
						map.put(key, value);
					}
				}
				for(var e: detailList) {
					if (e.contains("裁判")) continue;
					if (e.contains("追完")) continue;
					if (e.contains("母ノ氏名及死亡旧戸籍ニ依リ記載")) continue;
					if (e.contains("携帯入籍")) continue;
					if (e.contains("共ニ入籍")) continue;
					if (e.contains("出生")) continue;
					if (e.contains("に従い除籍")) continue;
					if (e.contains("に随い除籍")) continue;
					if (e.contains("とともに除籍")) continue;
					if (e.contains("分家ニ付キ") && e.contains("除籍")) continue;
					if (e.contains("改製により")) continue;
					if (e.contains("と更正")) continue;
					if (e.contains("養子離婚")) continue;
					if (e.contains("？？？？？")) continue;
					if (e.contains("昭和32年法務省令第27号")) continue;
					if (e.contains("夫") && e.contains("死亡")) continue;
					
					String date = findDate(e);
					if (date == null) {
						logger.info("## {}", e);
						continue;
					}
					int year = toYear(date);
					
					String key;
					String value = date;
					if (e.contains("婚姻届出") || e.contains("と婚姻") || e.contains("入籍ス")) {
						// 婚姻
						key = "婚姻";
						if (map.containsKey(key)) {
							String oldValue = map.get(key);
							if (!value.equals(oldValue)) {
								logger.warn("### DIFFERENT {}  {}  {}  {}", personID, key, oldValue, value);
							}
						} else {
//							logger.info("## {}  {}  {}", personID, key, value);
//							logger.info("   {}", e);
							map.put(key, date);
						}
						{
							Pattern p = Pattern.compile("[0-9]日(.+?)ト婚姻届出");
							Matcher m = p.matcher(e);
							if (m.find() && m.group(1).length() < 8) {
								String name = m.group(1);
//								logger.info("$  {}", name);
								map.put("配偶者", name);
								continue;
							}
						}
						{
							Pattern p = Pattern.compile("番地(.+?)ト婚姻届出");
							Matcher m = p.matcher(e);
							if (m.find() && m.group(1).length() < 8) {
								String name = m.group(1);
//								logger.info("$  {}", name);
								map.put("配偶者", name);
								continue;
							}
						}
						{
							Pattern p = Pattern.compile("番戸(.+?)ト婚姻届出");
							Matcher m = p.matcher(e);
							if (m.find() && m.group(1).length() < 8) {
								String name = m.group(1);
//								logger.info("$  {}", name);
								map.put("配偶者", name);
								continue;
							}
						}
						{
							Pattern p = Pattern.compile("[0-9]日(.+?)と婚姻");
							Matcher m = p.matcher(e);
							if (m.find() && m.group(1).length() < 8) {
								String name = m.group(1);
//								logger.info("$  {}", name);
								map.put("配偶者", name);
								continue;
							}
						}
						{
							Pattern p = Pattern.compile("^(.+?)と婚姻");
							Matcher m = p.matcher(e);
							if (m.find() && m.group(1).length() < 8) {
								String name = m.group(1);
//								logger.info("$  {}", name);
								map.put("配偶者", name);
								continue;
							}
						}
						{
							Pattern p = Pattern.compile("^(.+?)ト婚姻届出");
							Matcher m = p.matcher(e);
							if (m.find() && m.group(1).length() < 8) {
								String name = m.group(1);
//								logger.info("$  {}", name);
								map.put("配偶者", name);
								continue;
							}
						}
					} else if (e.contains("養嗣子")) {
						// 養嗣子
					} else if (e.contains("養子")) {
						// 養子
					} else if (e.contains("貰受")) {
						// 養子
					} else if (e.contains("家督相続")) {
						// 家督相続
					} else if (e.contains("相続ス")) {
						// 家督相続
					} else if (e.contains("死亡")) {
						// 死亡
						key = "死亡";
						if (map.containsKey(key)) {
							String oldValue = map.get(key);
							if (!value.equals(oldValue)) {
								logger.warn("### DIFFERENT {}  {}  {}  {}", personID, key, oldValue, value);
							}
						} else {
							map.put(key, date);
						}
					} else if (e.contains("分家")) {
						// 分家
					} else if (e.contains("隠居")) {
						// 隠居
					} else if (e.contains("廃嫡")) {
						// 廃嫡
					} else if (e.contains("嗣子")) {
						// 嗣子
					} else {
						logger.info("$$ {}", e);
					}
				}
				logger.info("person  {}  {}", personID, map);
				
				PersonDetail detail = new PersonDetail(person);
				if (map.containsKey("死亡")) {
					detail.deathDate = map.get("死亡");
				}
				if (map.containsKey("婚姻")) {
					detail.marriageDate = map.get("婚姻");
				}
				if (map.containsKey("配偶者")) {
					detail.spause = map.get("配偶者");
				}
				if (!detail.birthDate.isEmpty())    detail.birthYear    = String.format("%d", toYear(detail.birthDate));
				if (!detail.marriageDate.isEmpty()) detail.marriageYear = String.format("%d", toYear(detail.marriageDate));
				if (!detail.deathDate.isEmpty())    detail.deathYear    = String.format("%d", toYear(detail.deathDate));
				details.add(detail);
			}
		}
		
		for(var e: details) {
			logger.info("{}", String.format("%s,%s,%s,%s,%s,%s,%s,%s", e.familyName+e.givenName, e.birthDate, e.marriageDate, e.spause, e.deathDate, e.birthYear, e.marriageYear, e.deathYear));
		}
		CSVUtil.write(PersonDetail.class).withHeader(true).file("tmp/person-detail.csv", details);
	}
	
	public static void main(String[] args) {
		logger.info("START");

		List<Person>   personList   = new ArrayList<>();
		List<Register> registerList = new ArrayList<>();
		List<Record>   familyList   = new ArrayList<>();

		load(personList, registerList, familyList);
		
		PersonMap   personMap         = new PersonMap(personList);
		RegisterMap registerMap       = new RegisterMap(registerList);
		PersonEntryMap personEntryMap = new PersonEntryMap(familyList);

		check(personMap, registerMap, personEntryMap);
		
		buildPersonMap(personMap, registerMap, personEntryMap);
		
		logger.info("STOP");
		System.exit(0);
	}
}
