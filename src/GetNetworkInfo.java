import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Retrieve address network information
 * 
 * <b>NOTE: In windows this code do not work as expect</b>
 */
public class GetNetworkInfo {
	/**
	 * Get all IPv4 addresses for this node
	 * 
	 * @return
	 * @throws SocketException
	 */
	public static final List<NetStruct> getAllIPAddress() throws SocketException {
		return iterateAll(WANT_IP, SKIP_LOOPBACK | SKIP_DOWN);
	}

	/**
	 * Get all Mac addresses for this node
	 * 
	 * @return
	 * @throws SocketException
	 */
	public static final List<NetStruct> getAllMacAddress() throws SocketException {
		return iterateAll(WANT_MAC, SKIP_LOOPBACK | SKIP_DOWN);
	}

	/**
	 * Get all Mac/IPv4 addresses for this node
	 * 
	 * @return
	 * @throws SocketException
	 */
	public static final List<NetStruct> getAllMacIPAddress() throws SocketException {
		return iterateAll(WANT_MAC | WANT_IP, SKIP_LOOPBACK | SKIP_DOWN);
	}

	private static final int WANT_MAC = 1;
	private static final int WANT_IP = 2;
	private static final int SKIP_DOWN = 1;
	private static final int SKIP_LOOPBACK = 2;

	private static final List<NetStruct> iterateAll(final int wantFlags, final int skipFlags)
			throws SocketException {
		final Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
		final ArrayList<NetStruct> list = new ArrayList<NetStruct>();
		while (e.hasMoreElements()) {
			final NetworkInterface n = e.nextElement();
			final String dev = n.getName();
			String mac = "00:00:00:00:00:00";
			List<String> ips = null;
			if (((skipFlags & SKIP_DOWN) != 0) && !n.isUp())
				continue;
			if (((skipFlags & SKIP_LOOPBACK) != 0) && n.isLoopback())
				continue;
			int got = 0;
			if ((wantFlags & WANT_MAC) != 0) {
				final byte[] ha = n.getHardwareAddress();
				if ((ha != null) && (ha.length > 3)) {
					mac = getStringMacAddress(ha);
					got++;
				}
			}
			if ((wantFlags & WANT_IP) != 0) {
				ips = new ArrayList<String>(1);
				final Enumeration<InetAddress> i = n.getInetAddresses();
				boolean isOK = false;
				while (i.hasMoreElements()) {
					final InetAddress ia = i.nextElement();
					if (ia instanceof Inet4Address) {
						ips.add(getStringIPAddress(ia));
						isOK = true;
					}
				}
				if (isOK) {
					got++;
				}
			}
			if (got > 0) {
				list.add(new NetStruct(dev, mac, ips));
			}
		}
		return list;
	}

	private static String getStringMacAddress(final byte[] input) {
		final StringBuilder mac = new StringBuilder();
		for (int i = 0; i < input.length; i++) {
			mac.append(String.format("%02X", input[i])).append(':');
		}
		if (mac.length() > 0)
			mac.setLength(mac.length() - 1);
		return mac.toString();
	}

	private static String getStringIPAddress(final InetAddress addr) {
		return addr.getHostAddress();
	}

	public static class NetStruct {
		public final String dev;
		public final String mac;
		public final List<String> ips;

		public NetStruct(final String dev, final String mac, final List<String> ips) {
			this.dev = dev;
			this.mac = mac;
			this.ips = (ips == null ? Collections.<String> emptyList() : Collections.unmodifiableList(ips));
		}

		public String toString() {
			return dev + " " + mac + " " + ips;
		}
	}

	public static void main(final String[] args) throws Throwable {
		System.out.println("getAllMacAddress()=" + getAllMacAddress());
		System.out.println("getAllIPAddress()=" + getAllIPAddress());
		System.out.println("getAllMacIPAddress()=" + getAllMacIPAddress());
	}
}
