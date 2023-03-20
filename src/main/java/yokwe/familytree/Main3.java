package yokwe.familytree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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


class LifeEventSetMap {
	Map<String, Set<LifeEvent>> map = new TreeMap<>();
	//  personID

	private Set<LifeEvent> getSet(String personID) {
		Set <LifeEvent> set;
		if (map.containsKey(personID)) {
			set = map.get(personID);
		} else {
			set = new TreeSet<>();
			map.put(personID, set);
		}
		return set;
	}
	public void add(String personID, LifeEvent LifeEvent) {
		Set <LifeEvent> set = getSet(personID);
		set.add(LifeEvent);
	}
	public void add(String personID, List<LifeEvent> list) {
		Set <LifeEvent> set = getSet(personID);
		set.addAll(list);
	}
	
	boolean containsKey(String personID) {
		return map.containsKey(personID);
	}
	Set<String> keySet() {
		return map.keySet();
	}
	Set<LifeEvent>	get(String personID) {
		return map.get(personID);
	}
	Collection<Set<LifeEvent>> values() {
		return map.values();
	}
	Set<Map.Entry<String, Set<LifeEvent>>> entrySet() {
		return map.entrySet();
	}
}

public class Main3 {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static String URL_FAMILY_REGISTER = StringUtil.toURLString("tmp/family-register.ods");
	
	

	public static void main(String[] args) {
		logger.info("START");
		
		LifeEvent.Converter converter = new LifeEvent.Converter();
		LifeEvent.setVerboseString(true);
		
		List<Register> registerList = new ArrayList<>();
		List<Person>   personList   = new ArrayList<>();
		List<Record>   recordList   = new ArrayList<>();

		FamilyRegister.readSheet(URL_FAMILY_REGISTER, registerList, personList, recordList);
		
		Map<String, Register> registerMap = registerList.stream().collect(Collectors.toMap(o -> o.registerID, o -> o));
		Map<String, Person>   personMap   = personList.stream().collect(Collectors.toMap(o -> o.personID, o -> o));
		
		FamilyMap familyMap = FamilyMap.getInstance(recordList);
		logger.info("map {}", familyMap.size());
		
		LifeEventSetMap lifeEventSetMap = new LifeEventSetMap();
		
		for(var entry: familyMap.entrySet()) {
			var registerID = entry.getKey();
			var family     = entry.getValue();
			var register   = registerMap.get(registerID);
			
			var domicile = family.find("本籍地");
			var format   = family.find("形式");
			
			logger.info("## {}", registerID);
			if (!register.domicile.equals(domicile)) {
				logger.error("domicile  {}!", register.domicile);
				logger.error("domicile  {}!", domicile);
				logger.error("head {}", family.head);
				throw new UnexpectedException("Unexpected");
			}
			if (!register.format.equals(format)) {
				logger.error("format  {}  {}", register.format, format);
				throw new UnexpectedException("Unexpected");
			}

			for(var entry2: family.entrySet()) {
				var personID = entry2.getKey();
				var member   = entry2.getValue();
				var person   = personMap.get(personID);
				
				logger.info("## {}", personID);
				for(var string: member.find("記載事項")) {
					// convert string to LifeEvent
					LifeEvent event = converter.toLifeEvent(string);
					if (event != null) lifeEventSetMap.add(personID, event);
				}
			}
		}
		
		{
			record Entry (String personID, LifeEvent event) {};
			List<Entry> list = new ArrayList<>();
			for(var e: lifeEventSetMap.entrySet()) {
				String         personID = e.getKey();
				Set<LifeEvent> set      = e.getValue();
				for(var ee: set) {
					Entry entry = new Entry(personID, ee);
					list.add(entry);
				}
			}
			Collections.sort(list, (a, b) -> {
				int ret = 0;
				if (ret == 0) ret = a.event.type.compareTo(b.event.type);
				if (ret == 0) ret = a.personID.compareTo(b.personID);
				if (ret == 0 && a.event.date == null && b.event.date != null) ret = -1;
				if (ret == 0 && a.event.date != null && b.event.date == null) ret = 1;
				if (ret == 0 && a.event.date != null && b.event.date != null) ret = a.event.date.compareTo(b.event.date);
				return ret;
			});
			for(var e: list) {
				String type = e.event.type.toString();
				String date = e.event.date == null ? "****" : e.event.date.toString();
				String value = e.event.value == null ? "****" : e.event.value;
				
				logger.info("## event  {}", String.format("%s  %s  %s  %s  %s",
					type,
					StringUtil.padRightSpace(e.personID, 14),
					StringUtil.padRightSpace(date, 16),
					StringUtil.padRightSpace(value, 30),
					e.event.string));
			}
		}
		
		logger.info("STOP");
		System.exit(0);
	}
}