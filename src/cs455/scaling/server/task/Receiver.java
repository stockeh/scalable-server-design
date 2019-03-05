package cs455.scaling.server.task;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import cs455.scaling.server.ServerStatistics;
import cs455.scaling.server.ThreadPoolManager;
import cs455.scaling.util.Logger;
import cs455.scaling.util.TransmissionUtilities;

/**
 * Processes data as received from the clients.
 * 
 * The server will check a set of SelectionKeys, and upon one being
 * readable, a new receiver will be made to manage that data. In turn
 * adding the data to a collection of buffered data received from all
 * clients in the system.
 * 
 * @author stock
 *
 */
public class Receiver implements Task {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, false );

  private final SelectionKey key;

  private final ThreadPoolManager threadPoolManager;

  /**
   * Default constructor - save reference to thread pool, statistics,
   * and key ( associated with client ).
   * 
   * @param threadPoolManager
   * @param statistics
   * @param key
   */
  public Receiver(ThreadPoolManager threadPoolManager, SelectionKey key) {
    this.threadPoolManager = threadPoolManager;
    this.key = key;
  }

  /**
   * Read incoming messages from a given channel, check if the client
   * has disconnected, or if there is data to be process. Attach a null
   * object
   * 
   */
  @Override
  public void process() {
    ByteBuffer buffer = ByteBuffer.allocate( TransmissionUtilities.EIGHT_KB );

    SocketChannel client = ( SocketChannel ) key.channel();

    int bytesRead = 0;
    try
    {
      while ( buffer.hasRemaining() && bytesRead != -1 )
      {
        bytesRead = client.read( buffer );
      }
    } catch ( IOException e )
    {
      LOG.error( "Failed to read data from client. " + e.getMessage() );
      ( ( ServerStatistics ) key.attachment() ).deregister( client );
      return;
    }
    if ( bytesRead == -1 )
    {
      ( ( ServerStatistics ) key.attachment() ).deregister( client );
      try
      {
        client.close();
      } catch ( IOException e )
      {
        LOG.error( "Unable to close client connection: " + e.getMessage() );
        return;
      }

      LOG.info( "Client disconnected." );
    } else
    {
      threadPoolManager.addUnit( buffer.array(), client );
    }
    key.attach( null );
  }

}
