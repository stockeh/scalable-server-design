package cs455.scaling.server;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Working thread that processes objects off of the queue.
 * 
 * When a new task is available on the queue it will be processed by
 * the thread.
 * 
 * @author stock
 *
 */
class WorkerThread extends Thread {

  private final LinkedBlockingQueue<Task> queue;

  /**
   * Default constructor to construct the working thread, and hold a
   * reference to the queue.
   * 
   * @param queue
   */
  public WorkerThread(LinkedBlockingQueue<Task> queue) {
    this.queue = queue;
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
        e.printStackTrace();
        return;
      }
      try
      {
        task.run();
      } catch ( RuntimeException e )
      {
        System.out.println(
            "Thread pool is interrupted due to an issue: " + e.getMessage() );
      }
    }
  }
}
