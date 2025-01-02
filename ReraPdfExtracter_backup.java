package proppy.tool.rera;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.support.ui.Select;

import proppy.tool.utils.CommonUtils;
import proppy.tool.utils.StringUtil;
import proppy.tool.utils.ConfigReader;
import proppy.tool.utils.PartialFileNameChecker;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

class ReraPdfExtracter_backup {

	public static void main(String[] args) throws AWTException, IOException {
		/**
		 * view source is the input or read all list from UI. Decide what is the input ?
		 * --Done TBD -Giri Store all registration numbers in a variable to iterate
		 * Daily on -- Done batch create new date folder Iterate with registration
		 * number Download the -- Done created with the format given file in date folder
		 * with registration number (need to use regular expression) -- NA File name
		 * with project name and timestamp : Done to retry all the failed pdf download
		 * Get the download current folder path and ---TBD save the files -
		 * Done(Working) Set the chrome drivers to same folder path Download the
		 * complaints file - Done (working) project and builder - Checking Set the list
		 * of District and Taluk --Image search with Gmap&Googleimage --Done Put or
		 * download the pdf files in respective folders
		 **/

		// Specific to Chrome browser Todo: Move to reusable
		System.setProperty("webdriver.chrome.driver", "chrome\\chromedriver.exe");
		ChromeOptions options = new ChromeOptions();
		Map<String, Object> prefs = new HashMap<String, Object>();
		prefs.put("download.default_directory", "chrome");
		options.setExperimentalOption("prefs", prefs);
		WebDriver driver = new ChromeDriver(options);
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

		// Todo: All initialization
		Robot robot = new Robot();
		WebElement uiOption;
		String actualTitle = "";
		// To: environment configuration
		String baseUrl = "https://rera.karnataka.gov.in/home?language=en";
		// Todo: Taluk/Bangalore ? Case statement mapping

		//Variable of pdf and complaint file if exist then don't download new on "NO"
		//Variable of pdf and complaint file if exist then also download new on "YES"
		String pdfFileNewDownload = ConfigReader.readConfiguration("pdf.new");
        String complaintFileNewDownload = ConfigReader.readConfiguration("complaint.new");
        System.out.println("pdfFileNewDownload: " + pdfFileNewDownload);
        System.out.println("complaintFileNewDownload: " + complaintFileNewDownload);

        
		driver.get(baseUrl);
		driver.manage().window().maximize();
		// Getting the value of the title
		actualTitle = driver.getTitle();
		System.out.println("Title: " + actualTitle);

		/*** Level-1: Based on registration number get the details ***/ 
		uiOption = driver.findElement(By.linkText("Services"));
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].click();", uiOption);
		uiOption = driver.findElement(By.linkText("Project Status"));
		js.executeScript("arguments[0].click();", uiOption);
		waiting(5000);

		/*** 
		 * Get the registration number list from the input file-> ViewSourceFile.txt
		 * (Need to keep update)
		 ***/
		List<String> regNumList1 = regNumList();
		// For testing purpose
		// List<String> regNumList =
		// Arrays.asList("PRM/KA/RERA/1251/446/PR/300924/007105","PRM/KA/RERA/1251/308/PR/111124/007220","PRM/KA/RERA/1250/307/PR/071124/007208");

