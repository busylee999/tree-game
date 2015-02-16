package com.busylee.treegame;

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

import java.io.IOException;
import java.util.Random;

/**
 * Created by busylee on 14.02.15.
 */
public class TreeGame extends SimpleBaseGameActivity implements ITreeMaster {

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	private BitmapTextureAtlas mBitmapTextureAtlas;

	private static BranchEntity[][] mBranchMatrix;

	private ITiledTextureRegion
			mBranchRoot,
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
		this.mBranchRoot = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "branch_root.png", 0, 128, 1, 1); // 32x32
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

		int rowCount = CAMERA_HEIGHT / BranchEntity.BRANCH_HEIGHT;
		int columnCount = CAMERA_WIDTH / BranchEntity.BRANCH_WIDTH;
		mBranchMatrix = new BranchEntity[rowCount][columnCount];

		Random random = new Random();

		for(int i = 0 ; i < rowCount; ++i )
			for (int j = 0 ; j < columnCount ; ++j ) {
				if(i == 4 && j == 4) {
					mBranchMatrix[i][j] = addBranch(BranchType.Root, j , i, CAMERA_HEIGHT);
					continue;
				}
				mBranchMatrix[i][j] = addBranch(BranchType.values()[random.nextInt(BranchType.values().length - 1) + 1], j , i, CAMERA_HEIGHT );
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
	}

	private void deathAllBranches() {
		for(int i = 0 ; i < mBranchMatrix.length; ++i)
			for (int j = 0; j < mBranchMatrix[i].length; ++j)
				mBranchMatrix[i][j].setAliveState(false);
	}

	private BranchEntity createBranchEntity(BranchType branchType, int columnNumber, int rowNumber, int height) {
		BranchEntity branchEntity = null;

		switch (branchType) {
            case Root:
				if(branchRoot == null) {
					branchRoot = new BranchRoot(
							columnNumber,
							rowNumber,
							height,
							mBranchRoot, mVertexBufferObjectManager, this);
					branchEntity = branchRoot;
					break;
				}
			case Leaf:
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
