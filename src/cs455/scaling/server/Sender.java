package cs455.scaling.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
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
public class Sender implements Task {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, false );

  private final byte[][] data;

  private final SocketChannel[] clients;

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
  public Sender(ServerStatistics statistics, List<byte[]> data,
      List<SocketChannel> clients) {
    this.statistics = statistics;
    this.data = data
        .toArray( new byte[ data.size() ][ TransmissionUtilities.EIGHT_KB ] );
    this.clients = clients.toArray( new SocketChannel[ clients.size() ] );
  }

  /**
   * Executes when a new thread becomes available, writing the data back
   * to the respective client. The hash of the each message will be
   * computed, and sent as the pay load back to the client.
   * 
   */
  @Override
  public void process() {
    for ( int i = 0; i < data.length; ++i )
    {
      String hash = TransmissionUtilities.SHA1FromBytes( data[ i ] );
      try
      {
        clients[ i ].write( ByteBuffer.wrap( hash.getBytes() ) );
      } catch ( IOException e )
      {
        LOG.error( e.getMessage() );
      }
      statistics.increment( clients[ i ] );
    }
  }
}
