package firstTask;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public class Parser {

	private static final String INPUT_FILE = "src/main/java/firstTask/input.xml";
	private static final String OUTPUT_FILE = "src/main/java/firstTask/output.xml";
	private static final Pattern FIND_NAME = compile("\\sname\\s*=\\s*\"(\\W+)\"");
	private static final Pattern FIND_SURNAME = compile("surname\\s*=\\s*\"(\\W+)\"");
	private static final Pattern FIND_END_TAG = compile(">");
	private static final Pattern PATTERN_FOR_REPLACE = compile("\\w*name\\s*=\\s*\"(\\W+)\"\\s*\\w*name\\s*=\\s*\"(\\W+)\"");

	public static void main(String[] args) {
		parseAndWriteXML();
	}

	private static void parseAndWriteXML() {
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(INPUT_FILE));
			 BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(OUTPUT_FILE));) {

			String name = null;
			String surname = null;
			String temp = "";
			while (bufferedReader.ready()) {
				temp += bufferedReader.readLine() + "\r\n";
				Matcher matcherName = FIND_NAME.matcher(temp);
				Matcher matcherSurname = FIND_SURNAME.matcher(temp);
				Matcher matcherEndTag = FIND_END_TAG.matcher(temp);
				if (matcherName.find() && name == null) {
					name = matcherName.group().substring(1, matcherName.group().length() - 1);
				}
				if (matcherSurname.find() && surname == null) {
					surname = matcherSurname.group(1);
				}
				if (matcherEndTag.find()) {
					Matcher matcherForReplace = PATTERN_FOR_REPLACE.matcher(temp);
					String replacement;
					if (matcherForReplace.find()) {
						replacement = matcherForReplace.group();
						temp = temp.replaceAll(replacement, name + " " + surname + "\"");
					}
					bufferedWriter.write(temp);
					name = null;
					surname = null;
					temp = "";
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
