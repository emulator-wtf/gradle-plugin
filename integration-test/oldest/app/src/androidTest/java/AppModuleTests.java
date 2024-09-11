import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import wtf.emulator.test.AlwaysTrue;

public class AppModuleTests {
  @Test
  public void junitWorks() {
    assertTrue("huh?", true);
  }

  @Test
  public void checkTrue() {
    assertTrue("Always true is not true", new AlwaysTrue().check());
  }

  @Test
  public void checkFalse() {
    assertFalse("Always false is false", new AlwaysFalse().check());
  }
}
