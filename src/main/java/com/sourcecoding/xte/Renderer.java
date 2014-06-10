/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sourcecoding.xte;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jxl.Cell;
import jxl.CellType;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 *
 * @author Matthias
 */
public class Renderer {

    private Object payload;
    private Map<String, Object> loopPayload = new HashMap<>();
    private WritableSheet sheet;

    public void run(Object payload, Workbook template, OutputStream out) throws IOException, WriteException, BiffException {
        WorkbookSettings settings = new WorkbookSettings();
        settings.setWriteAccess("rw");
        settings.setEncoding("Cp1252");
        //TODO write blog - without setting writeAccess on a linux machine an exception is thrown:

        WritableWorkbook workbook = Workbook.createWorkbook(out, template, settings);
        sheet = workbook.getSheet(0);

        int replaceRowStart = 0;
        int replaceRowEnd = sheet.getRows();
        this.payload = payload;

        //replaceRows(replaceRowStart, replaceRowEnd);
        int currentRowNumber = 0;
        while (currentRowNumber <= sheet.getRows()) {
            currentRowNumber++;
            replaceRow(sheet.getRow(currentRowNumber));
        }

        workbook.write();
        workbook.close();
    }

    void repeater(Cell cell) throws WriteException {
        //repeat
        String formula = cell.getContents();
        String expression = formula.substring("{{repeat: ".length());
        expression = expression.replace("}}", "").trim();

        String pathName = expression.substring(expression.indexOf("in") + 3).trim();
        String scopeName = expression.substring(0, expression.indexOf("in")).trim();

        System.out.println("  -->loop (variable: " + scopeName + ")! " + formula);

        int iteratorRowStart = cell.getRow()+1; //first line will be removed
        int iteratorRowEnd = iteratorRowStart;
        int currentRowNumber = cell.getRow();
        
        sheet.removeRow(currentRowNumber);
        
        List<Cell[]> loopContent = new ArrayList<>();
        boolean endTagAvailable = false;
        while (currentRowNumber <= sheet.getRows()) {
            Cell[] loopCells = sheet.getRow(currentRowNumber);
            for (Cell loopCell : loopCells) {
                if (loopCell.getContents().startsWith("{{end-of-repeat: " + scopeName)) {
                    sheet.removeRow(currentRowNumber);
                    endTagAvailable = true;
                    break;
                }
            }
            if (endTagAvailable)
                break;
            loopContent.add(loopCells);
            iteratorRowEnd++;
            
            currentRowNumber++;
        }
        if (!endTagAvailable)
            throw new RuntimeException("no end-of-repeat found ({{end-of-repeat: " + scopeName + "}})");
        //currentRowNumber++;
        
        Object listValues = DataExtractor.getDataValue(payload, pathName);
        if (listValues == null)
            listValues = DataExtractor.getDataValue(loopPayload, pathName);

        if (listValues == null)
            throw new RuntimeException("no data for path '" + pathName + "' found");

        Iterable c = null;
        if (listValues instanceof Collection) {
            c = (Iterable) ((Collection) listValues);
        } else if (listValues instanceof Map) {
            c = (Iterable) ((Map) listValues).entrySet();
        } else {
            throw new RuntimeException("repeat is only possible for Collections and Maps! (" + scopeName + ")");
        }
       
        for (Object loopValue : c) {
            
            //copy row base block
            System.out.println("  copy block (" + loopContent.size() + " lines; at row " + currentRowNumber + ")");
            copyRows(sheet, loopContent, currentRowNumber);
            //currentRowNumber = currentRowNumber + loopContent.size();
        
            System.out.println("put into scopedCollection: " + scopeName + " : " + loopValue);

            System.out.println("loopValue type: " + loopValue.getClass());
            if (loopValue instanceof Map.Entry) {
                scopeName = scopeName.replace("(", "");
                scopeName = scopeName.replace(")", "");
                System.out.println("scopeName: " + scopeName);
                String scopeKeyName = scopeName.split(",")[0].trim();
                String scopeValueName = scopeName.split(",")[1].trim();
                System.out.println("scopedMap key: " + scopeKeyName + "/" + scopeValueName);
                Map.Entry<String, Object> entry = (Map.Entry<String, Object>) loopValue;
                loopPayload.put(scopeKeyName, entry.getKey());
                loopPayload.put(scopeValueName, entry.getValue());
               
               
            } else {
                loopPayload.put(scopeName, loopValue);                
            }

            System.out.println("replace lines: " + (currentRowNumber-loopContent.size()-1) + " to " + currentRowNumber);
            for (int i=(currentRowNumber-loopContent.size()); i< currentRowNumber; i++) {
                System.out.println("replace row --> " + convertToString(sheet.getRow(i)));
                 replaceRow(sheet.getRow(i));
            } 
             
        }
        
        System.out.println("remove lines: " + iteratorRowStart + " to " + iteratorRowEnd);
        for (int i=iteratorRowStart; i<iteratorRowEnd; i++) {
            System.out.println("remove row --> " + convertToString(sheet.getRow(i)));
            //sheet.removeRow(i);
        }    
        
        
    }

