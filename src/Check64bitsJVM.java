public class Check64bitsJVM {
	public static void main(final String[] args) throws Throwable {
		System.out.println("JVM is 64bits?: " + JVMis64bits());
	}
	public static boolean JVMis64bits() {
		final String propOsArch = System.getProperty("os.arch");
		final String propSunDataModel = System.getProperty("sun.arch.data.model");
		System.out.println("os.arch: " + propOsArch);
		System.out.println("sun.arch.data.model: " + propSunDataModel);
		// http://stackoverflow.com/questions/807263/how-do-i-detect-which-kind-of-jre-is-installed-32bit-vs-64bit
		if (propSunDataModel != null) {
			return propSunDataModel.equals("64");
		}
		if (propOsArch != null) {
			return propOsArch.contains("64");
		}
		return false;
	}
}
