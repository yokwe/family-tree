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
import yokwe.util.CSVUtil;
import yokwe.util.JapaneseDate;
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


class MyPerson {
	static Map<String, MyPerson> map = new TreeMap<>();
	//         id
	
	static MyPerson getInstance(String id) {
		if (map.containsKey(id)) {
			return map.get(id);
		} else {
			MyPerson myPerson = new MyPerson(id);
			map.put(id, myPerson);
			return myPerson;
		}
	}
	
	String          id;
	String          familyName;
	String          givenName;
	String          gender; // M or F
	String          relation; // relation to parent
	String          father;
	String          mother;
	String          spouse;
	JapaneseDate    birth;
	JapaneseDate    marriage;
	JapaneseDate    death;
	String          yearBirth;
	String          yearMarriage;
	String          yearDeath;
	
//	List<LifeEvent> list;
	
	MyPerson(String id_) {
		id = id_;
		familyName   = "";
		givenName    = "";
		gender       = "";
		relation     = "";
		father       = "";
		mother       = "";
		spouse       = "";
		birth        = JapaneseDate.UNDEFIEND;
		marriage     = JapaneseDate.UNDEFIEND;
		death        = JapaneseDate.UNDEFIEND;
		yearBirth    = "";
		yearMarriage = "";
		yearDeath    = "";
//		list         = new ArrayList<>();
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
}

public class Main {
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
				
