import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class MultithreadedInvertedIndexBuilder {

	/**
	 * The Multithreaded version of the method in InvertedIndexBuilder. Receives the
	 * path and an inverted index object, and open files from the given path.
	 * 
	 * @param path
	 *            the path of the file that is going to be opened
	 * @param I
	 *            which is the data structure
	 * @throws IOException
	 */
	public static void dirTraverser(Path dir, ThreadSafeInvertedIndex i, WorkQueue workers) {
		try {
			if (Files.isDirectory(dir)) {
				traverse(dir, i, workers);
			} else {

				if (dir.getFileName().toString().trim().toLowerCase().endsWith(".html")) {
					workers.execute(new BuildRunner(dir, i));
				}
				if (dir.getFileName().toString().trim().toLowerCase().endsWith(".htm")) {
					workers.execute(new BuildRunner(dir, i));
				}
			}
		} catch (IOException e) {
			System.out.println("IOException - Input file doesn't exist");
		}
	}

	/**
	 * 
	 * Recursively searches the input directory for other directories and files,
	 * which are parsed by different threads using multithreading
	 * 
	 * @param originalPath
	 *            the original directory to be searched
	 * @param index
	 *            the global inverted index
	 * @param workers
	 *            the work queue
	 */
	public static void traverse(Path originalPath, ThreadSafeInvertedIndex index, WorkQueue workers)
			throws IOException {

		try (DirectoryStream<Path> mylist = Files.newDirectoryStream(originalPath)) {
			for (Path element : mylist) {
				if (Files.isDirectory(element)) {
					dirTraverser(element, index, workers);
				} else {
					if (element.getFileName().toString().trim().toLowerCase().endsWith(".html")) {
						workers.execute(new BuildRunner(element, index));
					}
					if (element.getFileName().toString().trim().toLowerCase().endsWith(".htm")) {
						workers.execute(new BuildRunner(element, index));
					}
				}
			}
		}
		workers.finish();
	}

	private static class BuildRunner implements Runnable {
		private ThreadSafeInvertedIndex global;
		private InvertedIndex local;
		private Path file;

		public BuildRunner(Path file, ThreadSafeInvertedIndex index) {
			this.file = file;
			this.global = index;
			local = new InvertedIndex();
		}

		@Override
		public void run() {
			try {
				InvertedIndexBuilder.fileParser(this.file, local);
			} catch (IOException e) {
				e.printStackTrace();
			}
			global.addIndex(local);
		}
	}

}
