import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestModuleTests {

  @Test
  public void checkAlwaysTrue() {
    assertTrue("Always true was not true", new AlwaysTrue().check());
  }
}
