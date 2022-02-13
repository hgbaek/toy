package data;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 개요 : <br>
 * 작성일 : Aug 22, 2012<br>
 * 작성자 : 민경현<br>
 * Version : 1.00
 */


/*
 * Seegene Framework 메소드 호출 횟수 최적화 작업
 * 
 * 작업한 메소드 리스트: initData(Sheet)
 * 
 * 19.8.14 -
 * @author 조효재
 */

public class SgSheetData 
{
	private String sheetName;
	private Object [][] excelData;
	
	private int columnCount;
	
	private ArrayList<CellRangeAddress> mergedRangeList;
	
	private HashMap<Integer, HashMap<Integer, Short>> backgroundMap;
	
	private HashMap<Integer, Integer> columnWidthMap;
	
	private ArrayList<Point> imageLocattionList;
	
	/**
	 * Sheet 데이터를 관리합니다.
	 * @param sheet
	 */
	public SgSheetData(Sheet sheet)
	{
		this.sheetName = sheet.getSheetName();
		initData(sheet);
	}
	
	/**
	 * Sheet 데이터를 관리합니다.
	 * @param sheet
	 * @param autoSettingMerge
	 */
	public SgSheetData(Sheet sheet, boolean autoSettingMerge)
	{
		this.sheetName = sheet.getSheetName();
		initData(sheet, autoSettingMerge);
	}
	
	/**
	 * Sheet 데이터를 관리합니다.
	 * @param sheet
	 */
	public SgSheetData(String name, Object [][] datas)
	{
		this.sheetName = name;
		this.excelData = datas;
		for(Object [] columns : datas)
		{
			// Column count 를 설정합니다.
			if(columnCount<columns.length)
			{
				columnCount = columns.length;
			}
		}
	}
	
	/**
	 * Sheet 데이터를 관리합니다.
	 * @param sheet
	 */
	public SgSheetData(String name, ArrayList<ArrayList<Object>> datas)
	{
		this.sheetName = name;
		this.excelData = new Object[datas.size()][];
		for(int i=0; i<datas.size(); i++)
		{
			ArrayList<Object> columns = datas.get(i);
			// Column count 를 설정합니다.
			if(columnCount<columns.size())
			{
				columnCount = columns.size();
			}
			excelData[i] = new Object[columns.size()];
			for(int j=0; j<columns.size(); j++)
			{
				excelData[i][j] = columns.get(j);
			}
		}
	}
	
	/**
	 * Sheet 데이터를 관리합니다.
	 * @param sheet
	 */
	public SgSheetData(String name, List<List<String>> datas)
	{
		this.sheetName = name;
		this.excelData = new Object[datas.size()][];
		for(int i=0; i<datas.size(); i++)
		{
			List<String> columns = datas.get(i);
			// Column count 를 설정합니다.
			if(columnCount<columns.size())
			{
				columnCount = columns.size();
			}
			excelData[i] = new Object[columns.size()];
			for(int j=0; j<columns.size(); j++)
			{
				excelData[i][j] = columns.get(j);
			}
		}
	}
	
