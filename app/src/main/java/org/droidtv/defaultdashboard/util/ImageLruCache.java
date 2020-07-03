package org.droidtv.defaultdashboard.util;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

import org.droidtv.defaultdashboard.log.DdbLogUtility;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImageLruCache {
    private static final String LOG = ImageLruCache.class.getSimpleName();
    private static long totalSize = 0;
    private final int MAX_LRU_CACHE_SIZE = 64 * 1024;
    public static Map<String, DDBBitmap> mEvictedBitMapPool = new ConcurrentHashMap<>(10);
    private final LruCache<String, DDBBitmap> mBitmapCache;
	
	public class DDBBitmap{
		private Bitmap image;
		//TODO: use this refcount when there are multiple programs with same url
		private int refCount = 0;
		
		public DDBBitmap(Bitmap img){
			image = img;
			addReference();
		}

		public boolean isValid(){
			return (image != null && (!image.isRecycled()));
		}
		
		public Bitmap getImage(){
			return image;
		}
		
		public int getRefCount(){
			return refCount;
		}
		
		public void addReference(){ refCount = 1;}

		public void removeReference(){ refCount = 0;}

		public void recycleBitmap(){
			if(isValid()) {
				image.recycle();
				image = null;
			}
		}
	}

    public ImageLruCache(final int inputCacheSize) {
		Log.d(LOG, "ImageLruCache: MAX_LRU_CACHE_SIZE(KB) " + MAX_LRU_CACHE_SIZE);
            mBitmapCache = new LruCache<String, DDBBitmap>(MAX_LRU_CACHE_SIZE) {
            @Override
            protected int sizeOf(final String key, final DDBBitmap value) {
                final int size = getBitmapSize(value.getImage());
                return size;
            }

            @Override
            protected void entryRemoved(final boolean evicted, final String key, DDBBitmap oldValue, final DDBBitmap newValue) {
                DdbLogUtility.logCommon(LOG, "entryRemoved() called with: evicted = [" + evicted + "], key = [" + key + "], oldValue = [" + oldValue + "], newValue = [" + newValue + "]");
                synchronized (mBitmapCache) {
					if(oldValue.getRefCount() == 0){
						oldValue.recycleBitmap();
						oldValue = null;
					}else{
						addToEvictedBitmapPool(key, oldValue);
					}
                }
            }
        };
    }

    private long getMaxCacheSize(){
        return Constants.SIXTY_FOUR_MB;
    }

    /**
     * Get the size in bytes of a bitmap in a BitmapDrawable.
     *
     * @param bitmap
     * @return size in bytes
     */
    private static int getBitmapSize(final Bitmap bitmap) {
        return bitmap.getAllocationByteCount() / 1024;
    }

    /**
     * function to get the bitmap from the cache
     *
     * @param key - key to the bitmap
     * @return bitmap
     */
	 //Sup: This method needs to be cleaned up.
    /*public Bitmap getBitmap(final String key) {
//        Log.i(LOG, "getBitmap: " + key);
        if (key != null) {
            Bitmap bit = null;
            synchronized (mBitmapCache) {
               DDBBitmap bitmap = mBitmapCache.get(key);
			   if(bitmap != null){
				   bit = bitmap.getImage();
				   if(bit != null && bit.isRecycled()){
					   mBitmapCache.remove(key);
					   bit = null;
				   }				   
			    }
            }
            return bit;
        }
        return null;
    }*/
	
	
	public DDBBitmap getBitmap(final String key) {
		DDBBitmap ddbBitmap = null;
        if (key != null) {           
            synchronized (mBitmapCache) {
               ddbBitmap = mBitmapCache.get(key);
			   if(ddbBitmap != null){
				   Bitmap bit = ddbBitmap.getImage();
				   if(bit != null && bit.isRecycled()){
					   mBitmapCache.remove(key);
					   return null;
				   }
			    }else if(ddbBitmap == null){
				   ddbBitmap = getBitMapFromEvictedPool(key);
				   if(ddbBitmap != null && ddbBitmap.getImage() != null && !ddbBitmap.getImage().isRecycled()){
					   putBitmap(key, ddbBitmap);
				   }
			   }			   
            }
        }
        return ddbBitmap;
	}

    /**
     * function to insert the bitmap into the cache
     *
     * @param key    - unique identifier for the bitmap.
     * @param bitmap - the bitmap to be stored.
     */
    public void putBitmap(final String key, final DDBBitmap bitmap) {
        if ((key != null) && (bitmap != null)) {
            synchronized (mBitmapCache) {
                DDBBitmap oldBitmap = mBitmapCache.get(key);
               if(oldBitmap == null){//If bitmap doesnot exist then put into mBitmapCache
                   mBitmapCache.put(key, bitmap);
                   totalSize += bitmap.getImage().getByteCount();
               }else {//Dont put bitmap since already existing
					//Sup: check if we should recycle incomming bitmap
                   DdbLogUtility.logCommon(LOG, "bitmap already available for " + key);
               }
                DdbLogUtility.logCommon(LOG, "putBitmapputBitmap: MaxSize: " + mBitmapCache.maxSize() + " TotalSize(MB) " + (totalSize/(1024*1024)));
            }
        } else {
            Log.w(LOG, "Warning!! key : " + key + " bitmap: " + bitmap);
        }
    }

    public void recycleBitmap(Bitmap bitmap) {
        if(bitmap != null && !bitmap.isRecycled()){
            totalSize = totalSize - bitmap.getAllocationByteCount();
            bitmap.recycle();
            bitmap = null;
        }
    }

    public long getTotalUsedCacheSize() {
        return totalSize;
    }
	
	public synchronized static void addToEvictedBitmapPool(String key, DDBBitmap bitMap){
		mEvictedBitMapPool.put(key, bitMap);
		Log.d(LOG, "Adding "+ key + " evicted " + mEvictedBitMapPool.size());
		//AppUtil.printKeys(mEvictedBitMapPool, "addToEvictedBitmapPool", LOG);
		
	}
	
	public synchronized static DDBBitmap getBitMapFromEvictedPool(String key){
		if(mEvictedBitMapPool.containsKey(key)){
			return mEvictedBitMapPool.remove(key);
		}
		return null;
	}
	
	public synchronized static void RecycleEvictedBitmapPool(){
		//TODO: cleanup hashmap
		for(String key: mEvictedBitMapPool.keySet()){
			DDBBitmap ddbBitmap = mEvictedBitMapPool.remove(key);
			if(ddbBitmap != null) {
				ddbBitmap.recycleBitmap();
				ddbBitmap = null;
			}
		}
	}
	
	public void cleanupAllReferences(){
		//TODO: update reference count of all bitmaps in LRU cache and evicted list
		synchronized (mBitmapCache) {
			for (DDBBitmap ddbBitmap : mBitmapCache.snapshot().values()) {
				ddbBitmap.removeReference();
			}

			for (DDBBitmap ddbBitmap : mEvictedBitMapPool.values()) {
				ddbBitmap.removeReference();
			}
		}
	}

	//This method returns true if there is already a bitmap available.. if it is false then it means that you have got unbind for an image that is not decoded yet
	public synchronized boolean cleanupReference(String key){
		if(key == null) return false;
		DDBBitmap bitmap;
		 synchronized (mBitmapCache){
			 bitmap = mBitmapCache.get(key);
			 if(bitmap != null){
				 bitmap.removeReference();
				 return true;
			 }
		 }
		 
			//If it is not found in LRU Cache check if it can be recycled
			bitmap = mEvictedBitMapPool.remove(key);
			if(bitmap != null){
				bitmap.getImage().recycle();
				bitmap = null;
				return true;
			}

		 return false;
	}

    /**
     * function to clear the cache contents.
     */
    public void clearCache() {
		Log.d(LOG, "clearCache() called");
        totalSize = 0;
        //mBitmapCache.evictAll();
		Map<String, DDBBitmap> lruCache = mBitmapCache.snapshot();
		for(String key: lruCache.keySet()){
			DDBBitmap ddbBitmap = mBitmapCache.remove(key);
			if(ddbBitmap !=  null){
				ddbBitmap.recycleBitmap();
			}

		}
		RecycleEvictedBitmapPool();
		
    }
}
