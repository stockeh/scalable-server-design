package cs455.scaling.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import cs455.scaling.util.Logger;
import cs455.scaling.util.TransmissionUtilities;

/**
 * New tasks, containing a list of data, will be processed and sent
 * back to the clients.
 * 
 * A task contains the data and the respective clients for where to
 * respond to data. When a new thread is available in thread pool
 * manager, a thread will execute this task.
 * 
 * @author stock
 *
 */
public class Task {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, false );

  private final List<SelectionKey> data;

  private final ServerStatistics statistics;

  /**
   * Default constructor to build a new task. Data and clients are
   * associated with this task, and will run when a new thread becomes
   * available.
   * 
   * @param statistics
   * 
   * @param data a list of <code>byte[]</code> that will be converted to
   *        a <code>byte[][]</code>.
   * @param clients the associated socket channels for each of the
   *        messages.
   */
  public Task(ServerStatistics statistics, List<SelectionKey> data) {
    this.statistics = statistics;
    this.data = new LinkedList<SelectionKey>( data );
    
    data.clear();
  }

  /**
   * Executes when a new thread becomes available, writing the data back
   * to the respective client. The hash of the each message will be
   * computed, and sent as the pay load back to the client.
   * 
   */
  public void process() {
    ByteBuffer buffer = ByteBuffer.allocate( TransmissionUtilities.EIGHT_KB );

    for ( SelectionKey key : this.data )
    {
      SocketChannel client = ( SocketChannel ) key.channel();

      buffer.clear();
      int bytesRead = 0;
      while ( buffer.hasRemaining() && bytesRead != -1 )
      {
        try
        {
          bytesRead = client.read( buffer );
        } catch ( IOException e )
        {
          LOG.error( "Failed to read data from client: " + e.getMessage() );
        }
      }

      String hash = TransmissionUtilities.SHA1FromBytes( buffer.array() );

      try
      {
        client.write( ByteBuffer.wrap( hash.getBytes() ) );
      } catch ( IOException e )
      {
        LOG.error( "Failed to write to client: " + e.getMessage() );
      }
      statistics.increment( client );
    }
  }
}
