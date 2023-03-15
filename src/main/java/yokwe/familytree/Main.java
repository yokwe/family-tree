package yokwe.familytree;

import java.util.List;

import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.libreoffice.SpreadSheet;

public class Main {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static String URL_FAMILY_REGISTER = StringUtil.toURLString("tmp/family-register.ods");

	public static void main(String[] args) {
		try {
			logger.info("START");
			
			String url = URL_FAMILY_REGISTER;
			logger.info("url        {}", url);
			try (SpreadSheet spreadSheet = new SpreadSheet(url, true)) {
				List<String> sheetNameList = spreadSheet.getSheetNameList();
				sheetNameList.sort((a, b) -> a.compareTo(b));
				
				// Process Account
				for(String sheetName: sheetNameList) {
					logger.info("sheetName {}", sheetName);
				}
			}
			logger.info("STOP");
		} catch (Exception e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
		} finally {
			System.exit(0);
		}
	}
}
