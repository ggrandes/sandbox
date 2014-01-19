import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Properties;

/**
 * Like Spring context:property-placeholder for XML but for generic InputStream. This implementation uses
 * PushbackInputStream for rewrite InputStream on-fly.
 */
public class PlaceholderPushbackInputStream extends InputStream {
	public static final String ENABLE_TAG = "ENABLE-STREAM-PLACEHOLDER";
	public static final int ENABLE_TAG_DEEP = 64;
	public static final int BUFFER_LENGTH = 4096;
	//
	final PushbackInputStream is;
	final PlaceholderMapper mapper;
	final boolean useCommentForEnabling;
	//
	final StringBuilder unreadBuffer = new StringBuilder();
	final StringBuilder placeHolderBuffer = new StringBuilder();
	//
	boolean skipProcessing = false;
	State state = null;
	int currentDeep = 0;
	int currentMatchTag = 0;

	/**
	 * if useCommentForEnabling=true the "text files" must be enabled with comment in the begin of
	 * stream (first 64bytes) like this:
	 * 
	 * <pre>
	 * XML:
	 * 
	 *   &lt;!-- ENABLE-STREAM-PLACEHOLDER --&gt;
	 *   &lt;servlet&gt;
	 *     &lt;servlet-name&gt;FooServlet&lt;/servlet-name&gt;
	 *     &lt;servlet-class&gt;com.acme.FooServlet&lt;/servlet-class&gt;
	 * 
	 *     &lt;init-param&gt;
	 *       &lt;param-name&gt;myParam&lt;/param-name&gt;
	 *       &lt;param-value&gt;${placeholderMyParam}&lt;/param-value&gt;
	 *     &lt;/init-param&gt;
	 *   &lt;/servlet&gt;
	 * 
	 * Properties:
	 * 
	 *   ### ENABLE-STREAM-PLACEHOLDER ###
	 *   myOtherParam = ${placeholderForMyOtherParam}
	 * 
	 * INI file:
	 * 
	 *   ;;; ENABLE-STREAM-PLACEHOLDER ;;;
	 *   myOtherParam = ${placeholderForMyOtherParam}
	 *   
	 * jSON file:
	 * 
	 * {
	 *   "___ENABLE-STREAM-PLACEHOLDER___": "yes",
	 *   "myOtherParam": "${placeholderForMyOtherParam}"
	 * }
	 * </pre>
	 * 
	 * @param is the original InputStream
	 * @param mapper
	 * @param useCommentForEnabling true for conditional parsing, false=always parse
	 * @throws IOException
	 */
	public PlaceholderPushbackInputStream(final InputStream is, final PlaceholderMapper mapper,
			final boolean useCommentForEnabling) throws IOException {
		this.is = new PushbackInputStream(is, BUFFER_LENGTH);
		this.mapper = mapper;
		this.useCommentForEnabling = useCommentForEnabling;
		this.state = (useCommentForEnabling ? State.WANT_TAG : State.WANT_DOLAR_SIGN);
	}

	/**
	 * Internal logic for processing of bytes
	 * 
	 * @param b
	 * @throws IOException
	 */
	protected void processByte(final int b) throws IOException {
		switch (state) {
			case WANT_TAG:
				unreadBuffer.append((char) b);
				if (++currentDeep > ENABLE_TAG_DEEP) {
					skipProcessing = true;
					break;
				}
				if (b == ENABLE_TAG.charAt(currentMatchTag)) {
					if (++currentMatchTag >= ENABLE_TAG.length()) {
						state = State.WANT_DOLAR_SIGN;
					}
				} else {
					currentMatchTag = 0;
				}
				break;
			case WANT_DOLAR_SIGN:
				if (b == '$') {
					state = State.WANT_BRACE_BEGIN;
				} else {
					unreadBuffer.append((char) b);
				}
				break;
			case WANT_BRACE_BEGIN:
				if (b == '{') {
					state = State.WANT_BRACE_END;
				} else {
					state = State.WANT_BRACE_BEGIN;
					unreadBuffer.append((char) b);
				}
				break;
			case WANT_BRACE_END:
				if (b == '}') {
					state = State.WANT_DOLAR_SIGN;
					final String propName = placeHolderBuffer.toString();
					String value = mapper.mapPlaceHolder(propName);
					if (value == null)
						value = "${" + propName + "}";
					System.out.println("mapping name=" + propName + " value=" + value);
					unreadBuffer.append(value);
					placeHolderBuffer.setLength(0);
				} else {
					placeHolderBuffer.append((char) b);
				}
				break;
		}
	}

