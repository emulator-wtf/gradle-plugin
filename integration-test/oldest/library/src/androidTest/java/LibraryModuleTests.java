import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class LibraryModuleTests {
  @Test
  public void junitWorks() {
    assertTrue("huh?", true);
  }

  @Test
  public void checkFalse() {
    assertFalse("Always false is false", new AlwaysFalse().check());
  }
}
