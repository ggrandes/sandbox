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

//
//package org.apache.catalina.fake;
//
// In server.xml
//	Change this:
//	  <Service name="Catalina">
//	With this:
//	  <Service name="Catalina" className="org.apache.catalina.fake.TomcatCustomService">
//
import org.apache.catalina.core.StandardService;
import org.apache.catalina.connector.Connector;

/**
 * Proof of concept: Get Tomcat Connector Port from inner Servlet 
 */
public class TomcatCustomService extends StandardService {
	static {
		System.out.println("BEGIN: " + TomcatCustomService.class.getName());
	}

	@Override
	public void addConnector(final Connector connector) {
		super.addConnector(connector);
		System.out.println("addConnector: proto=" + connector.getProtocol() + " port=" + connector.getPort()
				+ " attr=" + connector.getAttribute("maxConnections"));
	}
}