	@Override
	public void close() throws IOException {
		is.close();
	}

	@Override
	public int available() throws IOException {
		return is.available();
	}

	@Override
	public int read() throws IOException {
		if (skipProcessing)
			return is.read();
		processByte(is.read());
		if (unreadBuffer.length() > 0) {
			is.unread(unreadBuffer.toString().getBytes());
			unreadBuffer.setLength(0);
		}
		return is.read();
	}

	@Override
	public int read(final byte[] b) throws IOException {
		if (skipProcessing)
			return is.read(b);
		if (b != null) {
			final byte[] buf = new byte[b.length];
			int read = is.read(buf);
			for (int i = 0; i < read; i++) {
				processByte(buf[i]);
			}
			if (unreadBuffer.length() > 0) {
				is.unread(unreadBuffer.toString().getBytes());
				unreadBuffer.setLength(0);
			}
		}
		return is.read(b);
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		if (skipProcessing)
			return is.read(b, off, len);
		if (b != null) {
			final byte[] buf = new byte[len];
			int read = is.read(buf);
			for (int i = 0; i < read; i++) {
				processByte(buf[i]);
			}
			if (unreadBuffer.length() > 0) {
				is.unread(unreadBuffer.toString().getBytes());
				unreadBuffer.setLength(0);
			}
		}
		return is.read(b, off, len);
	}

	@Override
	public long skip(final long n) throws IOException {
		return is.skip(n);
	}

	@Override
	public boolean markSupported() {
		return is.markSupported();
	}

	@Override
	public synchronized void mark(final int readlimit) {
		is.mark(readlimit);
	}

	@Override
	public synchronized void reset() throws IOException {
		is.reset();
	}

	/**
	 * Test ${placeholder} rest of text
	 */
	private static enum State {
		/**
		 * we are searching tag
		 * 
		 * @see PlaceholderPushbackInputStream#ENABLE_TAG
		 */
		WANT_TAG,
		/**
		 * we expect first '$'
		 */
		WANT_DOLAR_SIGN,
		/**
		 * we expect next '{'
		 */
		WANT_BRACE_BEGIN,
		/**
		 * we expect last '}'
		 */
		WANT_BRACE_END;
	}

	/**
	 * Interface for callback user for mapping placeholders
	 */
	public static interface PlaceholderMapper {
		public String mapPlaceHolder(final String name);
	}

	/**
	 * Simple Test
	 */
	public static void main(final String[] args) throws Throwable {
		final StringBuilder memoryProperties = new StringBuilder();
		memoryProperties.append("ph1=value1").append('\n');
		memoryProperties.append("ph2=value2").append('\n');
		final Properties base = new Properties();
		base.load(new ByteArrayInputStream(memoryProperties.toString().getBytes()));
		final PlaceholderMapper mapper = new PlaceholderMapper() {
			@Override
			public String mapPlaceHolder(final String name) {
				return base.getProperty(name);
			}
		};
		final StringBuilder memoryXML = new StringBuilder();
		memoryXML.append("<!-- ENABLE-STREAM-PLACEHOLDER -->").append('\n');
		memoryXML.append("<servlet>").append('\n');
		memoryXML.append("  <servlet-name>FooServlet</servlet-name>").append('\n');
		memoryXML.append("  <servlet-class>com.acme.FooServlet</servlet-class>").append('\n');
		memoryXML.append("  <init-param>").append('\n');
		memoryXML.append("    <param-name>myParam1</param-name>").append('\n');
		memoryXML.append("    <param-value>${ph1}</param-value>").append('\n');
		memoryXML.append("  </init-param>").append('\n');
		memoryXML.append("  <init-param>").append('\n');
		memoryXML.append("    <param-name>myParam3</param-name>").append('\n');
		memoryXML.append("    <param-value>${ph3}</param-value>").append('\n');
		memoryXML.append("  </init-param>").append('\n');
		memoryXML.append("</servlet>").append('\n');
		final PlaceholderPushbackInputStream is = new PlaceholderPushbackInputStream(
				new ByteArrayInputStream(memoryXML.toString().getBytes()), mapper, true);
		final byte[] bb = new byte[4096];
		final int read = is.read(bb);
		System.out.println(new String(bb, 0, read));
	}
}
