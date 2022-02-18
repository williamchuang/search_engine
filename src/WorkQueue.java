import java.util.LinkedList;

public class WorkQueue {

	/**
	 * Pool of worker threads that will wait in the background until work is
	 * available.
	 */
	private final PoolWorker[] workers;

	/** Queue of pending work requests. */
	private final LinkedList<Runnable> queue;

	/** Used to signal the queue should be shutdown. */
	private volatile boolean shutdown;

	/** The default number of threads to use when not specified. */
	public static final int DEFAULT = 5;

	private int pending;

	/**
	 * Starts a work queue with the default number of threads.
	 * 
	 * @see #WorkQueue(int)
	 */
	public WorkQueue() {
		this(DEFAULT);
	}

	/**
	 * Starts a work queue with multiple threads.
	 *
	 * @param threads
	 *            number of worker threads to be used in the workQueue
	 */
	public WorkQueue(int threads) {
		this.pending = 0;
		this.shutdown = false;
		this.queue = new LinkedList<Runnable>();
		this.workers = new PoolWorker[threads];

		for (int i = 0; i < threads; i++) {
			workers[i] = new PoolWorker();
			workers[i].start();
		}
	}

	/**
	 * Adds a work request to the queue. A thread will process this request when
	 * available.
	 *
	 * @param runner
	 *            work request (in the form of a {@link Runnable} object)
	 */
	public void execute(Runnable runner) {
		increase();
		synchronized (queue) {
			queue.addLast(runner);
			queue.notifyAll();
		}
	}

	/**
	 * Finishes the queue by shutting it down
	 */
	public synchronized void finish() {
		try {
			while (pending > 0) {
				this.wait();
			}
		} catch (InterruptedException e) {
			System.out.println("");
		}

	}

	/**
	 * Asks the queue to shutdown. Any unprocessed work will not be finished,
	 * but threads in-progress will not be interrupted.
	 */
	public void shutdown() {
		finish();
		shutdown = true;
		synchronized (queue) {
			queue.notifyAll();
		}
	}

	/**
	 * Removes from the work queue.
	 */
	private synchronized void decrease() {
		assert pending > 0;
		pending--;
		if (pending <= 0) {
			this.notifyAll();
		}
	}

	/**
	 * Returns the number of worker threads being used by the work queue.
	 *
	 * @return number of worker threads
	 */
	public int size() {
		return workers.length;
	}

	/**
	 * Adds to the work queue.
	 */
	private synchronized void increase() {
		pending++;
	}

	/**
	 * When work is available in the work queue, it will remove the work and run
	 * it. If a shutdown is detected, will exit immediately. Threads will run in
	 * background until a shutdown is requested.
	 */
	private class PoolWorker extends Thread {

		@Override
		public void run() {

			Runnable runner = null;

			while (true) {
				synchronized (queue) {
					while (queue.isEmpty() && !shutdown) {
						try {
							queue.wait();
						} catch (InterruptedException ex) {
							System.err.println("The work queue was interrupted.");
							Thread.currentThread().interrupt();
						}
					}

					if (shutdown) {
						break;
					} else {
						runner = queue.removeFirst();
					}
				}

				try {
					runner.run();
				} catch (RuntimeException ex) {
					System.err.println("Error with WorkQueue");
				} finally {
					decrease();
				}
			}
		}
	}
}