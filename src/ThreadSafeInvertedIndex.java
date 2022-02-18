import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.nio.file.Path;

public class ThreadSafeInvertedIndex extends InvertedIndex {

	private ReadWriteLock lock;

	public ThreadSafeInvertedIndex() {
		super();
		this.lock = new ReadWriteLock();
	}

	@Override
	public void add(String word, String text, int position, TreeMap<String, TreeSet<Integer>> path_position) {
		lock.lockReadWrite();
		try {
			super.add(word, text, position, path_position);

		} finally {
			lock.unlockReadWrite();
		}
	}

	@Override
	public void print(Path path) {
		lock.lockReadOnly();
		try {
			super.print(path);

		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public boolean containsWord(String word) {
		lock.lockReadOnly();
		try {
			return super.containsWord(word);

		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public int size() {
		lock.lockReadOnly();
		try {
			return super.size();

		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public List<SearchResult> exactForage(String[] querywords) {
		lock.lockReadOnly();
		try {
			return super.exactForage(querywords);

		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public List<SearchResult> partialForage(String[] querywords) {
		lock.lockReadOnly();
		try {
			return super.partialForage(querywords);

		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public String toString() {
		lock.lockReadOnly();
		try {
			return super.toString();

		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public void addIndex(InvertedIndex partialindex) {
		lock.lockReadWrite();
		try {
			super.addIndex(partialindex);

		} finally {
			lock.unlockReadWrite();
		}
	}

	@Override
	public void addAll(InvertedIndex other) {
		lock.lockReadWrite();
		try {
			super.addAll(other);
		} finally {
			lock.unlockReadWrite();
		}
	}

}
