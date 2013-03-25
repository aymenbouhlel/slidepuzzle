package jp.suwashimizu.th.slidepuzzle;

import android.util.Log;

public class LimitCache<T> {

	int topIndex;//ｷｯｼｭ外,全体での位置ここからsize分キャッシュがある
	int cacheSize;
	int topPosition;//配列内でのIndex cacheSize越える事無し

	Object[] objArray;

	public LimitCache(int cahceSize) {
		this.cacheSize = cahceSize;
		objArray = new Object[cacheSize];
	}

	public void cache(int index,T object){
		//キャッシュが配列内に無い時
		Log.d("cahceIndex",""+index);
		if(index < topIndex - cacheSize || index > topIndex + cacheSize){// i20 t15 b15 
			clearCache();
			topPosition = 0;
			topIndex = index;
			objArray[0] = object;
			Log.d("clear","topIndex="+topIndex+",topPosition="+topPosition);
			return;
		}
		//update　データ固定なためいらない？
		if(isCached(index)){
			int rIndex = topPosition + index - topIndex;
			if (rIndex >= cacheSize)
				rIndex -= cacheSize;
			
			objArray[rIndex] = object;
			//Log.d("update","topIndex="+topIndex+",topPosition="+topPosition);
		}

		//leftshift
		if(index < topIndex){
			int shifCount = topIndex - index;
			for(int i=0;i<shifCount;i++){
				Log.d("left_cache",""+getArrayPosition(i, topPosition, topIndex));
				objArray[getArrayPosition(i, topPosition, topIndex)] = null;
			}

			topPosition -= cacheSize;
			if(topPosition < 0)
				topPosition += cacheSize;
			topIndex = index;
			objArray[topPosition] = object;
			Log.d("left","topIndex="+topIndex+",topPosition="+topPosition);
			return;
		}
		//right shift
		if(index > topIndex+cacheSize -1){
			int shiftCount = index - topIndex - cacheSize +1;
			for(int i=0;i<shiftCount;i++)
				objArray[getArrayPosition(i, topPosition, topIndex)] = null;

			topPosition += shiftCount;
			if(topPosition >= cacheSize)
				topPosition -= cacheSize;
			topIndex = index - cacheSize +1;
			objArray[getArrayPosition(index, topPosition, topIndex)] = object;
			Log.d("right","topIndex="+topIndex+",topPosition="+topPosition);
			return;
		}
		//objArray[getArrayPosition(index, topPosition, topIndex)] = object;
	}

	public void clearCache(){
		for(Object o:objArray){
			o=null;
		}
	}

	@SuppressWarnings("unchecked")
	public T getCache(int index){
		
		if(!isCached(index))
			return null;

		int rIndex = topPosition + index - topIndex;//60+10  66
		if(rIndex >= cacheSize)
			rIndex -= cacheSize;
		return (T)objArray[rIndex];
	}

	public boolean isCached(int index){
		if(index < topIndex){
			return false;
		}
		if(index < topIndex + cacheSize-1)
			return true;
		return false;
	}
	private int getArrayPosition(int index,int topPosition,int topIndex){
		int rIndex = topPosition + index - topIndex;
		while(rIndex >= cacheSize)
			rIndex -= cacheSize;
		while(rIndex < 0)
			rIndex += cacheSize;
		return rIndex;		
	}
}
