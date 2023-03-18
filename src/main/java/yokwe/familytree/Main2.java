package yokwe.familytree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		List<Register> registerList = new ArrayList<>();
		List<Person>   personList   = new ArrayList<>();
		List<Record>   recordList   = new ArrayList<>();

		FamilyRegister.readSheet(URL_FAMILY_REGISTER, registerList, personList, recordList);
		
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
					
					List<Detail> details = Detail.M19.toDetailList(pairList.find("記載事項"));
					// FIXME 本籍
					for(var e: details) {
//						logger.info("DETAIL {}", e);
					}
					// FIXME
				}
				
			}
		}
		
		logger.info("STOP");
		System.exit(0);
	}
}
