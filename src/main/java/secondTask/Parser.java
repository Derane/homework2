package secondTask;

import org.json.JSONWriter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static java.util.Collections.reverseOrder;
import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

public class Parser {

	private static final String VIOLATIONS_INPUT_DIRECTORY = "src/main/java/secondTask/violations/";

	public static void main(String[] args) throws Exception {
		parseXMLViolationIntoJSONAndWrite();
	}

	private static void parseXMLViolationIntoJSONAndWrite()
			throws ParserConfigurationException, SAXException, IOException {
		File violationsInputDirectory = new File(VIOLATIONS_INPUT_DIRECTORY);
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		SAXParser parser = parserFactory.newSAXParser();
		SAXHandler handler = new SAXHandler();
		Map<String, Double> violationsMap = new HashMap<>();
		Optional<File[]> optionalFiles = Optional.ofNullable(violationsInputDirectory.listFiles());
		parseXmlAndFillMap(parser, handler, violationsMap, optionalFiles);

		var sortedMapOfViolations = violationsMap.entrySet()
				.stream()
				.sorted(comparingByValue(reverseOrder()))
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

		writeInJSON(sortedMapOfViolations);
	}

	private static void writeInJSON(LinkedHashMap<String, Double> map) throws IOException {
		try (FileWriter file = new FileWriter("src/main/java/secondTask/summary.json")) {
			map.forEach((key, value) -> {
				new JSONWriter(file)
						.object()
						.key(key)
						.value(value)
						.endObject();
				try {
					file.write("\n");
				} catch (IOException e) {
					e.printStackTrace();
				}

			});
			file.flush();
		}
	}

	private static void parseXmlAndFillMap(SAXParser parser, SAXHandler handler, Map<String, Double> violationsMap, Optional<File[]> optionalFiles) {
		optionalFiles.ifPresent(files -> {
			Arrays.stream(files).forEach(file -> {
				try {
					parser.parse(VIOLATIONS_INPUT_DIRECTORY + file.getName(), handler);
				} catch (SAXException | IOException e) {
					e.printStackTrace();
				}
				handler.violations.forEach(violation -> {
					if (!violationsMap.containsKey(violation.getType())) {
						violationsMap.put(violation.getType(), violation.getFineAmount());
					} else {
						violationsMap.put(violation.getType(), violationsMap.get(violation.getType()) + violation.getFineAmount());
					}
				});
			});
		});
	}

	private static class SAXHandler extends DefaultHandler {

		List<Violation> violations = new ArrayList<>();
		Violation violation = null;
		String content = null;

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if ("violation".equals(qName)) {
				violation = new Violation();
			}

		}

		@Override
		public void endElement(String uri, String localName, String qName) {
			switch (qName) {
				case "violation" -> {
					violations.add(violation);
				}
				case "type" -> {
					violation.setType(content);
				}
				case "fine_amount" -> {
					violation.setFineAmount(Double.valueOf(content));
				}
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) {
			content = String.copyValueOf(ch, start, length).trim();
		}
	}
}