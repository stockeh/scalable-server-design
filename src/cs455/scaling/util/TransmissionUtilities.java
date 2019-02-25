package cs455.scaling.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilities class that are shared between the client and the server.
 * 
 * @author stock
 *
 */
public class TransmissionUtilities {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, true );

  /**
   * Buffer size for the pay load (value) from the client to the server.
   */
  public static final int EIGHT_KB = 8000;

  /**
   * Buffer size for the pay load (hash) from the server to the client.
   */
  public static final int FORTY_B = 40;

  /**
   * Computes the SHA-1 hash of a byte array to a <code>String</code>.
   * The returned value will be left padded with zeros if less than
   * forty bytes.
   * 
   * @param data as an array of bytes
   * @return its representation as a hex string
   */
  public static String SHA1FromBytes(byte[] data) {
    MessageDigest digest = null;
    String algorithm = "SHA1";
    try
    {
      digest = MessageDigest.getInstance( algorithm );
    } catch ( NoSuchAlgorithmException e )
    {
      LOG.error( "No Such Algorithm, " + algorithm + " " + e.getMessage() );
      return "ERROR";
    }
    byte[] hash = digest.digest( data );
    BigInteger hashInt = new BigInteger( 1, hash );
    return String.format( "%40s", hashInt.toString( 16 ) ).replace( ' ', '0' );
  }
}
