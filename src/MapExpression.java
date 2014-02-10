/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Map Expression using System Properties, HashMap, and others
 * 
 * @link <a href="http://technobcn.wordpress.com/2013/09/30/java-expression-eval-system-property/">Java:
 *       Expression Eval (System Property)</a>
 */
public class MapExpression {
	public final String expression;
	private String mapped;
	private Mapper preMapper = null;
	private Mapper postMapper = null;
	private final ArrayList<Token> tokens = new ArrayList<Token>();
	private final StringBuilder buffer = new StringBuilder();

	/**
	 * Create Map Expression without evaluate
	 * 
	 * @param expression
	 *            to map
	 * @see #MapExpression(String, boolean)
	 */
	public MapExpression(final String expression) throws InvalidExpression {
		this(expression, null, SystemPropertyMapper.getInstance(), false);
	}

	/**
	 * Create Map Expression
	 * 
	 * @param expression
	 *            to map
	 * @param preMapper
	 *            mapper for parameters
	 * @param postMapper
	 *            mapper for parameters
	 * @param evalInit
	 *            false to skip evaluation on creation
	 * @throws InvalidExpression
	 * @see #setPostMapper(Mapper)
	 */
	public MapExpression(final String expression, final Mapper preMapper, final Mapper postMapper,
			final boolean evalInit) throws InvalidExpression {
		this.expression = expression;
		this.preMapper = preMapper;
		this.postMapper = postMapper;
		parseExpression(expression);
		if (evalInit)
			eval();
	}

	/**
	 * Create Map Expression
	 * 
	 * @param expression
	 *            to map
	 * @param postMap
	 *            map for parameters
	 * @param evalInit
	 * @throws InvalidExpression
	 */
	public MapExpression(final String expression, final Map<String, String> postMap, final boolean evalInit)
			throws InvalidExpression {
		this(expression, null, new MultiMapper().add(new MapMapper(postMap)).add(
				SystemPropertyMapper.getInstance()), evalInit);
	}

	/**
	 * Set post mapper for parameters
	 * 
	 * @param postMapper
	 */
	public void setPostMapper(final Mapper postMapper) {
		this.postMapper = postMapper;
	}

	/**
	 * Force reevaluate expression (if system property is changed after MapExpression was created)
	 * 
	 * @throws InvalidExpression
	 */
	public MapExpression eval() throws InvalidExpression {
		buffer.setLength(0);
		final int len = tokens.size();
		for (int i = 0; i < len; i++) {
			final Token tok = tokens.get(i);
			buffer.append(tok.isString ? tok.token : mapTokenPost(tok.token));
		}
		mapped = buffer.toString();
		return this;
	}

	/**
	 * Get mapped expression
	 * 
	 * @return evaluated expression
	 * @see #eval()
	 */
	public String get() {
		return mapped;
	}

	/**
	 * Parse input expression
	 * 
	 * @param expression
	 * @throws InvalidExpression
	 *             if expression is wrong
	 */
	void parseExpression(final String expression) throws InvalidExpression {
		if (expression == null)
			throw new IllegalArgumentException();
		tokens.clear();
		if (expression.isEmpty())
			return;
		// Find all ${tag}
		final int len = expression.length();
		int last = 0;
		for (int i = 0; i < len; i++) {
			final char cbegin = expression.charAt(i);
			if (cbegin == '$' && ((i + 1) < len)) {
				final char cnext = expression.charAt(++i);
				if (cnext == '{') {
					tokens.add(new Token(expression.substring(last, i - 1), true));
					last = i + 1;
					for (; i < len; i++) {
						final char cend = expression.charAt(i);
						if (cend == '}') {
							tokens.add(new Token(mapTokenPre(expression.substring(last, i)), false));
							last = i + 1;
							break;
						}
					}
					if ((i == len) && (last <= len))
						throw new InvalidExpression("Not well ended expression: "
								+ expression.substring(last - 2, len), len);
				}
			}
		}
		if (last < len)
			tokens.add(new Token(expression.substring(last, len), true));
	}