		for (int ri = 0; ri < regNumList1.size(); ri++) {

			System.out.println("Iteration for : --------------------->" + regNumList1.get(ri));

			// driver.findElement(By.id("regNo2")).sendKeys("PRM/KA/RERA/1251/446/PR/300924/007105");
			driver.findElement(By.id("regNo2")).click();
			waiting(1000);
			driver.findElement(By.id("regNo2")).clear();
			waiting(1000);
			driver.findElement(By.id("regNo2")).sendKeys(regNumList1.get(ri));
			waiting(2000);
			driver.findElement(By.xpath("//input[@type='submit']")).click();
			waiting(5000);
			((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
			// waiting(3000);

	
			/*** Level-2
			 * Get the details from UI search if pdfStatus length is zero then it has no pdf
			 * file
			 ***/
			System.out.println("Level-2: Get the details from UI-----");
			String projStatus = driver.findElement(By.xpath("//tr/td[7]")).getText();
			String pdfStatus = driver.findElement(By.xpath("//tr/td[4]")).getText();
			String projectName = driver.findElement(By.xpath("//tr/td[6]")).getText().trim();
			String districtName = driver.findElement(By.xpath("//tr/td[8]")).getText().trim();
			String propertyTaluk = driver.findElement(By.xpath("//tr/td[9]")).getText().trim();
			//System.out.println(" District :" +districtName +"and Taluk :"+ propertyTaluk);
			
			projectName = StringUtil.ReplaceDashWithHyphen(projectName);
			
			String districtCode=CommonUtils.getDistrictCode(districtName);
			String talukCode=CommonUtils.getTalukCode(propertyTaluk);
//			System.out.println(" District :" +districtCode +"and Taluk :"+ talukCode);
			String currentDir = System.getProperty("user.dir");
			String propFolderPath = "KA\\"+districtCode+"\\"+talukCode+"\\"+projectName;
//			System.out.println("Folder path: " + propFolderPath);
//    		CommonUtils.createFolderSubfolder(propFolderPath); 

			// TODO: Need to remove hardcode "BENGALURU URBAN" and replace with code 05, Mentin all allowed district names in a config file
			//Aslo benaglore rural
			if (projStatus.contains("APPROVED") && districtName.contains("BENGALURU URBAN") && pdfStatus.length() < 1) {

				System.out.println("Good to go.." + projStatus + ":" + "HasPDF" + ":" + districtName + ":");
				CommonUtils.createFolderSubfolder(propFolderPath); 
				/***
				 * For the image to download need the text file with property name, dist, taluk
				 * details
				 ***/
				try {
					// Open the file to write in append mode, true
					String imageTxt = "ImageInputData.txt";
					FileWriter imageOutputFile = new FileWriter(imageTxt, true);
					try (BufferedWriter imageWriter = new BufferedWriter(imageOutputFile)) {
						// Write content to the file
						imageWriter.write(projectName + "," + districtName + "," + propertyTaluk + "," + regNumList1.get(ri));
						imageWriter.newLine();
						// Flush the writer to save changes
						imageWriter.flush();
						System.out.println("Content successfully appended to: ImageInputData.txt.");
					}
				} catch (IOException e) {
					System.err.println("An error occurred: in appending file ImageInputData.txt " + e.getMessage());
				}

			} else {
				System.out.println(
						"Skip to next as.." + projStatus + ":" + pdfStatus.length() + ":" + districtName + ":");
				continue;
			}

			/*** Level-3: Complaints starts ***/
			System.out.println("Level-3: Check the complaints -----");
			//Todo: downloadPath need to change to KA/05/.... dynamically
			
//			String downloadPath = createDownloadFolderPath();
//			System.out.println("Project name is :" + projectName);
//			String dateStr = dateFuntion();
//			projectName = downloadPath + "\\" + projectName + dateStr;
//			System.out.println("File Name with timestamp -> " + projectName);
			
			
			System.out.println("Project name is :" + projectName);
			String dateStr = dateFuntion();
			projectName = currentDir + "\\" + propFolderPath + "\\" + projectName + dateStr;
			System.out.println("File Name with timestamp -> " + projectName);

			System.out.println("click on complaints option and copy table content to text file.....");
			driver.findElement(By.cssSelector("a[title='View Complaints']")).click();
			waiting(3000);

			// Create a file to write the output to complaints
			String complaintTxt = projectName + ".txt";
			File compOutputFile = new File(complaintTxt);
			BufferedWriter complaintWriter = new BufferedWriter(new FileWriter(compOutputFile));

			// -----Complaints On this Promoter ---------------
			try {
				complaintWriter.write("Complaints On this Promoter :" + regNumList1.get(ri));
				complaintWriter.newLine();
				// Find the table element by its ID or other appropriate selector
				// WebElement table = driver.findElement(By.id("complaintList")); 
				WebElement table = driver.findElement(By.xpath("//*[@id='menu-comp']//table[@id='complaintList']"));

				// Find all the rows in the table (including header row)
				List<WebElement> rows = table.findElements(By.tagName("tr"));
				// Iterate through each row
				for (WebElement row : rows) {
					// Find all the cells (td) in this row
					List<WebElement> cells = row.findElements(By.tagName("td"));
					// If the row contains cells (skip the header row)
					if (cells.size() > 0) {
						// Iterate over each cell and write its text to the file
						for (WebElement cell : cells) {
							System.out.print(cell.getText() + "\t");
							complaintWriter.write(cell.getText() + "\t"); // Separate by tabs
							complaintWriter.flush();
						}
						System.out.println();
						complaintWriter.newLine(); // Move to the next line after writing the row
					}
				}

				complaintWriter.close();
				System.out.println("Promoter complaints written to " + complaintTxt);

			} catch (IOException e) {
				e.printStackTrace();
			} catch (NoSuchElementException nos) {
				complaintWriter.write("No Complaints Found.");
				complaintWriter.newLine();
				complaintWriter.close();
			} finally {
				// Close the browser and the writer
				// driver.quit();
			}

			// -----Complaints On this Project ---------------
			complaintWriter = new BufferedWriter(new FileWriter(compOutputFile, true));
			try {

				complaintWriter.write("Complaints On this Project :" + regNumList1.get(ri));
				complaintWriter.newLine();
				driver.findElement(By.xpath("//a[contains(text(),'Complaints On this Project')]")).click();
				// WebElement tableP = driver.findElement(By.id("complaintList")); 
				WebElement table = driver.findElement(By.xpath("//*[@id='menu-comp2']//table[@id='complaintList']"));
				// Find all the rows in the table (including header row)
				List<WebElement> rows = table.findElements(By.tagName("tr"));
				// Iterate through each row
				for (WebElement row : rows) {
					// Find all the cells (td) in this row
					List<WebElement> cells = row.findElements(By.tagName("td"));
					// If the row contains cells (skip the header row)
					if (cells.size() > 0) {
						// Iterate over each cell and write its text to the file
						for (WebElement cell : cells) {
							System.out.print(cell.getText() + "\t");
							complaintWriter.write(cell.getText() + "\t"); // Separate by tabs
							complaintWriter.flush();
						}
						System.out.println();
						complaintWriter.newLine(); // Move to the next line after writing the row
					}
				}

				complaintWriter.close();
				System.out.println("Project complaints written to " + complaintTxt);

			} catch (IOException e) {
				e.printStackTrace();
			} catch (NoSuchElementException nos) {
				complaintWriter.write("No Complaints Found.");
				complaintWriter.close();
			} finally {
				// Close the browser and the writer
				// driver.quit();
			}

			//Close the complaints section
			driver.findElement(By.cssSelector("button[onclick='reload()']")).click();
			waiting(2000);

			/*** Complaints section ends ***/

			
			/*** Level: 4 PDF download section starts here ***/
			System.out.println("Level-4: Download the PDF file -----");
			String viewProjDetail = "//tr[td[contains(text()," + "'" + regNumList1.get(ri) + "'"
					+ ")]]//a[@title='View Project Details']";
			WebElement elementPdf = driver.findElement(By.xpath(viewProjDetail));
			elementPdf.click();

			waiting(10000);
			System.out.println("click on print option.....");
			// Todo: need to check later , either can we handle
			try {
				WebElement element = driver.findElement(By.xpath("//input[@onclick='generatePDF()']"));
				JavascriptExecutor ex = (JavascriptExecutor) driver;
				ex.executeScript("arguments[0].click()", element);
			} catch (Exception e) {
				waiting(1000);
			}

			waiting(3000);
			System.out.println("checking new window handles and switch.....");
			Set<String> handlers = driver.getWindowHandles();
			Iterator itr = handlers.iterator();
			String parent = (String) itr.next();
			String child = (String) itr.next();
			driver.switchTo().window(child);
			waiting(2000);

			/*** shadow ***/
			System.out.println("Selecting dropdown to select PDF.....");
			WebElement shadowHost = driver.findElement(By.xpath("//print-preview-app")).getShadowRoot()
					.findElement(By.cssSelector("print-preview-sidebar#sidebar")).getShadowRoot()
					.findElement(By.cssSelector("print-preview-destination-settings#destinationSettings"))
					.getShadowRoot().findElement(By.cssSelector("print-preview-destination-select#destinationSelect"))
					.getShadowRoot()
					.findElement(By.cssSelector("select"));
			Select se = new Select(shadowHost);
			se.selectByVisibleText("Save as PDF");

			waiting(5000);

			System.out.println("Selecting save as option.....");
			WebElement shadowHostSave = driver.findElement(By.cssSelector("print-preview-app")).getShadowRoot()
					.findElement(By.cssSelector("print-preview-sidebar#sidebar")).getShadowRoot()
					.findElement(By.cssSelector("print-preview-button-strip")).getShadowRoot()
					.findElement(By.cssSelector("cr-button.action-button"));

			shadowHostSave.click();
			// --------------
			waiting(5000);

			/*** Save as a file name***/
			// Robot class to handle window "Save As" popup...........
			robot.delay(2000);
			robot.keyPress(KeyEvent.VK_BACK_SPACE);
			robot.delay(2000);
			robot.keyRelease(KeyEvent.VK_BACK_SPACE);

			// Split the name for robot class purpose
			System.out.println("Name of pdf "+projectName);
			char[] chArr = projectName.toCharArray();
			for (int i = 0; i < chArr.length; i++) {
				// System.out.print(chArr[i]);
				type(chArr[i]);
			}

			robot.keyPress(KeyEvent.VK_ENTER);
			robot.delay(2000);
			robot.keyRelease(KeyEvent.VK_ENTER);
			robot.delay(1000);

			driver.switchTo().window(parent);
			driver.navigate().back();
			System.out.println("-----Test Passed!-----" + regNumList1.get(ri) + " : count : " + ri);
			waiting(10000);
		}

		// For next --End
		// close the web
		// driver.close();
	}

	static void waiting(int waitTIme) {
		try {
			//if (waitTIme > 4000) {
				System.out.println("waiting for ....." + waitTIme);
			//}
			Thread.sleep(waitTIme);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static String dateFuntion() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyyyyHHmm");
		LocalDateTime now = LocalDateTime.now();
		// System.out.println("time stamp is :" + dtf.format(now));
		return dtf.format(now);
	}

	public static void type(char character) throws AWTException {
		switch (character) {
		case 'a':
			doType(KeyEvent.VK_A);
			break;
		case 'b':
			doType(KeyEvent.VK_B);
			break;
		case 'c':
			doType(KeyEvent.VK_C);
			break;
		case 'd':
			doType(KeyEvent.VK_D);
			break;
		case 'e':
			doType(KeyEvent.VK_E);
			break;
		case 'f':
			doType(KeyEvent.VK_F);
			break;
		case 'g':
			doType(KeyEvent.VK_G);
			break;
		case 'h':
			doType(KeyEvent.VK_H);
			break;
		case 'i':
			doType(KeyEvent.VK_I);
			break;
		case 'j':
			doType(KeyEvent.VK_J);
			break;
		case 'k':
			doType(KeyEvent.VK_K);
			break;
		case 'l':
			doType(KeyEvent.VK_L);
			break;
		case 'm':
			doType(KeyEvent.VK_M);
			break;
		case 'n':
			doType(KeyEvent.VK_N);
			break;
		case 'o':
			doType(KeyEvent.VK_O);
			break;
		case 'p':
			doType(KeyEvent.VK_P);
			break;
		case 'q':
			doType(KeyEvent.VK_Q);
			break;
		case 'r':
			doType(KeyEvent.VK_R);
			break;
		case 's':
			doType(KeyEvent.VK_S);
			break;
		case 't':
			doType(KeyEvent.VK_T);
			break;
		case 'u':
			doType(KeyEvent.VK_U);
			break;
		case 'v':
			doType(KeyEvent.VK_V);
			break;
		case 'w':
			doType(KeyEvent.VK_W);
			break;
		case 'x':
			doType(KeyEvent.VK_X);
			break;
		case 'y':
			doType(KeyEvent.VK_Y);
			break;
		case 'z':
			doType(KeyEvent.VK_Z);
			break;
		case 'A':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_A);
			break;
		case 'B':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_B);
			break;
		case 'C':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_C);
			break;
		case 'D':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_D);
			break;
		case 'E':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_E);
			break;
		case 'F':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_F);
			break;
		case 'G':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_G);
			break;
		case 'H':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_H);
			break;
		case 'I':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_I);
			break;
		case 'J':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_J);
			break;
		case 'K':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_K);
			break;
		case 'L':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_L);
			break;
		case 'M':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_M);
			break;
		case 'N':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_N);
			break;
		case 'O':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_O);
			break;
		case 'P':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_P);
			break;
		case 'Q':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_Q);
			break;
		case 'R':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_R);
			break;
		case 'S':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_S);
			break;
		case 'T':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_T);
			break;
		case 'U':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_U);
			break;
		case 'V':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_V);
			break;
		case 'W':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_W);
			break;
		case 'X':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_X);
			break;
		case 'Y':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_Y);
			break;
		case 'Z':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_Z);
			break;
		case '!':
			doType(KeyEvent.VK_EXCLAMATION_MARK);
			break;
		case ' ':
			doType(KeyEvent.VK_SPACE);
			break;
		case '1':
			doType(KeyEvent.VK_1);
			break;
		case '2':
			doType(KeyEvent.VK_2);
			break;
		case '3':
			doType(KeyEvent.VK_3);
			break;
		case '4':
			doType(KeyEvent.VK_4);
			break;
		case '5':
			doType(KeyEvent.VK_5);
			break;
		case '6':
			doType(KeyEvent.VK_6);
			break;
		case '7':
			doType(KeyEvent.VK_7);
			break;
		case '8':
			doType(KeyEvent.VK_8);
			break;
		case '9':
			doType(KeyEvent.VK_9);
			break;
		case '0':
			doType(KeyEvent.VK_0);
			break;
		case '-':
			doType(KeyEvent.VK_MINUS);
			break;
		case '–':
			doType(KeyEvent.VK_MINUS);
			break;
		case ':':
			doType(KeyEvent.VK_SHIFT, KeyEvent.VK_SEMICOLON);
			break;
		case '\\':
			doType(KeyEvent.VK_BACK_SLASH);
			break;
		default:
			// doType(KeyEvent.VK_MINUS);
			// throw new IllegalArgumentException("Cannot type character " + character);
		}
	}

	private static void doType(int... keyCodes) throws AWTException {
		doType(keyCodes, 0, keyCodes.length);
	}

	private static void doType(int[] keyCodes, int offset, int length) throws AWTException {

		Robot robot = new Robot();
		if (length == 0) {
			return;
		}

		robot.keyPress(keyCodes[offset]);
		doType(keyCodes, offset + 1, length - 1);
		robot.keyRelease(keyCodes[offset]);
	}

	public static ArrayList<String> regNumList() throws FileNotFoundException {

		Scanner s = new Scanner(new File("ViewSourceFile.txt"));
		ArrayList<String> regNolist = new ArrayList<String>();
		int count = 1;
		String tempVar = "";

		while (s.hasNext()) {
			tempVar = s.next();
			if (tempVar.contains("PRM/KA/RERA")) {

				// Custom input string
				String[] arrOfStr = tempVar.split("'", 2);
				// for (String a : arrOfStr)
				// System.out.println(arrOfStr[1]);
				String regInterMidiate = arrOfStr[1];
				String[] arrOfStr1 = regInterMidiate.split("'", 2);
				// Result sting Registration number to add to array list
				// System.out.println(arrOfStr1[0]);

				regNolist.add(arrOfStr1[0]);
			}
		}
		// System.out.println(regNolist);
		System.out.println("Number of elements" + regNolist.size());
		s.close();

		return regNolist;

	}

