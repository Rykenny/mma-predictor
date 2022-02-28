package com.github.rykenny;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.PrintStream;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class UpdateEventsTest 
{
    /**
     * Rigorous Test :-)
     */
    // @Test
    // public void testFileExists()
    // {
    //     File file = new File("path/to/file.java");
    //     assertTrue(file.exists());
    // }
    @Test
    public void testExceptions()
    {
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        //redirect the System-output (normaly the console) to a variable
        System.setErr(new PrintStream(errContent));
        UpdateEvents.webScraper("bogus");
        assertTrue(errContent.toString().contains("MalformedURLException"));
    }
}

// tests to write.
// 1. ??