package cs455.scaling.server;

import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
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

  private final Thread[] threads;

  private final LinkedBlockingQueue<Task> queue;
  
  private final List<byte[]> buffer;
  
  private final List<SocketChannel> clients;

  private long initTime;

  private final int batchSize;

  private final int batchTime;

  private final ServerStatistics statistics;
  

  /**
   * Default constructor that is to be created only once along with the
   * server. A specified number of threads are created that will hold a
   * reference to the queue.
   * 
   * @param arguments
   * @param statistics 
   */
  public ThreadPoolManager(int[] arguments, ServerStatistics statistics) {
    final int numberOfThreads = arguments[1];
    this.threads = new Thread[ numberOfThreads ];
    this.queue = new LinkedBlockingQueue<Task>();
    this.buffer = new LinkedList<byte[]>();
    this.clients = new LinkedList<SocketChannel>();
    
    this.statistics = statistics;
    this.initTime = System.nanoTime();
    this.batchSize = arguments[ 2 ];
    this.batchTime = arguments[ 3 ];

    for ( int i = 0; i < numberOfThreads; ++i )
    {
      threads[ i ] = new Thread( new WorkerThread( queue, i ) );
    }
  }

  /**
   * After the threads are all created in the constructor for this
   * object, they are started and looking each looking for a message to
   * take off the queue.
   * 
   */
  public void start() {
    for ( int i = 0; i < threads.length; ++i )
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
  public void addTask(Task task) throws InterruptedException {
    queue.put( task );
  }
  
  public synchronized void addUnit(byte[] payload, SocketChannel client) {
    buffer.add( payload );
    clients.add( client );
    if ( buffer.size() == batchSize || ( ( int ) Math
        .round( ( System.nanoTime() - initTime ) / 1E9 ) == batchTime ) )
    {
      Sender sender = new Sender( statistics, buffer, clients );
      try
      {
        addTask( sender );
      } catch ( InterruptedException e )
      {
        LOG.error(
            "Unable to add task to thread pool queue. " + e.getMessage() );
      }
      initTime = System.nanoTime();
    }
  }
  
}
