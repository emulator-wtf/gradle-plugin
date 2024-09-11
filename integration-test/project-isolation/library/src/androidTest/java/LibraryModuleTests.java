import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
