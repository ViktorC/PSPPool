/*
 * Copyright 2017 Viktor Csomor
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
package net.viktorc.pp4j.api;

/**
 * An exception thrown when the execution of a submission is disrupted by an error unrelated to the submission itself.
 *
 * @author Viktor Csomor
 */
public class DisruptedExecutionException extends Exception {

  /**
   * Wraps the provided exception in a <code>DisruptedExecutionException</code>.
   *
   * @param e The cause exception.
   */
  public DisruptedExecutionException(Throwable e) {
    super(e);
  }

  /**
   * Creates a <code>DisruptedExecutionException</code> with the provided error message.
   *
   * @param message The error message describing the cause of the exception.
   */
  public DisruptedExecutionException(String message) {
    super(message);
  }

}