	/**
	 * Sheet 데이터를 설정합니다.
	 * @param sheet
	 */
	private void initData(Sheet sheet)
	{
		// Row 설정
		excelData = new Object[sheet.getLastRowNum()+1][];
		for(int row=0; row<sheet.getLastRowNum()+1; row++)
		{
			Row sheetRow = sheet.getRow(row);

			// Cell 설정
			if(sheetRow != null && sheetRow.getLastCellNum() > 0)
			{
				excelData[row] = new Object[sheetRow.getLastCellNum()];
				for(int column=0; column<sheetRow.getLastCellNum(); column++)
				{
					Cell cell = sheetRow.getCell(column);
					
					if(cell != null)
					{
						int cellType = cell.getCellType();
						// Column count 를 설정합니다.
						if(columnCount<sheetRow.getLastCellNum())
						{
							columnCount = sheetRow.getLastCellNum();
						}
						// Cell Type 별로 데이터를 설정합니다.
						if(cellType == Cell.CELL_TYPE_BOOLEAN)
						{
							excelData[row][column] = cell.getBooleanCellValue();
						}
						else if(cellType == Cell.CELL_TYPE_NUMERIC)
						{
							excelData[row][column] = cell.getNumericCellValue();
						}
						else if(cellType == Cell.CELL_TYPE_STRING)
						{
							excelData[row][column] = cell.getStringCellValue();
						}
						else if(cellType == Cell.CELL_TYPE_FORMULA)
						{
							if(cell instanceof XSSFCell)
							{
								XSSFCell xssfCell = (XSSFCell)cell;
								excelData[row][column] = xssfCell.getRawValue();
							}
							else if(cell instanceof HSSFCell)
							{
								HSSFCell hssfCell = (HSSFCell)cell;
								excelData[row][column] = hssfCell.toString();
								FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
								evaluator.evaluateFormulaCell(cell);
								excelData[row][column] = evaluator.evaluate(hssfCell).formatAsString();
							}
							else
							{
								excelData[row][column] = cell.toString();
							}
						}
						else
						{
							excelData[row][column] = cell.toString();
						}
					}
					else 
					{
						excelData[row][column] = null;
					}
				}
			}
		}
	}
	
