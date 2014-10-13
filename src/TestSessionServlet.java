/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
// In web.xml:
//	<servlet>
//		<servlet-name>test</servlet-name>
//		<servlet-class>com.acme.TestSessionServlet</servlet-class>
//		<load-on-startup>1</load-on-startup>
//	</servlet>
//	<servlet-mapping>
//		<servlet-name>test</servlet-name>
//		<url-pattern>/*</url-pattern>
//	</servlet-mapping>
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Example of Session Servlet
 */
public class TestSessionServlet extends HttpServlet {
	private static final long serialVersionUID = 42L;
	private static final String COUNTER_ATTR = TestSessionServlet.class.getName() + ".counter";

	@Override
	protected void service(final HttpServletRequest req, final HttpServletResponse res)
			throws ServletException, IOException {
		final PrintWriter out = res.getWriter();
		final HttpSession session = req.getSession();
		final AtomicInteger counter = getCounter(session);
		final String text = "OK:" + session.getId() + ":" + counter.incrementAndGet() + "\r\n";
		res.setContentType("text/plain");
		res.setContentLength(text.length());
		out.print(text);
		out.flush();
	}

	private AtomicInteger getCounter(final HttpSession session) {
		synchronized (session) {
			AtomicInteger counter = (AtomicInteger) session.getAttribute(COUNTER_ATTR);
			if (counter == null) {
				counter = new AtomicInteger();
				session.setAttribute(COUNTER_ATTR, counter);
			}
			return counter;
		}
	}
}
