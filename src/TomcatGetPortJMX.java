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
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Query;

/**
 * Proof of concept: Get Tomcat Connector Port from inner Servlet 
 */
public class TomcatGetPortJMX {
	public static final List<String> getEndPoints() throws JMException, UnknownHostException {
		final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		final Set<ObjectName> objs = mbs.queryNames(new ObjectName("*:type=Connector,*"),
				Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
		final String hostname = InetAddress.getLocalHost().getHostName();
		final InetAddress[] addresses = InetAddress.getAllByName(hostname);
		final ArrayList<String> endPoints = new ArrayList<String>();
		for (Iterator<ObjectName> i = objs.iterator(); i.hasNext();) {
			final ObjectName obj = i.next();
			final String scheme = mbs.getAttribute(obj, "scheme").toString();
			final String port = obj.getKeyProperty("port");
			for (InetAddress addr : addresses) {
				final String host = addr.getHostAddress();
				final String ep = scheme + "://" + host + ":" + port;
				endPoints.add(ep);
			}
		}
		return endPoints;
	}
}
