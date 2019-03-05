package cs455.scaling.server;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import cs455.scaling.server.task.Task;
import cs455.scaling.util.Logger;

/**
 * Working thread that processes objects off of the queue.
 * 
 * When a new task is available on the queue it will be processed by
 * the thread.
 * 
 * @author stock
 *
 */
class WorkerThread implements Runnable {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, false );

  private final LinkedBlockingQueue<Task> queue;

  private final int identifier;

  /**
   * Default constructor to construct the working thread, and hold a
   * reference to the queue.
   * 
   * @param queue
   */
  public WorkerThread(LinkedBlockingQueue<Task> queue, int identifier) {
    this.queue = queue;
    this.identifier = identifier;
  }

  /**
   * Run method that is executed when the thread is started by the
   * thread pool manager. The queue will be checked on each thread, and
   * if a task is present, it will be processed.
   * 
   */
  @Override
  public void run() {
    Task task;

    while ( true )
    {
      try
      {
        task = queue.take();
      } catch ( InterruptedException e )
      {
        LOG.error( "Unable to take message off queue: " + e.getMessage() );
        return;
      }

      LOG.debug(
          "Thread: " + Integer.toString( identifier ) + " is executing." );
      task.process();
    }
  }
}
