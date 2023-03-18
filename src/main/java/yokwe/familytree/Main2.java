package yokwe.familytree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import yokwe.familytree.FamilyRegister.Person;
import yokwe.familytree.FamilyRegister.Record;
import yokwe.familytree.FamilyRegister.Register;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class Main2 {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static String URL_FAMILY_REGISTER = StringUtil.toURLString("tmp/family-register.ods");

	
	public static class Pair {
		final String key;
		final String value;
		public Pair(String key, String value) {
			this.key   = key;
			this.value = value;
		}
	}
	
	public static class PairList {
		private List<Pair> list = new ArrayList<>();
		
		String findFirst (String key) {
			for(var e: list) {
				if (e.key.equals(key)) return e.value;
			}
			return null;
		}
		
		List<String> find(String key) {
			List<String> result = new ArrayList<>();
			for(var e: list) {
				if (e.key.equals(key)) result.add(e.value);
			}
			return result;
		}
		
		void add(Pair pair) {
			list.add(pair);
		}
	}
	
	
	public static class Family {
		String id;     // regiterID
		String format;

		PairList              head = new PairList();
		Map<String, PairList> body = new TreeMap<>();
		//  personID
		
		Family(String id) {
			this.id = id;
		}
		
		void init() {
			format = head.findFirst("形式");
		}
		
		Set<String> keySet() {
			return body.keySet();
		}
		PairList get(String personID) {
			if (body.containsKey(personID)) {
				return body.get(personID);
			} else {
				throw new UnexpectedException("Unpexpected");
			}
		}
		
		static String find(List<Pair> list, String key) {
			for(var e: list) {
				if (e.key.equals(key)) return e.value;
			}
			logger.error("Unknown key");
			logger.error("  key {}!", key);
			throw new UnexpectedException("Unpexpected");
		}
		String find(String key) {
			return head.findFirst(key);
		}
		String find(String key, String defaultValue) {
			String result = head.findFirst(key);
			return (result == null) ? defaultValue : result;
		}
	}
	
	public static class FamilyMap {
		private Map<String, Family> map = new TreeMap<>();
		//  registerID
		
		public Family get(String key) {
			return map.get(key);
		}
		public boolean containsKey(String key) {
			return map.containsKey(key);
		}
		public void put(String key, Family value) {
			map.put(key, value);
		}
		public Collection<Family> values() {
			return map.values();
		}
		public int size() {
			return map.size();
		}
	}
	
	private static FamilyMap buildFamilyMap(List<Record> recordList) {
		FamilyMap result = new FamilyMap();
		
		for(var record: recordList) {
			var registerID = record.registerID;
			
			// find family
			Family family;
			if (result.containsKey(registerID)) {
				family = result.get(registerID);
			} else {
				family = new Family(registerID);
				result.put(registerID, family);
			}
			
			// add pair to family
			var personID = record.personID;
			var pair     = new Pair(record.type, record.detail);
			if (record.personID == null) {
				family.head.add(pair);
			} else {
				PairList list;
				if (family.body.containsKey(personID)) {
					list = family.body.get(personID);
				} else {
					list = new PairList();
					family.body.put(personID, list);
				}
				list.add(pair);
			}
		}
		
		for(var e: result.values()) {
			e.init();
		}
		
		return result;
	}
	
	
	private static class DetailSetMap {
		Map<String, Set<Detail>> map = new TreeMap<>();
		//  personIDD

		private Set<Detail> getSet(String personID) {
			Set <Detail> set;
			if (map.containsKey(personID)) {
				set = map.get(personID);
			} else {
				set = new TreeSet<>();
				map.put(personID, set);
			}
			return set;
		}
		public void add(String personID, Detail detail) {
			Set <Detail> set = getSet(personID);
			set.add(detail);
		}
		public void add(String personID, List<Detail> list) {
			Set <Detail> set = getSet(personID);
			set.addAll(list);
		}
		
		boolean containsKey(String personID) {
			return map.containsKey(personID);
		}
		Set<String> keySet() {
			return map.keySet();
		}
		Set<Detail>	get(String personID) {
			return map.get(personID);
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		List<Register> registerList = new ArrayList<>();
		List<Person>   personList   = new ArrayList<>();
		List<Record>   recordList   = new ArrayList<>();

		FamilyRegister.readSheet(URL_FAMILY_REGISTER, registerList, personList, recordList);
		
		Map<String, Person> personMap = personList.stream().collect(Collectors.toMap(o -> o.personID, o -> o));
		
		DetailSetMap detailSetMap = new DetailSetMap();
		
		FamilyMap familyMap = buildFamilyMap(recordList);
		logger.info("map {}", familyMap.size());
		for(var family: familyMap.values()) {
			logger.info("family  {}  {}", family.id, family.format);
			if (family.format.equals(FamilyRecord.M19.FORMAT)) {
				// M19
				String domicile = family.find("本籍地");
				String previousHead= family.find("前戸主");
				
				// member of family
				for(var personID: family.keySet()) {					
					PairList pairList = family.get(personID);
					
					String relation = pairList.findFirst("続柄");
					String name = pairList.findFirst("名前");
					String relashionToFamily = pairList.findFirst("家族トノ続柄");
					String birthdate = pairList.findFirst("出生");
					
					
//					logger.info("person {}", personID, name);
					List<Detail> details = Detail.M19.toDetailList(pairList.find("記載事項"));
					// FIXME date of BIRTH
					
					// sanity check
					{
						Person person = personMap.get(personID);
						JapaneseDate dateB = JapaneseDate.getInstance(person.birthDate);
						JapaneseDate dateM = JapaneseDate.getInstance(person.marriageDate);
						JapaneseDate dateD = JapaneseDate.getInstance(person.deathDate);
						
						// BIRTH
						{
							JapaneseDate date = JapaneseDate.getInstance(birthdate);
							if (date.isDefined()) {
								if (!dateB.equals(date)) {
									logger.info("__ BIRTH  AA  {}  {}  {}", personID, dateB, date);
								}
								for(var e: details) {
									if (e.type == Detail.Type.BIRTH && e.date.isDefined()) {
										if (!dateB.equals(e.date)) {
											logger.info("__ BIRTH  BB  {}  {}  {}", personID, dateM, e.date);
										}
									}
								}
							}
						}
						
						// MARRIAGE
						{
							for(var e: details) {
								if (e.type == Detail.Type.MARRIAGE) {
									if (dateM.isDefined()) {
										if (!dateM.equals(e.date)) {
										}
									} else {
										logger.info("__ MARRAGIE  AA  {}  {}  {}", personID, dateM, e.date);
									}
								}
							}
						}
						
						// DEATH
						{
							for(var e: details) {
								if (e.type == Detail.Type.DEATH) {
									if (dateD.isDefined()) {
										if (!dateD.equals(e.date)) {
											logger.info("__ DEATH  AA  {}  {}  {}", personID, dateD, e.date);
										}
									} else {
										logger.info("__ DEATH  AA  {}  {}  {}", personID, dateD, e.date);
									}
								}
							}
						}
						
//						detailSetMap.add(personID, Detail.birth(date));
					}
					
					detailSetMap.add(personID, details);
//					if (!details.isEmpty()) logger.info("  detail {}", details);
					// FIXME 本籍
					// FIXME
					
				}
				
			}
		}
		for(var personID: detailSetMap.keySet()) {
			var set = detailSetMap.get(personID);
			if (!set.isEmpty()) logger.info("DETAIL  {}  {}", personID, set);
		}
		
		logger.info("STOP");
		System.exit(0);
	}
}
