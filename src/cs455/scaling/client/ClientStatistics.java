package cs455.scaling.client;

import java.sql.Timestamp;
import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hold statistics for the client that pertain to the number of sent
 * and received messages.
 * 
 * @author stock
 *
 */
public class ClientStatistics extends TimerTask {

  private final AtomicInteger sent = new AtomicInteger( 0 );

  private final AtomicInteger received = new AtomicInteger( 0 );

  /**
   * Increment the number of <b>sent</b> messages for a given client.
   * 
   */
  public void sent() {
    sent.incrementAndGet();
  }

  /**
   * Increment the number of <b>received</b> messages for a given
   * client.
   * 
   */
  public void received() {
    received.incrementAndGet();
  }

  /**
   * Allows the client to print the number of messages it has sent and
   * received during the last N seconds.
   * 
   * Scheduled in a parent class similar with:
   * 
   * Timer timer = new Timer(); timer.schedule(this, 0, N);
   * 
   */
  @Override
  public void run() {
    String timestamp =
        String.format( "%1$TF %1$TT", new Timestamp( new Date().getTime() ) );
    System.out.println( "[" + timestamp + "]" + " Total Sent Count: "
        + sent.get() + ", Total Received Count: " + received.get() + "\n");
  }
}