	/**
	 * Map Token Pre eval (when parseExpression() is called)
	 * 
	 * @param name
	 * @return value or name if not found
	 * @throws InvalidExpression
	 *             if expression is wrong
	 */
	String mapTokenPre(final String name) throws InvalidExpression {
		if (name.isEmpty())
			throw new InvalidExpression("Invalid name (empty)", 0);
		if (preMapper != null) {
			return preMapper.map(name);
		}
		return name;
	}

	/**
	 * Map Token Post parse (when eval() is called)
	 * 
	 * @param name
	 * @return value or name if not found
	 * @throws InvalidExpression
	 *             if expression is wrong
	 */
	String mapTokenPost(final String name) throws InvalidExpression {
		if (name.isEmpty())
			throw new InvalidExpression("Invalid name (empty)", 0);
		if (postMapper != null) {
			return postMapper.map(name);
		}
		return name;
	}

	@Override
	public String toString() {
		return super.toString() + " [expression=" + expression + "]";
	}

	public static class InvalidExpression extends ParseException {
		private static final long serialVersionUID = 42L;

		public InvalidExpression(final String s, final int errorOffset) {
			super(s, errorOffset);
		}
	}

	private static final class Token {
		public final String token;
		public final boolean isString;

		public Token(final String token, final boolean isToken) {
			this.token = token;
			this.isString = isToken;
		}
	}

	public static interface Mapper {
		public String map(final String input);
	}

	public static class MapMapper implements Mapper {
		private final Map<String, String> map;

		public MapMapper(final Map<String, String> map) {
			this.map = map;
		}

		@Override
		public String map(final String input) {
			if (map != null) {
				final String m = map.get(input);
				if (m != null)
					return m;
			}
			return null;
		}
	}

	public static class SystemPropertyMapper implements Mapper {
		private static final SystemPropertyMapper singleton = new SystemPropertyMapper();

		private SystemPropertyMapper() {
		}

		@Override
		public String map(final String propName) {
			return System.getProperty(propName);
		}

		public static SystemPropertyMapper getInstance() {
			return singleton;
		}
	}

	public static class MultiMapper implements Mapper {
		private final List<Mapper> mappers;

		public MultiMapper() {
			this(new ArrayList<Mapper>());
		}

		public MultiMapper(final ArrayList<Mapper> mappers) {
			this.mappers = mappers;
		}

		public MultiMapper(final CopyOnWriteArrayList<Mapper> mappers) {
			this.mappers = mappers;
		}

		public MultiMapper(final Vector<Mapper> mappers) {
			this.mappers = mappers;
		}

		public MultiMapper add(final Mapper m) {
			mappers.add(m);
			return this;
		}

		@Override
		public String map(final String input) {
			final int len = mappers.size();
			for (int i = 0; i < len; i++) {
				final Mapper m = mappers.get(i);
				final String result = m.map(input);
				if (result != null)
					return result;
			}
			return null;
		}
	}

	/**
	 * Simple Test
	 * 
	 * @param args
	 * @throws Throwable
	 */
	public static void main(final String[] args) throws Throwable {
		MapExpression m;
		System.setProperty("user.state", "lucky");
		Map<String, String> map = new HashMap<String, String>();
		map.put("static", "ouh yeah!");
		m = new MapExpression("Hi ${user.name}, you are ${user.state}!! ${static}", map, true);
		System.out.println(m.get());
		//
		final int TOTAL = (int) 1e6;
		long begin = 0;
		System.out.println("TOTAL=" + TOTAL);
		// Benchmark Parse+Eval
		begin = System.currentTimeMillis();
		for (int i = 0; i < TOTAL; i++) {
			new MapExpression("Hi ${user.name}, you are ${user.state}!! ${static}", map, true).get();
		}
		System.out.println("benchmark parse+eval=" + (System.currentTimeMillis() - begin));
		// Benchmark Parse+Eval
		begin = System.currentTimeMillis();
		m = new MapExpression("Hi ${user.name}, you are ${user.state}!! ${static}", map, true);
		for (int i = 0; i < TOTAL; i++) {
			m.eval().get();
		}
		System.out.println("benchmark eval=" + (System.currentTimeMillis() - begin));
	}
}
