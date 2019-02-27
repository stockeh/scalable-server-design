package cs455.scaling.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Random;
import cs455.scaling.util.Logger;
import cs455.scaling.util.TransmissionUtilities;

/**
 * The sender thread will run continuously sending messages from the
 * respective client to the server.
 * 
 * @author stock
 *
 */
public class SenderThread implements Runnable {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, true );

  private final SocketChannel channel;

  private final int messageRate;

  private ByteBuffer sendingBuffer;

  private final List<String> hashes;

  private final ClientStatistics statistics;

  /**
   * Default constructor to associate a specific client to the sending
   * thread. Once the object has been created, the thread can be stared.
   * @param statistics 
   * 
   * @param channel to associate where to send the message
   * @param messageRate rate, per-second, of sending messages
   * @param hashes stored in linked list to add hash of pay load
   */
  public SenderThread(ClientStatistics statistics, SocketChannel channel, int messageRate,
      List<String> hashes) {
    this.statistics = statistics;
    this.channel = channel;
    this.messageRate = messageRate;
    this.hashes = hashes;
    this.sendingBuffer = ByteBuffer.allocate( TransmissionUtilities.EIGHT_KB );
  }

  /**
   * Continuously running to send messages to the server.
   * 
   * New messages will be constructed with a random 8 KB array of bytes.
   * The hash is computed, stored in the linked list of hashes, and then
   * sent to the server. The buffer is cleared, and a new message
   * constructed.
   */
  @Override
  public void run() {
    // int rounds = 10; // rounds-- > 0
    while ( true )
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
        statistics.sent();
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
