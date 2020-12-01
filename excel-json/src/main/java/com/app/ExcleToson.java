package com.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.app.model.Product;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExcleToson {

	private static List<Product> readExcelFile(String filePath) {
		try {
			FileInputStream excelFile = new FileInputStream(new File(filePath));
			Workbook workbook = new XSSFWorkbook(excelFile);

			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rows = sheet.iterator();
			List<Product> lstProducts = new ArrayList<Product>();

			int rowNumber = 0;
			while (rows.hasNext()) {
				Row currentRow = rows.next();

				// skip header
				if (rowNumber == 0) {
					rowNumber++;
					continue;
				}

				Iterator<Cell> cellsInRow = currentRow.iterator();

				Product product = new Product();

				int cellIndex = 0;
				while (cellsInRow.hasNext()) {
					Cell currentCell = cellsInRow.next();

					if (cellIndex == 0) { // ID
						product.setId(String.valueOf(currentCell.getNumericCellValue()));
					} else if (cellIndex == 1) { // Name
						product.setConfDescription(currentCell.getStringCellValue());
					} else if (cellIndex == 2) { // Address
						product.setDescription(currentCell.getStringCellValue());
					} else if (cellIndex == 3) { // Age
						product.setData(currentCell.getStringCellValue());
					} else if (cellIndex == 4) { // Age
						product.setValidity(currentCell.getStringCellValue());
					} else if (cellIndex == 5) { // Age
						product.setPricing(currentCell.getNumericCellValue());
					}
					cellIndex++;
				}
				lstProducts.add(product);
			}
			return lstProducts;

		} catch (IOException e) {
			throw new RuntimeException("message = " + e.getMessage());
		}
	}

	static <T> Collection<List<T>> partitionBasedOnSize(List<T> inputList, int size) {
		final AtomicInteger counter = new AtomicInteger(0);
		return inputList.stream().collect(Collectors.groupingBy(s -> counter.getAndIncrement() / size)).values();
	}

	public static Map<String, Map<String, List<Product>>> pagingData(List<Product> products) {
		Map<String, Map<String, List<Product>>> actualProduct = new LinkedHashMap<>();
		Collection<List<Product>> c = partitionBasedOnSize(products, 3);
		System.out.println(c);
		int i = 1;
		Map<String, List<Product>> productMap = new LinkedHashMap<>();
		for (List<Product> list : c) {
			productMap.put("product" + i, list);
			i++;
		}
		actualProduct.put("page", productMap);
		return actualProduct;
	}

	private static String convertObjects2JsonString(Map<String, Map<String, List<Product>>> products) {
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = "";
		try {
			jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(products);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return jsonString;
	}

	public static void main(String[] args) {
		Map<String, Map<String, List<Product>>> products = pagingData(readExcelFile("product_list.xlsx"));
		try (FileWriter file = new FileWriter("products.json")) {
			file.write(convertObjects2JsonString(products));
			file.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
