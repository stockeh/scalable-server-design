package cs455.scaling.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Random;
import cs455.scaling.util.Logger;
import cs455.scaling.util.TransmissionUtilities;

public class SenderThread implements Runnable {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, true );

  private final SocketChannel channel;

  private final int messageRate;

  private ByteBuffer sendingBuffer;

  private List<String> hashes;

  public SenderThread(SocketChannel channel, int messageRate,
      List<String> hashes) {
    this.channel = channel;
    this.messageRate = messageRate;
    this.hashes = hashes;
    this.sendingBuffer = ByteBuffer.allocate( TransmissionUtilities.EIGHT_KB );
  }

  @Override
  public void run() {
    int rounds = 100; // rounds-- > 0
    while ( rounds-- > 0 )
    {
      byte[] msg = new byte[ TransmissionUtilities.EIGHT_KB ];
      ( new Random() ).nextBytes( msg );
      sendingBuffer = ByteBuffer.wrap( msg );

      String hash = TransmissionUtilities.SHA1FromBytes( msg );
      synchronized ( hashes )
      {
        hashes.add( hash );
      }
      try
      {
        channel.write( sendingBuffer );

        sendingBuffer.clear();
      } catch ( IOException e )
      {
        LOG.error( "Unable to send / receive message. " + e.getMessage() );
      }
      try
      {
        Thread.sleep( 1000 / messageRate );
      } catch ( InterruptedException e )
      {
        LOG.error( "Interrupted and unable to sleep between transmissions."
            + e.getMessage() );
      }
    }
  }

}
