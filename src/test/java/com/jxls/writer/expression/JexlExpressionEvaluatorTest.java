package com.jxls.writer.expression;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.internal.matchers.StringContains.containsString;
import static org.junit.matchers.JUnitMatchers.both;

/**
 * Date: Nov 2, 2009
 *
 * @author Leonid Vysochyn
 */
public class JexlExpressionEvaluatorTest {

    @Test
    public void simple2VarExpression(){
        String expression = "2 * x + y";
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("x", 2);
        vars.put("y", 3);
        ExpressionEvaluator expressionEvaluator = new JexlExpressionEvaluator(vars);
        Object result = expressionEvaluator.evaluate( expression );
        assertNotNull( result );
        assertEquals( "Simple 2-var expression evaluation result is wrong", "7", result.toString());
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldThrowEvaluationExceptionWhenError(){
        String expression = "2 * x + y )";
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("x", 2);
        vars.put("y", 3);
        ExpressionEvaluator expressionEvaluator = new JexlExpressionEvaluator(vars);
        thrown.expect(EvaluationException.class);
        thrown.expectMessage(both(containsString("error")).and(containsString(expression)));
        Object result = expressionEvaluator.evaluate( expression );
        assertNotNull( result );
    }
}