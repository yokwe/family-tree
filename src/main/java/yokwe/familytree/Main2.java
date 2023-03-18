package yokwe.familytree;

import java.util.ArrayList;
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

public class Main2 {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static String URL_FAMILY_REGISTER = StringUtil.toURLString("tmp/family-register.ods");
	
	private static class DetailSetMap {
		Map<String, Set<Detail>> map = new TreeMap<>();
		//  personID

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
		
		FamilyMap familyMap = FamilyMap.getInstance(recordList);
		logger.info("map {}", familyMap.size());
		
		for(var family: familyMap.values()) {
			var domicile = family.find("本籍地");
			var format   = family.find("形式");

			for(var pairList: family.values()) {
				for(var string: pairList.find("記載事項")) {
					// convert string to Detail
					
				}
			}
		}
		
		
//		for(var family: familyMap.values()) {
//			logger.info("family  {}  {}", family.id, family.getFormat());
//			if (family.getFormat().equals(FamilyRecord.M19.FORMAT)) {
//				// M19
//				String domicile     = family.find("本籍地");
//				String previousHead = family.find("前戸主");
//				
//				// member of family
//				for(var personID: family.keySet()) {					
//					FamilyMap.PairList pairList = family.get(personID);
//					
//					String relation = pairList.findFirst("続柄");
//					String name = pairList.findFirst("名前");
//					String relashionToFamily = pairList.findFirst("家族トノ続柄");
//					String birthdate = pairList.findFirst("出生");
//					
////					logger.info("person {}", personID, name);
//					List<Detail> details = Detail.M19.toDetailList(pairList.find("記載事項"));
//					// FIXME date of BIRTH
//					
//					// sanity check
//					{
//						Person person = personMap.get(personID);
//						JapaneseDate dateB = JapaneseDate.getInstance(person.birthDate);
//						JapaneseDate dateM = JapaneseDate.getInstance(person.marriageDate);
//						JapaneseDate dateD = JapaneseDate.getInstance(person.deathDate);
//						
//						// BIRTH
//						{
//							JapaneseDate date = JapaneseDate.getInstance(birthdate);
//							if (date.isDefined()) {
//								if (!dateB.equals(date)) {
//									logger.info("__ BIRTH  AA  {}  {}  {}", personID, dateB, date);
//								}
//								for(var e: details) {
//									if (e.type == Detail.Type.BIRTH && e.date.isDefined()) {
//										if (!dateB.equals(e.date)) {
//											logger.info("__ BIRTH  BB  {}  {}  {}", personID, dateM, e.date);
//										}
//									}
//								}
//							}
//						}
//						
//						// MARRIAGE
//						{
//							for(var e: details) {
//								if (e.type == Detail.Type.MARRIAGE) {
//									if (dateM.isDefined()) {
//										if (!dateM.equals(e.date)) {
//										}
//									} else {
//										logger.info("__ MARRAGIE  AA  {}  {}  {}", personID, dateM, e.date);
//									}
//								}
//							}
//						}
//						
//						// DEATH
//						{
//							for(var e: details) {
//								if (e.type == Detail.Type.DEATH) {
//									if (dateD.isDefined()) {
//										if (!dateD.equals(e.date)) {
//											logger.info("__ DEATH  AA  {}  {}  {}", personID, dateD, e.date);
//										}
//									} else {
//										logger.info("__ DEATH  AA  {}  {}  {}", personID, dateD, e.date);
//									}
//								}
//							}
//						}
//						
////						detailSetMap.add(personID, Detail.birth(date));
//					}
//					
//					detailSetMap.add(personID, details);
////					if (!details.isEmpty()) logger.info("  detail {}", details);
//					// FIXME 本籍
//					// FIXME
//					
//				}
//			}
//			
////			if (family.getFormat().equals(FamilyRecord.M31.FORMAT)) {
////				// M31
////				String domicile     = family.find("本籍地");
////				String previousHead = family.find("前戸主");
////				
////				// member of family
////				for(var personID: family.keySet()) {					
////					FamilyMap.PairList pairList = family.get(personID);
////					
////					String relation = pairList.findFirst("続柄");
////					String name = pairList.findFirst("名前");
////					String relashionToFamily = pairList.findFirst("家族トノ続柄");
////					String birthdate = pairList.findFirst("出生");
////					
//////					logger.info("person {}", personID, name);
////					List<Detail> details = Detail.M19.toDetailList(pairList.find("記載事項"));
////				}
////			}
//		}
		for(var personID: detailSetMap.keySet()) {
			var set = detailSetMap.get(personID);
			if (!set.isEmpty()) logger.info("DETAIL  {}  {}", personID, set);
		}
		
		logger.info("STOP");
		System.exit(0);
	}
}