	/**
	 * Sheet 데이터를 설정합니다.
	 * @param sheet
	 */
	private void initData(Sheet sheet, boolean autoSettingMerge)
	{
		// Merge 정보 설정
		if(autoSettingMerge)
		{
			mergedRangeList = new ArrayList<CellRangeAddress>();
			for(int i=0; i<sheet.getNumMergedRegions(); i++)
			{
				CellRangeAddress rangeAddress = sheet.getMergedRegion(i);
				mergedRangeList.add(rangeAddress);
			}
		}
				
		// Row 설정
		excelData = new Object[sheet.getLastRowNum()+1][];
		for(int row=0; row<sheet.getLastRowNum()+1; row++)
		{
			Row sheetRow = sheet.getRow(row);
			
			// Cell 설정
			if(sheetRow != null && sheetRow.getLastCellNum() > 0)
			{
				excelData[row] = new Object[sheetRow.getLastCellNum()];
				for(int column=0; column<sheetRow.getLastCellNum(); column++)
				{
					Cell cell = sheetRow.getCell(column);
					
					if(cell != null)
					{
						
						int cellType = cell.getCellType();
						// Column count 를 설정합니다.
						if(columnCount<sheetRow.getLastCellNum())
						{
							columnCount = sheetRow.getLastCellNum();
						}
						// Cell Type 별로 데이터를 설정합니다.
						if(cellType == Cell.CELL_TYPE_BOOLEAN)
						{
							excelData[row][column] = cell.getBooleanCellValue();
						}
						else if(cellType == Cell.CELL_TYPE_NUMERIC)
						{
							excelData[row][column] = cell.getNumericCellValue();
						}
						else if(cellType == Cell.CELL_TYPE_STRING)
						{
							excelData[row][column] = cell.getStringCellValue();
						}
						else if(cellType == Cell.CELL_TYPE_FORMULA)
						{
							if(cell instanceof XSSFCell)
							{
								XSSFCell xssfCell = (XSSFCell)cell;
								excelData[row][column] = xssfCell.getRawValue();
							}
							else if(cell instanceof HSSFCell)
							{
								HSSFCell hssfCell = (HSSFCell)cell;
								excelData[row][column] = hssfCell.toString();
								FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
								evaluator.evaluateFormulaCell(cell);
								excelData[row][column] = evaluator.evaluate(hssfCell).formatAsString();
							}
							else
							{
								excelData[row][column] = cell.toString();
							}
						}
						else
						{
							excelData[row][column] = cell.toString();
						}
					}
					else 
					{
						excelData[row][column] = null;
					}
					if(autoSettingMerge && mergedRangeList != null)
					{
						for(CellRangeAddress cellRangeAddress : mergedRangeList)
						{
							if(cellRangeAddress.getFirstRow() <= row && row <= cellRangeAddress.getLastRow())
							{
								if(cellRangeAddress.getFirstColumn() <= column && column <= cellRangeAddress.getLastColumn())
								{
									excelData[row][column] = excelData[cellRangeAddress.getFirstRow()][cellRangeAddress.getFirstColumn()];
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * 해당 Row 의 데이터 배열을 반환합니다.
	 * @param row
	 * @return
	 */
	public Object[] getRow(int row)
	{
		return excelData[row];
	}
	
	/**
	 * 해당 Row 의 데이터 배열을 반환합니다.
	 * @param row
	 * @return
	 */
	public Object[][] getRows()
	{
		return excelData;
	}
	
	/**
	 * Sheet 의 이름을 반환합니다.
	 * @return
	 */
	public String getSheetName()
	{
		return sheetName;
	}
	
	/**
	 * Sheet 의 데이터 배열을 반환합니다.
	 * @return
	 */
	public Object[][] getData()
	{
		return excelData;
	}
	
	/**
	 * 해당 cell 의 데이터를 반환합니다.
	 * @param row
	 * @param column
	 * @return
	 */
	public Object getData(int row, int column)
	{
		return excelData[row][column];
	}
	
	/**
	 * 해당 cell 의 데이터를 반환합니다.
	 * @param row
	 * @param columnNmae
	 * @return
	 */
	public Object getData(int row, Object columnName)
	{
		int column = getColumnIndex(columnName);
		if(column != -1)
		{
			return excelData[row][column];
		}
		return null;
	}
	
	/**
	 * 해당 cell 의 데이터를 반환합니다.
	 * @param row
	 * @param column
	 * @return
	 */
	public String getDataString(int row, int column)
	{
		if(excelData[row][column] != null)
		{
			return String.valueOf(excelData[row][column]);
		}
		return null;
	}
	
	/**
	 * 해당 cell 의 데이터를 반환합니다.
	 * @param row
	 * @param columnNmae
	 * @return
	 */
	public String getDataString(int row, Object columnName)
	{
		int column = getColumnIndex(columnName);
		if(column != -1)
		{
			if(excelData[row][column] != null)
			{
				return String.valueOf(excelData[row][column]);
			}
		}
		return null;
	}
	
	/**
	 * 해당 cell 의 데이터를 입력합니다.
	 * @param row
	 * @param column
	 * @return
	 */
	public Object setData(int row, int column, Object value)
	{
		return excelData[row][column] = value;
	}
	
	/**
	 * Row 수를 반환합니다.
	 * @return
	 */
	public int getRowCount()
	{
		return excelData.length;
	}
	
	/**
	 * Column 수를 반환합니다.
	 * @return
	 */
	public int getColumnCount()
	{
		return columnCount;
	}
	
	/**
	 * Column Index 를 반환합니다.
	 * @param column
	 * @return
	 */
	public int getColumnIndex(Object column)
	{
		if(excelData != null && excelData[0] != null)
		{
			for(int i=0; i<excelData[0].length; i++)
			{
				Object columnName = excelData[0][i];
				if(columnName.toString().toUpperCase().equals(column.toString().toUpperCase()))
				{
					return i;
				}
			}
		}
		return -1;
	}
	
	/**
	 * @return the mergedRangeList
	 */
	public ArrayList<CellRangeAddress> getMergedRangeList() 
	{
		if(mergedRangeList == null)
		{
			mergedRangeList = new ArrayList<CellRangeAddress>();
		}
		return mergedRangeList;
	}

	/**
	 * @param mergedRangeList the mergedRangeList to set
	 */
	public void setMergedRangeList(ArrayList<CellRangeAddress> mergedRangeList) 
	{
		this.mergedRangeList = mergedRangeList;
	}
	
	/**
	 * cell 의 배경색을 반환합니다.
	 * @param row
	 * @param col
	 * @return
	 */
	public Short getBackground(int row, int col)
	{
		if(backgroundMap != null && backgroundMap.containsKey(row))
		{
			if(backgroundMap.get(row).containsKey(col))
			{
				return backgroundMap.get(row).get(col);
			}
		}
		return null;
	}
	
	/**
	 * cell 의 배경색을 설정합니다.
	 * @param row
	 * @param col
	 * @param color
	 */
	public void setBackground(int row, int col, short color)
	{
		if(backgroundMap == null)
		{
			backgroundMap = new HashMap<Integer, HashMap<Integer,Short>>();
		}
		if(backgroundMap.containsKey(row))
		{
			backgroundMap.get(row).put(col, color);
		}
		else
		{
			HashMap<Integer,Short> colorMap = new HashMap<Integer, Short>();
			colorMap.put(col, color);
			backgroundMap.put(row, colorMap);
		}
	}
	
	/**
	 * cell 의 설정된 배경색을 삭제합니다.
	 * @param row
	 * @param col
	 */
	public void removeBackground(int row, int col)
	{
		if(backgroundMap.containsKey(row))
		{
			backgroundMap.get(row).remove(col);
		}
	}
	
	/**
	 * cell 에 적용할 배경색 Map 정보를 설정합니다. 
	 * @param backgroundMap
	 */
	public void setBackgroundMap(HashMap<Integer, HashMap<Integer, Short>> backgroundMap)
	{
		this.backgroundMap = backgroundMap;
	}
	
	/**
	 * 컬럼 사이즈를 설정합니다.
	 * @param column
	 * @param width
	 */
	public void setColumnWidth(int column, int width)
	{
		if(columnWidthMap == null)
		{
			columnWidthMap = new HashMap<Integer, Integer>();
		}
		columnWidthMap.put(column, width);
	}
	
	/**
	 * 컬럼 사이즈를 반환합니다.
	 * @param column
	 * @return
	 */
	public int getColumnWidth(int column)
	{
		if(columnWidthMap != null && columnWidthMap.containsKey(column))
		{
			return columnWidthMap.get(column);
		}
		else
		{
			return -1;
		}
	}
	
	/**
	 * 컬럼 사이즈 맵을 설정합니다.
	 * @param columnWidthMap
	 */
	public void setColumnWidthMap(HashMap<Integer, Integer> columnWidthMap)
	{
		this.columnWidthMap = columnWidthMap;
	}
	
	/**
	 * column 데이터를 삭제합니다.<br>
	 * 관련된 백그라운드 정보와 병합 정보도 같이 처리해 줍니다.
	 * @param removeColumn
	 */
	public void removeColumn(int removeColumn)
	{
		Object[][] cloneData = excelData.clone();
		excelData = new Object[cloneData.length][];
		// 해당 데이터를 삭제합니다.
		for(int i=0; i<cloneData.length; i++)
		{
			if(cloneData[i].length > removeColumn)
			{
				excelData[i] = new Object[cloneData[i].length-1];
				int index = 0;
				for(int j=0; j<cloneData[i].length; j++)
				{
					if(j != removeColumn)
					{
						excelData[i][index] = cloneData[i][j];
						index++;
					}
				}
			}
			else
			{
				excelData[i] = cloneData[i];
			}
		}
		// 컬럼 사이즈 정보를 재설정 합니다.
		if(columnWidthMap != null)
		{
			Object [] columns = columnWidthMap.keySet().toArray();
			Arrays.sort(columns);
			boolean checkColumnWidthRemove = false;
			for(Object column : columns)
			{
				if(removeColumn < (Integer)column)
				{
					checkColumnWidthRemove = true;
					columnWidthMap.put((Integer)column-1, columnWidthMap.get(column));
				}
			}
			if(checkColumnWidthRemove)
			{
				columnWidthMap.remove(columns[columns.length-1]);
			}
		}
		// 백그라운드 정보를 재설정 합니다.
		if(backgroundMap != null)
		{
			for(Object row : backgroundMap.keySet())
			{
				Object [] columns = backgroundMap.get(row).keySet().toArray();
				Arrays.sort(columns);
				boolean checkBackgroundRemove = false;
				for(Object column :  columns)
				{
					if(removeColumn < (Integer)column)
					{
						checkBackgroundRemove = true;
						backgroundMap.get(row).put((Integer)column-1, backgroundMap.get(row).get(column));
					}
				}
				// 데이터를 지운 적이 있으면 마지막 데이터는 삭제합니다.
				if(checkBackgroundRemove)
				{
					backgroundMap.get(row).remove(columns[columns.length-1]);
				}
			}
		}
		// 병합 정보를 재설정 합니다.
		if(mergedRangeList != null)
		{
			for(int k=mergedRangeList.size()-1; k>=0; k--)
			{
				CellRangeAddress cellRange = mergedRangeList.get(k);
				if(removeColumn == cellRange.getFirstColumn() && removeColumn == cellRange.getLastColumn())
				{
					mergedRangeList.remove(cellRange);
				}
				else if(removeColumn < cellRange.getFirstColumn())
				{
					cellRange.setFirstColumn(cellRange.getFirstColumn()-1);
					cellRange.setLastColumn(cellRange.getLastColumn()-1);
				}
				else if(removeColumn < cellRange.getLastColumn())
				{
					cellRange.setLastColumn(cellRange.getLastColumn()-1);
				}
			}
		}
	}
	
	/**
	 * row 데이터를 삭제합니다.<br>
	 * 관련된 백그라운드 정보와 병합 정보도 같이 처리해 줍니다.
	 * @param removeRow
	 */
	public void removeRow(int removeRow)
	{
		if(excelData.length > removeRow)
		{
			Object[][] cloneData = excelData.clone();
			excelData = new Object[cloneData.length-1][];
//			System.out.println(excelData[0].toString());
			int index = 0;
			for(int i=0; i<cloneData.length; i++)
			{
				if(i != removeRow)
				{
					excelData[index] = cloneData[i];
					index++;
				}
			}
			// 백그라운드 정보를 재설정 합니다.
			if(backgroundMap != null)
			{
				Object [] rows = backgroundMap.keySet().toArray();
				Arrays.sort(rows);
				boolean checkBackgroundRemove = false;
				for(Object row : rows)
				{
					if((Integer)row > removeRow)
					{
						checkBackgroundRemove = true;
						backgroundMap.put((Integer)row-1, backgroundMap.get(row));
					}
				}
				// 데이터를 지운 적이 있으면 마지막 데이터는 삭제합니다.
				if(checkBackgroundRemove)
				{
					backgroundMap.remove(rows[rows.length-1]);
				}
			}
			if(mergedRangeList!=null)
			{
				// 병합 정보를 재설정 합니다.
				for(int k=mergedRangeList.size()-1; k>=0; k--)
				{
					CellRangeAddress cellRange = mergedRangeList.get(k);
					if(removeRow == cellRange.getFirstRow() && removeRow == cellRange.getLastRow())
					{
						mergedRangeList.remove(cellRange);
					}
					else if(removeRow < cellRange.getFirstRow())
					{
						cellRange.setFirstRow(cellRange.getFirstRow()-1);
						cellRange.setLastRow(cellRange.getLastRow()-1);
					}
					else if(removeRow < cellRange.getLastRow())
					{
						cellRange.setLastRow(cellRange.getLastRow()-1);
					}
				}
			}
		}
	}
	
	/**
	 * row 데이터를 추가합니다.
	 */
	public void addRow(Object [] rows)
	{
		Object[][] cloneData = excelData.clone();
		excelData = new Object[cloneData.length+1][];
		for(int i=0; i<cloneData.length; i++)
		{
			excelData[i] = cloneData[i];
		}
		excelData[cloneData.length] = rows;
	}

	public void printExcelData() {

		for( int i=0; i<excelData.length; i++ ) {
			for( int j=0; j<excelData[i].length; j++ ) {
				
				System.out.println ("(" + i + "/" + j + ") " + sheetName + " xxx : " + excelData[i][j] );
			}
		}
	}
}

