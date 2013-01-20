import java.io.File;
import java.io.IOException;
import java.util.HashSet;

/**
 * Auto clean old temp files
 */
public class FileAutoCleaner {
	final static FileAutoCleaner singleton = new FileAutoCleaner();
	final HashSet<File> bag = new HashSet<File>();

	public static FileAutoCleaner getInstance() {
		return singleton;
	}

	public synchronized File createTempFile(String prefix, String suffix) throws IOException {
		File tmp = File.createTempFile(prefix, suffix);
		tmp.deleteOnExit();
		bag.add(tmp);
		return tmp;
	}

	public synchronized void cleanOldFiles(final int secondsOld) {
		long now = (System.currentTimeMillis() / 1000);
		for (File f : bag) {
			long expired = (f.lastModified() / 1000) + secondsOld;
			if (now >= expired) {
				System.out.println("Deleted file=" + f.getAbsolutePath());
				f.delete();
				bag.remove(f);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		FileAutoCleaner fac = FileAutoCleaner.getInstance();
		System.out.println(System.currentTimeMillis() / 1000);
		fac.createTempFile("deleteme", "tmp");
		for (int i = 0; i < 5; i++) {
			System.out.println(System.currentTimeMillis() / 1000);
			// delete if older than 2 seconds
			fac.cleanOldFiles(2);
			Thread.sleep(1000);
		}
	}

}
