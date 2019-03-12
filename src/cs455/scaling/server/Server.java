package cs455.scaling.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import cs455.scaling.server.task.Receiver;
import cs455.scaling.util.Logger;

/**
 * Only one server node in the system to manage incoming connections /
 * messages.
 * 
 * A server node will spawn a new thread pool manager with a set
 * number of threads. The following functionalities will be provided,
 * and rely on this thread pool:
 * 
 * <ul>
 * <li>Accept incoming traffic from these connections.</li>
 * <li>Groups data from the clients together into batches.</li>
 * <li>Replies to clients by sending back a hash code for each message
 * provided.</li>
 * </ul>
 * 
 * @author stock
 *
 */
class Server {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, false );

  private final ThreadPoolManager threadPoolManager;

  private final ServerStatistics statistics;

  /**
   * Entry point for the server, specifying the configuration via the
   * command arguments.
   * 
   * @param args command line arguments include; port-number,
   *        tread-pool-size, batch-size, and batch-time
   */
  public static void main(String[] args) {
    if ( args.length < 4 )
    {
      LOG.error( "USAGE: port-numumber tread-pool-size batch-size batch-time" );
      return;
    }
    LOG.info( "Server starting up at: " + new Date() );

    int[] arguments = new int[ 4 ];
    for ( int i = 0; i < 4; ++i )
    {
      arguments[ i ] = Integer.parseInt( args[ i ] );
    }

    Server server = new Server( arguments );

    server.threadPoolManager.start();

    Timer timer = new Timer();
    final int interval = 20000; // 20 seconds in milliseconds
    timer.schedule( server.statistics, 1000, interval );

    try
    {
      server.start( arguments[ 0 ] );
    } catch ( NumberFormatException | IOException e )
    {
      LOG.error( "Unable to initialize. " + e.getMessage() );
      return;
    }
  }

  /**
   * Server constructor to initialize the object configuration. The
   * thread pool manager is constructed at this point.
   * 
   * @param arguments
   */
  private Server(int[] arguments) {
    this.statistics = new ServerStatistics();
    this.threadPoolManager = new ThreadPoolManager( arguments, statistics );
  }

  /**
   * Once the server object is configured, it can set up a new server
   * socket channel, and begin accepting new connections.
   * 
   * @param port specifies the port to which the server socket channel
   *        will be listening.
   * @throws IOException
   */
  private void start(int port) throws IOException {
    Selector selector = Selector.open();
    String host = InetAddress.getLocalHost().getHostName();

    LOG.info( "Server starting on host: " + host + ", port: "
        + Integer.toString( port ) );

    ServerSocketChannel serverSocket = ServerSocketChannel.open();
    serverSocket.bind( new InetSocketAddress( host, port ) );
    serverSocket.configureBlocking( false );

    serverSocket.register( selector, SelectionKey.OP_ACCEPT );

    try
    {
      process( selector, serverSocket );
    } catch ( InterruptedException e )
    {
      LOG.error( "Failed to receive messages. Exiting application with error: "
          + e.getMessage() );
      return;
    }
  }

  /**
   * This method will continuously run accepting new connections, and
   * reading messages. These actions are managed by the thread pool.
   * 
   * @param selector
   * @param serverSocket
   * @throws IOException
   * @throws InterruptedException
   */
  private void process(Selector selector, ServerSocketChannel serverSocket)
      throws IOException, InterruptedException {
    while ( true )
    {
      selector.select();
      Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
      while ( iter.hasNext() )
      {

        SelectionKey key = iter.next();

        if ( key.isAcceptable() )
        {
          register( key, selector, serverSocket );
        }

        else if ( key.isReadable() && key.attachment() == null )
        {
          key.attach( statistics );
          threadPoolManager.addTask( new Receiver( threadPoolManager, key ) );
        }
        iter.remove();
      }
    }
  }

  /**
   * Invoked upon a new client registering itself with the server. The
   * server can perform this more efficiently than the thread pool,
   * hence the local operation.
   * 
   * @param key associated selection key to get client
   * @param selector used to register key with
   * @param serverSocket accepts the connection to client
   */
  private void register(SelectionKey key, Selector selector,
      ServerSocketChannel serverSocket) {
    SocketChannel client;
    try
    {
      client = serverSocket.accept();
      client.configureBlocking( false );
      client.register( selector, SelectionKey.OP_READ );
    } catch ( IOException e )
    {
      LOG.error( "Thread pool is interrupted due to an issue: " + e.getMessage()
          + ", unable to register client with selector." );
      return;
    }
    statistics.register( client );
  }
}