//	public static String getTalukCode(String option) {
//		//Eg: System.out.println(getTalukCode("Anekal"));
//	    switch (option) {
//	        case "ANEKAL":
//	            return "01";
//	        case "BENGALURU EAST":
//	            return "02";
//	        case "BENGALURU NORTH":
//	            return "03";
//	        case "BENGALURU SOUTH":
//	            return "04";
//	        case "YELAHANKA":
//	            return "05";
//	        default:	        	
//	            return "Error Invalid Taluk code: " + option;
//	    }
//	}
//
//	public static String getDistrictCode(String option) {
//		//Eg: System.out.println(getDistrictCode("Bengaluru Urban"));
//	    switch (option) {
//	        case "BENGALURU RURAL":
//	            return "04";
//	        case "BENGALURU URBAN":
//	            return "05";
//	        default:
//	            return "Error Invalid District code : " + option;
//	    }
//	}
	
	static String SetDownloadPath() {
		// Get the current working directory
		String currentDir = System.getProperty("user.dir");
		// Output the current directory
		System.out.println("Current directory: " + currentDir);
		return currentDir + "\\Download";
	}

	static String createDownloadFolderPath() {
		// Specify the directory path
		String directoryPath = SetDownloadPath(); // Change this to your desired folder path
		System.out.println("Current directory: " + directoryPath);
		// Create a File object
		File directory = new File(directoryPath);

		// Check if the directory exists, and create it if it doesn't
		if (!directory.exists()) {
			// Create the directory
			boolean created = directory.mkdir(); // Use mkdirs() if you want to create parent dirs as well

			if (created) {
				System.out.println("Directory created successfully.");
			} else {
				System.out.println("Failed to create directory.");
			}
		} else {
			System.out.println("Directory already exists.");
		}

		return directoryPath;

	}

}
