package fcampos.rawengine3D.io;



import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Little-Endian version of DataInputStream.
 * <p/>
 * Very similar to DataInputStream except it reads
 * little-endian instead of big-endian binary data. We can't extend
 * DataInputStream directly since it has only final methods, though
 * DataInputStream itself is not final. This forces us implement
 * LEDataInputStream with a DataInputStream object, and use wrapper methods.
 *
 * @author Roedy Green, Canadian Mind Products
 * @version 1.8 2007-05-24
 * @since 1998
 */
public final class LEDataInputStream extends DataInputStream implements DataInput
    {

    
     /**
     * work array for buffering input.
     *
     * @noinspection WeakerAccess
     */
    protected final byte[] work;
// -------------------------- PUBLIC STATIC METHODS --------------------------


    

// -------------------------- PUBLIC INSTANCE  METHODS --------------------------
    /**
     * constructor.
     *
     * @param in binary inputstream of little-endian data.
     */
    public LEDataInputStream( InputStream in )
    {
    	super(in);
        work = new byte[8];
    }

   
  

    /**
     * Read on char. like DataInputStream.readChar except little endian.
     *
     * @return little endian 16-bit unicode char from the stream.
     * @throws IOException if read fails.
     */
    public final char readLEChar() throws IOException
        {
        	readFully( work, 0, 2 );
        	return ( char ) ( ( work[ 1 ] & 0xff ) << 8 | ( work[ 0 ] & 0xff ) );
        }

    /**
     * Read a double. like DataInputStream.readDouble except little endian.
     *
     * @return little endian IEEE double from the datastream.
     * @throws IOException
     */
    public final double readLEDouble() throws IOException
        {
        	return Double.longBitsToDouble( readLong() );
        }

    /**
     * Read one float. Like DataInputStream.readFloat except little endian.
     *
     * @return little endian IEEE float from the datastream.
     * @throws IOException if read fails.
     */
    public final float readLEFloat() throws IOException
    {
        return Float.intBitsToFloat( readLEInt() );
    }

    
    
    /**
     * Read an int, 32-bits. Like DataInputStream.readInt except little endian.
     *
     * @return little-endian binary int from the datastream
     * @throws IOException if read fails.
     */
    public final int readLEInt() throws IOException
    {
    	readFully( work, 0, 4 );
        return ( work[ 3 ] & 0xff) << 24
               | ( work[ 2 ] & 0xff ) << 16
               | ( work[ 1 ] & 0xff ) << 8
               | ( work[ 0 ] & 0xff );
        
     }
    
    public int readByte2() throws IOException 
    {
    	readFully( work, 0, 1 );
    	
        int b1 = (work[0] & 0xFF);
      
        return (b1);
}
  
    
    public String readString() throws IOException 
    {
        String result = new String();
        byte inByte;
        while ( (inByte = (byte) read()) != 0)
            result += (char) inByte;
        
        return result;
    }
    
   
    /**
     * read a long, 64-bits.  Like DataInputStream.readLong except little endian.
     *
     * @return little-endian binary long from the datastream.
     * @throws IOException
     */
    public final long readLELong() throws IOException
        {
        readFully( work, 0, 8 );
        return ( long ) ( work[ 7 ] ) << 56
               |
               /* long cast needed or shift done modulo 32 */
               ( long ) ( work[ 6 ] & 0xff ) << 48
               | ( long ) ( work[ 5 ] & 0xff ) << 40
               | ( long ) ( work[ 4 ] & 0xff ) << 32
               | ( long ) ( work[ 3 ] & 0xff ) << 24
               | ( long ) ( work[ 2 ] & 0xff ) << 16
               | ( long ) ( work[ 1 ] & 0xff ) << 8
               | ( long ) ( work[ 0 ] & 0xff );
        }

    /**
     * Read short, 16-bits. Like DataInputStream.readShort except little endian.
     *
     * @return little endian binary short from stream.
     * @throws IOException if read fails.
     */
    public final char readLEShort() throws IOException
    {
        readFully( work, 0, 2 );
        return  (char) ( ( work[ 1 ] & 0xff ) << 8 | ( work[ 0 ] & 0xff ) );
    }

  
  

    /**
     * Read an unsigned short, 16 bits. Like DataInputStream.readUnsignedShort except little endian. Note, returns int
     * even though it reads a short.
     *
     * @return little-endian int from the stream.
     * @throws IOException if read fails.
     */
    public final int readLEUnsignedShort() throws IOException
    {
        readFully( work, 0, 2 );
        return ( ( work[ 1 ] & 0xff ) << 8 | ( work[ 0 ] & 0xff ) );
    }

    }