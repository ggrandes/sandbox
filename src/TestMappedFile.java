import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Random;

/**
 * Why not use java mapped files? Because they are unpredictable!
 * <br/> In Windows:
 * <br/> 		java.lang.OutOfMemoryError: Map failed
 * <br/> In Linux: 
 * <br/> 		can work, if you are lucky ;-)
 * <br/> References:
 * <br/> <a href="http://bugs.sun.com/view_bug.do?bug_id=4724038">Sun Bug</a>
 * <br/> <a href="http://stackoverflow.com/questions/2972986/how-to-unmap-a-file-from-memory-mapped-using-filechannel-in-java">How unmap? You can't</a>
 */
public class TestMappedFile {
	private static final Random r = new Random();
	private static final long filesize = 128 * 1024 * 1024;

	public static void main(String[] args) throws Exception {
		try {
			for (int i = 1; i < 128; i++) {
				System.out.println("Creating: " + i + " with size " + filesize);
				create(i);
			}
		} catch (Throwable t) {
			t.printStackTrace(System.out); // WTF? Yeah... mmap sucks
		}
	}
	private static void create(final int i) throws IOException {
		final File file = new File("/tmp/data/mmap." + i + ".tmp");
		final RandomAccessFile raf = new RandomAccessFile(file, "rw"); // RandomAccessFile allows both read and write
		raf.setLength(filesize);
		MappedByteBuffer buf = null;
		try {
			final int offset = (r.nextInt() & 0xFFFF);
			final byte b = (byte) (r.nextInt() & 0xFF);
			final FileChannel channel = raf.getChannel();
			buf = channel.map(MapMode.READ_WRITE, 0L, channel.size()-1); // Section of file to map
			// do something with buf
			buf.clear();
			write(buf, 0, b);
			write(buf, offset, b);
			write(buf, buf.limit()-1, b);
			buf.force();
			channel.force(false);
		} finally {
			//unmapMmaped(buf); // Non-portable clean mmaped
			raf.close(); // Mapping remains until file is closed
			buf = null;
			if (!file.delete()) {
				System.out.println("WTF? Can not delete file " + file.getAbsolutePath());
			}
		}
	}
	private static void write(final ByteBuffer buf, final int offset, final byte b) {
		System.out.println("Writing " + b + " in offset " + offset + "/" + buf.limit());
		buf.put(offset, b);
	}
	// Can throw: java.lang.Error: Cleaner terminated abnormally
	//private static void unmapMmaped(final ByteBuffer buf) {
	//	if ((buf != null) && (buf instanceof sun.nio.ch.DirectBuffer)) {
	//		final sun.misc.Cleaner cleaner = ((sun.nio.ch.DirectBuffer) buf).cleaner();
	//		cleaner.clean();
	//	}
	//}	
}
