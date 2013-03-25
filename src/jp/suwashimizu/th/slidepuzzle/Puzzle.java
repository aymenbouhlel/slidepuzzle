package jp.suwashimizu.th.slidepuzzle;

public class Puzzle {

	public static final int X_GRID = 4;
	public static final int Y_GRID = 4;

	public int[][] slide = new int[Y_GRID][X_GRID];

	public Puzzle() {
		for(int i=0;i<Y_GRID;i++)
			for(int j=0;j<X_GRID;j++)
				slide[i][j] = i*X_GRID+j;

		slide[Y_GRID-1][X_GRID-1] = -1;//空タイルの設定右下が空に
		shfle();
	}

	private void  shfle(){
		int x=3,y=3;
		for(int i=0;i<1000;i++){
			int	shfle = (int) (Math.random()*4);
			int _x,_y;

			switch(shfle){
			case 0://up
				_x = x;
				_y = y-1;
				break;
			case 1://right
				_x = x+1;
				_y = y;
				break;
			case 2://down
				_x = x;
				_y = y+1;
				break;
			default://left
				_x = x - 1;
				_y = y;
				break;
			}

			if(moveSlide(_x, _y)){
				x = _x;
				y = _y;
			}
		}	
	}

	public boolean moveSlide(int x,int y){

		if(x < X_GRID && x >= 0)
			if(y < Y_GRID && y >= 0){

				//右への移動判定
				if(x + 1 < X_GRID)
				if(slide[y][x+1] == -1 ){//入れ替え
					slide[y][x+1] = slide[y][x];			
					slide[y][x] = -1;
					return true;
				}
				if(x -1 >= 0)//左
				if(slide[y][x-1] == -1 ){
					slide[y][x-1] = slide[y][x];
					slide[y][x] = -1;
					return true;
				}
				if(y + 1 < Y_GRID)//下
				if(slide[y+1][x] == -1 ){
					slide[y+1][x] = slide[y][x];				
					slide[y][x] = -1;
					return true;
				}
				if(y -1 >= 0)//上
				if(slide[y-1][x] == -1 ){
					slide[y-1][x] = slide[y][x];				
					slide[y][x] = -1;
					return true;
				}
			}
		return false;
	}
	
	public boolean getClearState(){
		
		if(slide[X_GRID-1][Y_GRID-1] != -1)
			return false;
		
		for(int i=0;i<Y_GRID;i++)
			for(int j=0;j<X_GRID;j++){
				if(i*Y_GRID+j != slide[i][j] && i*Y_GRID+j < 15){					
					return false;
				}
			}
		
		return true;
	}

	public int[][] getGridState(){

		return slide;
	}
}
