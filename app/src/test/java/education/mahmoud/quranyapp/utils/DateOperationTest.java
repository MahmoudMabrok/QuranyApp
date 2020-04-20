package education.mahmoud.quranyapp.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class DateOperationTest {


    @Test
    public void getStringDate() {
        String s = DateOperation.getCurrentDateAsString();
        //  String s = DateOperation.getStringDate(new Date());
        assertEquals("10-8-2019", s);
    }

}