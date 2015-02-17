package com.busylee.treegame;

import android.widget.Toast;
import com.busylee.treegame.branch.*;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.util.FPSLogger;
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

	private BitmapTextureAtlas mBitmapTextureAtlas;

	private BranchEntity[][] mBranchMatrix;

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

		return this.mScene;
	}

	private void showTree() {

		int rowCount = 3;
		int columnCount = 3;

		mBranchMatrix = new BranchEntity[rowCount][columnCount];

		generateLevelMatrix(rowCount, columnCount);

		BranchType branchType = BranchType.Root;

		System.out.print("branches: \n");

		for(int i = 0 ; i < rowCount; ++i )
			for (int j = 0 ; j < columnCount ; ++j ) {
				int vertNumber = i * rowCount + j;
				int summ = MathUtils.sum(resultMatrix[vertNumber]);

				if(summ == 1)
					branchType = BranchType.Leaf;
				else if(summ == 3)
					branchType = BranchType.TripleEnded;
				else if (summ == 2) {
					branchType = BranchType.DoubleEnded;
					if(j > 0 && j + 1  < columnCount)
						if(resultMatrix[vertNumber][vertNumber - 1] == 1 && resultMatrix[vertNumber][vertNumber + 1] == 1)
							branchType = BranchType.LongBranch;

					if(i > 0 && i + 1 < rowCount)
						if(resultMatrix[vertNumber][vertNumber - columnCount] == 1 && resultMatrix[vertNumber][vertNumber + columnCount] == 1)
							branchType = BranchType.LongBranch;
				}

				System.out.print(vertNumber + " => " + branchType + "(" + summ +")\n");

				mBranchMatrix[i][j] = addBranch(branchType, j , i, CAMERA_HEIGHT );
			}
	}

	private void fill(int[] arr, int value) {
		for(int i = 0; i < arr.length; ++i)
			arr[i] = value;
	}

	private void generateLevelMatrix(int rowCount, int columnCount) {
		int verticiesCount = rowCount * columnCount;

		int INF = Integer.MAX_VALUE / 2;

		int[][] matrix = new int[verticiesCount][verticiesCount];
		resultMatrix = new int[verticiesCount][verticiesCount];

		int[] treeEdges = new int[verticiesCount];

		Random random = new Random(System.currentTimeMillis());

		for(int i = 0 ; i < verticiesCount; ++i)
			for (int j = 0; j < verticiesCount; ++j) {
				matrix[i][j] = INF;
				int count = 0;
				if(i - 1 >= 0 && i % columnCount != 0) {
					matrix[i][i - 1] = random.nextInt(10) + 1;
					count++;
				}
				if(i + 1 <verticiesCount && i % columnCount != columnCount - 1) {
					matrix[i][i + 1] = random.nextInt(10) + 1;
					count++;
				}
				if(i + rowCount < verticiesCount) {
					matrix[i][i + rowCount] = random.nextInt(10) + 1;
					count++;
				}
				if(i - rowCount >= 0 && count < 3)
					matrix[i][i - rowCount] = random.nextInt(10) + 1;
			}

		System.out.print("matrix: \n");

		StringBuilder sb = new StringBuilder();
		for( int i = 0; i < verticiesCount; ++i) {
			sb.setLength(0);
			for (int j = 0; j < verticiesCount; ++j) {
				sb.append(matrix[i][j]);
				sb.append(" ");
			}
			System.out.print(sb.toString() + "\n");
		}

		boolean[] used = new boolean [verticiesCount]; // массив помето
		int[] dist = new int [verticiesCount]; // массив расстояния. dist[v] = вес_ребра(MST, v)
		fill(dist, INF); // устанаавливаем расстояние до всех вершин INF
		dist[0] = 0; // для начальной вершины положим 0

		int[] s = new int[verticiesCount];

		for (;;) {
			int v = -1;
			for (int nv = 0; nv < verticiesCount; nv++) // перебираем вершины
				if (!used[nv] && dist[nv] < INF && (v == -1 || dist[v] > dist[nv])) { // выбираем самую близкую непомеченную вершину
					v = nv;
				}
			if (v == -1) break; // ближайшая вершина не найдена
			used[v] = true; // помечаем ее
			for (int to = 0; to < verticiesCount; to++)
				if (!used[to] && matrix[v][to] < INF) { // для всех непомеченных смежных
					if(dist[to] > matrix[v][to]) {
						dist[to] = matrix[v][to]; // улучшаем оценку расстояния (релаксация)
						treeEdges[to] = v;
					}
				}
			}

		System.out.print("result edges: \n");

		sb.setLength(0);
		for (int j = 0; j < verticiesCount; ++j) {
			sb.append(j + " => ");
			sb.append(treeEdges[j]);
			sb.append("\n");
		}
		System.out.print(sb.toString() + "\n");

		for(int i = 0; i < verticiesCount; ++i)
			if(treeEdges[i] != i) {
				resultMatrix[i][treeEdges[i]] = 1;
				resultMatrix[treeEdges[i]][i] = 1;
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
