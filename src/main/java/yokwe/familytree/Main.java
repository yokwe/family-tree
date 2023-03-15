package yokwe.familytree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import yokwe.familytree.FamilyRegister.Family;
import yokwe.familytree.FamilyRegister.Person;
import yokwe.familytree.FamilyRegister.Register;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;;

public class Main {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static String URL_FAMILY_REGISTER = StringUtil.toURLString("tmp/family-register.ods");

	private static void load(List<Person> personList, List<Register> registerList, List<Family> familyList) {
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
				var list = Sheet.extractSheet(spreadSheet, Family.class, sheetName);
				familyList.addAll(list);
			} else {
				logger.info("Unknown sheetName {}", sheetName);
				throw new UnexpectedException("Unexpected");
			}
		}
	}
	
	private static void check(List<Person> personList, List<Register> registerList, List<Family> familyList) {
		int countError = 0;
		Map<String, Person>   personMap = new TreeMap<>();
		// check personID duplicate in personList
		for(var e: personList) {
			if (personMap.containsKey(e.perrsonID)) {
				logger.error("Duplicate person id {}", e.perrsonID);
				countError++;
			} else {
				personMap.put(e.perrsonID, e);
			}
		}
		Map<String, Register> registerMap = new TreeMap<>();
		// check registerID duplicate in regeiserList
		for(var e: registerList) {
			if (registerMap.containsKey(e.registerID)) {
				logger.error("Duplicate register id {}", e.registerID);
				countError++;
			} else {
				registerMap.put(e.registerID, e);
			}
		}
		// check personID with personID in familyList
		{
			Map<String, Integer> countMap = new TreeMap<>();
			for(var e: personMap.keySet()) {
				countMap.put(e, 0);
			}
			Set<String> unknownSet = new TreeSet<>();
			for(var e: familyList) {
				String key = e.personID;
				if (key != null) {
					if (!personMap.containsKey(key)) {
						unknownSet.add(key);
					}
					countMap.put(key, 1);
				}
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
		// check personID with personID in familyList
		{
			Map<String, Integer> countMap = new TreeMap<>();
			for(var e: registerMap.keySet()) {
				countMap.put(e, 0);
			}
			Set<String> unknownSet = new TreeSet<>();
			for(var e: familyList) {
				String key = e.registerID;
				if (key != null) {
					if (!registerMap.containsKey(key)) {
						unknownSet.add(key);
					}
					countMap.put(key, 1);
				}
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
	
	public static void main(String[] args) {
		logger.info("START");

		List<Person>   personList   = new ArrayList<>();
		List<Register> registerList = new ArrayList<>();
		List<Family>   familyList   = new ArrayList<>();

		load(personList, registerList, familyList);
		check(personList, registerList, familyList);
		
		logger.info("STOP");
		System.exit(0);
	}
}
