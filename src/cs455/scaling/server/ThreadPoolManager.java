package cs455.scaling.server;

import java.util.concurrent.LinkedBlockingQueue;
import cs455.scaling.util.Logger;

public class ThreadPoolManager {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, true );

  private final int numberOfThreads;
  private final WorkerThread[] threads;
  private final LinkedBlockingQueue<Task> queue;

  public ThreadPoolManager(int numberOfThreads) {
    this.numberOfThreads = numberOfThreads;
    this.queue = new LinkedBlockingQueue<Task>();
    this.threads = new WorkerThread[ numberOfThreads ];

    for ( int i = 0; i < numberOfThreads; ++i )
    {
      threads[ i ] = new WorkerThread( queue );
    }
  }

  public void start() {
    for ( int i = 0; i < numberOfThreads; ++i )
    {
      threads[ i ].start();
    }
  }

  public void execute(Runnable task) throws InterruptedException {
    synchronized ( queue )
    {
      queue.put( ( Task ) task );
    }
  }
}
