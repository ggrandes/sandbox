import java.text.ParseException;

/**
 * Map Expression using System Properties
 * 
 * @link <a href="http://technobcn.wordpress.com/2013/09/30/java-expression-eval-system-property/">Java:
 *       Expression Eval (System Property)</a>
 */
public class MapExpression {
	public final String expression;
	private String mapped;

	/**
	 * Create Map Expression
	 * 
	 * @param expression
	 *            to map
	 */
	public MapExpression(final String expression) throws InvalidExpression {
		this.expression = expression;
		this.mapped = eval(expression);
	}

	/**
	 * Force reevaluate expression (if system property is changed after MapExpression was created)
	 * 
	 * @throws InvalidExpression
	 */
	public MapExpression reEval() throws InvalidExpression {
		mapped = eval(expression);
		return this;
	}

	/**
	 * Get mapped expression
	 * 
	 * @return evaluated expression
	 */
	public String get() {
		return mapped;
	}

	/**
	 * Evaluate input expression and return mapped expression
	 * 
	 * @param expression
	 * @return mapped expression
	 * @throws InvalidExpression
	 *             if expression is wrong
	 */
	String eval(final String expression) throws InvalidExpression {
		if (expression == null)
			throw new IllegalArgumentException();
		if (expression.isEmpty())
			return expression;
		// Find all ${system.property}
		final int len = expression.length();
		final StringBuilder sb = new StringBuilder(len);
		int last = 0;
		for (int i = 0; i < len; i++) {
			final char cbegin = expression.charAt(i);
			if (cbegin == '$' && ((i + 1) < len)) {
				final char cnext = expression.charAt(++i);
				if (cnext == '{') {
					sb.append(expression.substring(last, i - 1));
					last = i + 1;
					for (; i < len; i++) {
						final char cend = expression.charAt(i);
						if (cend == '}') {
							sb.append(mapToken(expression.substring(last, i)));
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
			sb.append(expression.substring(last, len));
		return sb.toString();
	}

	/**
	 * Map Token to System.getProperty()
	 * 
	 * @param property
	 *            name
	 * @return property value or null if not exists
	 * @throws InvalidExpression
	 *             if expression is wrong
	 */
	String mapToken(final String propName) throws InvalidExpression {
		if (propName.isEmpty())
			throw new InvalidExpression("Invalid property name (empty)", 0);
		return System.getProperty(propName);
	}

	public static class InvalidExpression extends ParseException {
		private static final long serialVersionUID = 42L;

		public InvalidExpression(final String s, final int errorOffset) {
			super(s, errorOffset);
		}
	}

	/**
	 * Simple Test
	 * 
	 * @param args
	 * @throws Throwable
	 */
	public static void main(final String[] args) throws Throwable {
		System.setProperty("user.state", "lucky");
		MapExpression m;
		m = new MapExpression("Hi ${user.name}, you are ${user.state}!!");
		System.out.println(m.get());
	}
}