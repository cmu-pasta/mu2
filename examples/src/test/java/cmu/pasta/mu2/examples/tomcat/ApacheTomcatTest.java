package cmu.pasta.mu2.examples.tomcat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.examples.xml.XMLDocumentUtils;
import edu.berkeley.cs.jqf.examples.xml.XmlDocumentGenerator;
import edu.berkeley.cs.jqf.examples.common.Dictionary;
import org.apache.tomcat.util.descriptor.web.WebXml;
import org.apache.tomcat.util.descriptor.web.WebXmlParser;

import org.junit.Assume;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import edu.berkeley.cs.jqf.fuzz.difffuzz.Comparison;
import edu.berkeley.cs.jqf.fuzz.difffuzz.DiffFuzz;

@RunWith(JQF.class)
public class ApacheTomcatTest {

    public String testWithInputStream(InputStream in) {
        InputSource inputSource = new InputSource(in);
        WebXml webXml = new WebXml();
        WebXmlParser parser = new WebXmlParser(false, false, true);
        boolean success = parser.parseWebXml(inputSource, webXml, false);
        Assume.assumeTrue(success);
        return webXml.toXml();
    }

    @Fuzz(repro="${repro}")
    public void fuzzWithGenerator(@From(XmlDocumentGenerator.class) @Dictionary("dictionaries/tomcat-webxml.dict") Document dom) {
        testWithInputStream(XMLDocumentUtils.documentToInputStream(dom));
    }

    @DiffFuzz
    public String testWithGenerator(@From(XmlDocumentGenerator.class) @Dictionary("dictionaries/tomcat-webxml.dict") Document dom) {
        return testWithInputStream(XMLDocumentUtils.documentToInputStream(dom));
    }

    @Fuzz
    public void debugWithGenerator(@From(XmlDocumentGenerator.class) @Dictionary("dictionaries/tomcat-webxml.dict") Document dom) {
        System.out.println(XMLDocumentUtils.documentToString(dom));
        testWithGenerator(dom);
    }

    @Fuzz
    public void testWithString(String input){
        testWithInputStream(new ByteArrayInputStream(input.getBytes()));
    }

    @DiffFuzz(cmp = "noncompare")
    public String testWithGeneratorNoncompare(@From(XmlDocumentGenerator.class) @Dictionary("dictionaries/tomcat-webxml.dict") Document dom) {
        return testWithInputStream(XMLDocumentUtils.documentToInputStream(dom));
    }

    @Comparison
    public static Boolean noncompare(String s1, String s2) {
        return true;
    }
}
