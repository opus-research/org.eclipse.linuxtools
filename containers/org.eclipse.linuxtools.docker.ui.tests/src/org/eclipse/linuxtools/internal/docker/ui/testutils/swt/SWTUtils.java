package org.eclipse.linuxtools.internal.docker.ui.testutils.swt;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import org.junit.ComparisonFailure;

public class SWTUtils {

	/**
	 * Calls <strong>synchronously</strong> the given {@link Runnable} in the
	 * default Display and returns the result
	 * 
	 * @param supplier
	 *            the Supplier to call
	 * @return the supplier's result
	 */
	public static <V> V syncExec(final Supplier<V> supplier) {
		final Queue<V> result = new ArrayBlockingQueue<>(1);
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				result.add(supplier.get());
			}
		});
		return result.poll();
	}

	/**
	 * Executes <strong>synchronously</strong> the given {@link Runnable} in the
	 * default Display
	 * 
	 * @param runnable
	 *            the {@link Runnable} to execute
	 */
	public static void syncExec(final Runnable runnable) {
		Display.getDefault().syncExec(runnable);
	}

	/**
	 * Executes <strong>synchronously</strong> the given {@link Runnable} in the
	 * default Display. The given {@link Runnable} is ran into a rapping
	 * {@link Runnable} that will catch the {@link ComparisonFailure} that may
	 * be raised during an assertion.
	 * 
	 * @param runnable
	 *            the {@link Runnable} to execute
	 * @throws ComparisonFailure
	 *             if an assertion failed.
	 * @throws SWTException if an {@link SWTException} occurred             
	 */
	public static void syncAssert(final Runnable runnable) throws SWTException, ComparisonFailure {
		final Queue<ComparisonFailure> failure = new ArrayBlockingQueue<>(1);
		final Queue<SWTException> swtException = new ArrayBlockingQueue<>(1);
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				try {
					runnable.run();
				} catch (ComparisonFailure e) {
					failure.add(e);
				} catch (SWTException e) {
					swtException.add(e);
				}
			}
		});
		if (!failure.isEmpty()) {
			throw failure.poll();
		}
		if (!swtException.isEmpty()) {
			throw swtException.poll();
		}
	}

	/**
	 * Executes the given {@link Runnable} <strong>asynchronously</strong> in
	 * the default {@link Display} and waits until all jobs are done before
	 * completing.
	 * 
	 * @param runnable
	 * @throws InterruptedException
	 */
	public static void asyncExec(final Runnable runnable) throws InterruptedException {
		final Queue<ComparisonFailure> failure = new ArrayBlockingQueue<>(1);
		final Queue<SWTException> swtException = new ArrayBlockingQueue<>(1);
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				try {
					runnable.run();
				} catch (ComparisonFailure e) {
					failure.add(e);
				} catch (SWTException e) {
					swtException.add(e);
				}
			}
		});
		Thread.sleep(TimeUnit.SECONDS.toMillis(1));
		while (!Job.getJobManager().isIdle()) {
			Thread.sleep(TimeUnit.SECONDS.toMillis(1));
		}
		if (!failure.isEmpty()) {
			throw failure.poll();
		}
		if (!swtException.isEmpty()) {
			throw swtException.poll();
		}
	}

	/**
	 * @param viewBot the {@link SWTBotView} containing the {@link Tree} to traverse
	 * @param path the node path in the {@link SWTBotTree} associated with the given {@link SWTBotView}
	 * @return the first {@link SWTBotTreeItem} matching the given node names
	 */
	public static SWTBotTreeItem getTreeItem(final SWTBotView viewBot, final String... path) {
		final SWTBotTree tree = viewBot.bot().tree();
		return getTreeItem(tree.getAllItems(), path);
	}

	private static SWTBotTreeItem getTreeItem(final SWTBotTreeItem[] treeItems, final String[] path) {
		final SWTBotTreeItem swtBotTreeItem = Stream.of(treeItems).filter(item -> item.getText().equals(path[0])).findFirst().get();
		if(path.length > 1) {
			final String[] remainingPath = new String[path.length -1];
			System.arraycopy(path, 1, remainingPath, 0, remainingPath.length);
			return getTreeItem(swtBotTreeItem.getItems(), remainingPath);
		}
		return swtBotTreeItem;
	}

}
