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
import java.util.List;
import java.util.Set;

import javax.management.AttributeValueExp;
import javax.management.BadAttributeValueExpException;
import javax.management.BadBinaryOpValueExpException;
import javax.management.BadStringOperationException;
import javax.management.InvalidApplicationException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Query;
import javax.management.QueryExp;
import javax.management.StringValueExp;
import javax.management.ValueExp;

/**
 * Proof of concept: Get Tomcat Connector Port from inner Servlet
 */
public class TomcatGetPortJMX {
	public static final List<String> getEndPoints() throws JMException, UnknownHostException {
		final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		// final QueryExp m = Query.match(Query.attr("protocol"), Query.value("HTTP/1.1"));
		final QueryExp m = Query.anySubString(new AttributeUpperValueExp("protocol"), Query.value("HTTP"));
		final Set<ObjectName> objs = mbs.queryNames(new ObjectName("*:type=Connector,*"), m);
		final String hostname = InetAddress.getLocalHost().getHostName();
		final InetAddress[] addresses = InetAddress.getAllByName(hostname);
		final ArrayList<String> endPoints = new ArrayList<String>();
		for (final ObjectName obj : objs) {
			final String scheme = mbs.getAttribute(obj, "scheme").toString();
			final String port = obj.getKeyProperty("port");
			for (final InetAddress addr : addresses) {
				if (addr.isAnyLocalAddress() || addr.isLoopbackAddress() || addr.isMulticastAddress())
					continue;
				final String host = addr.getHostAddress();
				final String ep = scheme + "://" + host + ":" + port;
				endPoints.add(ep);
			}
		}
		return endPoints;
	}

	public static class AttributeUpperValueExp extends AttributeValueExp {
		private static final long serialVersionUID = 42L;

		public AttributeUpperValueExp(final String attr) {
			super(attr);
		}

		@Override
		public ValueExp apply(final ObjectName name) throws BadStringOperationException,
				BadBinaryOpValueExpException, BadAttributeValueExpException, InvalidApplicationException {
			final ValueExp r = super.apply(name);
			if (r instanceof StringValueExp) {
				return new StringValueExp(((StringValueExp) r).getValue().toUpperCase());
			}
			return r;
		}
	}
}
