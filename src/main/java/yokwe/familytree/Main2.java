package yokwe.familytree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import yokwe.familytree.Detail.Type;
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
	
	
	public static class Family {
		String id;     // regiterID
		String format;

		List<Pair>              head = new ArrayList<>();
		Map<String, List<Pair>> body = new TreeMap<>();
		//  personID
		
		Family(String id) {
			this.id = id;
		}
		
		void init() {
			format = find(head, "形式");
		}
		
		List<Pair> get(String personID) {
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
			return find(head, key);
		}
		String find(String personID, String key) {
			return find(get(personID), key);
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
				List<Pair> list;
				if (family.body.containsKey(personID)) {
					list = family.body.get(personID);
				} else {
					list = new ArrayList<>();
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
		}
		
		
		logger.info("STOP");
		System.exit(0);
	}
}
