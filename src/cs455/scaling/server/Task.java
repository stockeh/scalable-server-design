package cs455.scaling.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import cs455.scaling.util.Logger;
import cs455.scaling.util.TransmissionUtilities;

public class Task implements Runnable {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, true );

  private byte[][] unit;

  private SocketChannel[] clients;

  public Task(List<byte[]> unit, List<SocketChannel> clients) {
    this.unit = unit
        .toArray( new byte[ unit.size() ][ TransmissionUtilities.EIGHT_KB ] );
    unit.clear();
    this.clients = clients.toArray( new SocketChannel[ clients.size() ] );
    clients.clear();
  }

  @Override
  public void run() {
    for ( int i = 0; i < unit.length; ++i )
    {
      String hash = TransmissionUtilities.SHA1FromBytes( unit[ i ] );
      try
      {
        clients[ i ].write( ByteBuffer.wrap( hash.getBytes() ) );
      } catch ( IOException e )
      {
        LOG.error( e.getMessage() );
      }
    }
  }

}
