package cs455.scaling.server.task;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import cs455.scaling.server.ServerStatistics;
import cs455.scaling.util.Logger;

/**
 * Task to delegate registration of a client with the server
 * (selector).
 * 
 * @author stock
 *
 */
public class Register implements Task {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, false );

  private final Selector selector;

  private final ServerSocketChannel serverSocket;

  private final SelectionKey key;

  /**
   * Default constructor - hold reference to the selector, server
   * socket, and key associated with a given client.
   * 
   * @param selector
   * @param serverSocket
   * @param key
   */
  public Register(Selector selector, ServerSocketChannel serverSocket,
      SelectionKey key) {
    this.selector = selector;
    this.serverSocket = serverSocket;
    this.key = key;
  }

  /**
   * Invoked upon a new client registering itself with the server, and
   * having an available working thread
   */
  @Override
  public void process() {
    SocketChannel client;
    try
    {
      client = serverSocket.accept();
      if ( client == null )
      {
        LOG.debug(
            "Null client caused a registration fault - to register, retrying." );
        key.attach( null );
        return;
      }
      client.configureBlocking( false );
      client.register( selector, SelectionKey.OP_READ );
    } catch ( IOException e )
    {
      LOG.error( "Thread pool is interrupted due to an issue: " + e.getMessage()
          + ", unable to register client with selector." );
      return;
    }

    ( ( ServerStatistics ) key.attachment() ).register( client );
    key.attach( null );
  }
}
