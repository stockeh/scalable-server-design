package cs455.scaling.server;

import java.util.concurrent.LinkedBlockingQueue;
import cs455.scaling.util.Logger;

/**
 * A manager for the thread pool that creates the specified number of
 * threads, and holds the queue of tasks that are needed to execute.
 * 
 * @author stock
 *
 */
public class ThreadPoolManager {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, true );

  private final int numberOfThreads;

  private final WorkerThread[] threads;

  private final LinkedBlockingQueue<Task> queue;

  /**
   * Default constructor that is to be created only once along with the
   * server. A specified number of threads are created that will hold a
   * reference to the queue.
   * 
   * @param numberOfThreads
   */
  public ThreadPoolManager(int numberOfThreads) {
    this.numberOfThreads = numberOfThreads;
    this.queue = new LinkedBlockingQueue<Task>();
    this.threads = new WorkerThread[ numberOfThreads ];

    for ( int i = 0; i < numberOfThreads; ++i )
    {
      threads[ i ] = new WorkerThread( queue );
    }
  }

  /**
   * After the threads are all created in the constructor for this
   * object, they are started and looking each looking for a message to
   * take off the queue.
   * 
   */
  public void start() {
    for ( int i = 0; i < numberOfThreads; ++i )
    {
      threads[ i ].start();
    }
  }

  /**
   * When a new task is created, it is added to the tail of the queue.
   * 
   * @param task
   * @throws InterruptedException
   */
  public void execute(Runnable task) throws InterruptedException {
    synchronized ( queue )
    {
      queue.put( ( Task ) task );
    }
  }
}
