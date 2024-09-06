package wtf.emulator.data;

import com.google.gson.TypeAdapterFactory;
import com.ryanharter.auto.value.gson.GsonTypeAdapterFactory;

@GsonTypeAdapterFactory public abstract class EwTypeAdapterFactory implements TypeAdapterFactory {
  public static TypeAdapterFactory create() {
    return new AutoValueGson_EwTypeAdapterFactory();
  }
}
