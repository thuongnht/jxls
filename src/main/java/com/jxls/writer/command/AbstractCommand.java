package com.jxls.writer.command;

import com.jxls.writer.*;

/**
 * @author Leonid Vysochyn
 *         Date: 21.03.2009
 */
public abstract class AbstractCommand implements Command {
    Cell cell;
    Size initialSize;

    protected AbstractCommand(Cell cell, Size initialSize) {
        this.cell = cell;
        this.initialSize = initialSize;
    }

    @Override
    public Cell getStartCell() {
        return cell;
    }

    @Override
    public Size getInitialSize() {
        return initialSize;
    }
}