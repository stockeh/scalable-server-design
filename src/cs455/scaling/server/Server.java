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
import cs455.scaling.util.Logger;
import cs455.scaling.util.TransmissionUtilities;

public class Server {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, true );

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

    Server server = new Server();
    try
    {
      server.start( Integer.parseInt( args[ 0 ] ) );
    } catch ( NumberFormatException | IOException e )
    {
      LOG.error( "Unable to initialize. " + e.getMessage() );
      return;
    }
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
          readAndRespond( key );
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
  private static void readAndRespond(SelectionKey key) throws IOException {

    ByteBuffer buffer = ByteBuffer.allocate( TransmissionUtilities.EIGHT_KB );

    SocketChannel client = ( SocketChannel ) key.channel();
    int bytesRead = client.read( buffer );

    if ( bytesRead == -1 )
    {
      client.close();
      LOG.info( "Client disconnected" );
    } else
    {
      String hash = TransmissionUtilities.SHA1FromBytes( buffer.array() );
      LOG.debug( "Received: " + hash );

      buffer.flip();
      client.write( ByteBuffer.wrap( hash.getBytes() ) );
      buffer.clear();
    }
  }

  /**
   * 
   * 
   * e8ec30fb368fc802ce200304478effc9da5aca1c
   * e8ec30fb368fc802ce200304478effc9da5aca1c
   * 
   * @param selector
   * @param serverSocket
   * @throws IOException
   */
  private static void register(Selector selector,
      ServerSocketChannel serverSocket) throws IOException {

    SocketChannel client = serverSocket.accept();
    client.configureBlocking( false );
    client.register( selector, SelectionKey.OP_READ );
    LOG.info( "New client " + client.getRemoteAddress() + " has registered" );
  }

}
