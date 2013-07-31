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
//	<filter>
//	    <filter-name>DumpFilter</filter-name>
//	    <filter-class>com.package.DumpFilter</filter-class>
//	</filter>
//	<filter-mapping>
//	    <filter-name>DumpFilter</filter-name>
//	    <url-pattern>/*</url-pattern>
//	</filter-mapping>
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
 
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
 
public final class DumpFilter implements Filter {
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
    private volatile int id = 0;
 
    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }
 
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            final FilterChain chain) throws IOException, ServletException {
        final String myId = Integer.toHexString(++id | 0x80000000);
        final String ts = formatLocalDateTime(System.currentTimeMillis());
        final StringBuilder sb = new StringBuilder(1024);
        final Enumeration<String> e = request.getParameterNames();
 
        while (e.hasMoreElements()) {
            final String key = e.nextElement();
            final String[] values = request.getParameterValues(key);
            sb.setLength(0);
            for (final String value : values) {
                sb.append(ts).append(" DUMP(").append(myId).append(':');
                sb.append(request.getRemoteAddr()).append(':');
                sb.append(request.getRemotePort()).append("): ");
                sb.append(key).append('=').append(value).append('\n');
            }
            System.out.print(sb.toString());
        }
        chain.doFilter(request, response);
    }
 
    private String formatLocalDateTime(final long millis) {
        synchronized (sdf) {
            return sdf.format(new Date(millis));
        }
    }
 
    @Override
    public void destroy() {
    }
}