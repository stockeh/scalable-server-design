package cs455.scaling.server;

import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import cs455.scaling.util.Logger;

/**
 * Server statistics for managing clients and throughput.
 * 
 * @author stock
 *
 */
public class ServerStatistics extends TimerTask {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, false );


  /**
   * Maintain a map of active clients, and the number of messages each
   * have sent.
   * 
   */
  private final ConcurrentHashMap<SocketChannel, LongAdder> map =
      new ConcurrentHashMap<>();

  /**
   * Add clients to a map for holding statistics.
   * 
   * @param client
   */
  public void register(SocketChannel client) {
    map.put( client, new LongAdder() );
  }

  /**
   * When a client disconnects it is removed from the map of statistics.
   * 
   * @param client
   */
  public void deregister(SocketChannel client) {
    if ( map.remove( client ) != null )
    {
      LOG.debug( "Client was successfully removed from statistics" );
    }
  }

  /**
   * Increment the value sent from a specific client.
   * 
   * @param client
   */
  public void increment(SocketChannel client) {
    map.computeIfAbsent( client, (v) -> new LongAdder() ).increment();
  }

  /**
   * Display the statistics for the current running server.
   * 
   * This display will include the following:
   * 
   * <ul>
   * <li>Server Throughput</li>
   * <li>Active Client Connections</li>
   * <li>Mean Per-client Throughput</li>
   * <li>Std. Dev. of Per-client Throughput</li>
   * </ul>
   * 
   */
  @Override
  public void run() {
    String timestamp =
        String.format( "%1$TF %1$TT", new Timestamp( new Date().getTime() ) );

    synchronized ( map )
    {
      double totalPerSecond =
          map.values().stream().mapToInt( i -> i.intValue() ).sum() / 20.0;

      double activeClients = map.size();

      double mean =
          ( activeClients != 0 ) ? totalPerSecond / activeClients
              : 0;

      System.out.println( "[" + timestamp + "]" + " Server Throughput: "
          + totalPerSecond + ", Active Client Connections: " + map.size()
          + ", Mean Per-client Throughput: " + mean + "\n" );

      // Reset all active clients to have sent zero messages.
      map.replaceAll( (k, v) -> new LongAdder() );
    }
  }

}
