package org.droidtv.defaultdashboard.util;

import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by sandeep.kumar on 14/11/2017.
 */

public class ThreadPoolExecutorWrapper {

    private static final String TAG = "ThreadPoolExecutorWrapper";

    public static final int PROGRAM_DATA_THREAD_POOL_EXECUTOR = 100;
    public static final int CHANNEL_LIST_THREAD_POOL_EXECUTOR = 101;
    public static final int SOURCE_THREAD_POOL_EXECUTOR = 102;
    public static final int OTHER_THREAD_POOL_EXECUTOR = 103;
    public static final int IMAGE_THREAD_THREAD_POOL_EXECUTOR = 104;
    public static final int ACCOUNT_THREAD_THREAD_POOL_EXECUTOR = 105;
    public static final int SMART_INFO_THREAD_THREAD_POOL_EXECUTOR = 106;
    public static final int GOOGLE_CAST_THREAD_POOL_EXECUTOR = 107;
    public static final int MYCHOICE_THREAD_POOL_EXECUTOR = 108;
    public static final int RECOMMENDATION_THREAD_POOL_EXECUTOR = 109;
    public static final int THREAD_PRIORITY_CUSTOM = 3;

    private static ThreadPoolExecutorWrapper sInstance;

    private ThreadPoolExecutor mProgramDataThreadPoolExecutor;
    private ThreadPoolExecutor mChannelListThreadPoolExecutor;
    private ThreadPoolExecutor mSourceThreadPoolExecutor;
    private ThreadPoolExecutor mThumbnailThreadPoolExecutor;
    private ThreadPoolExecutor mOtherThreadPoolExecutor;
    private ThreadPoolExecutor mAccountThreadPoolExecutor;
    private ThreadPoolExecutor mSmartInfoThreadPoolExecutor;
    private ThreadPoolExecutor mGoogleCastThreadPoolExecutor;
    private ThreadPoolExecutor mMyChoiceThreadPoolExecutor;
    private ThreadPoolExecutor mRecommendationThreadPoolExecutor;

    private ThreadPoolExecutorWrapper() {

    }

    public static ThreadPoolExecutorWrapper getInstance() {
        if (sInstance == null) {
            synchronized (ThreadPoolExecutorWrapper.class) {
                    sInstance = new ThreadPoolExecutorWrapper();
            }
        }
        return sInstance;
    }

    private ThreadPoolExecutor createThreadPoolExecutor(ThreadPool threadPool) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(threadPool.getMaxWorkerThreads(), threadPool.getMaxWorkerThreads(),
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new ThreadFactoryImpl(threadPool.getThreadName(), threadPool.getThreadPriority()));

        threadPoolExecutor.setCorePoolSize(threadPool.getCorePoolSize());
        threadPoolExecutor.setKeepAliveTime(threadPool.getKeepAliveTime(), TimeUnit.MINUTES);
        threadPoolExecutor.allowCoreThreadTimeOut(threadPool.allowCoreThreadTimeout());
        threadPoolExecutor.prestartAllCoreThreads();

