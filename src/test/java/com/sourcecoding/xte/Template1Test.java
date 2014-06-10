/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sourcecoding.xte;

import com.sourcecoding.xte.testobjs.AccountingContainer;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Date;
import org.junit.Test;
import org.junit.Before;

/**
 *
 * @author Matthias
 */
public class Template1Test {

    AccountingContainer ac = new AccountingContainer();

    @Before
    public void prepare() {
        ac = new AccountingContainer();
        ac.accountingDate = new Date();
        ac.accountingNet = new BigDecimal("180");
        ac.accountingGross = ac.accountingNet.multiply(new BigDecimal("1.19"));
        ac.accountingNumber = "123/123FR";
        ac.accountingPeriodFrom = "2014-06-01";
        ac.accountingPeriodTo = "2014-06-30";
        ac.accountingTax = ac.accountingGross.subtract(ac.accountingNet);
        ac.projectKey = "E123";
        ac.projectName = "XLS Template Engine";

        ac.addPerson(ac.new Person("Consultant", "Mustermann", "Max",
                300.0, 300.0 / 8.0, new BigDecimal(115.0), new BigDecimal(115.0 * 300.0)));
        ac.addPerson(ac.new Person("Consultant", "Sauer", "Susi",
                230.0, 230.0 / 8.0, new BigDecimal(115.0), new BigDecimal(115.0 * 230.0)));
        //ac.addPerson(ac.new Person("Seniour Consultant", "Checker", "Bunny",
        //        140.0, 140.0 / 8.0, new BigDecimal(150.0), new BigDecimal(150.0 * 140.0)));
    }

    @Test
    public void shouldRenderTemplate1() throws FileNotFoundException {
        URL templateUrl = this.getClass().getResource("/Template1Test.xls");
        FileOutputStream fos = new FileOutputStream("d:/test-out.xls");
        new XTEngine().setTemplateUrl(templateUrl).setOutputStream(fos)
                .setPayloadPOJO(ac)
                .render();
    }
}
