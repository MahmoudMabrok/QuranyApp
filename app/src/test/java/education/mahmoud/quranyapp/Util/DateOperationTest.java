package education.mahmoud.quranyapp.Util;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class DateOperationTest {


    @Test
    public void getStringDate() {
        String s = DateOperation.getCurrentDateAsString();
        //  String s = DateOperation.getStringDate(new Date());
        assertEquals("10-8-2019", s);
    }

}