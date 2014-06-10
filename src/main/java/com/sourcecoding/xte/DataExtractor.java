/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sourcecoding.xte;

import java.util.logging.Level;
import java.util.logging.Logger;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

/**
 *
 * @author Matthias
 */
public class DataExtractor {

    public static Object getDataValue(Object dataStructure, String path) {
        try {
            OgnlContext context = new OgnlContext();
            Object expression = Ognl.parseExpression(path);
            Object result = Ognl.getValue(expression, context, dataStructure);

            return result;

        } catch (OgnlException ex) {
            Logger.getLogger(DataExtractor.class.getName()).log(Level.SEVERE, ex.getMessage() ); //, ex);
            //throw new RuntimeException(ex);
            return null;
        }

    }

    public static String getStringValue(Object dataStructure, String path) {
        Object result = getDataValue(dataStructure, path);
        if (result == null)
            return null;
        return result.toString();
    }

}
