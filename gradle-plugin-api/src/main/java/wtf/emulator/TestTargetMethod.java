package wtf.emulator;

import com.google.auto.value.AutoValue;

import java.io.Serializable;

@AutoValue
public abstract class TestTargetMethod implements Serializable {
  public abstract String className();
  public abstract String method();

  @Override
  public String toString() {
    return className() + "#" + method();
  }

  public static TestTargetMethod.Builder builder() {
    return new AutoValue_TestTargetMethod.Builder();
  }

  public static TestTargetMethod create(String className, String method) {
    return builder().className(className).method(method).build();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract TestTargetMethod.Builder className(String className);
    public abstract TestTargetMethod.Builder method(String method);
    public abstract TestTargetMethod build();
  }
}
