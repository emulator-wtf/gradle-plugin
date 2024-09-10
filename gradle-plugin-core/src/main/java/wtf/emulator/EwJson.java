package wtf.emulator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import wtf.emulator.data.EwTypeAdapterFactory;

public class EwJson {
  // single global Gson instance for all EW stuff
  public static final Gson gson = new GsonBuilder()
      .registerTypeAdapterFactory(EwTypeAdapterFactory.create())
      .setPrettyPrinting()
      .create();
}
