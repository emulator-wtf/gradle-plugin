package wtf.emulator;

public class GradleCompat_6_1 implements GradleCompat {
  @Override
  public boolean isConfigurationCacheEnabled() {
    // config cache starting from 6.6
    return false;
  }
}