        return threadPoolExecutor;
    }

    public ThreadPoolExecutor getThreadPoolExecutor(int threadPoolExecutorType) {
        switch (threadPoolExecutorType) {
            case PROGRAM_DATA_THREAD_POOL_EXECUTOR:
                if (mProgramDataThreadPoolExecutor != null) {
                    return mProgramDataThreadPoolExecutor;
                }
                mProgramDataThreadPoolExecutor = createThreadPoolExecutor(new ProgramDataThreadPool());
                mProgramDataThreadPoolExecutor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        Log.v("ProgramDataTPE", " Execution rejected  " + r);
                    }
                });
                return mProgramDataThreadPoolExecutor;

            case CHANNEL_LIST_THREAD_POOL_EXECUTOR:
                if (mChannelListThreadPoolExecutor != null) {
                    return mChannelListThreadPoolExecutor;
                }
                mChannelListThreadPoolExecutor = createThreadPoolExecutor(new ChannelListThreadPool());
                mChannelListThreadPoolExecutor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        Log.v("ChannelListTPE", " Execution rejected  " + r);
                    }
                });
                return mChannelListThreadPoolExecutor;

            case SOURCE_THREAD_POOL_EXECUTOR:
                if (mSourceThreadPoolExecutor != null) {
                    return mSourceThreadPoolExecutor;
                }
                mSourceThreadPoolExecutor = createThreadPoolExecutor(new SourceThreadPool());
                mSourceThreadPoolExecutor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        Log.v("SourceTPE", " Execution rejected  " + r);
                    }
                });
                return mSourceThreadPoolExecutor;

            case IMAGE_THREAD_THREAD_POOL_EXECUTOR:
                if (mThumbnailThreadPoolExecutor != null) {
                    return mThumbnailThreadPoolExecutor;
                }
                mThumbnailThreadPoolExecutor = createThreadPoolExecutor(new ImageDataThreadPool());
                mThumbnailThreadPoolExecutor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        Log.v("ThumbnailTPE", " Execution rejected  " + r);
                    }
                });
                return mThumbnailThreadPoolExecutor;

            case OTHER_THREAD_POOL_EXECUTOR:
                if (mOtherThreadPoolExecutor != null) {
                    return mOtherThreadPoolExecutor;
                }
                mOtherThreadPoolExecutor = createThreadPoolExecutor(new OtherThreadPool());
                mOtherThreadPoolExecutor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        Log.v("OtherTPE", " Execution rejected  " + r);
                    }
                });
                return mOtherThreadPoolExecutor;

            case ACCOUNT_THREAD_THREAD_POOL_EXECUTOR:
                if (mAccountThreadPoolExecutor != null) {
                    return mAccountThreadPoolExecutor;
                }
                mAccountThreadPoolExecutor = createThreadPoolExecutor(new AccountThreadPool());
                mAccountThreadPoolExecutor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        Log.v("AccountTPE", " Execution rejected  " + r);
                    }
                });
                return mAccountThreadPoolExecutor;

            case SMART_INFO_THREAD_THREAD_POOL_EXECUTOR:
                if (mSmartInfoThreadPoolExecutor != null) {
                    return mSmartInfoThreadPoolExecutor;
                }
                mSmartInfoThreadPoolExecutor = createThreadPoolExecutor(new SmartInfoThreadPool());
                mSmartInfoThreadPoolExecutor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        Log.v("SmartInfoTPE", " Execution rejected  " + r);
                    }
                });
                return mSmartInfoThreadPoolExecutor;

            case GOOGLE_CAST_THREAD_POOL_EXECUTOR:
                if (mGoogleCastThreadPoolExecutor != null) {
                    return mGoogleCastThreadPoolExecutor;
                }
                mGoogleCastThreadPoolExecutor = createThreadPoolExecutor(new GoogleCastThreadPool());
                mGoogleCastThreadPoolExecutor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        Log.v("GoogleCastTPE", " Execution rejected  " + r);
                    }
                });
                return mGoogleCastThreadPoolExecutor;

            case MYCHOICE_THREAD_POOL_EXECUTOR:
                if (mMyChoiceThreadPoolExecutor != null) {
                    return mMyChoiceThreadPoolExecutor;
                }
                mMyChoiceThreadPoolExecutor = createThreadPoolExecutor(new MyChoiceThreadPool());
                mMyChoiceThreadPoolExecutor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        Log.v("MyChoiceTPE", " Execution rejected  " + r);
                    }
                });
                return mMyChoiceThreadPoolExecutor;
            case RECOMMENDATION_THREAD_POOL_EXECUTOR:
                if (mRecommendationThreadPoolExecutor != null) {
                    return mRecommendationThreadPoolExecutor;
                }
                mRecommendationThreadPoolExecutor = createThreadPoolExecutor(new RecommendationThreadPool());
                mRecommendationThreadPoolExecutor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        Log.v("RecommendationTPE", " Execution rejected  " + r);
                    }
                });
                return mRecommendationThreadPoolExecutor;
        }

        // Not matched any pre-specified thread pool executor types, hence return null
        return null;
    }

    private interface ThreadPool {

        int getMaxWorkerThreads();

        int getCorePoolSize();

        String getThreadName();

        int getThreadPriority();

        int getKeepAliveTime();

        boolean allowCoreThreadTimeout();
    }

    private static class ProgramDataThreadPool implements ThreadPool {
        private static final int MAX_WORKER_THREADS = 2;
        private static final int CORE_POOL_SIZE = 1;
        private static final String THREAD_NAME = "ProgramDataThread";
        private static final int THREAD_PRIORITY = THREAD_PRIORITY_CUSTOM;
        private static final int KEEP_ALIVE_TIME = 5;
        private static final boolean ALLOW_CORE_THREAD_TIMEOUT = true;

        @Override
        public int getMaxWorkerThreads() {
            return MAX_WORKER_THREADS;
        }

        @Override
        public int getCorePoolSize() {
            return CORE_POOL_SIZE;
        }

        @Override
        public String getThreadName() {
            return THREAD_NAME;
        }

        @Override
        public int getThreadPriority() {
            return THREAD_PRIORITY;
        }

        @Override
        public int getKeepAliveTime() {
            return KEEP_ALIVE_TIME;
        }

        @Override
        public boolean allowCoreThreadTimeout() {
            return ALLOW_CORE_THREAD_TIMEOUT;
        }
    }

    private static class ChannelListThreadPool implements ThreadPool {
        private static final int MAX_WORKER_THREADS = 6;
        private static final int CORE_POOL_SIZE = 1;
        private static final String THREAD_NAME = "ChannelListThread";
        private static final int THREAD_PRIORITY = THREAD_PRIORITY_CUSTOM;
        private static final int KEEP_ALIVE_TIME = 10;
        private static final boolean ALLOW_CORE_THREAD_TIMEOUT = false;

        @Override
        public int getMaxWorkerThreads() {
            return MAX_WORKER_THREADS;
        }

        @Override
        public int getCorePoolSize() {
            return CORE_POOL_SIZE;
        }

        @Override
        public String getThreadName() {
            return THREAD_NAME;
        }

        @Override
        public int getThreadPriority() {
            return THREAD_PRIORITY;
        }

        @Override
        public int getKeepAliveTime() {
            return KEEP_ALIVE_TIME;
        }

        @Override
        public boolean allowCoreThreadTimeout() {
            return ALLOW_CORE_THREAD_TIMEOUT;
        }
    }

    private static class SourceThreadPool implements ThreadPool {
        private static final int MAX_WORKER_THREADS = 1;
        private static final int CORE_POOL_SIZE = 1;
        private static final String THREAD_NAME = "SourceThread";
        private static final int THREAD_PRIORITY = THREAD_PRIORITY_CUSTOM;
        private static final int KEEP_ALIVE_TIME = 10;
        private static final boolean ALLOW_CORE_THREAD_TIMEOUT = false;

        @Override
        public int getMaxWorkerThreads() {
            return MAX_WORKER_THREADS;
        }

        @Override
        public int getCorePoolSize() {
            return CORE_POOL_SIZE;
        }

        @Override
        public String getThreadName() {
            return THREAD_NAME;
        }

        @Override
        public int getThreadPriority() {
            return THREAD_PRIORITY;
        }

        @Override
        public int getKeepAliveTime() {
            return KEEP_ALIVE_TIME;
        }

        @Override
        public boolean allowCoreThreadTimeout() {
            return ALLOW_CORE_THREAD_TIMEOUT;
        }
    }

    private static class ImageDataThreadPool implements ThreadPool {
        private static final int MAX_WORKER_THREADS = 3;
        private static final int CORE_POOL_SIZE = 1;
        private static final String THREAD_NAME = "ImageDataThread";
        private static final int THREAD_PRIORITY = THREAD_PRIORITY_CUSTOM;
        private static final int KEEP_ALIVE_TIME = 10;
        private static final boolean ALLOW_CORE_THREAD_TIMEOUT = true;

        @Override
        public int getMaxWorkerThreads() {
            return MAX_WORKER_THREADS;
        }

        @Override
        public int getCorePoolSize() {
            return CORE_POOL_SIZE;
        }

        @Override
        public String getThreadName() {
            return THREAD_NAME;
        }

        @Override
        public int getThreadPriority() {
            return THREAD_PRIORITY;
        }

        @Override
        public int getKeepAliveTime() {
            return KEEP_ALIVE_TIME;
        }

        @Override
        public boolean allowCoreThreadTimeout() {
            return ALLOW_CORE_THREAD_TIMEOUT;
        }
    }

    private static class OtherThreadPool implements ThreadPool {
        private static final int MAX_WORKER_THREADS = 2;
        private static final int CORE_POOL_SIZE = 1;
        private static final String THREAD_NAME = "OtherThread";
        private static final int THREAD_PRIORITY = THREAD_PRIORITY_CUSTOM;
        private static final int KEEP_ALIVE_TIME = 5;
        private static final boolean ALLOW_CORE_THREAD_TIMEOUT = true;

        @Override
        public int getMaxWorkerThreads() {
            return MAX_WORKER_THREADS;
        }

        @Override
        public int getCorePoolSize() {
            return CORE_POOL_SIZE;
        }

        @Override
        public String getThreadName() {
            return THREAD_NAME;
        }

        @Override
        public int getThreadPriority() {
            return THREAD_PRIORITY;
        }

        @Override
        public int getKeepAliveTime() {
            return KEEP_ALIVE_TIME;
        }

        @Override
        public boolean allowCoreThreadTimeout() {
            return ALLOW_CORE_THREAD_TIMEOUT;
        }
    }

    private static class AccountThreadPool implements ThreadPool {
        private static final int MAX_WORKER_THREADS = 1;
        private static final int CORE_POOL_SIZE = 1;
        private static final String THREAD_NAME = "AccountThread";
        private static final int THREAD_PRIORITY = THREAD_PRIORITY_CUSTOM;
        private static final int KEEP_ALIVE_TIME = 5;
        private static final boolean ALLOW_CORE_THREAD_TIMEOUT = true;

        @Override
        public int getMaxWorkerThreads() {
            return MAX_WORKER_THREADS;
        }

        @Override
        public int getCorePoolSize() {
            return CORE_POOL_SIZE;
        }

        @Override
        public String getThreadName() {
            return THREAD_NAME;
        }

        @Override
        public int getThreadPriority() {
            return THREAD_PRIORITY;
        }

        @Override
        public int getKeepAliveTime() {
            return KEEP_ALIVE_TIME;
        }

        @Override
        public boolean allowCoreThreadTimeout() {
            return ALLOW_CORE_THREAD_TIMEOUT;
        }
    }

    private static class SmartInfoThreadPool implements ThreadPool {
        private static final int MAX_WORKER_THREADS = 1;
        private static final int CORE_POOL_SIZE = 1;
        private static final String THREAD_NAME = "SmartInfoThread";
        private static final int THREAD_PRIORITY = THREAD_PRIORITY_CUSTOM;
        private static final int KEEP_ALIVE_TIME = 5;
        private static final boolean ALLOW_CORE_THREAD_TIMEOUT = true;

        @Override
        public int getMaxWorkerThreads() {
            return MAX_WORKER_THREADS;
        }

        @Override
        public int getCorePoolSize() {
            return CORE_POOL_SIZE;
        }

        @Override
        public String getThreadName() {
            return THREAD_NAME;
        }

        @Override
        public int getThreadPriority() {
            return THREAD_PRIORITY;
        }

        @Override
        public int getKeepAliveTime() {
            return KEEP_ALIVE_TIME;
        }

        @Override
        public boolean allowCoreThreadTimeout() {
            return ALLOW_CORE_THREAD_TIMEOUT;
        }
    }

    private static class GoogleCastThreadPool implements ThreadPool {
        private static final int MAX_WORKER_THREADS = 1;
        private static final int CORE_POOL_SIZE = 1;
        private static final String THREAD_NAME = "GoogleCastThread";
        private static final int THREAD_PRIORITY = THREAD_PRIORITY_CUSTOM;
        private static final int KEEP_ALIVE_TIME = 5;
        private static final boolean ALLOW_CORE_THREAD_TIMEOUT = true;

        @Override
        public int getMaxWorkerThreads() {
            return MAX_WORKER_THREADS;
        }

        @Override
        public int getCorePoolSize() {
            return CORE_POOL_SIZE;
        }

        @Override
        public String getThreadName() {
            return THREAD_NAME;
        }

        @Override
        public int getThreadPriority() {
            return THREAD_PRIORITY;
        }

        @Override
        public int getKeepAliveTime() {
            return KEEP_ALIVE_TIME;
        }

        @Override
        public boolean allowCoreThreadTimeout() {
            return ALLOW_CORE_THREAD_TIMEOUT;
        }
    }

    private static class MyChoiceThreadPool implements ThreadPool {
        private static final int MAX_WORKER_THREADS = 1;
        private static final int CORE_POOL_SIZE = 1;
        private static final String THREAD_NAME = "MyChoiceThread";
        private static final int THREAD_PRIORITY = THREAD_PRIORITY_CUSTOM;
        private static final int KEEP_ALIVE_TIME = 10;
        private static final boolean ALLOW_CORE_THREAD_TIMEOUT = true;

        @Override
        public int getMaxWorkerThreads() {
            return MAX_WORKER_THREADS;
        }

        @Override
        public int getCorePoolSize() {
            return CORE_POOL_SIZE;
        }

        @Override
        public String getThreadName() {
            return THREAD_NAME;
        }

        @Override
        public int getThreadPriority() {
            return THREAD_PRIORITY;
        }

        @Override
        public int getKeepAliveTime() {
            return KEEP_ALIVE_TIME;
        }

        @Override
        public boolean allowCoreThreadTimeout() {
            return ALLOW_CORE_THREAD_TIMEOUT;
        }
    }

    private static class RecommendationThreadPool implements ThreadPool {
        private static final int MAX_WORKER_THREADS = 1;
        private static final int CORE_POOL_SIZE = 1;
        private static final String THREAD_NAME = "Recommendation";
        private static final int THREAD_PRIORITY = THREAD_PRIORITY_CUSTOM;
        private static final int KEEP_ALIVE_TIME = 10;
        private static final boolean ALLOW_CORE_THREAD_TIMEOUT = true;

        @Override
        public int getMaxWorkerThreads() {
            return MAX_WORKER_THREADS;
        }

        @Override
        public int getCorePoolSize() {
            return CORE_POOL_SIZE;
        }

        @Override
        public String getThreadName() {
            return THREAD_NAME;
        }

        @Override
        public int getThreadPriority() {
            return THREAD_PRIORITY;
        }

        @Override
        public int getKeepAliveTime() {
            return KEEP_ALIVE_TIME;
        }

        @Override
        public boolean allowCoreThreadTimeout() {
            return ALLOW_CORE_THREAD_TIMEOUT;
        }
    }
    private static final class ThreadFactoryImpl implements ThreadFactory {

        private static final String TAG = "ThreadFactoryImpl";

        private String threadName;
        private int threadPriority;

        public ThreadFactoryImpl(String threadName) {
            this(threadName, THREAD_PRIORITY_CUSTOM);
        }

        public ThreadFactoryImpl(String threadName, int threadPriority) {
            this.threadName = threadName;
            this.threadPriority = threadPriority;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName(threadName);
            t.setPriority(threadPriority);
            return t;
        }
    }
}
