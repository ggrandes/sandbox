import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public final class ResponseHeadersFilter implements Filter {
	private Map<String, String> headers = null;

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		final LinkedHashMap<String, String> headers = 
				new LinkedHashMap<String, String>();
		final Enumeration<String> e = filterConfig.getInitParameterNames();
		while (e.hasMoreElements()) {
			final String name = e.nextElement();
			final String value = filterConfig.getInitParameter(name);
			headers.put(name, value);
		}
		this.headers = Collections.unmodifiableMap(headers);
	}

	@Override
	public void doFilter(final ServletRequest request, 
			final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {
		if (response instanceof HttpServletResponse) {
			final HttpServletResponse res = ((HttpServletResponse) response);
			for (final Entry<String, String> e : headers.entrySet()) {
				res.setHeader(e.getKey(), e.getValue());
			}
		}
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
	}
}
