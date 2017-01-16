package com.ppm.integration.agilesdk.connector.agm;

import com.ppm.integration.agilesdk.tm.TimeSheetIntegrationContext;
import com.hp.ppm.tm.model.TimeSheet;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by libingc on 2/26/2016.
 */
public class TestTimeSheetIntegrationContext implements TimeSheetIntegrationContext {
    public TimeSheet currentTimeSheet() {
        TimeSheet timeSheet = new TimeSheet();
        Calendar start = new GregorianCalendar();

        start.set(Calendar.MONTH, Calendar.JANUARY);
        start.set(Calendar.DAY_OF_MONTH, 1);
        
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);

        Calendar end = new GregorianCalendar();
        end.set(Calendar.MONTH, Calendar.MAY);
        end.set(Calendar.DAY_OF_MONTH, 15);
        
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND,59);
        
        System.out.println(start.getTime())	;
        System.out.println(end.getTime())	;
        timeSheet.setPeriodStartDate(convertToXMLGregorianCalendar(start.getTime()));
        timeSheet.setPeriodEndDate(convertToXMLGregorianCalendar(end.getTime()));
        return timeSheet;
    }


    public XMLGregorianCalendar convertToXMLGregorianCalendar(Date date) {

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        XMLGregorianCalendar gc = null;
        try {
            gc = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
        } catch (Exception e) {

            e.printStackTrace();
        }
        return gc;
    }

    public  Date convertToDate(XMLGregorianCalendar cal) throws Exception{
        GregorianCalendar ca = cal.toGregorianCalendar();
        return ca.getTime();
    }

    public static void main(String[] args) {
        TestTimeSheetIntegrationContext dateTest = new TestTimeSheetIntegrationContext();
        XMLGregorianCalendar d = dateTest.convertToXMLGregorianCalendar(new Date());
        System.out.println(d.getDay());
        XMLGregorianCalendar cal = null;
        try {
            cal = DatatypeFactory.newInstance().newXMLGregorianCalendar();
            cal.setMonth(06);
            cal.setYear(2010);
            Date date = dateTest.convertToDate(cal);
            String format = "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            System.out.println(formatter.format(date));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
