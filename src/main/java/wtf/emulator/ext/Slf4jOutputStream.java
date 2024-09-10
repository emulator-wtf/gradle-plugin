/*
 * Copyright (C) 2014 ZeroTurnaround <support@zeroturnaround.com>
 * Contains fragments of code from Apache Commons Exec, rights owned
 * by Apache Software Foundation (ASF).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// original source from https://github.com/zeroturnaround/zt-exec/blob/6149c117ef835355c2e96bd331a3fa27b6a14394/src/main/java/org/zeroturnaround/exec/stream/slf4j/Slf4jOutputStream.java
package wtf.emulator.ext;

import org.slf4j.Logger;

/**
 * Output stream that writes to a given {@link Logger}.
 *
 * @author Rein Raudjärv
 */
public abstract class Slf4jOutputStream extends LogOutputStream {

  protected final Logger log;

  public Slf4jOutputStream(Logger logger) {
    this.log = logger;
  }

  public Logger getLogger() {
    return log;
  }

}
