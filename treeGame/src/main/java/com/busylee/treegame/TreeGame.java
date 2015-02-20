package com.busylee.treegame;

import android.widget.Toast;
import com.busylee.treegame.branch.*;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.math.MathUtils;

import java.io.IOException;
import java.util.Random;

/**
 * Created by busylee on 14.02.15.
 */
public class TreeGame extends SimpleBaseGameActivity implements ITreeMaster {

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	final Random RANDOM = new Random(System.currentTimeMillis());
	final int RANDOM_START = 1;
	final int RANDOM_STOP = 100;

	private BitmapTextureAtlas mBitmapTextureAtlas;

	private BranchEntity[][] mBranchMatrix;
	private BranchEntity.Side[][] mBranchCorrectAnswer;

	int[][] resultMatrix;

	private ITiledTextureRegion
			mBranchRootTextureRegion,
			mBranchLeafTextureRegion,
			mBranchLongTextureRegion,
			mBranchDoubleEndedTextureRegion,
			mBranchTripleEndedTextureRegion;

	private Scene mScene;
	private VertexBufferObjectManager mVertexBufferObjectManager;

	private BranchRoot branchRoot = null;

	@Override
	protected void onCreateResources() throws IOException {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		this.mVertexBufferObjectManager = this.getVertexBufferObjectManager();

		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 64, 160, TextureOptions.BILINEAR);
		this.mBranchLeafTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "branch_leaf.png", 0, 0, 2, 1); // 32x32
		this.mBranchLongTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "branch_long.png", 0, 32, 2, 1); // 32x32
		this.mBranchDoubleEndedTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "branch_double_ended.png", 0, 64, 2, 1); // 32x32
		this.mBranchTripleEndedTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "branch_triple_ended.png", 0, 96, 2, 1); // 32x32
		this.mBranchRootTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "branch_root.png", 0, 128, 1, 1); // 32x32
		this.mBitmapTextureAtlas.load();
	}

	@Override
	protected Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mScene = new Scene();
		this.mScene.setBackground(new Background(0, 20, 20));

		showTree();

		this.mScene.setOnSceneTouchListener(new IOnSceneTouchListener() {
			@Override
			public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
				restartGame();
				return false;
			}
		});

		return this.mScene;
	}

	private void showTree() {

		int rowCount = CAMERA_HEIGHT / BranchEntity.BRANCH_HEIGHT;
		int columnCount = CAMERA_WIDTH / BranchEntity.BRANCH_WIDTH;

		int vertexCount = rowCount * columnCount;

		mBranchMatrix = new BranchEntity[rowCount][columnCount];
		mBranchCorrectAnswer = new BranchEntity.Side[rowCount][columnCount];

		generateLevelMatrix(rowCount, columnCount);

		BranchType branchType = BranchType.Root;
		BranchEntity.Side branchCorrectSide = BranchEntity.Side.Left;

		for(int i = 0 ; i < rowCount; ++i )
			for (int j = 0 ; j < columnCount ; ++j ) {
				int vertexNumber = i * columnCount + j;
				int summ = MathUtils.sum(resultMatrix[vertexNumber]);

				if(summ == 1) {
					branchType = BranchType.Leaf;
					if(vertexNumber != 0 && resultMatrix[vertexNumber][vertexNumber - 1] == 1)
						branchCorrectSide = BranchEntity.Side.Left;
					else if( vertexNumber < vertexCount - 1 && resultMatrix[vertexNumber][vertexNumber + 1] == 1)
						branchCorrectSide = BranchEntity.Side.Right;
					else if(i > 0 && resultMatrix[vertexNumber][vertexNumber - columnCount] == 1)
						branchCorrectSide = BranchEntity.Side.Top;
					else if(i < rowCount - 1 && resultMatrix[vertexNumber][vertexNumber + columnCount] == 1)
						branchCorrectSide = BranchEntity.Side.Bottom;
				}
				else if(summ == 3) {
					branchType = BranchType.TripleEnded;
					if(i == 0 || i > 0 && resultMatrix[vertexNumber][vertexNumber - columnCount] == 0)
						branchCorrectSide = BranchEntity.Side.Right;
					else if(i == rowCount - 1 || i < rowCount - 1 && resultMatrix[vertexNumber][vertexNumber + columnCount] == 0)
						branchCorrectSide = BranchEntity.Side.Left;
					else if(resultMatrix[vertexNumber][vertexNumber - 1] == 0)
						branchCorrectSide = BranchEntity.Side.Top;
					else if(resultMatrix[vertexNumber][vertexNumber + 1] == 0)
						branchCorrectSide = BranchEntity.Side.Bottom;
				}
				else if (summ == 2) {
					branchType = BranchType.DoubleEnded;
					if(vertexNumber == 0
							|| ( i==0 && resultMatrix[vertexNumber][vertexNumber + 1] == 1)
							|| ( i < rowCount - 1 && resultMatrix[vertexNumber][vertexNumber + 1] == 1 && resultMatrix[vertexNumber][vertexNumber + columnCount] == 1))
						branchCorrectSide = BranchEntity.Side.Right;
					else if(vertexNumber == vertexCount - 1
							|| ( i > 0 && resultMatrix[vertexNumber][vertexNumber - 1] == 1 && resultMatrix[vertexNumber][vertexNumber - columnCount] == 1)
							|| ( i == rowCount - 1 && resultMatrix[vertexNumber][vertexNumber - 1] == 1))
						branchCorrectSide = BranchEntity.Side.Left;
					else if ( (i > 0 && resultMatrix[vertexNumber][vertexNumber + 1] == 1 && resultMatrix[vertexNumber][vertexNumber - columnCount] == 1)
							|| ( i == rowCount - 1 && resultMatrix[vertexNumber][vertexNumber + 1] == 1))
						branchCorrectSide = BranchEntity.Side.Top;
					else if (( i==0 && resultMatrix[vertexNumber][vertexNumber - 1] == 1)
							|| ( i < rowCount - 1 && resultMatrix[vertexNumber][vertexNumber - 1] == 1 && resultMatrix[vertexNumber][vertexNumber + columnCount] == 1))
						branchCorrectSide = BranchEntity.Side.Bottom;

					if(j > 0 && j + 1  < columnCount) {
						if (resultMatrix[vertexNumber][vertexNumber - 1] == 1 && resultMatrix[vertexNumber][vertexNumber + 1] == 1) {
							branchType = BranchType.LongBranch;
							branchCorrectSide = BranchEntity.Side.Left;
						}
					}

					if(i > 0 && i + 1 < rowCount) {
						if (resultMatrix[vertexNumber - columnCount][vertexNumber] == 1 && resultMatrix[vertexNumber + columnCount][vertexNumber] == 1) {
							branchType = BranchType.LongBranch;
							branchCorrectSide = BranchEntity.Side.Top;
						}
					}

				}

				mBranchMatrix[i][j] = addBranch(branchType, j , i, CAMERA_HEIGHT );
				mBranchCorrectAnswer[i][j] = branchCorrectSide;
			}

		branchRoot.updateAliveState(BranchEntity.Side.Left);

	}

	private void showCorrectAnswer(int rowCount, int columnCount) {
		for(int i = 0; i < rowCount; ++i)
			for(int j = 0; j < columnCount; ++j)
				mBranchMatrix[i][j].setAnchorSide(mBranchCorrectAnswer[i][j]);

		branchRoot.updateAliveState(BranchEntity.Side.Left);
	}

	private void fill(int[] arr, int value) {
		for(int i = 0; i < arr.length; ++i)
			arr[i] = value;
	}

	private int getRandom(){
		return RANDOM.nextInt(RANDOM_STOP - RANDOM_START) + RANDOM_START;
	}

	private void generateLevelMatrix(int rowCount, int columnCount) {
		boolean showLog = false;
		int verticesCount = rowCount * columnCount;

		final int INF = Integer.MAX_VALUE / 2;

		int[][] matrix = new int[verticesCount][verticesCount];
		resultMatrix = new int[verticesCount][verticesCount];

		int[] treeEdges = new int[verticesCount];
		fill(treeEdges, -1);
		int[] edgesDegrees = new int[verticesCount];
		fill(edgesDegrees, 0);

		for(int i = 0 ; i < verticesCount; ++i) {
			fill(matrix[i], INF);
			fill(resultMatrix[i], 0);
		}

		for(int i = 0 ; i < verticesCount; ++i){
			if(i - 1 >= 0 && i % columnCount != 0)
				matrix[i][i - 1] = getRandom();

			if(i + 1 <verticesCount && i % columnCount != columnCount - 1)
				matrix[i][i + 1] = getRandom();

			if(i + columnCount < verticesCount)
				matrix[i][i + columnCount] = getRandom();

			if(i - columnCount >= 0)
				matrix[i][i - columnCount] = getRandom();
		}

		if(showLog) {
			System.out.print("matrix: \n");

			StringBuilder sb = new StringBuilder();
			for( int i = 0; i < verticesCount; ++i) {
				sb.setLength(0);
				for (int j = 0; j < verticesCount; ++j) {
					if(matrix[i][j] == INF)
						sb.append(0);
					else
						sb.append(matrix[i][j]);
					sb.append(" ");
				}
				System.out.print(sb.toString() + "\n");
			}

		}

		boolean[] used = new boolean [verticesCount]; // массив пометок
		int[] dist = new int [verticesCount]; // массив расстояния. dist[v] = вес_ребра(MST, v)
		fill(dist, INF); // устанаавливаем расстояние до всех вершин INF
		int startVertexNumber = 0;//(columnCount + rowCount) / 9;
		dist[startVertexNumber] = 0; // для начальной вершины положим 0
		treeEdges[startVertexNumber] = startVertexNumber;

		for (;;) {
			int v = -1;
			for (int nv = 0; nv < verticesCount; nv++) // перебираем вершины
				if (!used[nv] && dist[nv] < INF && (v == -1 || dist[v] > dist[nv])) { // выбираем самую близкую непомеченную вершину
					v = nv;
				}
			if (v == -1) break; // ближайшая вершина не найдена
			used[v] = true; // помечаем ее
			for (int to = 0; to < verticesCount; to++) {
				if (edgesDegrees[to] < 3 && edgesDegrees[v] < 3 && !used[to] && matrix[to][v] < INF) { // для всех непомеченных смежных
					if (dist[to] > matrix[to][v]) {
						dist[to] = matrix[to][v]; // улучшаем оценку расстояния (релаксация)
						if(treeEdges[to] == -1) {
							edgesDegrees[to]++;
						} else if (treeEdges[to] != to)
							edgesDegrees[treeEdges[to]]--;
						edgesDegrees[v]++;
						treeEdges[to] = v;
					}
				}
			}
		}

		if(showLog) {
			StringBuilder sb = new StringBuilder();

			System.out.print("result edges degrees: \n");
			for(int i =0; i< verticesCount; ++i) {
				if (edgesDegrees[i] == -1) {
					edgesDegrees[i] = 0;
				}
				System.out.println(i + " => " + edgesDegrees[i]);
			}

			System.out.print("result edges: \n");
			sb.setLength(0);
			for (int j = 0; j < verticesCount; ++j) {
				sb.append(j);
				sb.append(" => ");
				if(treeEdges[j] == -1) {
					System.out.print("tree not found restart: \n");
					generateLevelMatrix(rowCount, columnCount);
				}
				sb.append(treeEdges[j]);
				sb.append("\n");
			}
			System.out.print(sb.toString() + "\n");
		}

		for(int i = 0; i < verticesCount; ++i)
			if(treeEdges[i] != i) {
				resultMatrix[i][treeEdges[i]] = 1;
				resultMatrix[treeEdges[i]][i] = 1;
			}

		if(showLog) {
			StringBuilder sb = new StringBuilder();
			for( int i = 0; i < verticesCount; ++i) {
				sb.setLength(0);
				for (int j = 0; j < verticesCount; ++j) {
					sb.append(resultMatrix[i][j]);
				}
				System.out.print(sb.toString() + "\n");
			}
		}

	}

	private BranchEntity addBranch(BranchType branchType, int columnNumber, int rowNumber, int height) {
		BranchEntity branchEntity = createBranchEntity(branchType, columnNumber, rowNumber, height);
		this.mScene.registerTouchArea(branchEntity);
		this.mScene.attachChild(branchEntity);
		return branchEntity;
	}

	@Override
	public EngineOptions onCreateEngineOptions() {
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}

	@Override
	public void onBranchTouched() {
		deathAllBranches();
		branchRoot.updateAliveState(BranchEntity.Side.Left);

		//todo
		if(checkWin()) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(TreeGame.this, "You win!!!", Toast.LENGTH_SHORT).show();
				}
			});
			restartGame();
		}

	}

	private void restartGame() {
		branchRoot = null;
		removeTree();
		showTree();
	}

	private void removeTree() {
		for(int i = 0 ; i < mBranchMatrix.length; ++i)
			for (int j = 0; j < mBranchMatrix[i].length; ++j) {
				mScene.unregisterTouchArea(mBranchMatrix[i][j]);
				mBranchMatrix[i][j].detachSelf();
				mBranchMatrix[i][j].dispose();
			}
	}

	private void deathAllBranches() {
		for(int i = 0 ; i < mBranchMatrix.length; ++i)
			for (int j = 0; j < mBranchMatrix[i].length; ++j)
				mBranchMatrix[i][j].setAliveState(false);
	}

	private boolean checkWin() {
		for(int i = 0 ; i < mBranchMatrix.length; ++i)
			for (int j = 0; j < mBranchMatrix[i].length; ++j)
				if(!mBranchMatrix[i][j].isAlive())
					return false;

		return true;
	}

	private BranchEntity createBranchEntity(BranchType branchType, int columnNumber, int rowNumber, int height) {
		BranchEntity branchEntity = null;

		switch (branchType) {
            case Root:
			case Leaf:
				if(branchRoot == null) {
					branchRoot = new BranchRoot(
							columnNumber,
							rowNumber,
							height,
							mBranchRootTextureRegion, mVertexBufferObjectManager, this);
					branchEntity = branchRoot;
					break;
				}

				branchEntity = new BranchLeaf(
						columnNumber,
						rowNumber,
						height,
						mBranchLeafTextureRegion, mVertexBufferObjectManager, this);
				break;
			case LongBranch:
				branchEntity = new BranchLongEnded(
						columnNumber,
						rowNumber,
						height,
						mBranchLongTextureRegion, mVertexBufferObjectManager, this);
				break;
			case DoubleEnded:
				branchEntity = new BranchDoubleEnded(
						columnNumber,
						rowNumber,
						height,
						mBranchDoubleEndedTextureRegion, mVertexBufferObjectManager, this);
				break;
			case TripleEnded:
				branchEntity = new BranchTripleEnded(
						columnNumber,
						rowNumber,
						height,
						mBranchTripleEndedTextureRegion, mVertexBufferObjectManager, this);
				break;


		}

		return branchEntity;
	}

	@Override
	public BranchEntity getBranch(int i, int j) {
		if(i >= 0 && i < mBranchMatrix.length && j >= 0 && j < mBranchMatrix[0].length)
			return mBranchMatrix[i][j];
		else return null;
	}

	enum BranchType {
		Root,
		Leaf,
		LongBranch,
		DoubleEnded,
		TripleEnded
	}
}
