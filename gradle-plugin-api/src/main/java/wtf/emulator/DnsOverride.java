package wtf.emulator;

import com.google.auto.value.AutoValue;

import java.io.Serializable;

@AutoValue
public abstract class DnsOverride implements Serializable {
  public abstract String hostname();
  public abstract String ip();

  public static Builder builder() {
    return new AutoValue_DnsOverride.Builder();
  }

  public static DnsOverride create(String hostname, String ip) {
    return builder().hostname(hostname).ip(ip).build();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder hostname(String hostname);
    public abstract Builder ip(String ip);
    public abstract DnsOverride build();
  }
}