		for(var familyEntry: familyMap.entrySet()) {
			var registerID = familyEntry.getKey();
			var family     = familyEntry.getValue();
			var register   = registerMap.get(registerID);
			
			var domicile = family.findFIrst(FamilyRegister.DOMICILE);
			var format   = family.findFIrst(FamilyRegister.FORMAT);
			
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

			for(var personEntry: family.entrySet()) {
				var personID = personEntry.getKey();
				var member   = personEntry.getValue();
				var person   = personMap.get(personID);
				
				logger.info("## {}", personID);
				
				MyPerson myPerson = MyPerson.getInstance(personID);
				{
					if (myPerson.givenName.isEmpty()) {
						var value = member.findFirst(FamilyRegister.NAME);
						if (value != null && !value.isEmpty()) myPerson.givenName = value;
					}
					if (myPerson.father.isEmpty()) {
						var value = member.findFirst(FamilyRegister.FATHER, "").replace("亡", "");
						if (value != null && !value.isEmpty()) myPerson.father = value;
					}
					if (myPerson.mother.isEmpty()) {
						var value = member.findFirst(FamilyRegister.MOTHER, "").replace("亡", "");
						if (value != null && !value.isEmpty()) myPerson.mother = value;
					}
					if (!myPerson.birth.isDefined()) {
						var value = member.findFirst(FamilyRegister.BIRTH, "");
						if (value != null && !value.isEmpty()) {
							myPerson.birth = JapaneseDate.getInstance(value);
							myPerson.yearBirth = String.valueOf(myPerson.birth.year);
						}
					}
					if (myPerson.relation.isEmpty()) {
						var relation = member.findFirst(FamilyRegister.RELATION_TO_PARENT);
						if (relation != null && !relation.equals("-")) {
							if (myPerson.relation.isEmpty()) myPerson.relation = relation;
						}
					}
					if (myPerson.relation.isEmpty()) {
						var relation = member.findFirst(FamilyRegister.RELATION);
						if (relation != null && !relation.equals("-") && relation.length() == 2 && (relation.endsWith("男") || relation.endsWith("女"))) {
							if (myPerson.relation.isEmpty()) myPerson.relation = relation;
						}
					}
					if (myPerson.relation.isEmpty()) {
						var relation = member.findFirst(FamilyRegister.RELATION_TO_FAMILY);
						if (relation != null && !relation.equals("-") && (relation.endsWith("男") || relation.endsWith("女"))) {
							if (myPerson.relation.isEmpty()) myPerson.relation = relation.substring(relation.length() - 2);
						}
					}
					if (myPerson.gender.isEmpty()) {
						var relation = member.findFirst(FamilyRegister.RELATION);
						if (relation != null && !relation.equals("-")) {
							if (relation.endsWith("男")) myPerson.gender = "男";
							if (relation.endsWith("女")) myPerson.gender = "女";
							if (relation.equals("妻"))   myPerson.gender = "女";
							if (relation.equals("母"))   myPerson.gender = "女";
						}
					}
					if (myPerson.gender.isEmpty()) {
						var relation = member.findFirst(FamilyRegister.RELATION_TO_FAMILY);
						if (relation != null && !relation.equals("-")) {
							if (relation.endsWith("男")) myPerson.gender = "男";
							if (relation.endsWith("女")) myPerson.gender = "女";
							if (relation.equals("妻"))   myPerson.gender = "女";
							if (relation.equals("母"))   myPerson.gender = "女";
						}
					}
					if (myPerson.gender.isEmpty()) {
						var relation = member.findFirst(FamilyRegister.RELATION_TO_PARENT);
						if (relation != null && !relation.equals("-")) {
							if (relation.endsWith("男")) myPerson.gender = "男";
							if (relation.endsWith("女")) myPerson.gender = "女";
							if (relation.equals("妻"))   myPerson.gender = "女";
							if (relation.equals("母"))   myPerson.gender = "女";
						}
					}
					if (myPerson.father.isEmpty()) {
						var relation = member.findFirst(FamilyRegister.RELATION);
						if (relation != null && !relation.equals("-") && relation.length() == 2 && (relation.endsWith("男") || relation.endsWith("女"))) {
							var father = family.findMemberByRelation(FamilyRegister.HEAD);
							if (father != null) {
								var name = father.findFirst(FamilyRegister.NAME);
								if (name != null) myPerson.father = name.replace("亡", "");
							}
						}
					}
					if (myPerson.father.isEmpty()) {
						var relation = member.findFirst(FamilyRegister.RELATION);
						if (relation != null && relation.equals(FamilyRegister.HEAD)) {
							var exHead = family.findFIrst(FamilyRegister.EX_HEAD);
							if (exHead != null && exHead.startsWith("亡父")) {
								myPerson.father = exHead.replace("亡父", "");
							}
						}
					}
					if (myPerson.mother.isEmpty()) {
						var relation = member.findFirst(FamilyRegister.RELATION);
						if (relation != null && !relation.equals("-") && relation.length() == 2 && (relation.endsWith("男") || relation.endsWith("女"))) {
							var mother = family.findMemberByRelation(FamilyRegister.WIFE);
							if (mother != null) {
								var name = mother.findFirst(FamilyRegister.NAME);
								if (name != null && !name.equals("-")) myPerson.mother = name.replace("亡", "");
							}
						}
					}
					if (myPerson.mother.isEmpty()) {
						var relation = member.findFirst(FamilyRegister.RELATION);
						if (relation != null && relation.equals(FamilyRegister.HEAD)) {
							var mother = family.findMemberByRelation(FamilyRegister.MOTHER);
							if (mother != null) {
								var name = mother.findFirst(FamilyRegister.NAME);
								if (name != null && !name.equals("-")) myPerson.mother = name.replace("亡", "");
							}
						}
					}
					if (myPerson.spouse.isEmpty()) {
						var relation = member.findFirst(FamilyRegister.RELATION_TO_FAMILY);
						if (relation != null && relation.startsWith("父") && relation.endsWith("妻")) {
							myPerson.spouse = relation.substring(1, relation.length() - 1);
						}
					}
				}
				
				for(var string: member.find(FamilyRegister.DESCRIBED_ITEM)) {
					// convert string to LifeEvent
					LifeEvent event = converter.toLifeEvent(string);
					if (event != null) {
						// Replace 本籍 with domicile
						if (event.value != null && event.value.equals(FamilyRegister.DOMICILE)) {
							LifeEvent newEvent = new LifeEvent(event, domicile);
							event = newEvent;
						}
						lifeEventSetMap.add(personID, event);
						
						if (event.type == LifeEvent.Type.DEATH) {
							myPerson.death     = event.date;
							myPerson.yearDeath = String.valueOf(myPerson.death.year);
						}
						if (event.type == LifeEvent.Type.MARRIAGE) {
							myPerson.marriage     = event.date;
							myPerson.yearMarriage = String.valueOf(myPerson.marriage.year);

							if (event.value != null) myPerson.spouse = event.value;
						}
					}
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
		
		{
			CSVUtil.write(MyPerson.class).file("tmp/MyPerson.csv", MyPerson.map.values());
			for(var e: MyPerson.map.values()) {
//				if (!e.father.isEmpty()) continue;
				logger.info("MyPerson {}", e.toString());
			}
		}
		
		logger.info("STOP");
		System.exit(0);
	}
}