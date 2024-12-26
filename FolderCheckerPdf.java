package proppy.tool.rera;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import proppy.tool.utils.CommonUtils;

public class FolderCheckerPdf {
	public static void main(String[] args) {
		// Replace with the path to your folder
		// String folderPath = "path/to/your/folder";
		String folderPath = "KA/05/";

		// Specify the file path
		String imageFilePath = "ImageInputData.txt";
		try (BufferedReader imageReader = new BufferedReader(new FileReader(imageFilePath))) {
			String line;
			// Read the file line by line
			while ((line = imageReader.readLine()) != null) {

				// Split the line by the comma separator
				String[] imageParts = line.split(",");

				String projectName = imageParts[0].trim();
				String districtName = imageParts[1].trim();
				String propertyTaluk = imageParts[2].trim();

				String districtCode=CommonUtils.getDistrictCode(districtName);
				String talukCode=CommonUtils.getTalukCode(propertyTaluk);
				
	            
	        	//Folder structure
	        	String propFolderPath = "KA\\"+districtCode+"\\"+talukCode+"\\"+projectName;
	        	//String propFolderPath = "KA\\"+districtCode+"\\"+talukCode+"\\"+projectName+"\\Images";

				File folder = new File(propFolderPath);

				if (folder.exists() && folder.isDirectory()) {
					if (isFolderNotEmpty(folder)) {
//						System.out.println(propFolderPath + ": The folder and/or its subfolders are not empty.");
					} else {
						System.out.println(propFolderPath + ": Empty");
						try {
							// Open the file to write in append mode, true
							String imageTxt = "NoImageList.txt";
							FileWriter imageOutputFile = new FileWriter(imageTxt, true);
							try (BufferedWriter imageWriter = new BufferedWriter(imageOutputFile)) {
								// Write content to the file
								imageWriter.write(propFolderPath + ": Empty");
								imageWriter.newLine();
								imageWriter.flush();
							}
						} catch (IOException e) {
							System.err.println("An error occurred: in appending file NoImageList.txt " + e.getMessage());
						}
					}
				} else {
					System.out.println(propFolderPath + ": Folder path is not a valid");
					try {
						// Open the file to write in append mode, true
						String imageTxt = "NoImageList.txt";
						FileWriter imageOutputFile = new FileWriter(imageTxt, true);
						try (BufferedWriter imageWriter = new BufferedWriter(imageOutputFile)) {
							// Write content to the file
							imageWriter.write(propFolderPath + ": Folder path is not a valid");
							imageWriter.newLine();
							imageWriter.flush();
						}
					} catch (IOException e) {
						System.err.println("An error occurred: in appending file NoImageList.txt " + e.getMessage());
					}
				}

			}
		} catch (IOException e) {
			// Handle exceptions
			System.err.println("An error occurred while reading the file: " + e.getMessage());
		}
	}

	public static boolean isFolderNotEmpty(File folder) {
		File[] files = folder.listFiles();

		if (files == null || files.length == 0) {
			// The folder itself is empty
			return false;
		}

		for (File file : files) {
			if (file.isFile()) {
				// Found a file
				return true;
			} else if (file.isDirectory()) {
				// Recursively check subfolders
				if (isFolderNotEmpty(file)) {
					return true;
				}
			}
		}

		// No files found in this folder or its subfolders
		return false;
	}
}
