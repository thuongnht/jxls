package com.jxls.writer.transform.poi;

import com.jxls.writer.CellData;
import com.jxls.writer.CellRef;
import com.jxls.writer.command.Context;
import com.jxls.writer.transform.AbstractTransformer;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Leonid Vysochyn
 *         Date: 1/23/12 2:36 PM
 */
public class PoiTransformer extends AbstractTransformer {
    static Logger logger = LoggerFactory.getLogger(PoiTransformer.class);

    Workbook workbook;

    private PoiTransformer(Workbook workbook) {
        this.workbook = workbook;
    }

    public static PoiTransformer createTransformer(Workbook workbook) {
        PoiTransformer transformer = new PoiTransformer(workbook);
        transformer.readCellData();
        return transformer;
    }

    private void readCellData(){
        int numberOfSheets = workbook.getNumberOfSheets();
        for(int i = 0; i < numberOfSheets; i++){
            Sheet sheet = workbook.getSheetAt(i);
            SheetData sheetData = SheetData.createSheetData(sheet);
            sheetMap.put(sheetData.getSheetName(), sheetData);
        }
    }

    public void transform(CellRef cellRef, CellRef newCellRef, Context context) {
//        if(cellData == null ||  cellData.length <= cellRef.getSheet() || cellData[cellRef.getSheet()] == null ||
//                cellData[cellRef.getSheet()].length <= cellRef.getRow() || cellData[cellRef.getSheet()][cellRef.getRow()] == null ||
//                cellData[cellRef.getSheet()][cellRef.getRow()].length <= cellRef.getCol()) return;
        CellData cellData = this.getCellData(cellRef);
        if(cellData != null){
            cellData.addTargetPos(newCellRef);
            if(newCellRef == null || newCellRef.getSheetName() == null){
                logger.info("Target cellRef is null or has empty sheet name, cellRef=" + newCellRef);
                return;
            }
            Sheet destSheet = workbook.getSheet(newCellRef.getSheetName());
            if(destSheet == null){
                destSheet = workbook.createSheet(newCellRef.getSheetName());
            }
            SheetData sheetData = sheetMap.get(cellRef.getSheetName());
            if(!isIgnoreColumnProps()){
                destSheet.setColumnWidth(newCellRef.getCol(), sheetData.getColumnWidth(cellRef.getCol()));
            }
            Row destRow = destSheet.getRow(newCellRef.getRow());
            if (destRow == null) {
                destRow = destSheet.createRow(newCellRef.getRow());
            }
            if(!isIgnoreRowProps()){
                destSheet.getRow(newCellRef.getRow()).setHeight( sheetData.getRowData(cellRef.getRow()).getHeight());
            }
            org.apache.poi.ss.usermodel.Cell destCell = destRow.getCell(newCellRef.getCol());
            if (destCell == null) {
                destCell = destRow.createCell(newCellRef.getCol());
            }
            try{
                destCell.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK);
                ((PoiCellData)cellData).writeToCell(destCell, context);
                copyMergedRegions(cellData, newCellRef);
            }catch(Exception e){
                logger.error("Failed to write a cell with " + cellData + " and " + context, e);
            }
        }
    }

    private void copyMergedRegions(CellData sourceCellData, CellRef destCell) {
        if(sourceCellData.getSheetName() == null ){ throw new IllegalArgumentException("Sheet name is null in copyMergedRegion");}
        SheetData sheetData = sheetMap.get( sourceCellData.getSheetName() );
        CellRangeAddress cellMergedRegion = null;
        for (CellRangeAddress mergedRegion : sheetData.getMergedRegions()) {
            if(mergedRegion.getFirstRow() == sourceCellData.getRow() && mergedRegion.getFirstColumn() == sourceCellData.getCol()){
                cellMergedRegion = mergedRegion;
                break;
            }
        }
        if( cellMergedRegion != null){
            findAndRemoveExistingCellRegion(destCell);
            Sheet destSheet = workbook.getSheet(destCell.getSheetName());
            destSheet.addMergedRegion(new CellRangeAddress(destCell.getRow(), destCell.getRow() + cellMergedRegion.getLastRow() - cellMergedRegion.getFirstRow(),
                    destCell.getCol(), destCell.getCol() + cellMergedRegion.getLastColumn() - cellMergedRegion.getFirstColumn()));
        }
    }

    private void findAndRemoveExistingCellRegion(CellRef cellRef) {
        Sheet destSheet = workbook.getSheet(cellRef.getSheetName());
        int numMergedRegions = destSheet.getNumMergedRegions();
        List<Integer> regionsToRemove = new ArrayList<Integer>();
        for(int i = 0; i < numMergedRegions; i++){
            CellRangeAddress mergedRegion = destSheet.getMergedRegion(i);
            if( mergedRegion.getFirstRow() <= cellRef.getRow() && mergedRegion.getLastRow() >= cellRef.getRow() &&
                    mergedRegion.getFirstColumn() <= cellRef.getCol() && mergedRegion.getLastColumn() >= cellRef.getCol() ){
                destSheet.removeMergedRegion(i);
                break;
            }
        }
    }

    public void setFormula(CellRef cellRef, String formulaString) {
        if(cellRef == null || cellRef.getSheetName() == null ) return;
        Sheet sheet = workbook.getSheet(cellRef.getSheetName());
        if( sheet == null){
            sheet = workbook.createSheet(cellRef.getSheetName());
        }
        Row row = sheet.getRow(cellRef.getRow());
        if( row == null ){
            row = sheet.createRow(cellRef.getRow());
        }
        org.apache.poi.ss.usermodel.Cell poiCell = row.getCell(cellRef.getCol());
        if( poiCell == null ){
            poiCell = row.createCell(cellRef.getCol());
        }
        try{
            poiCell.setCellFormula( formulaString );
        }catch (Exception e){
            logger.error("Failed to set formula = " + formulaString + " into cell = " + cellRef.getCellName(), e);
        }
    }

    public int getSheetIndex(String sheetName) {
        return workbook.getSheetIndex(sheetName);
    }

}
