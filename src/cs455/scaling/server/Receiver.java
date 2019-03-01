package cs455.scaling.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import cs455.scaling.util.Logger;
import cs455.scaling.util.TransmissionUtilities;

public class Receiver implements Task {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, false );

  private final ServerStatistics statistics;

  private final SelectionKey key;

  private final ThreadPoolManager threadPoolManager;

  public Receiver(ThreadPoolManager threadPoolManager,
      ServerStatistics statistics, SelectionKey key) {
    this.threadPoolManager = threadPoolManager;
    this.statistics = statistics;
    this.key = key;
  }

  /**
   * Read incoming messages from a given channel, check if the client
   * has disconnected, or if there is data to be process.
   * 
   * @throws IOException
   */
  @Override
  public void process() throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate( TransmissionUtilities.EIGHT_KB );

    SocketChannel client = ( SocketChannel ) key.channel();
    int bytesRead = 0;
    while ( buffer.hasRemaining() && bytesRead != -1 )
    {
      bytesRead = client.read( buffer );
    }

    if ( bytesRead == -1 )
    {
      statistics.deregister( client );
      client.close();
      LOG.info( "Client disconnected" );
    } else
    {
      threadPoolManager.addUnit( buffer.array(), client );
      key.interestOps( SelectionKey.OP_READ );
    }
    buffer.clear();
  }

}
