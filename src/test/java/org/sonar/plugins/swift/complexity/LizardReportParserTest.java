/*
 * SonarQube Swift Plugin
 * Copyright (C) 2015 Backelite
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.swift.complexity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class LizardReportParserTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File correctFile;
    private File incorrectFile;

    @Before
    public void setup() throws IOException {
        correctFile = createCorrectFile();
        incorrectFile = createIncorrectFile();
    }

    public File createCorrectFile() throws IOException {
        File xmlFile = folder.newFile("correctFile.xml");
        BufferedWriter out = new BufferedWriter(new FileWriter(xmlFile));
        //header
        out.write("<?xml version=\"1.0\" ?>");
        out.write("<?xml-stylesheet type=\"text/xsl\" href=\"https://raw.github.com/terryyin/lizard/master/lizard.xsl\"?>");
        //root object and measure
        out.write("<cppncss><measure type=\"Function\"><labels><label>Nr.</label><label>NCSS</label><label>CCN</label></labels>");
        //items for function
        out.write("<item name=\"viewDidLoad(...) at App/Controller/Accelerate/AccelerationViewController.m:105\">");
        out.write("<value>2</value><value>15</value><value>1</value></item>");
        out.write("<item name=\"viewWillAppear:(...) at App/Controller/Accelerate/AccelerationViewController.m:130\">");
        out.write("<value>3</value><value>20</value><value>5</value></item>");
        //average and close funciton measure
        out.write("<average lable=\"NCSS\" value=\"17\"/><average lable=\"CCN\" value=\"3\"/><average lable=\"NCSS\" value=\"17\"/>");
        out.write("<average lable=\"CCN\" value=\"3\"/><average lable=\"NCSS\" value=\"17\"/><average lable=\"CCN\" value=\"3\"/>");
        out.write("<average lable=\"NCSS\" value=\"17\"/><average lable=\"CCN\" value=\"3\"/></measure>");
        //open file measure and add the labels
        out.write("<measure type=\"File\"><labels><label>Nr.</label><label>NCSS</label><label>CCN</label><label>Functions</label></labels>");
        //items for file
        out.write("<item name=\"App/Controller/Accelerate/AccelerationViewController.h\">");
        out.write("<value>1</value><value>2</value><value>0</value><value>0</value></item>");
        out.write("<item name=\"App/Controller/Accelerate/AccelerationViewController.m\">");
        out.write("<value>2</value><value>868</value><value>6</value><value>2</value></item>");
        //add averages
        out.write("<average lable=\"NCSS\" value=\"435\"/><average lable=\"CCN\" value=\"70\"/><average lable=\"Functions\" value=\"21\"/>");
        //add sum
        out.write("<sum lable=\"NCSS\" value=\"870\"/><sum lable=\"CCN\" value=\"141\"/><sum lable=\"Functions\" value=\"42\"/>");
        //close measures and root object
        out.write("</measure></cppncss>");

        out.close();

        return xmlFile;
    }

    public File createIncorrectFile() throws IOException {
        File xmlFile = folder.newFile("incorrectFile.xml");
        BufferedWriter out = new BufferedWriter(new FileWriter(xmlFile));
        //header
        out.write("<?xml version=\"1.0\" ?>");
        out.write("<?xml-stylesheet type=\"text/xsl\" href=\"https://raw.github.com/terryyin/lizard/master/lizard.xsl\"?>");
        //root object and measure
        out.write("<cppncss><measure type=\"Function\"><labels><label>Nr.</label><label>NCSS</label><label>CCN</label></labels>");
        //items for function
        out.write("<item name=\"viewDidLoad(...) at App/Controller/Accelerate/AccelerationViewController.m:105\">");
        out.write("<value>2</value><value>15</value><value>1</value></item>");
        out.write("<item name=\"viewWillAppear:(...) at App/Controller/Accelerate/AccelerationViewController.m:130\">");
        out.write("<value>3</value><value>20</value><value>5</value></item>");
        //average and close funciton measure
        out.write("<average lable=\"NCSS\" value=\"17\"/><average lable=\"CCN\" value=\"3\"/><average lable=\"NCSS\" value=\"17\"/>");
        out.write("<average lable=\"CCN\" value=\"3\"/><average lable=\"NCSS\" value=\"17\"/><average lable=\"CCN\" value=\"3\"/>");
        out.write("<average lable=\"NCSS\" value=\"17\"/><average lable=\"CCN\" value=\"3\"/></measure>");
        //open file measure and add the labels
        out.write("<measure type=\"File\"><labels><label>Nr.</label><label>NCSS</label><label>CCN</label><label>Functions</label></labels>");
        //items for file 3th value tag has no closing tag
        out.write("<item name=\"App/Controller/Accelerate/AccelerationViewController.h\">");
        out.write("<value>1</value><value>2</value><value>0<value>0</value></item>");
        out.write("<item name=\"App/Controller/Accelerate/AccelerationViewController.m\">");
        out.write("<value>2</value><value>868</value><value>6</value><value>2</value></item>");
        //add averages
        out.write("<average lable=\"NCSS\" value=\"435\"/><average lable=\"CCN\" value=\"70\"/><average lable=\"Functions\" value=\"21\"/>");
        //add sum
        out.write("<sum lable=\"NCSS\" value=\"870\"/><sum lable=\"CCN\" value=\"141\"/><sum lable=\"Functions\" value=\"42\"/>");
        //close measures and root object no close tag for measure
        out.write("</cppncss>");

        out.close();

        return xmlFile;
    }

    @Test
    public void parseReportShouldReturnMapWhenXMLFileIsCorrect() {
        LizardReportParser parser = new LizardReportParser();

        assertNotNull("correct file is null", correctFile);

        Map<String, List<Measure>> report = parser.parseReport(correctFile);

        assertNotNull("report is null", report);

        assertTrue("Key is not there", report.containsKey("App/Controller/Accelerate/AccelerationViewController.h"));
        List<Measure> list1 = report.get("App/Controller/Accelerate/AccelerationViewController.h");
        assertEquals(4, list1.size());

        for (Measure measure : list1) {
            String s = measure.getMetric().getKey();

            if (s.equals(CoreMetrics.FUNCTIONS_KEY)) {
                assertEquals("Header Functions has a wrong value", 0, measure.getIntValue().intValue());
            } else if (s.equals(CoreMetrics.COMPLEXITY_KEY)) {
                assertEquals("Header Complexity has a wrong value", 0, measure.getIntValue().intValue());
            } else if (s.equals(CoreMetrics.FILE_COMPLEXITY_KEY)) {
                assertEquals("Header File Complexity has a wrong value", 0.0d, measure.getValue().doubleValue(), 0.0d);
            } else if (s.equals(CoreMetrics.COMPLEXITY_IN_FUNCTIONS_KEY)) {
                assertEquals("Header Complexity in Functions has a wrong value", 0, measure.getIntValue().intValue());
            } else if (s.equals(CoreMetrics.FUNCTION_COMPLEXITY_KEY)) {
                assertEquals("Header Functions Complexity has a wrong value", 0.0d, measure.getValue().doubleValue(), 0.0d);
            }
        }

        assertTrue("Key is not there", report.containsKey("App/Controller/Accelerate/AccelerationViewController.m"));

        List<Measure> list2 = report.get("App/Controller/Accelerate/AccelerationViewController.m");
        assertEquals(7, list2.size());
        for (Measure measure : list2) {
            String s = measure.getMetric().getKey();

            if (s.equals(CoreMetrics.FUNCTIONS_KEY)) {
                assertEquals("MFile Functions has a wrong value", 2, measure.getIntValue().intValue());
            } else if (s.equals(CoreMetrics.COMPLEXITY_KEY)) {
                assertEquals("MFile Complexity has a wrong value", 6, measure.getIntValue().intValue());
            } else if (s.equals(CoreMetrics.FILE_COMPLEXITY_KEY)) {
                assertEquals("MFile File Complexity has a wrong value", 6.0d, measure.getValue().doubleValue(), 0.0d);
            } else if (s.equals(CoreMetrics.COMPLEXITY_IN_FUNCTIONS_KEY)) {
                assertEquals("MFile Complexity in Functions has a wrong value", 6, measure.getIntValue().intValue());
            } else if (s.equals(CoreMetrics.FUNCTION_COMPLEXITY_KEY)) {
                assertEquals("MFile Functions Complexity has a wrong value", 3.0d, measure.getValue().doubleValue(), 0.0d);
            }
        }
    }

    @Test
    public void parseReportShouldReturnNullWhenXMLFileIsIncorrect() {
        LizardReportParser parser = new LizardReportParser();

        assertNotNull("correct file is null", incorrectFile);

        Map<String, List<Measure>> report = parser.parseReport(incorrectFile);
        assertNull("report is not null", report);

    }

}
