/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sourcecoding.xte.testobjs;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Matthias
 */
public class AccountingContainer {

    public Date accountingDate;
    public String accountingPeriodFrom;
    public String accountingPeriodTo;
    public String projectKey;
    public String projectName;
    public String accountingNumber;
    
    public BigDecimal accountingNet = new BigDecimal("0.0");
    public BigDecimal accountingTax = new BigDecimal("0.0");
    public BigDecimal accountingGross = new BigDecimal("0.0");
    public Long taxRate = (long) 19; //default
    
    public Map<String, PersonGroup> peopleByTitle = new LinkedHashMap<>();

    public void addPerson(Person person) {
        PersonGroup pg;
        if (peopleByTitle.containsKey(person.title)) {
            pg = (PersonGroup) peopleByTitle.get(person.title);
        } else {
            pg = new PersonGroup();
            pg.groupName = person.title;
            peopleByTitle.put(person.title, pg);
        }
        pg.personGroup.add(person);
        pg.sum = pg.sum.add(person.totalPrice);
        accountingNet = accountingNet.add(person.totalPrice);
        
        double taxFactor = taxRate / 100.0;
        accountingTax = new BigDecimal ( accountingNet.doubleValue() * taxFactor );
        accountingGross =   new BigDecimal ( accountingNet.doubleValue() * (1+taxFactor) );
        
    }

    public class PersonGroup {

        public String groupName;
        public List<Person> personGroup = new ArrayList<>();
        public BigDecimal sum = new BigDecimal("0.0");
    }

    public class Person {

        public Person() {
        }

        public Person(String title, String lastname, String firstname, Double totalHours, Double totalDays, BigDecimal pricePerHour, BigDecimal totalPrice) {
            this.title = title;
            this.lastname = lastname;
            this.firstname = firstname;
            this.totalHours = totalHours;
            this.totalDays = totalDays;
            this.pricePerHour = pricePerHour;
            this.totalPrice = totalPrice;
        }

        public String title;
        public String lastname;
        public String firstname;
        public Double totalHours;
        public Double totalDays;
        public BigDecimal pricePerHour;
        public BigDecimal totalPrice;
    }

}
