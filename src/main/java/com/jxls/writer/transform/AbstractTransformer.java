package com.jxls.writer.transform;

import com.jxls.writer.CellData;
import com.jxls.writer.Pos;
import com.jxls.writer.command.Context;
import com.jxls.writer.transform.poi.RowData;
import com.jxls.writer.transform.poi.SheetData;

import java.util.*;

/**
 * @author Leonid Vysochyn
 *         Date: 2/6/12 6:33 PM
 */
public abstract class AbstractTransformer implements Transformer {

    boolean ignoreColumnProps = false;
    boolean ignoreRowProps = false;
    protected Map<String, SheetData> sheetMap = new LinkedHashMap<String, SheetData>();

    public List<Pos> getTargetPos(Pos pos) {
        CellData cellData = getCellData(pos);
        if (cellData != null) {
            return cellData.getTargetPos();
        } else {
            return new ArrayList<Pos>();
        }
    }

    public void resetTargetCells() {
        for (SheetData sheetData : sheetMap.values()) {
            for (int i = 0; i < sheetData.getNumberOfRows(); i++) {
                RowData rowData = sheetData.getRowData(i);
                if (rowData != null) {
                    for (int j = 0; j < rowData.getNumberOfCells(); j++) {
                        CellData cellData = rowData.getCellData(j);
                        if (cellData != null) {
                            cellData.resetTargetPos();
                        }
                    }
                }
            }
        }
    }

    public CellData getCellData(Pos pos) {
        if (pos == null || pos.getSheetName() == null) return null;
        SheetData sheetData = sheetMap.get(pos.getSheetName());
        if (sheetData == null) return null;
        RowData rowData = sheetData.getRowData(pos.getRow());
        if (rowData == null) return null;
        return rowData.getCellData(pos.getCol());
    }

    public boolean isIgnoreColumnProps() {
        return ignoreColumnProps;
    }

    public void setIgnoreColumnProps(boolean ignoreColumnProps) {
        this.ignoreColumnProps = ignoreColumnProps;
    }

    public boolean isIgnoreRowProps() {
        return ignoreRowProps;
    }

    public void setIgnoreRowProps(boolean ignoreRowProps) {
        this.ignoreRowProps = ignoreRowProps;
    }

    public Set<CellData> getFormulaCells() {
        Set<CellData> formulaCells = new HashSet<CellData>();
        for (SheetData sheetData : sheetMap.values()) {
            for (int i = 0; i < sheetData.getNumberOfRows(); i++) {
                RowData rowData = sheetData.getRowData(i);
                if (rowData != null) {
                    for (int j = 0; j < rowData.getNumberOfCells(); j++) {
                        CellData cellData = rowData.getCellData(j);
                        if (cellData != null && cellData.isFormulaCell()) {
                            formulaCells.add(cellData);
                        }
                    }
                }
            }
        }
        return formulaCells;
    }
}