import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

/**
 * Like Spring context:property-placeholder for XML but for generic InputStream. This implementation read all
 * inputstream in memory and uses MapExpression for resolve placeholders.
 */
public class PlaceholderMemoryInputStream extends InputStream {
	public static final String ENABLE_TAG = "ENABLE-STREAM-PLACEHOLDER";
	public static final int ENABLE_TAG_DEEP = 64;
	final InputStream is;

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
	public PlaceholderMemoryInputStream(final InputStream is, final PlaceholderMapper mapper,
			final boolean useCommentForEnabling) throws IOException {
		this.is = wrap(is, mapper, useCommentForEnabling);
	}

	protected InputStream wrap(final InputStream is, final PlaceholderMapper mapper,
			final boolean useCommentForEnabling) throws IOException {
		String content = readFully(is);
		if (useCommentForEnabling) {
			if (content.substring(0, Math.min(content.length(), ENABLE_TAG_DEEP)).indexOf(ENABLE_TAG) != -1)
				content = rewrite(content, mapper);
		} else {
			content = rewrite(content, mapper);
		}
		return new ByteArrayInputStream(content.getBytes());
	}

	protected String readFully(final InputStream is) throws IOException {
		Reader in = null;
		try {
			final StringBuilder sb = new StringBuilder(1024);
			final char[] buf = new char[1024];
			in = new InputStreamReader(is);
			int len = -1;
			while ((len = in.read(buf)) != -1) {
				sb.append(buf, 0, len);
			}
			return sb.toString();
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (Exception ign) {
			}
			try {
				if (in != null)
					in.close();
			} catch (Exception ign) {
			}
		}
	}

	protected String rewrite(final String input, final PlaceholderMapper mapper) throws IOException {
		try {
			final MapExpression exp = new MapExpression(input) {
				@Override
				String mapToken(final String propName) throws InvalidExpression {
					final String value = mapper.mapPlaceHolder(propName);
					if (value == null)
						return "${" + propName + "}";
					return value;
				}
			};
			return exp.get();
		} catch (Exception e) {
			throw new IOException(e);
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
		return is.read();
	}

	@Override
	public int read(final byte[] b) throws IOException {
		return is.read(b);
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
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
		final PlaceholderMemoryInputStream is = new PlaceholderMemoryInputStream(new ByteArrayInputStream(memoryXML
				.toString().getBytes()), mapper, true);
		final byte[] bb = new byte[4096];
		final int read = is.read(bb);
		System.out.println(new String(bb, 0, read));
	}
}
