package net.viktorc.pp4j;

import net.viktorc.pp4j.api.ProcessManagerFactory;
import net.viktorc.pp4j.api.ProcessPool;
import net.viktorc.pp4j.api.jpp.JavaProcessOptions;
import net.viktorc.pp4j.api.jpp.ProcessPoolExecutorService;
import net.viktorc.pp4j.impl.StandardProcessPool;
import net.viktorc.pp4j.impl.jpp.JavaProcessPoolExecutorService;

/**
 * A class for convenience and factory methods for creating instances of implementations of the 
 * {@link net.viktorc.pp4j.api.ProcessPool} interface.
 * 
 * @author Viktor Csomor
 *
 */
public class ProcessPools {

	/**
	 * Only static methods...
	 */
	private ProcessPools() {
		
	}
	/**
	 * Returns a pool of processes. The initial size of the pool is the greater of the minimum pool size and the 
	 * reserve size. This method blocks until the initial number of processes started up. The size of the pool is 
	 * dynamically adjusted based on the pool parameters and the rate of incoming submissions. It is a convenience 
	 * method for the constructor
	 * {@link net.viktorc.pp4j.impl.StandardProcessPool#StandardProcessPool(ProcessManagerFactory, int, int, int, boolean)} 
	 * with <code>verbose</code> set to <code>false</code>.
	 * 
	 * @param managerFactory  A {@link net.viktorc.pp4j.api.ProcessManagerFactory} instance that is used to build 
	 * {@link net.viktorc.pp4j.api.ProcessManager} instances that manage the processes' life cycle in the pool.
	 * @param minPoolSize The minimum size of the process pool.
	 * @param maxPoolSize The maximum size of the process pool.
	 * @param reserveSize The number of available processes to keep in the pool.
	 * @return A pool of process executors each hosting a process.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the processes to start up.
	 */
	public static ProcessPool newCustomProcessPool(ProcessManagerFactory managerFactory, int minPoolSize, 
			int maxPoolSize, int reserveSize) throws InterruptedException {
		return new StandardProcessPool(managerFactory, minPoolSize, maxPoolSize, reserveSize, false);
	}
	/**
	 * Returns a pool of a fixed number of processes. It is a convenience method for calling
	 * {@link #newCustomProcessPool(ProcessManagerFactory, int, int, int)} with <code>minPoolSize</code> 
	 * and <code>maxPoolSize</code> equal and a <code>reserveSize</code> of <code>0</code>. The number of 
	 * executors in the pool is always kept at the specified value.
	 * 
	 * @param managerFactory  A {@link net.viktorc.pp4j.api.ProcessManagerFactory} instance that is used to build 
	 * {@link net.viktorc.pp4j.api.ProcessManager} instances that manage the processes' life cycle in the pool.
	 * @param size The number of processes to maintain in the pool.
	 * @return A fixed size pool of process executors.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the processes to start up.
	 */
	public static ProcessPool newFixedProcessPool(ProcessManagerFactory managerFactory, int size)
			throws InterruptedException {
		return newCustomProcessPool(managerFactory, size, size, 0);
	}
	/**
	 * Returns a pool of processes that grows in size as required. It is a convenience method for calling 
	 * {@link #newCustomProcessPool(ProcessManagerFactory, int, int, int)} with <code>0</code> as 
	 * the <code>minPoolSize</code> and the <code>reserveSize</code>, and <code>Integer.MAX_VALUE</code> as the 
	 * maximum pool size.
	 * 
	 * @param managerFactory  A {@link net.viktorc.pp4j.api.ProcessManagerFactory} instance that is used to build 
	 * {@link net.viktorc.pp4j.api.ProcessManager} instances that manage the processes' life cycle in the pool.
	 * @return An unbounded pool of process executors.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the processes to start up.
	 */
	public static ProcessPool newCachedProcessPool(ProcessManagerFactory managerFactory)
			throws InterruptedException {
		return newCustomProcessPool(managerFactory, 0, Integer.MAX_VALUE, 0);
	}
	/**
	 * Returns a fixed size pool holding a single process. It is a convenience method for calling the method
	 * {@link #newFixedProcessPool(ProcessManagerFactory, int)} with <code>size</code> set to <code>1
	 * </code>.
	 * 
	 * @param managerFactory  A {@link net.viktorc.pp4j.api.ProcessManagerFactory} instance that is used to build 
	 * {@link net.viktorc.pp4j.api.ProcessManager} instances that manage the processes' life cycle in the pool.
	 * @return A pool holding a single process executor.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the processes to start up.
	 */
	public static ProcessPool newSingleProcessPool(ProcessManagerFactory managerFactory)
			throws InterruptedException {
		return newFixedProcessPool(managerFactory, 1);
	}
	/**
	 * Returns a custom process pool using Java processes. The initial size of the pool is the greater of the 
	 * minimum pool size and the reserve size. This method blocks until the initial number of processes started 
	 * up. The size of the pool is dynamically adjusted based on the pool parameters and the rate of incoming 
	 * submissions. It is a convenience method for the constructor
	 * {@link net.viktorc.pp4j.impl.jpp.JavaProcessPoolExecutorService#JavaProcessPoolExecutorService(JavaProcessOptions, int, int, int, boolean)} 
	 * with <code>verbose</code> set to <code>false</code>.
	 * 
	 * @param options The options for the "java" program used to create the new JVM. If it is null, no options 
	 * are used.
	 * @param minPoolSize The minimum size of the process pool.
	 * @param maxPoolSize The maximum size of the process pool.
	 * @param reserveSize The number of available processes to keep in the pool.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the core threads to start 
	 * up.
	 * @throws IllegalArgumentException If the manager factory is null, or the minimum pool size is less than 0, or 
	 * the maximum pool size is less than the minimum pool size or 1, or the reserve size is less than 0 or greater 
	 * than the maximum pool size.
	 */
	public static ProcessPoolExecutorService newCustomProcessPoolExecutorService(JavaProcessOptions options, 
			int minPoolSize, int maxPoolSize, int reserveSize)
					throws InterruptedException {
		return new JavaProcessPoolExecutorService(options, minPoolSize, maxPoolSize, reserveSize, false);
	}
	/**
	 * Returns a custom process pool using Java processes. The initial size of the pool is the greater of the 
	 * minimum pool size and the reserve size. This method blocks until the initial number of processes started 
	 * up. The size of the pool is dynamically adjusted based on the pool parameters and the rate of incoming 
	 * submissions. It is a convenience method for 
	 * {@link #newCustomProcessPoolExecutorService(JavaProcessOptions, int, int, int)} with <code>options</code> 
	 * set to <code>null</code>.
	 * 
	 * @param minPoolSize The minimum size of the process pool.
	 * @param maxPoolSize The maximum size of the process pool.
	 * @param reserveSize The number of available processes to keep in the pool.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the core threads to start 
	 * up.
	 * @throws IllegalArgumentException If the minimum pool size is less than 0, or the maximum pool size is less 
	 * than the minimum pool size or 1, or the reserve size is less than 0 or greater than the maximum pool size.
	 */
	public static ProcessPoolExecutorService newCustomProcessPoolExecutorService(int minPoolSize, int maxPoolSize,
			int reserveSize) throws InterruptedException {
		return newCustomProcessPoolExecutorService(null, minPoolSize, maxPoolSize, reserveSize);
	}
	/**
	 * Returns a pool of a fixed number of Java processes. It is a convenience method for calling
	 * {@link #newCustomProcessPoolExecutorService(JavaProcessOptions, int, int, int)} with <code>minPoolSize
	 * </code> and <code>maxPoolSize</code> set to the value of <code>size</code> and <code>reserveSize</code> 
	 * set to <code>0</code>. The number of executors in the pool is always kept at the specified value.
	 * 
	 * @param options The options for the "java" program used to create the new JVM. If it is null, no options 
	 * are used.
	 * @param size The number of processes to maintain in the pool.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the core threads to start 
	 * up.
	 */
	public static ProcessPoolExecutorService newFixedProcessPoolExecutorService(JavaProcessOptions options,
			int size) throws InterruptedException {
		return newCustomProcessPoolExecutorService(options, size, size, 0);
	}
	/**
	 * Returns a pool of a fixed number of Java processes. It is a convenience method for calling
	 * {@link #newFixedProcessPoolExecutorService(JavaProcessOptions, int)} with <code>options</code> set to 
	 * <code>null</code>. The number of executors in the pool is always kept at the specified value.
	 * 
	 * @param size The number of processes to maintain in the pool.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the core threads to start 
	 * up.
	 */
	public static ProcessPoolExecutorService newFixedProcessPoolExecutorService(int size)
			throws InterruptedException {
		return newFixedProcessPoolExecutorService(null, size);
	}
	/**
	 * Returns a pool of Java processes that grows in size as required.. It is a convenience method for calling
	 * {@link #newCustomProcessPoolExecutorService(JavaProcessOptions, int, int, int)} with <code>minPoolSize
	 * </code> set to <code>0</code>, <code>maxPoolSize</code> set to <code>Integer.MAX_VALUE</code> and <code>
	 * reserveSize</code> set to <code>0</code>.
	 * 
	 * @param options The options for the "java" program used to create the new JVM. If it is null, no options 
	 * are used.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the core threads to 
	 * start up.
	 */
	public static ProcessPoolExecutorService newCachedProcessPoolExecutorService(JavaProcessOptions options)
			throws InterruptedException {
		return newCustomProcessPoolExecutorService(options, 0, Integer.MAX_VALUE, 0);
	}
	/**
	 * Returns a pool of Java processes that grows in size as required.. It is a convenience method for calling
	 * {@link #newCachedProcessPoolExecutorService(JavaProcessOptions)} with <code>options</code> set to 
	 * <code>null</code>.
	 * 
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the core threads to 
	 * start up.
	 */
	public static ProcessPoolExecutorService newCachedProcessPoolExecutorService()
			throws InterruptedException {
		return newCachedProcessPoolExecutorService(null);
	}
	/**
	 * Returns a fixed size pool holding a single Java process. It is a convenience method for calling the method
	 * {@link #newFixedProcessPoolExecutorService(JavaProcessOptions, int)} with <code>size</code> set to 
	 * <code>1</code>.
	 * 
	 * @param options The options for the "java" program used to create the new JVM. If it is null, no options 
	 * are used.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the core threads to 
	 * start up.
	 */
	public static ProcessPoolExecutorService newSingleProcessPoolExecutorService(JavaProcessOptions options)
			throws InterruptedException {
		return newFixedProcessPoolExecutorService(options, 1);
	}
	/**
	 * Returns a fixed size pool holding a single Java process. It is a convenience method for calling the method
	 * {@link #newSingleProcessPoolExecutorService(JavaProcessOptions)} with <code>options</code> set to 
	 * <code>null</code>.
	 * 
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the core threads to 
	 * start up.
	 */
	public static ProcessPoolExecutorService newSingleProcessPoolExecutorService()
			throws InterruptedException {
		return newSingleProcessPoolExecutorService(null);
	}
	
}
