package yokwe.familytree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import yokwe.familytree.FamilyRegister.Record;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class FamilyMap {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static record Pair(String key, String value) {}
	
	public static class PairList implements Iterable<Pair> {
		private final List<Pair> list = new ArrayList<>();
		
		public Iterator<Pair> iterator() {
			return list.iterator();
		}
		
		public String findFirst (String key) {
			for(var e: list) {
				if (e.key.equals(key)) return e.value;
			}
			return null;
		}
		
		public List<String> find(String key) {
			List<String> result = new ArrayList<>();
			for(var e: list) {
				if (e.key.equals(key)) result.add(e.value);
			}
			return result;
		}
		
		@Override
		public String toString() {
			return list.toString();
		}
	}

	public static class Family {
		public final String id; // regiterID

		public final PairList              head   = new PairList();
		public final Map<String, PairList> member = new TreeMap<>();
		//                personID
		
		public Set<String> keySet() {
			return member.keySet();
		}
		public Collection<PairList> values() {
			return member.values();
		}
		public PairList get(String key) {
			return member.get(key);
		}
		public Set<Map.Entry<String, PairList>> entrySet() {
			return member.entrySet();
		}
		
		
		public Family(String id) {
			this.id = id;
		}
		
		public String find(String key) {
			return head.findFirst(key);
		}
		public String find(String key, String defaultValue) {
			String result = head.findFirst(key);
			return (result == null) ? defaultValue : result;
		}
		
		public String getFormat() {
			return find("形式");
		}
	}
	
	
	public static FamilyMap getInstance(List<Record> recordList) {
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
				family.head.list.add(pair);
			} else {
				// add pair to list
				PairList list;
				if (family.member.containsKey(personID)) {
					list = family.member.get(personID);
				} else {
					list = new PairList();
					family.member.put(personID, list);
				}
				list.list.add(pair);
			}
		}
		
		return result;
	}

	
	//
	// FamilyMap
	//

	private Map<String, Family> map = new TreeMap<>();
	//          registerID
	
	public Family get(String key) {
		return map.get(key);
	}
	public void put(String key, Family value) {
		map.put(key, value);
	}
	public boolean containsKey(String key) {
		return map.containsKey(key);
	}
	public Collection<Family> values() {
		return map.values();
	}
	public Set<Map.Entry<String, Family>> entrySet() {
		return map.entrySet();
	}
	public int size() {
		return map.size();
	}
}

