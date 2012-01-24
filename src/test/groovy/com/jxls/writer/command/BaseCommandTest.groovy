package com.jxls.writer.command

import spock.lang.Specification
import com.jxls.writer.Cell
import com.jxls.writer.Size

import com.jxls.writer.transform.Transformer

/**
 * @author Leonid Vysochyn
 * Date: 1/18/12 6:25 PM
 */
class BaseCommandTest extends Specification{
    def "test init"(){
        given:
            def transformer = Mock(Transformer)
        when:
            def area = new BaseCommand(new Cell(1,1), new Size(5,5), transformer)
        then:
            assert area.startCell == new Cell(1,1)
            assert area.initialSize == new Size(5,5)
            assert area.transformer == transformer
    }
    
    def "test size"(){
        given:
            def area = new BaseCommand(new Cell(1,1), new Size(10,15))
            def innerArea = Mock(Command)
            area.addCommand(innerArea)
            def context = new Context()
        when:
            innerArea.getInitialSize() >> new Size(1,1)
            innerArea.getSize(context) >> new Size(3,5)
        then:
            assert area.getSize(context) == new Size(12, 19)
    }

    def "test applyAt with inner command"(){
        given:
            def area = new BaseCommand(new Cell(1,1), new Size(10,15),Mock(Transformer))
            def innerArea = Mock(Command)
            innerArea.getStartCell() >> new Cell(2,3)
            area.addCommand(innerArea)
            def context = new Context()
        when:
            area.applyAt(new Cell(4,5), context)
        then:
            1 * innerArea.applyAt(new Cell(5,7), context)
    }
    
    def "test applyAt for simple command"(){
        given:
            def area = new BaseCommand(new Cell(1,1), new Size(2,3))
            def transformer = Mock(Transformer)
            def context = new Context()
            area.setTransformer(transformer)
        when:
            area.applyAt(new Cell(3,4), context)
        then:
            1 * transformer.transform(new Cell(1,1), new Cell(3,4), context)
            1 * transformer.transform(new Cell(1,2), new Cell(3,5), context)
            1 * transformer.transform(new Cell(1,3), new Cell(3,6), context)
            1 * transformer.transform(new Cell(2,1), new Cell(4,4), context)
            1 * transformer.transform(new Cell(2,2), new Cell(4,5), context)
            1 * transformer.transform(new Cell(2,3), new Cell(4,6), context)
            0 * _._
    }


}