    void replaceRow(Cell[] cells) throws WriteException {
        System.out.println("-->replaceRow: " + convertToString(cells));

        for (Cell cell : cells) {
            String baseContent = cell.getContents();

            if (baseContent.contains("{{") && baseContent.contains("}}")) {

                if (baseContent.startsWith("{{repeat:")) {
                    repeater(cell);

                } else {

                    Object objValue = getValue(baseContent);

                    String value = objValue.toString();

                    WritableCell modifyCell = sheet.getWritableCell(cell.getColumn(), cell.getRow());

                    System.out.println("         obj details: " + objValue + " + " + objValue.getClass() + " - cellType: "
                            + modifyCell.getType() + " / format : "
                            + modifyCell.getCellFormat().getFormat().getFormatString());

                    if (modifyCell.getType() == CellType.LABEL) {
                        Label l = (Label) cell;
                        l.setString(value);
                        cell.getCellFormat();

                        WritableCellFormat format = new WritableCellFormat(cell.getCellFormat());
                        if (objValue.getClass().equals(java.util.Date.class)) {
                            Date dateValue = (Date) objValue;
                            jxl.write.DateTime xlsCell = new jxl.write.DateTime(cell.getColumn(), cell.getRow(), dateValue, format);
                            sheet.addCell(xlsCell);
                        } else if (objValue instanceof Number) {
                            Double numberValue = Double.valueOf(value);
                            jxl.write.Number xlsCell = new jxl.write.Number(cell.getColumn(), cell.getRow(), numberValue, format);
                            sheet.addCell(xlsCell);
                        }

                    }
                }
            }

        }

    }

    protected Object getValue(String formula) {
        //replace
        //loop for more than one replacements
        Object value = formula;
        List<String> fields = getFields(formula);
        for (String field : fields) {
            String fieldPath = field.replace("{{", "").replace("}}", "");

            Object innerValue = DataExtractor.getDataValue(payload, fieldPath);
            if (innerValue == null)
                innerValue = DataExtractor.getDataValue(loopPayload, fieldPath);
            if (innerValue == null) {                
                //skip
                System.out.println("  no value found for field {{" + fieldPath + "}}");
                continue;
            }
            if (fields.size() > 1 || innerValue instanceof String) {
                value = value.toString().replace(field, innerValue.toString());
            } else {
                value = innerValue;
            }

        }
        System.out.println("  field '" + formula + "' is replaced with value: " + value);
        return value;
    }

    private String convertToString(Cell[] cells) {
        StringBuilder sb = new StringBuilder();

        for (Cell cell : cells) {
            sb.append("Cell(").append(cell.getRow()).append("|").append(cell.getColumn()).append("):").append(cell.getContents()).append("; ");
        }

        return sb.toString();
    }

    private void copyRows(WritableSheet sheet, List<Cell[]> rows, int insertRowNumber) throws WriteException {
        for (Cell[] cells : rows) {
            sheet.insertRow(insertRowNumber);

            int cellcounter = 0;
            for (Cell readCell : cells) {
                WritableCell cellSource = sheet.getWritableCell(readCell.getColumn(), readCell.getRow());
                WritableCell newCell = cellSource.copyTo(cellcounter, insertRowNumber);
                sheet.addCell(newCell);
                cellcounter++;
            }
            insertRowNumber++;
        }
    }

    List<String> getFields(String formulaValue) {

        List<String> result = new ArrayList<>();
        String patternString = "(\\{\\{[^\\}]+\\}\\})";

        Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(formulaValue);

        while (matcher.find()) {
            result.add(matcher.group(1));
            //System.out.println("found: " + matcher.group(1));
        }
        return result;
    }
}
