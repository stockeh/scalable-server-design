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
import cs455.scaling.util.Logger;
import cs455.scaling.util.TransmissionUtilities;

public class Server {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, true );

  private final ThreadPoolManager threadPoolManager;

  private List<byte[]> unit;

  private List<SocketChannel> clients;

  private final int batchSize;

  private final int batchTime;

  private long initTime;

  /**
   * Driver
   * 
   * @param args
   */
  public static void main(String[] args) {
    if ( args.length < 4 )
    {
      LOG.error( "USAGE: portnum tread-pool-size batch-size batch-time" );
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
    try
    {
      server.start( arguments[ 0 ] );
    } catch ( NumberFormatException | IOException e )
    {
      LOG.error( "Unable to initialize. " + e.getMessage() );
      return;
    }
  }

  public Server(int[] arguments) {
    this.unit = new LinkedList<byte[]>();
    this.clients = new LinkedList<SocketChannel>();
    this.threadPoolManager = new ThreadPoolManager( arguments[ 1 ] );
    this.batchSize = arguments[ 2 ];
    this.batchTime = arguments[ 3 ];
    this.initTime = System.nanoTime();
  }

  /**
   * Processor
   * 
   * @param port
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
   * Read and respond to messages
   * 
   * @param key
   * @throws IOException
   */
  private void read(SelectionKey key) throws IOException {

    ByteBuffer buffer = ByteBuffer.allocate( TransmissionUtilities.EIGHT_KB );

    SocketChannel client = ( SocketChannel ) key.channel();
    int bytesRead = client.read( buffer );

    if ( bytesRead == -1 )
    {
      client.close();
      LOG.info( "Client disconnected" );
    } else
    {
      LOG.debug( "Received: "
          + TransmissionUtilities.SHA1FromBytes( buffer.array() ) );
      process( buffer, client );
    }
  }

  private synchronized void process(ByteBuffer buffer, SocketChannel client) {
    unit.add( buffer.array() );
    clients.add( client );
    if ( unit.size() == batchSize || ( ( int ) Math
        .round( ( System.nanoTime() - initTime ) / 1E9 ) == batchTime ) )
    {
      Task task = new Task( unit, clients );
      try
      {
        threadPoolManager.execute( task );
      } catch ( InterruptedException e )
      {
        e.printStackTrace();
      }
      initTime = System.nanoTime();
    }
    buffer.clear();
  }

  /**
   * 
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
    LOG.info( "New client " + client.getRemoteAddress() + " has registered" );
  }

}
