package cs455.scaling.server;

import java.io.IOException;

/**
 * Public interface to delegate tasks to available working threads.
 * This can include reading new data from clients via the
 * {@link Reader}, and sending data back to clients with the
 * {@link Sender}.
 * 
 * @author stock
 *
 */
interface Task {

  /**
   * Main method that will be invoked when a working thread grabs a new
   * task off the queue.
   * 
   * @throws IOException
   */
  public void process() throws IOException;

}
