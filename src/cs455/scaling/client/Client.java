package cs455.scaling.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import cs455.scaling.util.Logger;
import cs455.scaling.util.TransmissionUtilities;

public class Client {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, true );

  private List<String> hashes;

  private SocketChannel channel;

  private ByteBuffer receivingBuffer;

  /**
   * Driver
   * 
   * @param args
   */
  public static void main(String[] args) {

    if ( args.length < 3 )
    {
      LOG.error( "USAGE: server-host server-port message-rate" );
      return;
    }
    LOG.info( "Client starting up at: " + new Date() );

    int serverPort, messageRate;

    try
    {
      serverPort = Integer.parseInt( args[ 1 ] );
      messageRate = Integer.parseInt( args[ 2 ] );
    } catch ( NumberFormatException e )
    {
      LOG.error( "Unable to parse command line arguments. " + e.getMessage() );
      return;
    }
    Client client;

    try
    {
      client = new Client( args[ 0 ], serverPort );
    } catch ( IOException e )
    {
      LOG.error( "Unable to initialize. " + e.getMessage() );
      return;
    }
    ( new Thread(
        new SenderThread( client.channel, messageRate, client.hashes ) ) )
            .start();
    client.read( messageRate );
  }

  /**
   * Client constructor which establishes a new connection with the
   * server (as specified by the arguments). Allocates memory for the
   * sending, and receiving buffer, as well as a new
   * <code>LinkedList</code> for the computed hashes.
   * 
   * @param serverHost
   * @param serverPort
   * @throws IOException
   */
  private Client(String serverHost, int serverPort) throws IOException {
    channel =
        SocketChannel.open( new InetSocketAddress( serverHost, serverPort ) );

    receivingBuffer = ByteBuffer.allocate( TransmissionUtilities.FOURTY_B );

    hashes = new LinkedList<>();
  }

  /**
   * Sender
   * 
   * @param messageRate
   */
  private void read(int messageRate) {
    while ( true )
    {
      try
      {
        channel.read( receivingBuffer );

        acknowledgeResponse();

        receivingBuffer.clear();
      } catch ( IOException e )
      {
        LOG.error( "Unable to send / receive message. " + e.getMessage() );
      }
    }
  }

  /**
   * Acknowledge a response and remove from the transmitted hashes if
   * found.
   * 
   */
  private void acknowledgeResponse() {
    String response = new String( receivingBuffer.array() ).trim();
    synchronized ( hashes )
    {
      if ( hashes.remove( response ) )
      {
        LOG.debug( "Sucessfully processed " + response );
      }
      LOG.debug(
          "There are still " + hashes.size() + " transmission to process." );
    }
  }
}
