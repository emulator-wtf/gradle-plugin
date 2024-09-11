import static org.junit.Assert.assertTrue;

import org.junit.Test;

import wtf.emulator.test.AlwaysTrue;

public class TestModuleTests {
  @Test
  public void junitWorks() {
    assertTrue("huh?", true);
  }

  @Test
  public void checkTrue() {
    assertTrue("Always true is true", new AlwaysTrue().check());
  }
}
