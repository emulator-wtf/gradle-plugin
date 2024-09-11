import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AppModuleTests {
  @Test
  public void junitWorks() {
    assertTrue("huh?", true);
  }

  @Test
  public void checkAlwaysTrue() {
    assertTrue("Always true was not true", new AlwaysTrue().check());
  }
}
