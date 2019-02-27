package cs455.scaling.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import cs455.scaling.util.Logger;
import cs455.scaling.util.TransmissionUtilities;

/**
 * Only one server node in the system to manage incoming connections /
 * messages.
 * 
 * A server node will spawn a new thread pool manager with a set
 * number of threads. The following functionalities will be provided,
 * and rely on this thread pool:
 * 
 * <ul>
 * <li>Accept incoming network connections from the clients.</li>
 * <li>Accept incoming traffic from these connections.</li>
 * <li>Groups data from the clients together into batches.</li>
 * <li>Replies to clients by sending back a hash code for each message
 * provided.</li>
 * </ul>
 * 
 * @author stock
 *
 */
public class Server {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, false );

  private final ThreadPoolManager threadPoolManager;

  private List<byte[]> data;

  private List<SocketChannel> clients;

  private final ServerStatistics statistics;

  private final int batchSize;

  private final int batchTime;

  private long initTime;

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
    timer.schedule( server.statistics, 0, interval );

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
  public Server(int[] arguments) {
    this.data = new LinkedList<byte[]>();
    this.clients = new LinkedList<SocketChannel>();
    this.statistics = new ServerStatistics();
    this.threadPoolManager = new ThreadPoolManager( arguments[ 1 ] );
    this.batchSize = arguments[ 2 ];
    this.batchTime = arguments[ 3 ];
    this.initTime = System.nanoTime();
  }

  /**
   * Once the server object is configured, it can set up a new server
   * socket channel, and begin accepting new connections.
   * 
   * This method will continuously run accepting new connections, and
   * reading messages.
   * 
   * @param port specifies the port to which the server socket channel
   *        will be listening.
   * @throws IOException
   */
  private void start(int port) throws IOException {
    Selector selector = Selector.open();
    // TODO: Use the actual host
    // String host = InetAddress.getLocalHost().getHostName();
    String host = "localhost";
    ServerSocketChannel serverSocket = ServerSocketChannel.open();
    serverSocket.bind( new InetSocketAddress( host, 5001 ) );
    serverSocket.configureBlocking( false );

    serverSocket.register( selector, SelectionKey.OP_ACCEPT );

    while ( true )
    {
      selector.select();
      Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
      while ( iter.hasNext() )
      {

        SelectionKey key = iter.next();

        if ( key.isAcceptable() )
        {
          register( selector, serverSocket );
        }

        if ( key.isReadable() )
        {
          read( key );
        }
        iter.remove();
      }
    }
  }

  /**
   * Read incoming messages from a given channel, check if the client
   * has disconnected, or if there is data to be process.
   * 
   * @param key a token representing the registration of a
   *        SelectableChannel with a Selector.
   * @throws IOException
   */
  private void read(SelectionKey key) throws IOException {

    ByteBuffer buffer = ByteBuffer.allocate( TransmissionUtilities.EIGHT_KB );

    SocketChannel client = ( SocketChannel ) key.channel();
    int bytesRead = client.read( buffer );

    if ( bytesRead == -1 )
    {
      statistics.deregister( client );
      client.close();
      LOG.info( "Client disconnected" );
    } else
    {
      LOG.debug( "Received: "
          + TransmissionUtilities.SHA1FromBytes( buffer.array() ) );
      process( buffer, client );
    }
  }

  /**
   * Synchronized method to process incoming messages from a given
   * client. The data that was read into the buffer, and its respective
   * client is added to a linked list. A new task is created and added
   * to a queue for the thread pool manager to process with the data and
   * client information.
   * 
   * @param buffer containing the data pay load that is to be converted
   *        to a <code>byte[]</byte>.
   * @param client connection information for where to send the data
   *        back to.
   */
  private synchronized void process(ByteBuffer buffer, SocketChannel client) {
    data.add( buffer.array() );
    clients.add( client );
    // TODO: is it important to have own thread for checking timing?
    if ( data.size() == batchSize || ( ( int ) Math
        .round( ( System.nanoTime() - initTime ) / 1E9 ) == batchTime ) )
    {
      Task task = new Task( statistics, data, clients );
      try
      {
        threadPoolManager.addTask( task );
      } catch ( InterruptedException e )
      {
        LOG.error(
            "Unable to add task to thread pool queue. " + e.getMessage() );
      }
      initTime = System.nanoTime();
    }
    LOG.debug( "Unit Size: " + data.size() );
    buffer.clear();
  }

  /**
   * Invoked upon a new client registering itself with the server.
   * 
   * @param selector
   * @param serverSocket
   * @throws IOException
   */
  private void register(Selector selector, ServerSocketChannel serverSocket)
      throws IOException {

    SocketChannel client = serverSocket.accept();
    client.configureBlocking( false );
    client.register( selector, SelectionKey.OP_READ );
    statistics.register( client );

    LOG.info( "New client " + client.getRemoteAddress() + " has registered" );
  }
}
