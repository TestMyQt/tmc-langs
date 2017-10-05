package fi.helsinki.cs.tmc.langs.qmake;

import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.RunResult.Status;
import fi.helsinki.cs.tmc.langs.domain.TestResult;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public final class QTestResultParser {

    private static final String DOC_NULL_ERROR_MESSAGE = "Failed to parse test results";
    private static final String SAX_PARSER_ERROR = "SAX parser error occured";
    private static final String PARSING_DONE_MESSAGE = "Qt test cases parsed.";

    private static final Logger log = LoggerFactory.getLogger(QTestResultParser.class);

    private Path testResults;
    private List<TestResult> tests;

    public QTestResultParser(Path testResults) {
        this.testResults = testResults;
        this.tests = parseTestCases(testResults);
    }

    private List<TestResult> parseTestCases(Path testOutput) {
        Document doc;
        try {
            doc = prepareDocument(testOutput);
        } catch (ParserConfigurationException | IOException e) {
            log.error("Unexpected exception, could not parse Qt testcases.", e);
            return new ArrayList<>();
        }

        NodeList nodeList = doc.getElementsByTagName("TestFunction");
        List<TestResult> cases = createQtTestCases(nodeList);

        log.info(PARSING_DONE_MESSAGE);

        return cases;
    }

    private Document prepareDocument(Path testOutput)
            throws ParserConfigurationException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
        documentBuilder.setErrorHandler(null); // Silence logging
        dbFactory.setValidating(false);

        InputStream inputStream = new FileInputStream(testOutput.toFile());
        Reader reader = new InputStreamReader(inputStream, "UTF-8");
        InputSource is = new InputSource(reader);
        is.setEncoding("UTF-8");

        Document doc = null;
        try {
            doc = documentBuilder.parse(is);
        } catch (SAXException ex) {
            log.info(SAX_PARSER_ERROR);
            log.info(ex.toString());
        }

        if (doc == null) {
            log.info(DOC_NULL_ERROR_MESSAGE);
            throw new IllegalStateException(DOC_NULL_ERROR_MESSAGE);
        }

        doc.getDocumentElement().normalize();

        return doc;
    }

    private List<TestResult> createQtTestCases(NodeList nodeList) {
        List<TestResult> cases = new ArrayList<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Element node = (Element) nodeList.item(i);
            Element message = (Element) node.getElementsByTagName("Message").item(0);
            Element incident = (Element) node.getElementsByTagName("Incident").item(0);

            boolean passed = incident.getAttribute("type").equals("pass");

            if (passed && message == null) {
                continue;
            }
            Element desc;

            if (message != null) {
                desc = (Element) message.getElementsByTagName("Description").item(0);
            } else {
                desc = (Element) incident.getElementsByTagName("Description").item(0);
            }

            String msg = desc.getTextContent();
            String id = node.getAttribute("name");
            // Format: TMC:exercise_id.point 
            // TMC:156.1
            List<String> points = new ArrayList<>();
            if (msg.contains(".")) {
                String[] split = msg.split("\\.");
                points.add(split[1]);
            }

            if (passed) {
                msg = "";
            }

            ImmutableList<String> trace = ImmutableList.of();
            cases.add(new TestResult(id, passed, ImmutableList.copyOf(points), msg, trace));
        }

        return cases;
    }

    /**
     * Returns the test results of the tests in this file.
     */
    public List<TestResult> getTestResults() {
        return this.tests;
    }

    /**
     * Returns the combined status of the tests in this file.
     */
    public Status getResultStatus() {
        for (TestResult result : getTestResults()) {
            if (!result.isSuccessful()) {
                return Status.TESTS_FAILED;
            }
        }

        return Status.PASSED;
    }

    /**
     * Returns the run result of this file.
     */
    public RunResult result() {
        return new RunResult(
                getResultStatus(),
                ImmutableList.copyOf(getTestResults()),
                new ImmutableMap.Builder<String, byte[]>().build());
    }
}
