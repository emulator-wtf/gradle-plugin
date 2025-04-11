package wtf.emulator.junit;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nullable;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;

public class JUnitXmlParser {
  private static final SAXParserFactory spf = SAXParserFactory.newInstance();

  public static JUnitResults parseJUnitXml(InputStream inputStream) throws Exception {
    try (inputStream) {
      final JUnitResultsBuilder builder = new JUnitResultsBuilder();

      spf.newSAXParser().parse(inputStream, new DefaultHandler() {
        private boolean parsingFailure = false;
        private final StringBuilder failureBuilder = new StringBuilder();
        private boolean parsingSkipped = false;
        private final StringBuilder skippedBuilder = new StringBuilder();

        @Override
        public void startElement(@Nullable String uri, String localName, @Nullable String qName, Attributes attributes) {
          if ("testsuite".equals(qName)) {
            builder.visitTestsuite(attributes);
          } else if ("testcase".equals(qName)) {
            builder.visitTestcaseStart(attributes);
          } else if ("failure".equals(qName)) {
            parsingFailure = true;
          } else if ("skipped".equals(qName)) {
            parsingSkipped = true;
          }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
          if (parsingFailure) {
            failureBuilder.append(ch, start, length);
          } else if (parsingSkipped) {
            skippedBuilder.append(ch, start, length);
          }
        }

        @Override
        public void endElement(@Nullable String uri, @Nullable String localName, @Nullable String qName) {
          if ("testcase".equals(qName)) {
            builder.visitTestcaseEnd();
          } else if ("failure".equals(qName)) {
            builder.visitFailure(failureBuilder.toString().trim());
            parsingFailure = false;
            failureBuilder.setLength(0);
          } else if ("skipped".equals(qName)) {
            builder.visitSkipped(skippedBuilder.toString().trim());
            parsingSkipped = false;
            skippedBuilder.setLength(0);
          }
        }
      });

      return builder.build();
    }
  }
}
