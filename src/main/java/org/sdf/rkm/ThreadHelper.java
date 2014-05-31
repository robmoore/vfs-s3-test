package org.sdf.rkm;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Stolen from <a href="http://nadeausoftware.com/articles/2008/04/java_tip_how_list_and_find_threads_and_thread_groups">Java tip: How to list and find threads and thread groups</a> article.
 */
public class ThreadHelper {
    private ThreadGroup rootThreadGroup = null;

    Thread[] getThreadsByName(final String name) {
        if (name == null)
            throw new NullPointerException("Null name");
        final Thread[] threads = getAllThreads();
        final List<Thread> mThreads = new ArrayList<Thread>();
        for (Thread thread : threads)
            if (thread.getName().matches(name))
                mThreads.add(thread);
        return mThreads.toArray(new Thread[mThreads.size()]);
    }

    Thread[] getAllThreads() {
        final ThreadGroup root = getRootThreadGroup();
        final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        int nAlloc = threadMXBean.getThreadCount();
        int n;
        Thread[] threads;
        do {
            nAlloc *= 2;
            threads = new Thread[nAlloc];
            n = root.enumerate(threads, true);
        } while (n == nAlloc);
        return Arrays.copyOf(threads, n);
    }

    ThreadGroup getRootThreadGroup() {
        if (rootThreadGroup != null)
            return rootThreadGroup;
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        ThreadGroup ptg;
        while ((ptg = tg.getParent()) != null)
            tg = ptg;
        return tg;
    }
}
