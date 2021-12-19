package cxs15.pyrmont.digestertest;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.digester.Digester;

public class Test03 {

    public static void main(String[] args) {
        String path = System.getProperty("user.dir") + File.separator + "etc";
        File file = new File(path, "employee2.xml");
        Digester digester = new Digester();
        digester.addRuleSet(new cxs15.pyrmont.digestertest.EmployeeRuleSet());
        try {
            cxs15.pyrmont.digestertest.Employee employee = (cxs15.pyrmont.digestertest.Employee) digester.parse(file);
            ArrayList offices = employee.getOffices();
            Iterator iterator = offices.iterator();
            System.out.println("-------------------------------------------------");
            while (iterator.hasNext()) {
                cxs15.pyrmont.digestertest.Office office = (cxs15.pyrmont.digestertest.Office) iterator.next();
                cxs15.pyrmont.digestertest.Address address = office.getAddress();
                System.out.println(office.getDescription());
                System.out.println("Address : " +
                        address.getStreetNumber() + " " + address.getStreetName());
                System.out.println("--------------------------------");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
  
