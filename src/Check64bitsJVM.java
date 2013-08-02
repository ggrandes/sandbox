import java.lang.management.ManagementFactory;

public class Check64bitsJVM {
	public static void main(final String[] args) throws Throwable {
		System.out.println("JVM is 64bits?: " + JVMis64bits());
	}

	public static boolean JVMis64bits() {
		// http://stackoverflow.com/questions/807263/how-do-i-detect-which-kind-of-jre-is-installed-32bit-vs-64bit
		try {
			final String propSunDataModel = System.getProperty("sun.arch.data.model");
			if (propSunDataModel != null) {
				return propSunDataModel.equals("64");
			}
		} catch (Exception ign) {
		}
		try {
			final String propOsArch = System.getProperty("os.arch");
			if (propOsArch != null) {
				return propOsArch.contains("64");
			}
		} catch (Exception ign) {
		}
		try {
			final String jmxArchJMX = ManagementFactory.getOperatingSystemMXBean().getArch();
			if (jmxArchJMX != null) {
				return jmxArchJMX.contains("64");
			}
		} catch (Exception ign) {
		}
		return false;
	}
}
