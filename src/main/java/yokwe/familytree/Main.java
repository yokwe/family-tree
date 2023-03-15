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

	
	public static void main(String[] args) {
		try {
			logger.info("START");
			
			final List<Person>   personList   = new ArrayList<>();
			final List<Register> registerList = new ArrayList<>();
			final List<Family>   familyList   = new ArrayList<>();
			
			String url = URL_FAMILY_REGISTER;
			logger.info("url        {}", url);
			try (SpreadSheet spreadSheet = new SpreadSheet(url, true)) {
				List<String> sheetNameList = spreadSheet.getSheetNameList();
//				sheetNameList.sort((a, b) -> a.compareTo(b));
				logger.info("sheetName {}", sheetNameList);
				
				// Process Account
				for(String sheetName: sheetNameList) {
					logger.info("sheetName {}", sheetName);
					if (sheetName.equals("戸籍一覧")) {
						var list = Sheet.extractSheet(spreadSheet, Register.class, sheetName);
						registerList.addAll(list);
					} else if (sheetName.equals("人物一覧")) {
						var list = Sheet.extractSheet(spreadSheet, Person.class, sheetName);
						personList.addAll(list);
					} else if (sheetName.startsWith("戸籍-")) {
						var list = Sheet.extractSheet(spreadSheet, Family.class, sheetName);
						familyList.addAll(list);
//						logger.info("family {} {}", sheetName, list.size());
					} else {
						logger.info("Unknown sheetName {}", sheetName);
						throw new UnexpectedException("Unexpected");
					}
				}
				logger.info("reading spreadsheet is finished");
				
				logger.info("register {}", registerList.size());
				logger.info("person   {}", personList.size());
				logger.info("family   {}", familyList.size());
				
				int countError = 0;
				Map<String, Person>   personMap = new TreeMap<>();
				for(var e: personList) {
//					logger.info("person {}", StringUtil.toString(e));
					if (personMap.containsKey(e.perrsonID)) {
						logger.error("Duplicate person id {}", e.perrsonID);
						countError++;
					} else {
						personMap.put(e.perrsonID, e);
					}
				}
				Map<String, Register> registerMap = new TreeMap<>();
				for(var e: registerList) {
//					logger.info("register {}", StringUtil.toString(e));
					if (registerMap.containsKey(e.registerID)) {
						logger.error("Duplicate register id {}", e.registerID);
						countError++;
					} else {
						registerMap.put(e.registerID, e);
					}
				}
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
				logger.info("STOP");
			}
		} catch (Exception e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
		} finally {
			System.exit(0);
		}
	}
}
