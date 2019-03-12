package cs455.scaling.server.task;

import java.io.IOException;

/**
 * Public interface to delegate tasks to available working threads.
 * This can include; registering new clients with {@link Register},
 * reading data from clients via the {@link Receiver}, and sending
 * data back to clients with the {@link Sender}.
 * 
 * @author stock
 *
 */
public interface Task {

  /**
   * Main method that will be invoked when a working thread grabs a new
   * task off the queue.
   * 
   * @throws IOException
   */
  public void process();

}
