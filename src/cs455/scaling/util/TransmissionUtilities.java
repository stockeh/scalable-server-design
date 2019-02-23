package cs455.scaling.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TransmissionUtilities {

  /**
   * Have the ability to log output INFO, DEBUG, ERROR configured by
   * Logger(INFO, DEBUG) and LOGGER#MASTER for ERROR settings.
   */
  private static final Logger LOG = new Logger( true, true );

  public static final int EIGHT_KB = 8000;

  public static final int FOURTY_B = 40;

  /**
   * computes the SHA-1 hash of a byte array to a <code>String</code>.
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
    return String.format("%40s", hashInt.toString( 16 )).replace(' ', '0');
  }


}
