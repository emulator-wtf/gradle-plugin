package wtf.emulator.junit;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JUnitResults {
  private final List<JUnitTestSuite> testSuites;

  public JUnitResults(List<JUnitTestSuite> testSuites) {
    this.testSuites = testSuites;
  }

  public List<JUnitTestSuite> getTestSuites() {
    return testSuites;
  }

  public JUnitResults plus(JUnitResults results) {
    List<JUnitTestSuite> newSuites =
      Stream.concat(testSuites.stream(), results.testSuites.stream())
        .collect(Collectors.groupingBy(
          JUnitTestSuite::getName,
          Collectors.reducing(JUnitTestSuite::plus)
        ))
        .values().stream()
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());

    return new JUnitResults(newSuites);
  }
}
